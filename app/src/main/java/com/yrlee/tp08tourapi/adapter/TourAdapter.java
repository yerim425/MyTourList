package com.yrlee.tp08tourapi.adapter;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yrlee.tp08tourapi.MainActivity;
import com.yrlee.tp08tourapi.R;
import com.yrlee.tp08tourapi.data.TourItem;

import java.util.ArrayList;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.VH> {
    Context context;
    ArrayList<TourItem> touristItems;
    public TourAdapter(Context context, ArrayList<TourItem> list){
        this.context = context;
        this.touristItems = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.recycler_tour_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TourItem item = touristItems.get(position);
        holder.tvTitle.setText(item.title);
        String addr = item.addr1;
        if(item.addr2 != null) addr+=item.addr2;
        holder.tvAddr.setText(addr);
        if(item.tel==null) {
            holder.tvTel.setVisibility(GONE);
            holder.tvTel.setText("");
        }
        else {
            holder.tvTel.setVisibility(VISIBLE);
            holder.tvTel.setText(item.tel);
        }
        String category = ((MainActivity)context).categoryMap.get(item.cat1);
        if(category!=null){
            holder.tvCategory.setVisibility(VISIBLE);
            holder.tvCategory.setText("#"+category);
        }else{
            holder.tvCategory.setVisibility(GONE);
            holder.tvCategory.setText("");
        }
        Glide.with(holder.itemView)
                .load(item.firstImage)
                .centerCrop()
                .placeholder(R.drawable.img_search)
                .into(holder.ivImage);

    }

    @Override
    public int getItemCount() {
        return touristItems.size();
    }

    class VH extends RecyclerView.ViewHolder{

        TextView tvTitle, tvAddr, tvTel, tvCategory;
        ImageView ivImage;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAddr = itemView.findViewById(R.id.tv_addr);
            tvTel = itemView.findViewById(R.id.tv_tel);
            tvCategory = itemView.findViewById(R.id.tv_category);
            ivImage = itemView.findViewById(R.id.iv_first_image);
        }
    }
}
