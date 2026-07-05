package com.yrlee.tp08tourapi.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yrlee.tp08tourapi.MainActivity;
import com.yrlee.tp08tourapi.R;
import com.yrlee.tp08tourapi.adapter.TourAdapter;
import com.yrlee.tp08tourapi.data.TourItem;

import java.util.ArrayList;

// tour item list 보여줌
public class TourFragment extends Fragment {

    RecyclerView recyclerView;
    TourAdapter tourAdapter;
    ArrayList<TourItem> tourItems = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tour, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        tourAdapter = new TourAdapter(getContext(), tourItems);
        recyclerView.setAdapter(tourAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView,
                                   int dx,
                                   int dy) {

                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0) return;

                LinearLayoutManager manager =
                        (LinearLayoutManager) recyclerView.getLayoutManager();

                int visibleItemCount = manager.getChildCount();
                int totalItemCount = manager.getItemCount();
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {

                    ((MainActivity)getActivity()).loadNextTouristPage();
                }
            }
        });
    }

    public void addItems(ArrayList<TourItem> items){
        int size = tourItems.size();
        if(!items.isEmpty()){
            tourItems.addAll(items);
            tourAdapter.notifyItemRangeChanged(size, items.size());
        }
    }
}