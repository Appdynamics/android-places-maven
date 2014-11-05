package com.example.appdynamics.placesdemo;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Copyright (c) 2014 Mark Prichard
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public abstract class GoogleGeoInfo {
    private static final String TAG = GoogleGeoInfo.class.getName();

    public class Location implements Serializable {
        private BigDecimal lat;
        private BigDecimal lng;

        public BigDecimal getLat() {
            return lat;
        }

        public BigDecimal getLng() {
            return lng;
        }
    }

    public class Geometry implements Serializable {
        private Location location;

        public Location getLocation() {
            return location;
        }
    }

    public class Place implements Serializable {
        private Geometry geometry;
        private String formatted_address;

        public Geometry getGeometry() {
            return geometry;
        }

        public String getFormatted_address() {
            return formatted_address;
        }
    }

    public class PlaceResult implements Serializable {
        private Place results[];
        private String status;
        private String next_page_token;

        public String getNext_page_token() {
            return next_page_token;
        }

        public Place[] getResults() {
            return results;
        }

        public String getStatus() {
            return status;
        }
    }

    public class Elevation implements Serializable {
        private BigDecimal elevation;
        private Location location;
        private BigDecimal resolution;

        public BigDecimal getElevation() {
            return elevation;
        }

        public Location getLocation() {
            return location;
        }

        public BigDecimal getResolution() {
            return resolution;
        }
    }

    public class ElevationResult implements Serializable {
        private Elevation results[];
        private String status;

        public Elevation[] getResults() {
            return results;
        }

        public String getStatus() {
            return status;
        }
    }

    private static final String GEOCODE_API_BASE = "https://maps.googleapis.com/maps/api/geocode";
    private static final String ELEVATION_API_BASE = "https://maps.googleapis.com/maps/api/elevation";

    private static final String JSON = "/json";
    private static final String ADDRESS = "?address=";
    private static final String LOCATIONS = "?locations=";
    private static final String KEY = "&key=";
    private static final String encoding = "utf8";

    private String mJsonOutput;

    private static String getGeoCodeInfoRequest(String addressString) {
        String search = "";
        try {
            search = GoogleGeoInfo.GEOCODE_API_BASE
                    + GoogleGeoInfo.JSON
                    + GoogleGeoInfo.ADDRESS + URLEncoder.encode(addressString, encoding)
                    + GoogleGeoInfo.KEY + GooglePlacesKey.API_KEY;
            return search;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return search;
    }

    private static String getElevationInfoRequest(Location location) {
        String search = "";
        try {
            search = GoogleGeoInfo.ELEVATION_API_BASE
                    + GoogleGeoInfo.JSON
                    + GoogleGeoInfo.LOCATIONS + location.getLat() + "," + location.getLng()
                    + GoogleGeoInfo.KEY + GooglePlacesKey.API_KEY;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return search;
    }

    public void getGeocodeInfo (final String addressString) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String search = getGeoCodeInfoRequest(addressString);
                    mJsonOutput = doGet(new URL(search));
                } catch (Exception e) {
                    Log.e(TAG, "Exception: ", e);
                }
                return mJsonOutput;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    PlaceResult places = new Gson().fromJson(mJsonOutput, PlaceResult.class);

                    if (places.getStatus().equalsIgnoreCase("OK"))
                        onGeocodeSuccess(places);
                    else
                        onGeocodeFailure(places.getStatus());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void getElevationInfo (final Location location) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String search = getElevationInfoRequest(location);
                    mJsonOutput = doGet(new URL(search));
                } catch (Exception e) {
                    Log.e(TAG, "Exception: ", e);
                }
                return mJsonOutput;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    ElevationResult elevation = new Gson().fromJson(mJsonOutput, ElevationResult.class);

                    if (elevation.getStatus().equalsIgnoreCase("OK"))
                        onElevationSuccess(elevation);
                    else
                        onElevationFailure(elevation.getStatus());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // Callbacks to calling Activity/Fragment
    abstract public void onGeocodeSuccess(PlaceResult places);
    abstract public void onGeocodeFailure(String status);
    abstract public void onElevationSuccess(ElevationResult elevation);
    abstract public void onElevationFailure(String status);

    private static String doGet(URL url) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try {
            Log.d(TAG, "Request URL: " + url.toString());

            conn = (HttpURLConnection) url.openConnection();

            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception: ", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jsonResults.toString();
    }
}
