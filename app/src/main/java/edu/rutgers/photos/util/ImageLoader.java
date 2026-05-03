package edu.rutgers.photos.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageLoader {

    private static final String TAG = "ImageLoader";

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static final LruCache<String, Bitmap> CACHE;
    static {
        int maxKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheKb = maxKb / 8;
        CACHE = new LruCache<String, Bitmap>(cacheKb) {
            @Override protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    private ImageLoader() {}

    public static void loadInto(final ImageView view, final Uri uri,
                                final int reqW, final int reqH,
                                final int placeholderRes) {
        if (view == null) return;
        if (uri == null) {
            view.setImageResource(placeholderRes);
            return;
        }

        final String key = uri.toString() + "#" + reqW + "x" + reqH;
        Bitmap cached = CACHE.get(key);
        if (cached != null) {
            view.setImageBitmap(cached);
            view.setTag(key);
            return;
        }

        view.setImageResource(placeholderRes);
        view.setTag(key);
        final WeakReference<ImageView> ref = new WeakReference<>(view);
        final Context appCtx = view.getContext().getApplicationContext();

        EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                Bitmap bmp = decodeSampled(appCtx, uri, reqW, reqH);
                if (bmp != null) CACHE.put(key, bmp);
                final Bitmap result = bmp;
                MAIN.post(new Runnable() {
                    @Override public void run() {
                        ImageView v = ref.get();
                        if (v == null) return;
                        Object t = v.getTag();
                        if (t != null && key.equals(t)) {
                            if (result != null) v.setImageBitmap(result);
                        }
                    }
                });
            }
        });
    }

    private static Bitmap decodeSampled(Context ctx, Uri uri, int reqW, int reqH) {
        ContentResolver cr = ctx.getContentResolver();
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = cr.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        } catch (Exception e) {
            Log.w(TAG, "bounds decode failed for " + uri, e);
            return null;
        }
        int sample = computeSampleSize(bounds.outWidth, bounds.outHeight, reqW, reqH);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        try (InputStream in2 = cr.openInputStream(uri)) {
            return BitmapFactory.decodeStream(in2, null, opts);
        } catch (Exception e) {
            Log.w(TAG, "decode failed for " + uri, e);
            return null;
        }
    }

    private static int computeSampleSize(int w, int h, int reqW, int reqH) {
        int sample = 1;
        if (w <= 0 || h <= 0 || reqW <= 0 || reqH <= 0) return 1;
        while ((w / sample) > reqW * 2 && (h / sample) > reqH * 2) {
            sample *= 2;
        }
        return Math.max(1, sample);
    }
}
