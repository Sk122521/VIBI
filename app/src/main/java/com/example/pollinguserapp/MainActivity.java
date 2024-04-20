package com.example.pollinguserapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pollinguserapp.databinding.ActivityMainBinding;
import com.example.pollinguserapp.databinding.CustomViewLayoutBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;
    private FrameLayout circleContainer;
    private TextView textView;
    private String textToDisplay;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));

        // words = getResources().getString(R.string.text_c).split("\\s+"); // Split your text into words
        textToDisplay = getResources().getString(R.string.text_c);
        // Start displaying text word by word
        displayTextLetterByLetter();


        final FrameLayout main = activityMainBinding.main;

//        circleContainer = new FrameLayout(this);
//        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        circleContainer.setLayoutParams(containerParams);
//        main.addView(circleContainer);

        int[] imageResources = {R.drawable.hare_krishna,R.drawable.aglepolling,R.drawable.nomadspolling,R.drawable.naturepolling, R.drawable.templepolling};


        int numViews = 5;
        for (int i = 0; i < numViews; i++) {
            int x = i;
            CustomViewLayoutBinding customViewBinding = CustomViewLayoutBinding.inflate(LayoutInflater.from(this), main, false);
            View v = customViewBinding.getRoot();


            FirebaseDatabase.getInstance().getReference().child("categories").child(Integer.toString(x)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()){
                        if (snapshot.hasChild("image")){
                            Glide.with(getApplicationContext()).load(snapshot.child("image").getValue().toString()).into(customViewBinding.imageCategories);
                        }else{
                            customViewBinding.imageCategories.setImageResource(imageResources[x]);
                        }
                    }else{
                        customViewBinding.imageCategories.setImageResource(imageResources[x]);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            customViewBinding.imageCategories.setImageResource(imageResources[i]);
            main.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove the listener to ensure it only gets called once
                    main.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // Calculate 5% of the parent's width and height
                    int parentWidth = main.getWidth();
                    int parentHeight = main.getHeight();
                    int viewSize = (int) (Math.min(parentWidth, parentHeight) * 0.2);
                    int maxTranslation = Math.min(parentWidth, parentHeight) / 2 - viewSize / 2;

                    // 5% of the minimum dimension
                 //   Toast.makeText(MainActivity.this, Integer.toString(parentWidth), Toast.LENGTH_SHORT).show();
                    v.setBackgroundColor(0xffff0000);
                    // Force the views to a nice size (150x100 px) that fits my display.
                    // This should of course be done in a display size independent way.
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(viewSize, viewSize);

                    // Place all views in the center of the layout. We'll transform them
                    // away from there in the code below.
                    lp.gravity = Gravity.CENTER;
                    // Set layout params on view.
                    v.setLayoutParams(lp);

                    // Calculate the angle of the current view. Adjust by 90 degrees to
                    // get View 0 at the top. We need the angle in degrees and radians.
                    float angleDeg = x * 360.0f / numViews - 90.0f;
                    float angleRad = (float) (angleDeg * Math.PI / 180.0f);
                    float translationX = maxTranslation * (float) Math.cos(angleRad);
                    float translationY = maxTranslation * (float) Math.sin(angleRad);


                    v.setTranslationX(translationX);
                    v.setTranslationY(translationY);

               //     Toast.makeText(MainActivity.this, Float.toString(300 * (float) Math.cos(angleRad))+"  ues", Toast.LENGTH_SHORT).show();
//                    v.setTranslationX(300 * (float) Math.cos(angleRad));
//                    v.setTranslationY(300 * (float) Math.sin(angleRad));
                    // Set the rotation of the view.
                    v.setRotation(angleDeg + 90.0f);

                   // customViewBinding.imageCategories.setImageResource(imageResources[x]);

//            RippleDrawable rippleDrawable = (RippleDrawable) getResources().getDrawable(R.drawable.ripple_effect, null);
//            cardView.setBackground(rippleDrawable);

                    main.addView(v);

                    customViewBinding.imageCategories.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MainActivity.this, TopicsActivity.class);
                            intent.putExtra("categoriesNo",Integer.toString(x));
                            startActivity(intent);
                        }
                    });
//                    customViewBinding.imageCategories.setOnLongClickListener(new View.OnLongClickListener() {
//                        @Override
//                        public boolean onLongClick(View v) {
//                            Intent intent = new Intent(MainActivity.this, ChangeCategory.class);
//                            intent.putExtra("categoriesNo", Integer.toString(x));
//                            startActivity(intent);
//                            return true;
//                        }
//                    });

//            ObjectAnimator rotation = ObjectAnimator.ofFloat(v, View.ROTATION, 0, 360);
//            rotation.setDuration(10000); // Adjust duration as needed
//            rotation.setRepeatCount(ObjectAnimator.INFINITE);
//            rotation.setInterpolator(null); // Linear interpolator for smooth rotation
//            rotation.start();
                    float startScale = 0.8f;
                    float endScale = 1.3f; // Scale factor when image is at 270 degrees
                    if (x == numViews) endScale = 1.0f; // Reset scale factor for the last image
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, View.SCALE_X, startScale, endScale, startScale);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, View.SCALE_Y, startScale, endScale, startScale);
                    scaleX.setDuration(10000); // Adjust duration as needed
                    scaleX.setRepeatCount(ObjectAnimator.INFINITE);
                    scaleX.setInterpolator(null); // Linear interpolator for smooth scaling
                    scaleY.setDuration(10000); // Adjust duration as needed
                    scaleY.setRepeatCount(ObjectAnimator.INFINITE);
                    scaleY.setInterpolator(null); // Linear interpolator for smooth scaling
                    scaleX.start();
                    scaleY.start();

                }
            });

        }
        ObjectAnimator rotation = ObjectAnimator.ofFloat(main, View.ROTATION, 0, 360);
        rotation.setDuration(10000); // Adjust duration as needed
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.setInterpolator(null); // Linear interpolator for smooth rotation
        rotation.start();
    }

        @Override
        protected void onDestroy () {
            super.onDestroy();

            activityMainBinding = null;
        }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            adjustViewsPosition();
//        }
//    }

//    private void adjustViewsPosition() {
//        int circleRadius = Math.min(activityMainBinding.main.getWidth(), activityMainBinding.main.getHeight()) / 2;
//        int centerX = activityMainBinding.main.getWidth() / 2;
//        int centerY = activityMainBinding.main.getHeight() / 2;
//        for (int i = 0; i < circleContainer.getChildCount(); i++) {
//            View view = circleContainer.getChildAt(i);
//            double angle = Math.toRadians((360 / 5) * i + circleContainer.getRotation());
//            int xPosition = (int) (centerX + circleRadius * Math.cos(angle)) - view.getWidth() / 2;
//            int yPosition = (int) (centerY + circleRadius * Math.sin(angle)) - view.getHeight() / 2;
//            view.setX(xPosition);
//            view.setY(yPosition);
//        }
//    }

    private void displayTextLetterByLetter() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentIndex < textToDisplay.length()) {
                    activityMainBinding.text.append(String.valueOf(textToDisplay.charAt(currentIndex)));
                    currentIndex++;
                    handler.postDelayed(this, 100); // Change the delay as per your preference
                }
            }
        }, 100); // Initial delay, adjust as needed
    }


    }