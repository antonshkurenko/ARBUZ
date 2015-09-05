package me.cullycross.arbuz.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.parse.ParseGeoPoint;

import java.util.List;

import de.greenrobot.event.EventBus;
import me.cullycross.arbuz.content.CrimeLocation;
import me.cullycross.arbuz.events.FetchedDataEvent;
import me.cullycross.arbuz.utils.ParseHelper;

public class FetchLocationsIntentService extends IntentService {

    private static final String ACTION_FETCH = "me.cullycross.arbuz.services.action.FETCH_ITEMS";
    private static final String EXTRA_LAT_LNG = "me.cullycross.arbuz.services.extra.LAT_LNG";
    private static final String EXTRA_DISTANCE = "me.cullycross.arbuz.services.extra.DISTANCE";


    public static void startActionFetch(Context context, LatLng point, double distance) {
        Intent intent = new Intent(context, FetchLocationsIntentService.class);
        intent.setAction(ACTION_FETCH);
        intent.putExtra(EXTRA_LAT_LNG, point);
        intent.putExtra(EXTRA_DISTANCE, distance);
        context.startService(intent);
    }

    public FetchLocationsIntentService() {
        super("FetchLocationsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH.equals(action)) {
                final LatLng center = intent.getParcelableExtra(EXTRA_LAT_LNG);
                final double distance = intent.getDoubleExtra(EXTRA_DISTANCE, 0);
                handleActionFetch(center, distance);
            }
        }
    }


    private void handleActionFetch(LatLng center, double distance) {
        ParseGeoPoint point =
                new ParseGeoPoint(center.latitude, center.longitude);
        ParseHelper.getInstance().downloadNear(point, distance, new ParseHelper.OnLoadCrimesListener() {
            @Override
            public void onLoadCrimes(List<CrimeLocation> crimes) {
                EventBus.getDefault().post(new FetchedDataEvent(crimes));
            }
        });
    }
}
