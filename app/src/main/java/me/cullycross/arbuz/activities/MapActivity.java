package me.cullycross.arbuz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import me.cullycross.arbuz.R;
import me.cullycross.arbuz.SettingsActivity;
import me.cullycross.arbuz.events.LocationFoundEvent;
import me.cullycross.arbuz.fragments.ArbuzMapFragment;
import me.cullycross.arbuz.utils.LocationHelper;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ArbuzMapFragment.OnMapInteractionListener {

    private GoogleMap mMap;

    private LocationHelper mLocationHelper;

    @Bind (R.id.action_settings)
    Button actionSettings;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initLocationHelper();
        initMapFragment();

        ButterKnife.bind(this);
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
