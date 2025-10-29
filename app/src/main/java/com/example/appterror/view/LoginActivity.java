// Archivo: LoginActivity.java (Modificado)
package com.example.appterror.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appterror.R;

public class LoginActivity extends AppCompatActivity {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userNameEditText = findViewById(R.id.userName);
        passwordEditText = findViewById(R.id.TextPassword);
        startButton = findViewById(R.id.start);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = userNameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (usuario.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Debes rellenar el campo de usuario", Toast.LENGTH_SHORT).show();
                    userNameEditText.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    return;
                } else {
                    userNameEditText.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                }

                if (password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Debes rellenar el campo de contraseña", Toast.LENGTH_SHORT).show();
                    passwordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    return;
                } else {
                    passwordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                }

                if (!isAlphanumeric(usuario)) {
                    Toast.makeText(LoginActivity.this, "El usuario solo puede contener letras y números", Toast.LENGTH_SHORT).show();
                    userNameEditText.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    return;
                }

                if (!isAlphanumeric(password)) {
                    Toast.makeText(LoginActivity.this, "La contraseña solo puede contener letras y números", Toast.LENGTH_SHORT).show();
                    passwordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    return;
                }

                if (password.length() < 4 || password.length() > 10) {
                    Toast.makeText(LoginActivity.this, "La contraseña debe tener entre 4 y 10 caracteres", Toast.LENGTH_SHORT).show();
                    passwordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    return;
                }

                testLogin(usuario, password);
            }
        });
    }

    private boolean isAlphanumeric(String texto) {
        return texto.matches("[a-zA-Z0-9]+");
    }

    void testLogin(String user, String pass) {
        Toast.makeText(LoginActivity.this, "¡Bienvenido " + user + "!", Toast.LENGTH_SHORT).show();

        Bundle data = new Bundle();
        data.putString("userNameKey", user);
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        intent.putExtras(data);
        startActivity(intent);
    }
}
