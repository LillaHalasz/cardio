package hu.user.kardioapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HealthMattersActivity extends AppCompatActivity
{
    BluetoothLeService mBluetoothLeService;

    @Bind(R.id.rgHighBlood)
    RadioGroup rgHighBlood;
    @Bind(R.id.etHighBloodValue)
    EditText etBloodValue;
    @Bind(R.id.tvHighBloodValue)
    TextView tvHighBloodValue;
    @Bind(R.id.rgTriglicerid)
    RadioGroup rgTriglicerid;
    @Bind(R.id.etTriValue)
    EditText etTriValue;
    @Bind(R.id.tvTriValue)
    TextView tvTriValue;
    @Bind(R.id.btn_back)
    Button back;
    @Bind(R.id.btn_openMap)
    Button openMap;
    @Bind(R.id.et_height)
    EditText etHeight;
    @Bind(R.id.et_weight)
    EditText etWeight;


    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_matters);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Egészségi állapot");
        ButterKnife.bind(this);

        rgHighBlood.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if (checkedId == R.id.rbHighBloodYes)
                {
                    etBloodValue.setVisibility(View.VISIBLE);
                    tvHighBloodValue.setVisibility(View.VISIBLE);
                }
                else
                {
                    etBloodValue.setVisibility(View.INVISIBLE);
                    tvHighBloodValue.setVisibility(View.INVISIBLE);
                }

            }
        });

        rgTriglicerid.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if (checkedId == R.id.rbHighTriYes)
                {
                    etTriValue.setVisibility(View.VISIBLE);
                    tvTriValue.setVisibility(View.VISIBLE);
                }
                else
                {
                    etTriValue.setVisibility(View.INVISIBLE);
                    tvTriValue.setVisibility(View.INVISIBLE);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
              /*  try
                {
                    if (mBluetoothLeService != null)
                    {
                        mBluetoothLeService.readCustomCharacteristic();
                    }
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
*/
                Intent intent = new Intent(HealthMattersActivity.this, PersonalDetailsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        openMap.setOnClickListener(new View.OnClickListener()
                                   {
                                       @Override
                                       public void onClick(View view)
                                       {
                                           sharedPreferences = getSharedPreferences("Personal", Context.MODE_PRIVATE);
                                           SharedPreferences.Editor editor = sharedPreferences.edit();
                                           editor.putInt("Weight", Integer.valueOf(etWeight.getText().toString()));
                                           editor.putInt("Height", Integer.valueOf(etHeight.getText().toString()));
                                           editor.commit();
                                           // Intent intent = new Intent(HealthMattersActivity.this, WalkingScreenActivity.class);
                                           Intent intent = new Intent(HealthMattersActivity.this, DeviceScanActivity.class);
                                           startActivity(intent);
                                           finish();
                                       }
                                   }
        );
    }
}
