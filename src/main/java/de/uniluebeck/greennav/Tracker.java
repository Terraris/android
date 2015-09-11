package de.uniluebeck.greennav;


import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class Tracker extends Service implements LocationListener
{

    MainActivity t;
    private final Context mContext;
    boolean GPSEnabled = false;

    boolean NetworkEnabled = false;

    boolean canGetLocation = true;

    Location location;
    double latitude = 0;
    double longitude = 0;

    List<JSInterface> jsInterfaceList = new LinkedList<JSInterface>();

    public void registerJSInterface(JSInterface jsInterface)
    {
        jsInterfaceList.add(jsInterface);
    }

    /* The minimum distance to change Updates in meters */
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    /* The minimum time between updates in milliseconds */
    private static final long MIN_TIME_BW_UPDATES = 100; // 0.1 second

    protected LocationManager locationManager;

    public Tracker(Context context)
    {

        this.mContext = context;
        try
        {
            Log.d("GreenNav Android", "Tracker:Tracker(), mContext = " + String.valueOf(mContext));
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            Log.d("GreenNav Android", "Tracker:Tracker(), locationManager = " + (locationManager != null));
            /* GPS status */
            GPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.d("GreenNav Android", "Tracker:Tracker(), GPSEnabled = " + GPSEnabled);

            /* network status */
            //NetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d("GreenNav Android", "Tracker:Tracker(), NetworkEnabled = " + NetworkEnabled);
            if (!GPSEnabled) // && !NetworkEnabled
            {

            /* no network enabled */
            } else
            {
                this.canGetLocation = true;
                /* First get location from Network Provider */
                //    if (NetworkEnabled)
                //   {
                //       locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                //        Log.d("Network", "Network");
                //        if (locationManager != null)
                //        {
                //            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                //            if (location != null)
                //            {
                //                latitude = location.getLatitude();
                //                longitude = location.getLongitude();
                //            }
                //        }
                //    }
                /* if GPS Enabled get lat/long using GPS Services */
                //if (GPSEnabled)
                //{
                Log.d("GreenNav Android", "Tracker:Tracker(), GPSEnabled!");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("GreenNav Android", "Tracker:Tracker(), LocationManager = " + locationManager);
                if (locationManager != null)
                {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null)
                    {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
                //}
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void stopUsingGPS()
    {


        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(Tracker.this);


    }

    @Override
    public int checkSelfPermission(String accessFineLocation)
    {
        return PackageManager.PERMISSION_GRANTED;
    }


    public double getLatitude()
    {
        if (location != null)
        {
            latitude = location.getLatitude();
        }
        return latitude;
    }


    public double getLongitude()
    {
        if (location != null)
        {
            longitude = location.getLongitude();
        }
        return longitude;
    }


    public boolean canGetLocation()
    {
        return this.canGetLocation;
    }


    public void showSettingsAlert()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);

        dialog.setTitle("GPS is set");

        dialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.d("GreenNav Android", "Tracker:onLocationChanged()");
        Log.d("GreenNav Android", "Tracker:onLocationChanged(), Coordinates = " + location.getLatitude() + " - " + location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        for (JSInterface jsInterface : jsInterfaceList)
            jsInterface.fireGPSChangeEvent(latitude, longitude);
        //myInterface.registerGPSCallback(String.valueOf(coordinates));
    }

    @Override
    public void onProviderDisabled(String provider)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }


    /**
     * GPS tracking
     */
    public double[] track()
    {
        double[] coordinates = new double[2];
        Log.d("GreenNav Android", "Tracker:Track(), canGetLocation = " + canGetLocation);
        if (canGetLocation())
        {
            Log.d("GreenNav Android", "Tracker:Track(), Latitude = " + String.valueOf(getLatitude()));
            Log.d("GreenNav Android", "Tracker:Track(), Longitude = " + String.valueOf(getLongitude()));
            coordinates[0] = getLatitude();
            coordinates[1] = getLongitude();
        }
        return coordinates;
    }

}