package me.cullycross.arbuz.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import me.cullycross.arbuz.content.CrimeLocation;
import me.cullycross.arbuz.events.DoneLoadEvent;
import me.cullycross.arbuz.events.FetchedDataEvent;
import me.cullycross.arbuz.utils.ParseHelper;

public class BackgroundQueueIntentService extends IntentService {

    private static final String TAG = BackgroundQueueIntentService.class.getName();

    private static final String ACTION_FETCH = "me.cullycross.arbuz.services.action.FETCH_ITEMS";
    private static final String EXTRA_LAT_LNG = "me.cullycross.arbuz.services.extra.LAT_LNG";
    private static final String EXTRA_DISTANCE = "me.cullycross.arbuz.services.extra.DISTANCE";

    public static void startActionFetch(Context context, LatLng point, double distance) {
        Intent intent = new Intent(context, BackgroundQueueIntentService.class);
        intent.setAction(ACTION_FETCH);
        intent.putExtra(EXTRA_LAT_LNG, point);
        intent.putExtra(EXTRA_DISTANCE, distance);
        context.startService(intent);
    }

    public BackgroundQueueIntentService() {
        super("BackgroundQueueIntentService");
    }



    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_FETCH:
                    final LatLng center = intent.getParcelableExtra(EXTRA_LAT_LNG);
                    final double distance = intent.getDoubleExtra(EXTRA_DISTANCE, 0);
                    handleActionFetch(center, distance);
                    break;
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

            @Override
            public void onDoneLoad() {
                EventBus.getDefault().post(new DoneLoadEvent());
            }
        });
    }
}
