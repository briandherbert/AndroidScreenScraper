package com.burningaltar.screenscraper;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

/**
 * Created by bherbert on 8/9/16.
 */

public abstract class ScreenService extends Service {
    static final String TAG = "ScreenService";

    public final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind");
        return mBinder;
    }

    public class LocalBinder extends Binder {
        ScreenService getService() {
            return getScreenService();
        }
    }

    final ScreenService getScreenService() {
        return this;
    }

    public abstract void onImage(byte[] png);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service onStartCommand");
        return Service.START_NOT_STICKY;
    }

    public void requestScreenshot() {
        ScreenActivity.startForImageCallback(this);
    }
}
