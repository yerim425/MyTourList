package com.yrlee.tp08tourapi.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmark_tour")
public class BookmarkTour {
    @PrimaryKey
    @NonNull
    public String contentId = "";
    public String contentTypeId;
    public String title;
    public String firstImage;
    public String address;
    public String mapx;
    public String mapy;
    public String cat1;
    public String tel;

}
