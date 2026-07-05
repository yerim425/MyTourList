package com.yrlee.tp08tourapi.data;

public class TourDetailItem {

    public String contentId;
    public String contentTypeId;

    // 상세소개
    public String title;
    public String createTime;
    public String modifiedItem="";
    public String tel="";
    public String homePage="";
    public String firstImage="";
    public String firstImage2="";
    public String addr1="";
    public String addr2="";
    public String mapx="";
    public String mapy="";
    public String overview="";

    //

    public TourDetailItem(String id, String typeId){
        this.contentId = id;
        this.contentTypeId = typeId;
    }

}
