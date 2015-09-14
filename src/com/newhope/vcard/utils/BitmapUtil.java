package com.newhope.vcard.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;

public class BitmapUtil {
	
	/**
     * Decodes the bitmap with the given sample size
     */
    public static Bitmap decodeBitmapFromBytes(byte[] bytes, int sampleSize) {
        final BitmapFactory.Options options;
        if (sampleSize <= 1) {
            options = null;
        } else {
            options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }
	
	/**
	 * 圆角正方形
	 * */
	public static Bitmap getRoundRectBitmap(Bitmap bitmap, int size){
        if(bitmap == null){
            return null;
        }
        
        bitmap = resizeBitMap(bitmap, size);
        
        Bitmap roundRectBitmap = Bitmap.createBitmap(bitmap.getWidth(),  
                bitmap.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(roundRectBitmap);  
        final int color = 0xff424242;  
        final Paint paint = new Paint();  
        final Rect rect = new Rect(0, 0, size, size);  
        final RectF rectF = new RectF(rect);  
        final float roundPx = 20;  
        paint.setAntiAlias(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        paint.setColor(color);  
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
        canvas.drawBitmap(bitmap, rect, rect, paint);  
        return roundRectBitmap;
    }
	
	public static Bitmap getRoundBitmap(Bitmap bitmap, int size){
        if(bitmap == null){
            return null;
        }
        
        bitmap = resizeBitMap(bitmap, size);
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        float left, top, right, bottom;  
        
        if (width <= height) {  
            if(width > size){
                left = (width - size)/2;
                top = (height - size)/2;
                right = (width + size)/2;
                bottom = (height + size)/2;
            }else{
                left = 0;
                top = (height - width)/2;
                right = width;
                bottom = (height + width)/2;
            } 
        } else {  
            if(height > size){
                left = (width - size)/2;
                top = (height - size)/2;
                right = (width + size)/2;
                bottom = (height + size)/2;
            }else{
                left = (height - width)/2;
                top = 0;
                right = (width + size)/2;
                bottom = height;
            }   
        }  
  
        Bitmap roundBitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);  
        Canvas canvas = new Canvas(roundBitmap);  
  
        final int color = 0xff424242;  
        final Paint paint = new Paint();  
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);//截取图片中间的模块  
        final Rect dst = new Rect(0, 0, size, size);
        //final RectF rectF = new RectF(dst);  
  
        paint.setAntiAlias(true);// 设置画笔无锯齿  
  
        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas  
        paint.setColor(color);  
  
        // 以下有两种方法画圆,drawRounRect和drawCircle  
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。  
        canvas.drawCircle(size/2, size/2, size/2, paint);  
  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452  
        canvas.drawBitmap(bitmap, src, dst, paint); //以Mode.SRC_IN模式合并bitmap和已经draw了的
        
        return roundBitmap;
    }
	
	/**
	 * 圆形
	 * */
	public static Bitmap decodeRoundBitmapFromBytes(byte[] bytes,int sampleSize, int size) {
    	final BitmapFactory.Options options;
        if (sampleSize <= 1) {
            options = null;
        } else {
            options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        bitmap = resizeBitMap(bitmap, size);
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        float left, top, right, bottom;  
        
        if (width <= height) {  
            if(width > size){
                left = (width - size)/2;
                top = (height - size)/2;
                right = (width + size)/2;
                bottom = (height + size)/2;
            }else{
                left = 0;
                top = (height - width)/2;
                right = width;
                bottom = (height + width)/2;
            } 
        } else {  
            if(height > size){
                left = (width - size)/2;
                top = (height - size)/2;
                right = (width + size)/2;
                bottom = (height + size)/2;
            }else{
                left = (height - width)/2;
                top = 0;
                right = (width + size)/2;
                bottom = height;
            }   
        }  
  
        Bitmap roundBitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);  
        Canvas canvas = new Canvas(roundBitmap);  
  
        final int color = 0xff424242;  
        final Paint paint = new Paint();  
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);//截取图片中间的模块  
        final Rect dst = new Rect(0, 0, size, size);
  
        paint.setAntiAlias(true);// 设置画笔无锯齿  
  
        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas  
        paint.setColor(color);  

        canvas.drawCircle(size/2, size/2, size/2, paint);  
  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
        canvas.drawBitmap(bitmap, src, dst, paint);
        
        return roundBitmap;
    }
	
	public static Bitmap resizeBitMap(Bitmap map, int size){
        Matrix mt = new Matrix();
        float scaleW = 1;//横向缩放系数，1表示不变
        float scaleH = 1;//纵向缩放系数，1表示不变        
        //设置图片缩小比例
        double scale = ((double)size)/map.getWidth();
        //计算出这次要缩小的比例
        scaleW = (float)(scaleW*scale);
        scaleH = (float)(scaleH*scale);
        mt.postScale(scaleW,scaleH);
        return Bitmap.createBitmap(map,0,0,map.getWidth(),map.getHeight(),mt,true);
    }
}
