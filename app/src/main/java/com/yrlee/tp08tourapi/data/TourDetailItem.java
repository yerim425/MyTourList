package com.yrlee.tp08tourapi.data;

import java.util.List;

public class TourDetailItem {

    public String contentId;
    public String contentTypeId = "";

    // 공통정보조회
    public String title;
    public String homePage="";
    public String firstImage="";
    public String addr1="";
    public String addr2="";
    public String mapx="";
    public String mapy="";
    public String overview="";
    public String lclsSystm1; // 대분류(자연,문화시설, 축제 등)

    public String lclsSystm2; // 중분류

    public String lclsSystm3; // 소분류

    public String takeTime; // 총 소요시간 - 여행코스

    public List<TourDetailIntroItem> introList;
    public List<TourDetailRecommendItem> recommendList;

    public TourDetailItem(String id){
        this.contentId = id;
    }

}

