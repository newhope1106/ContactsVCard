package com.newhope.vcard.common;

import com.newhope.vcard.R;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.newhope.vcard.model.ImageModel;
import com.newhope.vcard.utils.BitmapUtil;
import com.newhope.vcard.utils.ImageLoaderUtils;

import android.util.Log;
import android.content.ComponentCallbacks2;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

public abstract class ImageLoaderManager implements ComponentCallbacks2{
	
	private static ImageLoaderManagerImpl sLoaderManger;

	public abstract void loadPhoto(ImageView imageView, Uri imageUri);
	
	/**
     * Remove photo from the supplied image view. This also cancels current pending load request
     * inside this photo manager.
     */
    public abstract void removePhoto(ImageView view);

    /**
     * Temporarily stops loading photos from the database.
     */
    public abstract void pause();

    /**
     * Resumes loading photos from the database.
     */
    public abstract void resume();

    /**
     * Marks all cached photos for reloading.  We can continue using cache but should
     * also make sure the photos haven't changed in the background and notify the views
     * if so.
     */
    public abstract void refreshCache();
    
    public static synchronized ImageLoaderManager getInstance(Context context) {

    	if (sLoaderManger == null) {
    		sLoaderManger = new ImageLoaderManagerImpl(context);
    	}
    	return sLoaderManger;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    // ComponentCallbacks2
    @Override
    public void onLowMemory() {
    }

    // ComponentCallbacks2
    @Override
    public void onTrimMemory(int level) {
    } 
}

class ImageLoaderManagerImpl extends ImageLoaderManager implements Callback{
	private final static String TAG = "ImageLoaderManagerImpl";
	
	/**
     * Type of message sent by the UI thread to itself to indicate that some photos
     * need to be loaded.
     */
    private static final int MESSAGE_REQUEST_LOADING = 1;
    
    /**
     * Type of message sent by the loader thread to indicate that some photos have
     * been loaded.
     */
    private static final int MESSAGE_PHOTOS_LOADED = 2;
    
    /**
     * Maintains the state of a particular photo.
     */
    private static class BitmapHolder {
        final byte[] bytes;

        volatile boolean fresh;
        Bitmap bitmap;
        Reference<Bitmap> bitmapRef;

        public BitmapHolder(byte[] bytes) {
            this.bytes = bytes;
            this.fresh = true;
        }
    }
    
    private final Context mContext;
    
    private static int sImageSize;
    
    private static int DEFAULT_RES_ICON_ID = R.drawable.default_icon;

    /**
     * An LRU cache for bitmap holders. The cache contains bytes for photos just
     * as they come from the database. Each holder has a soft reference to the
     * actual bitmap.
     */
    private final LruCache<Object, BitmapHolder> mBitmapHolderCache;

    /**
     * {@code true} if ALL entries in {@link #mBitmapHolderCache} are NOT fresh.
     */
    private volatile boolean mBitmapHolderCacheAllUnfresh = true;
    
    /**
     * Level 2 LRU cache for bitmaps. This is a smaller cache that holds
     * the most recently used bitmaps to save time on decoding
     * them from bytes (the bytes are stored in {@link #mBitmapHolderCache}.
     */
    private final LruCache<Object, Bitmap> mBitmapCache;

    /**
     * A map from ImageView to the corresponding photo ID or uri, encapsulated in a request.
     * The request may swapped out before the photo loading request is started.
     */
    private final ConcurrentHashMap<ImageView, ImageModel> mPendingRequests =
            new ConcurrentHashMap<ImageView, ImageModel>();

    /**
     * Handler for messages sent to the UI thread.
     */
    private final Handler mMainThreadHandler = new Handler(this);

    /**
     * Thread responsible for loading photos from the database. Created upon
     * the first request.
     */
    private LoaderThread mLoaderThread;

    /**
     * A gate to make sure we only send one instance of MESSAGE_PHOTOS_NEEDED at a time.
     */
    private boolean mLoadingRequested;

