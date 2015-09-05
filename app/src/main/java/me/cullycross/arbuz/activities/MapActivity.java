package me.cullycross.arbuz.activities;

import android.graphics.Color;
import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Bind;
import de.greenrobot.event.EventBus;
import me.cullycross.arbuz.R;
import me.cullycross.arbuz.events.LocationFoundEvent;
import me.cullycross.arbuz.fragments.ArbuzMapFragment;
import me.cullycross.arbuz.fragments.DirectionsDialogFragment;
import me.cullycross.arbuz.utils.LocationHelper;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ArbuzMapFragment.OnMapInteractionListener {

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

    

    /**
     * Method for handling events, sent by EventBus
     * @param event location
     */
    public void onEvent(LocationFoundEvent event) {
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(event.getLocation(), 10f));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
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
}
