// Archivo: FaseReceiver.java (VERSIÓN CORREGIDA)
package com.example.appterror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FaseReceiver extends BroadcastReceiver {
    private static final int TOTAL_FASES = 4;

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

        //  Envia el aviso, INCLUYENDO la nueva fase como dato.
        Intent broadcastIntent = new Intent(ACTION_FASE_CAMBIADA);
        broadcastIntent.putExtra("nuevaFase", nuevaFase);
        context.sendBroadcast(broadcastIntent);
        Log.d("FaseReceiver", "Broadcast ACTION_FASE_CAMBIADA enviado con la nueva fase: " + nuevaFase);

        long proximoCambio = System.currentTimeMillis() + VigilanciaService.INTERVALO_FASE;
        VigilanciaService.programarProximoCambioDeFase(context, proximoCambio);
    }
}




