package com.yrlee.tp08tourapi.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yrlee.tp08tourapi.R;

import java.util.ArrayList;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.VH> {

    private ArrayList<String> imageList;

    public ImagePagerAdapter(ArrayList<String> imageList){
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new VH(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        Glide.with(holder.iv)
                .load(imageList.get(position))
                .placeholder(R.drawable.img_no_supported)
                .into(holder.iv);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public void setImageList(ArrayList<String> list){
        this.imageList = list;
        notifyItemChanged(imageList.size());
    }

    static class VH extends RecyclerView.ViewHolder{
        ImageView iv;
        public VH(@NonNull View itemView){
            super(itemView);
            iv = (ImageView) itemView;
        }
    }
}
