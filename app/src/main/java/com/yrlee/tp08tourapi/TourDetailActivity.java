package com.yrlee.tp08tourapi;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import static com.yrlee.tp08tourapi.BuildConfig.API_KEY;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.yrlee.tp08tourapi.adapter.ImagePagerAdapter;
import com.yrlee.tp08tourapi.adapter.TourDetailAdapter;
import com.yrlee.tp08tourapi.data.TourDetailIntroItem;
import com.yrlee.tp08tourapi.data.TourDetailItem;
import com.yrlee.tp08tourapi.data.TourDetailRecommendItem;
import com.yrlee.tp08tourapi.room.BookmarkManager;
import com.yrlee.tp08tourapi.room.BookmarkRepository;
import com.yrlee.tp08tourapi.room.BookmarkTour;
import com.yrlee.tp08tourapi.util.Constants;
import com.yrlee.tp08tourapi.util.DateUtil;
import com.yrlee.tp08tourapi.util.XmlParserUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TourDetailActivity extends AppCompatActivity {

    private String contentId;
    private String contentTypeId;

    // ui
    private TextView tvFailLoadData;
    private TextView tvTitle;
    private TextView tvTags;
    private LinearLayout layoutAddress;
    private TextView tvAddress;
    private TextView tvSubAddress;
    private TextView tvOverview;
    private RecyclerView recyclerview;
    private ShimmerFrameLayout shimmerFrameLayout;
    private CheckBox cbBookmark;

    // progress bar
    private ProgressBar progressBar;
    private boolean isLoading = false;

    // adapter
    private ImagePagerAdapter imagePagerAdapter; // viewpager
    private TourDetailAdapter tourDetailAdapter;

    // url 관련
    String BASE_URL = "https://apis.data.go.kr/B551011/KorService2/";
    String BASE_TYPE = "?&serviceKey=" + API_KEY + "&MobileOS=AND&MobileApp=TripPocket&_type=xml";
    private String detailAddr; // 상세정보 조회
    private String detailRequestKeyword = "detailCommon2";
    private String introAddr; // 소개정보 조회
    private String introRequestKeyword = "detailIntro2";
    private String repeatAddr; // 반복정보 조회 - 여행코스
    private String repeatRequestKeyword = "detailInfo2";

    // 상세정보 객체
    private TourDetailItem tourItem;

    // 스레드를 여러개 생성하는 것을 방지
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    // bookmark repository
    private BookmarkRepository bookmarkRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tour_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // bookmark repository
        bookmarkRepository = new BookmarkRepository(this);

        // ui 초기화
        tvFailLoadData = findViewById(R.id.tv_fail_load_data);
        progressBar = findViewById(R.id.progressbar);
        tvTitle = findViewById(R.id.tvTitle);
        tvTags = findViewById(R.id.tv_tags);
        layoutAddress = findViewById(R.id.layout_address);
        tvAddress = findViewById(R.id.tv_address);
        tvSubAddress = findViewById(R.id.tv_sub_address);
        recyclerview = findViewById(R.id.recyclerview);
        tvOverview = findViewById(R.id.tv_overview);
        shimmerFrameLayout = findViewById(R.id.shimmer_layout);
        cbBookmark = findViewById(R.id.cb_bookmark);

        // contentId 확인 후 tour 데이터 요청
        contentId = getIntent().getStringExtra("contentId");

        if (contentId == null || contentId.isEmpty()) {
            tvFailLoadData.setVisibility(VISIBLE);
        } else {
            tourItem = new TourDetailItem(contentId);
            Log.d("here", "contentId: " + contentId);

            loadCommonData();
        }

        // 뒤로가기
        findViewById(R.id.iv_back).setOnClickListener(v -> {
            finish();
        });

        // 카카오맵 가기
        layoutAddress.setOnClickListener(v -> {
            openKakaoMap(tourItem.mapy, tourItem.mapx);
        });

        // 북마크
        // 체크 상태 설정
        cbBookmark.setChecked(BookmarkManager.getInstance().isBookmarked(contentId));

    }

    // 1. 공통 정보 조회
    public void loadCommonData() {
        setLoading(true);

        executor.execute(() -> {
            // 기본 상세 정보 조회 url
            detailAddr = BASE_URL + detailRequestKeyword + BASE_TYPE + "&contentId=" + tourItem.contentId;


            Log.d("here", detailAddr);
            try {
                XmlPullParser xpp = XmlParserUtil.getParser(detailAddr);
                detailParse(xpp);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // ui 업데이트
            runOnUiThread(this::updateCommonUI);

            // 2. 상세 소개 정보 조회
            loadIntroData();

            // 3. 여행코스 리스트 조회
            if ("25".equals(tourItem.contentTypeId)) {
                loadRepeatData();
            }
        });
    }


    // 1.1 공통 정보 xml 문서 파싱
    private void detailParse(XmlPullParser xpp) throws XmlPullParserException, IOException {
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    String tagName = xpp.getName();
                    if (tagName.equals("contenttypeid")) {
                        xpp.next();
                        tourItem.contentTypeId = xpp.getText();
                    } else if (tagName.equals("title")) {
                        xpp.next();
                        tourItem.title = xpp.getText();
                        Log.d("url", "title: " + xpp.getText());
                    } else if (tagName.equals("homepage")) {
                        xpp.next();
                        tourItem.homePage = xpp.getText();
                    } else if (tagName.equals("firstimage")) {
                        xpp.next();
                        tourItem.firstImage = xpp.getText();
                    } else if (tagName.equals("addr1")) {
                        xpp.next();
                        tourItem.addr1 = xpp.getText();
                    } else if (tagName.equals("addr2")) {
                        xpp.next();
                        tourItem.addr2 = xpp.getText();
                    } else if (tagName.equals("mapx")) {
                        xpp.next();
                        tourItem.mapx = xpp.getText();
                    } else if (tagName.equals("mapy")) {
                        xpp.next();
                        tourItem.mapy = xpp.getText();
                    } else if (tagName.equals("lclsSystm1")) {
                        xpp.next();
                        tourItem.lclsSystm1 = xpp.getText();
                    } else if (tagName.equals("lclsSystm2")) {
                        xpp.next();
                        tourItem.lclsSystm2 = xpp.getText();
                    } else if (tagName.equals("lclsSystm3")) {
                        xpp.next();
                        tourItem.lclsSystm3 = xpp.getText();
                    } else if (tagName.equals("overview")) {
                        xpp.next();
                        tourItem.overview = xpp.getText().replace(". ", ".\n\n");
                    }
                    break;
                case XmlPullParser.END_TAG:
            }
            eventType = xpp.next();
        }
    }

    // 1.2 공통 정보 조회 - ui 업데이트
    private void updateCommonUI() {
        if (isFinishing() || isDestroyed()) return;
        // 제목
        tvTitle.setText(tourItem.title);

        // 메인 이미지
        // viewpager
        ArrayList<String> imageList = new ArrayList<>();
        imageList.add(tourItem.firstImage);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        imagePagerAdapter = new ImagePagerAdapter(imageList);
        viewPager.setAdapter(imagePagerAdapter);

        // 장소 소개
        tvOverview.setText(tourItem.overview.replaceAll("<br\\s*/?>", "\n"));

        // layout visible, tags
        StringBuilder sb = new StringBuilder();
        String cat1 = Constants.CATEGORY1_MAP.get(tourItem.lclsSystm1);
        if (cat1 != null) sb.append(cat1);

        if ("25".equals(tourItem.contentTypeId)) { // 여행코스
            // address
            layoutAddress.setVisibility(GONE);

            // tags
            String cat2 = Constants.CATEGORY2_MAP.get(tourItem.lclsSystm1).get(tourItem.lclsSystm2);
            if (cat2 != null) sb.append(" > ").append(cat2);

        } else {
            // address
            layoutAddress.setVisibility(VISIBLE);
            tvAddress.setText((tourItem.addr1));
            if (tourItem.addr2 != null) {
                tvSubAddress.setVisibility(VISIBLE);
                tvSubAddress.setText(tourItem.addr2);
            }

            // tags
            if (!"AC".equals(tourItem.lclsSystm1)
                    && !"FD".equals(tourItem.lclsSystm1)
                    && !"SH".equals(tourItem.lclsSystm1)) {

                Map<String, String> cat2Map = Constants.CATEGORY2_MAP.get(tourItem.lclsSystm1);
                String cat2 = cat2Map != null ? cat2Map.get(tourItem.lclsSystm2) : null;

                Map<String, String> cat3Map = Constants.CATEGORY3_MAP.get(tourItem.lclsSystm2);
                String cat3 = cat3Map != null ? cat3Map.get(tourItem.lclsSystm3) : null;

                if (cat2 != null) {
                    sb.append(" > ").append(cat2);
                }

                if (cat3 != null && !cat3.equals(cat2)) {
                    sb.append(" > ").append(cat3);
                }
            }
        }
        tvTags.setText(sb.toString());

        // 리스너 등록 - 찜 등록/해지 요청
        cbBookmark.setVisibility(VISIBLE);
        cbBookmark.setOnClickListener(v->{
            if(cbBookmark.isChecked()){
                BookmarkTour bt = new BookmarkTour();
                bt.contentId = contentId;
                bt.contentTypeId = tourItem.contentTypeId;
                bt.title = tourItem.title;
                bt.address = tourItem.addr1;
                bt.firstImage = tourItem.firstImage;
                bt.mapx = tourItem.mapx;
                bt.mapy = tourItem.mapy;
                bt.lclsSystm1 = tourItem.lclsSystm1;
                bookmarkRepository.insert(bt);
            }else{
                bookmarkRepository.delete(contentId);
            }
        });

        setLoading(false);
    }

    // 2. 소개 정보 조회
    public void loadIntroData() {
        runOnUiThread(() -> {
            shimmerFrameLayout.setVisibility(VISIBLE);
            shimmerFrameLayout.startShimmer();
            recyclerview.setVisibility(GONE);
        });
        executor.execute(() -> {

            Log.d("here", "contentTypeId: " + tourItem.contentTypeId);
            introAddr = BASE_URL + introRequestKeyword + BASE_TYPE
                    + "&contentId=" + tourItem.contentId
                    + "&contentTypeId=" + tourItem.contentTypeId;
            Log.d("here", "intro url: " + introAddr);

            try {
                XmlPullParser xpp = XmlParserUtil.getParser(introAddr);
                introParse(xpp);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // ui
            runOnUiThread(this::updateIntroUI);
        });

    }

    // 2.1 소개 상세 정보 xml 파싱
    private void introParse(XmlPullParser xpp) throws XmlPullParserException, IOException {

        int eventType = xpp.getEventType();
        if ("25".equals(tourItem.contentTypeId)) {// 여행코스 정보
            // 코스 총 소요시간 데이터 가져오기
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String tagName = xpp.getName();
                        if (tagName.equals("taketime")) {
                            xpp.next();
                            tourItem.takeTime = xpp.getText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                }
                eventType = xpp.next();
            }

        } else { // 관광지, 문화시설, 레포츠, 축제 정보
            // 관광지, 문화시설, 레포츠 정보 : 이용 요금, 이용시설, 주차시설, 주차요금, 문의및안내, 쉬는날
            // 축제 정보 : 주최자정보, 주최자연락처, 행사시작일, 행사종료일, 공연시간, 행사장소, 이용요금
            List<TourDetailIntroItem> introList = new ArrayList<>();
            // 행사시작일, 종료일
            String eventStartDate = "", eventEndDate = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String tagName = xpp.getName();

                        if (tagName.contains("usefee")) {
                            addIfNotEmpty(introList, xpp, "usefee");

                        } else if (tagName.contains("usetime")) {
                            addIfNotEmpty(introList, xpp, "usetime");

                        } else if (tagName.contains("parkingfee")) {
                            addIfNotEmpty(introList, xpp, "parkingfee");

                        } else if (tagName.equals("parking")) {
                            addIfNotEmpty(introList, xpp, "parking");

                        } else if (tagName.contains("infocenter")) {
                            addIfNotEmpty(introList, xpp, "infocenter");

                        } else if (tagName.contains("restdate")) {
                            addIfNotEmpty(introList, xpp, "restdate");

                        } else if (tagName.equals("sponsor1")) {
                            addIfNotEmpty(introList, xpp, "sponsor1");

                        } else if (tagName.equals("eventplace")) {
                            addIfNotEmpty(introList, xpp, "eventplace");

                        } else if (tagName.equals("playtime")) {
                            addIfNotEmpty(introList, xpp, "playtime");

                        } else if (tagName.equals("eventstartdate")) {
                            xpp.next();
                            eventStartDate = xpp.getText();

                        } else if (tagName.equals("eventenddate")) {
                            xpp.next();
                            eventEndDate = xpp.getText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                }
                eventType = xpp.next();
            }
            if(eventStartDate != null && !eventStartDate.isEmpty()){
                introList.add(
                        new TourDetailIntroItem("eventdate", DateUtil.getEventDate(eventStartDate, eventEndDate))
                );
            }
            tourItem.introList = introList;
        }
    }

    // 2.2 소개 정보 조회 - ui 업데이트
    private void updateIntroUI() {
        if ("25".equals(tourItem.contentTypeId)) { // 여행코스
            if (tourItem.takeTime != null) {
                String tags = tvTags.getText() + "\n소요시간 " + tourItem.takeTime;
                tvTags.setText(tags);
            }
        } else {

            tourDetailAdapter = new TourDetailAdapter(this, tourItem);
            recyclerview.setAdapter(tourDetailAdapter);

            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(GONE);
            recyclerview.setVisibility(VISIBLE);
        }
    }

    // 3. 반복 정보 조회 - 여행코스
    public void loadRepeatData() {
        runOnUiThread(() -> {
            shimmerFrameLayout.setVisibility(VISIBLE);
            shimmerFrameLayout.startShimmer();
            recyclerview.setVisibility(GONE);
        });
        executor.execute(() -> {

            Log.d("here", "contentTypeId: " + tourItem.contentTypeId);
            repeatAddr = BASE_URL + repeatRequestKeyword + BASE_TYPE
                    + "&contentId=" + tourItem.contentId
                    + "&contentTypeId=" + tourItem.contentTypeId;
            Log.d("here", "repeat url: " + repeatAddr);

            try {
                XmlPullParser xpp = XmlParserUtil.getParser(repeatAddr);
                repeatParse(xpp);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // ui
            runOnUiThread(this::updateRepeatUI);
        });
    }

    // 3.1 반복 정보 조회 parseing
    private void repeatParse(XmlPullParser xpp) throws XmlPullParserException, IOException {
        int eventType = xpp.getEventType();
        // contentId, 장소이름, 소개, image url
        List<TourDetailRecommendItem> recommendList = new ArrayList<>();
        TourDetailRecommendItem item = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    String tagName = xpp.getName();
                    if(tagName.contains("item")){
                        xpp.next();
                        item = new TourDetailRecommendItem();
                    }else if (tagName.contains("subcontentid")) {
                        xpp.next();
                        Log.d("here", "id: " + xpp.getText());
                        item.id = xpp.getText();
                    } else if (tagName.contains("subname")) {
                        xpp.next();
                        item.title = xpp.getText();
                    } else if (tagName.contains("subdetailoverview")) {
                        xpp.next();
                        item.overview = xpp.getText();
                    } else if (tagName.equals("subdetailimg")) {
                        xpp.next();
                        item.image = xpp.getText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    String tagNames = xpp.getName();
                    if (tagNames.equals("item")) recommendList.add(item);
            }
            eventType = xpp.next();
        }
        tourItem.recommendList = recommendList;
    }

    // 3.2 반복 정보 조회 - ui 업데이트
    private void updateRepeatUI() {
        tourDetailAdapter = new TourDetailAdapter(this, tourItem);
        recyclerview.setAdapter(tourDetailAdapter);

        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(GONE);
        recyclerview.setVisibility(VISIBLE);
    }

    private void setLoading(Boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(isLoading ? VISIBLE : INVISIBLE);

    }

    // 데이터가 있는지 확인 후 introList에 추가
    private void addIfNotEmpty(List<TourDetailIntroItem> list,
                               XmlPullParser xpp,
                               String title) throws IOException, XmlPullParserException {

        xpp.next();
        String text = xpp.getText();

        if (text != null && !text.trim().isEmpty()) {
            list.add(new TourDetailIntroItem(title, text));
        }
    }


    // 카카오맵 가기
    public void openKakaoMap(String latitude, String longitude) {

        if (latitude == null || longitude == null) {
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