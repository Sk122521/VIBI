package com.example.pollinguserapp;



import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.pollinguserapp.databinding.ActivityMapBinding;
import com.example.pollinguserapp.model.resultModel;
import com.example.pollinguserapp.utils.LocationUtils;
import com.example.pollinguserapp.utils.ResultUtils;
import com.example.pollinguserapp.utils.getLatLngBounds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import android.widget.SearchView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.Dispatchers;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMapBinding binding;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Context mContext;
    private DatabaseReference ActiveTopicRef,mRef,dref;


    private String categoryNo,gd,no_of_votes,continent,from,topicno;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker searchMarker;
    String[] continents = {"australia","america","europe","africa", "asia"};

    private resultModel resultModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.searchView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply the insets as a margin to the view. This solution sets only the
            // bottom, left, and right dimensions, but you can apply whichever insets are
            // appropriate to your layout. You can also update the view padding if that's
            // more appropriate.
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.leftMargin = insets.left + dpToPx(16);
            mlp.bottomMargin = insets.bottom;
            mlp.rightMargin = insets.right + dpToPx(16);
            mlp.topMargin  = insets.top + dpToPx(16);
            v.setLayoutParams(mlp);

            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            return WindowInsetsCompat.CONSUMED;
        });

        mContext = this;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        categoryNo = getIntent().getStringExtra("categoryNo");
        gd = getIntent().getStringExtra("gd");
        no_of_votes = getIntent().getStringExtra("no_of_votes");
        from = getIntent().getStringExtra("from");
        continent = continents[Integer.parseInt(categoryNo)];
        topicno = getIntent().getStringExtra("key");

        mRef = FirebaseDatabase.getInstance().getReference("categories").child(categoryNo);
        ActiveTopicRef = mRef.child("activetopic");

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query,null,"n");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchLocation(newText,null,"n");
                return false;
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for permission to access location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            zoomToCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Add marker click listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                float currentZoomLevel = googleMap.getCameraPosition().zoom;

                if (currentZoomLevel < 5){
                    showresult();
                }else{
                    getDataPoints(LocationUtils.getZipCode(marker.getPosition().latitude,marker.getPosition().longitude,getApplicationContext()));
                }
                //  showMarkerDialog(marker.getPosition());
                return false;
            }
        });

        if (from.equals("deactive")){
            mRef.child("topics").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //continent = snapshot.child("continent").getValue().toString();
                    zoomToCurrentLocation();
                    for (DataSnapshot snap : snapshot.getChildren()){

                        for (DataSnapshot s : snap.child("voteslocation").getChildren()){
                            String zipcode =  s.getKey();
                        //    Toast.makeText(MapsActivity.this, zipcode, Toast.LENGTH_SHORT).show();
                            showPercentage(zipcode);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            mRef.child("activetopic").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //continent = snapshot.child("continent").getValue().toString();
                    zoomToCurrentLocation();

                    for (DataSnapshot s : snapshot.child("voteslocation").getChildren()){
                        String zipcode =  s.getKey();
                       // Toast.makeText(MapsActivity.this, zipcode, Toast.LENGTH_SHORT).show();
                        showPercentage(zipcode);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }


    private void searchLocation(String locationName,Float percentage, String s) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                // Zoom to searched location
                if (s.equals("s")){
                    View customMarkerView = getLayoutInflater().inflate(R.layout.custom_marker_layout, null);
                    TextView markerText = customMarkerView.findViewById(R.id.markerText);

                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    String PercentValue = decimalFormat.format(percentage);

                    markerText.setText(PercentValue + " %");

                    customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

                    customMarkerView.setDrawingCacheEnabled(true);
                    customMarkerView.buildDrawingCache();
                    Bitmap bitmap = Bitmap.createBitmap(customMarkerView.getDrawingCache());
                    customMarkerView.setDrawingCacheEnabled(false);

                    BitmapDescriptor customMarker = BitmapDescriptorFactory.fromBitmap(bitmap);

                    searchMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title(PercentValue +" %").icon(customMarker));
                }else{
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12));
                    searchMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void showPercentage(String zipcode){

        if (from.equals("deactive")){

            dref =  mRef.child("topics").child(topicno);
            dref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snap) {
                    String zipcodevotes, GdZipcodeWise;

                    if (snap.child("voteslocation").child(zipcode).exists()){
                        zipcodevotes = snap.child("voteslocation").child(zipcode).child("noofvotes").getValue().toString();
                        GdZipcodeWise = snap.child("voteslocation").child(zipcode).child("gdzipcodewise").getValue().toString();
                        if (((Integer.parseInt(zipcodevotes)+1)*100 )< (0.8 * Integer.parseInt(GdZipcodeWise))){
                            GdZipcodeWise  = Integer.toString(Integer.parseInt(GdZipcodeWise)+1000);
                        }
                    }else{
                        zipcodevotes = "0";
                        GdZipcodeWise  = Integer.toString(2000);
                    }
                    ResultUtils resultUtils = new ResultUtils(Integer.parseInt(zipcodevotes),Integer.parseInt(GdZipcodeWise));
                    resultModel = resultUtils.getResult();
                    searchLocation(zipcode,resultModel.getAverage_percent(),"s");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else{
            dref =  mRef.child("activetopic");
            dref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snap) {
                    String zipcodevotes, GdZipcodeWise;

                    if (snap.child("voteslocation").child(zipcode).exists()){
                        zipcodevotes = snap.child("voteslocation").child(zipcode).child("noofvotes").getValue().toString();
                        GdZipcodeWise = snap.child("voteslocation").child(zipcode).child("gdzipcodewise").getValue().toString();
                        if (((Integer.parseInt(zipcodevotes)+1)*100 )< (0.8 * Integer.parseInt(GdZipcodeWise))){
                            GdZipcodeWise  = Integer.toString(Integer.parseInt(GdZipcodeWise)+1000);
                        }
                    }else{
                        zipcodevotes = "0";
                        GdZipcodeWise  = Integer.toString(2000);
                    }
                    ResultUtils resultUtils = new ResultUtils(Integer.parseInt(zipcodevotes),Integer.parseInt(GdZipcodeWise));
                    resultModel = resultUtils.getResult();
                    searchLocation(zipcode,resultModel.getAverage_percent(),"s");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    private void getDataPoints(String zipcode){


        if (from.equals("deactive")){

            dref =  mRef.child("topics").child(topicno);
            dref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snap) {
                    String zipcodevotes, GdZipcodeWise;

                    if (snap.child("voteslocation").child(zipcode).exists()){
                        zipcodevotes = snap.child("voteslocation").child(zipcode).child("noofvotes").getValue().toString();
                        GdZipcodeWise = snap.child("voteslocation").child(zipcode).child("gdzipcodewise").getValue().toString();
                        if (((Integer.parseInt(zipcodevotes)+1)*100 )< (0.8 * Integer.parseInt(GdZipcodeWise))){
                            GdZipcodeWise  = Integer.toString(Integer.parseInt(GdZipcodeWise)+1000);
                        }
                    }else{
                        zipcodevotes = "0";
                        GdZipcodeWise  = Integer.toString(2000);
                    }
                    ResultUtils resultUtils = new ResultUtils(Integer.parseInt(zipcodevotes),Integer.parseInt(GdZipcodeWise));
                    resultModel = resultUtils.getResult();
                    showMarkerDialog(resultModel,zipcode);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            dref =  mRef.child("activetopic");
            dref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snap) {
                    String zipcodevotes, GdZipcodeWise;

                    if (snap.child("voteslocation").child(zipcode).exists()){
                        zipcodevotes = snap.child("voteslocation").child(zipcode).child("noofvotes").getValue().toString();
                        GdZipcodeWise = snap.child("voteslocation").child(zipcode).child("gdzipcodewise").getValue().toString();
                        if (((Integer.parseInt(zipcodevotes)+1)*100 )< (0.8 * Integer.parseInt(GdZipcodeWise))){
                            GdZipcodeWise  = Integer.toString(Integer.parseInt(GdZipcodeWise)+1000);
                        }
                    }else{
                        zipcodevotes = "0";
                        GdZipcodeWise  = Integer.toString(2000);
                    }
                    ResultUtils resultUtils = new ResultUtils(Integer.parseInt(zipcodevotes),Integer.parseInt(GdZipcodeWise));
                    resultModel = resultUtils.getResult();
                    showMarkerDialog(resultModel,zipcode);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    private void showMarkerDialog(final resultModel resultModel,String zipcode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Result of Zip code searched")
                .setMessage("\nMargin of Error(MOE): " + resultModel.getError() + "\nVote Percentage: " + resultModel.getAverage_percent())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    private void showresult(){
        ResultUtils resultUtils = new ResultUtils(Integer.parseInt(no_of_votes),Integer.parseInt(gd));
        resultModel resultModel = resultUtils.getResult();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Result")
                .setMessage("\nMargin of Error(MOE): " + resultModel.getError() + "\nVote Percentage: " + resultModel.getAverage_percent())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    zoomToCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void zoomToCurrentLocation() {

        if (continent.equals("asia")){
            zoom(new getLatLngBounds().AsiaBounds());
        }else if(continent.equals("europe")){
            zoom(new getLatLngBounds().EuropeBounds());
        }else if(continent.equals("africa")){
            zoom(new getLatLngBounds().AfricaBounds());
        }else if(continent.equals("america")){
            zoom(new getLatLngBounds().NorthAmericaBounds());
        }else if(continent.equals("australia")){
            zoom(new getLatLngBounds().AustraliaBounds());
        }
        //LatLngBounds boundsIndia = new LatLngBounds(new LatLng(23.63936, 68.14712), new LatLng(28.20453, 97.34466));

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return ;
//        }else{
//
//            fusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(this, location -> {
//                        if (location != null) {
//                            double latitude = location.getLatitude();
//                            double longitude = location.getLongitude();
//                            String zipcode = getZipCode(latitude, longitude,this);
//
//
//
//                            if (searchMarker != null) {
//                                searchMarker.remove();
//                            }
//
//                            // Add marker to the searched location
//                            searchMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title(zipcode));
//
//
//                            //   Toast.makeText(TopicsActivity.this, "Latitude: " + latitude + "\nLongitude: " + longitude + "\nZipcode: " + zipcode, Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(MapsActivity.this, "Location is null , Please click on device location button", Toast.LENGTH_LONG).show();
//                        }
//                    })
//                    .addOnFailureListener(this, e -> {
//                        Toast.makeText(MapsActivity.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });
//        }
    }

    public void zoom(LatLngBounds bounds){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 0));
       // mMap.setLatLngBoundsForCameraTarget(bounds);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

//        LatLng newDelhi = new LatLng(28.6139, 77.2090); // Coordinates for New Delhi
//        mMap.addMarker(new MarkerOptions().position(newDelhi).title("India"));
    }

    public void setMarker(){
        View customMarkerView = getLayoutInflater().inflate(R.layout.custom_marker_layout, null);
        TextView markerText = customMarkerView.findViewById(R.id.markerText);
        markerText.setText("Custom Text");

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

        customMarkerView.setDrawingCacheEnabled(true);
        customMarkerView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(customMarkerView.getDrawingCache());
        customMarkerView.setDrawingCacheEnabled(false);

        BitmapDescriptor customMarker = BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}

