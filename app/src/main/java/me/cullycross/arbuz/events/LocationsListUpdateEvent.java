package me.cullycross.arbuz.events;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by: cullycross
 * Date: 9/4/15
 * For my shining stars!
 */
public class LocationsListUpdateEvent extends Event {

    protected final List<Location> mLocations;

    public LocationsListUpdateEvent(List<Location> locations) {
        mLocations = locations;
    }

    public List<Location> getLocations() {
        return mLocations;
    }
}
