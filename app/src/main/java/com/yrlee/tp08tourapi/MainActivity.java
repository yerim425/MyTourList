package com.yrlee.tp08tourapi;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.yrlee.tp08tourapi.data.TourItem;
import com.yrlee.tp08tourapi.fragment.TourFragment;
import com.yrlee.tp08tourapi.room.AppDatabase;
import com.yrlee.tp08tourapi.room.BookmarkCallback;
import com.yrlee.tp08tourapi.room.BookmarkDao;
import com.yrlee.tp08tourapi.util.Constants;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
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
    TourFragment tourFragment = null;

    // 로딩 프로그래스 바
    ProgressBar progressBar;

    // 플로팅 버튼
    FloatingActionButton fab;

    // no data text
    TextView tvNoData;

    // 페이징
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int totalCount = 0;

    // 찜 관리를 위한 RoomDB
    private AppDatabase db;
    private BookmarkDao bookmarkDao;

    // 새로운 api 요청을 위한 식별 id
    private int requestId = 0;


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

        // 검색 버튼 리스너
        findViewById(R.id.btn_search).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, tourFragment = new TourFragment()).commit();
            currentPage = 1;
            tvItemCount.setText("0/0");

            String query = getString(R.string.tourist);
            TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
            if(tab != null && tab.getText() != null){
                query = tab.getText().toString();
            }
            loadContentData(query);
        });

        // 탭 레이아웃 설정
        getSupportFragmentManager().beginTransaction().add(R.id.container, tourFragment = new TourFragment()).commit();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 해당하는 콘텐츠의 데이터들 요청
                String tabName = tab.getText().toString();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, tourFragment = new TourFragment()).commit();
                // 선택된 탭으로 loadContentData
                currentPage = 1;
                tvItemCount.setText("0/0");

                loadContentData(tabName);
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

    public void loadContentData(String contentTypeName) {
        int currentRequestId = ++requestId;
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

                Log.d("url", address);
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
//                                    Log.d("mainactivity", item.contentId);
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
                        if(currentRequestId != requestId) return;
                        if (isFinishing() || isDestroyed()) return;
                       try{ // 화면에 아이템 개수 보이기
                           int currentSize = Integer.parseInt(itemSize[0])*Integer.parseInt(itemSize[1]);
                           int totalSize = Integer.parseInt(itemSize[2]);
                           if(currentSize >= totalSize) isLastPage = true;
                           tvItemCount.setText(currentSize + "/" + totalSize);
                           tvItemCount.setVisibility(VISIBLE);

                           if(totalSize==0) tvNoData.setVisibility(VISIBLE);
                           else tvNoData.setVisibility(INVISIBLE);

                       }catch(NumberFormatException | NullPointerException e){
                           tvItemCount.setVisibility(INVISIBLE);
                           tvNoData.setVisibility(VISIBLE);
                           isLastPage = true;
                       }
                        tourFragment.addItems(tourItems);
                    });
                }
//                catch (IOException | XmlPullParserException e) {
//                    throw new RuntimeException(e);
//                }
                catch (Exception e) {
                    Log.e("ROOM", Log.getStackTraceString(e));
                }
                finally {
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

    // 스크롤 시 다음 장소 리스트 요청하기
    public void loadNextTouristPage(){

        if(isLoading) return;
        if(isLastPage) return;
        currentPage++;

        String query = getString(R.string.tourist);
        TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
        if(tab != null && tab.getText() != null){
            query = tab.getText().toString();
        }
        loadContentData(query);

    }

    // 찜 등록 여부 확인
    public void isBookmarked(String contentId, BookmarkCallback callback){
        new Thread(()->{
//            Log.d("ROOM", "contentId = " + contentId);
           boolean result = bookmarkDao.isBookmarked(contentId);
           // 콜백
            new Handler(Looper.getMainLooper()).post(()->{
                callback.onResult(result);
            });
        }).start();
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