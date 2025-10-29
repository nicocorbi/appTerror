

package com.example.appterror.controller;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.appterror.R; // Asegúrate de tener un icono en res/drawable

import java.util.ArrayList;
import java.util.Random;

public class AlertaReceiver extends BroadcastReceiver {

    public static final String CANAL_ALERTAS_ID = "CanalDeAlertas";
    private static int idNotificacionActual = 200; // ID inicial para que no choque con el del servicio

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlertaReceiver", "¡Alarma recibida! Procesando notificación.");

        // 1. Recuperamos la lista de mensajes que nos envió el GestorDeAlertas
        ArrayList<String> mensajes = intent.getStringArrayListExtra("mensajes");

        // 2. Verificamos que la lista no esté vacía
        if (mensajes != null && !mensajes.isEmpty()) {
            // 3. Seleccionamos un mensaje al azar de la lista
            String mensajeSeleccionado = mensajes.get(new Random().nextInt(mensajes.size()));

            // 4. Enviamos la notificación con el mensaje seleccionado
            enviarNotificacionDeAlerta(context, "Actividad Anómala Detectada", mensajeSeleccionado);
        } else {
            Log.w("AlertaReceiver", "No se encontraron mensajes en el Intent.");
        }
    }

    private void enviarNotificacionDeAlerta(Context context, String titulo, String contenido) {
        crearCanalDeNotificaciones(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL_ALERTAS_ID)
                .setSmallIcon(R.drawable.ic_alerta_notificacion)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contenido))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // --- INICIO DE LA CORRECCIÓN ---
        // Añadimos la comprobación de permiso obligatoria para Android 13 (API 33) y superior.
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Si no tenemos permiso, no podemos mostrar la notificación.
            // En un BroadcastReceiver, no podemos solicitar el permiso aquí, así que solo lo registramos en el log.
            // Es crucial que el permiso se solicite en tu Activity (LoginActivity).
            Log.w("AlertaReceiver", "No se puede mostrar la notificación porque falta el permiso POST_NOTIFICATIONS.");
            return; // Salimos del método para no intentar mostrar la notificación.
        }
        // Si tenemos permiso, la línea de abajo se ejecuta sin error.
        notificationManager.notify(idNotificacionActual++, builder.build());
        // --- FIN DE LA CORRECCIÓN ---
    }

    private void crearCanalDeNotificaciones(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canalAlertas = new NotificationChannel(
                    CANAL_ALERTAS_ID,
                    "Alertas de Pánico",
                    NotificationManager.IMPORTANCE_HIGH // IMPORTANCE_HIGH es crucial para la visibilidad
            );
            canalAlertas.setDescription("Notificaciones críticas sobre actividad anómala.");
            canalAlertas.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canalAlertas);
            }
        }
    }
}
