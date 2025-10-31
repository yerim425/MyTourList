package com.yrlee.tp08tourapi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yrlee.tp08tourapi.R;
import com.yrlee.tp08tourapi.data.CodeItem;

import java.util.ArrayList;

public class AreaAdapter extends RecyclerView.Adapter<AreaAdapter.VH> {
    Context context;
    ArrayList<CodeItem> areaItems;
    public AreaAdapter(Context context, ArrayList<CodeItem> list){
        this.context = context;
        this.areaItems = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.recycler_area_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CodeItem item = areaItems.get(position);
        holder.tvName.setText(item.name);

    }

    @Override
    public int getItemCount() {
        return areaItems.size();
    }

    class VH extends RecyclerView.ViewHolder{

        TextView tvName;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}
