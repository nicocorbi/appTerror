package com.example.appterror.view;

import android.Manifest;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
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

    private int faseActual = 1; // Variable para saber qué mostrar
    private ImageView imageNoticiaGrande, imageNoticia1, imageNoticia2, imageNoticia3;

    // Oyente para los cambios de fase en tiempo real
    private BroadcastReceiver faseChangeReceiver;

    // Lanzadores de permisos
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

        // Forzamos el reinicio del estado a Fase 1 cada vez que la actividad se crea.
        VigilanciaService.guardarFase(this, 1);
        Log.d("NoticiasActivity", "Estado FORZADO a Fase 1 al crear la actividad.");

        imageNoticiaGrande = findViewById(R.id.image_noticia_grande);
        imageNoticia1 = findViewById(R.id.image_noticia_1);
        imageNoticia2 = findViewById(R.id.image_noticia_2);
        imageNoticia3 = findViewById(R.id.image_noticia_3);

        // Inicializamos el oyente (Receiver) que actualizará la UI en tiempo real
        faseChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Nos aseguramos de que el aviso es el que nos interesa
                if (intent != null && FaseReceiver.ACTION_FASE_CAMBIADA.equals(intent.getAction())) {
                    Log.d("NoticiasActivity", "¡Recibido aviso de cambio de fase en tiempo real!");
                    // Leemos la fase actual, que ya ha sido actualizada por el FaseReceiver
                    faseActual = VigilanciaService.getFaseGuardada(NoticiasActivity.this);
                    // ¡La magia! Recargamos las imágenes con el contenido de la nueva fase.
                    cargarContenidoDeLaFase();
                }
            }
        };

        setupBottomNavigation();
        verificarPermisosEIniciarServicios(); // Esto iniciará el servicio, que ahora leerá la fase 1.
    }

    // Archivo: NoticiasActivity.java
// Dentro del método onStart()

    @Override
    protected void onStart() {
        super.onStart();
        // Empezamos a escuchar los avisos de cambio de fase cuando la app se vuelve visible.

        // [INICIO DE LA CORRECCIÓN]
        // Verificamos la versión de Android para usar el método correcto de registerReceiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Para Android 13 y superior, es obligatorio especificar si es exportado o no.
            // RECEIVER_NOT_EXPORTED significa que solo nuestra app puede enviarle avisos.
            registerReceiver(faseChangeReceiver, new IntentFilter(FaseReceiver.ACTION_FASE_CAMBIADA), RECEIVER_NOT_EXPORTED);
        } else {
            // Para versiones antiguas, usamos el método tradicional.
            registerReceiver(faseChangeReceiver, new IntentFilter(FaseReceiver.ACTION_FASE_CAMBIADA));
        }
        // [FIN DE LA CORRECIÓN]

        Log.d("NoticiasActivity", "Oyente de cambio de fase REGISTRADO.");
    }


    @Override
    protected void onStop() {
        super.onStop();
        // Dejamos de escuchar cuando la app ya no es visible para ahorrar recursos.
        // Usamos un bloque try-catch por si el receiver no se llegó a registrar.
        try {
            unregisterReceiver(faseChangeReceiver);
            Log.d("NoticiasActivity", "Oyente de cambio de fase ANULADO.");
        } catch (IllegalArgumentException e) {
            Log.w("NoticiasActivity", "El BroadcastReceiver no estaba registrado, no se hace nada.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver a la actividad, leemos la fase actual guardada por el servicio
        this.faseActual = VigilanciaService.getFaseGuardada(this);
        Log.d("NoticiasActivity", "Actividad resumida. Mostrando contenido para la fase " + this.faseActual);
        cargarContenidoDeLaFase();
    }


    // --- LA CADENA DE PERMISOS E INICIO ---
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
        // Ahora solo iniciamos el servicio una vez. Él se encargará de todo.
        Intent serviceIntent = new Intent(this, VigilanciaService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Log.d("NoticiasActivity", "Orden de inicio enviada al VigilanciaService.");
    }


    // ========= MÉTODOS DE LA UI =========

    private void cargarContenidoDeLaFase() {
        Log.d("NoticiasActivity", "Cargando contenido visual para la fase: " + faseActual);
        // Switch para determinar qué imágenes mostrar
        switch (faseActual) {
            case 1:
                // Carga las imágenes de la Fase 1
                imageNoticiaGrande.setImageResource(R.drawable.fase1_noticia_grande);
                imageNoticia1.setImageResource(R.drawable.fase1_noticia_1);
                imageNoticia2.setImageResource(R.drawable.fase1_noticia_2);
                imageNoticia3.setImageResource(R.drawable.fase1_noticia_3);
                break;

            case 2:
                // Carga las imágenes de la Fase 2
                imageNoticiaGrande.setImageResource(R.drawable.fase2_noticia_grande);
                imageNoticia1.setImageResource(R.drawable.fase2_noticia_1);
                imageNoticia2.setImageResource(R.drawable.fase2_noticia_2);
                imageNoticia3.setImageResource(R.drawable.fase2_noticia_3);
                break;

            default:
                // Opcional: Cargar imágenes por defecto si la fase es desconocida
                Log.w("NoticiasActivity", "Fase desconocida: " + faseActual + ". Se cargarán las imágenes de la fase 1.");
                imageNoticiaGrande.setImageResource(R.drawable.fase1_noticia_grande);
                imageNoticia1.setImageResource(R.drawable.fase1_noticia_1);
                imageNoticia2.setImageResource(R.drawable.fase1_noticia_2);
                imageNoticia3.setImageResource(R.drawable.fase1_noticia_3);
                break;
        }
    }


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
}








