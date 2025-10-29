package com.example.appterror.controller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.appterror.R;

public class VigilanciaService extends Service {

    public static final String CHANNEL_ID = "VigilanciaServiceChannel";
    public static final int NOTIFICATION_ID = 3;

    private GestorDeAlertas gestorDeAlertas; // Añadimos la referencia al gestor

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializamos el gestor y creamos el canal de notificación al crear el servicio
        gestorDeAlertas = new GestorDeAlertas(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("VigilanciaService", "Servicio iniciado.");

        // 1. Creamos y mostramos la notificación permanente para convertirlo en Foreground Service
        crearNotificacionPermanente();

        // 2. Usamos el GestorDeAlertas para programar las alarmas repetitivas
        // Suponemos que la "fase" viene en el Intent, o usamos 1 por defecto.
        int faseActual = (intent != null) ? intent.getIntExtra("faseActual", 1) : 1;
        Log.d("VigilanciaService", "Programando alertas para la fase: " + faseActual);
        gestorDeAlertas.iniciar(faseActual);

        // 3. START_STICKY intentará reiniciar el servicio si el sistema lo mata (por memoria, no por el usuario)
        return START_STICKY;
    }

    private void crearNotificacionPermanente() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Modo Pánico Activado")
                .setContentText("La protección en segundo plano está activa.")
                .setSmallIcon(R.drawable.ic_alerta_notificacion) // Asegúrate de tener este icono
                .setPriority(NotificationCompat.PRIORITY_MIN) // Prioridad baja para no molestar
                .setOngoing(true) // Hace que no se pueda deslizar para cerrarla
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("VigilanciaService", "El servicio está siendo destruido. Deteniendo alarmas.");
        // MUY IMPORTANTE: Cuando el servicio se detiene, cancelamos las alarmas
        // para no dejar "alarmas huérfanas" que intenten ejecutarse indefinidamente.
        if (gestorDeAlertas != null) {
            gestorDeAlertas.detener();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Servicio de Vigilancia",
                    NotificationManager.IMPORTANCE_MIN); // IMPORTANCE_MIN para que sea silenciosa
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}




