package hu.user.kardioapplication;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

/**
 * Created by User on 2016.04.12..
 */
public class LocationHandlerIntentService extends IntentService
{
    private String TAG = this.getClass().getSimpleName();
    public LocationHandlerIntentService()
    {
        super("Fused Location");
    }

    public LocationHandlerIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {

        Log.i(TAG, "onHandleIntent");

        Intent broadcastIntent = new Intent("updateLocation");


        if (LocationResult.hasResult(intent))
        {
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            if (location != null)
            {
                Log.i(TAG, "onHandleIntent " + location.getLatitude() + "," + location.getLongitude());

                broadcastIntent.putExtra("latitude", location.getLatitude());
                broadcastIntent.putExtra("longitude", location.getLongitude());
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

            }
            else Log.i("onHandleIntent", "location=null");
        }
    }
}
