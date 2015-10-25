package com.example.eric.yourfault;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 10/7/15.
 */
public class EarthquakeAdapter extends RecyclerView.Adapter<EarthquakeAdapter.EarthquakeViewHolder> {

  private final String CLIENT_ID = "client_id=c08c9d2ec94b414dbf75ea17751d4f43";
  private final String urlPrefix = "https://api.instagram.com/v1/media/search?";
  public JSONObject j;
  public ImageView image;
  public double lat;
  public double lng;

  private List<EarthquakeInfo> earthquakeList;
  public EarthquakeAdapter(List<EarthquakeInfo> list) {
    this.earthquakeList = list;
  }

  @Override
  public int getItemCount() {
    return earthquakeList.size();
  }

  @Override
  public void onBindViewHolder(EarthquakeViewHolder evh, int i) {
    EarthquakeInfo e = earthquakeList.get(i);
    lat = e.latitude;
    lng = e.longitude;

    String size = e.photoUrls.size() + "";
    Log.d(e.location, size);

    evh.vLocation.setText(e.location);
    evh.vCountry.setText(e.country);

    String mag = Double.toString(e.magnitude);
    evh.vMagnitude.setText(mag + " mag");
    setMagnitudeColor(e, evh.vMagnitude);

    String dist = Integer.toString(e.distanceTo);
    evh.vDistance.setText(dist + " mi away");

    image = evh.vImage;
  }

  @Override
  public EarthquakeViewHolder onCreateViewHolder(ViewGroup v, int i) {
    View itemView = LayoutInflater.
            from(v.getContext()).
            inflate(R.layout.earthquake_list_item, v, false);
    return new EarthquakeViewHolder(itemView);
  }

  public void setMagnitudeColor(EarthquakeInfo e, TextView vMagnitude) {
    if (e.magnitude <= 3.0) {
      vMagnitude.setTextColor(Color.parseColor("#009688"));
      e.accentColor = "#009688";
    } else if (e.magnitude <= 4.5) {
      vMagnitude.setTextColor(Color.parseColor("#FF9800"));
      e.accentColor = "#FF9800";
    } else {
      vMagnitude.setTextColor(Color.parseColor("#D50000"));
      e.accentColor = "#D50000";
    }
  }

  private void setImage(ImageView image, String thumbnailUrl) {
    if (thumbnailUrl != null) {
      new ImageDownloader(image).execute(thumbnailUrl);
    } else {

    }
  }

  public static class EarthquakeViewHolder extends RecyclerView.ViewHolder {
    protected TextView vLocation;
    protected TextView vCountry;
    protected TextView vMagnitude;
    protected TextView vDistance;
    protected ImageView vImage;

    public EarthquakeViewHolder(View v) {
      super(v);
      vLocation = (TextView) v.findViewById(R.id.location_title);
      vCountry = (TextView) v.findViewById(R.id.location_country);
      vMagnitude = (TextView) v.findViewById(R.id.magnitude);
      vDistance = (TextView) v.findViewById(R.id.distance);
      vImage = (ImageView) v.findViewById(R.id.location_photo);
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
