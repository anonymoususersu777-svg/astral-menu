package com.astral.menu;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ScreenCaptureService extends Service {
    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_RESULT_DATA = "resultData";

    private MediaProjection projection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private HandlerThread bgThread;
    private Handler bgHandler;
    private int screenW, screenH, density;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) { stopSelf(); return START_NOT_STICKY; }
        int rc = intent.getIntExtra(EXTRA_RESULT_CODE, -1);
        Intent data = intent.getParcelableExtra(EXTRA_RESULT_DATA);
        if (rc == -1 || data == null) { stopSelf(); return START_NOT_STICKY; }

        MediaProjectionManager mgr =
                (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        if (mgr == null) { stopSelf(); return START_NOT_STICKY; }
        projection = mgr.getMediaProjection(rc, data);
        if (projection == null) { stopSelf(); return START_NOT_STICKY; }

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null) wm.getDefaultDisplay().getRealMetrics(dm);
        screenW = dm.widthPixels; screenH = dm.heightPixels; density = dm.densityDpi;

        bgThread = new HandlerThread("astral-cap"); bgThread.start();
        bgHandler = new Handler(bgThread.getLooper());

        imageReader = ImageReader.newInstance(screenW, screenH,
                PixelFormat.RGBA_8888, 2);
        imageReader.setOnImageAvailableListener(this::onFrame, bgHandler);

        virtualDisplay = projection.createVirtualDisplay(
                "astral-vd", screenW, screenH, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, bgHandler);

        return START_STICKY;
    }

    private void onFrame(ImageReader reader) {
        Image img = null;
        try {
            img = reader.acquireLatestImage();
            if (img == null) return;
            ByteBuffer buf = img.getPlanes()[0].getBuffer();
            Bitmap bmp = Bitmap.createBitmap(screenW, screenH,
                    Bitmap.Config.ARGB_8888);
            buf.rewind();
            bmp.copyPixelsFromBuffer(buf);
            TargetTracker.update(findGreyMan(bmp));
            bmp.recycle();
        } catch (Exception ignored) {
        } finally { if (img != null) img.close(); }
    }

    // Серый человечек NoomiClone — ищем серые пятна
    private List<float[]> findGreyMan(Bitmap bmp) {
        List<float[]> out = new ArrayList<>();
        int step = 20;
        int w = bmp.getWidth(), h = bmp.getHeight();
        for (int y = (int)(h*0.25); y < (int)(h*0.85); y += step) {
            for (int x = (int)(w*0.15); x < (int)(w*0.85); x += step) {
                int p = bmp.getPixel(x, y);
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = p & 0xFF;
                // серый = r ≈ g ≈ b, значения 60-180
                int maxDiff = Math.max(Math.abs(r-g), Math.max(Math.abs(g-b), Math.abs(r-b)));
                if (maxDiff < 20 && r > 60 && r < 180) {
                    out.add(new float[]{x, y});
                }
            }
        }
        return out;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (virtualDisplay != null) virtualDisplay.release();
        if (projection != null) projection.stop();
        if (imageReader != null) imageReader.close();
        if (bgThread != null) bgThread.quitSafely();
    }
}
