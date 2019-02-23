package com.swg_games_lab.nanicki.artguide.fragment.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.swg_games_lab.nanicki.artguide.ApplicationActivity;
import com.swg_games_lab.nanicki.artguide.R;
import com.swg_games_lab.nanicki.artguide.csv.CSVreader;
import com.swg_games_lab.nanicki.artguide.listener.MyLocationListener;
import com.swg_games_lab.nanicki.artguide.listener.RouteReceiver;
import com.swg_games_lab.nanicki.artguide.model.Place;
import com.swg_games_lab.nanicki.artguide.util.ConnectionUtil;
import com.swg_games_lab.nanicki.artguide.util.MarkerUtil;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.IconOverlay;

import java.lang.ref.WeakReference;


public class MapFragment extends MapBottomButtonsFragment implements RouteReceiver {

    // Fields
    private static final String TAG = "MapFragment";
    private boolean NO_CONNECTION_MODE;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Context context = container.getContext();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;

        View result;
        boolean connected = ConnectionUtil.isConnected(locationManager, context);
        if (!connected) {
            NO_CONNECTION_MODE = true;
            result = inflater.inflate(R.layout.out_of_connection, container, false);
        } else {
            NO_CONNECTION_MODE = false;
            result = inflater.inflate(R.layout.fragment_map, container, false);
        }
        return result;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (NO_CONNECTION_MODE) {
            ImageView imageView = (ImageView) view.findViewById(R.id.out_of_connection_show_dialog);
            imageView.setOnClickListener(v -> ConnectionUtil.buildAlertMessageNoConncetion(view.getContext()));
            return;
        }

        // Инициализация layoutов
        init(view);
        // Place Description layout
        initMapMarker(view);
        // Bottom srting buttons
        initBottomSortingButtons(view);
        // Route Info
        initRouteInfoLayout(view);
        // Настройка карты
        setUpMap();
        // Setting up dialog (appears on tap up)
        initMarkerView(view);
        // Setting up close route Dialog
        initCloseRouteView(view);
        // Loading markers
        loadMarkers();


    }


    protected void init(View view) {
        //load/initialize the osmdroid configuration, this can be done
        Context context = getContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        //inflate and create the map
        map = (MapView) view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // Получение текущих координат
        myLocationListener = new MyLocationListener(this);

        // TODO: Add Later
//        this.registerReceiver(mConnReceiver,
//                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }


    public void onLocationChanged() {
        Log.d(TAG, "onLocationChanged called");
        if (isAlive || lastDrownItem == null) {
            Log.d(TAG, "Will not rebuild route because lastDrownItem is null");
            return;
        }

        routeBuilding = true;
        Toast.makeText(getContext(), "Перестраиваю", Toast.LENGTH_SHORT).show();
        if (updateRoadTask != null)
            updateRoadTask.cancel(true);
        updateRoadTask = null;
        postUserLocationAndCallUpdateRoadTask((GeoPoint) lastDrownItem.getPosition());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        if (NO_CONNECTION_MODE) {
            Context context = getContext();
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            assert locationManager != null;
            boolean connected = ConnectionUtil.isConnected(locationManager, context);
            if (connected) {
                ApplicationActivity activity = (ApplicationActivity) getActivity();
                activity.rebindMapFragment();
            }
            return;
        }

        if (isAlive) {
            return;
        }

        myLocationListener.mapFragment = new WeakReference<>(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);

        map.onResume();
        isAlive = true;
        Bundle extras = getArguments();

        if (extras != null) {
            int id = extras.getInt("TAG");
            if (id != lastId) {
                lastId = id;
                checkBundle(lastId);
                return;
            }
        }
        if (lastDrownItem != null) {
            requestDrawRoute(lastItem);
        } else if (!map.getOverlays().contains(lastMarkers)) {
            // Маркеры настроены можно добавить
            map.getOverlays().add(lastMarkers);
        }
    }

    private void checkBundle(int id) {

        Context context = getContext();
        map.getOverlays().remove(lastMarkers);
        layoutBottomButtons.setVisibility(View.GONE);

        Place place = CSVreader.getPlaceById(id);
        String title = place.getTitle();
        int imageSmall = place.getImageSmall();
        double latitude = place.getLatitude();
        double longitude = place.getLongitude();

        if (lastDrownItem != null) {
            map.getOverlays().remove(lastDrownItem);
        }

        lastDrownItem = new IconOverlay(new GeoPoint(latitude, longitude), context.getDrawable(MarkerUtil.getMapMarkerByPlaceId(lastId)));
        map.getOverlays().add(lastDrownItem);


        mapRouteImage.setImageDrawable(context.getDrawable(imageSmall));
        closeRouteImage.setImageDrawable(context.getDrawable(imageSmall));
        mapRouteTitle.setText(title);
        // Hide description
        mapRouteTime.setVisibility(View.GONE);
        mapRouteWalkImage.setVisibility(View.GONE);
        mapRouteLength.setVisibility(View.GONE);
        // Show progress bar
        mapRouteProgressBar.setVisibility(View.VISIBLE);
        // Show
        mapRouteInfo.setVisibility(View.VISIBLE);

        setArguments(null);
        postUserLocationAndCallUpdateRoadTask(new GeoPoint(latitude, longitude));

    }

    @Override
    public void onStop() {
        super.onStop();
        if (NO_CONNECTION_MODE)
            return;

        locationManager.removeUpdates(myLocationListener);
        myLocationListener.mapFragment = null;
        stopRoute();

        // FIXME: Sould this method call exist???
        //map.destroyDrawingCache();
    }

    public void stopRoute() {
        isAlive = false;
        if (updateRoadTask != null)
            updateRoadTask.cancel(true);
        updateRoadTask = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (NO_CONNECTION_MODE)
            return;
        map.onPause();
    }

}
