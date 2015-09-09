package me.cullycross.arbuz.utils;

import android.content.Context;
import android.util.Log;

import com.android.internal.util.Predicate;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.cullycross.arbuz.content.CrimeLocation;

/**
 * Created by: cullycross
 * Date: 9/5/15
 * For my shining stars!
 */
public class ParseHelper {

    public static final float NEAR_DISTANCE = 0.3f;

    private static final int FETCH_OBJECTS_LIMIT = 25;
    private static final String TAG = ParseHelper.class.getName();
    private Context mContext;

    private final NoLimitFindCallback mFindCallback;

    // store in the RAM
    private final static Set<CrimeLocation> sCrimes = new HashSet<>();

    // skip values
    private int mSkip = 0;

    private static final ParseHelper INSTANCE = new ParseHelper();

    public static ParseHelper getInstance() {
        return INSTANCE;
    }

    protected ParseHelper() {
        mFindCallback = new NoLimitFindCallback();
    }

    public void init(Context ctx) {
        mContext = ctx;
    }

    public Set<CrimeLocation> getCrimes() {
        return sCrimes;
    }

    public void downloadAll(OnLoadCrimesListener listener) {
        ParseQuery<CrimeLocation> query = ParseQuery.getQuery(CrimeLocation.class);
        query.setLimit(FETCH_OBJECTS_LIMIT);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);

        mFindCallback.mListener = listener;

        query.findInBackground(mFindCallback);
    }

    public void downloadNear(ParseGeoPoint near, OnLoadCrimesListener listener) {
        downloadNear(near, 0, listener);
    }

    public void downloadNear(ParseGeoPoint near, double distance, OnLoadCrimesListener listener) {
        ParseQuery<CrimeLocation> query = ParseQuery.getQuery(CrimeLocation.class);
        query.setLimit(FETCH_OBJECTS_LIMIT);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);

        distance /= 1000;

        mFindCallback.mListener = listener;
        mFindCallback.mNear = near;
        mFindCallback.mDistance = distance;

        if (distance != 0) {
            distance = NEAR_DISTANCE;
        }

        if (near != null) {
            query.whereWithinKilometers(
                    CrimeLocation.LOCATION,
                    near,
                    distance);
        }

        query.findInBackground(mFindCallback);
    }

    private class NoLimitFindCallback implements FindCallback<CrimeLocation> {

        ParseGeoPoint mNear;
        OnLoadCrimesListener mListener;
        double mDistance;

        @Override
        public void done(List<CrimeLocation> locations, ParseException e) {
            if (e == null && locations.size() != 0) {

                locations.removeAll(sCrimes);

                if (locations.size() != 0) {
                    if (mListener != null) {
                        mListener.onLoadCrimes(locations);
                    }

                    Log.d(TAG, "Before: sCrimes.size() = " + sCrimes.size() +
                            " locations.size() = " + locations.size() + " sum = " + (sCrimes.size() + locations.size()));
                    sCrimes.addAll(locations);

                    mSkip += FETCH_OBJECTS_LIMIT;

                    ParseQuery<CrimeLocation> query = ParseQuery.getQuery(CrimeLocation.class);

                    if (mDistance == 0) {
                        mDistance = NEAR_DISTANCE;
                    }

                    if (mNear != null) {
                        query.whereWithinKilometers(
                                CrimeLocation.LOCATION,
                                mNear,
                                mDistance);
                    }

                    query.setLimit(FETCH_OBJECTS_LIMIT)
                            .setSkip(mSkip)
                            .setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK)
                            .findInBackground(this);
                }
            } else {
                // reset everything

                if (mListener != null) {
                    mListener.onDoneLoad();
                }
                mNear = null;
                mListener = null;
                mDistance = 0;
                mSkip = 0;
            }
        }
    }

    public interface OnLoadCrimesListener {
        void onLoadCrimes(List<CrimeLocation> crimes);
        void onDoneLoad();
    }
}
