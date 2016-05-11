package hu.user.kardioapplication;

import java.io.UnsupportedEncodingException;

/**
 * Created by User on 2016.05.10..
 */
public interface BluetoothLeListener
{
   // void onCharacteristicWriteCallback(int status);
     void onCharacteristicWriteFinish(boolean result) throws UnsupportedEncodingException;
}
