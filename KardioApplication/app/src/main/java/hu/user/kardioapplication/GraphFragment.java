package hu.user.kardioapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by User on 2016.04.21..
 */
public class GraphFragment extends Fragment
{
    @Bind(R.id.tvMaxBpmValue)
    TextView maxBpmValue;
    @Bind(R.id.tvMinBpmValue)
    TextView minBpmValue;
    @Bind(R.id.tvAverageBpmValue)
    TextView averageBpmValue;
	

    public GraphFragment()
    {
    }

    LineChart mChart;
    ArrayList<Entry> entries;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.graph, container, false);
        ButterKnife.bind(this, view);
        String file = "myvalues";

        entries = new ArrayList<>();
        entries = readSavedBpmValues(file);

        mChart = (LineChart) view.findViewById(R.id.chart);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisLineColor(Color.rgb(0, 121, 107));
        xAxis.setTextColor(Color.rgb(0, 121, 107));
        xAxis.setGridColor(Color.LTGRAY);

        YAxis leftAxis = mChart.getAxisLeft();
        YAxis rightAxis = mChart.getAxisRight();

        rightAxis.setDrawLabels(false);
        leftAxis.setAxisLineColor(Color.rgb(0, 121, 107));
        leftAxis.setTextColor(Color.rgb(0, 121, 107));
        leftAxis.setGridColor(Color.LTGRAY);


        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++)
        {
            xVals.add((i) + "");
        }
        LineDataSet set1 = new LineDataSet(entries, "");

        set1.setDrawCircles(false);
        set1.setColor(Color.rgb(0, 121, 107));
        set1.setLineWidth(2f);
        set1.setDrawValues(false);


        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData lineData = new LineData(xVals, dataSets);
        mChart.setData(lineData);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("");
        mChart.invalidate();

        float max = 0;
        float min = entries.get(0).getVal();
        float sum = 0;

        for (int i = 0; i < entries.size(); i++)
        {

            if (entries.get(i).getVal() > max) max = entries.get(i).getVal();
            if (entries.get(i).getVal() < min) min = entries.get(i).getVal();
            sum += entries.get(i).getVal();
        }

        maxBpmValue.setText(Float.toString(max));
        minBpmValue.setText(Float.toString(min));
        averageBpmValue.setText(Float.toString(sum / entries.size()));

        return view;
    }


    public ArrayList<Entry> readSavedBpmValues(String filename)
    {
        ArrayList<Entry> points = null;
        try
        {
            FileInputStream in = getActivity().openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                sb.append(line);

            }
            String string = sb.toString();
            Log.i("String", string);

            String[] raw = string.split(",");

            points = new ArrayList<>();
            for (int i = 0; i < raw.length; i++)
            {
                points.add(new Entry(Integer.parseInt(raw[i]), i));
            }

            String dir = getActivity().getFilesDir().getAbsolutePath();
            File fileToDelete = new File(dir, filename);
            boolean isDeleted = fileToDelete.delete();
            Log.i("Delete Check", "File deleted: " + dir + "/myFile " + isDeleted);

            inputStreamReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return points;
    }
}
