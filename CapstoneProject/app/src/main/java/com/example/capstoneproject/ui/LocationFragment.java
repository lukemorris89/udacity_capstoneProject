package com.example.capstoneproject.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstoneproject.R;
import com.example.capstoneproject.data.TestViewModel;
import com.example.capstoneproject.model.Test;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationFragment extends Fragment implements OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<Test>,
        ClusterManager.OnClusterInfoWindowClickListener<Test>,
        ClusterManager.OnClusterItemClickListener<Test>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Test> {

    private Context mContext;
    private MapView mapView;
    private GoogleMap googleMap;
    private ClusterManager<Test> mClusterManager;
    private Cluster<Test> mClickedCluster;
    private Test mClickedClusterItem;
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

    private float mMapZoomLevel = 12;
    private boolean mMapZoomLevelChanged = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


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
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                mMapZoomLevelChanged = true;
            }
        });
        mClusterManager = new ClusterManager<>(mContext, googleMap);
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mClusterManager.cluster();
            }
        });

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
        googleMap.setOnInfoWindowClickListener(mClusterManager);
        googleMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mClusterManager.getClusterMarkerCollection().setInfoWindowAdapter(
                new MyCustomAdapterForClusters());
        mClusterManager.getMarkerCollection().setInfoWindowAdapter(
                new MyCustomAdapterForItems());
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
        mClusterManager
                .setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Test>() {
                    @Override
                    public boolean onClusterClick(Cluster<Test> cluster) {
                        mClickedCluster = cluster;
                        return false;
                    }
                });
        mClusterManager
                .setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Test>() {
                    @Override
                    public boolean onClusterItemClick(Test test) {
                        mClickedClusterItem = test;
                        return false;
                    }
                });
        if (countChecked > 0) {
            for (int i = 0; i < countChecked; i++) {
                for (int j = 0; j < mTestResultOptions.size(); j++) {
                    if (mFilterCheckboxes.get(j).isChecked()) {
                        for (Test test: mAllTestsGrouped.get(j)) {
                            mClusterManager.addItem(test);
                        }
                    }
                    mClusterManager.setRenderer(new CustomClusterRenderer(mContext, googleMap, mClusterManager));

                }
            }
        }
        mClusterManager.cluster();
    }

    protected void startLocationUpdates() {
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
            if (mMapZoomLevelChanged) {
                mMapZoomLevel = googleMap.getCameraPosition().zoom;
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, mMapZoomLevel));
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Test> cluster) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Test> cluster) {

    }

    @Override
    public boolean onClusterItemClick(Test item) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Test test) {
                Intent intent = new Intent(mContext, RecordTestActivity.class);
                intent.putExtra("test_id", mClickedClusterItem.getTestID());
                mContext.startActivity(intent);
    }

    private class CustomClusterRenderer extends DefaultClusterRenderer<Test> {

        public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<Test> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(Test test, MarkerOptions markerOptions) {
            for (int i = 0; i < mTestResultOptions.size(); i++) {
                if (test.getTestResult().equals(mTestResultOptions.get(i))) {
                    final BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(mMarkerColors[i]);
                    markerOptions.icon(markerDescriptor);
                }
            }
        }
    }

    public class MyCustomAdapterForClusters implements GoogleMap.InfoWindowAdapter {

        private final View mClusterWindowView;

        MyCustomAdapterForClusters() {
            mClusterWindowView = getLayoutInflater().inflate(
                    R.layout.cluster_info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            TextView positiveCount = ((TextView) mClusterWindowView
                    .findViewById(R.id.cluster_count_positive));
            TextView negativeCount = ((TextView) mClusterWindowView
                    .findViewById(R.id.cluster_count_negative));
            TextView inconclusiveCount = ((TextView) mClusterWindowView
                    .findViewById(R.id.cluster_count_inconclusive));

            if (mClickedCluster != null) {
                int positiveTestsCount = 0;
                int negativeTestsCount = 0;
                int inconclusiveTestsCount = 0;

                for (Test test: mClickedCluster.getItems()) {
                    if (test.getTestResult().equals("Positive")) {
                        positiveTestsCount++;
                    } else if (test.getTestResult().equals("Negative")) {
                        negativeTestsCount++;
                    } else {
                        inconclusiveTestsCount++;
                    }
                }
                positiveCount.setText(String.valueOf(positiveTestsCount));
                negativeCount.setText(String.valueOf(negativeTestsCount));
                inconclusiveCount.setText(String.valueOf(inconclusiveTestsCount));
            }
            return mClusterWindowView;
        }
    }

    public class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter {

        private final View mClusterItemView;

        MyCustomAdapterForItems() {
            mClusterItemView = getLayoutInflater().inflate(
                    R.layout.cluster_item_info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            TextView testIdTextView = (TextView) mClusterItemView
                    .findViewById(R.id.cluster_item_test_id);
            TextView patientIdTextView = (TextView) mClusterItemView
                    .findViewById(R.id.cluster_item_patient_id);
            TextView testResultTextView = (TextView) mClusterItemView
                    .findViewById(R.id.cluster_item_test_result);
            TextView testDateTextView = (TextView) mClusterItemView
                    .findViewById(R.id.cluster_item_date);
            Button viewTestButton = (Button) mClusterItemView.findViewById(R.id.cluster_item_view_test_button);

            if (mClickedClusterItem != null) {
                testIdTextView.setText(String.valueOf(mClickedClusterItem.getTestID()));
                patientIdTextView.setText(mClickedClusterItem.getPatientID());
                testResultTextView.setText(mClickedClusterItem.getTestResult());
                testDateTextView.setText(mClickedClusterItem.getTestDateFormatted());
                viewTestButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, RecordTestActivity.class);
                        intent.putExtra("test_id", mClickedClusterItem.getTestID());
                        mContext.startActivity(intent);
                    }
                });
            }
            return mClusterItemView;
        }
    }

}
