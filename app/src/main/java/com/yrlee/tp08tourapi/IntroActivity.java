package com.yrlee.tp08tourapi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.RoomDatabase;

import com.yrlee.tp08tourapi.room.AppDatabase;
import com.yrlee.tp08tourapi.room.BookmarkDao;
import com.yrlee.tp08tourapi.room.BookmarkManager;
import com.yrlee.tp08tourapi.room.BookmarkTour;

import java.util.HashSet;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private AppDatabase db;
    private BookmarkDao bookmarkDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getInstance(this);
        bookmarkDao = db.getBookmarkDao();

        new Thread(() -> {

            List<BookmarkTour> bookmarkList = bookmarkDao.getAll();

            // 필요하면 HashSet으로 변환
            HashSet<String> bookmarkIds = new HashSet<>();
            for (BookmarkTour item : bookmarkList) {
                bookmarkIds.add(item.contentId);
            }

            // 전역 저장
            BookmarkManager.getInstance().setBookmarkIds(bookmarkIds);

            runOnUiThread(() -> {
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }, 2000);
            });

        }).start();
    }
}