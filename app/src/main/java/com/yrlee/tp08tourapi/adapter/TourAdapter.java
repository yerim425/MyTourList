package com.yrlee.tp08tourapi.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yrlee.tp08tourapi.R;
import com.yrlee.tp08tourapi.TourDetailActivity;
import com.yrlee.tp08tourapi.data.TourItem;
import com.yrlee.tp08tourapi.room.BookmarkManager;
import com.yrlee.tp08tourapi.room.BookmarkRepository;
import com.yrlee.tp08tourapi.room.BookmarkTour;
import com.yrlee.tp08tourapi.util.Constants;
import com.yrlee.tp08tourapi.util.DateUtil;

import java.util.ArrayList;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.VH> {
    Context context;
    ArrayList<TourItem> touristItems;
    BookmarkRepository bookmarkRepository;

    public TourAdapter(Context context, ArrayList<TourItem> list){
        this.context = context;
        this.touristItems = list;
        bookmarkRepository = new BookmarkRepository(context);
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
        if(item.addr1 == null){
            holder.tvAddr.setVisibility(GONE);
        }else{
            holder.tvAddr.setText(item.addr2==null ? item.addr1 : item.addr1 + ' ' + item.addr2);
            holder.tvAddr.setVisibility(VISIBLE);
        }
        if(item.eventStartDate==null || item.eventStartDate.isEmpty()) {
            holder.tvDate.setVisibility(GONE);
            holder.tvDate.setText("");
        }
        else {
            holder.tvDate.setText(DateUtil.getEventDate(item.eventStartDate, item.eventEndDate));
            holder.tvDate.setVisibility(VISIBLE);
        }
        String category = Constants.CATEGORY1_MAP.get(item.lclsSystm1);
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
                .placeholder(R.drawable.img_logo)
                .into(holder.ivImage);


        holder.layout.setOnClickListener( v-> {
            Intent intent = new Intent(context, TourDetailActivity.class);
                    intent.putExtra("contentId", item.contentId);
            context.startActivity(intent);
            }
        );

        // 기존 리스너 제거 - RecyclerView는 ViewHolder를 재사용하기 때문에
        holder.cbBookmark.setOnCheckedChangeListener(null);

        // 체크 상태 설정
        holder.cbBookmark.setChecked(
                BookmarkManager.getInstance().isBookmarked(item.contentId)
        );

        // 다시 리스너 등록 - 찜 등록/해지 요청
        holder.cbBookmark.setOnClickListener(v->{
            if(holder.cbBookmark.isChecked()){
                BookmarkTour bt = new BookmarkTour();
                bt.contentId = item.contentId;
                bt.contentTypeId = item.contentTypeId;
                bt.title = item.title;
                bt.address = item.addr1;
                bt.firstImage = item.firstImage;
                bt.mapx = item.mapx;
                bt.mapy = item.mapy;
                bt.lclsSystm1 = item.lclsSystm1;
                bookmarkRepository.insert(bt);
            }else{
                bookmarkRepository.delete(item.contentId);
            }

        });
    }

    @Override
    public int getItemCount() {
        return touristItems.size();
    }

    class VH extends RecyclerView.ViewHolder{

        TextView tvTitle, tvAddr, tvDate, tvCategory;
        ImageView ivImage;
        RelativeLayout layout;

        CheckBox cbBookmark;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAddr = itemView.findViewById(R.id.tv_addr);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvCategory = itemView.findViewById(R.id.tv_category);
            ivImage = itemView.findViewById(R.id.iv_first_image);
            layout = itemView.findViewById(R.id.layout_item);
            cbBookmark = itemView.findViewById(R.id.cb_bookmark);
        }
    }
}
