package me.cullycross.arbuz.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.ClusterManager;

import me.cullycross.arbuz.content.CrimeLocation;

/**
 * Created by: cullycross
 * Date: 9/6/15
 * For my shining stars!
 */
public class FetchingClusterManager extends ClusterManager<CrimeLocation> {

    private OnCameraChangeListener mListener;

    public FetchingClusterManager(Context context, GoogleMap map, @NonNull OnCameraChangeListener listener) {
        super(context, map);
        mListener = listener;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        super.onCameraChange(cameraPosition);
        if (mListener != null) {
            mListener.onCameraChanged(cameraPosition);
        }
    }

    public interface OnCameraChangeListener {
        void onCameraChanged(CameraPosition cameraPosition);
    }
}
