package me.cullycross.arbuz.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterManager;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import me.cullycross.arbuz.R;
import me.cullycross.arbuz.adapters.CrimeInfoViewAdapter;
import me.cullycross.arbuz.content.CrimeLocation;
import me.cullycross.arbuz.events.DoneLoadEvent;
import me.cullycross.arbuz.events.FetchedDataEvent;
import me.cullycross.arbuz.events.LocationFoundEvent;
import me.cullycross.arbuz.fragments.ArbuzMapFragment;
import me.cullycross.arbuz.fragments.DirectionsDialogFragment;
import me.cullycross.arbuz.services.BackgroundQueueIntentService;
import me.cullycross.arbuz.services.LocationService;
import me.cullycross.arbuz.utils.FetchingClusterManager;
import me.cullycross.arbuz.utils.LocationHelper;

/**
 * todo(CullyCross): later add heatmap
 */

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ArbuzMapFragment.OnMapInteractionListener,
        DirectionsDialogFragment.OnFragmentInteractionListener,
        FetchingClusterManager.OnCameraChangeListener {

    private static final String TAG = MapActivity.class.getName();

    @Bind(R.id.toggle_button_location)
    ToggleButton mToggleButtonLocation;
    @Bind(R.id.action_safe_way)
    Button mActionSafeWay;
    @Bind(R.id.action_settings)
    Button mActionSettings;
    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;

    private static final String FRAGMENT_DIALOG_DIRECTIONS = "fragment_dialog_directions";

    private static final int CAMERA_POSITION_CHANGED = 100;
    private static final int DEFAULT_FETCH_DELAY = 750;

    private GoogleMap mMap;

    private LocationHelper mLocationHelper;

    private FetchingClusterManager mClusterManager;

    private CrimeLocation mClickedCrime;

    private Polyline mLine = null;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            final CameraPosition cameraPosition = ((CameraPosition) msg.obj);

            BackgroundQueueIntentService
                    .startActionFetch(
                            MapActivity.this,
                            cameraPosition.target,
                            SphericalUtil.computeDistanceBetween(
                                    cameraPosition.target,
                                    mMap.getProjection()
                                            .getVisibleRegion()
                                            .latLngBounds.northeast)
                    );
        }
    };

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
    }

    /**
     * Method for hiding progressbar on done load of the crimes
     *
     * @param event contains locations
     */
    public void onEventMainThread(DoneLoadEvent event) {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    public CrimeLocation getClickedCrime() {
        return mClickedCrime;
    }

    @OnCheckedChanged(R.id.toggle_button_location)
    public void onChecked(boolean flag) {
        if (flag) {
            startService(new Intent(this, LocationService.class));
        } else {
            stopService(new Intent(this, LocationService.class));
        }
    }

    @Override
    public void onCameraChanged(CameraPosition cameraPosition) {

        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.bringToFront();

        mHandler.removeMessages(CAMERA_POSITION_CHANGED);
        mHandler.sendMessageDelayed(
                mHandler.obtainMessage(CAMERA_POSITION_CHANGED, cameraPosition), DEFAULT_FETCH_DELAY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mClusterManager = new FetchingClusterManager(this, mMap, this);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<CrimeLocation>() {
            @Override
            public boolean onClusterItemClick(CrimeLocation crimeLocation) {
                mClickedCrime = crimeLocation;
                return false;
            }
        });
        mClusterManager
                .getMarkerCollection()
                .setOnInfoWindowAdapter(new CrimeInfoViewAdapter(
                        LayoutInflater.from(this), this));

        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
    }

    @OnClick(R.id.action_settings)
    public void openSettings() {
        Intent openSettingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(openSettingsIntent);
    }

    @Override
    public void onWayFound(final List<LatLng> safeWay) {

        final PolylineOptions options = new PolylineOptions().width(7).color(Color.BLUE).geodesic(true);
        for (LatLng point : safeWay) {
            options.add(point);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mLine != null) {
                    mLine.remove();
                }
                mLine = mMap.addPolyline(options);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(safeWay.get(0))
                        .include(safeWay.get(safeWay.size() - 1));

                LatLngBounds latLngBounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50));
            }
        });
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
