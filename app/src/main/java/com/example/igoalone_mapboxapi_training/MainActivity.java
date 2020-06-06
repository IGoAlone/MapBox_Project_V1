package com.example.igoalone_mapboxapi_training;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

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
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

//버튼 클릭 이벤트마다 다르게 요청보냄
//요청 파라미터: 사용자 위도 / 안전요소
//받아와서 마커 띄움

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {
    //네비게이션 기능

    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;
    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    // variables needed to initialize navigation
    private Button button;

    //목적지 검색기능
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private CarmenFeature user;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

    int flag = 0;

    private Location currentLocation;
    private double currentLatitude;
    private double currentLongitude;

    // 안전요소 flag
    private boolean cctvFlag = false;
    private boolean bellFlag = false;
    private boolean storeFlag = false;
    private boolean polFlag = false;

    //
    //List<Bell> bell;
//    private List<Cctv> cctv = new List<Cctv>();
//    private List<Bell> bell;
//    private List<Store> store;
//    private List<Police> police;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));

        setContentView(R.layout.activity_main);
        //setContentView(findViewById(R.id.media_route_menu_item));


        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);



        /**
         * 버튼 별 task 수행
         */

        // cctv button
        ImageButton cctvButton = findViewById(R.id.imageButton);
        cctvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONTask().execute("http://172.30.1.4:3000/cctv");
                Toast.makeText(MainActivity.this, "현재위치 \n위도 " + currentLatitude + "\n경도 " + currentLongitude, Toast.LENGTH_LONG).show();
                flag = 0;
            }
        });

        // police button
        ImageButton policeButton = findViewById(R.id.imageButton2);
        policeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONTask().execute("http://172.30.1.4:3000/police");
                Toast.makeText(MainActivity.this, "현재위치 \n위도 " + currentLatitude + "\n경도 " + currentLongitude, Toast.LENGTH_LONG).show();
                flag = 3;
            }
        });

        // bell button
        ImageButton bellButton = findViewById(R.id.imageButton3);
        bellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONTask().execute("http://172.30.1.4:3000/bell");
                Toast.makeText(MainActivity.this, "현재위치 \n위도 " + currentLatitude + "\n경도 " + currentLongitude, Toast.LENGTH_LONG).show();
                flag = 1;
            }
        });

        // store button
        ImageButton storeButton = findViewById(R.id.imageButton4);
        storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONTask().execute("http://172.30.1.4:3000/store");
                Toast.makeText(MainActivity.this, "현재위치 \n위도 " + currentLatitude + "\n경도 " + currentLongitude, Toast.LENGTH_LONG).show();
                flag = 2;
            }
        });
    }

    public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            System.out.println("jsontask in method");

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

            //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();

            System.out.println("result : " + result);

            Gson gson = new Gson(); // parsing

            ImageButton removeMarkerButton = findViewById(R.id.imageButton5);
            removeMarkerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapboxMap.clear();
                }
            });

            /**
             * CCTV
             */

            if (flag == 0) {
                System.out.println("flag0");
                Type listType = new TypeToken<ArrayList<Cctv>>() {
                }.getType();
                List<Cctv> cctv = gson.fromJson(result, listType);
                System.out.println("나는야 CCTV : " + cctv.toString());
                //mapboxMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)));

                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Icon icon = iconFactory.fromResource(R.drawable.cctv);

                int i = 0;

                if (cctvFlag) {

                    //mapboxMap.clear();
                    //mapboxMap.removeMarker(cctvMarkerList.get(0));
                    //cctvMarkerList.clear();
                    cctvFlag = false;
                } else {


                    for (Cctv c : cctv) {
                        double tmpLat = cctv.get(i).getLatitude();
                        double tmpLon = cctv.get(i).getLongitude();

                        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(tmpLat, tmpLon)).icon(icon));

                        i++;
                    }

                    cctvFlag = true;
                }


                //3331

                /**
                 * Bell
                 */

            } else if (flag == 1) {
                System.out.println("flag1");
                Type listType = new TypeToken<ArrayList<Bell>>() {
                }.getType();
                List<Bell> bell = gson.fromJson(result, listType);
                System.out.println("나는야 Bell : " + bell.toString());

                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Icon icon = iconFactory.fromResource(R.drawable.bell);

                int i = 0;

                if (bellFlag) {

                    //mapboxMap.clear();
                    //mapboxMap.removeMarker();
                    bellFlag = false;
                } else {
                    for (Bell b : bell) {

                        double tmpLat = bell.get(i).getLatitude();
                        double tmpLon = bell.get(i).getLongitude();
                        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(tmpLat, tmpLon)).icon(icon));
                        i++;
                    }
                    bellFlag = true;
                }

                //118

                /**
                 * Store
                 */

            } else if (flag == 2) {
                System.out.println("flag2");
                Type listType = new TypeToken<ArrayList<Store>>() {
                }.getType();
                List<Store> store = gson.fromJson(result, listType);
                System.out.println("나는야 store : " + store.toString());

                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Icon icon = iconFactory.fromResource(R.drawable.convenience_store);

                int i = 0;

                if (storeFlag) {

                    //mapboxMap.clear();
                    //mapboxMap.removeMarker();
                    storeFlag = false;
                } else {
                    for (Store s : store) {

                        double tmpLat = store.get(i).getLatitude();
                        double tmpLon = store.get(i).getLongitude();
                        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(tmpLat, tmpLon)).icon(icon));
                        i++;
                    }
                    storeFlag = true;
                }

                //233

                /**
                 * Police
                 */

            } else if (flag == 3) {
                System.out.println("flag3");
                Type listType = new TypeToken<ArrayList<Police>>() {
                }.getType();
                List<Police> police = gson.fromJson(result, listType);
                System.out.println("나는야 police : " + police.toString());

                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Icon icon = iconFactory.fromResource(R.drawable.police);

                int i = 0;

                if (polFlag) {

                    //mapboxMap.clear();
                    //mapboxMap.removeMarker();
                    polFlag = false;
                } else {
                    for (Police p : police) {

                        double tmpLat = police.get(i).getLatitude();
                        double tmpLon = police.get(i).getLongitude();
                        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(tmpLat, tmpLon)).icon(icon));
                        i++;
                    }
                    polFlag = true;
                }

                //25
            }
            //flag++;
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        CameraPosition  position = new CameraPosition.Builder().zoom(20).build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        mapboxMap.setStyle(Style.LIGHT, new
                Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        initSearchFab();

                        style.addImage(symbolIconId, BitmapFactory.decodeResource(
                                MainActivity.this.getResources(), R.drawable.igoalone_marker));
                        // Create an empty GeoJSON source using the empty feature collection
                        setUpSource(style);

                        //검색된 위치의 피처 좌표를 표시하기 위해 새 심볼 레이어를 설정
                        setupLayer(style);

                        enableLocationComponent(style);


                        addDestinationIconSymbolLayer(style);

                        button = findViewById(R.id.startButton);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean simulateRoute = true;
                                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                        .directionsRoute(currentRoute)
                                        .shouldSimulateRoute(simulateRoute)
                                        .build();

                                //네비게이션 호출 부분 없앰
                                // NavigationLauncher.startNavigation(MainActivity.this, options);
                            }
                        });
                    }
                });
    }

    //검색 누르면 화면 전환
    private void initSearchFab() {
        findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MainActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
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

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    // Move map camera to the selected location
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

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
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

                        // Draw the route on the map
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
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);

            // 사용자의 최종 위치 받아
            currentLocation = locationComponent.getLastKnownLocation();
            currentLatitude = currentLocation.getLatitude();
            currentLongitude = currentLocation.getLongitude();

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
        startActivity(myIntent);
    }

    public void cctvButtonClick(View v) { // cctv
        Toast.makeText(this, "cctv", Toast.LENGTH_LONG).show();
//        Intent intent = new Intent(this,Main2Activity.class);
//        startActivity(intent);

    }

    public void policeButtonClick(View v) { // 경찰서

    }

    public void bellButtonClick(View v) { // 안전벨

    }

    public void conButtonClick(View v) { // 편의점

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_action, menu);

        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.app_bar_search :
//                // TODO : process the click event for action_search item.
//                initSearchFab();
//                return true;
//            // ...
//            // ...
//            default :
//                return super.onOptionsItemSelected(item) ;
//        }
//    }
}