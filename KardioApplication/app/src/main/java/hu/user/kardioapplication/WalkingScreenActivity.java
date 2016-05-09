package hu.user.kardioapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
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
    @Bind(R.id.katt)
    Button button;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private String mDeviceName;
    private String mDeviceAddress;
    public BluetoothLeService mBluetoothLeService;
    public String birthPlace;

    private SharedPreferences sharedPref;

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

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walking);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
       /* startService(gattServiceIntent);
        if (!mBluetoothLeService.initialize())
        {
            Log.i(TAG, "Unable to initialize Bluetooth");
            finish();
        }
        mBluetoothLeService.connect(mDeviceAddress);
*/
        startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        sharedPref = getApplicationContext().getSharedPreferences("Personal", Context.MODE_PRIVATE);
        birthPlace = sharedPref.getString("BirthPlace", "No Data");
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();


        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

       /* try
        {*/
                if (mBluetoothLeService != null)
                {
                    // mBluetoothLeService.writeCustomCharacteristic(birthPlace);
                    mBluetoothLeService.writeCustomCharacteristic(0x01);
                }
   /*     }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
*/
       /* try
        {
            if (mBluetoothLeService != null)
            {
                mBluetoothLeService.readCustomCharacteristic();
            }
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }*/
            }
        });
    }

}
