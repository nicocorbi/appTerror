// Archivo: FaseReceiver.java (NUEVO)
package com.example.appterror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FaseReceiver extends BroadcastReceiver {
    private static final int TOTAL_FASES = 2;

    // LÍNEA 1 (NUEVA): Definimos un "nombre en clave" para el aviso.
    public static final String ACTION_FASE_CAMBIADA = "com.example.appterror.FASE_CAMBIADA";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("FaseReceiver", "¡Alarma de cambio de fase recibida!");

        int faseActual = VigilanciaService.getFaseGuardada(context);
        int nuevaFase = (faseActual % TOTAL_FASES) + 1;
        VigilanciaService.guardarFase(context, nuevaFase);

        Log.d("FaseReceiver", "CAMBIO DE FASE GLOBAL. Nueva fase: " + nuevaFase);

        GestorDeAlertas gestorDeAlertas = new GestorDeAlertas(context);
        gestorDeAlertas.detener();
        gestorDeAlertas.iniciar(nuevaFase);

        // LÍNEA 2 (NUEVA): Enviamos el aviso a todo el sistema con el nombre en clave.
        Intent broadcastIntent = new Intent(ACTION_FASE_CAMBIADA);
        context.sendBroadcast(broadcastIntent);

        long proximoCambio = System.currentTimeMillis() + VigilanciaService.INTERVALO_FASE;
        VigilanciaService.programarProximoCambioDeFase(context, proximoCambio);
    }
}



