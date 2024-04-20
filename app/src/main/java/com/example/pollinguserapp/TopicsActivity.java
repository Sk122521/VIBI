package com.example.pollinguserapp;



import static com.example.pollinguserapp.utils.LocationUtils.checkLocationPermission;
import static com.example.pollinguserapp.utils.LocationUtils.getZipCode;
import static com.example.pollinguserapp.utils.LocationUtils.requestLocationPermission;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import com.example.pollinguserapp.databinding.ActivityTopicsBinding;
import com.example.pollinguserapp.model.Item;
import com.example.pollinguserapp.model.resultModel;
import com.example.pollinguserapp.utils.DepthPageTransformer;
import com.example.pollinguserapp.utils.ResultUtils;
import com.example.pollinguserapp.viewHolder.ViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;

public class TopicsActivity extends AppCompatActivity {

    private ActivityTopicsBinding binding;
    private Timer timer;

    private String categoryNo,gd,no_of_votes;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mRef;
    private  Query ActiveTopicRef;
     private String n;
     private FirebaseRecyclerAdapter<Item, ViewHolder> firebaseRecyclerAdapter;
    private Handler handler;
    private final int DELAY_MS = 2000; // Delay in milliseconds
    private final int PAGE_COUNT = 5; // Number of pages
    private int currentPage = 0;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   EdgeToEdge.enable(this);
        binding = ActivityTopicsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoryNo = getIntent().getStringExtra("categoriesNo");


       // Toast.makeText(TopicsActivity.this,categoryNo, Toast.LENGTH_SHORT).show();

        mRef = FirebaseDatabase.getInstance().getReference("categories").child(categoryNo);
        ActiveTopicRef = mRef.child("activetopic");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        binding.viewPager.setPageTransformer(new DepthPageTransformer());

        DisplayDeactiveTopics();
        setActiveTopic();
        startAutoScroll();



        binding.voteBtn.setOnClickListener(v -> checkActiveTopicAvailibility("vote"));
        binding.MapBtn.setOnClickListener(v -> checkActiveTopicAvailibility("map"));



        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply the insets as a margin to the view. This solution sets only the
            // bottom, left, and right dimensions, but you can apply whichever insets are
            // appropriate to your layout. You can also update the view padding if that's
            // more appropriate.
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.leftMargin = insets.left;
            mlp.bottomMargin = insets.bottom+ dpToPx(16);;
            mlp.rightMargin = insets.right;
            v.setLayoutParams(mlp);

            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            return WindowInsetsCompat.CONSUMED;
        });
    }

    public void GotoMapActivity(){
        Intent i = new Intent(TopicsActivity.this,MapsActivity.class);
        i.putExtra("categoryNo",categoryNo);
        i.putExtra("gd",gd);
        i.putExtra("from","active");
        i.putExtra("no_of_votes",no_of_votes);
        i.putExtra("key","null");
        startActivity(i);
    }
    private void checkActiveTopicAvailibility(String s){
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("activetopic")) {

                    if (s.equals("vote")){
                     castVote();
                    }else{
                      GotoMapActivity();
                    }
                }else{
                    Toast.makeText(TopicsActivity.this, "There is no active topic", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void castVote() {

                if (checkLocationPermission(TopicsActivity.this)) {
                    getLastLocation();
                } else {
                    // Permission has not been granted yet, request it
                    requestLocationPermission(TopicsActivity.this);
                }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }else{
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                             String zipcode = getZipCode(latitude, longitude,this);
                            if (zipcode != null){
                                goAheadtoCast(zipcode);
                            }else{
                                Toast.makeText(this, "Please wait , Currently Could not get Location ", Toast.LENGTH_SHORT).show();
                            }
                            //   Toast.makeText(TopicsActivity.this, "Latitude: " + latitude + "\nLongitude: " + longitude + "\nZipcode: " + zipcode, Toast.LENGTH_LONG).show();
                        } else {
                            binding.animationView.setVisibility(View.GONE);
                            Toast.makeText(TopicsActivity.this, "Location is null , Please click on device location button", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Toast.makeText(TopicsActivity.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, proceed with your logic
                // For example, start location updates or show the user's location on a map
                // startLocationUpdates();
                getLastLocation();
            } else {
                // Location permission denied, inform the user accordingly
                showPermissionDeniedDialog();
                Toast.makeText(this, "Please Provide Location Permission to vote", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void goAheadtoCast(String zipcode){

        binding.animationView.setVisibility(View.VISIBLE);
        binding.animationView.playAnimation();

        ActiveTopicRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    Toast.makeText(TopicsActivity.this, noofvotes, Toast.LENGTH_SHORT).show();
                }else{
                    noofvotes = "0";
                    Gd = Integer.toString(5000);
                    Toast.makeText(TopicsActivity.this, noofvotes, Toast.LENGTH_SHORT).show();
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

//        Map<String, Object> childmap = new HashMap<>();
//        childmap.put(zipcode,Zipmap);

        Map<String, Object> map = new HashMap<>();
        map.put("noofvotes",String.valueOf(Integer.parseInt(noofvotes)+1));
        map.put("gd",Gd);

        VoteCasted(map,zipcode,Zipmap);
    }

    public void VoteCasted(Map<String, Object> map, String zipcode , Map<String, Object> zipmap){
        mRef.child("activetopic").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mRef.child("activetopic").child("voteslocation").child(zipcode).updateChildren(zipmap).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setActiveTopic();
                            }
                        },  60*60*1000);
                        //  showResults(map.get("gd").toString(), String.valueOf(Integer.parseInt(map.get("noofvotes").toString())+1));

                        binding.voteBtn.setIconResource(R.drawable.baseline_where_to_voted_24);
                        binding.voteBtn.setText("VOTED");
                        // Apply animation to the icon

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
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
    public void setAdapter(String n){
        Query startAt = mRef.child("topics").orderByChild("timestamp");//.limitToFirst(Integer.parseInt(n));
        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(startAt, Item.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Item,ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Item model) {
                // ScrollRv();
                //  holder.imageVie
                Glide.with(getApplicationContext()).load(model.getImage()).into(holder.imageView);
                holder.title.setText(model.getTitle());

                ResultUtils resultUtils = new ResultUtils(Integer.parseInt(model.getNoofvotes()),Integer.parseInt(model.getGd()));
                resultModel resultModel = resultUtils.getResult();

                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                String PercentageFormattedValue = decimalFormat.format(resultModel.getAverage_percent());

                holder.percentage.setText("Percentage = "+PercentageFormattedValue +" %");
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       // Toast.makeText(TopicsActivity.this,getRef(currentPage).getKey(), Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(TopicsActivity.this, OldTopicActivity.class);
                        i.putExtra("topicKey",getRef(currentPage).getKey());
                        i.putExtra("Category",categoryNo);
                        startActivity(i);
                    }
                });
            }
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.deactive_item_topic_layout,parent,false));
            }
        };
        binding.viewPager.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
