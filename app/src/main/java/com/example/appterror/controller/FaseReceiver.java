// Archivo: FaseReceiver.java (NUEVO)
package com.example.appterror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FaseReceiver extends BroadcastReceiver {
    private static final int TOTAL_FASES = 2;

    // En FaseReceiver.java
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("FaseReceiver", "¡Alarma de cambio de fase recibida!");

        int faseActual = VigilanciaService.getFaseGuardada(context);
        int nuevaFase = (faseActual % TOTAL_FASES) + 1;
        VigilanciaService.guardarFase(context, nuevaFase);

        Log.d("FaseReceiver", "CAMBIO DE FASE GLOBAL. Nueva fase: " + nuevaFase);

        // [LA LÓGICA IMPORTANTE ESTÁ AQUÍ]
        // Reinicia la cadena de alertas para la nueva fase
        GestorDeAlertas gestorDeAlertas = new GestorDeAlertas(context);
        gestorDeAlertas.detener(); // Detiene las alarmas de la fase vieja
        gestorDeAlertas.iniciar(nuevaFase); // Inicia las alarmas de la nueva fase

        // Vuelve a programar el siguiente cambio de fase
        long proximoCambio = System.currentTimeMillis() + VigilanciaService.INTERVALO_FASE;
        VigilanciaService.programarProximoCambioDeFase(context, proximoCambio);

    }
}


