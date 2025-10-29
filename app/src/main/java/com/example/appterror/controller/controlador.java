package com.example.appterror.controller;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appterror.R;

public class controlador {
    public String encriptarCesar(String textoPlano, int desplazamiento) {


        String textoCifrado = "";

        for (char caracter : textoPlano.toCharArray()) {
            if (Character.isUpperCase(caracter)) {
                char base = 'A';
                textoCifrado += (char) (((caracter - base + desplazamiento) % 26) + base);
            } else if (Character.isLowerCase(caracter)) {
                char base = 'a';
                textoCifrado += (char) (((caracter - base + desplazamiento) % 26) + base);
            } else if (Character.isDigit(caracter)) {
                char base = '0';
                textoCifrado += (char) (((caracter - base + desplazamiento) % 10) + base);
            } else {
                textoCifrado += caracter;
            }
        }

        return textoCifrado;
    }

}
