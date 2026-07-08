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
import com.yrlee.tp08tourapi.BookmarkActivity;
import com.yrlee.tp08tourapi.MainActivity;
import com.yrlee.tp08tourapi.R;
import com.yrlee.tp08tourapi.TourDetailActivity;
import com.yrlee.tp08tourapi.data.TourItem;
import com.yrlee.tp08tourapi.room.BookmarkManager;
import com.yrlee.tp08tourapi.room.BookmarkRepository;
import com.yrlee.tp08tourapi.room.BookmarkTour;
import com.yrlee.tp08tourapi.util.Constants;
import com.yrlee.tp08tourapi.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.VH> {

    private Context context;
    private List<BookmarkTour> itemList;
    private BookmarkRepository bookmarkRepository;

    public BookmarkAdapter(Context context, List<BookmarkTour> items){
        this.context = context;
        this.itemList = items;
        bookmarkRepository = new BookmarkRepository(context);
    }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.recycler_tour_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkAdapter.VH holder, int position) {

        BookmarkTour item = itemList.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvAddr.setText(item.address);
        if(item.address == null){
            holder.tvAddr.setVisibility(GONE);
        }else{
            holder.tvAddr.setText(item.address);
            holder.tvAddr.setVisibility(VISIBLE);
        }
        if(item.eventStartDate==null || item.eventStartDate.isEmpty()) {
            holder.tvDate.setVisibility(GONE);
            holder.tvDate.setText("");
        } else {
            String date = DateUtil.getEventDate(item.eventStartDate, item.eventEndDate);
            holder.tvDate.setVisibility(VISIBLE);
            holder.tvDate.setText(date);
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
                .placeholder(R.drawable.img_search)
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
                bt.title = item.title;
                bt.address = item.address;
                bt.firstImage = item.firstImage;
                bt.mapx = item.mapx;
                bt.mapy = item.mapy;
                bt.lclsSystm1 = item.lclsSystm1;
                bt.eventStartDate = item.eventStartDate;
                bt.eventEndDate = item.eventEndDate;
                bookmarkRepository.insert(bt);
            }else{
                bookmarkRepository.delete(item.contentId);
            }

        });
        // 위로 스크롤 시 새로 고침
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setItems(List<BookmarkTour> list){
        itemList.addAll(list);
        notifyItemChanged(itemList.size());
    }

    public void clear(){
        itemList.clear();
        notifyDataSetChanged();
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
