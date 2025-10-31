package com.example.appterror.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appterror.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ConsejosActivity extends AppCompatActivity {

    // --- VARIABLES PARA EL SISTEMA DE FASES ---
    private int faseActual = 1;
    private final int TOTAL_FASES = 4; // Ajusta este número al total de fases que tendrás

    private TextView consejo1, consejo2, consejo3, consejo4, consejo5;

    // Handler y Runnable para el cambio automático de fase
    private final Handler faseHandler = new Handler(Looper.getMainLooper());
    private Runnable faseRunnable;
    private final long TIEMPO_DE_CAMBIO = 10000;
    /**
     * Se ejecuta al crear la actividad por primera vez. Se encarga de configurar la vista,
     * inicializar los componentes de la interfaz, preparar la lógica para el cambio de fases
     * y cargar el contenido inicial.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consejos);

        // --- INICIALIZACIÓN DE VISTAS ---
        consejo1 = findViewById(R.id.consejo_1);
        consejo2 = findViewById(R.id.consejo_2);
        consejo3 = findViewById(R.id.consejo_3);
        consejo4 = findViewById(R.id.consejo_4);
        consejo5 = findViewById(R.id.consejo_5);

        // --- LÓGICA DE FASES ---
        inicializarContadorDeFase();
        cargarContenidoDeLaFase();

        // --- CÓDIGO DE NAVEGACIÓN ---
        setupBottomNavigation();
    }
    /**
     * Define la tarea que se ejecutará repetidamente para cambiar de fase.
     * Incrementa el contador de fase, actualiza el contenido de la pantalla llamando a
     * cargarContenidoDeLaFase(), y se vuelve a programar a sí misma para ejecutarse
     * después del intervalo de tiempo definido.
     */

    private void inicializarContadorDeFase() {
        faseRunnable = new Runnable() {
            /**
             * Contiene la lógica que se ejecuta cada vez que el temporizador (Handler) se activa.
             * Avanza a la siguiente fase y actualiza la interfaz de usuario.
             */
            @Override
            public void run() {
                // Incrementa la fase y la reinicia si llega al final
                faseActual++;
                if (faseActual > TOTAL_FASES) {
                    faseActual = 1;
                }
                // Carga los nuevos consejos
                cargarContenidoDeLaFase();
                // Vuelve a programar la tarea
                faseHandler.postDelayed(this, TIEMPO_DE_CAMBIO);
            }
        };
    }
    /**
     * Se ejecuta cuando la actividad vuelve a ser visible para el usuario.
     * Inicia (o reanuda) el ciclo de cambio de fases programando la primera ejecución
     * del `faseRunnable`.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Inicia el contador cuando la actividad es visible
        faseHandler.postDelayed(faseRunnable, TIEMPO_DE_CAMBIO);
    }
    /**
     * Se ejecuta cuando la actividad deja de ser visible para el usuario.
     * Detiene el ciclo de cambio de fases para evitar que se ejecute en segundo plano
     * y así ahorrar recursos y batería.
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Detiene el contador cuando la actividad no es visible para ahorrar recursos
        faseHandler.removeCallbacks(faseRunnable);
    }
    /**
     * Actualiza los textos de los TextView en la pantalla según el valor de `faseActual`.
     * Utiliza una estructura `switch` para seleccionar y asignar los consejos correspondientes
     * a cada una de las cuatro fases.
     */

    private void cargarContenidoDeLaFase() {
        // Selecciona qué textos mostrar según la fase actual
        switch (faseActual) {
            case 1:
                consejo1.setText("Mantén siempre tus pertenencias a la vista.");
                consejo2.setText("Evita caminar solo por calles poco iluminadas.");
                consejo3.setText("Informa a alguien de confianza sobre tu ruta.");
                consejo4.setText("Revisa que las cerraduras y ventanas estén bien aseguradas.");
                consejo5.setText("Si notas algo fuera de lugar, repórtalo a las autoridades.");

                break;

            case 2:
                consejo1.setText("Evita mirar directamente a reflejos o sombras en movimiento.");
                consejo2.setText("Mantén una luz encendida durante la noche, especialmente en zonas oscuras.");
                consejo3.setText("No respondas si escuchas que alguien te llama por tu nombre y no ves a nadie.");
                consejo4.setText("Guarda objetos personales importantes cerca de ti, especialmente símbolos religiosos.");
                consejo5.setText("Permanece con otras personas; evita quedarte completamente solo.");

                break;

            case 3:
                consejo1.setText("No toques ni te acerques a personas que actúen de forma extraña o violenta.");
                consejo2.setText("Si escuchas voces en tu cabeza, repite una frase positiva para mantener el control.");
                consejo3.setText("Coloca sal en puertas y ventanas como medida de protección.");
                consejo4.setText("Evita lugares donde hayan ocurrido muertes recientes o rituales extraños.");
                consejo5.setText("Graba sonidos o videos si presencias algo; puede servir para identificar patrones.");

                break;

            case 4:
                consejo1.setText("Refúgiate en lugares con energía fuerte: iglesias, templos o zonas sagradas.");
                consejo2.setText("No confíes en lo que veas; las apariencias pueden ser ilusiones creadas por ellos.");
                consejo3.setText("Evita hacer ruido, los espíritus reaccionan a la vibración del sonido.");
                consejo4.setText("Si un espectro intenta poseerte, concéntrate en un recuerdo feliz y respira profundamente.");
                consejo5.setText("Recuerda: no todos los fantasmas son hostiles, algunos aún pueden ayudarte.");

                break;

            default:
                // Caso por defecto, carga la fase 1
                cargarContenidoDeLaFase();
                break;
        }
    }
    /**
     * Configura la barra de navegación inferior (BottomNavigationView).
     * Establece el ítem seleccionado por defecto y define el comportamiento para cada
     * clic en los ítems, permitiendo navegar entre las diferentes actividades
     * (Home, Consejos, Maps, Noticias).
     */

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_consejos);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MenuActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_consejos) {
                return true;
            } else if (itemId == R.id.navigation_maps) {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_noticias) {
                startActivity(new Intent(getApplicationContext(), NoticiasActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}

