package hu.user.kardioapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;

public class HealthMattersActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_matters);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ButterKnife.bind(this);

      /*  final EditText etBloodValue = (EditText) findViewById(R.id.etBloodValue);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rgHighBlood);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if (checkedId == R.id.rbHighBloodYes)
                {
                    etBloodValue.setVisibility(View.VISIBLE);
                }
                else etBloodValue.setVisibility(View.INVISIBLE);
            }
        });*/

        Button back = (Button) findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(HealthMattersActivity.this, PersonalDetailsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button openMap = (Button) findViewById(R.id.btn_openMap);
        openMap.setOnClickListener(new View.OnClickListener()
                                   {
                                       @Override
                                       public void onClick(View view)
                                       {
                                           Intent intent = new Intent(HealthMattersActivity.this, MapActivity.class);
                                           startActivity(intent);
                                           finish();
                                       }
                                   }
        );
    }
}
