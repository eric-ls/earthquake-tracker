package com.example.eric.yourfault;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity {
  private static final String START_ACTIVITY = "/start_activity";

  private GoogleApiClient mApiClient;
  // The following are used for the shake detection
  private SensorManager mSensorManager;
  private Sensor mAccelerometer;
  private ShakeDetector mShakeDetector;

  private RelativeLayout vLayout;
  private TextView vText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mApiClient = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
              @Override
              public void onConnected(Bundle connectionHint) {
            /* successfully connected */
                Log.d("onCreate", "connected!");
              }

              @Override
              public void onConnectionSuspended(int cause) {
            /* connection was interrupted */
              }
            }).build();

    // Get the layout elements
    vLayout = (RelativeLayout) findViewById(R.id.background);
    vText = (TextView) findViewById(R.id.watch_title);
    // ShakeDetector initialization
    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mShakeDetector = new ShakeDetector();
    mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
      @Override
      public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
        handleShakeEvent(count);
      }
    });

    new Thread(new Runnable() {
      @Override
      public void run() {
        Log.d("onStartCommand", "running");
        mApiClient.connect();
      }
    }).start();
  }

  public void handleShakeEvent(int i) {
    vLayout.setBackgroundColor(Color.parseColor("#009688"));
    vText.setText("Showing new photo!");

    Intent intent = new Intent(this, EarthquakeService.class);
    startService(intent);

    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        // Do something after 5s = 5000ms
        stopShakeEvent();
      }
    }, 3000);
  }

  public void stopShakeEvent() {
    vLayout.setBackgroundColor(Color.parseColor("#000000"));
    vText.setText("Sensing shake...");
  }

  @Override
  public void onResume() {
    super.onResume();
    // Add the following line to register the Session Manager Listener onResume
    mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
  }

  @Override
  public void onPause() {
    // Add the following line to unregister the Sensor Manager onPause
    mSensorManager.unregisterListener(mShakeDetector);
    super.onPause();
  }

}
