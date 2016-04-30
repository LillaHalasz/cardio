package hu.user.kardioapplication;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by User on 2016.04.21..
 */
public class SummaryScreenActivity extends FragmentActivity
{
    @Bind(R.id.btnExit)
    Button btnExit;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Összesítés");
        ButterKnife.bind(this);

        btnExit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

    }

}
