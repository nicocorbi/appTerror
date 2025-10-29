// Archivo: MapsActivity.java (Versión final con centrado en la ubicación del usuario)
package com.example.appterror.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location; // Importante: importar android.location.Location
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.appterror.R;
// Importaciones necesarias para obtener la ubicación
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    // 1. Declara el cliente para obtener la ubicación
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final LatLng defaultLocation = new LatLng(40.416775, -3.703790); // Ubicación por defecto (Madrid)
    private static final float DEFAULT_ZOOM = 15f;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    activarMiUbicacion();
                } else {
                    Toast.makeText(this, "Permiso denegado. Mostrando ubicación por defecto.", Toast.LENGTH_LONG).show();
                    // Si el permiso es denegado, movemos la cámara a la ubicación por defecto
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // 2. Inicializa el cliente de ubicación
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // --- CÓDIGO DE NAVEGACIÓN ---
        setupBottomNavigation();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        verificarPermisosDeUbicacion();
    }

    private void verificarPermisosDeUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            activarMiUbicacion();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void activarMiUbicacion() {
        // Esta comprobación es obligatoria
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // 3. LLAMA AL MÉTODO PARA OBTENER LA UBICACIÓN Y MOVER LA CÁMARA
        obtenerUbicacionYCentrarMapa();
    }

    private void obtenerUbicacionYCentrarMapa() {
        // La comprobación del permiso es obligatoria aquí también
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Obtiene la última ubicación conocida. Es una tarea asíncrona.
        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // La tarea fue exitosa. La ubicación puede ser nula si el GPS estaba apagado.
                Location lastKnownLocation = task.getResult();
                if (lastKnownLocation != null) {
                    // 4. Mueve la cámara a la ubicación del usuario
                    LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));
                } else {
                    // Si la ubicación es nula, mueve a la ubicación por defecto
                    Log.d("MapsActivity", "La última ubicación conocida es nula. Usando ubicación por defecto.");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                }
            } else {
                // La tarea falló. Mueve a la ubicación por defecto.
                Log.e("MapsActivity", "Excepción al obtener la ubicación.", task.getException());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_maps);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MenuActivity.class));
                return true;
            } else if (itemId == R.id.navigation_consejos) {
                startActivity(new Intent(getApplicationContext(), ConsejosActivity.class));
                return true;
            } else if (itemId == R.id.navigation_noticias) {
                startActivity(new Intent(getApplicationContext(), NoticiasActivity.class));
                return true;
            } else if (itemId == R.id.navigation_maps) {
                return true;
            }
            return false;
        });
    }
}

