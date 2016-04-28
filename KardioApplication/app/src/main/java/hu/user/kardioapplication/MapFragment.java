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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by User on 2016.04.20..
 */
public class MapFragment extends Fragment
{
    SupportMapFragment mapFragment;
    GoogleMap googleMap;

    ArrayList<LatLng> myRoutePoints;
    ArrayList<LatLng> plannedRoutePoints;

    boolean isDrawRoute;
    private Polyline myRoute;

    public MapFragment()
    {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.map, container, false);

        String gpxFile = "ksch.gpx";
        List<Location> gpxList = decodeGPX(gpxFile);


        plannedRoutePoints = new ArrayList<>();
        for (int i = 0; i < gpxList.size(); i++)
        {
            LatLng latLng = new LatLng(gpxList.get(i).getLatitude(), gpxList.get(i).getLongitude());
            plannedRoutePoints.add(latLng);

        }

        setUpMap();

        myRoutePoints = new ArrayList<>();
        isDrawRoute = false;
        final ToggleButton startStop = (ToggleButton) view.findViewById(R.id.btn_start);
        startStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                isDrawRoute = isChecked;

                if (isChecked)
                {
                    getActivity().startService(new Intent(getActivity(), BackgroundLocationService.class));
                    myRoutePoints.clear();
                    redrawMapLine();
                }
                else
                {
                    getActivity().stopService(new Intent(getActivity(), BackgroundLocationService.class));
                }
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(locationUpdateReceiver, new IntentFilter("updateLocation"));
        return view;

    }

    private void setUpMap()
    {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
        {
            Log.i("MapFragment", "first if");
            mapFragment.getMapAsync(new OnMapReadyCallback()
            {
                @Override
                public void onMapReady(GoogleMap map)
                {
                    googleMap = map;
                    if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
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

    private List<Location> decodeGPX(String file)
    {
        List<Location> list = new ArrayList<>();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream fileInputStream = getActivity().getAssets().open(file);
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

    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i("MapFragment", "ReceiveLocation");

            double currentLatitude = intent.getDoubleExtra("latitude", 0);
            double currentLongitude = intent.getDoubleExtra("longitude", 0);
            final LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            if (isDrawRoute)
            {
                myRoutePoints.add(latLng);
                redrawMapLine();
            }
        }
    };

}
