package hu.user.kardioapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by User on 2016.05.11..
 */
public class WalkingActivity extends AppCompatActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walk);
        ButterKnife.bind(this);
    }
}
