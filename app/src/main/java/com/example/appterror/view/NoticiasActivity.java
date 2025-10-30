// Archivo: NoticiasActivity.java (VERSIÓN CORREGIDA)
package com.example.appterror.view;

import android.Manifest;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

// Import estático necesario para RECEIVER_NOT_EXPORTED en APIs nuevas
import static android.content.Context.RECEIVER_NOT_EXPORTED;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.appterror.R;
import com.example.appterror.controller.FaseReceiver;
import com.example.appterror.controller.VigilanciaService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NoticiasActivity extends AppCompatActivity {

    private int faseActual = 1;
    private ImageView imageNoticiaGrande, imageNoticia1, imageNoticia2, imageNoticia3;
    private BroadcastReceiver faseChangeReceiver;

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

        imageNoticiaGrande = findViewById(R.id.image_noticia_grande);
        imageNoticia1 = findViewById(R.id.image_noticia_1);
        imageNoticia2 = findViewById(R.id.image_noticia_2);
        imageNoticia3 = findViewById(R.id.image_noticia_3);

        faseChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && FaseReceiver.ACTION_FASE_CAMBIADA.equals(intent.getAction())) {
                    // [CORRECCIÓN CLAVE]: Leemos la nueva fase directamente del aviso (Intent).
                    int nuevaFaseRecibida = intent.getIntExtra("nuevaFase", -1);
                    if (nuevaFaseRecibida != -1) {
                        Log.d("NoticiasActivity", "Aviso de cambio de fase en TIEMPO REAL recibido. Nueva fase: " + nuevaFaseRecibida);
                        faseActual = nuevaFaseRecibida;
                        cargarContenidoDeLaFase();
                    }
                }
            }
        };

        setupBottomNavigation();
        verificarPermisosEIniciarServicios();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Registramos el receptor para escuchar cambios de fase en tiempo real (mientras la app está visible)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(faseChangeReceiver, new IntentFilter(FaseReceiver.ACTION_FASE_CAMBIADA), RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(faseChangeReceiver, new IntentFilter(FaseReceiver.ACTION_FASE_CAMBIADA));
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(faseChangeReceiver);
            Log.d("NoticiasActivity", "Oyente de cambio de fase ANULADO.");
        } catch (IllegalArgumentException e) {
            Log.w("NoticiasActivity", "El BroadcastReceiver no estaba registrado, no se anula.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver a la actividad, SIEMPRE leemos el estado guardado para asegurar que la UI esté sincronizada.
        this.faseActual = VigilanciaService.getFaseGuardada(this);
        Log.d("NoticiasActivity", "Actividad resumida. Mostrando contenido para la fase guardada: " + this.faseActual);
        cargarContenidoDeLaFase();
    }

    private void cargarContenidoDeLaFase() {
        Log.d("NoticiasActivity", "Cargando contenido visual para la fase: " + faseActual);
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
            case 3:
                imageNoticiaGrande.setImageResource(R.drawable.fase3_noticia_grande);
                imageNoticia1.setImageResource(R.drawable.fase3_noticia1);
                imageNoticia2.setImageResource(R.drawable.fase3_noticia_2);
                imageNoticia3.setImageResource(R.drawable.fase3_noticia3);
                break;
            case 4:
                imageNoticiaGrande.setImageResource(R.drawable.fase4_noticia_grande);
                imageNoticia1.setImageResource(R.drawable.fase4_noticia_1);
                imageNoticia2.setImageResource(R.drawable.fase4_noticia_2);
                imageNoticia3.setImageResource(R.drawable.fase4_noticia_2);
                break;
            default:
                Log.w("NoticiasActivity", "Fase desconocida: " + faseActual + ". Cargando fase 1 por defecto.");
                imageNoticiaGrande.setImageResource(R.drawable.fase1_noticia_grande);
                imageNoticia1.setImageResource(R.drawable.fase1_noticia_1);
                imageNoticia2.setImageResource(R.drawable.fase1_noticia_2);
                imageNoticia3.setImageResource(R.drawable.fase1_noticia_3);
                break;
        }
    }

    // ... (El resto de métodos de la clase: setupBottomNavigation, permisos, etc., permanecen igual)
    // Se incluyen aquí para que sea el script completo.

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_noticias);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_noticias) return true;

            Intent intent = null;
            if (itemId == R.id.navigation_home) intent = new Intent(getApplicationContext(), MenuActivity.class);
            else if (itemId == R.id.navigation_consejos) intent = new Intent(getApplicationContext(), ConsejosActivity.class);
            else if (itemId == R.id.navigation_maps) intent = new Intent(getApplicationContext(), MapsActivity.class);

            if (intent != null) {
                startActivity(intent);
                finish();
            }
            return true;
        });
    }

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
            } else {
                iniciarElServicio();
            }
        } else {
            iniciarElServicio();
        }
    }

    private void iniciarElServicio() {
        Intent serviceIntent = new Intent(this, VigilanciaService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Log.d("NoticiasActivity", "Orden de inicio enviada al VigilanciaService.");
    }
}









