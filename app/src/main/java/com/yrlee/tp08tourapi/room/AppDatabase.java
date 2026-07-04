package com.yrlee.tp08tourapi.room;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {BookmarkTour.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // bookmark_tour 테이블의 CRUD 작업을 수행하는 Dao 객체를 얻어오는 작업 추상메소드
    public abstract BookmarkDao getBookmarkDao(); // BookmarkDao 라는 인터페이스를 객체로 만들어 리턴해주는 것을 알아서 해줌.

    public static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "tour_db"
            ).build();
        }
        return instance;
    }
}