package com.example.appterror.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.appterror.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.nio.charset.StandardCharsets;


public class MapsActivity extends AppCompatActivity implements LocationListener {

    private MapView map = null;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager locationManager;
    private RequestQueue volleyQueue;

    private static final String TAG = "MapsActivity_OSM";
    private static final float DEFAULT_ZOOM = 16.0f;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                // Comprobamos si CUALQUIERA de los dos permisos fue concedido
                if (Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_FINE_LOCATION)) ||
                        Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION))) {
                    Log.d(TAG, "Permiso de ubicación concedido.");
                    setupMapAndLocation();
                } else {
                    Log.d(TAG, "Permiso de ubicación denegado.");
                    Toast.makeText(this, "El permiso de ubicación es necesario para mostrar tu posición.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_maps);

        volleyQueue = Volley.newRequestQueue(this);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Llamamos al método que ahora gestionará todo el flujo de permisos y configuración.
        checkLocationPermissionAndSetupMap();
        setupBottomNavigation();
    }

    private void checkLocationPermissionAndSetupMap() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        // Comprobamos si tenemos CUALQUIERA de los dos permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permisos ya concedidos. Configurando mapa.");
            setupMapAndLocation();
        } else {
            // Si no tenemos ninguno, los solicitamos
            Log.d(TAG, "Solicitando permisos de ubicación.");
            requestPermissionLauncher.launch(permissions);
        }
    }

    private void setupMapAndLocation() {
        //  Comprobación de seguridad redundante pero buena práctica
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si llegamos aquí sin permisos (no debería pasar), no hacemos nada.
            return;
        }

        // Configuración de la capa de ubicación
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        this.mLocationOverlay.enableMyLocation();
        this.mLocationOverlay.enableFollowLocation();
        this.mLocationOverlay.setDrawAccuracyEnabled(true);
        map.getOverlays().add(this.mLocationOverlay);
        this.mLocationOverlay.runOnFirstFix(() -> {
            if (isFinishing() || isDestroyed()) return; // Evitar crashes si la actividad se cierra
            runOnUiThread(() -> {
                if (map != null && mLocationOverlay.getMyLocation() != null) {
                    map.getController().setCenter(mLocationOverlay.getMyLocation());
                    map.getController().setZoom(DEFAULT_ZOOM);
                    findAndDrawChurches(); // Buscamos iglesias una vez que el mapa está centrado
                }
            });
        });

        // Intentar centrar con la última ubicación conocida para una carga visual más rápida
        try {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                GeoPoint startPoint = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                map.getController().setCenter(startPoint);
                map.getController().setZoom(DEFAULT_ZOOM);
                findAndDrawChurches(); // Buscamos también aquí por si la primera fijación tarda
            } else {
                // Ubicación por defecto si no hay última conocida
                GeoPoint defaultPoint = new GeoPoint(41.3851, 2.1734); // Barcelona
                map.getController().setCenter(defaultPoint);
                map.getController().setZoom(12.0);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error de seguridad al obtener la última ubicación.", e);
            Toast.makeText(this, "No se pudo acceder a la ubicación por un problema de permisos.", Toast.LENGTH_SHORT).show();
        }
    }


    private void findAndDrawChurches() {
        if (map == null || volleyQueue == null) return;
        if (map.getBoundingBox() == null) {
            Log.w(TAG, "BoundingBox es nulo, no se puede buscar iglesias aún.");
            return;
        }

        BoundingBox boundingBox = map.getBoundingBox();
        String overpassQuery = "[out:json];" +
                "node[amenity=place_of_worship][religion=christian]" +
                "(" + boundingBox.getLatSouth() + "," + boundingBox.getLonWest() + "," +
                boundingBox.getLatNorth() + "," + boundingBox.getLonEast() + ");" +
                "out;";

        String url = "https://overpass-api.de/api/interpreter?data=" + new String(overpassQuery.getBytes(), StandardCharsets.UTF_8);
        Log.d(TAG, "Overpass URL: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.d(TAG, "Overpass response received.");
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray elements = jsonObject.getJSONArray("elements");

                        Drawable churchIcon = ContextCompat.getDrawable(this, R.drawable.ic_church_marker);

                        for (int i = 0; i < elements.length(); i++) {
                            JSONObject element = elements.getJSONObject(i);
                            double lat = element.getDouble("lat");
                            double lon = element.getDouble("lon");

                            JSONObject tags = element.optJSONObject("tags");
                            String name = (tags != null && tags.has("name")) ? tags.getString("name") : "Iglesia";

                            Marker churchMarker = new Marker(map);
                            churchMarker.setPosition(new GeoPoint(lat, lon));
                            churchMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            churchMarker.setIcon(churchIcon);
                            churchMarker.setTitle("Zona Segura: " + name);
                            map.getOverlays().add(churchMarker);
                        }
                        map.invalidate();
                        Log.d(TAG, "Se añadieron " + elements.length() + " marcadores de iglesias.");
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al parsear la respuesta de Overpass", e);
                    }
                },
                error -> Log.e(TAG, "Error en la petición a Overpass: " + error.toString())
        );

        volleyQueue.add(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
        // Sirve para solo pedir actualizaciones si tenemos el permiso
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10f, this);
            } catch (SecurityException e) {
                Log.e(TAG, "No se pueden solicitar actualizaciones de ubicación por falta de permisos.", e);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
        locationManager.removeUpdates(this);
        if (volleyQueue != null) {
            volleyQueue.cancelAll(TAG);
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(@NonNull String provider) {}
    @Override
    public void onProviderDisabled(@NonNull String provider) {}
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_maps);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_maps) {
                return true;
            }
            Intent intent = null;
            if (itemId == R.id.navigation_home) {
                intent = new Intent(getApplicationContext(), MenuActivity.class);
            } else if (itemId == R.id.navigation_consejos) {
                intent = new Intent(getApplicationContext(), ConsejosActivity.class);
            } else if (itemId == R.id.navigation_noticias) {
                intent = new Intent(getApplicationContext(), NoticiasActivity.class);
            }
            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
            return true;
        });
    }
}



