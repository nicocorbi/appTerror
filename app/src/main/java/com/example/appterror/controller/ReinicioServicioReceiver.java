package com.example.appterror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ReinicioServicioReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Nos aseguramos de que el evento que nos despierta sea el de arranque completado.
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            Log.d("ReinicioServicio", "Dispositivo arrancado. Reiniciando VigilanciaService...");
            // Inicia el servicio de VigilanciaService con la fase 1 por defecto.
            Intent serviceIntent = new Intent(context, VigilanciaService.class);
            serviceIntent.putExtra("faseActual", 1); // Inicia con la fase por defecto
            // Comprobamos la versión de Android para determinar cómo iniciar el servicio.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
