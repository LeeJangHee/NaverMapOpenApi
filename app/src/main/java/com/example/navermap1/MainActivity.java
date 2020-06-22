package com.example.navermap1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.IDNA;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private NaverMap mMap;
    public LatLng curr_LOC;
    public LatLng prev_LOC;

    LocationManager locationManager;
    LocationListener locationListener;

    public Geocoder geocoder;
    public List<Address> fromLocation = null;
    private double latitude;
    private double longitude;
    private Frag_data frag_data;

    Marker mk;
    boolean bMarker = true;
    private InfoWindow infoWindow1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frag_data = new Frag_data();
        geocoder = new Geocoder(this);


        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("82q1hjq5wl"));

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }

        //지도 플레그먼트 onMapReady
        mapFragment.getMapAsync(this);

        //데이터 플레그먼트
        getSupportFragmentManager().beginTransaction().replace(R.id.data_fragment, frag_data).commit();
    }


    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // 지도 객체를 여러 메소도에서 사용할 수 있도록 글로벌 객체로 할당
        mMap = naverMap;


        locationListener = new LocationListener() {
            // 위치가 변할 때마다 호출
            public void onLocationChanged(Location location) {
                updateMap(location);
            }

            // 위치서비스가 변경될 때
            public void onStatusChanged(String provider, int status, Bundle extras) {
                alertStatus(provider);
            }

            // 사용자에 의해 Provider 가 사용 가능하게 설정될 때
            public void onProviderEnabled(String provider) {
                alertProvider(provider);
            }

            // 사용자에 의해 Provider 가 사용 불가능하게 설정될 때
            public void onProviderDisabled(String provider) {
                checkProvider(provider);
            }
        };

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        String locationProvider;

        locationProvider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(locationProvider, 1, 1, locationListener);

        locationProvider = LocationManager.NETWORK_PROVIDER;
        locationManager.requestLocationUpdates(locationProvider, 1, 1, locationListener);
    }

    public void checkProvider(String provider) {
        Toast.makeText(this, provider + "에 의한 위치서비스가 꺼져 있습니다. 켜주세요...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    public void alertProvider(String provider) {
        Toast.makeText(this, provider + "서비스가 켜졌습니다!", Toast.LENGTH_LONG).show();
    }

    public void alertStatus(String provider) {
        Toast.makeText(this, "위치서비스가 " + provider + "로 변경되었습니다!", Toast.LENGTH_LONG).show();
    }

    public void updateMap(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        mk = new Marker();
        infoWindow1 = new InfoWindow();

        curr_LOC = new LatLng(latitude, longitude);

        // 이전 위치가 없는 경우
        if (prev_LOC == null) {
            CameraUpdate cameraUpdate = CameraUpdate.zoomTo(15);
            CameraUpdate.scrollTo(curr_LOC);
            mMap.moveCamera(cameraUpdate);

            LocationOverlay locationOverlay = mMap.getLocationOverlay();
            locationOverlay.setVisible(true);
            locationOverlay.setPosition(curr_LOC);

            prev_LOC = curr_LOC;

        } else {
            CameraUpdate cameraUpdate1 = CameraUpdate.scrollTo(curr_LOC);
            mMap.moveCamera(cameraUpdate1);

            LocationOverlay locationOverlay = mMap.getLocationOverlay();
            locationOverlay.setVisible(true);
            locationOverlay.setPosition(curr_LOC);

            prev_LOC = curr_LOC;
        }

        try {
            fromLocation = geocoder.getFromLocation(prev_LOC.latitude, prev_LOC.longitude, 10);
            //임의좌표지정 테스트
//            fromLocation = geocoder.getFromLocation(36.4800984,127.2802807, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }

        frag_data.setmLocationTextView(fromLocation.get(0).getAddressLine(0));
        frag_data.setAdminText(fromLocation.get(0).getAdminArea());

        if (frag_data.getAddress().get(frag_data.getIndex()).first.equals(fromLocation.get(0).getAdminArea())
                && bMarker) {
            addInfo();
            bMarker = false;
            Log.d("추가정보", "ok");
        } else if(!(frag_data.getAddress().get(frag_data.getIndex()).first.equals(fromLocation.get(0).getAdminArea()))){
            bMarker = true;
            Log.d("bMarker", "true");
        } else {
            mk.setMap(null);
            infoWindow1.setMap(null);
            Log.d("추가정보", "no");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
    }

    public void addInfo() {

        String city = frag_data.getTag_name();
        final String pollution = frag_data.getPollution_degree();

        mk.setPosition(prev_LOC);
        mk.setMap(mMap);

        mk.setCaptionText(city);
        mk.setCaptionColor(Color.RED);
        mk.setCaptionHaloColor(Color.YELLOW);
        mk.setCaptionTextSize(20);

        infoWindow1.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return pollution;
            }
        });
        infoWindow1.open(mk);


    }

}