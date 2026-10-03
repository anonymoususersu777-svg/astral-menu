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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_OVERLAY = 1001;
    private static final int REQ_CAPTURE = 1002;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(60, 120, 60, 60);

        TextView title = new TextView(this);
        title.setText("ASTRAL MENU"); title.setTextSize(28f);
        title.setTextColor(0xFFD32F2F); title.setPadding(0, 0, 0, 40);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("1. Разрешение overlay\n2. Разрешение захвата\n3. Запустить меню\n4. Свернуть и открыть игру");
        info.setTextColor(0xFFEDEDED); info.setTextSize(15f);
        info.setPadding(0, 0, 0, 50);
        root.addView(info);

        Button b1 = new Button(this); b1.setText("1. Разрешение overlay");
        b1.setOnClickListener(v -> requestOverlay()); root.addView(b1);
        Button b2 = new Button(this); b2.setText("2. Разрешение захвата");
        b2.setOnClickListener(v -> requestCapture()); root.addView(b2);
        Button b3 = new Button(this); b3.setText("3. Запустить Astral");
        b3.setOnClickListener(v -> startAstral()); root.addView(b3);

        setContentView(root);
    }

    private void requestOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())), REQ_OVERLAY);
            } else Toast.makeText(this, "Уже разрешено",
                    Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Сначала overlay", Toast.LENGTH_SHORT).show();
            return;
        }
        startForegroundService(new Intent(this, AstralOverlayService.class));
        Toast.makeText(this, "Astral запущен", Toast.LENGTH_SHORT).show();
        moveTaskToBack(true);
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
