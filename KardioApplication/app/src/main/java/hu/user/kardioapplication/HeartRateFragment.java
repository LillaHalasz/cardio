package hu.user.kardioapplication;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by User on 2016.04.20..
 */
public class HeartRateFragment extends Fragment
{
    @Bind(R.id.tv_hr) TextView tvHeartRate;
    @Bind(R.id.indicator) ImageView img;

    int heartRate;
    public List<Integer> measuredHeartRate;

    public HeartRateFragment()
    {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.heartrate, container, false);
        ButterKnife.bind(this, view);

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

            if (heartRate < 110)
            {
                // ((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(2000);
            }
            WriteToFile(heartRate);
        }
    };

    private void WriteToFile(int value)
    {
        String stringToWrite = String.valueOf(value);
        String file = "myvalues";
        String separator = ",";
        try {
            FileOutputStream fOut = getActivity().openFileOutput(file,getActivity().MODE_APPEND);
            fOut.write(stringToWrite.getBytes());
            fOut.write(separator.getBytes());
            fOut.close();
        }

        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void pumpHeart()
    {
        img.animate().scaleXBy(0.2f).scaleYBy(0.2f).setDuration(50).setListener(scaleUpListener);
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
