package com.example.eric.yourfault;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchListenerService extends WearableListenerService {
  private static final String START_ACTIVITY = "/start_activity";
  private static final String TAG = "BroadcastService";
  public static final String BROADCAST_ACTION = "changePhoto";
  private final Handler handler = new Handler();
  Intent intent;
  int counter = 0;

  @Override
  public void onCreate() {
    super.onCreate();
    intent = new Intent(BROADCAST_ACTION);
  }

  @Override
  public void onStart(Intent intent, int startId) {
    handler.removeCallbacks(sendUpdatesToUI);
    handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
  }
  private Runnable sendUpdatesToUI = new Runnable() {
    public void run() {
      DisplayLoggingInfo();
      handler.postDelayed(this, 10000); // 10 seconds
    }
  };

  private void DisplayLoggingInfo() {
    Log.d(TAG, "entered DisplayLoggingInfo");
    sendBroadcast(intent);
  }

  @Override
  public void onDestroy() {
    handler.removeCallbacks(sendUpdatesToUI);
    super.onDestroy();
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    Log.d("message", "received message from watch");
    sendBroadcast(intent);
    //ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
    //ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
  }
}

