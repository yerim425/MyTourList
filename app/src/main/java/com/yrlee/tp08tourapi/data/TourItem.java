package com.yrlee.tp08tourapi.data;

public class TourItem {

    public String contentId; // 콘텐츠 id
    public String contentTypeId; // 콘텐츠 타입 id (12:관광지, ...)
    public String title; // 관광지 이름
    public String firstImage; // 관광지 대표 이미지(원본)
    public String addr1; // 주소
    public String addr2; // 상세 주소
    public String mapx;
    public String mapy;
    public String eventStartDate;
    public String eventEndDate;
    public String lclsSystm1; // 분류(자연, 문화시설, 축제 등)
}
