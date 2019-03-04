package com.burningaltar.screenreaderdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import com.burningaltar.screenscraper.ScreenActivity;
import com.burningaltar.screenscraper.ScreenService;

public class MainActivity extends Activity {
    static final String TAG = MainActivity.class.getSimpleName();

    static final int REQ_GET_SCREENSHOT = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        Log.v(TAG, "clicked");
        // capture. Toggle between this and the bottom lines to either get image back here or to service
        ScreenActivity.startForImageResult(this, REQ_GET_SCREENSHOT);

//        Intent intent = new Intent(this, GetScreenService.class);
//        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "got request code " + requestCode + " " + data);

        switch (requestCode) {

            case REQ_GET_SCREENSHOT:
                if (resultCode == RESULT_OK && data != null) {
                    byte[] png = data.getByteArrayExtra(ScreenActivity.KEY_IMAGE_BYTE_ARRAY);

                    Log.v(TAG, "Got png size " + png.length);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(png, 0, png.length);
                    ((ImageView) findViewById(R.id.img)).setImageBitmap(bitmap);
                }

                break;
        }
    }
}
