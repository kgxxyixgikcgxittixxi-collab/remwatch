package rem.face;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;
import java.io.File;
import java.util.Calendar;

public class RemFace extends CanvasWatchFaceService {
    @Override
    public Engine onCreateEngine() { return new E(); }

    private class E extends Engine {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Calendar cal = Calendar.getInstance();
        private final Handler handler = new Handler();
        private Bitmap bg;
        private boolean ambient = false;
        private boolean visible = true;
        private final Runnable tick = new Runnable() { public void run() { invalidate(); } };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            setWatchFaceStyle(new WatchFaceStyle.Builder(RemFace.this).setAcceptsTapEvents(true).build());
            File f = new File(Environment.getExternalStorageDirectory(), "Pictures/rem_c2.png");
            bg = BitmapFactory.decodeFile(f.getAbsolutePath());
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
        }

        @Override public void onAmbientModeChanged(boolean a) { super.onAmbientModeChanged(a); ambient = a; invalidate(); }
        @Override public void onVisibilityChanged(boolean v) { super.onVisibilityChanged(v); visible = v; if (v) invalidate(); }
        @Override public void onTimeTick() { super.onTimeTick(); invalidate(); }

        private void schedule() {
            handler.removeCallbacks(tick);
            if (visible && !ambient) {
                long now = System.currentTimeMillis();
                handler.postDelayed(tick, (now / 60000 + 1) * 60000 - now);
            }
        }

        @Override
        public void onDraw(Canvas c, Rect b) {
            cal.setTimeInMillis(System.currentTimeMillis());
            float w = b.width(), h = b.height(), cx = w / 2f, cy = h / 2f, r = Math.min(w, h) / 2f;
            if (bg != null && !ambient) c.drawBitmap(bg, null, new Rect(0, 0, b.width(), b.height()), paint);
            else c.drawColor(Color.BLACK);
            int min = cal.get(Calendar.MINUTE);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(10);
            double ha = Math.toRadians((cal.get(Calendar.HOUR) % 12 + min / 60f) * 30f);
            c.drawLine(cx, cy, cx + (float)(Math.sin(ha) * r * 0.45), cy - (float)(Math.cos(ha) * r * 0.45), paint);
            paint.setStrokeWidth(6);
            double ma = Math.toRadians(min * 6f);
            c.drawLine(cx, cy, cx + (float)(Math.sin(ma) * r * 0.7), cy - (float)(Math.cos(ma) * r * 0.7), paint);
            paint.setTextSize(42);
            c.drawText(String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), min), cx, h * 0.78f, paint);
            schedule();
        }
    }
}
