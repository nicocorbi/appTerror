// Archivo: GestorDeAlertas.java (VERSIÓN FINAL Y CORREGIDA)
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
    public static final long INTERVALO_ALERTAS = 30 * 60 * 1000L;

    // --- [CORRECCIÓN 1]: Las listas de mensajes ahora son 'static final' ---
    // Esto permite que el método estático 'getMensajesPorFase' acceda a ellas.
    private static final List<String> mensajesFase1 = Arrays.asList(
            "El gobierno informa de sucesos inusuales: objetos que se mueven sin explicación.",
            "Vecinos reportan luces intermitentes en el cielo nocturno.",
            "Se han detectado alteraciones electromagnéticas en varias ciudades."
    );
    private static final List<String> mensajesFase2 = Arrays.asList(
            "ALERTA: Se ha perdido el contacto con la estación espacial internacional.",
            "ÚLTIMA HORA: Fallo masivo en los sistemas de satélites GPS a nivel global.",
            "AVISO: Las comunicaciones por radio están sufriendo interferencias desconocidas."
    );
    private static final List<String> mensajesFase3 = Arrays.asList(
            "ALERTA:Varias personas estan actuando extraño y estan atacando a personas al azar.",
            "ÚLTIMA HORA: Barcelona ha caido, alejaros de las zonas de peligro.",
            "AVISO: Se estan reportando numerosos casos de desapariciones ."
    );
    private static final List<String> mensajesFase4 = Arrays.asList(
            "ALERTA: vamos a por ti .",
            "ÚLTIMA HORA: te estamos viendo.",
            "AVISO: no podras escapar."
    );

    public GestorDeAlertas(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    // --- [NUEVO MÉTODO PÚBLICO Y ESTÁTICO] ---
    /**
     * Devuelve la lista de mensajes correspondiente a una fase específica.
     * Al ser 'public static', puede ser llamado desde cualquier parte de la app (ej: MenuActivity)
     * sin necesidad de crear una instancia de GestorDeAlertas.
     * @param fase La fase de la que se quieren obtener los mensajes.
     * @return Una lista de Strings con los mensajes.
     */
    public static List<String> getMensajesPorFase(int fase) {
        switch (fase) {
            case 1: return mensajesFase1;
            case 2: return mensajesFase2;
            case 3: return mensajesFase3;
            case 4: return mensajesFase4;
            default:
                Log.w("GestorDeAlertas", "getMensajesPorFase: Fase desconocida (" + fase + "). Devolviendo mensajes de Fase 1.");
                return mensajesFase1;
        }
    }


    public void iniciar(int faseActual) {
        Log.d("GestorDeAlertas", "Programando PRIMERA alarma exacta para la fase " + faseActual);
        // Llama al método para programar la primera alarma de la cadena.
        programarProximaAlarma(context, faseActual, System.currentTimeMillis() + INTERVALO_ALERTAS);
    }


    public static void programarProximaAlarma(Context context, int faseActual, long triggerAtMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // --- [CORRECCIÓN 2]: Simplificamos la obtención de mensajes ---
        // Ahora usamos el nuevo método estático. Es más limpio y reutiliza la lógica.
        List<String> mensajes = getMensajesPorFase(faseActual);

        Intent intent = new Intent(context, AlertaReceiver.class);
        intent.putStringArrayListExtra("mensajes", new ArrayList<>(mensajes));
        intent.putExtra("faseActual", faseActual);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_ALERTAS,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // La lógica para programar la alarma se mantiene igual.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                Log.w("GestorDeAlertas", "No se pueden programar alarmas exactas. Usando alarma inexacta.");
                am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
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






