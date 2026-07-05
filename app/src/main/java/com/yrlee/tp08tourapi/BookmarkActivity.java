package com.yrlee.tp08tourapi;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.yrlee.tp08tourapi.adapter.BookmarkAdapter;
import com.yrlee.tp08tourapi.adapter.TourAdapter;
import com.yrlee.tp08tourapi.room.AppDatabase;
import com.yrlee.tp08tourapi.room.BookmarkDao;
import com.yrlee.tp08tourapi.room.BookmarkRepository;
import com.yrlee.tp08tourapi.room.BookmarkTour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {

    // db 읽어오기
    // 뿌리기
    // 함수 연결

    private List<BookmarkTour> bookmarkTours;

    // ui
    private TextView tvSize;
    private RecyclerView rvTours;
    private TextView tvNoBookmark;

    // adapter
    private BookmarkAdapter tourAdapter;

    private BookmarkRepository repository;

    // swipe refresh layout
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bookmark);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new BookmarkRepository(this);

        // ui 초기화
        tvSize = findViewById(R.id.tv_size);
        rvTours = findViewById(R.id.rv_tours);
        tvNoBookmark = findViewById(R.id.tv_no_bookmark);

        loadBookmarkTours();

        // 새로고침
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(() -> {
            bookmarkTours.clear(); // 기존 데이터 초기화
            tourAdapter.clear();
            loadBookmarkTours(); // 다시 북마크 데이터 로드
            swipeRefresh.setRefreshing(false);
        });
    }

    private void loadBookmarkTours(){
        // 북마크 장소 데이터 가져오기
        repository.getAll(itemList->{
            bookmarkTours = itemList;
            if(tourAdapter == null) {
                tourAdapter = new BookmarkAdapter(this, bookmarkTours);
                rvTours.setAdapter(tourAdapter);
            }else{
                tourAdapter.setItems(bookmarkTours);
            }
            tvSize.setText(""+bookmarkTours.size());
            if(bookmarkTours.isEmpty()) tvNoBookmark.setVisibility(VISIBLE);
            else tvNoBookmark.setVisibility(INVISIBLE);
        });
    }

    public void openKakaoMap(String title, String latitude, String longitude) {

        if(latitude == null || longitude == null){
            Toast.makeText(this, "위경도가 옳바르지 않습니다.", Toast.LENGTH_SHORT).show();
        }

        String url = "kakaomap://look?p=" + latitude + "," + longitude;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("net.daum.android.map");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "카카오맵이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}