package com.yrlee.tp08tourapi.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmark_tour")
public class BookmarkTour {
    @PrimaryKey
    @NonNull
    public String contentId = "";
    public String title;
    public String image;
    public String address;
    public String mapx;
    public String mapy;
    public String category;

}
