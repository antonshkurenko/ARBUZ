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
import me.cullycross.arbuz.events.SaveToLocalStoreEvent;
import me.cullycross.arbuz.utils.ParseHelper;

public class BackgroundQueueIntentService extends IntentService {

    private static final String TAG = BackgroundQueueIntentService.class.getName();

    private static final String ACTION_FETCH = "me.cullycross.arbuz.services.action.FETCH_ITEMS";
    private static final String EXTRA_LAT_LNG = "me.cullycross.arbuz.services.extra.LAT_LNG";
    private static final String EXTRA_DISTANCE = "me.cullycross.arbuz.services.extra.DISTANCE";
    private static final String EXTRA_LOCAL_STORE = "me.cullycross.arbuz.services.extra.LOCAL_STORE";

    public static void startActionFetch(Context context, LatLng point, double distance, boolean localStore) {
        Intent intent = new Intent(context, BackgroundQueueIntentService.class);
        intent.setAction(ACTION_FETCH);
        intent.putExtra(EXTRA_LAT_LNG, point);
        intent.putExtra(EXTRA_DISTANCE, distance);
        intent.putExtra(EXTRA_LOCAL_STORE, localStore);
        context.startService(intent);
    }

    public static void startActionFetch(Context context, boolean localStore) {
        Intent intent = new Intent(context, BackgroundQueueIntentService.class);
        intent.setAction(ACTION_FETCH);
        intent.putExtra(EXTRA_LOCAL_STORE, localStore);
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
                    final boolean localStore = intent.getBooleanExtra(EXTRA_LOCAL_STORE, false);
                    if(center == null && distance == 0) {
                        handleActionFetch(localStore);
                    } else {
                        handleActionFetch(center, distance, localStore);
                    }
                    break;
            }
        }
    }


    private void handleActionFetch(LatLng center, double distance, boolean localStore) {
        final ParseGeoPoint point =
                new ParseGeoPoint(center.latitude, center.longitude);

        final ParseHelper.OnLoadCrimesListener listener = new ParseHelper.OnLoadCrimesListener() {
            @Override
            public void onLoadCrimes(List<CrimeLocation> crimes) {
                EventBus.getDefault().post(new FetchedDataEvent(crimes));
            }

            @Override
            public void onDoneLoad() {
                EventBus.getDefault().post(new DoneLoadEvent());
            }
        };

        if (localStore) {
            ParseHelper.getInstance().downloadNearFromLocalStore(point, distance, listener);
        } else {
            ParseHelper.getInstance().downloadNearFromParse(point, distance, listener);
        }
    }

    private void handleActionFetch(boolean localStore) {
        final ParseHelper.OnLoadCrimesListener listener = new ParseHelper.OnLoadCrimesListener() {
            @Override
            public void onLoadCrimes(List<CrimeLocation> crimes) {
                EventBus.getDefault().post(new SaveToLocalStoreEvent(crimes.size()));
            }

            @Override
            public void onDoneLoad() {
                EventBus.getDefault().post(new DoneLoadEvent());
            }
        };

        if (localStore) {
            ParseHelper.getInstance().downloadAllFromLocalStore(listener);
        } else {
            ParseHelper.getInstance().downloadAllFromParse(listener);
        }
    }
}
