package com.moviesinfo.www.googlem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
//import android.location.LocationListener;
import com.google.android.gms.location.LocationListener;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE=99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void searchclick(View view)
    {
    if(view.getId()==R.id.BSearch)
    {
        EditText ptlocation=(EditText)findViewById(R.id.PTLocation);
        String location=ptlocation.getText().toString();
        List<Address> addressList = null;
        MarkerOptions mo=new MarkerOptions();
        if(! location.equals(""))
        {
            Geocoder geocoder=new Geocoder(this);
            try {
                addressList=geocoder.getFromLocationName(location,4);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(int i=0;i<addressList.size();i++)
            {
              Address myadress=addressList.get(i);
              LatLng latLng=new LatLng(myadress.getLatitude(),myadress.getLongitude());
              mo.position(latLng);
              mo.title("Your Search Result");
              mMap.addMarker(mo);
              mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch ((requestCode)){
           case REQUEST_LOCATION_CODE:
               if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
           {
               if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
               {
                   if(googleApiClient==null)
                   {
                       buildGoogleApiClient();
                   }
                   mMap.setMyLocationEnabled(true);
               }
           }
           else
               {
                   Toast.makeText(this,"Permission denied",Toast.LENGTH_LONG).show();
               }
               return;
       }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
       {
           buildGoogleApiClient();
           mMap.setMyLocationEnabled(true);

       }


        }



    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        if (currentLocationMarker!=null)
        {
            currentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(googleApiClient !=null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
         locationRequest=new LocationRequest();
         locationRequest.setInterval(1000);
         locationRequest.setFastestInterval(1000);
         locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
         if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
             LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
         }
    }
    public boolean checkLocationPermission()
    {
    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
        }
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
        }
        return false;
    }
    else
        return true;
    }




    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    protected synchronized  void buildGoogleApiClient()
    {
        googleApiClient=new GoogleApiClient.Builder(this).addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                addApi(LocationServices.API).build();
        googleApiClient.connect();
    }
}
