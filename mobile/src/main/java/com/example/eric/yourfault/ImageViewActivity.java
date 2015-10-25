package com.example.eric.yourfault;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.ArrayList;

public class ImageViewActivity extends Activity {
  ArrayList<String> urlList;
  ImageView vImage;
  Button vButton;
  int urlListIndex = 0;
  private Intent intent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_view);

    Intent intent = this.getIntent();
    urlList = intent.getStringArrayListExtra("urlList");

    vImage = (ImageView) findViewById(R.id.photo);
    vButton = (Button) findViewById(R.id.next_photo_button);
    intent = new Intent(this, WatchListenerService.class);
    changePhoto();

    vButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        changePhoto();
      }
    });
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    if(intent.getStringExtra("methodName").equals("changePhoto")){
      changePhoto();
    }
  }

  public void changePhoto() {
    if (urlList == null || urlList.size() == 0) {
      Toast.makeText(getBaseContext(), "No photos available!", Toast.LENGTH_SHORT).show();
      return;
    }
    if (urlListIndex >= urlList.size() || urlList.get(urlListIndex) == null) {
      urlListIndex = 0;
      changePhoto();
    } else {
      String u = urlList.get(urlListIndex);
      urlListIndex++;
      new ImageDownloader(vImage).execute(u);
    }
  }


  class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public ImageDownloader(ImageView bmImage) {
      this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
      String url = urls[0];
      Bitmap mIcon = null;
      try {
        InputStream in = new java.net.URL(url).openStream();
        mIcon = BitmapFactory.decodeStream(in);
      } catch (Exception e) {
        Log.e("Error", e.getMessage());
      }
      return mIcon;
    }

    protected void onPostExecute(Bitmap result) {
      bmImage.setImageBitmap(result);
    }
  }

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      changePhoto();
    }
  };

  @Override
  public void onResume() {
    super.onResume();
    registerReceiver(broadcastReceiver, new IntentFilter(WatchListenerService.BROADCAST_ACTION));
  }

  @Override
  public void onPause() {
    super.onPause();
    unregisterReceiver(broadcastReceiver);
  }
}
