package com.example.capstoneproject.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    private Context mContext;

    public LocationHelper(Context context) {
        mContext = context;
    }

    public String getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        String testLocation = null;

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);
            if (addresses.size() > 0) {
                int maxIndex = address.getMaxAddressLineIndex();
                for (int i = 0; i < maxIndex - 1; i++) {
                    builder.append(address.getAddressLine(i));
                    builder.append("\n");
                }
                builder.append(address.getAddressLine(maxIndex));
                testLocation = builder.toString();

            } else {
                testLocation = "N/A";
            }
            return testLocation;
        }
        catch (IOException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
}
