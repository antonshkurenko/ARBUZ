package me.cullycross.arbuz;

import android.app.Application;

import com.parse.Parse;

import me.cullycross.arbuz.utils.LocationHelper;
import me.cullycross.arbuz.utils.ParseHelper;

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
        ParseHelper.getInstance().init(this);
        Parse.initialize(this,
                getResources().getString(R.string.parse_application_id),
                getResources().getString(R.string.parse_client_key));
    }
}
