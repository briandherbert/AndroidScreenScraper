package com.burningaltar.screenreaderdemo;

import android.content.Intent;
import android.util.Log;

import com.burningaltar.screenscraper.ScreenService;

/**
 * Created by bherbert on 8/9/16.
 */

public class GetScreenService extends ScreenService {
    static final String TAG = GetScreenService.class.getSimpleName();

    @Override
    public void onImage(byte[] png) {
        Log.v(TAG, "got image, size " + png.length);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        requestScreenshot();
        return super.onStartCommand(intent, flags, startId);
    }
}
