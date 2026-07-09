package com.yrlee.tp08tourapi;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.yrlee.tp08tourapi.data.TourItem;
import com.yrlee.tp08tourapi.fragment.TourFragment;
import com.yrlee.tp08tourapi.room.AppDatabase;
import com.yrlee.tp08tourapi.room.BookmarkDao;
import com.yrlee.tp08tourapi.util.Constants;
import com.yrlee.tp08tourapi.util.XmlParserUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MainActivity extends AppCompatActivity {

    String BASE_URL = "https://apis.data.go.kr/B551011/KorService2/";
    String BASE_TYPE = "?MobileOS=AND&MobileApp=TripPocket&_type=xml&numOfRows=20&serviceKey=" + BuildConfig.API_KEY;


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
    TourFragment tourFragment = null; // 12:관광지
    TourFragment cultureFragment = null; // 14:문화시설
    TourFragment festivalFragment = null; // 15:공연행사축제
    TourFragment leportsFragment = null; // 24:레포츠
    TourFragment recommendFragment = null; // 25:여행코스
    TourFragment currentFragment = null;

    // 로딩 프로그래스 바
    ProgressBar progressBar;

    // 플로팅 버튼
    FloatingActionButton fab;

    // no data text
    TextView tvNoData;

    // 페이징
//    private int currentPage = 1;
    private boolean isLoading = false;
//    private boolean isLastPage = false;
    //private int totalCount = 0;

    // 찜 관리를 위한 RoomDB
    private AppDatabase db;
    private BookmarkDao bookmarkDao;

    // 새로운 api 요청을 위한 식별 id
//    private int requestId = 0;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> currentTask;



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

        // 찜 관리 데이터베이스
        db = AppDatabase.getInstance(this);
        bookmarkDao = db.getBookmarkDao();

        // 프로그래스 바
        progressBar = findViewById(R.id.progressbar);

        // 플로팅 버튼
        fab = findViewById(R.id.fab);

        // no data ui
        tvNoData = findViewById(R.id.tv_no_data);

        // 화면에 보여진 데이터 개수
        tvItemCount = findViewById(R.id.tv_item_cnt);

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
        areaAdapter.addAll(Constants.SIDO_MAP.keySet());
        inputLayoutArea.requestFocus();
        actvArea.setText("서울", false);
        actvArea.dismissDropDown();

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

        // fragment 초기화
        tourFragment = new TourFragment();
        currentFragment = tourFragment;
        getSupportFragmentManager().beginTransaction().add(R.id.container, currentFragment).commit();

        // 검색 버튼 리스너
        findViewById(R.id.btn_search).setOnClickListener(v -> {
            setLoading(false);

            String tabName = getString(R.string.tourist);
            TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
//            TourFragment fragment = null;
            if(tab != null && tab.getText() != null){
                tabName = tab.getText().toString();
//                if(tabName.equals(getString(R.string.tourist))) fragment = tourFragment;
//                else if(tabName.equals(getString(R.string.cultural_facilities))) fragment = cultureFragment;
//                else if(tabName.equals(getString(R.string.festival))) fragment = festivalFragment;
//                else if(tabName.equals(getString(R.string.leisure_sports))) fragment = leportsFragment;
//                else if(tabName.equals(getString(R.string.travel_course))) fragment = recommendFragment;
            }
//            if(fragment == null) {
//                fragment = new TourFragment();
//            }
            tourFragment = cultureFragment = festivalFragment = leportsFragment = recommendFragment = null;
            currentFragment = new TourFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
            tvItemCount.setText("0/0");
            loadContentData(tabName);

        });

        // 탭 레이아웃 설정

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setLoading(false);
                // 해당하는 콘텐츠의 데이터들 요청
                String tabName = tab.getText().toString();
                TourFragment fragment;
                if(tabName.equals(getString(R.string.tourist))) fragment = tourFragment;
                else if(tabName.equals(getString(R.string.cultural_facilities))) fragment = cultureFragment;
                else if(tabName.equals(getString(R.string.festival))) fragment = festivalFragment;
                else if(tabName.equals(getString(R.string.leisure_sports))) fragment = leportsFragment;
                else if(tabName.equals(getString(R.string.travel_course))) fragment = recommendFragment;
                else fragment = null;

                if(fragment == null) {
                    currentFragment = new TourFragment();;
                    tvItemCount.setText("0/0");
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
                    loadContentData(tabName);
                }else{
                    currentFragment = fragment;
                    tvItemCount.setText(currentFragment.getCurrentSize() + "/" + currentFragment.getTotalSize());
                    if(currentFragment.getCurrentSize() == 0) tvNoData.setVisibility(VISIBLE);
                    else tvNoData.setVisibility(INVISIBLE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
                }

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });



        // 처음에 한 번 ContentData 호출
        loadContentData(getString(R.string.tourist));

        // 플로팅 버튼 -> 찜 목록 화면
        fab.setOnClickListener(v->{
            startActivity(new Intent(this, BookmarkActivity.class));
        });

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

    private void loadContentData(String contentTypeName) {
        // 현재 페이지 가져오기
//        final int currentRequestId = ++requestId;
        // 이전 작업 취소
        if (currentTask != null && !currentTask.isDone()) {
//            currentTask.cancel(true);
        }
        tvNoData.setVisibility(INVISIBLE);
        setLoading(true);
        String contentTypeId = Constants.CONTENT_TYPE_MAP.get(contentTypeName);
        Log.d("url", "contentTypeId: "+contentTypeId);
        int currentPage = currentFragment.getCurrentPage();
        TourFragment targetFragment = currentFragment;
        currentTask = executor.submit(() -> {
            String areaCode = Constants.SIDO_MAP.get(actvArea.getText().toString());
            String singunguCode = Objects.requireNonNull(Constants.SIGUNGU_MAP.get(areaCode)).get(actvAreaDetail.getText().toString());
            String address;
            if("15".equals(contentTypeId)){
                String today = LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                address = BASE_URL + "searchFestival2" + BASE_TYPE +
                        "&lDongRegnCd=" + areaCode +
                        "&pageNo="+currentPage+
                        "&arrange=C&eventStartDate=" + today;
            }else{
                address = BASE_URL + "areaBasedList2" + BASE_TYPE +
                        "&contentTypeId=" + contentTypeId +
                        "&lDongRegnCd=" + areaCode +
                        "&arrange=O&pageNo="+currentPage;
            }
            if(singunguCode != null)
                address += "&lDongSignguCd="+singunguCode;


            Log.d("url", address);

            // api 요청
            try {
                XmlPullParser xpp = XmlParserUtil.getParser(address);
//                tourListParse(xpp, currentRequestId, targetFragment, targetTabName);
                tourListParse(xpp, targetFragment, contentTypeId);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }


        });
    }

    // 스크롤 시 다음 장소 리스트 요청하기
    public void loadNextTouristPage(){
        if(isLoading || currentFragment.getIsLastPage()) return;
        currentFragment.increasePage();

        String query = getString(R.string.tourist);
        TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
        if(tab != null && tab.getText() != null){
            query = tab.getText().toString();
        }
        loadContentData(query);
    }

    // tour list xml 파싱
    private void tourListParse(XmlPullParser xpp, TourFragment targetFragment, String contentTypeId) throws XmlPullParserException, IOException{
        ArrayList<TourItem> tourItems = new ArrayList<>();
        TourItem item = null;
        String[] itemSize = new String[3]; // 0:numOfRows, 1:pageNo, 2:totalCount
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    String tagName = xpp.getName();
                    if (tagName.equals("item")) {
                        item = new TourItem();
                    } else if (tagName.equals("addr1")) {
                        xpp.next();
                        item.addr1 = xpp.getText();
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
                    } else if (tagName.equals("lclsSystm1")) {
                        xpp.next();
                        item.lclsSystm1 = xpp.getText();
                    } else if (tagName.equals("mapx")) {
                        xpp.next();
                        item.mapx = xpp.getText();
                    } else if (tagName.equals("mapy")) {
                        xpp.next();
                        item.mapy = xpp.getText();
                    } else if (tagName.equals("eventstartdate")) {
                        xpp.next();
                        item.eventStartDate = xpp.getText();
                    } else if (tagName.equals("eventenddate")) {
                        xpp.next();
                        item.eventEndDate = xpp.getText();
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
                    if (Thread.currentThread().isInterrupted()) return; // 취소된 task인지 확인
            }
            eventType = xpp.next();
        }
        runOnUiThread(()->{
//            if(currentRequestId != requestId) return;
            updateUI(targetFragment, contentTypeId, tourItems, itemSize);
        });
    }

    private void updateUI(TourFragment targetFragment, String contentTypeId, ArrayList<TourItem> tourItems, String[] itemSize){

        if (isFinishing() || isDestroyed()) return;

        try{ // 화면에 아이템 개수 보이기
            int currentSize = Integer.parseInt(itemSize[0]);
            int currentPage = Integer.parseInt(itemSize[1]);
            int totalSize = Integer.parseInt(itemSize[2]);
            targetFragment.setItemSize(currentSize, currentPage, totalSize);
//            if(currentSize >= totalSize) currentFragment.setIsLastPage(true);
            if(targetFragment == currentFragment){
                tvItemCount.setText(currentFragment.getCurrentSize() + "/" + currentFragment.getTotalSize());
                tvItemCount.setVisibility(VISIBLE);

                if(totalSize==0) tvNoData.setVisibility(VISIBLE);
                else tvNoData.setVisibility(INVISIBLE);
            }


        }catch(NumberFormatException | NullPointerException e){
            if(targetFragment == currentFragment){
                tvItemCount.setVisibility(INVISIBLE);
                tvNoData.setVisibility(VISIBLE);
            }
            targetFragment.setIsLastPage(true);
        }
        targetFragment.addItems(tourItems);
        if(contentTypeId.equals("12")) tourFragment = targetFragment;
        else if(contentTypeId.equals("14")) cultureFragment = targetFragment;
        else if(contentTypeId.equals("15")) festivalFragment = targetFragment;
        else if(contentTypeId.equals("28")) leportsFragment = targetFragment;
        else if(contentTypeId.equals("25")) recommendFragment = targetFragment;

        if(targetFragment == currentFragment) setLoading(false);
    }


    private void setLoading(Boolean loading){
        isLoading = loading;
        progressBar.setVisibility(isLoading ? VISIBLE : INVISIBLE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }


}