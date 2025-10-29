package com.example.appterror.view;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.appterror.R;
import com.example.appterror.controller.GestorDeAlertas; // Importamos el gestor
import com.example.appterror.controller.VigilanciaService; // Importamos el servicio
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NoticiasActivity extends AppCompatActivity {

    private int faseActual = 1;
    private final int TOTAL_FASES = 2;
    private ImageView imageNoticiaGrande, imageNoticia1, imageNoticia2, imageNoticia3;
    private final Handler handlerNoticias = new Handler(Looper.getMainLooper());
    private Runnable runnableNoticias;
    private final long TIEMPO_DE_CAMBIO = 30000;

    private GestorDeAlertas gestorDeAlertas; // ¡El gestor vuelve!

    // (Los lanzadores de permisos se quedan igual)
    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) verificarPermisoOverlay();
        else Toast.makeText(this, "El permiso de notificaciones es vital.", Toast.LENGTH_LONG).show();
    });
    private final ActivityResultLauncher<Intent> overlayPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (Settings.canDrawOverlays(this)) verificarPermisoAlarma();
        else Toast.makeText(this, "El permiso de superposición es necesario.", Toast.LENGTH_SHORT).show();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noticias);

        gestorDeAlertas = new GestorDeAlertas(this); // Creamos la instancia

        imageNoticiaGrande = findViewById(R.id.image_noticia_grande);
        imageNoticia1 = findViewById(R.id.image_noticia_1);
        imageNoticia2 = findViewById(R.id.image_noticia_2);
        imageNoticia3 = findViewById(R.id.image_noticia_3);

        inicializarContadorNoticias();
        cargarContenidoDeLaFase();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handlerNoticias.postDelayed(runnableNoticias, TIEMPO_DE_CAMBIO);
        verificarPermisosEIniciarServicios(); // La cadena de arranque
    }

    @Override
    protected void onPause() {
        super.onPause();
        handlerNoticias.removeCallbacks(runnableNoticias);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerCiclos();
    }

    private void inicializarContadorNoticias() {
        runnableNoticias = () -> {
            faseActual = (faseActual % TOTAL_FASES) + 1;
            cargarContenidoDeLaFase();
            gestorDeAlertas.detener(); // Detenemos las alarmas anteriores
            gestorDeAlertas.iniciar(faseActual); // Reinicia las alarmas con la nueva fase
            handlerNoticias.postDelayed(runnableNoticias, TIEMPO_DE_CAMBIO);
        };
    }

    // --- CADENA DE PERMISOS Y ARRANQUE ---
    private void verificarPermisosEIniciarServicios() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            verificarPermisoOverlay();
        }
    }

    private void verificarPermisoOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            overlayPermissionLauncher.launch(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        } else {
            verificarPermisoAlarma();
        }
    }

    private void verificarPermisoAlarma() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName())));
                Toast.makeText(this, "Activa el permiso de alarmas y vuelve.", Toast.LENGTH_LONG).show();
            } else {
                iniciarServicios();
            }
        } else {
            iniciarServicios();
        }
    }

    private void iniciarServicios() {
        // Usamos un Handler para evitar el crash en Android 12+
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                // 1. Inicia el escudo
                Intent serviceIntent = new Intent(this, VigilanciaService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }

                // 2. Inicia el cerebro de las alarmas
                gestorDeAlertas.iniciar(faseActual);
                Log.d("NoticiasActivity", "Servicios (escudo y alarmas) iniciados.");
            }
        }, 500);
    }

    private void detenerCiclos() {
        handlerNoticias.removeCallbacks(runnableNoticias);
        stopService(new Intent(this, VigilanciaService.class)); // Detiene el escudo
        if (gestorDeAlertas != null) {
            gestorDeAlertas.detener(); // Detiene las alarmas
        }
    }

    // ========= INICIO DE LA CORRECCIÓN: MÉTODOS QUE FALTABAN =========

    private void cargarContenidoDeLaFase() {
        switch (faseActual) {
            case 1:
                imageNoticiaGrande.setImageResource(R.drawable.fase1_noticia_grande);
                imageNoticia1.setImageResource(R.drawable.fase1_noticia_1);
                imageNoticia2.setImageResource(R.drawable.fase1_noticia_2);
                imageNoticia3.setImageResource(R.drawable.fase1_noticia_3);
                break;
            case 2:
                imageNoticiaGrande.setImageResource(R.drawable.fase2_noticia_grande);
                imageNoticia1.setImageResource(R.drawable.fase2_noticia_1);
                imageNoticia2.setImageResource(R.drawable.fase2_noticia_2);
                imageNoticia3.setImageResource(R.drawable.fase2_noticia_3);
                break;
            default:
                // Por si acaso, volvemos a la fase 1
                imageNoticiaGrande.setImageResource(R.drawable.fase1_noticia_grande);
                imageNoticia1.setImageResource(R.drawable.fase1_noticia_1);
                imageNoticia2.setImageResource(R.drawable.fase1_noticia_2);
                imageNoticia3.setImageResource(R.drawable.fase1_noticia_3);
                faseActual = 1;
                break;
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_noticias);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            // Evita relanzar la misma actividad
            if (itemId == R.id.navigation_noticias) {
                return true;
            }

            detenerCiclos(); // Detenemos todo antes de salir
            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                intent = new Intent(getApplicationContext(), MenuActivity.class);
            } else if (itemId == R.id.navigation_consejos) {
                intent = new Intent(getApplicationContext(), ConsejosActivity.class);
            } else if (itemId == R.id.navigation_maps) {
                intent = new Intent(getApplicationContext(), MapsActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                finish(); // Cerramos la actividad actual
            }
            return true;
        });
    }
    // ===============================================================
}






