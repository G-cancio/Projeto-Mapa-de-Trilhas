package com.example.mapadetrilhas;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.Locale;

public class RegistrarTrilhaActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private static final int REQUEST_LOCATION_UPDATES = 1;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    TrilhasDB trilhadb;

    private GoogleMap mMap;
    private Marker marcadorUsuario;
    private Circle circuloPrecisao;
    private BitmapDescriptor iconePersonalizado;

    // Elementos para desenhar a linha do percurso no mapa
    private Polyline rotaTrilha;
    private ArrayList<LatLng> listaPontosTrilha = new ArrayList<>();

    // Componentes da Interface (Views vinculadas ao seu XML)
    private TextView tvVelocidade, tvVelocidadeMax, tvCronometro, tvDistancia;
    private Button buttonRegistrar;

    // Variáveis de Controle da Trilha Real
    private boolean gravandoTrilha = false;
    private Location localizacaoAnterior = null;
    private float distanciaTotalPercorrida = 0f; // Em metros
    private float velocidadeMaximaRegistrada = 0f; // Em km/h

    // Variáveis do Cronômetro
    private int segundosTranscorridos = 0;
    private Handler cronometroHandler;
    private Runnable cronometroRunnable;

    // Variáveis globais para reter o tempo inicial entre os cliques do botão registrar
    private int dataInicioSalva = 0;
    private int horaInicioSalva = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_trilha);

        // Vinculando as Views exatamente com os IDs do seu XML complementar
        tvVelocidade = findViewById(R.id.tvVelocidade);
        tvVelocidadeMax = findViewById(R.id.tvVelocidadeMax);
        tvCronometro = findViewById(R.id.tvCronometro);
        tvDistancia = findViewById(R.id.tvDistancia);
        buttonRegistrar = findViewById(R.id.button_registrar);

        // Configurando os escutadores de clique nos botões do XML
        buttonRegistrar.setOnClickListener(this);
        findViewById(R.id.button_voltar_registrar).setOnClickListener(this);

        // Configuração do motor do GPS (Atualiza a cada 2 segundos se mover pelo menos 5 metros)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .setMinUpdateDistanceMeters(5)
                .build();

        // Callback disparado sempre que o celular se comunica com os satélites de GPS
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    atualizarPosicaoNoMapa(location);

                    // Os cálculos matemáticos e o desenho da rota só acontecem se o botão registrar estiver ativo
                    if (gravandoTrilha) {
                        calcularDadosDaTrilha(location);
                    }
                }
            }
        };

        // Carrega o fragmento do Google Maps de forma assíncrona
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        aplicarConfiguracoesMapa();

        // Prepara o ícone personalizado com tamanho controlado
        Bitmap imagemOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.marcador_usuario);
        Bitmap imagemRedimensionada = Bitmap.createScaledBitmap(imagemOriginal, 120, 120, false);
        iconePersonalizado = BitmapDescriptorFactory.fromBitmap(imagemRedimensionada);

        // Solicita as permissões em tempo de execução e liga a captação das coordenadas
        solicitarPermissaoEIniciarGps();
    }

    private void solicitarPermissaoEIniciarGps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_UPDATES);
        } else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    private void atualizarPosicaoNoMapa(Location location) {
        if (mMap == null) return;

        LatLng coordenadasReais = new LatLng(location.getLatitude(), location.getLongitude());
        float precisaoReal = location.getAccuracy();

        if (marcadorUsuario == null) {
            marcadorUsuario = mMap.addMarker(new MarkerOptions()
                    .position(coordenadasReais)
                    .title("Sua Posição")
                    .icon(iconePersonalizado));

            circuloPrecisao = mMap.addCircle(new CircleOptions()
                    .center(coordenadasReais)
                    .radius(precisaoReal)
                    .strokeWidth(2f)
                    .strokeColor(0xFF007FFF)
                    .fillColor(0x33007FFF));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadasReais, 17f));
        } else {
            marcadorUsuario.setPosition(coordenadasReais);
            circuloPrecisao.setCenter(coordenadasReais);
            circuloPrecisao.setRadius(precisaoReal);
        }

        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        String tipoNavegacao = prefs.getString("tipo_navegacao", "northup");

        com.google.android.gms.maps.model.CameraPosition.Builder cameraBuilder =
                new com.google.android.gms.maps.model.CameraPosition.Builder()
                        .target(coordenadasReais)
                        .zoom(mMap.getCameraPosition().zoom);

        if (tipoNavegacao.equals("courseup") && location.hasBearing()) {
            cameraBuilder.bearing(location.getBearing());
        } else {
            cameraBuilder.bearing(0f);
        }

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
    }

    private void calcularDadosDaTrilha(Location localizacaoAtual) {
        LatLng novoPonto = new LatLng(localizacaoAtual.getLatitude(), localizacaoAtual.getLongitude());
        listaPontosTrilha.add(novoPonto);

        if (rotaTrilha == null) {
            PolylineOptions opcoesLinha = new PolylineOptions()
                    .addAll(listaPontosTrilha)
                    .width(10f)
                    .color(Color.RED)
                    .geodesic(true);
            rotaTrilha = mMap.addPolyline(opcoesLinha);
        } else {
            rotaTrilha.setPoints(listaPontosTrilha);
        }

        float velocidadeKmH = 0f;
        if (localizacaoAtual.hasSpeed()) {
            velocidadeKmH = localizacaoAtual.getSpeed() * 3.6f;
        }
        tvVelocidade.setText(String.format(Locale.getDefault(), "Vel. Instantânea: %.1f km/h", velocidadeKmH));

        if (velocidadeKmH > velocidadeMaximaRegistrada) {
            velocidadeMaximaRegistrada = velocidadeKmH;
            tvVelocidadeMax.setText(String.format(Locale.getDefault(), "Vel. Máxima: %.1f km/h", velocidadeMaximaRegistrada));
        }

        if (localizacaoAnterior != null) {
            float distanciaEntrePontos = localizacaoAnterior.distanceTo(localizacaoAtual);
            distanciaTotalPercorrida += distanciaEntrePontos;

            if (distanciaTotalPercorrida < 1000) {
                tvDistancia.setText(String.format(Locale.getDefault(), "Distância Total: %.0f m", distanciaTotalPercorrida));
            } else {
                float distanciaKm = distanciaTotalPercorrida / 1000f;
                tvDistancia.setText(String.format(Locale.getDefault(), "Distância Total: %.2f km", distanciaKm));
            }
        }

        localizacaoAnterior = localizacaoAtual;
    }

    private void iniciarCronometro() {
        cronometroHandler = new Handler(Looper.getMainLooper());
        cronometroRunnable = new Runnable() {
            @Override
            public void run() {
                segundosTranscorridos++;

                int horas = segundosTranscorridos / 3600;
                int minutos = (segundosTranscorridos % 3600) / 60;
                int secs = segundosTranscorridos % 60;

                String tempoFormatado = String.format(Locale.getDefault(), "Tempo: %02d:%02d:%02d", horas, minutos, secs);
                tvCronometro.setText(tempoFormatado);

                cronometroHandler.postDelayed(this, 1000);
            }
        };
        cronometroHandler.postDelayed(cronometroRunnable, 1000);
    }

    private void pararCronometro() {
        if (cronometroHandler != null && cronometroRunnable != null) {
            cronometroHandler.removeCallbacks(cronometroRunnable);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_registrar) {
            if (!gravandoTrilha) {
                // Ação: LIGAR O GRAVADOR DA TRILHA
                gravandoTrilha = true;
                localizacaoAnterior = null;
                distanciaTotalPercorrida = 0f;
                velocidadeMaximaRegistrada = 0f;
                segundosTranscorridos = 0;

                dataInicioSalva = obterDataAtualComoInt();
                horaInicioSalva = obterHoraAtualComoInt();

                listaPontosTrilha.clear();
                if (rotaTrilha != null) {
                    rotaTrilha.remove();
                    rotaTrilha = null;
                }

                iniciarCronometro();
                buttonRegistrar.setText("Parar Registro");
                Toast.makeText(this, "Monitoramento da trilha iniciado!", Toast.LENGTH_SHORT).show();
            } else {
                // Ação: DESLIGAR O GRAVADOR DA TRILHA
                gravandoTrilha = false;
                pararCronometro();
                buttonRegistrar.setText("Iniciar Registro");

                // 1. Captura o momento exato do FIM antes de abrir a caixinha
                final int dataFimSalva = obterDataAtualComoInt();
                final int horaFimSalva = obterHoraAtualComoInt();

                // 2. Criar uma caixinha de alerta com um campo de texto (EditText) de forma dinâmica
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setTitle("Salvar Trilha");
                builder.setMessage("Digite um nome para a sua trilha:");

                final android.widget.EditText inputNome = new android.widget.EditText(this);
                inputNome.setHint("Ex: Caminhada no Parque");
                builder.setView(inputNome);

                // Botão de Confirmar/Salvar
                builder.setPositiveButton("Salvar", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String nomeDigitado = inputNome.getText().toString().trim();

                        if (nomeDigitado.isEmpty()) {
                            nomeDigitado = "Trilha do dia " + dataFimSalva;
                        }

                        // 3. Instancia a classe Trilha e popula com os dados coletados
                        Trilha trilha = new Trilha();
                        trilha.setNomeTrilha(nomeDigitado);
                        trilha.setDataInicio(dataInicioSalva);
                        trilha.setHoraInicio(horaInicioSalva);
                        trilha.setDataFim(dataFimSalva);
                        trilha.setHoraFim(horaFimSalva);
                        trilha.setVelocidadeMaxima(velocidadeMaximaRegistrada);

                        // Cálculo da velocidade média em float estruturado
                        float velocidadeMediaCalculada = 0f;
                        if (segundosTranscorridos > 0 && distanciaTotalPercorrida > 0) {
                            float distanciaEmKm = distanciaTotalPercorrida / 1000f;
                            float tempoEmHoras = segundosTranscorridos / 3600f;
                            velocidadeMediaCalculada = distanciaEmKm / tempoEmHoras;
                        }
                        trilha.setVelocidadeMedia(velocidadeMediaCalculada);

                        // Grava na tabela do banco de dados
                        trilhadb = new TrilhasDB(RegistrarTrilhaActivity.this);
                        trilhadb.salvarTrilha(trilha);

                        Toast.makeText(RegistrarTrilhaActivity.this, "Trilha finalizada! Dados guardados no banco.", Toast.LENGTH_SHORT).show();
                    }
                });

                // Botão de Cancelar
                builder.setNegativeButton("Descartar", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        dialog.cancel();
                        Toast.makeText(RegistrarTrilhaActivity.this, "Trilha descartada.", Toast.LENGTH_SHORT).show();
                    }
                });

                // Mostra a caixinha na tela do celular
                builder.show();
            }
        }

        if (id == R.id.button_voltar_registrar) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_UPDATES && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            solicitarPermissaoEIniciarGps();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarConfiguracoesMapa();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        pararCronometro();
    }

    private void aplicarConfiguracoesMapa() {
        if (mMap == null) return;
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        String tipoMapa = prefs.getString("tipo_mapa", "vetorial");
        mMap.setMapType(tipoMapa.equals("satelite") ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
    }

    private int obterDataAtualComoInt() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int ano = c.get(java.util.Calendar.YEAR);
        int mes = c.get(java.util.Calendar.MONTH) + 1;
        int dia = c.get(java.util.Calendar.DAY_OF_MONTH);
        return (ano * 10000) + (mes * 100) + dia;
    }

    private int obterHoraAtualComoInt() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int hora = c.get(java.util.Calendar.HOUR_OF_DAY);
        int minuto = c.get(java.util.Calendar.MINUTE);
        int segundo = c.get(java.util.Calendar.SECOND);
        return (hora * 10000) + (minuto * 100) + segundo;
    }
}