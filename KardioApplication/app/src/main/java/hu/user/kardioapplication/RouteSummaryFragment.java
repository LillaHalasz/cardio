package hu.user.kardioapplication;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by User on 2016.04.29..
 */
public class RouteSummaryFragment extends Fragment
{
    @Bind(R.id.tvDistanceValue)
    TextView tvDistanceValue;
    @Bind(R.id.tvDurationValue)
    TextView tvDurationValue;
    SharedPreferences sharedPreferences;

    public RouteSummaryFragment()
    {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.routesummary, container, false);
        ButterKnife.bind(this, view);
        JodaTimeAndroid.init(getContext());

        sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("Details", Context.MODE_PRIVATE);
        double distance = (sharedPreferences.getInt("distance", 20000000));
        double distanceInM = distance / 100;
        double distanceInKm = distanceInM / 1000;
        double distanceInCm = distanceInM / 100;
        long startTimeMillis = sharedPreferences.getLong("startTime", 0);
        long endTimeMillis = sharedPreferences.getLong("endTime", 0);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.commit();

        if (startTimeMillis == 0)
        {
            String duration = CalculateDuration(0,0);
            tvDurationValue.setText(duration);
        }
        else
        {
            String duration = CalculateDuration(startTimeMillis, endTimeMillis);
            tvDurationValue.setText(duration);
        }

        return view;
    }


    public String CalculateDuration(long start, long stop)
    {
        DateTime startTime = new DateTime(start);
        DateTime endTime = new DateTime(stop);
        Duration duration = new Duration(startTime, endTime);
        Period period = duration.toPeriod();
        PeriodFormatter format = new PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendHours()
                .appendLiteral(":")
                .appendMinutes()
                .appendLiteral(":")
                .appendSeconds()
                .toFormatter();
        return format.print(period);
    }
}
