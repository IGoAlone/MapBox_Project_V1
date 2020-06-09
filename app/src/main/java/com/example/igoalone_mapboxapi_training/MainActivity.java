package com.example.igoalone_mapboxapi_training;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.HashMap;
import java.util.List;

// 위치
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;

// 마커
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

// 경로계산
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

// 네비게이션 ui
import android.view.View;
import android.widget.Button;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

//서버 통신
import android.os.AsyncTask;

import com.example.igoalone_mapboxapi_training.DAO.Bell;
import com.example.igoalone_mapboxapi_training.DAO.Cctv;
import com.example.igoalone_mapboxapi_training.DAO.Police;
import com.example.igoalone_mapboxapi_training.DAO.Store;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private LocationComponentActivationOptions locationComponentActivationOptions;
    private LocationComponentOptions locationComponentOptions;
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    private Button button;

    //목적지 검색기능
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private CarmenFeature user;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

    private Location currentLocation;
    private double currentLatitude;
    private double currentLongitude;

    // 안전요소 flag
    int flag = 0;
    private boolean cctvFlag = false;
    private boolean bellFlag = false;
    private boolean storeFlag = false;
    private boolean polFlag = false;

    private String currentAddress = null;

    final String api_key = "NCSEKYAJD5OM5SXA";
    final String api_secret = "LU37HTRDDYKX7RYNFFATW5IVLA9K6R1B";
    Friend friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.access_token));

        friend = getIntent().getParcelableExtra("friend");
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        // cctv button
        ImageButton cctvButton = findViewById(R.id.imageButton);
        cctvButton.setOnClickListener(v -> {
            new markerTask().execute("http://172.30.1.27:3000/cctv");
            Toast.makeText(MainActivity.this, "현재위치 \n위도 " + currentLatitude + "\n경도 " + currentLongitude, Toast.LENGTH_LONG).show();
            flag = 0;
        });

        // police button
        ImageButton policeButton = findViewById(R.id.imageButton2);
        policeButton.setOnClickListener(v -> {
            new markerTask().execute("http://172.30.1.27:3000/police");
            Toast.makeText(MainActivity.this, "현재위치 \n위도 " + currentLatitude + "\n경도 " + currentLongitude, Toast.LENGTH_LONG).show();
            flag = 3;
        });

        // bell button
        ImageButton bellButton = findViewById(R.id.imageButton3);
        bellButton.setOnClickListener(v -> {
            new markerTask().execute("http://172.30.1.27:3000/bell");
            Toast.makeText(MainActivity.this, "현재위치 \n위도 " + currentLatitude + "\n경도 " + currentLongitude, Toast.LENGTH_LONG).show();
            flag = 1;
        });

        // store button
        ImageButton storeButton = findViewById(R.id.imageButton4);
        storeButton.setOnClickListener(v -> {
            new markerTask().execute("http://172.30.1.27:3000/store");
            Toast.makeText(MainActivity.this, "현재위치 \n위도 " + currentLatitude + "\n경도 " + currentLongitude, Toast.LENGTH_LONG).show();
            flag = 2;
        });
    }

    public class markerTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection con = null;
            BufferedReader reader = null;
            try {

                URL url = new URL(urls[0] + "?" + "latitude=" + currentLatitude + "&" + "longitude=" + currentLongitude); // 쿼리를 노드서버에 전달

                System.out.println("url : " + url);
                con = (HttpURLConnection) url.openConnection();
                con.connect();
                InputStream stream = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) buffer.append(line);
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                con.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) { // 노드서버가 디비로부터 데이터 받아서 일로 갖고옴
            super.onPostExecute(result);

            System.out.println("result : " + result);

            Gson gson = new Gson(); // parsing

            ImageButton removeMarkerButton = findViewById(R.id.imageButton5);
            removeMarkerButton.setOnClickListener(v -> mapboxMap.clear());

            if (flag == 0) { //3331
                Type listType = new TypeToken<ArrayList<Cctv>>() {
                }.getType();
                List<Cctv> cctv = gson.fromJson(result, listType);

//                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
//                Icon icon = iconFactory.fromResource(R.drawable.soo_cctv);
                Bitmap cctvMarker = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.soo_cctv);
                cctvMarker = Bitmap.createScaledBitmap(cctvMarker,125,200,true);

                if (cctvFlag) {
                    cctvFlag = false;
                } else {
                    for (int i=0;i<cctv.size();i++) {
                        double tmpLat = cctv.get(i).getLatitude();
                        double tmpLon = cctv.get(i).getLongitude();
                        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(tmpLat, tmpLon)).icon(IconFactory.getInstance(MainActivity.this).fromBitmap(cctvMarker)));
                    }
                    cctvFlag = true;
                }
            } else if (flag == 1) {//118
                Type listType = new TypeToken<ArrayList<Bell>>() {
                }.getType();
                List<Bell> bell = gson.fromJson(result, listType);

                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Icon icon = iconFactory.fromResource(R.drawable.soo_bell);

                if (bellFlag) {
                    bellFlag = false;
                } else {
                    for (int i=0;i<bell.size();i++) {
                        double tmpLat = bell.get(i).getLatitude();
                        double tmpLon = bell.get(i).getLongitude();
                        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(tmpLat, tmpLon)).icon(icon));
                    }
                    bellFlag = true;
                }
            } else if (flag == 2) {//233
                Type listType = new TypeToken<ArrayList<Store>>() {
                }.getType();
                List<Store> store = gson.fromJson(result, listType);

                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Icon icon = iconFactory.fromResource(R.drawable.soo_conveni);

                if (storeFlag) {
                    storeFlag = false;
                } else {
                    for (int i=0;i<store.size();i++) {
                        double tmpLat = store.get(i).getLatitude();
                        double tmpLon = store.get(i).getLongitude();
                        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(tmpLat, tmpLon)).icon(icon));
                    }
                    storeFlag = true;
                }
            } else if (flag == 3) {//25
                Type listType = new TypeToken<ArrayList<Police>>() {
                }.getType();
                List<Police> police = gson.fromJson(result, listType);

                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Icon icon = iconFactory.fromResource(R.drawable.soo_police);

                if (polFlag) {
                    polFlag = false;
                } else {
                    for (int i=0;i<police.size();i++) {
                        double tmpLat = police.get(i).getLatitude();
                        double tmpLon = police.get(i).getLongitude();
                        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(tmpLat, tmpLon)).icon(icon));
                    }
                    polFlag = true;
                }
            }
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        CameraPosition position = new CameraPosition.Builder().zoom(20).build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/nahyun/ck8qrxnfn0hwc1ioibio1rq0l/draft"), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                setUpSource(style);
                //검색된 위치의 피처 좌표를 표시하기 위해 새 심볼 레이어를 설정
                enableLocationComponent(style);
                Bitmap destination = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.mapbox_marker_icon_default);
                destination = Bitmap.createScaledBitmap(destination,125,200,true);
                style.addImage(symbolIconId,destination);
                setupLayer(style);

            }
        });
    }


    //검색 누르면 화면 전환
    private void initSearchFab() {
       // findViewById(R.id.fab_location_search).setOnClickListener(view -> {
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#EEEEEE"))
                            .limit(10)
                            .build(PlaceOptions.MODE_CARDS))
                    .build(MainActivity.this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
    //    });
    }


    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[]{0f, -8f})
        ));
    }

    //검색한 목적지 정보 받아옴
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 2000);

                    set_destination_route(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                            ((Point) selectedCarmenFeature.geometry()).longitude()));
                }

            }
        }
    }

    public boolean set_destination_route(@NonNull LatLng point) {

        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());
        getRoute(originPoint, destinationPoint);
        return true;
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = mapboxMap.getLocationComponent();
            locationComponentOptions = LocationComponentOptions.builder(this).build();
            locationComponentActivationOptions = new LocationComponentActivationOptions.Builder(this,loadedMapStyle).locationComponentOptions(locationComponentOptions).build();
            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);

            currentLocation = locationComponent.getLastKnownLocation();
            currentLatitude = currentLocation.getLatitude();
            currentLongitude = currentLocation.getLongitude();
            //currentLatitude = 37.283110;
            //currentLongitude = 127.044915;

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }


    // Button 관련 메서드
    public void sosButtonClick(View v) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:112"));
        smsSend(friend);
        startActivity(myIntent);
    }

    public void smsSend(Friend friend) {
        AsyncTask<Friend, Void, Void> asyncTask = new AsyncTask<Friend, Void, Void>() {
            @Override
            protected Void doInBackground(Friend... friends) {
                Message coolsms = new Message(api_key, api_secret);
                HashMap<String, String> params = new HashMap<String, String>();
                String currentPoint = null;
                try {
                    currentPoint = pointToAddress(currentLatitude,currentLongitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                params.put("to", friends[0].getNumber());
                params.put("from","01045443837"); //사전에 사이트에서 번호를 인증하고 등록하여야 함
                params.put("type", "SMS");
                params.put("text", "[나혼자간다] "+"긴급 상황입니다. 현재 위치:"+currentPoint); //메시지 내용
                params.put("app_version", "test app 1.2");
                try { JSONObject obj = (JSONObject)coolsms.send(params);
                    System.out.println(obj.toString()); //전송 결과 출력
                } catch (CoolsmsException e) {
                    System.out.println(e.getMessage());
                    System.out.println(e.getCode());
                }
                return null;
            }
        }; asyncTask.execute(friend);

    }

    public String pointToAddress(Double latitude,Double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(this);
        List<Address> address;
        address = geocoder.getFromLocation(latitude,longitude,
                1);
        return address.get(0).getAddressLine(0);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search :
                initSearchFab();
                return true ;
            default :
                return super.onOptionsItemSelected(item) ;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_action, menu) ;
        return true ;
    }
}