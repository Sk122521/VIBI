package com.example.pollinguserapp.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class getLatLngBounds{
    public LatLngBounds AustraliaBounds(){
        return  new LatLngBounds( new LatLng(-43.7405, 113.6594),  // South west corner (latitude, longitude)
                new LatLng(-10.0516, 153.5695));
    }
    public LatLngBounds NorthAmericaBounds(){
        return new LatLngBounds(
                new LatLng(24.396308, -125.0), // Southwest corner (latitude, longitude)
                new LatLng(49.384358, -66.93457)
        );
    }
    public LatLngBounds AfricaBounds(){
        return new LatLngBounds(new LatLng(-33.751748,22.635609),new LatLng(23.920992,44.031730));
    }
    public LatLngBounds EuropeBounds(){
        return new LatLngBounds( new LatLng(35.0, -10.0), // South west corner (approximately)
                new LatLng(60.0, 40.0)  );
    }
//    public LatLngBounds AsiaBounds(){
//        return new LatLngBounds(new LatLng(23.63936, 68.14712), new LatLng(28.20453, 97.34466));
//    }
    public LatLngBounds AsiaBounds(){
        return new LatLngBounds( new LatLng(6.75, 68.0), // South west corner
                new LatLng(35.5, 97.25) );
    }
}
