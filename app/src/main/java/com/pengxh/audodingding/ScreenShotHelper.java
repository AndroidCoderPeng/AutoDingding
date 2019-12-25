package com.pengxh.audodingding;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Handler;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:35
 */
public class ScreenShotHelper {
    interface OnScreenShotListener {
        void onFinish(Bitmap bitmap);
    }

    private OnScreenShotListener mOnScreenShotListener;

    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private final SoftReference<Context> mRefContext;

    public ScreenShotHelper(Context context, int resultCode, Intent data, OnScreenShotListener onScreenShotListener) {
        this.mOnScreenShotListener = onScreenShotListener;
        this.mRefContext = new SoftReference<>(context);

        mMediaProjection = getMediaProjectionManager().getMediaProjection(resultCode, data);
        mImageReader = ImageReader.newInstance(getScreenWidth(), getScreenHeight(), PixelFormat.RGBA_8888, 1);
    }

    public void startScreenShot() {
        createVirtualDisplay();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new CreateBitmapTask().execute();
            }
        }, 1000);
    }

    public class CreateBitmapTask extends AsyncTask<Image, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Image... params) {
            Image image = mImageReader.acquireLatestImage();
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();

            int pixelStride = planes[0].getPixelStride();

            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mVirtualDisplay.release();
            mMediaProjection.stop();

            if (mOnScreenShotListener != null) {
                mOnScreenShotListener.onFinish(bitmap);
            }
        }
    }

    private MediaProjectionManager getMediaProjectionManager() {
        return (MediaProjectionManager) getContext().getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
    }

    private void createVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                "screen-mirror",
                getScreenWidth(),
                getScreenHeight(),
                Resources.getSystem().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(),
                null,
                null
        );
    }

    private Context getContext() {
        return mRefContext.get();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
