package me.cullycross.arbuz.activities;

import android.graphics.Color;
import android.content.Intent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterManager;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import me.cullycross.arbuz.R;
import me.cullycross.arbuz.content.CrimeLocation;
import me.cullycross.arbuz.events.LocationFoundEvent;
import me.cullycross.arbuz.fragments.ArbuzMapFragment;
import me.cullycross.arbuz.fragments.DirectionsDialogFragment;
import me.cullycross.arbuz.utils.FetchingClusterManager;
import me.cullycross.arbuz.utils.LocationHelper;
import me.cullycross.arbuz.utils.ParseHelper;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ArbuzMapFragment.OnMapInteractionListener,
        DirectionsDialogFragment.OnFragmentInteractionListener,
        FetchingClusterManager.OnCameraChangeListener {

    private static final String TAG = MapActivity.class.getName();

    private static final String FRAGMENT_DIALOG_DIRECTIONS = "fragment_dialog_directions";

    @Bind(R.id.toggle_button_heatmap)
    ToggleButton mToggleButtonHeatmap;
    @Bind(R.id.action_safe_way)
    Button mActionSafeWay;
    @Bind(R.id.action_red_button)
    Button mActionRedButton;
    @Bind(R.id.action_settings)
    Button mActionSettings;

    private GoogleMap mMap;

    private LocationHelper mLocationHelper;

    private FetchingClusterManager mClusterManager;

    /**
     * Method for handling events, sent by EventBus
     *
     * @param event location
     */
    public void onEvent(LocationFoundEvent event) {
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(event.getLocation(), 14f));
        }
    }

    @Override
    public void onCameraChanged(CameraPosition cameraPosition) {
        LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;
        fetchNearCrimes(bounds);
    }


    private void fetchNearCrimes(LatLngBounds bounds) {
        LatLng center = mMap.getCameraPosition().target;

        double distance = SphericalUtil.computeDistanceBetween(center, bounds.northeast);

        ParseGeoPoint point =
                new ParseGeoPoint(center.latitude, center.longitude);
        ParseHelper.getInstance().downloadNear(point, distance, new ParseHelper.OnLoadCrimesListener() {
            @Override
            public void onLoadCrimes(List<CrimeLocation> crimes) {

                CrimeLocation[] crimeLocations =
                        crimes.toArray(new CrimeLocation[crimes.size()]);

                new AsyncTask<CrimeLocation, CrimeLocation, Void>() {

                    @Override
                    protected Void doInBackground(CrimeLocation... crimes) {
                        for (CrimeLocation crime : crimes) {
                            publishProgress(crime);
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(CrimeLocation... values) {
                        super.onProgressUpdate(values);
                        mClusterManager.addItem(values[0]);
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        mClusterManager.cluster();
                    }
                }.execute(crimeLocations);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mClusterManager = new FetchingClusterManager(this, mMap, this);
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
    }

    @OnClick(R.id.action_settings)
    public void openSettings() {
        Intent openSettingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(openSettingsIntent);
    }

    // todo(CullyCross): later find safe way
    @Override
    public void onWayFound(List<List<LatLng>> safeWay) {
        for (List<LatLng> list : safeWay) {

            final PolylineOptions options = new PolylineOptions().width(5).color(Color.BLACK).geodesic(true);
            for (LatLng point : list) {
                options.add(point);
            }

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mMap.addPolyline(options);
                }
            });
        }
    }

    @OnClick(R.id.action_safe_way)
    protected void showDirectionsDialog() {
        DirectionsDialogFragment dialog = DirectionsDialogFragment.newInstance();
        dialog.show(getSupportFragmentManager(), FRAGMENT_DIALOG_DIRECTIONS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        initLocationHelper();
        initMapFragment();
        initParse();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void initLocationHelper() {
        mLocationHelper = LocationHelper.getInstance();
        mLocationHelper.connect();
    }

    private void initMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initParse() {
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        ParseObject.registerSubclass(CrimeLocation.class);
    }
}
