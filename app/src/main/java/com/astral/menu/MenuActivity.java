package com.astral.menu;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

public class MenuActivity extends Activity {
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.astral_menu);

        CheckBox cbHat     = findViewById(R.id.cbChinaHat);
        CheckBox cbFps     = findViewById(R.id.cbFps);
        CheckBox cbSm      = findViewById(R.id.cbSmooth);
        CheckBox cbNoLag   = findViewById(R.id.cbNoLag);
        CheckBox cbStretch = findViewById(R.id.cbStretch);
        TextView close     = findViewById(R.id.btnClose);
        TextView hide      = findViewById(R.id.btnHide);

        cbHat.setOnCheckedChangeListener((v, c)     -> AstralOverlayService.chinaHat  = c);
        cbFps.setOnCheckedChangeListener((v, c)     -> AstralOverlayService.fps120    = c);
        cbSm.setOnCheckedChangeListener((v, c)      -> AstralOverlayService.smooth    = c);
        cbNoLag.setOnCheckedChangeListener((v, c)   -> AstralOverlayService.noLag     = c);
        cbStretch.setOnCheckedChangeListener((v, c) -> AstralOverlayService.stretch43 = c);

        close.setOnClickListener(v -> finish());
        hide.setOnClickListener(v  -> finish());
    }
}
