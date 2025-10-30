// Archivo: VigilanciaService.java (VERSIÓN LIMPIA Y FINAL)
package com.example.appterror.controller;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.appterror.R;

public class VigilanciaService extends Service {

    public static final String CANAL_SERVICIO_ID = "CanalDeVigilancia";
    public static final long INTERVALO_FASE = 90 * 60 * 1000L;
    private static final int REQUEST_CODE_FASE = 2002;

    private GestorDeAlertas gestorDeAlertas;

    private static final String PREFS_NAME = "EstadoAppTerror";
    private static final String KEY_FASE_ACTUAL = "faseActual";

    @Override
    public void onCreate() {
        super.onCreate();
        gestorDeAlertas = new GestorDeAlertas(this);
        crearCanalDeNotificacion();
        Log.d("VigilanciaService", "Servicio Creado.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("VigilanciaService", "Servicio Iniciado.");
        mostrarNotificacionPrimerPlano();

        // [CORRECCIÓN CLAVE]: Forzamos que la fase sea 1 en el PRIMER arranque del servicio.
        // Si el servicio ya se estaba ejecutando, esto no tiene efecto negativo.
        // Pero si es la primera vez (después de instalar o borrar datos), asegura Fase 1.
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!prefs.contains(KEY_FASE_ACTUAL)) {
            guardarFase(this, 1);
        }

        int faseActualGuardada = getFaseGuardada(this);

        // Inicia las cadenas de alarmas con la fase correcta.
        iniciarCadenasDeAlarmas(faseActualGuardada);

        return START_STICKY;
    }

    private void iniciarCadenasDeAlarmas(int faseInicial) {
        // 1. Inicia INMEDIATAMENTE la cadena de alertas con la fase actual.
        //    Esto asegura que desde el segundo cero haya alertas de la fase correcta (Fase 1 al inicio).
        gestorDeAlertas.iniciar(faseInicial);
        Log.d("VigilanciaService", "Cadena de ALARMAS iniciada INMEDIATAMENTE para la fase " + faseInicial);

        // 2. Programa el PRIMER cambio de fase para que ocurra en 30 segundos.
        //    El FaseReceiver se encargará de los cambios posteriores.
        long proximoCambio = System.currentTimeMillis() + INTERVALO_FASE;
        programarProximoCambioDeFase(this, proximoCambio);
        Log.d("VigilanciaService", "PRIMER CAMBIO DE FASE programado para dentro de 30 segundos.");
    }

    public static void programarProximoCambioDeFase(Context context, long triggerAtMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, FaseReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_FASE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Comprobación de versión para compatibilidad
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gestorDeAlertas != null) {
            gestorDeAlertas.detener();
        }
        // Cancelar la alarma de cambio de fase
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, FaseReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, REQUEST_CODE_FASE, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
        Log.d("VigilanciaService", "Servicio Destruido y cadenas de alarmas detenidas.");
    }

    // --- MÉTODOS DE SOPORTE (SharedPreferences) ---
    public static void guardarFase(Context context, int fase) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_FASE_ACTUAL, fase).apply();
    }

    public static int getFaseGuardada(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Devuelve 1 como valor por defecto si la clave no existe.
        return prefs.getInt(KEY_FASE_ACTUAL, 1);
    }

    // --- MÉTODOS DE SOPORTE (Notificaciones) ---
    private void mostrarNotificacionPrimerPlano() {
        Notification notification = new NotificationCompat.Builder(this, CANAL_SERVICIO_ID)
                .setContentTitle("Modo Vigilancia Activado")
                .setContentText("Analizando anomalías en segundo plano.")
                .setSmallIcon(R.drawable.ic_alerta_notificacion) // Asegúrate de tener este icono
                .build();
        startForeground(1, notification);
    }

    private void crearCanalDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CANAL_SERVICIO_ID,
                    "Servicio de Vigilancia",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}








