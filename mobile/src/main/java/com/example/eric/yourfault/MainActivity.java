package com.example.eric.yourfault;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

  private final String USGS_URL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/4.5_day.geojson";
  protected static final String TAG = "MainActivity";
  public static int UPDATE_INTERVAL_MS = 10000;
  public static int FASTEST_INTERVAL_MS = 20000;
  private static final int INTERVAL = 300000;
  private static final int SECOND = 100;

  public List<EarthquakeInfo> earthquakeList;
  public EarthquakeInfo mostRecentEarthquake;
  public JSONObject json;
  public JSONObject instagramJson;
  GoogleApiClient mGoogleApiClient;
  Location mLastLocation;
  SwipeRefreshLayout swipeToRefresh;
  RecyclerView list;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.cardList);
    LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    recyclerView.setLayoutManager(layoutManager);
    RecyclerView.ItemDecoration itemDecoration = new
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
    recyclerView.addItemDecoration(itemDecoration);

    swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh);
    swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        refreshList();
      }
    });

    Context context = getApplicationContext();
    list = (RecyclerView) findViewById(R.id.cardList);
    list.setHasFixedSize(true);
    LinearLayoutManager llm = new LinearLayoutManager(this);
    llm.setOrientation(LinearLayoutManager.VERTICAL);
    list.setLayoutManager(llm);

    list.addOnItemTouchListener(
            new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
              @Override
              public void onItemClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this, EarthquakeViewActivity.class);
                EarthquakeInfo earthquakeData = earthquakeList.get(position);

                intent.putExtra("earthquake", earthquakeData);
                startActivity(intent);
              }
            })
    );

    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    mGoogleApiClient = buildGoogleApiClient(this);

    if (networkInfo != null && networkInfo.isConnected()) {
      // download json data
      new DownloadDataTask().execute(USGS_URL);
    }

    createAndStartTimer();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    // noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  protected GoogleApiClient buildGoogleApiClient(Context context) {
    return new GoogleApiClient.Builder(context)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(Wearable.API)
            .addApi(LocationServices.API)
            .build();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mGoogleApiClient.isConnected()) {
      mGoogleApiClient.disconnect();
    }
  }

  @Override
  public void onConnected(Bundle bundle) {
    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
            mGoogleApiClient);

    LocationRequest locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_INTERVAL_MS);

    LocationServices.FusedLocationApi
            .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
            .setResultCallback(new ResultCallback<Status>() {
              @Override
              public void onResult(Status status) {
                if (status.getStatus().isSuccess()) {
                  Log.d(TAG, "Successfully requested");
                } else {
                  Log.e(TAG, status.getStatusMessage());
                }

              }
            });
  }

  @Override
  public void onLocationChanged(Location location) {
    mLastLocation = location;
  }

  @Override
  public void onConnectionFailed(ConnectionResult result) {
    // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
    // onConnectionFailed.
    Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
  }


  @Override
  public void onConnectionSuspended(int cause) {
    // The connection to Google Play services was lost for some reason. We call connect() to
    // attempt to re-establish the connection.
    Log.i(TAG, "Connection suspended");
    mGoogleApiClient.connect();
  }

  private class DownloadDataTask extends AsyncTask<String, Void, String> {
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
        json = new JSONObject(result);
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

  public void refreshList() {
    new Handler().post(new Runnable() {
      @Override
      public void run() {
        try {
          earthquakeList = new ArrayList<>();
          earthquakeList = createEarthquakeList();
          EarthquakeAdapter ea = new EarthquakeAdapter(earthquakeList);
          list.setAdapter(ea);
        } catch (JSONException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    onItemsLoadComplete();
  }

  public void onItemsLoadComplete() {
    swipeToRefresh.setRefreshing(false);
  }

  private List<EarthquakeInfo> createEarthquakeList() throws JSONException, IOException {
    JSONArray allEarthquakes = json.getJSONArray("features");
    List<EarthquakeInfo> result = new ArrayList<>();

    for (int i = 0; i < allEarthquakes.length(); i += 1) {
      JSONObject features = allEarthquakes.getJSONObject(i);
      EarthquakeInfo ei = createEarthquakeInfoObject(features);
      result.add(ei);
    }
    return result;
  }

  private void createAndStartTimer() {
    /* Query the USGS API for data at the end of interval */
    CountDownTimer timer = new CountDownTimer(INTERVAL, SECOND) {
      @Override
      public void onTick(long millisUntilFinished) { }

      @Override
      public void onFinish() {
        /* Get the saved JSON file and store in variable */
        /* will be implemented later */

        /* Get the new JSON file */
        Log.d("timer", "finished one timer cycle");
        new DownloadDataTask().execute(USGS_URL);
        try {
          // get the most recent earthquake
          mostRecentEarthquake = getFirstEarthquakeFromJson();
        } catch (JSONException e) {
          e.printStackTrace();
        }

        // notify user of earthquake
        Intent i = new Intent(getBaseContext(), EarthquakeWatchService.class);
        i.putExtra("earthquake", mostRecentEarthquake);
        startService(i);
        createAndStartTimer();
      }
    };

    timer.start();
  }

  private EarthquakeInfo createEarthquakeInfoObject(JSONObject features) throws JSONException {
    EarthquakeInfo ei = new EarthquakeInfo();
    JSONObject properties = features.getJSONObject("properties");
    JSONObject geometry = features.getJSONObject("geometry");
    JSONArray coordinates = geometry.getJSONArray("coordinates");

    ei.longitude = coordinates.getDouble(0);
    ei.latitude = coordinates.getDouble(1);
    Location loc = new Location("earthquakeLocation");
    loc.setLatitude(ei.latitude);
    loc.setLongitude(ei.longitude);

    ei.loc = loc;
    if (mLastLocation != null) {
      ei.distanceTo = (int) (mLastLocation.distanceTo(ei.loc) * (float) 0.000621371192);
    }
    ei.id = features.getString("id");
    ei.magnitude = properties.getDouble("mag");
    ei.time = properties.getInt("time");
    ei.url = properties.getString("url");

    String location = properties.getString("place");
    if (location.contains(",")) {
      if (location.contains("of")) {
        ei.location = location.split(",")[0].split("of")[1].trim();
      } else {
        ei.location = location.split(",")[0];
      }
      ei.country = location.split(",")[1].trim();
    } else {
      ei.location = location;
      ei.country = "";
    }

    // get instagram data array
    String instagramJsonUrl = formUrl(ei.latitude, ei.longitude);
    try {
      String s = new DownloadInstagramDataTask().execute(instagramJsonUrl).get();
      instagramJson = new JSONObject(s);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    ei.photoUrls = getPhotoUrl();
    Log.d("photoUrls", ei.photoUrls.toString());

    return ei;
  }

  private EarthquakeInfo getFirstEarthquakeFromJson() throws JSONException {
    JSONArray allEarthquakes = json.getJSONArray("features");
    JSONObject features = allEarthquakes.getJSONObject(0);

    if (features == null) {
      return null;
    }
    EarthquakeInfo e = createEarthquakeInfoObject(features);
    return e;
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
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }
}
