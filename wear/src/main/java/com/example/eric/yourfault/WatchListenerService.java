package com.example.eric.yourfault;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchListenerService extends WearableListenerService {
  private static final String START_ACTIVITY = "/start_activity";

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    String data = new String(messageEvent.getData());
    Log.d("data", data);
    String[] earthquakeInfo = data.split("\\|");
    String location = earthquakeInfo[0];
    String magnitude = earthquakeInfo[1];

    Log.d("data", location);
    Log.d("data", magnitude);

    int notificationId = 001;

    NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.earthquake)
                    .setContentTitle("Recent Earthquake")
                    .setContentText(magnitude + " mag. near " + location);

    NotificationManagerCompat notificationManager =
            NotificationManagerCompat.from(this);
    notificationManager.notify(notificationId, notificationBuilder.build());

    if (messageEvent.getPath().equalsIgnoreCase(START_ACTIVITY)) {
    } else {
      super.onMessageReceived( messageEvent );
    }

  }
}
