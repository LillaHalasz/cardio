package hu.user.kardioapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by User on 2016.05.11..
 */
public class WalkingActivity extends FragmentActivity
{
    @Bind(R.id.btnFinish)
    Button btnFinish;

    SupportMapFragment mapFragment;
    GoogleMap googleMap;

    ArrayList<LatLng> myRoutePoints;
    ArrayList<LatLng> plannedRoutePoints;

    SharedPreferences sharedPreferences;

    boolean isDrawRoute;
    private Polyline myRoute;
    private float distance;

    private boolean firstRun = true;


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walk);
        ButterKnife.bind(this);
        JodaTimeAndroid.init(this);

        sharedPreferences = getSharedPreferences("Details", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        String gpxFile = "ksch.gpx";
        // List<Location> gpxList = decodeGPX(gpxFile);
        plannedRoutePoints = (ArrayList<LatLng>) decodeGPX(gpxFile);

        setUpMap();


        myRoutePoints = new ArrayList<>();
        isDrawRoute = false;
        final ToggleButton startStop = (ToggleButton) findViewById(R.id.btn_start);
        startStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                startService(new Intent(getApplicationContext(), BackgroundLocationService.class));
                isDrawRoute = isChecked;

                if (isChecked)
                {
                    myRoutePoints.clear();
                    redrawMapLine();

                    if (firstRun)
                    {
                        DateTime startTime = DateTime.now();
                        long startMillis = startTime.getMillis();
                        editor.putLong("startTime", startMillis);
                        editor.commit();
                        Log.i("StartTime", "time:" + startTime + "millis:" + startMillis);

                        firstRun = false;
                    }
                }
                else
                {
                    stopService(new Intent(getApplicationContext(), BackgroundLocationService.class));
                }
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i("WalkingScreen", "finishClick");
                int distanceInt = (int) (distance * 100);
                editor.putInt("distance", distanceInt);
                DateTime endTime = DateTime.now();
                long endTimeMillis = endTime.getMillis();
                editor.putLong("endTime", endTimeMillis);
                editor.commit();
                stopService(new Intent(getApplicationContext(), BackgroundLocationService.class));
                stopService(new Intent(getApplicationContext(), BackgroundPulseService.class));
                stopService(new Intent(getApplicationContext(), BluetoothLeService.class));
                startActivity(new Intent(getApplicationContext(), SummaryScreenActivity.class));
                finish();
            }
        });

        final IntentFilter locationIntent = new IntentFilter("updateLocation");
        registerReceiver(locationUpdateReceiver, locationIntent);
        //  registerReceiver(locationUpdateReceiver, new IntentFilter("updateLocation"));
    }

    private void setUpMap()
    {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
        {
            Log.i("MapFragment", "first if");
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
                    setPolylineOnMap();
                }
            });
        }
    }

    private void setPolylineOnMap()
    {
        googleMap.addPolyline(drawPlannedRoute(plannedRoutePoints));

        PolylineOptions polylineOptions = new PolylineOptions()
                .color(Color.rgb(255, 193, 7))
                .width(5);
        polylineOptions.addAll(new ArrayList<LatLng>());
        myRoute = googleMap.addPolyline(polylineOptions);
    }

    public PolylineOptions drawPlannedRoute(ArrayList<LatLng> arrayList)
    {

        PolylineOptions polylineOptionsPlanned = new PolylineOptions();
        polylineOptionsPlanned.addAll(arrayList);
        polylineOptionsPlanned
                .width(5)
                .color(Color.rgb(0, 121, 107));
        return polylineOptionsPlanned;
    }

    private List<LatLng> decodeGPX(String file)
    {
        List<LatLng> list = new ArrayList<>();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream fileInputStream = getAssets().open(file);
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


                LatLng latLng = new LatLng(newLatitude_double, newLongitude_double);

              /*  String newLocationName = newLatitude + ":" + newLongitude;
                Location newLocation = new Location(newLocationName);
                newLocation.setLatitude(newLatitude_double);
                newLocation.setLongitude(newLongitude_double);

                list.add(newLocation);*/
                list.add(latLng);

            }
            fileInputStream.close();
        }
        catch (ParserConfigurationException | SAXException | IOException e)
        {
            e.printStackTrace();
        }

        return list;
    }

    private void redrawMapLine()
    {
        myRoute.setPoints(myRoutePoints);

    }


    double previousLatitude = 0;
    double previousLongitude = 0;
    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i("MapFragment", "ReceiveLocation");

            double currentLatitude = intent.getDoubleExtra("latitude", 0);
            double currentLongitude = intent.getDoubleExtra("longitude", 0);
            final LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            float[] result = new float[1];
            if (isDrawRoute)
            {
                myRoutePoints.add(latLng);
                if (myRoutePoints.size() >= 2)
                {
                    Location.distanceBetween(Math.toRadians(previousLatitude), Math.toRadians(previousLongitude), Math.toRadians(currentLatitude), Math.toRadians(currentLongitude), result);
                    Log.i("result[0]", "" + result[0]);
                    distance += result[0];
                    previousLatitude = currentLatitude;
                    previousLongitude = currentLongitude;
                    Log.i("distance", "" + distance);
                }
                else
                {
                    previousLatitude = currentLatitude;
                    previousLongitude = currentLongitude;
                }
                redrawMapLine();
            }
        }
    };
}
