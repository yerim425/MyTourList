package com.yrlee.tp08tourapi.room;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.function.Consumer;

public class BookmarkRepository {

    private final BookmarkDao bookmarkDao;

    public BookmarkRepository(Context context){
        bookmarkDao = AppDatabase.getInstance(context).getBookmarkDao();
    }

    public void insert(BookmarkTour tour) {
        BookmarkManager.getInstance().add(tour.contentId);
        new Thread(() -> bookmarkDao.insert(tour)).start();
    }

    public void delete(String contentId) {
        BookmarkManager.getInstance().remove(contentId);
        new Thread(() -> bookmarkDao.deleteById(contentId)).start();
    }

    public void getAll(Consumer<List<BookmarkTour>> callback) {
        new Thread(() -> {
            List<BookmarkTour> list = bookmarkDao.getAll();

            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(list);
            });
        }).start();
    }
}
