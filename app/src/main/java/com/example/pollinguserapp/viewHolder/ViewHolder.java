package com.example.pollinguserapp.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pollinguserapp.R;


public class ViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public TextView title, percentage;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.deactive_topic_image);
        title = itemView.findViewById(R.id.topic_title);
        percentage = itemView.findViewById(R.id.topic_percentage);
    }
}