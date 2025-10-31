package com.yrlee.tp08tourapi;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.yrlee.tp08tourapi.data.CodeItem;
import com.yrlee.tp08tourapi.data.TourItem;
import com.yrlee.tp08tourapi.fragment.AccommodationFragment;
import com.yrlee.tp08tourapi.fragment.CultureFragment;
import com.yrlee.tp08tourapi.fragment.FestivalFragment;
import com.yrlee.tp08tourapi.fragment.LeportsFragment;
import com.yrlee.tp08tourapi.fragment.RestaurantFragment;
import com.yrlee.tp08tourapi.fragment.ShoppingFragment;
import com.yrlee.tp08tourapi.fragment.TouristFragment;
import com.yrlee.tp08tourapi.fragment.TravelCourseFragment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    //String BASE_URL = "https://apis.data.go.kr/B551011/KorService1/";
    String BASE_URL = "https://apis.data.go.kr/B551011/KorService2/";
    String BASE_TYPE = "?MobileOS=AND&MobileApp=TripPocket&_type=xml";
//    String API_KEY = "oaEqMy4KQboCdSKksNqjpIiAUvGmfifILLU46GwXiQ3IhM9tVam1FEIlOQxM4stcJYI6LickRAyXqw0mKfPx%2FA%3D%3D";
    String API_KEY = BuildConfig.API_KEY;


    // areaCode 관련 TextInputLayout
    TextInputLayout inputLayoutArea;
    AutoCompleteTextView actvArea;
    ArrayAdapter areaAdapter;
    Map<String, String> areaMap = new HashMap<>(); // <지역이름, 지역코드>

    // sigunguCode 관련 TextInputLayout
    TextInputLayout inputLayoutAreaDetail;
    AutoCompleteTextView actvAreaDetail;
    ArrayAdapter areaDetailAdapter;
    Map<String, String> areaDetailMap = new HashMap<>();

    // 콘텐츠 타입-----------------------------
    Map<String, String> contentTypeMap = new HashMap<>();
    TabLayout tabLayout;

    TextView tvItemCount; // 화면에 보여진 아이템 개수

    public Map<String, String> categoryMap = new HashMap<>(); // <카테고리 코드, 카테고리 이름>

    // 콘텐츠 별 프레그먼트
    TouristFragment touristFragment = null;
    LeportsFragment leportsFragment = null;
    CultureFragment cultureFragment = null;
    FestivalFragment festivalFragment = null;
    AccommodationFragment accommodationFragment = null;
    RestaurantFragment restaurantFragment = null;
    ShoppingFragment shoppingFragment = null;
    TravelCourseFragment travelCourseFragment = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 아이템 카테고리 목록 요청
        loadCategoryCodeData();

        // 콘텐츠 타입 데이터 셋팅 // (12:관광지, 14:문화시설, 15:축제공연행사, 25:여행코스, 28:레포츠, 32:숙박, 38:쇼핑, 39:음식점) ID
        contentTypeMap.put(getString(R.string.tourist), "12");
        contentTypeMap.put(getString(R.string.cultural_facilities), "14");
        contentTypeMap.put(getString(R.string.festival), "15");
        contentTypeMap.put(getString(R.string.travel_course), "25");
        contentTypeMap.put(getString(R.string.leisure_sports), "28");
        contentTypeMap.put(getString(R.string.accommodation), "32");
        contentTypeMap.put(getString(R.string.shopping), "38");
        contentTypeMap.put(getString(R.string.restaurant), "39");
        tabLayout = findViewById(R.id.tab_layout);
        for(String type: contentTypeMap.keySet()){
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(type);
            tabLayout.addTab(tab);
        }

        // 초기 화면 - 서울-전체 관광지 리스트
        areaMap.put(getString(R.string.seoul), "1");
        areaDetailMap.put(getString(R.string.total), "");
        // 지역 코드 데이터 요청
        inputLayoutArea = findViewById(R.id.input_layout_area);
        actvArea = (AutoCompleteTextView) inputLayoutArea.getEditText(); // AutoCompleteTextView extends EditText
        areaAdapter = new ArrayAdapter(this, R.layout.recycler_area_item, new ArrayList<String>());
        actvArea.setAdapter(areaAdapter);
        inputLayoutArea.requestFocus();
        actvArea.dismissDropDown();
        loadAreaData(""); // "서울", "부산", "제주", ...
        actvArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = actvArea.getText().toString();
                loadAreaData(areaMap.get(name)); // 선택한 지역의 상세 지역 이름 데이터 요청
            }
        });

        // 상세 지역 코드 데이터 요청
        inputLayoutAreaDetail = findViewById(R.id.input_layout_detail);
        actvAreaDetail = (AutoCompleteTextView) inputLayoutAreaDetail.getEditText();
        areaDetailAdapter = new ArrayAdapter(this, R.layout.recycler_area_item, new ArrayList<String>());
        actvAreaDetail.setAdapter(areaDetailAdapter);
        loadAreaData("1"); // 서울 전체 -> "강남구", "도봉구", "성북구", ...

        // 검색 버튼 리스너
        findViewById(R.id.btn_search).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, touristFragment = new TouristFragment()).commit();
            tabLayout.getTabAt(0).select();
        });

        // 탭 레이아웃 설정
        getSupportFragmentManager().beginTransaction().add(R.id.container, touristFragment = new TouristFragment()).commit();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 해당하는 콘텐츠의 데이터들 요청
                String tabName = tab.getText().toString();
                if(tabName.equals(getString(R.string.tourist))) getSupportFragmentManager().beginTransaction().replace(R.id.container, touristFragment = new TouristFragment()).commit();
                else if(tabName.equals(getString(R.string.cultural_facilities))) getSupportFragmentManager().beginTransaction().replace(R.id.container, cultureFragment = new CultureFragment()).commit();
                else if(tabName.equals(getString(R.string.leisure_sports))) getSupportFragmentManager().beginTransaction().replace(R.id.container, leportsFragment = new LeportsFragment()).commit();
                else if(tabName.equals(getString(R.string.festival))) getSupportFragmentManager().beginTransaction().replace(R.id.container, festivalFragment = new FestivalFragment()).commit();
                else if(tabName.equals(getString(R.string.accommodation))) getSupportFragmentManager().beginTransaction().replace(R.id.container, accommodationFragment = new AccommodationFragment()).commit();
                else if(tabName.equals(getString(R.string.restaurant))) getSupportFragmentManager().beginTransaction().replace(R.id.container, restaurantFragment = new RestaurantFragment()).commit();
                else if(tabName.equals(getString(R.string.shopping))) getSupportFragmentManager().beginTransaction().replace(R.id.container, shoppingFragment = new ShoppingFragment()).commit();
                else if(tabName.equals(getString(R.string.travel_course))) getSupportFragmentManager().beginTransaction().replace(R.id.container, travelCourseFragment = new TravelCourseFragment()).commit();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 화면에 보여진 데이터 개수
        tvItemCount = findViewById(R.id.tv_item_cnt);
    }

    // 지역 코드 조회
    public void loadAreaData(String areaCode) {
        Thread t = new Thread() {
            @Override
            public void run() {
                String requestKeyWord = "areaCode2";
                String address = BASE_URL + requestKeyWord + BASE_TYPE +"&numOfRows=30&pageNo=1" + "&serviceKey=" + API_KEY;
                if(!areaCode.isEmpty()) {  // "1"
                    address += "&areaCode="+areaCode;
                    areaDetailMap.clear();
                    runOnUiThread(()->{
                        inputLayoutAreaDetail.setEnabled(false);
                    });

                }else{
                    areaMap.clear();
                }
                try {
                    URL url = new URL(address);
                    InputStream is = url.openStream();
                    InputStreamReader isr = new InputStreamReader(is);

                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser xpp = factory.newPullParser();

                    xpp.setInput(isr);
                    int size = 0;
                    int eventType = xpp.getEventType();
                    CodeItem item = null;
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                break;
                            case XmlPullParser.START_TAG:
                                String tagName = xpp.getName();
                                if (tagName.equals("item")) {
                                    item = new CodeItem();
                                } else if (tagName.equals("code")) {
                                    xpp.next();
                                    item.code = xpp.getText();
                                } else if (tagName.equals("name")) {
                                    xpp.next();
                                    item.name = xpp.getText();
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                String tagNames = xpp.getName();
                                if (tagNames.equals("item")) {
                                    size++;
                                    Log.d("area name", "size: "+size+" name: "+item.name + " code: "+item.code);
                                    if(areaCode.isEmpty()) {
                                        areaMap.put(item.name, item.code);
                                    } else {
                                        areaDetailMap.put(item.name, item.code);
                                    }
                                }
                        }
                        eventType = xpp.next();
                    }
                    runOnUiThread(() -> {
                        for(String i: areaDetailMap.keySet()){
                            Log.d("area item", i);
                        }
                        if(areaCode.isEmpty()) {
                            areaAdapter.clear();
                            areaAdapter.addAll(new ArrayList<>(areaMap.keySet()));
                            areaAdapter.notifyDataSetChanged();
                        }
                        else {
                            areaDetailAdapter.clear();
                            areaDetailAdapter.add(getString(R.string.total));
                            areaDetailAdapter.addAll(new ArrayList<>(areaDetailMap.keySet()));
                            areaDetailAdapter.notifyDataSetChanged();
                            areaDetailMap.put(getString(R.string.total), "");
                            actvAreaDetail.setText(getString(R.string.total), false);
                            inputLayoutAreaDetail.setEnabled(true);
                        }
                    });
                } catch (IOException | XmlPullParserException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        t.start();
    }



    public void loadContentData(String contentTypeName) {
        Thread t = new Thread() {
            @Override
            public void run() {

                ArrayList<TourItem> tourItems = new ArrayList<>();
                String areaCode = areaMap.get(actvArea.getText().toString());
                String singunguCode = areaDetailMap.get(actvAreaDetail.getText().toString());
                if(areaCode==null||singunguCode==null){
                    areaCode = "1"; singunguCode = ""; // 서울-전체로 검색함
                }

                String requestKeyWord = "areaBasedList2";
                String address = BASE_URL + requestKeyWord + BASE_TYPE + "&arrange=O&serviceKey=" + API_KEY
                        + "&contentTypeId=" + contentTypeMap.get(contentTypeName) + "&areaCode=" + areaCode;

                if(!singunguCode.isEmpty())
                    address += "&sigunguCode="+singunguCode;

                Log.d("content item", address);
                try {
                    URL url = new URL(address);
                    InputStream is = url.openStream(); // 바이트 스트림
                    InputStreamReader isr = new InputStreamReader(is); // 문자 스트림

                    // Reader가 읽어들인 문자들을 xml 파일로 보고 분석해주는 분석가 객체 준비
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser xpp = factory.newPullParser();
                    // 분석가에게 xml 데이터를 읽어오는 무지개 로드를 알려주기
                    xpp.setInput(isr);
                    // xpp를 이용하여 xml 문서를 분석 시작!!
                    TourItem item = null;
                    String[] itemSize = new String[3]; // 0:numOfRows, 1:pageNo, 2:totalCount
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                runOnUiThread(()-> {
                                    //Snackbar.make(recyclerView, getString(R.string.loading_data), Snackbar.LENGTH_SHORT).show();
                                });
                                break;
                            case XmlPullParser.START_TAG:
                                String tagName = xpp.getName();
                                if (tagName.equals("item")) {
                                    item = new TourItem();
                                } else if (tagName.equals("addr1")) {
                                    xpp.next();
                                    item.addr1 = xpp.getText();
                                } else if (tagName.equals("addr2")) {
                                    xpp.next();
                                    item.addr2 = xpp.getText();
                                } else if (tagName.equals("contentid")) {
                                    xpp.next();
                                    item.contentId = xpp.getText();
                                } else if (tagName.equals("contenttypeid")) {
                                    xpp.next();
                                    item.contentTypeId = xpp.getText();
                                } else if (tagName.equals("title")) {
                                    xpp.next();
                                    item.title = xpp.getText();
                                } else if (tagName.equals("firstimage")) {
                                    xpp.next();
                                    item.firstImage = xpp.getText();
                                } else if (tagName.equals("firstimage2")) {
                                    xpp.next();
                                    item.firstImage2 = xpp.getText();
                                } else if (tagName.equals("tel")) {
                                    xpp.next();
                                    item.tel = xpp.getText();
                                } else if (tagName.equals("cat1")) {
                                    xpp.next();
                                    item.cat1 = xpp.getText();
                                } else if(tagName.equals("numOfRows")){
                                    xpp.next();
                                    itemSize[0] = xpp.getText();
                                } else if(tagName.equals("pageNo")){
                                    xpp.next();
                                    itemSize[1] = xpp.getText();
                                } else if(tagName.equals("totalCount")){
                                    xpp.next();
                                    itemSize[2] = xpp.getText();
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                String tagNames = xpp.getName();
                                if (tagNames.equals("item")) tourItems.add(item);

                        }
                        eventType = xpp.next();
                    }
                    runOnUiThread(() -> {
                       try{ // 화면에 아이템 개수 보이기
                           tvItemCount.setVisibility(VISIBLE);
                           int size = Integer.parseInt(itemSize[0])*Integer.parseInt(itemSize[1]);
                           tvItemCount.setText(size + "/" + itemSize[2]);
                       }catch(NumberFormatException | NullPointerException e){
                           tvItemCount.setVisibility(INVISIBLE);
                       }
                       if(contentTypeName.equals(getString(R.string.tourist))) touristFragment.addItems(tourItems);
                       else if(contentTypeName.equals(getString(R.string.leisure_sports))) leportsFragment.addItems(tourItems);
                       else if(contentTypeName.equals(getString(R.string.cultural_facilities))) cultureFragment.addItems(tourItems);
                       else if(contentTypeName.equals(getString(R.string.festival))) festivalFragment.addItems(tourItems);
                       else if(contentTypeName.equals(getString(R.string.accommodation))) accommodationFragment.addItems(tourItems);
                       else if(contentTypeName.equals(getString(R.string.restaurant))) restaurantFragment.addItems(tourItems);
                       else if(contentTypeName.equals(getString(R.string.shopping))) shoppingFragment.addItems(tourItems);
                       else if(contentTypeName.equals(getString(R.string.travel_course))) travelCourseFragment.addItems(tourItems);
                    });
                } catch (IOException | XmlPullParserException e) {
                    throw new RuntimeException(e);
                }

            }
        };
        t.start(); // 자동으로 run() 영역 안에 코드가 수행됨(별도 Thread 직원객체에 의해)
    }
    public void loadCategoryCodeData() {
        Thread t = new Thread() {
            @Override
            public void run() {
                String requestKeyWord = "categoryCode2";
                String address = BASE_URL + requestKeyWord + BASE_TYPE + "&serviceKey=" + API_KEY;

                try {
                    URL url = new URL(address);
                    InputStream is = url.openStream();
                    InputStreamReader isr = new InputStreamReader(is);

                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser xpp = factory.newPullParser();

                    xpp.setInput(isr);
                    CodeItem item = null;
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                break;
                            case XmlPullParser.START_TAG:
                                String tagName = xpp.getName();
                                if (tagName.equals("item")) {
                                    item = new CodeItem();
                                } else if (tagName.equals("code")) {
                                    xpp.next();
                                    item.code = xpp.getText();
                                } else if (tagName.equals("name")) {
                                    xpp.next();
                                    item.name = xpp.getText();
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                String tagNames = xpp.getName();
                                if (tagNames.equals("item")) {
                                    categoryMap.put(item.code, item.name); // <"A01","자연">
                                }
                        }
                        eventType = xpp.next();
                    }
                    runOnUiThread(() -> {
                        Log.d("category item", "item size: "+categoryMap.size());
                    });
                } catch (IOException | XmlPullParserException e) {
                    throw new RuntimeException(e);
                }

            }
        };
        t.start();
    }

}