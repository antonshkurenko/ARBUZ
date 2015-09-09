package me.cullycross.arbuz.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import de.greenrobot.event.EventBus;
import me.cullycross.arbuz.R;
import me.cullycross.arbuz.events.Event;
import me.cullycross.arbuz.events.LocationsListUpdateEvent;
import me.cullycross.arbuz.utils.LocationHelper;
import me.cullycross.arbuz.utils.ParseHelper;

/**
 * Created by: cullycross
 * Date: 9/4/15
 * For my shining stars!
 */
public class LocationService extends Service {

    private static final String TAG = LocationService.class.getCanonicalName();

    private static final int NOTIFICATION_ID = 22;

    private NotificationManagerCompat mNotificationManager;

    /**
     * handle location changes
     * @param event contains list of the last locations
     */
    public void onEventBackgroundThread(LocationsListUpdateEvent event) {
        Location before = event.getLocations().get(1);
        Location now = event.getLocations().get(0);

        LatLng beforeLatLng = new LatLng(
                before.getLatitude(),
                before.getLongitude()
        );

        LatLng nowLatLng = new LatLng(
                now.getLatitude(),
                now.getLongitude()
        );

        int nowPoints = LocationHelper
                .getInstance()
                .getLocationPoints(
                        nowLatLng, ParseHelper.getInstance().getCrimes());

        LatLng predictionLatLng = SphericalUtil.computeOffset(nowLatLng,
                2 * SphericalUtil.computeDistanceBetween(
                        beforeLatLng, nowLatLng),
                SphericalUtil.computeHeading(beforeLatLng, nowLatLng)
                );

        int predictionPoints = LocationHelper
                .getInstance()
                .getLocationPoints(
                        predictionLatLng, ParseHelper.getInstance().getCrimes());

        Log.d(TAG, "Now points: " + nowPoints + ", " + "prediction points: " + predictionPoints);

        if (predictionPoints * 0.75f > nowPoints) {
            showNotification("Warning!", "Warning!", "You're moving into the dangerous zone.");
        } else if (predictionPoints < nowPoints * 0.75f) {
            showNotification("Gratz!", "Gratz!", "You're moving out of the dangerous zone.");
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        mNotificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LocationHelper.getInstance().startLocationUpdates();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification(String ticker, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_location_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setTicker(ticker)
                .setAutoCancel(true);

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
