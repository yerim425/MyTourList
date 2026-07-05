package com.yrlee.tp08tourapi;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import static com.yrlee.tp08tourapi.BuildConfig.API_KEY;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.yrlee.tp08tourapi.adapter.ImagePagerAdapter;
import com.yrlee.tp08tourapi.data.TourDetailItem;
import com.yrlee.tp08tourapi.data.TourItem;
import com.yrlee.tp08tourapi.util.Constants;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class TourDetailActivity extends AppCompatActivity {

    private String contentId;
    private String contentTypeId;

    // ui
    private TextView tvFailLoadData;
    private TextView tvTitle;

    // progress bar
    private ProgressBar progressBar;
    private boolean isLoading = false;

    // viewpager
    private ImagePagerAdapter imagePagerAdapter;

    // 상세정보 객체
    private TourDetailItem tourItem;

    String BASE_URL = "https://apis.data.go.kr/B551011/KorService2/";
    String BASE_TYPE = "?MobileOS=AND&MobileApp=TripPocket&_type=xml";

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

        // ui 초기화
        tvFailLoadData = findViewById(R.id.tv_fail_load_data);
        progressBar = findViewById(R.id.progressbar);
        tvTitle = findViewById(R.id.tvTitle);

        // viewpager
        imagePagerAdapter = new ImagePagerAdapter(null);

        contentId = getIntent().getStringExtra("contentId");
        contentTypeId = getIntent().getStringExtra("contentTypeId");
        if(contentId == null || contentId.isEmpty() || contentTypeId == null || contentTypeId.isEmpty()){
            tvFailLoadData.setVisibility(VISIBLE);
        }else{
            tourItem = new TourDetailItem(contentId, contentTypeId);
            Log.d("here", "contentId: " + contentId + " contentTypeId: " + contentTypeId);

            loadContentData();
        }


    }

    public void loadContentData() {
//        int currentRequestId = ++requestId;
        setLoading(true);
        Thread t1 = new Thread() {
            @Override
            public void run() {

                String requestKeyWord = "detailCommon2";
                String address = BASE_URL + requestKeyWord + BASE_TYPE +
                        "&serviceKey=" + API_KEY +
                        "&contentId=" + tourItem.contentId;

                Log.d("here", address);
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
//                    String[] itemSize = new String[3]; // 0:numOfRows, 1:pageNo, 2:totalCount
                    ArrayList<String> imageList = new ArrayList<>();
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                break;
                            case XmlPullParser.START_TAG:
                                String tagName = xpp.getName();
                                if (tagName.equals("title")) {
                                    xpp.next();
                                    tourItem.title = xpp.getText();
                                    Log.d("url", "title: " + xpp.getText());
                                } else if (tagName.equals("createdtime")) {
                                    xpp.next();
                                    tourItem.createTime = xpp.getText();
                                } else if (tagName.equals("modifiedtime")) {
                                    xpp.next();
                                    tourItem.modifiedItem = xpp.getText();
                                } else if (tagName.equals("tel")) {
                                    xpp.next();
                                    tourItem.tel = xpp.getText();
                                } else if (tagName.equals("homepage")) {
                                    xpp.next();
                                    tourItem.homePage = xpp.getText();
                                } else if (tagName.equals("firstimage")) {
                                    xpp.next();
                                    tourItem.firstImage = xpp.getText();
                                    imageList.add(xpp.getText());
                                } else if (tagName.equals("firstimage2")) {
                                    xpp.next();
                                    tourItem.firstImage2 = xpp.getText();
                                    imageList.add(xpp.getText());
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
                                } else if (tagName.equals("overview")) {
                                    xpp.next();
                                    tourItem.firstImage = xpp.getText();
                                }
                                break;
                            case XmlPullParser.END_TAG:
//                                String tagNames = xpp.getName();
//                                if (tagNames.equals("item")) tourItems.add(item);
                        }
                        eventType = xpp.next();
                    }
                    runOnUiThread(() -> {
//                        if(currentRequestId != requestId) return;
                        if (isFinishing() || isDestroyed()) return;

                        // ui 반영
                        tvTitle.setText(tourItem.title);
                        ViewPager2 viewPager = findViewById(R.id.viewPager);
                        imagePagerAdapter.setImageList(imageList);
                        viewPager.setAdapter(imagePagerAdapter);

                    });
                }
//                catch (IOException | XmlPullParserException e) {
//                    throw new RuntimeException(e);
//                }
                catch (Exception e) {
                    tvFailLoadData.setVisibility(VISIBLE);
                    Log.e("TourDetailActivity error", Log.getStackTraceString(e));
                }
                finally {
                    setLoading(false);
                }
            }
        };
        t1.start();
    }

    private void setLoading(Boolean loading){
        isLoading = loading;
        progressBar.setVisibility(isLoading ? VISIBLE : INVISIBLE);

    }
}