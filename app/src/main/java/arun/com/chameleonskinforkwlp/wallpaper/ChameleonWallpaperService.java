package arun.com.chameleonskinforkwlp.wallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.HashMap;
import java.util.List;

import arun.com.chameleonskinforkwlp.engine.Swatch;
import arun.com.chameleonskinforkwlp.util.Size;
import arun.com.chameleonskinforkwlp.wallpaper.drawing.LollipopWallRenderer;
import arun.com.chameleonskinforkwlp.wallpaper.drawing.Renderer;

public class ChameleonWallpaperService extends WallpaperService {
    public static final String COLOR_CARRY_FILTER = "com.arun.wallpaper.colors";
    public static String VIBRANT = "VIBRANT";
    public static String DARK_VIBRANT = "DARK_VIBRANT";
    public static String MUTED = "MUTED";
    public static String LIGHT_MUTED = "LIGHT_MUTED";
    public static String DARK_MUTED = "DARK_MUTED";
    private List<Swatch> extractedColors = null;

    @Override
    public Engine onCreateEngine() {
        return new ChameleonWallpaperEngine();
    }

    private class ChameleonWallpaperEngine extends Engine {
        private final Handler handler = new Handler();
        private final Paint paint = new Paint();
        private final boolean touchEnabled;
        int height;
        private int width;
        private boolean visible = true;

        private final Runnable drawRunnable = new Runnable() {
            @Override
            public void run() {
                draw();
            }

        };

        final BroadcastReceiver colorExtractedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(COLOR_CARRY_FILTER)) {
                    extractedColors = intent.getParcelableArrayListExtra("Colors");
                    if (extractedColors != null) {
                        // Re render with updated extractedColors
                        if (visible) {
                            handler.post(drawRunnable);
                        } else {
                            handler.removeCallbacks(drawRunnable);
                        }
                    }
                }
            }
        };

        ChameleonWallpaperEngine() {
            touchEnabled = true;
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            handler.post(drawRunnable);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            registerReceiver(colorExtractedReceiver, new IntentFilter(COLOR_CARRY_FILTER));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(colorExtractedReceiver);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunnable);
            } else {
                handler.removeCallbacks(drawRunnable);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunnable);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            this.width = width;
            this.height = height;
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if (touchEnabled) {
                super.onTouchEvent(event);
            }
        }

        private void draw() {
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    // Draw the frame here.
                    drawFrame(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            handler.removeCallbacks(drawRunnable);
            if (visible) {
                handler.postDelayed(drawRunnable, 5000);
            }
        }

        private void drawFrame(Canvas canvas) {
            int muted = Color.parseColor("#E91E63");
            int darkMuted = Color.parseColor("#880E4F");
            int vibrant = Color.parseColor("#00BCD4");
            int darkVibrant = Color.parseColor("#00ACC1");
            int lightMuted = Color.parseColor("#80DEEA");

            // Setting extracted colors
            if (extractedColors != null) {
                muted = extractedColors.get(4) != null ? extractedColors.get(4).getBackgroundColor() : muted;
                darkMuted = extractedColors.get(5) != null ? extractedColors.get(5).getBackgroundColor() : darkMuted;
                vibrant = extractedColors.get(1) != null ? extractedColors.get(1).getBackgroundColor() : vibrant;
                darkVibrant = extractedColors.get(2) != null ? extractedColors.get(2).getBackgroundColor() : darkVibrant;
                lightMuted = extractedColors.get(6) != null ? extractedColors.get(6).getBackgroundColor() : lightMuted;
            }

            final HashMap<String, Integer> colorMap = new HashMap<>();
            colorMap.put(VIBRANT, vibrant);
            colorMap.put(DARK_VIBRANT, darkVibrant);
            colorMap.put(MUTED, muted);
            colorMap.put(LIGHT_MUTED, lightMuted);
            colorMap.put(DARK_MUTED, darkMuted);

            final Renderer renderer = new LollipopWallRenderer();
            renderer.draw(canvas, paint, new Size(width, height), colorMap);
        }
    }
}