package com.yrlee.tp08tourapi.room;

import java.util.HashSet;

public class BookmarkManager {

    private static BookmarkManager instance;
    private HashSet<String> bookmarkIds = new HashSet<>();
    // BookmarkManager를 앱에서 딱 하나만 존재하는 객체(Singleton) 로 만들기 위해 생성자를 private로 막기(new 금지)
    private BookmarkManager(){};

    public static BookmarkManager getInstance(){
        if(instance == null){
            instance = new BookmarkManager();
        }
        return instance;
    }

    public void setBookmarkIds(HashSet<String> ids){
        bookmarkIds.clear();
        bookmarkIds = ids;
    }

    public boolean isBookmarked(String id){
        return bookmarkIds.contains(id);
    }

    public void add(String id){
        bookmarkIds.add(id);
    }

    public void remove(String id){
        bookmarkIds.remove(id);
    }

    public int size() {
        return bookmarkIds.size();
    }

    public HashSet<String> getBookmarkIds() {
        return new HashSet<>(bookmarkIds);
    }

}
