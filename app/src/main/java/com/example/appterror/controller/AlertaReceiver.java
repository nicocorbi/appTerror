// Archivo: AlertaReceiver.java (MODIFICADO)
package com.example.appterror.controller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.appterror.R;

import java.util.ArrayList;
import java.util.Random;

public class AlertaReceiver extends BroadcastReceiver {

    public static final String CANAL_ALERTAS_ID = "CanalDeAlertas";
    private static int notificationIdCounter = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlertaReceiver", "¡Alarma exacta recibida! Mostrando notificación.");

        // 1. OBTENER DATOS Y MOSTRAR NOTIFICACIÓN (tu lógica actual)
        ArrayList<String> mensajes = intent.getStringArrayListExtra("mensajes");
        int faseActual = intent.getIntExtra("faseActual", 1);

        if (mensajes == null || mensajes.isEmpty()) {
            Log.e("AlertaReceiver", "No se recibieron mensajes para la notificación.");
            return;
        }

        String mensajeNotificacion = mensajes.get(new Random().nextInt(mensajes.size()));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear canal de notificación (necesario para Android 8.0 y superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CANAL_ALERTAS_ID, "Alertas de la Aplicación", NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones de alerta sobre eventos en la aplicación.");
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, CANAL_ALERTAS_ID)
                .setSmallIcon(R.drawable.ic_alerta_notificacion)
                .setContentTitle("ALERTA INMINENTE (Fase " + faseActual + ")")
                .setContentText(mensajeNotificacion)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensajeNotificacion))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(notificationIdCounter++, notification);

        // 2. [LA CLAVE] REPROGRAMAR LA SIGUIENTE ALARMA EN LA CADENA
        long proximaAlarmaMillis = System.currentTimeMillis() + GestorDeAlertas.INTERVALO_ALERTAS;
        Log.d("AlertaReceiver", "Reprogramando siguiente alerta para la fase " + faseActual);
        GestorDeAlertas.programarProximaAlarma(context, faseActual, proximaAlarmaMillis);
    }
}

