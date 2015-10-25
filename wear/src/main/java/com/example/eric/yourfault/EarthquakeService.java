package com.example.eric.yourfault;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class EarthquakeService extends Service {

  private GoogleApiClient mApiClient;

  private static final String START_ACTIVITY = "/start_activity";

  @Override
  public void onCreate() {
    super.onCreate();
    /* Initialize the googleAPIClient for message passing */
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
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        Log.d("onStartCommand", "running");
        mApiClient.connect();
        sendMessage(START_ACTIVITY, "shake");
      }
    }).start();

    return START_STICKY;
  }

  private void sendMessage(final String path, final String text) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        Log.d("run", "inside AJFKLDSJFLKJDSKLFJ()");
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
        Log.d("nodes", Integer.toString(nodes.getNodes().size()));
        for (Node node : nodes.getNodes()) {
          Log.d("current node", node.toString());
          MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                  mApiClient, node.getId(), path, text.getBytes()).await();
        }
      }
    }).start();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mApiClient.disconnect();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
