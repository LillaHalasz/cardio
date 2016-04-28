package hu.user.kardioapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by User on 2016.04.20..
 */
public class WalkingScreenActivity extends FragmentActivity
{
    @Bind(R.id.btnFinish)
    Button btnFinish;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walking);
        ButterKnife.bind(this);

        btnFinish.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i("WalkingScreen", "finishClick");
                stopService(new Intent(WalkingScreenActivity.this, BackgroundLocationService.class));
                stopService(new Intent(WalkingScreenActivity.this, BackgroundPulseService.class));
                startActivity(new Intent(WalkingScreenActivity.this, SummaryScreenActivity.class));
                finish();
            }
        });
    }
}
