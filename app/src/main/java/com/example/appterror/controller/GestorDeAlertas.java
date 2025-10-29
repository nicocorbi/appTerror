// Archivo: GestorDeAlertas.java (MODIFICADO)
package com.example.appterror.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GestorDeAlertas {

    private final Context context;
    private final AlarmManager alarmManager;

    private static final int REQUEST_CODE_ALERTAS = 1001;
    public static final long INTERVALO_ALERTAS = 10000; // 10 segundos

    // Los mensajes se gestionan aquí (sin cambios)
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
        Log.d("GestorDeAlertas", "Programando PRIMERA alarma exacta para la fase " + faseActual);
        // Llama al método para programar la primera alarma de la cadena.
        // El resto se programarán en cadena desde AlertaReceiver.
        programarProximaAlarma(context, faseActual, System.currentTimeMillis() + INTERVALO_ALERTAS);
    }

    // En GestorDeAlertas.java

    public static void programarProximaAlarma(Context context, int faseActual, long triggerAtMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        List<String> mensajes = (faseActual == 1)
                ? new GestorDeAlertas(context).mensajesFase1
                : new GestorDeAlertas(context).mensajesFase2;

        Intent intent = new Intent(context, AlertaReceiver.class);
        intent.putStringArrayListExtra("mensajes", new ArrayList<>(mensajes));
        intent.putExtra("faseActual", faseActual);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_ALERTAS,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // [INICIO DE LA CORRECIÓN]
        // Añadimos la misma comprobación de versión que en VigilanciaService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Si estamos en Android 12+, PREGUNTAMOS si tenemos el permiso
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else {
            // Si estamos en una versión ANTERIOR a Android 12, podemos llamar directamente
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
        // [FIN DE LA CORRECIÓN]
    }


    public void detener() {
        Log.d("GestorDeAlertas", "Deteniendo cadena de alarmas de alerta.");
        Intent intent = new Intent(context, AlertaReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_ALERTAS,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}





