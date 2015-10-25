package com.example.eric.yourfault;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


/**
 * Created by Eric on 10/7/15.
 */
public class EarthquakeInfo implements Parcelable {

  public String id;
  public String location;
  public String country;
  public double magnitude;
  public int time;
  public String url;
  public Location loc;
  public double latitude;
  public double longitude;
  public int distanceTo;
  public String accentColor;
  public String thumbnailUrl;
  public ArrayList<String> photoUrls = new ArrayList<>();

  public EarthquakeInfo() { }

  protected EarthquakeInfo(Parcel in) {
    this.id = in.readString();
    this.location = in.readString();
    this.country = in.readString();
    this.magnitude = in.readDouble();
    this.time = in.readInt();
    this.url = in.readString();
    this.loc = in.readParcelable(Location.class.getClassLoader());
    this.latitude = in.readDouble();
    this.longitude = in.readDouble();
    this.distanceTo = in.readInt();
    this.accentColor = in.readString();
    this.thumbnailUrl = in.readString();
    in.readList(photoUrls, String.class.getClassLoader());
  }

  public static final Creator<EarthquakeInfo> CREATOR = new Creator<EarthquakeInfo>() {
    @Override
    public EarthquakeInfo createFromParcel(Parcel in) {
      return new EarthquakeInfo(in);
    }

    @Override
    public EarthquakeInfo[] newArray(int size) {
      return new EarthquakeInfo[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(location);
    dest.writeString(country);
    dest.writeDouble(magnitude);
    dest.writeInt(time);
    dest.writeString(url);
    dest.writeParcelable(loc, flags);
    dest.writeDouble(latitude);
    dest.writeDouble(longitude);
    dest.writeInt(distanceTo);
    dest.writeString(accentColor);
    dest.writeString(thumbnailUrl);
    dest.writeList(photoUrls);
  }
}
