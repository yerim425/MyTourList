package com.yrlee.tp08tourapi.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(BookmarkTour tour);

    @Delete
    void delete(BookmarkTour tour);

    @Query("SELECT * FROM bookmark_tour")
    List<BookmarkTour> getAll();

    @Query("SELECT * FROM bookmark_tour WHERE contentId = :id LIMIT 1")
    BookmarkTour getById(String id);

    @Query("DELETE FROM bookmark_tour WHERE contentId = :id")
    void deleteById(String id);

    @Query("SELECT EXISTS(SELECT 1 FROM bookmark_tour WHERE contentId = :id)")
    boolean isBookmarked(String id);
}
