// Archivo: MenuActivity.java (Modificado para incluir permisos)
package com.example.appterror.view;

// --- INICIO DE IMPORTS AÑADIDOS ---
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.example.appterror.controller.VigilanciaService;
// --- FIN DE IMPORTS AÑADIDOS ---

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appterror.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MenuActivity extends AppCompatActivity {

    private TextView userNameMenuTextView;
    private Button buttonPuntosSeguros, buttonUltimasNoticias, buttonConsejos;

    // --- INICIO DE CÓDIGO AÑADIDO PARA PERMISOS ---
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // El usuario concedió el permiso de notificaciones. Ahora verificamos la batería.
                    verificarPermisoBateriaEIniciarServicio();
                } else {
                    // El usuario denegó el permiso. Informamos que la funcionalidad estará limitada.
                    Toast.makeText(this, "Las notificaciones de alerta no funcionarán sin este permiso.", Toast.LENGTH_LONG).show();
                    // Aún así, intentamos verificar la batería e iniciar el servicio.
                    verificarPermisoBateriaEIniciarServicio();
                }
            });
    // --- FIN DE CÓDIGO AÑADIDO PARA PERMISOS ---


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // --- INICIO DE LLAMADA A LA LÓGICA DE PERMISOS ---
        // Justo al crear la actividad, antes de configurar la UI, iniciamos la verificación.
        solicitarPermisoDeNotificaciones();
        // --- FIN DE LLAMADA A LA LÓGICA DE PERMISOS ---


        // --- CÓDIGO PARA MOSTRAR EL NOMBRE DE USUARIO (SIN CAMBIOS) ---
        userNameMenuTextView = findViewById(R.id.userNameMenu);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String nombreUsuarioRecibido = extras.getString("userNameKey");
            if (nombreUsuarioRecibido != null && !nombreUsuarioRecibido.isEmpty()) {
                userNameMenuTextView.setText("Hi! " + nombreUsuarioRecibido);
            } else {
                userNameMenuTextView.setText("Hi! Invitado");
            }
        }

        // --- LÓGICA PARA LOS NUEVOS BOTONES (SIN CAMBIOS) ---
        buttonPuntosSeguros = findViewById(R.id.button_puntos_seguros);
        buttonUltimasNoticias = findViewById(R.id.button_ultimas_noticias);
        buttonConsejos = findViewById(R.id.button_consejos);

        buttonPuntosSeguros.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, MapsActivity.class));
        });

        buttonUltimasNoticias.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, NoticiasActivity.class));
        });

        buttonConsejos.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, ConsejosActivity.class));
        });

        // --- CÓDIGO DE NAVEGACIÓN INFERIOR (SIN CAMBIOS) ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_consejos) {
                startActivity(new Intent(getApplicationContext(), ConsejosActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_noticias) {
                startActivity(new Intent(getApplicationContext(), NoticiasActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_maps) {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_home) {
                return true;
            }
            return false;
        });
    }

    // --- INICIO DE MÉTODOS AÑADIDOS PARA GESTIONAR PERMISOS E INICIAR SERVICIO ---
    private void solicitarPermisoDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                verificarPermisoBateriaEIniciarServicio();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Permiso de Notificaciones")
                        .setMessage("Esta aplicación necesita enviar notificaciones para alertarte sobre actividad anómala, incluso si está cerrada. Por favor, concede el permiso.")
                        .setPositiveButton("Aceptar", (dialog, which) ->
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            Toast.makeText(this, "Advertencia: Las alertas no funcionarán.", Toast.LENGTH_SHORT).show();
                            verificarPermisoBateriaEIniciarServicio();
                        })
                        .show();
            }
        } else {
            verificarPermisoBateriaEIniciarServicio();
        }
    }

    private void verificarPermisoBateriaEIniciarServicio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permiso Adicional Requerido")
                        .setMessage("Para garantizar que las alertas funcionen en todo momento, es necesario desactivar la optimización de batería para esta app. ¿Desea ir a la configuración ahora?")
                        .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            Toast.makeText(this, "Advertencia: Las alertas pueden no funcionar correctamente en segundo plano.", Toast.LENGTH_LONG).show();
                            iniciarMiServicio();
                        })
                        .show();
            } else {
                iniciarMiServicio();
            }
        } else {
            iniciarMiServicio();
        }
    }

    private void iniciarMiServicio() {
        Intent serviceIntent = new Intent(this, VigilanciaService.class);
        serviceIntent.putExtra("faseActual", 1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    // --- FIN DE MÉTODOS AÑADIDOS ---
}



