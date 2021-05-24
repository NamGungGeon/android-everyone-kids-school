package com.example.hci_project;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import org.jetbrains.annotations.NotNull;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


import java.util.ArrayList;
import java.util.List;

import android.util.Log;


import java.io.IOException;
import java.io.InputStream;

// 엑셀 파일 불러오기 라이브러리
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.HttpMethod;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView schoolSearchTv;
    private ViewPager2 viewPager;
    private List<Fragment> viewPagerFragmentList = new ArrayList<>();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    static public NaverMap naverMap; // 네이버 맵 객체 - 다른 곳에서도 접근 가능하게 하려고 일단 전역변수화함.

    //데이터베이스
    static Sheet sheet0; // 어린이집 기본 현황
    static Sheet sheet1; // 유치원 보험가입 현황
    static Sheet sheet2; // 유치원 공제회 가입 현황
    static Sheet sheet3; // 유치원 안전점검 및 안전점검 실시 현황
    static Sheet sheet4; // 유치원 환경 위생 관리 현황
    static Sheet sheet5; // 유치원 근속 연수 현황
    static Sheet sheet6; // 유치원 통학 차량 현황
    static Sheet sheet7; // 유치원 수업 일수 현황
    static Sheet sheet8; // 유치원 직위 자격별 교직원 현황
    static Sheet sheet9; // 유치원 교실 면적 현황
    static Sheet sheet10; // 유치원 기본 현황
    static Sheet sheet11; // 유치원 건물 현황

    static final int PERMISSIONS_REQUEST = 0x0000001;



    // Getter
    public static Sheet getSheet0() {
        return sheet0;
    }

    public static Sheet getSheet1() {
        return sheet1;
    }

    public static Sheet getSheet2() {
        return sheet2;
    }

    public static Sheet getSheet3() {
        return sheet3;
    }

    public static Sheet getSheet4() {
        return sheet4;
    }

    public static Sheet getSheet5() {
        return sheet5;
    }

    public static Sheet getSheet6() {
        return sheet6;
    }

    public static Sheet getSheet7() {
        return sheet7;
    }

    public static Sheet getSheet8() {
        return sheet8;
    }

    public static Sheet getSheet9() {
        return sheet9;
    }

    public static Sheet getSheet10() {
        return sheet10;
    }

    public static Sheet getSheet11() {
        return sheet11;
    }


    static MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OnCheckPermission(); // 권한 확인


        System.out.println("hello");
        String add = getAddress_DAUM("서울특별시 광진구 아차산로44길 26");
        System.out.println(add);

        // 네이버 지도 설정
        NaverMapOptions options = new NaverMapOptions() // 초기 화면 위치, 경도 생성자 설정
                .camera(new CameraPosition(new LatLng(37.543344020789625, 127.07557079824849), 14)) // 건국대 기준으로 지도를 연다.
                .mapType(NaverMap.MapType.Basic);

        options.locationButtonEnabled(true).tiltGesturesEnabled(false);

        mapFragment = MapFragment.newInstance(options); // 옵션 설정
        mapFragment.getMapAsync(this);


        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE); // 현재 위치 갱신

        comeonDB(); // DB 가져오기

        schoolSearchTv= findViewById(R.id.search_school_tv);
        viewPager= findViewById(R.id.main_viewPager);



        viewPagerFragmentList.add(mapFragment); // 커스텀 맵 프래그먼트 : 기존 new MapFragment 대체
        viewPagerFragmentList.add(new CompareSchoolFragment());
        viewPagerFragmentList.add(new BookmarkActivity());


        initUI(); // UI 초기화

    }

    // 처음 UI 설정
    private void initUI(){
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @NotNull
            @Override
            public Fragment createFragment(int position) {
                return viewPagerFragmentList.get(position);
            }

            @Override
            public int getItemCount() {
                return viewPagerFragmentList.size();
            }
        });
        viewPager.setOffscreenPageLimit(viewPagerFragmentList.size());
        viewPager.setUserInputEnabled(false);

        schoolSearchTv.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchSchoolActivity.class)));

        BottomNavigationView bottomNavigationView= findViewById(R.id.bottomNavBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            schoolSearchTv.setVisibility(View.GONE);

            switch (item.getTitle().toString()){
                case "주변":
                    schoolSearchTv.setVisibility(View.VISIBLE);
                    viewPager.setCurrentItem(0);
                    break;
                case "비교":
                    viewPager.setCurrentItem(1);
                    break;
                case "즐겨찾기":
                    viewPager.setCurrentItem(2);
                    break;
            }
            return true;
        });


    }

    private long backButtonClickTime= 0;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(System.currentTimeMillis()- backButtonClickTime>= 1000){
            Toast.makeText(this, "앱을 종료하시려면 뒤로가기 버튼을 한번 더 누르세요", Toast.LENGTH_SHORT).show();
            backButtonClickTime= System.currentTimeMillis();
        }else{
            finish();
        }
    }

    public void comeonDB() { // DB 불러오기
        try {
            InputStream is = getBaseContext().getResources().getAssets().open("kindergardenDB.xls"); // 유치원 현황
            InputStream is2 = getBaseContext().getResources().getAssets().open("childhomeDB.xls"); // 어린이집 현황
            Workbook wb = Workbook.getWorkbook(is);
            Workbook wb2 = Workbook.getWorkbook(is2);

            if(wb != null) {
                sheet1 = wb.getSheet(0);   // 0번 째  '유치원 보험가입 현황' 시트 불러오기
                if(sheet1 != null) {
                    int colTotal = sheet1.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet1.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet1.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }

                sheet2 = wb.getSheet(1);   // 1번 째 시트 '유치원 공제회 가입 현황' 불러오기
                if(sheet2 != null) {
                    int colTotal = sheet2.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet2.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet2.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }

                sheet3 = wb.getSheet(2);   // 2번 째 시트 '유치원 안전점검 및 안전점검 실시 현황' 불러오기
                if(sheet3 != null) {
                    int colTotal = sheet3.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet3.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet3.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }

                sheet4 = wb.getSheet(3);   // 3번 째 시트 '유치원 환경 위생 관리 현황' 불러오기
                if(sheet4 != null) {
                    int colTotal = sheet4.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet4.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet4.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }


                sheet5 = wb.getSheet(4);   // 4번 째 시트 '유치원 근속 연수 현황'불러오기
                if(sheet5 != null) {
                    int colTotal = sheet5.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet5.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet5.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }


                sheet6 = wb.getSheet(5);   // 5번 째 시트 '유치원 통학 차량 현황' 불러오기
                if(sheet6 != null) {
                    int colTotal = sheet6.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet6.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet6.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }


                sheet7 = wb.getSheet(6);   // 6번 째 시트 '유치원 직위 자격별 교직원 현황' 불러오기
                if(sheet7 != null) {
                    int colTotal = sheet7.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet7.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet7.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }


                sheet8 = wb.getSheet(7);   // 7번 째 시트 '유치원 직위 자격별 교직원 현황' 불러오기
                if(sheet8 != null) {
                    int colTotal = sheet8.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet8.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet8.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }


                sheet9 = wb.getSheet(8);   // 8번 째 시트 '유치원 교실 면적 현황' 불러오기
                if(sheet9 != null) {
                    int colTotal = sheet9.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet9.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet9.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }


                sheet10 = wb.getSheet(9);   // 9번 째 시트 '유치원 기본 현황' 불러오기
                if(sheet10 != null) {
                    int colTotal = sheet10.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet10.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet10.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }


                sheet11 = wb.getSheet(10);   // 10번 째 시트 '유치원 건물 현황' 불러오기
                if(sheet11 != null) {
                    int colTotal = sheet11.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet11.getColumn(colTotal-1).length;

                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet11.getCell(col, row).getContents();
                            sb.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb.toString()); // 가져오는지 로그 확인
                    }
                }

            }

            if(wb2 != null) {
                Sheet sheet0 = wb2.getSheet(0);   // 어린이집 시트 불러오기
                if(sheet0 != null) {
                    int colTotal = sheet0.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet0.getColumn(colTotal-1).length;

                    StringBuilder sb2;
                    for(int row=rowIndexStart;row<rowTotal;row++) {
                        sb2 = new StringBuilder();
                        for(int col=0;col<colTotal;col++) {
                            String contents = sheet0.getCell(col, row).getContents();
                            sb2.append("col"+col+" : "+contents+" , ");
                        }
                        Log.i("test", sb2.toString()); // 가져오는지 로그 확인
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }

    //위치 권한 설정
    public void OnCheckPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED

                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                Toast.makeText(this, "앱 실행을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(this,

                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},

                        PERMISSIONS_REQUEST);

            } else {

                ActivityCompat.requestPermissions(this,

                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},

                        PERMISSIONS_REQUEST);

            }
        }
    }


    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        switch (requestCode) {

            case PERMISSIONS_REQUEST :

                if (grantResults.length > 0

                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(this, "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_LONG).show();

                }

                break;

        }

        // 네이버 지도 소스 권한
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);


    }

    @UiThread
    @Override
    public void onMapReady(@NonNull @NotNull NaverMap naverMap) { // 맵 설정
        this.naverMap = naverMap;

        naverMap.setLocationSource(locationSource); // 자기 위치 설정 
        naverMap.setMapType(NaverMap.MapType.Basic); // 기본형 지도
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true); // 빌딩 그룹 생성

        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow); // 위치 추적 모드 실행


        //네이버 지도 추가 UI 설정
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true); // 현 위치 버튼 활성화

        // 마커 표시하기
        Marker marker = new Marker();
        marker.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker.setMap(naverMap);

        // 클릭 리스너
        naverMap.setOnMapClickListener((point, coord) ->
                Toast.makeText(this, coord.latitude + ", " + coord.longitude,
                        Toast.LENGTH_SHORT).show());

        // 롱 클릭 리스너
        naverMap.setOnMapLongClickListener((point, coord) ->
                Toast.makeText(this, coord.latitude + ", " + coord.longitude,
                        Toast.LENGTH_SHORT).show());

        // 심벌 클릭 리스너
        naverMap.setOnSymbolClickListener(symbol -> {
            if ("서울특별시청".equals(symbol.getCaption())) {
                Toast.makeText(this, "서울시청 클릭", Toast.LENGTH_SHORT).show();
                // 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
                return true;
            }
            // 이벤트 전파, OnMapClick 이벤트가 발생함
            return false;
        });
    }

    public void NearMarker() { // 화면에 있는 유치원의 마커를 표시함
        int count = 0 ;
        Sheet sheet = getSheet8(); // 유치원 기본 현황 시트

        String address = null;

        address = sheet.getCell(7, 2).getContents(); // 유치원 주소는 7번째 열에 있음. row는 줄.

        // 네이버 API 주소 검색 요청
        String response = getAddress_DAUM(address);

        // 마커는 최대 10개
        Marker marker1 = new Marker();
        marker1.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker1.setMap(naverMap);

        Marker marker2 = new Marker();
        marker2.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker2.setMap(naverMap);

        Marker marker3 = new Marker();
        marker3.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker3.setMap(naverMap);

        Marker marker4 = new Marker();
        marker4.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker4.setMap(naverMap);

        Marker marker5 = new Marker();
        marker5.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker5.setMap(naverMap);

        Marker marker6 = new Marker();
        marker6.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker6.setMap(naverMap);

        Marker marker7 = new Marker();
        marker7.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker7.setMap(naverMap);

        Marker marker8 = new Marker();
        marker8.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker8.setMap(naverMap);

        Marker marker9 = new Marker();
        marker9.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker9.setMap(naverMap);

        Marker marker10 = new Marker();
        marker10.setPosition(new LatLng(37.541680773674464, 127.07943250328056));
        marker10.setMap(naverMap);
        
    }

    // 주소 검색
    public String getAddress_DAUM(String data) {

        final String Client_ID = "o82z0vth6u"; // 인증용 클라이언트 아이디
        final String Client_Secret = "LjN7euyVUTJlH2haz5RmIMmwwTVq77I6ODNipNrE"; // 클라이언트 인증키

        final String API_URL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=";

        try {
            String addr = URLEncoder.encode(data, "UTF-8");  // data : 주소입력
            String apiURL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + addr; //json
            //String apiURL = "https://openapi.naver.com/v1/map/geocode.xml?query=" + addr; // xml

            // http 프로토콜로 api 호출
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", Client_ID);
            con.setRequestProperty("X-NCP-APIGW-API-KEY", Client_Secret);

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 응답코드가 200이면 정상
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());
            return response.toString(); // 응답 반환
        } catch (Exception e) {
            System.out.println(e);
            return null; // 오류면 주소값 없이 반환
        }
    }


    // 거리 계산 코드

    /**
     * 두 지점간의 거리 계산
     *
     * @param lat1 지점 1 위도
     * @param lon1 지점 1 경도
     * @param lat2 지점 2 위도
     * @param lon2 지점 2 경도
     * @param unit 거리 표출단위
     * @return
     */

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if(unit == "meter"){
            dist = dist * 1609.344;
        }

        return (dist);
    }


    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }



}

