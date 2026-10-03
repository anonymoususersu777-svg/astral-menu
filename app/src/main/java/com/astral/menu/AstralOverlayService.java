package com.astral.menu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AstralOverlayService extends Service {
    public static volatile boolean chinaHat  = false;
    public static volatile boolean fakeMoney = false;
    public static volatile boolean skins     = false;
    public static volatile int moneyValue    = 999999;

    private WindowManager wm;
    private View menuView;
    private VisualOverlayView visualView;
    private WindowManager.LayoutParams menuParams;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        startForeground(1, buildNotification());
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm == null) { stopSelf(); return; }
        try {
            visualView = new VisualOverlayView(this);
            WindowManager.LayoutParams vp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);
            wm.addView(visualView, vp);

            menuView = LayoutInflater.from(this).inflate(R.layout.astral_menu, null);
            menuParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            menuParams.gravity = Gravity.TOP | Gravity.START;
            menuParams.x = 40; menuParams.y = 120;
            wm.addView(menuView, menuParams);
            bindMenu();
        } catch (Exception e) {
            Toast.makeText(this, "Overlay: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    private void bindMenu() {
        CheckBox cbHat   = menuView.findViewById(R.id.cbChinaHat);
        CheckBox cbMoney = menuView.findViewById(R.id.cbFakeMoney);
        CheckBox cbSkins = menuView.findViewById(R.id.cbSkins);
        SeekBar  sbMoney = menuView.findViewById(R.id.sbMoney);
        TextView tvVal   = menuView.findViewById(R.id.tvMoneyVal);
        TextView close   = menuView.findViewById(R.id.btnClose);

        cbHat.setOnCheckedChangeListener((v, c)   -> chinaHat  = c);
        cbMoney.setOnCheckedChangeListener((v, c) -> fakeMoney = c);
        cbSkins.setOnCheckedChangeListener((v, c) -> skins     = c);

        sbMoney.setProgress(moneyValue);
        tvVal.setText(String.format("$%,d", moneyValue));
        sbMoney.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                moneyValue = p; tvVal.setText(String.format("$%,d", p));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        close.setOnClickListener(v -> stopSelf());
        makeDraggable(menuView.findViewById(R.id.header));
    }

    private void makeDraggable(View handle) {
        handle.setOnTouchListener(new View.OnTouchListener() {
            float dx, dy;
            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dx = e.getRawX() - menuParams.x;
                        dy = e.getRawY() - menuParams.y;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        menuParams.x = (int)(e.getRawX() - dx);
                        menuParams.y = (int)(e.getRawY() - dy);
                        wm.updateViewLayout(menuView, menuParams);
                        return true;
                }
                return false;
            }
        });
    }

    private Notification buildNotification() {
        String chId = "astral";
        NotificationManager nm =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.createNotificationChannel(new NotificationChannel(
                    chId, "Astral", NotificationManager.IMPORTANCE_LOW));
        }
        return new Notification.Builder(this, chId)
                .setContentTitle("Astral active")
                .setContentText("Menu is running")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .build();
    }

    @Override public void onDestroy() {
        super.onDestroy();
        try {
            if (menuView   != null) wm.removeView(menuView);
            if (visualView != null) wm.removeView(visualView);
        } catch (Exception ignored) {}
        menuView = null; visualView = null;
        TargetTracker.clear();
    }
}
