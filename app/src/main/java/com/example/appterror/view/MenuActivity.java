package com.example.appterror.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.appterror.R;
import com.example.appterror.controller.VigilanciaService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MenuActivity extends AppCompatActivity {

    private TextView userNameMenuTextView;
    private Button buttonPuntosSeguros, buttonUltimasNoticias, buttonConsejos;
    private Button buttonForzarAlerta, buttonCambiarFase;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    verificarPermisoBateriaEIniciarServicio();
                } else {
                    Toast.makeText(this, "Las notificaciones no funcionarán sin este permiso.", Toast.LENGTH_LONG).show();
                    verificarPermisoBateriaEIniciarServicio();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        solicitarPermisoDeNotificaciones();

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

        buttonPuntosSeguros = findViewById(R.id.button_puntos_seguros);
        buttonUltimasNoticias = findViewById(R.id.button_ultimas_noticias);
        buttonConsejos = findViewById(R.id.button_consejos);

        buttonPuntosSeguros.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, MapsActivity.class)));
        buttonUltimasNoticias.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, NoticiasActivity.class)));
        buttonConsejos.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, ConsejosActivity.class)));

        // --- LÓGICA PARA BOTONES DE DESARROLLADOR ---
        buttonForzarAlerta = findViewById(R.id.button_dev_forzar_alerta);
        buttonCambiarFase = findViewById(R.id.button_dev_cambiar_fase);

        buttonForzarAlerta.setOnClickListener(v -> {
            Log.d("MenuActivity_Dev", "Botón 'Forzar Alerta' pulsado.");
            forzarAlertaAhora();
            Toast.makeText(this, "Alerta manual generada.", Toast.LENGTH_SHORT).show();
        });

        //  El botón solo cambia la fase y te mantiene en la pantalla actual.
        buttonCambiarFase.setOnClickListener(v -> {
            Log.d("MenuActivity_Dev", "Botón 'Cambiar Fase' pulsado.");
            forzarCambioDeFase();
            Toast.makeText(this, "Fase cambiada en segundo plano.", Toast.LENGTH_SHORT).show();
        });

        setupBottomNavigation();
    }

    /**
     * Lanza una notificación de alerta inmediatamente usando los mensajes reales del GestorDeAlertas.
     */
    private void forzarAlertaAhora() {
        // 1. Obtenemos la fase actual, como antes.
        int faseActual = VigilanciaService.getFaseGuardada(this);

        // 2. Creamos una instancia del GestorDeAlertas.
        //    Es necesario usar el nombre completo del paquete si hay ambigüedad.
        com.example.appterror.controller.GestorDeAlertas gestor = new com.example.appterror.controller.GestorDeAlertas(this);

        //Usamos el método para obtener la lista de mensajes REALES.
        java.util.List<String> mensajesReales = gestor.getMensajesPorFase(faseActual);

        //  Preparamos y enviamos el broadcast al AlertaReceiver con los mensajes correctos.
        Intent intentAlerta = new Intent(this, com.example.appterror.controller.AlertaReceiver.class);
        intentAlerta.putExtra("faseActual", faseActual);
        intentAlerta.putStringArrayListExtra("mensajes", new java.util.ArrayList<>(mensajesReales));
        sendBroadcast(intentAlerta);

        Log.d("MenuActivity_Dev", "Alerta manual forzada para la fase " + faseActual + " con mensajes reales.");
    }


    private void forzarCambioDeFase() {
        Intent intentFase = new Intent(this, com.example.appterror.controller.FaseReceiver.class);
        sendBroadcast(intentFase);
    }


    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            }
            Intent intent = null;
            if (itemId == R.id.navigation_consejos) {
                intent = new Intent(getApplicationContext(), ConsejosActivity.class);
            } else if (itemId == R.id.navigation_noticias) {
                intent = new Intent(getApplicationContext(), NoticiasActivity.class);
            } else if (itemId == R.id.navigation_maps) {
                intent = new Intent(getApplicationContext(), MapsActivity.class);
            }
            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
            return true;
        });
    }

    private void solicitarPermisoDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setTitle("Permiso de Notificaciones")
                        .setMessage("Esta aplicación necesita enviar notificaciones para alertarte. Por favor, concede el permiso.")
                        .setPositiveButton("Aceptar", (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            Toast.makeText(this, "Advertencia: Las alertas no funcionarán.", Toast.LENGTH_SHORT).show();
                            verificarPermisoBateriaEIniciarServicio();
                        })
                        .show();
            } else {
                verificarPermisoBateriaEIniciarServicio();
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
                        .setMessage("Para garantizar que las alertas funcionen siempre, desactiva la optimización de batería para esta app.")
                        .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            Toast.makeText(this, "Advertencia: Las alertas pueden no funcionar en segundo plano.", Toast.LENGTH_LONG).show();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}




