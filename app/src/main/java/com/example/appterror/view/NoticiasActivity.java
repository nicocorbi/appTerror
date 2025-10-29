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
// Importamos SharedPreferences
import com.example.appterror.controller.VigilanciaService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NoticiasActivity extends AppCompatActivity {

    private int faseActual = 1; // Variable para saber qué mostrar
    private ImageView imageNoticiaGrande, imageNoticia1, imageNoticia2, imageNoticia3;

    // Los lanzadores de permisos no cambian
    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) verificarPermisoOverlay();
        else Toast.makeText(this, "El permiso de notificaciones es vital.", Toast.LENGTH_LONG).show();
    });
    private final ActivityResultLauncher<Intent> overlayPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (Settings.canDrawOverlays(this)) verificarPermisoAlarma();
        else Toast.makeText(this, "El permiso de superposición es necesario.", Toast.LENGTH_SHORT).show();
    });

    // Archivo: NoticiasActivity.java

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noticias);

        // [INICIO DE LA CORRECCIÓN CLAVE]
        // Forzamos el reinicio del estado a Fase 1 cada vez que la actividad se crea.
        // Esto asegura que la experiencia del usuario siempre comience desde el principio.
        VigilanciaService.guardarFase(this, 1);
        Log.d("NoticiasActivity", "Estado FORZADO a Fase 1 al crear la actividad.");
        // [FIN DE LA CORRECCIÓN CLAVE]

        imageNoticiaGrande = findViewById(R.id.image_noticia_grande);
        imageNoticia1 = findViewById(R.id.image_noticia_1);
        imageNoticia2 = findViewById(R.id.image_noticia_2);
        imageNoticia3 = findViewById(R.id.image_noticia_3);

        setupBottomNavigation();
        verificarPermisosEIniciarServicios(); // Esto iniciará el servicio, que ahora leerá la fase 1.
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Al volver a la actividad, leemos la fase actual guardada por el servicio
        this.faseActual = VigilanciaService.getFaseGuardada(this);
        Log.d("NoticiasActivity", "Actividad resumida. Mostrando contenido para la fase " + this.faseActual);
        cargarContenidoDeLaFase();
    }

    // Ya no necesitamos onPause ni onDestroy para los handlers
    // tampoco el método inicializarContadorNoticias()

    // --- LA CADENA DE PERMISOS E INICIO SE SIMPLIFICA ---
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

    // El método detenerCiclos ya no es necesario aquí.

    // ========= LOS MÉTODOS DE LA UI SIGUEN IGUAL =========

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
        // Este método no cambia, solo quitamos la llamada a detenerCiclos()
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_noticias);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_noticias) return true;

            // Ya no es necesario detener nada, el servicio sigue vivo
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







