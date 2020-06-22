package com.example.navermap1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class Frag_data extends Fragment {

    private View view;
    private TextView mDustTextView;
    private TextView mLocationTextView;
    private Button mDustButton;

    private String admin;
    private boolean bSet_itemCode;
    private boolean bSet_city;

    public ArrayList<Pair<String, String>> address;
    private String pollution_degree = "";
    private String tag_name = "";
    private int index;
    private DownloadWebpageTask task;


    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull final ViewGroup container,
                             @NonNull Bundle saveInstanceState) {
        view = (View) inflater.inflate(R.layout.activity_data, container, false);
        mDustTextView = view.findViewById(R.id.dust);
        mLocationTextView = view.findViewById(R.id.my_gps);
        mDustButton = view.findViewById(R.id.btn_dust);

        final String api = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnMesureLIst?itemCode=PM10&dataGubun=HOUR&searchCondition=WEEK&pageNo=1&numOfRows=10&ServiceKey=r574IhbxmWgIdO3abztcyzdxq5GLpHZd3rKeqHF61o0Teni14aEpCShmDg0zBLmh9sASwYO7WHDJFdOmdxKCAg%3D%3D";

        task = new DownloadWebpageTask();

        setAddPair();

        mDustButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.execute(api);
            }
        });

        return view;
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        public long now;
        public Date mDate;
        HttpURLConnection conn = null;

        @Override
        protected String doInBackground(String... urls) {
            try {
                String txt = (String) downloadUrl((String) urls[0]);
                return txt;
            } catch (Exception e) {
                return "다운로드실패";
            }
        }

        protected void onPostExecute(String result) {
            bSet_itemCode = false;
            bSet_city = false;

            now = System.currentTimeMillis();
            mDate = new Date(now);

            String itemCode = "";
            pollution_degree = "";
            tag_name = "";

            int cnt = 0;
            index = 0;

            mDustTextView.setText("");
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(result));

                // 현재 이벤트 유형 반환(START_DOCUMENT, START_TAG, TEXT, END_TAG, END_DOCUMENT
                int eventType = xpp.getEventType();

                // 이벤트 유형이 문서 마지막이 될 때까지 반복
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {

                    } else if (eventType == XmlPullParser.START_TAG) {
                        tag_name = xpp.getName();
                        if (bSet_itemCode == false && tag_name.equals("itemCode"))
                            bSet_itemCode = true;
                        if (itemCode.equals("PM10") &&
                                (tag_name.equals("seoul") ||
                                        tag_name.equals("busan") ||
                                        tag_name.equals("daegu") ||
                                        tag_name.equals("incheon") ||
                                        tag_name.equals("gwangju") ||
                                        tag_name.equals("daejeon") ||
                                        tag_name.equals("ulsan") ||
                                        tag_name.equals("gyeonggi") ||
                                        tag_name.equals("gangwon") ||
                                        tag_name.equals("chungbuk") ||
                                        tag_name.equals("chungnam") ||
                                        tag_name.equals("jeonbuk") ||
                                        tag_name.equals("jeonnam") ||
                                        tag_name.equals("gyeongbuk") ||
                                        tag_name.equals("gyeongnam") ||
                                        tag_name.equals("jeju") ||
                                        tag_name.equals("sejong")))

                            bSet_city = true;

                    } else if (eventType == XmlPullParser.TEXT) {
                        if (bSet_itemCode) {
                            itemCode = xpp.getText();

                            if (itemCode.equals("PM10")) {
                                cnt++;
                                bSet_itemCode = false;
                            }
                            if (cnt > 1)
                                break;
                        }
                        if (bSet_city) {
                            pollution_degree = xpp.getText();

                            //시도별 이름이 똑같은 부분 출력
                            if (address.get(index).first.equals(admin) &&
                                    address.get(index).second.equals(tag_name)) {
                                mDustTextView.setText("" + admin + ", " + pollution_degree);

                                setIndex(index);
                                setTag_name(tag_name);
                                setPollution_degree(pollution_degree);

                                task.cancel(true);
                                break;
                            }
                            index++;
                            bSet_city = false;
                        }

                    } else if (eventType == XmlPullParser.END_TAG) {

                    }

                    eventType = xpp.next();
                }
            } catch (Exception e) {
            }

        }

        private String downloadUrl(String api) throws IOException {
            try {
                URL url = new URL(api);
                conn = (HttpURLConnection) url.openConnection();
                BufferedInputStream buf = new BufferedInputStream(conn.getInputStream());
                BufferedReader bufreader = new BufferedReader(new InputStreamReader(buf, "utf-8"));

                String line = null;
                String page = "";
                while ((line = bufreader.readLine()) != null) {
                    page += line;
                }
                return page;
            } finally {
                conn.disconnect();
            }
        }
    }

    public void setmLocationTextView(String mLocationTextView) {
        this.mLocationTextView.setText(mLocationTextView);
    }

    public void setAdminText(String adminText) {
        this.admin = adminText;
    }

    //xml데이터와 주소데이터 묶음
    public void setAddPair() {
        address = new ArrayList<Pair<String, String>>();
        address.add(0, new Pair<String, String>("서울특별시", "seoul"));
        address.add(1, new Pair<String, String>("부산광역시", "busan"));
        address.add(2, new Pair<String, String>("대구광역시", "daegu"));
        address.add(3, new Pair<String, String>("인천광역시", "incheon"));
        address.add(4, new Pair<String, String>("광주광역시", "gwangju"));
        address.add(5, new Pair<String, String>("대전광역시", "daejeon"));
        address.add(6, new Pair<String, String>("울산광역시", "ulsan"));
        address.add(7, new Pair<String, String>("경기도", "gyeonggi"));
        address.add(8, new Pair<String, String>("강원도", "gangwon"));
        address.add(9, new Pair<String, String>("충청북도", "chungbuk"));
        address.add(10, new Pair<String, String>("충청남도", "chungnam"));
        address.add(11, new Pair<String, String>("전라북도", "jeonbuk"));
        address.add(12, new Pair<String, String>("전라남도", "jeonnam"));
        address.add(13, new Pair<String, String>("경상북도", "gyeongbuk"));
        address.add(14, new Pair<String, String>("경상남도", "gyeongnam"));
        address.add(15, new Pair<String, String>("제주특별자치도", "jeju"));
        address.add(16, new Pair<String, String>("세종특별자치시", "sejong"));
    }

    public ArrayList<Pair<String, String>> getAddress() {
        return address;
    }

    public String getPollution_degree() {
        return pollution_degree;
    }

    public void setPollution_degree(String pollution_degree) {
        this.pollution_degree = pollution_degree;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}

