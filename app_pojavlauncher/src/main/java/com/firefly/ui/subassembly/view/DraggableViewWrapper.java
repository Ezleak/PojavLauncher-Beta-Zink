package com.firefly.ui.subassembly.view;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

public class DraggableViewWrapper {
    private final View mainView;
    private final AttributesFetcher fetcher;
    private long lastUpdateTime = 0;
    private float initialX = 0f;
    private float initialY = 0f;
    private float touchX = 0f;
    private float touchY = 0f;

    public DraggableViewWrapper(View mainView, AttributesFetcher fetcher) {
        this.mainView = mainView;
        this.fetcher = fetcher;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init() {
        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (updateRateLimits()) return false;

                        initialX = fetcher.get()[0];
                        initialY = fetcher.get()[1];
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (updateRateLimits()) return false;

                        int x = (int) Math.max(fetcher.getScreenPixels().minX,
                                Math.min(fetcher.getScreenPixels().maxX,
                                        initialX + (event.getRawX() - touchX)));
                        int y = (int) Math.max(fetcher.getScreenPixels().minY,
                                Math.min(fetcher.getScreenPixels().maxY,
                                        initialY + (event.getRawY() - touchY)));

                        fetcher.set(x, y);
                        return true;
                }
                return false;
            }
        });
    }

    // Avoid performance issues caused by too frequent updates
    private boolean updateRateLimits() {
        boolean limit = false;
        long millis = getCurrentTimeMillis();
        if (millis - lastUpdateTime < 5) {
            limit = true;
        }
        lastUpdateTime = millis;
        return limit;
    }

    public interface AttributesFetcher {
        ScreenPixels getScreenPixels(); // Retrieve screen constraints
        int[] get(); // Get x, y values
        void set(int x, int y);
    }

    public static class ScreenPixels {
        public int minX;
        public int minY;
        public int maxX;
        public int maxY;

        public ScreenPixels(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
}