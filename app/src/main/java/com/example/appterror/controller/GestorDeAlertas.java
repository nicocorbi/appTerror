
// ruta: app/src/main/java/com/example/appterror/controller/GestorDeAlertas.java
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

    private static final String TAG = "GestorDeAlertas";
    private final Context context;
    private final AlarmManager alarmManager;

    // --- [NUEVO] Instancia del gestor musical ---
    private final GestorMusical gestorMusical;

    private static final int REQUEST_CODE_ALERTAS = 1001;
    public static final long INTERVALO_ALERTAS = 30 * 60 * 1000L;
    // Listas de strings con los textos de las alertas para cada fase
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
            "ÚLTIMA HORA: Barcelona ha caido y no por los fantasmas...",
            "AVISO: Se estan reportando numerosos casos de desapariciones ."
    );
    private static final List<String> mensajesFase4 = Arrays.asList(
            "ALERTA: vamos a por ti .",
            "ÚLTIMA HORA: te estamos viendo.",
            "AVISO: no podras escapar."
    );
    // Constructor de la clase GestorDeAlertas que recibe el contexto de la aplicación como parámetro y inicializa el AlarmManager.
    public GestorDeAlertas(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // --- [NUEVO] Inicializamos el GestorMusical ---
        this.gestorMusical = new GestorMusical(this.context);
    }
    // Método estático para obtener los mensajes correspondientes a una fase
    public static List<String> getMensajesPorFase(int fase) {
        switch (fase) {
            case 1: return mensajesFase1;
            case 2: return mensajesFase2;
            case 3: return mensajesFase3;
            case 4: return mensajesFase4;
            default:
                Log.w(TAG, "getMensajesPorFase: Fase desconocida (" + fase + "). Devolviendo mensajes de Fase 1.");
                return mensajesFase1;
        }
    }
    // Método para iniciar el gestor de alertas con la fase actual especificada
    public void iniciar(int faseActual) {
        Log.d(TAG, "Iniciando gestor para la fase " + faseActual);


        // Detiene cualquier ciclo anterior para evitar solapamientos
        gestorMusical.detener();
        // Si estamos en la fase 4, inicia el ciclo de música.
        if (faseActual == 4) {
            Log.d(TAG, "Fase 4 detectada. Iniciando gestor musical.");
            gestorMusical.iniciar();
        }

        // --- La lógica de notificaciones se mantiene igual ---
        Log.d(TAG, "Programando PRIMERA alarma de notificación para la fase " + faseActual);
        programarProximaAlarma(context, faseActual, System.currentTimeMillis() + INTERVALO_ALERTAS);
    }
    // Método estático para programar la próxima alarma de notificación con la fase actual especificada
    public static void programarProximaAlarma(Context context, int faseActual, long triggerAtMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                Log.w(TAG, "No se pueden programar alarmas exactas. Usando alarma inexacta.");
                am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
        Log.d(TAG, "Próxima alarma de notificación programada para la fase " + faseActual);
    }
    // Método para detener el gestor de alertas y cancelar todas las alarmas
    public void detener() {
        Log.d(TAG, "Deteniendo todos los procesos del gestor de alertas.");


        // Detiene el ciclo de música
        gestorMusical.detener();

        // --- La lógica para detener notificaciones se mantiene igual ---
        Intent intent = new Intent(context, AlertaReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_ALERTAS,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null && alarmManager != null) {
            Log.d(TAG, "Cancelando alarmas de notificación pendientes.");
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}





