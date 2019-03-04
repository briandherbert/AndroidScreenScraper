package com.burningaltar.screenscraper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by bherbert on 8/5/16.
 */

public class ScreenActivity extends Activity implements ImageTransmogrifier.Listener {
    static final String TAG = ScreenActivity.class.getSimpleName();
    public static final String KEY_IMAGE_BYTE_ARRAY = "BYTE_ARRAY";

    public static final String EXTRA_SERVICE_CLASS = "EXTRA_SERVICE_CLASS";

    private static final int REQUEST_SCREENSHOT = 1;
    private MediaProjectionManager mgr;
    private MediaProjection mProjection;
    ImageTransmogrifier mImageT;
    private VirtualDisplay mVirtDisplay;

    Handler mHandler = new Handler();

    private ScreenService mScreenService;

    boolean mIsFirstRun = true;

    Class mServiceClass = null;

    public static void startForImageResult(Activity activity, int resultCode) {
        Intent intent = new Intent(activity, ScreenActivity.class);
        activity.startActivityForResult(intent, resultCode);
    }

    public static void startForImageCallback(ScreenService service) {
        Intent intent = new Intent(service, ScreenActivity.class);
        intent.putExtra(EXTRA_SERVICE_CLASS, service.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        service.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_SERVICE_CLASS)) {
            mServiceClass = (Class) extras.getSerializable(EXTRA_SERVICE_CLASS);
        }
    }

    @Override
    public void updateImage(byte[] png) {
        Log.v(TAG, "got image " + png.length);

        mProjection.stop();

        if (!isFinishing()) {
            if (mScreenService != null) {
                mScreenService.onImage(png);
            } else {
                Intent intent = new Intent();
                intent.putExtra(KEY_IMAGE_BYTE_ARRAY, png);
                setResult(Activity.RESULT_OK, intent);
            }
        }

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        Log.v(TAG, "got result " + resultCode);
        if (requestCode == REQUEST_SCREENSHOT) {
            if (resultCode == RESULT_OK) {
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getProjection(resultCode, data);
                    }
                }, 500);
            }
        }
    }

    public void getProjection(int resultCode, Intent data) {
        mProjection = mgr.getMediaProjection(resultCode, data);
        mImageT = new ImageTransmogrifier(this, mHandler);

        MediaProjection.Callback cb = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                mVirtDisplay.release();
            }
        };

        mVirtDisplay = mProjection.createVirtualDisplay("andprojector",
                mImageT.getWidth(), mImageT.getHeight(),
                getResources().getDisplayMetrics().densityDpi,
                0, mImageT.getSurface(), null, mHandler);
        mProjection.registerCallback(cb, mHandler);
    }

    @Override
    public void onDestroy() {
        mProjection.stop();

        super.onDestroy();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.v(TAG, "connected");

            mScreenService = ((ScreenService.LocalBinder) service).getService();

            //Toast.makeText(ScreenActivity.this, "connected", Toast.LENGTH_SHORT).show();
            startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.v(TAG, "disconnected");

            mScreenService = null;
            //Toast.makeText(ScreenActivity.this, "disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onPause() {
        super.onPause();

        Log.v(TAG, "onPause");

        if (mServiceClass != null) {
            try {
                unbindService(mConnection);
            } catch (Exception e) {
                // noop
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isFinishing() && mIsFirstRun) {
            if (mServiceClass == null) {
                startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
            } else {
                Log.v(TAG, "bind service");
                bindService(new Intent(this, mServiceClass), mConnection, Context.BIND_AUTO_CREATE);
            }
        }

        mIsFirstRun = false;
    }
}
