package com.example.appdynamics.placesdemo;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.services.civicinfo.model.GeographicDivision;
import com.google.api.services.civicinfo.model.Office;
import com.google.api.services.civicinfo.model.Official;
import com.google.api.services.civicinfo.model.RepresentativeInfoResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private GoogleGeoInfo.Location mLocation;
        private GoogleGeoInfo.Place mPlace;
        private EditText mAddress;
        private Button mButton;
        private TextView mLatLegend;
        private TextView mLatitude;
        private TextView mLngLegend;
        private TextView mLongitude;
        private TextView mElevationLegend;
        private TextView mElevation;
        private TextView mOfficialsLegend;
        private ListView mOfficials;

        public PlaceholderFragment() {
        }

        private ArrayList<String> getElectedOfficials(RepresentativeInfoResponse response) {
            // Stores list of elected officials with district and office
            final ArrayList<String> list = new ArrayList<String>();

            // Prepare collections for divisions, offices and officials
            Map<String, GeographicDivision> divisionMap = response.getDivisions();
            Collection<GeographicDivision> divisions = divisionMap.values();
            List<Office> offices = response.getOffices();
            List<Official> officials = response.getOfficials();

            // Format as: "Official, Office (Division)" and add to list
            for (GeographicDivision division : divisions) {
                List<Long> officeIndices = division.getOfficeIndices();
                for (Long i : officeIndices) {
                    List<Long> officialsIndices = offices.get(i.intValue()).getOfficialIndices();
                    for (Long j : officialsIndices) {
                        list.add(officials.get(j.intValue()).getName()
                                + ", "
                                + offices.get(i.intValue()).getName()
                                + " ("
                                + division.getName()
                                + ")");
                    }
                }
            }

            // Sort alphabetically and return
            Collections.sort(list);
            return list;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mAddress = (EditText) rootView.findViewById(R.id.editText);
            mButton = (Button) rootView.findViewById(R.id.button);
            mLatLegend = (TextView) rootView.findViewById(R.id.lat_legend);
            mLatitude = (TextView) rootView.findViewById(R.id.text_lat);
            mLngLegend = (TextView) rootView.findViewById(R.id.lng_legend);
            mLongitude = (TextView) rootView.findViewById(R.id.text_lng);
            mElevationLegend = (TextView) rootView.findViewById(R.id.elevation_legend);
            mElevation = (TextView) rootView.findViewById(R.id.elevation);
            mOfficialsLegend = (TextView) rootView.findViewById(R.id.elected_officials_legend);
            mOfficials = (ListView) rootView.findViewById(R.id.listView);

            // Get Geolocation, Elevation and Civic Info data when user enters search address
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String searchAddress = mAddress.getText().toString();
                    Log.d(TAG, "searchAddress: " + searchAddress);
                    if (! searchAddress.isEmpty()) {
                        try {
                            // Check for Geolocation information first
                            GoogleGeoInfo geoInfo = new GoogleGeoInfo() {
                                @Override
                                public void onGeocodeSuccess(PlaceResult places) {
                                    mPlace = places.getResults()[0];
                                    mLocation = mPlace.getGeometry().getLocation();

                                    // Display Lat/Lng info
                                    mLatLegend.setText(R.string.lat_legend);
                                    mLatitude.setText(" " + mLocation.getLat().toPlainString());
                                    mLngLegend.setText(R.string.lng_legend);
                                    mLongitude.setText(" " + mLocation.getLng().toPlainString());

                                    // Get elevation from Elevation API
                                    getElevationInfo(mLocation);

                                    // Get civic information from Civic Information API
                                    GoogleCivicInfo civicInfo = new GoogleCivicInfo() {
                                        @Override
                                        public void onSuccess(RepresentativeInfoResponse response) {
                                            try {
                                                // Get list of elected officials by district and office
                                                final ArrayList<String> list = getElectedOfficials(response);

                                                mOfficialsLegend.setText(R.string.elected_officials_legend);

                                                final ArrayAdapter<String> adapter =
                                                        new ArrayAdapter<String>(rootView.getContext(), R.layout.division_list, list);
                                                mOfficials.setAdapter(adapter);
                                            }
                                            catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure() {
                                            Log.d(TAG, "civicInfo.getInfo() failed");
                                        }
                                    };
                                    civicInfo.getInfo(mPlace.getFormatted_address());
                                }

                                @Override
                                public void onGeocodeFailure(String status) {
                                    Log.e(TAG, "Error: " + status);
                                }

                                @Override
                                public void onElevationSuccess(ElevationResult elevation) {
                                    try {
                                        mElevationLegend.setText(R.string.elevation_legend);
                                        mElevation.setText(" " + elevation.getResults()[0]
                                                .getElevation()
                                                .setScale(2, BigDecimal.ROUND_HALF_UP));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onElevationFailure(String status) {
                                    Log.d(TAG, "Error: " + status);
                                }
                            };
                            geoInfo.getGeocodeInfo(searchAddress);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            return rootView;
        }
    }
}
