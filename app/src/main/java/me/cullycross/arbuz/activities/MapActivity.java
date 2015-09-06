package me.cullycross.arbuz.activities;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import me.cullycross.arbuz.R;
import me.cullycross.arbuz.content.CrimeLocation;
import me.cullycross.arbuz.events.FetchedDataEvent;
import me.cullycross.arbuz.events.LocationFoundEvent;
import me.cullycross.arbuz.fragments.ArbuzMapFragment;
import me.cullycross.arbuz.fragments.DirectionsDialogFragment;
import me.cullycross.arbuz.services.FetchLocationsIntentService;
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

    /*@Bind(R.id.toggle_button_heatmap)
    ToggleButton mToggleButtonHeatmap;*/
    @Bind(R.id.action_safe_way)
    Button mActionSafeWay;
    @Bind(R.id.action_red_button)
    Button mActionRedButton;
    @Bind(R.id.action_settings)
    Button mActionSettings;

    private GoogleMap mMap;

    private LocationHelper mLocationHelper;

    private FetchingClusterManager mClusterManager;
    /*
    private HeatmapTileProvider mHeatmapTileProvider = null;
    private TileOverlay mHeatTileOverlay;

    private List<WeightedLatLng> mWeightedCrimes = null;
    */

    /**
     * Method for handling found location
     *
     * @param event location
     */
    public void onEvent(LocationFoundEvent event) {
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(event.getLocation(), 14f));
        }
    }

    /**
     * Method for handling fetched data from the background queue
     *
     * @param event contains locations
     */
    public void onEventMainThread(FetchedDataEvent event) {
        mClusterManager.addItems(event.getLocations());
        mClusterManager.cluster();
        //addWeightedToHeatmap();
    }

    /**
     * todo(CullyCross): later add heatmap
     */
    /*@OnCheckedChanged(R.id.toggle_button_heatmap)
    public void onChecked(boolean flag) {
        if (flag) {
            if (mWeightedCrimes == null ||
                    ParseHelper.getInstance().getCrimes().size() != mWeightedCrimes.size()) {
                addWeightedToHeatmap();
                mHeatTileOverlay = mMap.addTileOverlay(
                        new TileOverlayOptions().tileProvider(mHeatmapTileProvider));
            }
            mHeatTileOverlay.setVisible(true);
            for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
                (marker).setVisible(false);
            }

            for(Marker marker : mClusterManager.getClusterMarkerCollection().getMarkers()) {
                (marker).setVisible(false);
            }
        } else {
            mHeatTileOverlay.setVisible(false);
            for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
                (marker).setVisible(true);
            }

            for(Marker marker : mClusterManager.getClusterMarkerCollection().getMarkers()) {
                (marker).setVisible(true);
            }
        }
    }*/

    @Override
    public void onCameraChanged(CameraPosition cameraPosition) {
        FetchLocationsIntentService
                .startActionFetch(
                        this,
                        cameraPosition.target,
                        SphericalUtil.computeDistanceBetween(
                                cameraPosition.target,
                                mMap.getProjection()
                                        .getVisibleRegion()
                                        .latLngBounds.northeast)
                );
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

    /*private void addWeightedToHeatmap() {
        mWeightedCrimes =
                ParseHelper
                        .getInstance()
                        .convertCrimeToWeighted(ParseHelper.getInstance().getCrimes());

        if (mHeatmapTileProvider == null && mWeightedCrimes.size() != 0) {
            mHeatmapTileProvider = new HeatmapTileProvider
                    .Builder()
                    .weightedData(mWeightedCrimes)
                    .build();
        } else {
            mHeatmapTileProvider.setWeightedData(mWeightedCrimes);
        }
    }*/
}