    /**
     * Flag indicating if the image loading is paused.
     */
    private boolean mPaused;

    /** Cache size for {@link #mBitmapHolderCache} for devices with "large" RAM. */
    private static final int HOLDER_CACHE_SIZE = 80000000;

    /** Cache size for {@link #mBitmapCache} for devices with "large" RAM. */
    private static final int BITMAP_CACHE_SIZE = 36864 * 48 * 40; // 1728*40K
    
    public ImageLoaderManagerImpl(Context context) {
    	mContext = context;
    	final float cacheSizeAdjustment = 1.0f;
    	final int bitmapCacheSize = (int) (cacheSizeAdjustment * BITMAP_CACHE_SIZE);
        mBitmapCache = new LruCache<Object, Bitmap>(bitmapCacheSize) {
            @Override 
            protected int sizeOf(Object key, Bitmap value) {
                return value.getByteCount();
            }

            @Override 
            protected void entryRemoved(
                    boolean evicted, Object key, Bitmap oldValue, Bitmap newValue) {
            }
        };
        final int holderCacheSize = (int) (cacheSizeAdjustment * HOLDER_CACHE_SIZE);
        mBitmapHolderCache = new LruCache<Object, BitmapHolder>(holderCacheSize) {
            @Override 
            protected int sizeOf(Object key, BitmapHolder value) {
                return value.bytes != null ? value.bytes.length : 0;
            }

            @Override 
            protected void entryRemoved(
                    boolean evicted, Object key, BitmapHolder oldValue, BitmapHolder newValue) {
            }
        };

        sImageSize = mContext.getResources().getDimensionPixelSize(R.dimen.image_list_item_size);
        
        Log.i(TAG, "Cache adj: " + cacheSizeAdjustment);
    }
    
    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory: " + level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            // Clear the caches.  Note all pending requests will be removed too.
            clear();
        }
    }
    
    @Override
	public void loadPhoto(ImageView imageView, Uri imageUri) {
    	if (imageUri == null) {
    		applyDefaultImageView(imageView);
    	} else {
    		ImageModel imageModel = ImageModel.createFromUri(imageUri, null);
    		boolean loaded = loadCachedPhoto(imageView, imageModel);
            if (loaded) {
                mPendingRequests.remove(imageView);
            } else {
                mPendingRequests.put(imageView, imageModel);
                if (!mPaused) {
                    // Send a request to start loading photos
                    requestLoading();
                }
            }
    	}
	}
    
    @Override
    public void removePhoto(ImageView view) {
        view.setImageDrawable(null);
        mPendingRequests.remove(view);
    }

    @Override
    public void refreshCache() {
        if (mBitmapHolderCacheAllUnfresh) {
            Log.d(TAG, "refreshCache -- no fresh entries.");
            return;
        }
        Log.d(TAG, "refreshCache");
        mBitmapHolderCacheAllUnfresh = true;
        for (BitmapHolder holder : mBitmapHolderCache.snapshot().values()) {
            holder.fresh = false;
        }
    }
    
    /**
     * Checks if the photo is present in cache.  If so, sets the photo on the view.
     *
     * @return false if the photo needs to be (re)loaded from the provider.
     */
    private boolean loadCachedPhoto(ImageView view, ImageModel imageModel) {
        BitmapHolder holder = mBitmapHolderCache.get(imageModel.getKey());
        
        if (holder == null) {
            // The bitmap has not been loaded ==> show default avatar
        	applyDefaultImageView(view);
            return false;
        }

        if (holder.bytes == null) {
        	applyDefaultImageView(view);
            return holder.fresh;
        }

        Bitmap cachedBitmap = holder.bitmapRef == null ? null : holder.bitmapRef.get();
        if (cachedBitmap == null) {
            if (holder.bytes.length < 8 * 1024) {
                inflateBitmap(holder);
                cachedBitmap = holder.bitmap;
                if (cachedBitmap == null) return false;
            } else {
                // This is bigger data. Let's send that back to the Loader so that we can
                // inflate this in the background
            	applyDefaultImageView(view);
                return false;
            }
        }
        
        view.setImageBitmap(cachedBitmap);

        // Put the bitmap in the LRU cache. But only do this for images that are small enough
        // (we require that at least six of those can be cached at the same time)
        if (cachedBitmap.getByteCount() < mBitmapCache.maxSize() / 6) {
            mBitmapCache.put(imageModel.getKey(), cachedBitmap);
        }

        // Soften the reference
        holder.bitmap = null;

        return holder.fresh;
    }
    
    /**
     * If necessary, decodes bytes stored in the holder to Bitmap.  As long as the
     * bitmap is held either by {@link #mBitmapCache} or by a soft reference in
     * the holder, it will not be necessary to decode the bitmap.
     */
    private static void inflateBitmap(BitmapHolder holder) {
        byte[] bytes = holder.bytes;
        if (bytes == null || bytes.length == 0) {
            return;
        }

        // Check the soft reference.  If will be retained if the bitmap is also
        // in the LRU cache, so we don't need to check the LRU cache explicitly.
        if (holder.bitmapRef != null) {
            holder.bitmap = holder.bitmapRef.get();
            if (holder.bitmap != null) {
                return;
            }
        }

        try {
            Bitmap bitmap = BitmapUtil.decodeBitmapFromBytes(bytes, 1);
            bitmap = BitmapUtil.resizeBitMap(bitmap, sImageSize);
            holder.bitmap = bitmap;
            holder.bitmapRef = new SoftReference<Bitmap>(bitmap);
        } catch (OutOfMemoryError e) {
            // Do nothing - the photo will appear to be missing
        }
    }
    
    public void clear() {
        Log.d(TAG, "clear");
        mPendingRequests.clear();
        mBitmapHolderCache.evictAll();
        mBitmapCache.evictAll();
    }
    
    @Override
    public void pause() {
        mPaused = true;
    }

    @Override
    public void resume() {
        mPaused = false;
        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }
    
    /**
     * Sends a message to this thread itself to start loading images.  If the current
     * view contains multiple image views, all of those image views will get a chance
     * to request their respective photos before any of those requests are executed.
     * This allows us to load images in bulk.
     */
    private void requestLoading() {
        if (!mLoadingRequested) {
            mLoadingRequested = true;
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
	        case MESSAGE_REQUEST_LOADING: {
	            mLoadingRequested = false;
	            if (!mPaused) {
	                ensureLoaderThread();
	                mLoaderThread.requestLoading();
	            }
	            return true;
	        }
	
	        case MESSAGE_PHOTOS_LOADED: {
	            if (!mPaused) {
	                processLoadedImages();
	            }
	            return true;
	        }
		}
	    return false;
	}
	
	public void ensureLoaderThread() {
        if (mLoaderThread == null) {
            mLoaderThread = new LoaderThread(mContext.getContentResolver());
            mLoaderThread.start();
        }
    }
	
	/**
     * Goes over pending loading requests and displays loaded photos.  If some of the
     * photos still haven't been loaded, sends another request for image loading.
     */
    private void processLoadedImages() {
        Iterator<ImageView> iterator = mPendingRequests.keySet().iterator();
        while (iterator.hasNext()) {
            ImageView view = iterator.next();
            ImageModel key = mPendingRequests.get(view);
            boolean loaded = loadCachedPhoto(view, key);
            if (loaded) {
                iterator.remove();
            }
        }

        softenCache();

        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }
    
    /**
     * Removes strong references to loaded bitmaps to allow them to be garbage collected
     * if needed.  Some of the bitmaps will still be retained by {@link #mBitmapCache}.
     */
    private void softenCache() {
        for (BitmapHolder holder : mBitmapHolderCache.snapshot().values()) {
            holder.bitmap = null;
        }
    }
    
    /**
     * Stores the supplied bitmap in cache.
     */
    private void cacheBitmap(Object key, byte[] bytes, boolean preloading) {
        BitmapHolder holder = new BitmapHolder(bytes);

        // Unless this image is being preloaded, decode it right away while
        // we are still on the background thread.
        if (!preloading) {
            inflateBitmap(holder);
        }

        mBitmapHolderCache.put(key, holder);
        mBitmapHolderCacheAllUnfresh = false;
    }
    
    /**
     * Populates an array of photo IDs that need to be loaded. Also decodes bitmaps that we have
     * already loaded
     */
    private void obtainPhotoUrisToLoad(Set<ImageModel> uris) {
        uris.clear();

        boolean jpegsDecoded = false;

        /*
         * Since the call is made from the loader thread, the map could be
         * changing during the iteration. That's not really a problem:
         * ConcurrentHashMap will allow those changes to happen without throwing
         * exceptions. Since we may miss some requests in the situation of
         * concurrent change, we will need to check the map again once loading
         * is complete.
         */
        Iterator<ImageModel> iterator = mPendingRequests.values().iterator();
        while (iterator.hasNext()) {
        	ImageModel request = iterator.next();
            final BitmapHolder holder = mBitmapHolderCache.get(request.getKey());
            if (holder != null && holder.bytes != null && holder.fresh &&
                    (holder.bitmapRef == null || holder.bitmapRef.get() == null)) {
                // This was previously loaded but we don't currently have the inflated Bitmap
                inflateBitmap(holder);
                jpegsDecoded = true;
            } else {
                if (holder == null || !holder.fresh) {
                	uris.add(request);
                }
            }
        }

        if (jpegsDecoded){
        	mMainThreadHandler.sendEmptyMessage(MESSAGE_PHOTOS_LOADED);
        }
    }
    
    /**
     * The thread that performs loading of photos from the database.
     */
    private class LoaderThread extends HandlerThread implements Callback {
        private static final int BUFFER_SIZE = 1024*16;
        private static final int MESSAGE_LOAD_PHOTOS = 1;

        private final ContentResolver mResolver;
        private final Set<ImageModel> mPhotoUris = new HashSet<ImageModel>();

        private Handler mLoaderThreadHandler;

        public LoaderThread(ContentResolver resolver) {
            super(TAG);
            mResolver = resolver;
        }

        public void ensureHandler() {
            if (mLoaderThreadHandler == null) {
                mLoaderThreadHandler = new Handler(getLooper(), this);
            }
        }
        
        /**
         * Sends a message to this thread to load requested photos.  Cancels a preloading
         * request, if any: we don't want preloading to impede loading of the photos
         * we need to display now.
         */
        public void requestLoading() {
            ensureHandler();
            mLoaderThreadHandler.sendEmptyMessage(MESSAGE_LOAD_PHOTOS);
        }

        /**
         * Receives the above message, loads photos and then sends a message
         * to the main thread to process them.
         */
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_LOAD_PHOTOS:
                    loadPhotosInBackground();
                    break;
            }
            return true;
        }

       
        private void loadPhotosInBackground() {
            obtainPhotoUrisToLoad(mPhotoUris);
            loadUriBasedPhotos();
        }

        /**
         * Loads photos referenced with Uris. Those can be remote thumbnails
         * (from directory searches), display photos etc
         */
        private void loadUriBasedPhotos() {
            for (ImageModel uriRequest : mPhotoUris) {
                Uri uri = uriRequest.uri;
                
                byte[] imageBytes = ImageLoaderUtils.loadImages(mResolver, uri);
                if (imageBytes != null) {
                	cacheBitmap(uri, imageBytes, false);
                	mMainThreadHandler.sendEmptyMessage(MESSAGE_PHOTOS_LOADED);
                } else {
                	cacheBitmap(uri, null, false);
                }
            }
        }
    }
	
	private void applyDefaultImageView(ImageView imageView) {
		Bitmap bmp = BitmapUtil.readBitMap(mContext, DEFAULT_RES_ICON_ID);
		bmp = BitmapUtil.resizeBitMap(bmp, sImageSize);
		imageView.setImageBitmap(bmp);
	}
}
