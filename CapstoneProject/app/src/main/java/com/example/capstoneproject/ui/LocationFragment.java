package com.example.capstoneproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstoneproject.R;
import com.example.capstoneproject.data.TestViewModel;
import com.example.capstoneproject.model.Test;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private ClusterManager<Test> mClusterManager;
    private List<Test> mAllTests;

    private TestViewModel mTestViewModel;
    private String[] mTestResultOptions = { "Positive", "Negative", "Inconclusive" };

    private int mTotalTests;

    public LocationFragment() {
        // Required empty public constructor
    }

    public static LocationFragment newInstance(String param1, String param2) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            mTestViewModel = new ViewModelProvider(this.getActivity()).get(TestViewModel.class);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false);
        // Retrieve the content view that renders the map.

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
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
        mAllTests = new ArrayList<>();

        for (int i = 0; i < mTestResultOptions.length; i++) {
            mTestViewModel.getTestGroup(mTestResultOptions[i]).observe(getViewLifecycleOwner(), tests -> {
                mAllTests.addAll(tests);
//                for (int j = 0; j < tests.size(); j++) {
//                    double testLatitude = tests.get(j).getTestLatitude();
//                    double testLongitude = tests.get(j).getTestLongitude();
//                    LatLng testLocation = new LatLng(testLatitude, testLongitude);
//                    googleMap.addMarker(new MarkerOptions().position(testLocation));
//                }
                setUpClusterer();
            });
        }
    }

    private void setUpClusterer() {
        // Position the map.
        if (mAllTests.size() > 0) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mAllTests.get(0).getPosition(), 10));
        }
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<Test>(this.getContext(), googleMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        googleMap.setOnCameraIdleListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);

        // Add cluster items (markers) to the cluster manager.
        addItems();
    }

    private void addItems() {
        for (Test test: mAllTests) {
            mClusterManager.addItem(test);
        }
    }
}
