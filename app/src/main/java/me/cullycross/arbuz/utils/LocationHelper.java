package me.cullycross.arbuz.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import me.cullycross.arbuz.events.LocationFoundEvent;
import me.cullycross.arbuz.events.LocationsListUpdateEvent;

/**
 * Created by: cullycross
 * Date: 9/4/15
 * For my shining stars!
 */
public class LocationHelper
        implements GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private static final int SCAN_PERIOD = 60 * 1000;
    private static final int SAVED_LOCATIONS_COUNT = 10;

    private static final int MAX_ELEMENTS = 3;
    private static final String TAG = LocationHelper.class.getName();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Geocoder mGeocoder;

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
        mGeocoder = new Geocoder(mContext, Locale.getDefault());

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
     * <p/>
     * {@link #onLocationChanged(Location)}
     */
    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this
        );
    }

    public void getDirections(LatLng from,
                              LatLng to,
                              String mode,
                              @NonNull final OnRoutesFoundListener listener) {

        String url = makeURL(
                from.latitude, from.longitude,
                to.latitude,
                to.longitude,
                mode
        );

        Log.v(TAG, url);

        RequestQueue queue = Volley.newRequestQueue(mContext);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Tranform the string into a json object
                            final JSONObject json = new JSONObject(response);

                            JSONArray routeArray = json.getJSONArray("routes");
                            List<String> encodedRoutes = getEncodedRoutes(routeArray);
                            List<List<LatLng>> routes = new ArrayList<>();
                            for(String s : encodedRoutes) {
                                routes.add(PolyUtil.decode(s));
                            }

                            if (listener != null) {
                                listener.onRouteFound(routes);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // ignore
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
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

        if (mSavedLocations.size() == SAVED_LOCATIONS_COUNT) {
            mSavedLocations.remove(SAVED_LOCATIONS_COUNT - 1);
        }
        mSavedLocations.add(location);

        EventBus.getDefault().post(new LocationsListUpdateEvent(mSavedLocations));
    }

    @Nullable
    public List<Address> fetchAddresses(String address) throws IOException {
        return mGeocoder.getFromLocationName(address, MAX_ELEMENTS);
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

    private String makeURL(double sourcelat, double sourcelng, double destlat,
                           double destlng, String mode) {

        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelng));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlng));
        urlString.append("&sensor=false&mode=");
        urlString.append(mode);
        urlString.append("&alternatives=true");

        return urlString.toString();
    }

    private List<String> getEncodedRoutes(JSONArray routes) throws JSONException{
        List<String> encodedRoutes = new ArrayList<>();

        for(int i = 0; i < routes.length(); i++) {
            final JSONObject route = routes.getJSONObject(i);
            JSONObject overviewPolylines = route
                    .getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            encodedRoutes.add(encodedString);
        }

        for(String s : encodedRoutes) {
            Log.v(TAG, s);
        }
        return encodedRoutes;
    }

    public interface OnRoutesFoundListener {
        void onRouteFound(List<List<LatLng>> routes);
    }
}
