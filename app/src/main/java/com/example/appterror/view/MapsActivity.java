// Archivo: MapsActivity.java (VERSIÓN CON OPENSTREETMAP)
package com.example.appterror.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;import android.location.LocationListener;
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

import com.example.appterror.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Map;

public class MapsActivity extends AppCompatActivity implements LocationListener {

    private MapView map = null;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager locationManager;

    private static final String TAG = "MapsActivity_OSM";
    private static final float DEFAULT_ZOOM = 16.0f;

    // Lanzador para solicitar permisos de ubicación
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean isGranted = false;
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    if (entry.getValue()) {
                        isGranted = true;
                        break;
                    }
                }
                if (isGranted) {
                    Log.d(TAG, "Permiso de ubicación concedido.");
                    setupMapAndLocation(); // Si se conceden, configuramos el mapa y la ubicación
                } else {
                    Log.d(TAG, "Permiso de ubicación denegado.");
                    Toast.makeText(this, "El permiso de ubicación es necesario para mostrar tu posición.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- IMPORTANTE para osmdroid ---
        // Esto carga la configuración de osmdroid, incluyendo el 'user agent' para evitar ser bloqueado.
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        // ---------------------------------

        setContentView(R.layout.activity_maps);

        // Inicializamos el mapa desde el layout
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK); // Establece el proveedor de losetas del mapa

        // Habilitar controles de zoom
        map.setMultiTouchControls(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        checkLocationPermissionAndSetupMap();
        setupBottomNavigation();
    }

    private void checkLocationPermissionAndSetupMap() {
        // Lista de permisos que necesitamos
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        // Comprobamos si ya tenemos los permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permisos ya concedidos. Configurando mapa.");
            setupMapAndLocation();
        } else {
            // Si no, los solicitamos
            Log.d(TAG, "Solicitando permisos de ubicación.");
            requestPermissionLauncher.launch(permissions);
        }
    }

    private void setupMapAndLocation() {
        // Comprobación de seguridad
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Salimos si no tenemos permisos
        }

        // Creamos la capa de "Mi Ubicación"
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        this.mLocationOverlay.enableMyLocation(); // Habilitamos el seguimiento de la ubicación
        this.mLocationOverlay.enableFollowLocation(); // Hacemos que el mapa siga al usuario
        this.mLocationOverlay.setDrawAccuracyEnabled(true); // Dibuja el círculo de precisión

        map.getOverlays().add(this.mLocationOverlay);

        // Centramos el mapa en la última ubicación conocida
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            GeoPoint startPoint = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            map.getController().setCenter(startPoint);
            map.getController().setZoom(DEFAULT_ZOOM);
        } else {
            // Si no hay última ubicación, podemos poner una por defecto (ej. Barcelona)
            GeoPoint defaultPoint = new GeoPoint(41.3851, 2.1734);
            map.getController().setCenter(defaultPoint);
            map.getController().setZoom(12.0); // Zoom más alejado
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reanudamos la configuración del mapa y la obtención de la ubicación
        if (map != null) {
            map.onResume();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausamos la configuración del mapa y la obtención de la ubicación para ahorrar batería
        if (map != null) {
            map.onPause();
        }
        locationManager.removeUpdates(this);
    }

    // --- Métodos del LocationListener ---

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Este método se podría usar para actualizar cosas cuando la ubicación cambia,
        // pero MyLocationNewOverlay ya lo gestiona visualmente.
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {}

    @Override
    public void onProviderDisabled(@NonNull String provider) {}


    /**
     * Configura la barra de navegación inferior.
     */
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


