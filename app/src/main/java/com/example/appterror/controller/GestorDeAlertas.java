package com.example.appterror.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GestorDeAlertas {

    private final Context context;
    private final AlarmManager alarmManager;

    // Los mensajes se gestionan aquí
    private final List<String> mensajesFase1 = Arrays.asList(
            "El gobierno informa de sucesos inusuales: objetos que se mueven sin explicación.",
            "Vecinos reportan luces intermitentes en el cielo nocturno.",
            "Se han detectado alteraciones electromagnéticas en varias ciudades."
    );
    private final List<String> mensajesFase2 = Arrays.asList(
            "ALERTA: Se ha perdido el contacto con la estación espacial internacional.",
            "ÚLTIMA HORA: Fallo masivo en los sistemas de satélites GPS a nivel global.",
            "AVISO: Las comunicaciones por radio están sufriendo interferencias desconocidas."
    );

    public GestorDeAlertas(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void iniciar(int faseActual) {
        Log.d("GestorDeAlertas", "Iniciando alarmas para la fase " + faseActual);

        List<String> mensajes = (faseActual == 1) ? mensajesFase1 : mensajesFase2;

        // Creamos el Intent que se enviará cuando suene la alarma
        Intent intent = new Intent(context, AlertaReceiver.class);
        intent.putStringArrayListExtra("mensajes", new ArrayList<>(mensajes));

        // Creamos el PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0, // Código de solicitud único
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Programamos la alarma para que se repita cada 10 segundos
        long intervalo = 10000; // 10 segundos
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + intervalo, // Primera alarma en 10 segundos
                intervalo,
                pendingIntent
        );
    }

    public void detener() {
        Log.d("GestorDeAlertas", "Deteniendo todas las alarmas.");
        Intent intent = new Intent(context, AlertaReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}




