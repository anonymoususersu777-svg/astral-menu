package com.astral.menu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

public class VisualOverlayView extends View {
    private final Paint redPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public VisualOverlayView(Context c) {
        super(c);
        redPaint.setColor(0xFFD32F2F);  redPaint.setStyle(Paint.Style.FILL);
        goldPaint.setColor(0xFFFFD700); goldPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(38f);
        textPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        textPaint.setShadowLayer(6f, 0f, 0f, 0xFFD32F2F);
    }

    @Override protected void onDraw(Canvas cv) {
        super.onDraw(cv);
        if (AstralOverlayService.chinaHat) {
            for (float[] t : TargetTracker.getTargets()) {
                if (t == null || t.length < 2) continue;
                drawChinaHat(cv, t[0], t[1] - 160f);
            }
        }
        if (AstralOverlayService.fakeMoney) drawFakeMoney(cv);
        if (AstralOverlayService.skins)     drawSkins(cv);
        postInvalidateOnAnimation();
    }

    private void drawChinaHat(Canvas cv, float cx, float cy) {
        float hatW = 90f, hatH = 70f;
        RectF hat = new RectF(cx - hatW/2f, cy - hatH, cx + hatW/2f, cy);
        Path cone = new Path();
        cone.moveTo(cx, hat.top);
        cone.lineTo(hat.left,  hat.bottom - 14f);
        cone.lineTo(hat.right, hat.bottom - 14f);
        cone.close();
        cv.drawPath(cone, redPaint);
        RectF brim = new RectF(hat.left - 14f, hat.bottom - 14f,
                               hat.right + 14f, hat.bottom);
        cv.drawOval(brim, redPaint);
        cv.drawPath(buildStar(cx, hat.bottom - 30f, 12f), goldPaint);
    }

    private void drawFakeMoney(Canvas cv) {
        String money = String.format("$%,d", AstralOverlayService.moneyValue);
        float x = getWidth() - textPaint.measureText(money) - 60f;
        textPaint.setColor(0xFF2E7D32);
        cv.drawText(money, x, 140f, textPaint);
    }

    private void drawSkins(Canvas cv) {
        float baseY = getHeight() - 140f;
        textPaint.setColor(0xFFD32F2F); textPaint.setTextSize(34f);
        cv.drawText("AK-47 | Astral Redline", 60f, baseY, textPaint);
        textPaint.setColor(0xFFEDEDED); textPaint.setTextSize(26f);
        cv.drawText("★ StatTrak™  |  Factory New", 60f, baseY + 44f, textPaint);
    }

    private Path buildStar(float cx, float cy, float r) {
        Path p = new Path();
        for (int i = 0; i < 10; i++) {
            double ang = Math.toRadians(-90 + i * 36);
            float rr = (i % 2 == 0) ? r : r * 0.45f;
            float x = cx + (float)(Math.cos(ang) * rr);
            float y = cy + (float)(Math.sin(ang) * rr);
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.close();
        return p;
    }
}
