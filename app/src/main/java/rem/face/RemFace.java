package rem.face;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import java.io.File;
import java.util.Calendar;

public class RemFace extends WallpaperService {
    @Override
    public Engine onCreateEngine() { return new E(); }

    private class E extends Engine {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Calendar cal = Calendar.getInstance();
        private final Handler handler = new Handler();
        private Bitmap bg;
        private boolean visible = false;
        private final Runnable tick = new Runnable() { public void run() { draw(); } };
        private final BroadcastReceiver timeTick = new BroadcastReceiver() {
            public void onReceive(Context c, Intent i) { draw(); }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            File f = new File(Environment.getExternalStorageDirectory(), "Pictures/rem_c2.png");
            bg = BitmapFactory.decodeFile(f.getAbsolutePath());
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            RemFace.this.registerReceiver(timeTick, new IntentFilter(Intent.ACTION_TIME_TICK));
        }

        @Override
        public void onDestroy() {
            try { RemFace.this.unregisterReceiver(timeTick); } catch (Exception e) {}
            handler.removeCallbacks(tick);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean v) {
            visible = v;
            if (v) { draw(); } else { handler.removeCallbacks(tick); }
        }

        private void schedule() {
            handler.removeCallbacks(tick);
            if (visible) {
                long now = System.currentTimeMillis();
                handler.postDelayed(tick, (now / 60000 + 1) * 60000 - now);
            }
        }

        private void draw() {
            if (!visible) return;
            SurfaceHolder h = getSurfaceHolder();
            Canvas c = null;
            try {
                c = h.lockCanvas();
                if (c == null) return;
                cal.setTimeInMillis(System.currentTimeMillis());
                float w = c.getWidth(), hh = c.getHeight();
                if (bg != null) c.drawBitmap(bg, null, new Rect(0, 0, (int) w, (int) hh), paint);
                else c.drawColor(Color.BLACK);
                float cx = w / 2f, cy = hh / 2f, r = Math.min(w, hh) / 2f;
                int min = cal.get(Calendar.MINUTE);
                paint.setStrokeWidth(10);
                double ha = Math.toRadians((cal.get(Calendar.HOUR) % 12 + min / 60f) * 30f);
                c.drawLine(cx, cy, cx + (float)(Math.sin(ha) * r * 0.45), cy - (float)(Math.cos(ha) * r * 0.45), paint);
                paint.setStrokeWidth(6);
                double ma = Math.toRadians(min * 6f);
                c.drawLine(cx, cy, cx + (float)(Math.sin(ma) * r * 0.7), cy - (float)(Math.cos(ma) * r * 0.7), paint);
                paint.setTextSize(42);
                c.drawText(String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), min), cx, hh * 0.78f, paint);
            } finally {
                if (c != null) h.unlockCanvasAndPost(c);
            }
            schedule();
        }
    }
}
