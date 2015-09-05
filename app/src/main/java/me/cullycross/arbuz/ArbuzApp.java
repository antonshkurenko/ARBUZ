package me.cullycross.arbuz;

import android.app.Application;

import me.cullycross.arbuz.utils.LocationHelper;

/**
 * Created by: cullycross
 * Date: 9/4/15
 * For my shining stars!
 */
public class ArbuzApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        LocationHelper.getInstance().init(this);
    }
}
