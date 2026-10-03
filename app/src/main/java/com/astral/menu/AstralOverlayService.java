package com.astral.menu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class AstralOverlayService extends Service {
    public static volatile boolean chinaHat  = false;
    public static volatile boolean fps120    = false;
    public static volatile boolean smooth    = false;
    public static volatile boolean noLag     = false;
    public static volatile boolean stretch43 = false;

    private WindowManager wm;
    private View menuView, fabView, visualView;
    private WindowManager.LayoutParams menuParams, fabParams;

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
            menuParams.x = 40; menuParams.y = 200;
            wm.addView(menuView, menuParams);

            fabView = buildFab();
            fabParams = new WindowManager.LayoutParams(
                    dp(52), dp(52),
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            fabParams.gravity = Gravity.TOP | Gravity.START;
            fabParams.x = 20; fabParams.y = 200;
            wm.addView(fabView, fabParams);

            bindMenu();
            bindFab();
            menuView.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(this, "Overlay: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private View buildFab() {
        TextView fab = new TextView(this);
        fab.setText("A");
        fab.setTextColor(0xFFFFFFFF);
        fab.setTextSize(20f);
        fab.setGravity(Gravity.CENTER);
        fab.setTypeface(null, Typeface.BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(0xFFD32F2F);
        bg.setStroke(dp(2), 0xFFFFFFFF);
        fab.setBackground(bg);
        return fab;
    }

    private void bindFab() {
        final boolean[] moved = {false};
        fabView.setOnTouchListener(new View.OnTouchListener() {
            float dx, dy, startX, startY;
            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dx = e.getRawX() - fabParams.x;
                        dy = e.getRawY() - fabParams.y;
                        startX = e.getRawX(); startY = e.getRawY();
                        moved[0] = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(e.getRawX() - startX) > dp(6)
                                || Math.abs(e.getRawY() - startY) > dp(6)) {
                            moved[0] = true;
                        }
                        fabParams.x = (int) (e.getRawX() - dx);
                        fabParams.y = (int) (e.getRawY() - dy);
                        wm.updateViewLayout(fabView, fabParams);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved[0]) {
                            menuView.setVisibility(
                                    menuView.getVisibility() == View.VISIBLE
                                            ? View.GONE : View.VISIBLE);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void bindMenu() {
        CheckBox cbHat     = menuView.findViewById(R.id.cbChinaHat);
        CheckBox cbFps     = menuView.findViewById(R.id.cbFps);
        CheckBox cbSm      = menuView.findViewById(R.id.cbSmooth);
        CheckBox cbNoLag   = menuView.findViewById(R.id.cbNoLag);
        CheckBox cbStretch = menuView.findViewById(R.id.cbStretch);
        TextView close     = menuView.findViewById(R.id.btnClose);
        TextView hide      = menuView.findViewById(R.id.btnHide);

        cbHat.setOnCheckedChangeListener((v, c)     -> chinaHat  = c);
        cbFps.setOnCheckedChangeListener((v, c)     -> fps120    = c);
        cbSm.setOnCheckedChangeListener((v, c)      -> smooth    = c);
        cbNoLag.setOnCheckedChangeListener((v, c)   -> noLag     = c);
        cbStretch.setOnCheckedChangeListener((v, c) -> stretch43 = c);

        close.setOnClickListener(v -> menuView.setVisibility(View.GONE));
        hide.setOnClickListener(v  -> menuView.setVisibility(View.GONE));

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
                        menuParams.x = (int) (e.getRawX() - dx);
                        menuParams.y = (int) (e.getRawY() - dy);
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
                .setContentTitle("Astral v0.1.0 beta")
                .setContentText("Menu is running")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .build();
    }

    @Override public void onDestroy() {
        super.onDestroy();
        try {
            if (menuView   != null) wm.removeView(menuView);
            if (fabView    != null) wm.removeView(fabView);
            if (visualView != null) wm.removeView(visualView);
        } catch (Exception ignored) {}
        menuView = fabView = visualView = null;
        TargetTracker.clear();
    }
}
