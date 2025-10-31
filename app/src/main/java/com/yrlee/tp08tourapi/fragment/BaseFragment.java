package com.yrlee.tp08tourapi.fragment;

import static android.view.View.VISIBLE;

import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.yrlee.tp08tourapi.adapter.TourAdapter;
import com.yrlee.tp08tourapi.data.TourItem;

import java.util.ArrayList;

public class BaseFragment extends Fragment {

    RecyclerView recyclerView;
    TourAdapter tourAdapter;
    ArrayList<TourItem> tourItems = new ArrayList<>();
    TextView tvNoData;

    public void addItems(ArrayList<TourItem> items){
        int size = tourItems.size();
        if(items.isEmpty() && size == 0){
            tvNoData.setVisibility(VISIBLE);
        }else{
            tourItems.addAll(items);
            tourAdapter.notifyItemRangeChanged(size, items.size());
        }
    }
}
