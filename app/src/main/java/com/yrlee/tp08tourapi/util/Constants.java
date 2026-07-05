package com.yrlee.tp08tourapi.util;

import static java.util.Map.entry;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Constants {

    public static final Map<String, String> CONTENT_TYPE_MAP;
    public static final Map<String, String> CATEGORY_MAP;
    public static final Map<String, String> SIDO_MAP;
    public static final Map<String, Map<String, String>> SIGUNGU_MAP;


    static {

//        CONTENT_TYPE_MAP = Map.of(
//                "관광지", "12",
//                "문화시설", "14",
//                "축제공연행사", "15",
//                "여행코스", "25",
//                "레포츠", "28");
//
        Map<String, String> typeMap = new LinkedHashMap<>();
        typeMap.put("관광지", "12");
        typeMap.put("문화시설", "12");
        typeMap.put("축제공연행사", "15");
        typeMap.put("레포츠", "28");
        typeMap.put("여행코스", "25");
        CONTENT_TYPE_MAP = Collections.unmodifiableMap(typeMap);;

        CATEGORY_MAP = Map.of(
                "A01", "자연",
                "A02", "인문(문화/예술/역사)",
                "A03", "레포츠",
                "B02", "숙박",
                "A04", "쇼핑",
                "C01", "추천코스",
                "A05", "음식"
        );
        Map<String, String> map = new LinkedHashMap<>();
        map.put("서울", "1");
        map.put("인천", "2");
        map.put("대전", "3");
        map.put("대구", "4");
        map.put("광주", "5");
        map.put("부산", "6");
        map.put("울산", "7");
        map.put("세종특별자치시", "8");
        map.put("경기도", "31");
        map.put("강원특별자치도", "32");
        map.put("충청북도", "33");
        map.put("충청남도", "34");
        map.put("경상북도", "35");
        map.put("경상남도", "36");
        map.put("전북특별자치도", "37");
        map.put("전라남도", "38");
        map.put("제주특별자치도", "39");
        SIDO_MAP = Collections.unmodifiableMap(map);

        SIGUNGU_MAP = Map.ofEntries(
                entry("1", Map.ofEntries(
                        entry("강남구", "1"),
                        entry("강동구", "2"),
                        entry("강북구", "3"),
                        entry("강서구", "4"),
                        entry("관악구", "5"),
                        entry("광진구", "6"),
                        entry("구로구", "7"),
                        entry("금천구", "8"),
                        entry("노원구", "9"),
                        entry("도봉구", "10"),
                        entry("동대문구", "11"),
                        entry("동작구", "12"),
                        entry("마포구", "13"),
                        entry("서대문구", "14"),
                        entry("서초구", "15"),
                        entry("성동구", "16"),
                        entry("성북구", "17"),
                        entry("송파구", "18"),
                        entry("양천구", "19"),
                        entry("영등포구", "20"),
                        entry("용산구", "21"),
                        entry("은평구", "22"),
                        entry("종로구", "23"),
                        entry("중구", "24"),
                        entry("중랑구", "25")
                )),
                entry("2", Map.ofEntries(
                        entry("강화군", "1"),
                        entry("계양구", "2"),
                        entry("미추홀구", "3"),
                        entry("남동구", "4"),
                        entry("동구", "5"),
                        entry("부평구", "6"),
                        entry("서구", "7"),
                        entry("연수구", "8"),
                        entry("옹진군", "9"),
                        entry("중구", "10")
                )),
                entry("3", Map.ofEntries(
                        entry("대덕구", "1"),
                        entry("동구", "2"),
                        entry("서구", "3"),
                        entry("유성구", "4"),
                        entry("중구", "5")
                )),
                entry("4", Map.ofEntries(
                        entry("남구", "1"),
                        entry("달서구", "2"),
                        entry("달성군", "3"),
                        entry("동구", "4"),
                        entry("북구", "5"),
                        entry("서구", "6"),
                        entry("수성구", "7"),
                        entry("중구", "8"),
                        entry("군위군", "9")
                )),
                entry("5", Map.ofEntries(
                        entry("광산구", "1"),
                        entry("남구", "2"),
                        entry("동구", "3"),
                        entry("북구", "4"),
                        entry("서구", "5")
                )),
                entry("6", Map.ofEntries(
                        entry("강서구", "1"),
                        entry("금정구", "2"),
                        entry("기장군", "3"),
                        entry("남구", "4"),
                        entry("동구", "5"),
                        entry("동래구", "6"),
                        entry("부산진구", "7"),
                        entry("북구", "8"),
                        entry("사상구", "9"),
                        entry("사하구", "10"),
                        entry("서구", "11"),
                        entry("수영구", "12"),
                        entry("연제구", "13"),
                        entry("영도구", "14"),
                        entry("중구", "15"),
                        entry("해운대구", "16")
                )),
                entry("7", Map.ofEntries(
                        entry("중구", "1"),
                        entry("남구", "2"),
                        entry("동구", "3"),
                        entry("북구", "4"),
                        entry("울주군", "5")
                )),
                entry("8", Map.ofEntries(
                        entry("세종특별자치시", "1")
                )),
                entry("31", Map.ofEntries(
                        entry("가평군", "1"),
                        entry("고양시", "2"),
                        entry("과천시", "3"),
                        entry("광명시", "4"),
                        entry("광주시", "5"),
                        entry("구리시", "6"),
                        entry("군포시", "7"),
                        entry("김포시", "8"),
                        entry("남양주시", "9"),
                        entry("동두천시", "10"),
                        entry("부천시", "11"),
                        entry("성남시", "12"),
                        entry("수원시", "13"),
                        entry("시흥시", "14"),
                        entry("안산시", "15"),
                        entry("안성시", "16"),
                        entry("안양시", "17"),
                        entry("양주시", "18"),
                        entry("양평군", "19"),
                        entry("여주시", "20"),
                        entry("연천군", "21"),
                        entry("오산시", "22"),
                        entry("용인시", "23"),
                        entry("의왕시", "24"),
                        entry("의정부시", "25"),
                        entry("이천시", "26"),
                        entry("파주시", "27"),
                        entry("평택시", "28"),
                        entry("포천시", "29"),
                        entry("하남시", "30"),
                        entry("화성시", "31")
                )),
                entry("32", Map.ofEntries(
                        entry("강릉시", "1"),
                        entry("고성군", "2"),
                        entry("동해시", "3"),
                        entry("삼척시", "4"),
                        entry("속초시", "5"),
                        entry("양구군", "6"),
                        entry("양양군", "7"),
                        entry("영월군", "8"),
                        entry("원주시", "9"),
                        entry("인제군", "10"),
                        entry("정선군", "11"),
                        entry("철원군", "12"),
                        entry("춘천시", "13"),
                        entry("태백시", "14"),
                        entry("평창군", "15"),
                        entry("홍천군", "16"),
                        entry("화천군", "17"),
                        entry("횡성군", "18")
                )),
                entry("33", Map.ofEntries(
                        entry("괴산군", "1"),
                        entry("단양군", "2"),
                        entry("보은군", "3"),
                        entry("영동군", "4"),
                        entry("옥천군", "5"),
                        entry("음성군", "6"),
                        entry("제천시", "7"),
                        entry("진천군", "8"),
                        entry("청원군", "9"),
                        entry("청주시", "10"),
                        entry("충주시", "11"),
                        entry("증평군", "12")
                )),
                entry("34", Map.ofEntries(
                        entry("공주시", "1"),
                        entry("금산군", "2"),
                        entry("논산시", "3"),
                        entry("당진시", "4"),
                        entry("보령시", "5"),
                        entry("부여군", "6"),
                        entry("서산시", "7"),
                        entry("서천군", "8"),
                        entry("아산시", "9"),
                        entry("예산군", "11"),
                        entry("천안시", "12"),
                        entry("청양군", "13"),
                        entry("태안군", "14"),
                        entry("홍성군", "15"),
                        entry("계룡시", "16")
                )),
                entry("35", Map.ofEntries(
                        entry("경산시", "1"),
                        entry("경주시", "2"),
                        entry("고령군", "3"),
                        entry("구미시", "4"),
                        entry("김천시", "6"),
                        entry("문경시", "7"),
                        entry("봉화군", "8"),
                        entry("상주시", "9"),
                        entry("성주군", "10"),
                        entry("안동시", "11"),
                        entry("영덕군", "12"),
                        entry("영양군", "13"),
                        entry("영주시", "14"),
                        entry("영천시", "15"),
                        entry("예천군", "16"),
                        entry("울릉군", "17"),
                        entry("울진군", "18"),
                        entry("의성군", "19"),
                        entry("청도군", "20"),
                        entry("청송군", "21"),
                        entry("칠곡군", "22"),
                        entry("포항시", "23")
                )),
                entry("36", Map.ofEntries(
                        entry("거제시", "1"),
                        entry("거창군", "2"),
                        entry("고성군", "3"),
                        entry("김해시", "4"),
                        entry("남해군", "5"),
                        entry("밀양시", "7"),
                        entry("사천시", "8"),
                        entry("산청군", "9"),
                        entry("양산시", "10"),
                        entry("의령군", "12"),
                        entry("진주시", "13"),
                        entry("창녕군", "15"),
                        entry("창원시", "16"),
                        entry("통영시", "17"),
                        entry("하동군", "18"),
                        entry("함안군", "19"),
                        entry("함양군", "20"),
                        entry("합천군", "21")
                )),
                entry("37", Map.ofEntries(
                        entry("고창군", "1"),
                        entry("군산시", "2"),
                        entry("김제시", "3"),
                        entry("남원시", "4"),
                        entry("무주군", "5"),
                        entry("부안군", "6"),
                        entry("순창군", "7"),
                        entry("완주군", "8"),
                        entry("익산시", "9"),
                        entry("임실군", "10"),
                        entry("장수군", "11"),
                        entry("전주시", "12"),
                        entry("정읍시", "13"),
                        entry("진안군", "14")
                )),
                entry("38", Map.ofEntries(
                        entry("강진군", "1"),
                        entry("고흥군", "2"),
                        entry("곡성군", "3"),
                        entry("광양시", "4"),
                        entry("구례군", "5"),
                        entry("나주시", "6"),
                        entry("담양군", "7"),
                        entry("목포시", "8"),
                        entry("무안군", "9"),
                        entry("보성군", "10"),
                        entry("순천시", "11"),
                        entry("신안군", "12"),
                        entry("여수시", "13"),
                        entry("영광군", "16"),
                        entry("영암군", "17"),
                        entry("완도군", "18"),
                        entry("장성군", "19"),
                        entry("장흥군", "20"),
                        entry("진도군", "21"),
                        entry("함평군", "22"),
                        entry("해남군", "23"),
                        entry("화순군", "24")
                )), entry("39", Map.ofEntries(
                        entry("남제주군", "1"),
                        entry("북제주군", "2"),
                        entry("서귀포시", "3"),
                        entry("제주시", "4")
                ))
        );
    }

}
