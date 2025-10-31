// Archivo: GestorMusical.java
package com.example.appterror.controller;

import android.content.Context;
import android.media.MediaPlayer;import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.appterror.R;

public class GestorMusical {

    private static final String TAG = "GestorMusical";
    private static final long INTERVALO_REPRODUCCION = 10 * 60 * 1000L;
    private static final long DURACION_REPRODUCCION = 30 * 1000L;

    private final Context context;
    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    private final Runnable cicloReproduccion = new Runnable() {
        @Override
        public void run() {
            // Doble verificación: si el ciclo se detuvo mientras esperaba, no hacer nada.
            if (!isRunning) return;

            iniciarReproduccion();

            // Programar la detención después de 20 segundos
            handler.postDelayed(GestorMusical.this::detenerReproduccion, DURACION_REPRODUCCION);

            // Volver a ejecutar este ciclo en 30 segundos
            handler.postDelayed(this, INTERVALO_REPRODUCCION);
        }
    };

    public GestorMusical(Context context) {
        this.context = context.getApplicationContext();
    }

    public void iniciar() {

        if (isRunning) {
            Log.d(TAG, "El ciclo de música ya está iniciado. Se ignora la nueva llamada.");
            return;
        }
        isRunning = true;
        Log.d(TAG, "Iniciando ciclo de música para la fase 4.");
        // Ejecuta el ciclo inmediatamente en lugar de esperar.
        handler.post(cicloReproduccion);
    }

    public void detener() {
        if (!isRunning) return;
        isRunning = false;


        // Elimina CUALQUIER callback o mensaje pendiente en el handler.
        // Esto previene que se ejecuten tareas de inicio o detención que
        // ya estuvieran en la cola, evitando reproducciones duplicadas.
        handler.removeCallbacksAndMessages(null);

        detenerReproduccion(); // Detiene cualquier reproducción en curso
        Log.d(TAG, "Ciclo de música detenido y tareas pendientes canceladas.");
    }

    private void iniciarReproduccion() {
        // Detenemos y liberamos cualquier instancia anterior para asegurar un estado limpio.
        detenerReproduccion();

        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.cancion_fase_4);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> {
                    detenerReproduccion();
                });
                mediaPlayer.start();
                Log.d(TAG, "Reproducción iniciada.");
            } else {
                Log.e(TAG, "Error al crear MediaPlayer. ¿Falta el recurso de audio o es inválido?");
            }
        } catch (Exception e) {
            Log.e(TAG, "Excepción al iniciar la reproducción", e);
            mediaPlayer = null;
        }
    }

    private void detenerReproduccion() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                try {
                    mediaPlayer.stop();
                } catch (IllegalStateException e) {
                    Log.w(TAG, "MediaPlayer ya estaba detenido o en un estado inválido.", e);
                }
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "Recursos de MediaPlayer liberados.");
        }
    }
}


