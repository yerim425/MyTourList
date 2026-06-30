package com.yrlee.tp08tourapi;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.yrlee.tp08tourapi.util.Constants;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


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

    // sigunguCode 관련 TextInputLayout
    TextInputLayout inputLayoutAreaDetail;
    AutoCompleteTextView actvAreaDetail;
    ArrayAdapter areaDetailAdapter;

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

    // 로딩 프로그래스 바
    ProgressBar progressBar;

    // 페이징
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int totalCount = 0;

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
//        loadCategoryCodeData();

        // 프로그래스 바
        progressBar = findViewById(R.id.progressbar);

        // 카테고리 별 탭 추가
        tabLayout = findViewById(R.id.tab_layout);
        for(String type: Constants.CONTENT_TYPE_MAP.keySet()){
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(type);
            tabLayout.addTab(tab);
        }

        // 초기 화면 - 서울-전체 관광지 리스트
        inputLayoutArea = findViewById(R.id.input_layout_area);
        actvArea = (AutoCompleteTextView) inputLayoutArea.getEditText(); // AutoCompleteTextView extends EditText
        areaAdapter = new ArrayAdapter(this, R.layout.recycler_area_item, new ArrayList<String>());
        actvArea.setAdapter(areaAdapter);
        inputLayoutArea.requestFocus();
        actvArea.setText("서울", false);
        actvArea.dismissDropDown();
        areaAdapter.addAll(Constants.SIDO_MAP.keySet());

        // 상세 지역 리스트 가져오기
        inputLayoutAreaDetail = findViewById(R.id.input_layout_detail);
        actvAreaDetail = (AutoCompleteTextView) inputLayoutAreaDetail.getEditText();
        areaDetailAdapter = new ArrayAdapter(this, R.layout.recycler_area_item, new ArrayList<String>());
        actvAreaDetail.setAdapter(areaDetailAdapter);
        updateAreaDetail("서울"); //// 선택한 지역의 상세 지역 리스트 가져오기

        actvArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = actvArea.getText().toString();
                //loadAreaData(areaMap.get(name));
                updateAreaDetail(name); //// 선택한 지역의 상세 지역 리스트 가져오기
            }
        });

        // 검색 버튼 리스너
        findViewById(R.id.btn_search).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, touristFragment = new TouristFragment()).commit();
            tabLayout.getTabAt(0).select();
            currentPage = 1;
            loadContentData(getString(R.string.tourist));
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

                // 선택된 탭으로 loadContentData
                currentPage = 1;
                loadContentData(tabName);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 화면에 보여진 데이터 개수
        tvItemCount = findViewById(R.id.tv_item_cnt);

        // 처음에 한 번 ContentData 호출
        loadContentData(getString(R.string.tourist));
    }

    // 상세 지역 리스트 업데이트
    private void updateAreaDetail(String areaName){
        areaDetailAdapter.clear();
        areaDetailAdapter.add(getString(R.string.total));
        areaDetailAdapter.addAll(Constants.SIGUNGU_MAP.get(Constants.SIDO_MAP.get(areaName)).keySet());
        areaDetailAdapter.notifyDataSetChanged();
        actvAreaDetail.setText(getString(R.string.total), false);
        inputLayoutAreaDetail.setEnabled(true);

    }

    public void loadContentData(String contentTypeName) {
        setLoading(true);
        Thread t = new Thread() {
            @Override
            public void run() {

                ArrayList<TourItem> tourItems = new ArrayList<>();
                String areaCode = Constants.SIDO_MAP.get(actvArea.getText().toString());
                String singunguCode = Objects.requireNonNull(Constants.SIGUNGU_MAP.get(areaCode)).get(actvAreaDetail.getText().toString());

                String requestKeyWord = "areaBasedList2";
                String address = BASE_URL + requestKeyWord + BASE_TYPE +
                        "&arrange=O&serviceKey=" + API_KEY +
                        "&contentTypeId=" + Constants.CONTENT_TYPE_MAP.get(contentTypeName) +
                        "&areaCode=" + areaCode +
                        "&pageNo="+currentPage +
                        "&numOfRows=20";

                if(singunguCode != null)
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
                                } else if (tagName.equals("mapx")) {
                                    xpp.next();
                                    item.mapx = xpp.getText();
                                } else if (tagName.equals("mapy")) {
                                    xpp.next();
                                    item.mapy = xpp.getText();
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
                           if(size >= Integer.parseInt(itemSize[2])) isLastPage = true;
                           tvItemCount.setText(size + "/" + itemSize[2]);

                           Log.d("size", Arrays.toString(itemSize));

                       }catch(NumberFormatException | NullPointerException e){
                           tvItemCount.setVisibility(INVISIBLE);
                           isLastPage = true;
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
                }finally {
                    setLoading(false);
                }

            }
        };
        t.start(); // 자동으로 run() 영역 안에 코드가 수행됨(별도 Thread 직원객체에 의해)
    }

    private void setLoading(Boolean loading){
        isLoading = loading;
        progressBar.setVisibility(isLoading ? VISIBLE : INVISIBLE);

    }
    public void loadNextTouristPage(){

        if(isLoading) return;
        if(isLastPage) return;
        currentPage++;

        loadContentData(getString(R.string.tourist));
    }

    public void openKakaoMap(String title, String latitude, String longitude) {

        if(latitude == null || longitude == null){
            Toast.makeText(this, "위경도가 옳바르지 않습니다.", Toast.LENGTH_SHORT).show();
        }

        String url = "kakaomap://look?p=" + latitude + "," + longitude;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("net.daum.android.map");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "카카오맵이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

}