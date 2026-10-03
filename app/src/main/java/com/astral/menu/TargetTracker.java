package com.astral.menu;

import java.util.ArrayList;
import java.util.List;

public final class TargetTracker {
    private static final List<float[]> targets = new ArrayList<>();
    private TargetTracker() {}
    public static synchronized void update(List<float[]> t) {
        targets.clear();
        if (t != null) targets.addAll(t);
    }
    public static synchronized float[][] getTargets() {
        float[][] out = new float[targets.size()][];
        for (int i = 0; i < targets.size(); i++) out[i] = targets.get(i);
        return out;
    }
    public static synchronized void clear() { targets.clear(); }
}
