package hu.user.kardioapplication;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by User on 2016.04.20..
 */
public class HeartRateFragment extends Fragment
{

    int heartRate;
    private TextView tvHeartRate;
    private ImageView img;
    private List<Integer> measuredHeartRate;
    public HeartRateFragment()
    {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.heartrate, container, false);

        tvHeartRate = (TextView) view.findViewById(R.id.tv_hr);
        img = (ImageView) view.findViewById(R.id.indicator);
        measuredHeartRate = new ArrayList<>();

        getActivity().startService(new Intent(getActivity(), BackgroundPulseService.class));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(heartRateUpdateReceiver, new IntentFilter("getHeartRate"));
        return view;
    }

    private BroadcastReceiver heartRateUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i("MapActivity", "ReceiveHeartRate");
            heartRate = intent.getIntExtra("heartRate", 0);
            measuredHeartRate.add(heartRate);
            Log.i("MapActivity", "" + heartRate);
            tvHeartRate.setText("" + heartRate);
            pumpHeart();

        }
    };

    private void pumpHeart()
    {
        img.animate().scaleXBy(0.2f).scaleYBy(0.2f).setDuration(50).setListener(scaleUpListener);
        //playBeat();
    }

    private Animator.AnimatorListener scaleDownListener = new Animator.AnimatorListener()
    {

        @Override
        public void onAnimationStart(Animator animation)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAnimationRepeat(Animator animation)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAnimationEnd(Animator animation)
        {
            // img.animate().scaleXBy(0.2f).scaleYBy(0.2f).setDuration(100).setListener(scaleUpListener);
        }

        @Override
        public void onAnimationCancel(Animator animation)
        {
            // TODO Auto-generated method stub

        }
    };

    private Animator.AnimatorListener scaleUpListener = new Animator.AnimatorListener()
    {

        @Override
        public void onAnimationStart(Animator animation)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAnimationRepeat(Animator animation)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAnimationEnd(Animator animation)
        {
            img.animate().scaleXBy(-0.2f).scaleYBy(-0.2f).setDuration(50).setListener(scaleDownListener);

        }

        @Override
        public void onAnimationCancel(Animator animation)
        {
            // TODO Auto-generated method stub

        }
    };


}
