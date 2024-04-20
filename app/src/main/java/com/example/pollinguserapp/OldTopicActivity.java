package com.example.pollinguserapp;


import static com.example.pollinguserapp.utils.LocationUtils.checkLocationPermission;
import static com.example.pollinguserapp.utils.LocationUtils.getZipCode;
import static com.example.pollinguserapp.utils.LocationUtils.requestLocationPermission;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import com.example.pollinguserapp.databinding.ActivityOldTopicBinding;
import com.example.pollinguserapp.model.resultModel;
import com.example.pollinguserapp.utils.ResultUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class OldTopicActivity extends AppCompatActivity {
    private ActivityOldTopicBinding binding;
    private String categoryNo,topicNo,gd,no_of_votes;
    private DatabaseReference mRef;
    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOldTopicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoryNo = getIntent().getStringExtra("Category");
        topicNo = getIntent().getStringExtra("topicKey");

        mRef = FirebaseDatabase.getInstance().getReference("categories").child(categoryNo);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setDeActiveTopic();
        binding.voteBtn.setOnClickListener(v -> checkActiveTopicAvailibility("vote"));
        binding.MapBtn.setOnClickListener(v -> checkActiveTopicAvailibility("map"));

    }

    public void setDeActiveTopic() {
        mRef.child("topics").child(topicNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.activeTopicDesc.setText(snapshot.child("description").getValue().toString());
                    binding.activeTopicTitle.setText(snapshot.child("title").getValue().toString());

                    gd = snapshot.child("gd").getValue().toString();
                    no_of_votes = snapshot.child("noofvotes").getValue().toString();

                    binding.percentageText.setText(String.format("Percentage that voted for above topic of out of sample of %s : ", gd));

                    showResults(gd,no_of_votes);
                    //code of image
                    Glide.with(getApplicationContext()).load(snapshot.child("image").getValue().toString()).into(binding.activeTopicImage);
                    // Do something with the reference
                }else{
                    binding.cardView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showResults(String gd, String noOfVotes){
        ResultUtils resultUtils = new ResultUtils(Integer.parseInt(noOfVotes),Integer.parseInt(gd));
        resultModel resultModel = resultUtils.getResult();

        binding.circleProgressBar.setProgress(resultModel.getAverage_percent());
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedValue = decimalFormat.format(resultModel.getError());
        String PercentageFormattedValue = decimalFormat.format(resultModel.getAverage_percent());
        if (Float.toString(resultModel.getError()) == "Infinity"){
            binding.percentageText.setText("A survey conducted with a sample size of "+gd+" revealed that "+PercentageFormattedValue+"% of respondents supported the topic.");
        }else{
            binding.percentageText.setText("A survey conducted with a sample size of "+gd+" revealed that "+PercentageFormattedValue+"% of respondents supported the topic. The Margin of Error (MOE) for this survey is "+formattedValue+"%.");
        }

       // Toast.makeText(this, resultModel.getAverage_percent().toString(), Toast.LENGTH_SHORT).show();

    }

    private void checkActiveTopicAvailibility(String s){
        if (s.equals("vote")){
            castVote();
        }else{
            GotoMapActivity();
        }
    }
    private void castVote() {
        binding.animationView.setVisibility(View.VISIBLE);
        binding.animationView.playAnimation();

        if (checkLocationPermission(OldTopicActivity.this)) {
            getLastLocation();
        } else {
            // Permission has not been granted yet, request it
            requestLocationPermission(OldTopicActivity.this);
        }
    }
    public void GotoMapActivity(){
        Intent i = new Intent(OldTopicActivity.this,MapsActivity.class);
        i.putExtra("categoryNo",categoryNo);
        i.putExtra("gd",gd);
        i.putExtra("from","deactive");
        i.putExtra("no_of_votes",no_of_votes);
        i.putExtra("key",topicNo);
        startActivity(i);
    }
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }else{
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String zipcode = getZipCode(latitude, longitude,this);
                            goAheadtoCast(zipcode);
                            //   Toast.makeText(TopicsActivity.this, "Latitude: " + latitude + "\nLongitude: " + longitude + "\nZipcode: " + zipcode, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(OldTopicActivity.this, "Location is null , Please click on device location button", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Toast.makeText(OldTopicActivity.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    public void goAheadtoCast(String zipcode){

        mRef.child("topics").child(topicNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {

                String noofvotes;
                String zipcodevotes;
                String Gd , GdZipcodeWise;

                if (snap.child("noofvotes").exists()){
                    noofvotes = snap.child("noofvotes").getValue().toString();
                    Gd  = snap.child("gd").getValue().toString();

                    if (((Integer.parseInt(noofvotes)+1)*100 ) > (0.8 * Integer.parseInt(Gd))){
                        Gd =  Integer.toString(Integer.parseInt(Gd)+2000);
                    }
                    //  createTopicHashMap(Gd,GdZipcodeWise);
                    Toast.makeText(OldTopicActivity.this, noofvotes, Toast.LENGTH_SHORT).show();
                }else{
                    noofvotes = "0";
                    Gd = Integer.toString(5000);
                    Toast.makeText(OldTopicActivity.this, noofvotes, Toast.LENGTH_SHORT).show();
                }

                if (snap.child("voteslocation").child(zipcode).exists()){
                    zipcodevotes = snap.child("voteslocation").child(zipcode).child("noofvotes").getValue().toString();
                    GdZipcodeWise = snap.child("voteslocation").child(zipcode).child("gdzipcodewise").getValue().toString();
                    if (((Integer.parseInt(zipcodevotes)+1)*100 ) > (0.8 * Integer.parseInt(GdZipcodeWise))){
                        GdZipcodeWise  = Integer.toString(Integer.parseInt(GdZipcodeWise)+1000);
                    }
                }else{
                    zipcodevotes = "0";
                    GdZipcodeWise  = Integer.toString(2000);
                }

                createTopicHashMap(Gd, GdZipcodeWise, zipcodevotes,noofvotes,zipcode);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void createTopicHashMap(String Gd , String GdZipcodeWise, String zipcodevotes, String noofvotes, String zipcode){
        Map<String, Object> Zipmap = new HashMap<>();
        Zipmap.put("noofvotes",String.valueOf(Integer.parseInt(zipcodevotes)+1));
        Zipmap.put("gdzipcodewise",GdZipcodeWise);


        Map<String, Object> map = new HashMap<>();
        map.put("noofvotes",String.valueOf(Integer.parseInt(noofvotes)+1));
        map.put("gd",Gd);


        VoteCasted(map,zipcode, Zipmap);
    }
    public void VoteCasted(Map<String, Object> map, String Zipcode,Map<String, Object>  ZipMap){
        mRef.child("topics").child(topicNo).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                mRef.child("topics").child(topicNo).child("voteslocation").child(Zipcode).updateChildren(ZipMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        binding.animationView.setAnimation(R.raw.complete);
                                        binding.animationView.loop(false);
                                        binding.animationView.playAnimation();

                                        binding.animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                // Hide the animation view after second animation completes
                                                binding.animationView.setVisibility(View.GONE);
                                                // Re-enable the download button
                                                //  downloadButton.setEnabled(true);
                                            }
                                        });
                                        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.voteBtn, "rotation", 0f, 360f);
                                        animator.setDuration(500);
                                        animator.start();

                                        //Toast.makeText(TopicsActivity.this, map.get("noofvotes").toString()+" "+map.get("gd").toString(), Toast.LENGTH_SHORT).show();
                                        handler = new Handler(Looper.getMainLooper());
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                setDeActiveTopic();
                                            }
                                        },  60*60*1000);
                                        //  showResults(map.get("gd").toString(), String.valueOf(Integer.parseInt(map.get("noofvotes").toString())+1));

                                        binding.voteBtn.setIconResource(R.drawable.baseline_where_to_voted_24);
                                        binding.voteBtn.setText("VOTED");
                                    }
                                });
                // Apply animation to the icon

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


}