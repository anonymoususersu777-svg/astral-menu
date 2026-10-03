package com.astral.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_OVERLAY = 1001;
    private static final int REQ_CAPTURE = 1002;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        Button bOverlay = findViewById(R.id.btnOverlay);
        Button bCapture = findViewById(R.id.btnCapture);
        Button bStart   = findViewById(R.id.btnStart);
        Button bStop    = findViewById(R.id.btnStop);

        bOverlay.setOnClickListener(v -> requestOverlay());
        bCapture.setOnClickListener(v -> requestCapture());
        bStart.setOnClickListener(v   -> startAstral());
        bStop.setOnClickListener(v    -> stopAstral());
    }

    private void requestOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())), REQ_OVERLAY);
            } else {
                Toast.makeText(this, "Overlay уже разрешён",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestCapture() {
        MediaProjectionManager mgr =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mgr != null)
            startActivityForResult(mgr.createScreenCaptureIntent(), REQ_CAPTURE);
    }

    private void startAstral() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Сначала дай разрешение overlay",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        startForegroundService(new Intent(this, AstralOverlayService.class));
        Toast.makeText(this, "Меню запущено. Открывай игру.",
                Toast.LENGTH_LONG).show();
        moveTaskToBack(true);
    }

    private void stopAstral() {
        stopService(new Intent(this, AstralOverlayService.class));
        stopService(new Intent(this, ScreenCaptureService.class));
        Toast.makeText(this, "Меню выключено", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_CAPTURE && res == RESULT_OK && data != null) {
            Intent svc = new Intent(this, ScreenCaptureService.class);
            svc.putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, res);
            svc.putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, data);
            startForegroundService(svc);
        }
    }
}
