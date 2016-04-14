package hu.user.kardioapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by User on 2016.03.13..
 */
public class MapActivity extends FragmentActivity
{
    SupportMapFragment mapFragment;
    GoogleMap googleMap;
    ArrayList<LatLng> myRoutePoints;
    ArrayList<LatLng> plannedRoutePoints;

    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MainActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    protected Location currentLocation;
    private LocationRequest mLocationRequest;
    double currentLatitude;
    double currentLongitude;

    int heartRate;

    boolean isDrawRoute;

    Polyline myRoute;

    TextView tvHeartRate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        tvHeartRate = (TextView) findViewById(R.id.tv_hr);

        String path = Environment.getExternalStorageDirectory().toString() + "/qsch.gpx";
        File gpxFile = new File(path);
        List<Location> gpxList = decodeGPX(gpxFile);

        plannedRoutePoints = new ArrayList<>();
        for (int i = 0; i < gpxList.size(); i++)
        {
            LatLng latLng = new LatLng(gpxList.get(i).getLatitude(), gpxList.get(i).getLongitude());
            plannedRoutePoints.add(latLng);

        }

        setUpMapIfNeeded();


        myRoutePoints = new ArrayList<>();
        isDrawRoute = false;
        final ToggleButton startStop = (ToggleButton) findViewById(R.id.btn_start);
        startStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                isDrawRoute = isChecked;

                if (isChecked)
                {
                    Intent intent = new Intent(MapActivity.this, BackgroundLocationService.class);
                    startService(intent);

                    myRoutePoints.clear();
                    redrawMapLine();

                    Intent intentHeartRate = new Intent(MapActivity.this, BackgroundPulseService.class);
                    startService(intentHeartRate);


                }
                else
                {
                    Intent intent = new Intent(MapActivity.this, BackgroundLocationService.class);
                    stopService(intent);

                    Intent intentHeartRate = new Intent(MapActivity.this, BackgroundPulseService.class);
                    stopService(intentHeartRate);
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver, new IntentFilter("updateLocation"));
        LocalBroadcastManager.getInstance(this).registerReceiver(heartRateUpdateReceiver, new IntentFilter("getHeartRate"));
    }

    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i("MapActivity", "ReceiveLocation");

            currentLatitude = intent.getDoubleExtra("latitude", 0);
            currentLongitude = intent.getDoubleExtra("longitude", 0);
            final LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            if (isDrawRoute)
            {
                myRoutePoints.add(latLng);
                redrawMapLine();
            }
        }
    };

    private BroadcastReceiver heartRateUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i("MapActivity", "ReceiveHeartRate");
            heartRate = intent.getIntExtra("heartRate", 0);
            Log.i("MapActivity", "" + heartRate);
            tvHeartRate.setText("" + heartRate);

        }
    };

    private void setUpMapIfNeeded()
    {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
        {
            mapFragment.getMapAsync(new OnMapReadyCallback()
            {
                @Override
                public void onMapReady(GoogleMap map)
                {
                    googleMap = map;
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().isZoomControlsEnabled();
                    setUpMap();
                }
            });
        }
    }

    private void setUpMap()
    {
        googleMap.addPolyline(drawPlannedRoute(plannedRoutePoints));

        PolylineOptions polylineOptions = new PolylineOptions()
                .color(Color.BLUE)
                .width(5);
        polylineOptions.addAll(new ArrayList<LatLng>());
        myRoute = googleMap.addPolyline(polylineOptions);
    }

    private void redrawMapLine()
    {
        /*PolylineOptions polylineOptions;
        googleMap.clear();
        setUpMap();
        polylineOptions = new PolylineOptions()
                .color(Color.BLUE)
                .width(5);
        polylineOptions.addAll(myRoutePoints);
        googleMap.addPolyline(polylineOptions);*/

        myRoute.setPoints(myRoutePoints);

    }


    public PolylineOptions drawPlannedRoute(ArrayList<LatLng> arrayList)
    {

        PolylineOptions polylineOptionsPlanned = new PolylineOptions();
        polylineOptionsPlanned.addAll(arrayList);
        polylineOptionsPlanned
                .width(5)
                .color(Color.RED);
        return polylineOptionsPlanned;
    }

    private List<Location> decodeGPX(File file)
    {
        List<Location> list = new ArrayList<>();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            FileInputStream fileInputStream = new FileInputStream(file);
            Document document = documentBuilder.parse(fileInputStream);
            Element elementRoot = document.getDocumentElement();

            NodeList nodelist_trkpt = elementRoot.getElementsByTagName("trkpt");

            for (int i = 0; i < nodelist_trkpt.getLength(); i++)
            {

                Node node = nodelist_trkpt.item(i);
                NamedNodeMap attributes = node.getAttributes();

                String newLatitude = attributes.getNamedItem("lat").getTextContent();
                Double newLatitude_double = Double.parseDouble(newLatitude);

                String newLongitude = attributes.getNamedItem("lon").getTextContent();
                Double newLongitude_double = Double.parseDouble(newLongitude);

                String newLocationName = newLatitude + ":" + newLongitude;
                Location newLocation = new Location(newLocationName);
                newLocation.setLatitude(newLatitude_double);
                newLocation.setLongitude(newLongitude_double);

                list.add(newLocation);

            }
            fileInputStream.close();
        }
        catch (ParserConfigurationException e)
        {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SAXException e)
        {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
// TODO Auto-generated catch block
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public void onBackPressed()
    {

    }
}

