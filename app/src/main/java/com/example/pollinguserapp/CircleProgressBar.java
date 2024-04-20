package com.example.pollinguserapp;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CircleProgressBar extends View {
    private Float progress = (float) 0;
    private int maxProgress = 100;
    private Paint circlePaint;
    private Paint textPaint;
    private float animationProgress = 0;
    private Paint progressPaint;

    public CircleProgressBar(Context context) {
        super(context);
        init();
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setColor(Color.GRAY);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(10);
        circlePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);

        progressPaint = new Paint();
        progressPaint.setColor(Color.BLUE); // Change color as needed
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20);
        progressPaint.setAntiAlias(true);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnimation();
            }
        });
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        int viewWidth = getWidth();
//        int viewHeight = getHeight();
//        int radius = Math.min(viewWidth, viewHeight) / 2 - 10;
//        float centerX = viewWidth / 2;
//        float centerY = viewHeight / 2;
//
//        // Draw the full circle
//        canvas.drawCircle(centerX, centerY, radius, circlePaint);
//
//        // Calculate the sweep angle based on the progress
//        float sweepAngle = 360f * progress / maxProgress;
//
//        // Draw the progress arc
//        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
//                -90, sweepAngle, false, progressPaint);
//
//        // Draw the text in the center
//        canvas.drawText(progress + "%", centerX, centerY, textPaint);
//    }
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int viewWidth = getWidth();
    int viewHeight = getHeight();
    int radius = Math.min(viewWidth, viewHeight) / 2 - 10;
    float centerX = viewWidth / 2;
    float centerY = viewHeight / 2;

    // Draw the full circle
    canvas.drawCircle(centerX, centerY, radius, circlePaint);

    // Calculate the sweep angle based on the progress
    float sweepAngle = 360f * progress / maxProgress;

    // Draw the progress arc
    canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
            -90, sweepAngle, false, progressPaint);

    // Adjust text size to fit inside the circle
    textPaint.setTextSize(20); // Initial text size
    textPaint.setTextAlign(Paint.Align.CENTER);
    float textWidth = textPaint.measureText(String.format("%.1f", progress) + "%");
    float textHeight = textPaint.descent() - textPaint.ascent();
    float textSize = Math.min(viewWidth, viewHeight) * 0.5f / textWidth * textHeight;
    textPaint.setTextSize(textSize);

    // Draw the text in the center
    float yOffset = centerY - (textPaint.ascent() + textPaint.descent()) / 2;
    canvas.drawText(String.format("%.1f", progress) + "%", centerX, yOffset, textPaint);
}



    public void setProgress(Float progress) {
        this.progress = progress;
        startAnimation();
    }

    private void startAnimation() {
        CircleAnimation animation = new CircleAnimation(animationProgress, progress);
        animation.setDuration(1000);
        startAnimation(animation);
    }

    private class CircleAnimation extends Animation {
        private float startAngle;
        private float endAngle;

        CircleAnimation(float start, float end) {
            startAngle = start;
            endAngle = end;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation transformation) {
            animationProgress = startAngle + ((endAngle - startAngle) * interpolatedTime);
            invalidate();
        }
    }
}

