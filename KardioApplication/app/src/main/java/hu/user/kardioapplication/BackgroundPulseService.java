package hu.user.kardioapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by User on 2016.04.14..
 */
public class BackgroundPulseService extends Service
{

    private Timer timer = null;

    final Handler mHandler = new Handler();
    private Intent mIntent;

    class MyTimerTask extends TimerTask
    {

        @Override
        public void run()
        {
            mIntent = new Intent("getHeartRate");
            Log.i("PulseService", "run");
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    int hr = genRandomNumber(80, 120);
                    mIntent.putExtra("heartRate", hr);
                    Log.i("PulseService", "randomNumber = " + hr);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);

                }
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    MyTimerTask myTimerTask = new MyTimerTask();

    @Override
    public void onCreate()
    {
        Log.i("PulseService", "onCreate");
        super.onCreate();

        if (timer != null)
        {
            timer.cancel();
        }
        else
        {
            timer = new Timer();
        }
        timer.schedule(myTimerTask, 0, 5000);
    }

    @Override
    public void onDestroy()
    {
        Log.i("PulseService", "stopTimerTask");
        super.onDestroy();
        myTimerTask.cancel();
    }

    public int genRandomNumber(int min, int max)
    {
        Random rand = new Random();
        int randomNumber = rand.nextInt((max - min) + 1) + min;
        return randomNumber;
    }
}
