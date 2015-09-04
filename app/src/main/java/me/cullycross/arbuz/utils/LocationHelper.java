package me.cullycross.arbuz.utils;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import me.cullycross.arbuz.events.LocationFoundEvent;
import me.cullycross.arbuz.events.LocationsListUpdateEvent;

/**
 * Created by: cullycross
 * Date: 9/4/15
 * For my shining stars!
 */
public class LocationHelper implements
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private static final int SCAN_PERIOD = 60 * 1000;
    private static final int SAVED_LOCATIONS_COUNT = 10;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private Context mContext;

    private final List<Location> mSavedLocations;

    // Location helper singleton
    private static final LocationHelper INSTANCE = new LocationHelper();

    public static LocationHelper getInstance() {
        return INSTANCE;
    }

    protected LocationHelper() {
        mSavedLocations = new ArrayList<>(SAVED_LOCATIONS_COUNT);
    }

    public void init(Context ctx) {
        mContext = ctx.getApplicationContext();

        buildGoogleApiClient();
        createLocationRequest();
    }


    /**
     * connect to location API, also find last last known location
     */
    public void connect() {
        mGoogleApiClient.connect();
    }


    /**
     * start monitor for location updates
     */
    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this
        );
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        LatLng point;

        if (lastLocation != null) {
            point = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        } else {
            point = new LatLng(50.450091, 30.523415); // Kyiv
        }

        EventBus.getDefault().postSticky(new LocationFoundEvent(point));
    }

    @Override
    public void onConnectionSuspended(int i) {
        // currently ignored
    }

    // todo(CullyCross): test it with real device
    @Override
    public void onLocationChanged(Location location) {

        if(mSavedLocations.size() == SAVED_LOCATIONS_COUNT) {
            mSavedLocations.remove(SAVED_LOCATIONS_COUNT - 1);
        }
        mSavedLocations.add(location);

        EventBus.getDefault().post(new LocationsListUpdateEvent(mSavedLocations));
    }

    /**
     * for periodic location requests
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(SCAN_PERIOD);
        mLocationRequest.setFastestInterval(SCAN_PERIOD / 2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildGoogleApiClient() {
        // todo(CullyCross): handle connection error later via adding listener
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }
}
