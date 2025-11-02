# USO DE CHAT GPT
## ALERTA RECEIBER
 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CANAL_ALERTAS_ID, "Alertas de la Aplicación", NotificationManager.IMPORTANCE_HIGH
            );

 .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
## FaseReceiber
gestorDeAlertas.detener();
broadcastIntent.putExtra("nuevaFase", nuevaFase);

## Gestor de Alertas
el texto dentro de estas listas:
private static final List<String> mensajesFase1 = Arrays.asList(
            "El gobierno informa de sucesos inusuales: objetos que se mueven sin explicación.",
            "Vecinos reportan luces intermitentes en el cielo nocturno.",
            "Se han detectado alteraciones electromagnéticas en varias ciudades."
    );
PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_ALERTAS,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
y el RTC_WAKEUP
## Reinicio Servicio Receiver
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
## Vigilancia Receiber
no hay codigo copiado como tal pero si le pregunte a chat gpt  para saber que me hacia falta en la app y me contesto que tenia que añadirle esto 

## Consejos Activity
el texto dentro de:
 consejo1.setText("Refúgiate en lugares con energía fuerte: iglesias, templos o zonas sagradas.");
                consejo2.setText("No confíes en lo que veas; las apariencias pueden ser ilusiones creadas por ellos.");
                consejo3.setText("Evita hacer ruido, los espíritus reaccionan a la vibración del sonido.");
                consejo4.setText("Si un espectro intenta poseerte, concéntrate en un recuerdo feliz y respira profundamente.");
                consejo5.setText("Recuerda: no todos los fantasmas son hostiles, algunos aún pueden ayudarte.");
y el onpause:
 @Override
    protected void onPause() {
        super.onPause();
        // Detiene el contador cuando la actividad no es visible para ahorrar recursos
        faseHandler.removeCallbacks(faseRunnable);
    }

## MapsActivity
el manejo de la ubicacion
 @Override
    public void onLocationChanged(@NonNull Location location) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(@NonNull String provider) {}
    @Override
    public void onProviderDisabled(@NonNull String provider) {}
y la capa de ubicacion
 this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        this.mLocationOverlay.enableMyLocation();
        this.mLocationOverlay.enableFollowLocation();
        this.mLocationOverlay.setDrawAccuracyEnabled(true);
tambien el como detecta las iglesias
BoundingBox boundingBox = map.getBoundingBox();
        String overpassQuery = "[out:json];" +
                "node[amenity=place_of_worship][religion=christian]" +
                "(" + boundingBox.getLatSouth() + "," + boundingBox.getLonWest() + "," +
                boundingBox.getLatNorth() + "," + boundingBox.getLonEast() + ");" +
                "out;";
## MenuActivity
dentro del solicitar permiso de notificaciones copie del chat gpt esto
.setTitle("Permiso de Notificaciones")
                        .setMessage("Esta aplicación necesita enviar notificaciones para alertarte. Por favor, concede el permiso.")
                        .setPositiveButton("Aceptar", (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            Toast.makeText(this, "Advertencia: Las alertas no funcionarán.", Toast.LENGTH_SHORT).show();
                            verificarPermisoBateriaEIniciarServicio();
                        })
## NoticiasActivity
private void verificarPermisosEIniciarServicios() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            verificarPermisoOverlay();
        }
    }
hice el primero pero ya el resto se lo pedi al chat gpt, soy un vago lo se...
case 3:
                imageNoticiaGrande.setImageResource(R.drawable.fase3_noticia_grande);
                imageNoticia1.setImageResource(R.drawable.fase3_noticia1);
                imageNoticia2.setImageResource(R.drawable.fase3_noticia_2);
                imageNoticia3.setImageResource(R.drawable.fase3_noticia3);
                break;
y dento del onstart 
} else {
            registerReceiver(faseChangeReceiver, new IntentFilter(FaseReceiver.ACTION_FASE_CAMBIADA));
        }

