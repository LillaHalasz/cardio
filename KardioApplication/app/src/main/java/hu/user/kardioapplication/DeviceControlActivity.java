package hu.user.kardioapplication;


import android.app.Activity;
import android.app.assist.AssistContent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by User on 2016.05.02..
 */
public class DeviceControlActivity extends Activity
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
                //              mConnectionState.setText(resourceId);
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

    public void onClickWrite(View v) throws UnsupportedEncodingException
    {
        if (mBluetoothLeService != null)
        {
            mBluetoothLeService.writeCustomCharacteristic(0x01);
        }
    }

    public void onClickRead(View v) throws UnsupportedEncodingException
    {
        if (mBluetoothLeService != null)
        {
           // mBluetoothLeService.readCustomCharacteristic();
            UUID service = UUID.fromString("e4b83ca0-d191-11e4-90a5-0002a5d5c51b");
            UUID charac = UUID.fromString("00f45340-d192-11e4-a94a-0002a5d5c51b");
            mBluetoothLeService.setCharacteristicNotification(service, charac, true);
        }
    }

  
    /* @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {

        if(BluetoothGatt.GATT_SUCCESS == status)
        {
            // characteristic was read successful
        }
        else if(BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION == status ||
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION == status)
        {
        *//*
         * failed to complete the operation because of encryption issues,
         * this means we need to bond with the device
         *//*

        *//*
         * registering Bluetooth BroadcastReceiver to be notified
         * for any bonding messages
         *//*
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(mReceiver, filter);
        }
        else
        {
            // operation failed for some other reason
        }
    }*/

   /* private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                switch(state){
                    case BluetoothDevice.BOND_BONDING:
                        // Bonding...
                        break;

                    case BluetoothDevice.BOND_BONDED:
                        // Bonded...
                        mActivity.unregisterReceiver(mReceiver);
                        break;

                    case BluetoothDevice.BOND_NONE:
                        // Not bonded...
                        break;
                }
            }
        }
    };*/



}
