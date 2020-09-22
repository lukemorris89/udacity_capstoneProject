package com.example.capstoneproject.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstoneproject.R;
import com.example.capstoneproject.data.TestViewModel;
import com.example.capstoneproject.model.Test;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private Context mContext;
    private MapView mapView;
    private GoogleMap googleMap;
    private ClusterManager<Test> mClusterManager;
    private List<List<Test>> mAllTestsGrouped;
    private TestViewModel mTestViewModel;
    private String[] mTestResultOptionsArray = { "Positive", "Negative", "Inconclusive" };
    private List<String> mTestResultOptions;
    private float[] mMarkerColors = { 174.0f, 4.0f, 45.0f };
    private List<CheckBox> mFilterCheckboxes;
    private LocationRequest mLocationRequest;
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    private LatLng mCurrentLocation;

    private long UPDATE_INTERVAL = 60 * 1000;  /* 60 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private int mTotalTests;

    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        if (getActivity() != null) {
            mTestViewModel = new ViewModelProvider(this.getActivity()).get(TestViewModel.class);
        }
        mAllTestsGrouped = new ArrayList<>();
        mTestResultOptions = Arrays.asList(mTestResultOptionsArray);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_location, container, false);
        mFilterCheckboxes = new ArrayList<>();
        mFilterCheckboxes.add(view.findViewById(R.id.filter_checkbox_positive));
        mFilterCheckboxes.add(view.findViewById(R.id.filter_checkbox_negative));
        mFilterCheckboxes.add(view.findViewById(R.id.filter_checkbox_inconclusive));
        for (CheckBox checkBox: mFilterCheckboxes) {
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkMapFilters();
                }
            });
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        startLocationUpdates();

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        mClusterManager = new ClusterManager<>(mContext, googleMap);

        for (int i = 0; i < mTestResultOptions.size(); i++) {
            mAllTestsGrouped.add(new ArrayList<>());
            int finalI = i;
            mTestViewModel.getTestGroup(mTestResultOptions.get(i)).observe(getViewLifecycleOwner(), tests -> {
                mAllTestsGrouped.get(finalI).addAll(tests);
                checkMapFilters();
            });
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkMapFilters() {
        int countChecked = 0;
        for (CheckBox checkBox: mFilterCheckboxes) {
            if (checkBox.isChecked()) {
                countChecked++;
            }
        }
        mClusterManager.clearItems();
        if (countChecked == mFilterCheckboxes.size()) {
            for (int i = 0; i < mAllTestsGrouped.size(); i++) {
                for (Test test : mAllTestsGrouped.get(i)) {
                    mClusterManager.addItem(test);
                }
                mClusterManager.setRenderer(new CustomClusterRenderer(mContext, googleMap, mClusterManager, i));
            }
        }
        else if (countChecked > 0) {
            for (int i = 0; i < countChecked; i++) {
                for (int j = 0; j < mTestResultOptions.size(); j++) {
                    if (mFilterCheckboxes.get(j).isChecked()) {
                        for (Test test: mAllTestsGrouped.get(j)) {
                            mClusterManager.addItem(test);
                        }
                        mClusterManager.setRenderer(new CustomClusterRenderer(mContext, googleMap, mClusterManager, j));
                    }
                }
            }
        }
        googleMap.setOnMarkerClickListener(mClusterManager);
        googleMap.setOnCameraIdleListener(mClusterManager);
        mClusterManager.cluster();
    }

    private class CustomClusterRenderer extends DefaultClusterRenderer<Test> {
        private int colorIndex;
        private final IconGenerator mClusterIconGenerator;

        public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<Test> clusterManager, int colorIndex) {
            super(context, map, clusterManager);
            this.colorIndex = colorIndex;
            mClusterIconGenerator = new IconGenerator(mContext.getApplicationContext());
        }

        @Override
        protected void onBeforeClusterItemRendered(Test test, MarkerOptions markerOptions) {
            final BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(mMarkerColors[this.colorIndex]);
            markerOptions.icon(markerDescriptor);
        }
    }


    protected void startLocationUpdates() {
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(mContext);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
            LocationServices.getFusedLocationProviderClient(mContext).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            // do work here
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    },
                    Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        mCurrentLatitude = location.getLatitude();
        mCurrentLongitude = location.getLongitude();
        mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 12));
        }
    }

    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(mContext);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });

    }
}
