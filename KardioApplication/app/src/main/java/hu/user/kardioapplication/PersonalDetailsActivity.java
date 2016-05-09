package hu.user.kardioapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PersonalDetailsActivity extends AppCompatActivity
{
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private String mDeviceName;
    private String mDeviceAddress;
    BluetoothLeService mBluetoothLeService;

    @Bind(R.id.et_lastname)
    EditText etLastName;
    @Bind(R.id.et_firstname)
    EditText etFirstName;
    @Bind(R.id.et_telnumber)
    EditText etTelNum;
    @Bind(R.id.et_email)
    EditText email;
    @Bind(R.id.datePicker)
    DatePicker datePicker;
    @Bind(R.id.et_birthplace)
    EditText etBirthPlace;
    @Bind(R.id.btn_continue)
    Button btnContinue;

    SharedPreferences sharedPreferences;

    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service)
        {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize())
            {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_details);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Személyes adatok");

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

      /*  Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);*/

        ButterKnife.bind(this);

        setDividerColor(datePicker, Color.MAGENTA);

        //checkViews();


      /*  btnContinue.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent intent = new Intent(PersonalDetailsActivity.this, HealthMattersActivity.class);
                startActivity(intent);
                finish();
            }
        });*/


    }
    @OnClick(R.id.btn_continue)
    public void startActivity(Button button)
    {
       /* try
        {
            if(mBluetoothLeService != null)
            {
                mBluetoothLeService.writeCustomCharacteristic(etBirthPlace.getText().toString());
            }
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }*/

        sharedPreferences = getSharedPreferences("Personal", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(etFirstName.getText().toString(),"FirstName");
        editor.putString(etLastName.getText().toString(),"LastName");
        editor.putString(etTelNum.getText().toString(),"TelNumber");
        editor.putString("HELLIKA", "BirthPlace");
        editor.commit();
        Intent intent = new Intent(PersonalDetailsActivity.this, HealthMattersActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkViews()
    {
        etLastName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Validation.hasText(etLastName);
            }
        });

        etFirstName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Validation.hasText(etFirstName);
            }
        });

        email.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Validation.isEmailAddress(email, true);
            }
        });

        etTelNum.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Validation.isPhoneNumber(etTelNum, false);
            }
        });

     /*   btnContinue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkValidation())
                {
                    Intent intent = new Intent(PersonalDetailsActivity.this, HealthMattersActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(), "Kérem ellenőrizze, hogy minden mezőt kitöltött-e!", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if (!Validation.hasText(etFirstName)) ret = false;
        if (!Validation.hasText(etLastName)) ret = false;
        if (!Validation.isEmailAddress(email, true)) ret = false;
        if (!Validation.isPhoneNumber(etTelNum, false)) ret = false;

        return ret;
    }

    private void setDividerColor(DatePicker picker, int color)
    {

        java.lang.reflect.Field[] pickerFields = DatePicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields)
        {
            if (pf.getName().equals("mSelectionDivider"))
            {
                pf.setAccessible(true);
                try
                {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                }
                catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

}
