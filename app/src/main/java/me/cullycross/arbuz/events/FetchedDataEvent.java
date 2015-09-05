package me.cullycross.arbuz.events;

import android.location.Location;

import java.util.List;

import me.cullycross.arbuz.content.CrimeLocation;

/**
 * Created by: cullycross
 * Date: 9/6/15
 * For my shining stars!
 */
public class FetchedDataEvent extends Event {

    protected final List<CrimeLocation> mLocations;

    public FetchedDataEvent(List<CrimeLocation> locations) {
        mLocations = locations;
    }

    public List<CrimeLocation> getLocations() {
        return mLocations;
    }

}
