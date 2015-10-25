package com.example.eric.yourfault;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class EarthquakeViewActivity extends FragmentActivity implements OnMapReadyCallback {
  LatLng latLng;
  EarthquakeInfo e;
  public JSONObject instagramJson;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_earthquake_view);

    ActionBar bar = getActionBar();
    bar.setDisplayHomeAsUpEnabled(true);
    bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00FFFFFF")));
    bar.setDisplayShowTitleEnabled(false);

    Intent intent = this.getIntent();
    e = intent.getParcelableExtra("earthquake");
    int accentColor = Color.parseColor(e.accentColor);

    String instagramJsonUrl = formUrl(e.latitude, e.longitude);
    new DownloadInstagramDataTask().execute(instagramJsonUrl);

    LinearLayout earthquakeViewContainer = (LinearLayout) findViewById(R.id.earthquake_title_container);
    earthquakeViewContainer.setBackgroundColor(accentColor);


    TextView vTitle = (TextView) findViewById(R.id.earthquakeTitle);
    TextView vCountry = (TextView) findViewById(R.id.earthquakeCountry);
    TextView vMag = (TextView) findViewById(R.id.earthquakeMagnitude);
    TextView vDist = (TextView) findViewById(R.id.earthquakeDistance);
    TextView vNumPhotos = (TextView) findViewById(R.id.numPhotos);
    Button vMoreInfoBtn = (Button) findViewById(R.id.more_info_button);
    Button vPhotoButton = (Button) findViewById(R.id.photo_button);

    vTitle.setText(e.location);
    vCountry.setText(e.country);
    vMag.setText(Double.toString(e.magnitude));
    vDist.setText(Integer.toString(e.distanceTo));
    vNumPhotos.setText(Integer.toString(e.photoUrls.size()));
    vMoreInfoBtn.setTextColor(accentColor);
    vPhotoButton.setTextColor(accentColor);


    vMoreInfoBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Uri uri = Uri.parse(e.url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
      }
    });

    vPhotoButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getBaseContext(), ImageViewActivity.class);
        ArrayList<String> urlList = e.photoUrls;
        intent.putExtra("urlList", urlList);
        startActivity(intent);
      }
    });
    if (e.photoUrls != null) {
      Log.d("urls", e.photoUrls.toString());

    }
    latLng = new LatLng(e.latitude, e.longitude);




    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    googleMap.addMarker(new MarkerOptions().position(latLng).title("Earthquake"));

    CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(2.5f).build();
    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
    googleMap.moveCamera(cameraUpdate);
  }

  // Instagram stuff
  private String formUrl(double latitude, double longitude) {
    String CLIENT_ID = "client_id=c08c9d2ec94b414dbf75ea17751d4f43";
    String urlPrefix = "https://api.instagram.com/v1/media/search?";
    String urlLat = "lat=" + Double.toString(latitude);
    String urlLng = "lng=" + Double.toString(longitude);
    return urlPrefix + urlLat + "&" + urlLng + "&" + CLIENT_ID + "&distance=5000";
  }

  public ArrayList<String> getPhotoUrl() throws JSONException {
    ArrayList<String> urlList= new ArrayList<>();
    if (instagramJson == null) {
      return urlList;
    }
    JSONArray data = instagramJson.getJSONArray("data");
    if (data == null || data.length() == 0) {
      return urlList;
    }
    for (int i = 0; i < data.length(); i += 1) {
      JSONObject photo = data.getJSONObject(i);
      JSONObject images = photo.getJSONObject("images");
      String u = images.getJSONObject("standard_resolution").getString("url");
      urlList.add(u);

    }
    return urlList;
  }

  private class DownloadInstagramDataTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {
      try {
        return getData(urls[0]);
      } catch (IOException e) {
        return "Unable to retrieve data.";
      } catch (JSONException e) {
        return "JSON exception";
      }
    }

    @Override
    protected void onPostExecute(String result) {
      try {
        instagramJson = new JSONObject(result);
        e.photoUrls = getPhotoUrl();
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  private String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public String getData(String url) throws IOException, JSONException {
    InputStream response = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(response, Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      return jsonText;
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }
}
