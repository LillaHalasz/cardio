package hu.user.kardioapplication;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by User on 2016.04.20..
 */
public class WalkingScreenActivity extends FragmentActivity implements BluetoothLeListener
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

    public static String USER_DATA_SERVICE = "2296dc20-d192-11e4-9b39-0002a5d5c51b";
    public static String DEVICE_CONTROL_SERVICE = "e4b83ca0-d191-11e4-90a5-0002a5d5c51b";
    public static String FIRST_NAME_CHARAC = "00002A8A-0000-1000-8000-00805f9b34fb";
    public static String LAST_NAME_CHARAC = "00002A90-0000-1000-8000-00805f9b34fb";
    public static String PLACE_OF_BIRTH_CHARAC = "27dd4340-d192-11e4-b727-0002a5d5c51b";
    public static String EMAIL_CHARAC = "00002A87-0000-1000-8000-00805f9b34fb";
    public static String PHONE_NUMBER_CHARAC = "2cff0b60-d192-11e4-9640-0002a5d5c51b";
    public static String DATE_OF_BIRTH_CHARAC = "00002A85-0000-1000-8000-00805f9b34fb";
    public static String GENDER_CHARAC = "00002A8C-0000-1000-8000-00805f9b34fb";
    public static String AGE_CHARAC = "00002A80-0000-1000-8000-00805f9b34fb";
    public static String WEIGHT_CHARAC = "00002A98-0000-1000-8000-00805f9b34fb";
    public static String HEIGHT_CHARAC = "00002A8E-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_CONTROL_CHARAC = "00f45340-d192-11e4-a94a-0002a5d5c51b";

    String firstName;
    String lastName;
    String phoneNumber;
    String eMail;
    String placeOfBirth;
    int gender;
    int birthYear;
    int birthMonth;
    int birthDay;
    int age;
    int weight;
    int height;

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
            mBluetoothLeService.setBluetoothLeListener(WalkingScreenActivity.this);
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

        }


        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                invalidateOptionsMenu();
                if (mBluetoothLeService != null)
                {
                    //  mBluetoothLeService.writeCustomCharacteristic(0x01);
                    ble_InitState = INIT_STATE_FIRST_NAME;
                    ble_RetryNum = 3;

                    try
                    {
                        mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, FIRST_NAME_CHARAC, firstName);
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                invalidateOptionsMenu();

            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                // Show all the supported services and characteristics on the user interface./
                //               displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {

            }
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

        SharedPreferences sharedPref = getSharedPreferences("Personal", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        firstName = sharedPref.getString("FirstName", "Ismeretlen");
        lastName = sharedPref.getString("LastName", "Ismeretlen");
        phoneNumber = sharedPref.getString("TelNumber", "Ismeretlen");
        placeOfBirth = sharedPref.getString("BirthPlace", "Ismeretlen");
        eMail = sharedPref.getString("Email", "Ismeretlen");
        gender = sharedPref.getInt("Gender", 0);
        birthYear = sharedPref.getInt("BirthYear", 0);
        birthMonth = sharedPref.getInt("BirthMonth", 0);
        birthDay = sharedPref.getInt("BirthDay", 0);
        byte[] dateOfBirth = new byte[4];

        age = sharedPref.getInt("Age", 0);
        weight = sharedPref.getInt("Weight", 0);
        height = sharedPref.getInt("Height", 0);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            }
        });

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null)
        {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mBluetoothLeService.unpairDevice(mDeviceAddress);
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    private static IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    private static final int INIT_STATE_NONE = 0;
    private static final int INIT_STATE_FIRST_NAME = 1;
    private static final int INIT_STATE_LAST_NAME = 2;
    private static final int INIT_STATE_EMAIL = 3;
    private static final int INIT_STATE_PHONE_NUMBER = 4;
    private static final int INIT_STATE_PLACE_OF_BIRTH = 5;
    private static final int INIT_STATE_AGE = 6;
    private static final int INIT_STATE_GENDER = 7;
    private static final int INIT_STATE_DATE_OF_BIRTH = 8;
    private static final int INIT_STATE_WEIGHT = 9;
    private static final int INIT_STATE_HEIGHT = 10;
    private static final int INIT_STATE_SUBSCRIBE_RESPONSE= 11;
    private static final int INIT_STATE_START_MEASUREMENT = 12;
    private static final int INIT_STATE_SUSCRIBE_HEART_RATE = 13;


    private int ble_InitState = INIT_STATE_NONE;
    private static final int BLE_RETRY_NUMBER = 3;
    private int ble_RetryNum;
    private byte[] start_cmd = {0x01};


    @Override
    public void onCharacteristicWriteFinish(boolean result) throws UnsupportedEncodingException
    {
        Log.d("Listener", "Characteristic is written " + (result ? "True" : "False"));

        switch (this.ble_InitState)
        {
            case INIT_STATE_FIRST_NAME:
                if (!result)
                {
                    if (--this.ble_RetryNum != 0)
                        mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, FIRST_NAME_CHARAC, firstName);
                    //else
                    // Cannot write BLE
                }
                else
                {
                    this.ble_RetryNum = BLE_RETRY_NUMBER;
                    this.ble_InitState = INIT_STATE_LAST_NAME;
                    mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, LAST_NAME_CHARAC, lastName);
                }

                break;

            case INIT_STATE_LAST_NAME:
                /*if ( !result )
                {
                    if (--this.ble_RetryNum)
                        mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, FIRST_NAME_CHARAC, firstName);
                    //else
                    // Cannot write BLE
                }*/
                this.ble_RetryNum = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_EMAIL;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, EMAIL_CHARAC, eMail);

                break;

            case INIT_STATE_EMAIL:

                this.ble_RetryNum = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_PHONE_NUMBER;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, PHONE_NUMBER_CHARAC, phoneNumber);
                break;

            case INIT_STATE_PHONE_NUMBER:
                this.ble_RetryNum = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_PLACE_OF_BIRTH;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, PLACE_OF_BIRTH_CHARAC, placeOfBirth);
                break;

            case INIT_STATE_PLACE_OF_BIRTH:

                this.ble_RetryNum = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_AGE;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE,AGE_CHARAC, age);
                break;

            case INIT_STATE_AGE:
                this.ble_RetryNum = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_GENDER;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, GENDER_CHARAC, gender);
                break;

            case INIT_STATE_GENDER:
                this.ble_RetryNum = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_DATE_OF_BIRTH;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, PHONE_NUMBER_CHARAC, phoneNumber);
                break;

            case INIT_STATE_DATE_OF_BIRTH:
                this.ble_RetryNum = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_WEIGHT;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, WEIGHT_CHARAC, weight);
                break;

            case INIT_STATE_WEIGHT:
                this.ble_RetryNum = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_HEIGHT;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, HEIGHT_CHARAC, height);
                break;

            case INIT_STATE_HEIGHT:
                this.ble_RetryNum = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_SUBSCRIBE_RESPONSE;
                mBluetoothLeService.setCharacteristicNotification(DEVICE_CONTROL_SERVICE, DEVICE_CONTROL_CHARAC, true);
                mBluetoothLeService.writeCustomCharacteristic(DEVICE_CONTROL_SERVICE, DEVICE_CONTROL_CHARAC, 0x01);
                break;

            case INIT_STATE_SUBSCRIBE_RESPONSE:
                this.ble_RetryNum = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_START_MEASUREMENT;
                mBluetoothLeService.writeCustomCharacteristic(DEVICE_CONTROL_SERVICE, DEVICE_CONTROL_CHARAC, 0x01);

                break;


            default:
                Log.i("DeviceControlActivity", "Invalid BLE init state");
                break;
        }
    }


}