//        // Set custom page transformer for transition effect
//        binding.viewPager.setPageTransformer(new DepthPageTransformer());
    }
    public void DisplayDeactiveTopics(){
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){

                    if (snapshot.child("nodeactivetopic").exists() && !Objects.equals(snapshot.child("nodeactivetopic").getValue(), "0")){
                        n = snapshot.child("nodeactivetopic").getValue().toString();
                       // Toast.makeText(TopicsActivity.this, n +" no ", Toast.LENGTH_SHORT).show();
                        if (!n.equals("0")){
                            setAdapter(n);
                        }
                       //
                    }else{
                        mRef.child("topics").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                n = Integer.toString((int)snapshot.getChildrenCount());
                              //  Toast.makeText(TopicsActivity.this, n +" co ", Toast.LENGTH_SHORT).show();
                               // setAdapter(n);
                                if (!n.equals("0")){
                                    setAdapter(n);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void setActiveTopic() {
        ActiveTopicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.activeTopicDesc.setText(snapshot.child("description").getValue().toString());
                    binding.activeTopicTitle.setText(snapshot.child("title").getValue().toString());

                     gd = snapshot.child("gd").getValue().toString();
                     no_of_votes = snapshot.child("noofvotes").getValue().toString();

                    binding.percentageText.setText(String.format("Percentage that voted for above topic of out of sample of %s : ", gd));
//                    binding.ErrorText.setText(String.format("Margin Of Error(MOE) out of sample of %s : ", gd));

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

    private void startAutoScroll() {
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Increment current page index
                currentPage++;
             //   Toast.makeText(TopicsActivity.this, currentPage +" no ", Toast.LENGTH_SHORT).show();
                if (currentPage >= Integer.parseInt(n)) {
                    currentPage = 0; // Reset to the first page
                }
                binding.viewPager.setCurrentItem(currentPage, true); // Smooth scroll to the next page

                // Repeat the process recursively
                handler.postDelayed(this, DELAY_MS);
            }
        }, DELAY_MS);
    }
    public int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Permission Denied");
        builder.setMessage("Please grant location permission in settings to use this feature.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Request location permission again
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                binding.animationView.setVisibility(View.GONE);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}