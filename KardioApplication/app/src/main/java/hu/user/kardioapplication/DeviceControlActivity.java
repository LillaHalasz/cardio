package hu.user.kardioapplication;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by User on 2016.05.02..
 */
public class DeviceControlActivity extends Activity implements BluetoothLeListener
{
    private final static String TAG = DeviceControlActivity.class.getSimpleName();


    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> listOfServices;

    public static String USER_DATA_SERVICE = "2296dc20-d192-11e4-9b39-0002a5d5c51b";
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
    public static String DEVICE_CONTROL_SERVICE = "e4b83ca0-d191-11e4-90a5-0002a5d5c51b";
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
            mBluetoothLeService.setBluetoothLeListener(DeviceControlActivity.this);
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
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                // Show all the supported services and characteristics on the user interface./
                //               displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener()
    {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                    int childPosition, long id)
        {
            if (mGattCharacteristics != null)
            {
                final BluetoothGattCharacteristic characteristic =
                        mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0)
                {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null)
                    {
                        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.readCharacteristic(characteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
                {
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLeService.setCharacteristicNotification(
                            characteristic, true);
                }
                return true;
            }
            return false;
        }
    };

    private void clearUI()
    {
        //    mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText("No data");
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        // Sets up UI references.
  /*      ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);*/
        mDataField = (TextView) findViewById(R.id.data_value);

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
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


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


    private void updateConnectionState(final int resourceId)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data)
    {
        if (data != null)
        {
            mDataField.setText(data);
        }
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


    private int ble_InitState = INIT_STATE_NONE;
    private static final int BLE_RETRY_NUMBER = 3;
    private int ble_RetryNum;


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

                this.ble_InitState = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_PHONE_NUMBER;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, PHONE_NUMBER_CHARAC, phoneNumber);
                break;

            case INIT_STATE_PHONE_NUMBER:
                this.ble_InitState = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_PLACE_OF_BIRTH;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, PLACE_OF_BIRTH_CHARAC, placeOfBirth);
                break;

            case INIT_STATE_PLACE_OF_BIRTH:

                this.ble_InitState = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_AGE;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE,AGE_CHARAC, age);
                break;

            case INIT_STATE_AGE:
                this.ble_InitState = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_GENDER;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, GENDER_CHARAC, gender);
                break;

            case INIT_STATE_GENDER:
                this.ble_InitState = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_DATE_OF_BIRTH;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, PHONE_NUMBER_CHARAC, phoneNumber);
                break;

            case INIT_STATE_DATE_OF_BIRTH:
                this.ble_InitState = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_WEIGHT;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, WEIGHT_CHARAC, weight);
                break;

            case INIT_STATE_WEIGHT:
                this.ble_InitState = BLE_RETRY_NUMBER;
                this.ble_InitState = INIT_STATE_HEIGHT;
                mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, HEIGHT_CHARAC, height);
                break;

            case INIT_STATE_HEIGHT:
                this.ble_InitState = BLE_RETRY_NUMBER;
               // mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, PHONE_NUMBER_CHARAC, BigInteger.valueOf(gender).toByteArray());
                break;


            default:
                Log.i("DeviceControlActivity", "Invalid BLE init state");
                break;
        }
    }
 /*   @Override
    public void onCharacteristicWriteCallback(int status)
    {
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            Log.d("Listener", "success");
        }
    }*/


    public void onClickWrite(View v) throws UnsupportedEncodingException
    {
        if (mBluetoothLeService != null)
        {
            //  mBluetoothLeService.writeCustomCharacteristic(0x01);
            ble_InitState = INIT_STATE_FIRST_NAME;
            ble_RetryNum = 3;

            mBluetoothLeService.writeCustomCharacteristic(USER_DATA_SERVICE, FIRST_NAME_CHARAC, firstName);
        }
    }

    public void onClickRead(View v) throws UnsupportedEncodingException
    {
        if (mBluetoothLeService != null)
        {
            // mBluetoothLeService.readCustomCharacteristic();
            UUID service = UUID.fromString("e4b83ca0-d191-11e4-90a5-0002a5d5c51b");
            UUID charac = UUID.fromString("00f45340-d192-11e4-a94a-0002a5d5c51b");
            mBluetoothLeService.setCharacteristicNotification(DEVICE_CONTROL_SERVICE, DEVICE_CONTROL_CHARAC, true);


        }
    }


}
