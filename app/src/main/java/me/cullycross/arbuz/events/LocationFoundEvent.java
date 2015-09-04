package me.cullycross.arbuz.events;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by: cullycross
 * Date: 9/4/15
 * For my shining stars!
 */
public class LocationFoundEvent extends Event {

    protected final LatLng mLocation;

    public LatLng getLocation() {
        return mLocation;
    }

    public LocationFoundEvent(LatLng location) {
        mLocation = location;
    }

    public LocationFoundEvent(Location location) {
        mLocation = new LatLng(
                location.getLatitude(),
                location.getLongitude());
    }
}
