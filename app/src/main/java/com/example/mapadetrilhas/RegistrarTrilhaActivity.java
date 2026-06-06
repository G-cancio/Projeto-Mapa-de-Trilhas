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

    private TrilhasDB trilhadb;

    private GoogleMap mMap;
    private Marker marcadorUsuario;
    private Circle circuloPrecisao;
    private BitmapDescriptor iconePersonalizado;

    private Polyline rotaTrilha;
    private ArrayList<LatLng> listaPontosTrilha = new ArrayList<>();

    private TextView tvVelocidade, tvVelocidadeMax, tvCronometro, tvDistancia;
    private Button buttonRegistrar, buttonVoltar;

    private boolean gravandoTrilha = false;
    private Location localizacaoAnterior = null;
    private float distanciaTotalPercorrida = 0f;
    private float velocidadeMaximaRegistrada = 0f;

    private int segundosTranscorridos = 0;
    private int dataInicioSalva = 0;
    private int horaInicioSalva = 0;

    private int idTrilhaVisualizacao = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_trilha);

        tvVelocidade = findViewById(R.id.tvVelocidade);
        tvVelocidadeMax = findViewById(R.id.tvVelocidadeMax);
        tvCronometro = findViewById(R.id.tvCronometro);
        tvDistancia = findViewById(R.id.tvDistancia);
        buttonRegistrar = findViewById(R.id.button_registrar);
        buttonVoltar = findViewById(R.id.button_voltar_registrar);

        trilhadb = new TrilhasDB(this);

        if (getIntent().hasExtra("id_trilha")) {
            idTrilhaVisualizacao = getIntent().getIntExtra("id_trilha", -1);
        }

        buttonRegistrar.setOnClickListener(this);
        buttonVoltar.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .setMinUpdateDistanceMeters(5)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (idTrilhaVisualizacao != -1) return;
                for (Location location : locationResult.getLocations()) {
                    atualizarPosicaoNoMapa(location);
                    if (gravandoTrilha) {
                        calcularDadosDaTrilha(location);
                    }
                }
            }
        };

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

        Bitmap imagemOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.marcador_usuario);
        Bitmap imagemRedimensionada = Bitmap.createScaledBitmap(imagemOriginal, 120, 120, false);
        iconePersonalizado = BitmapDescriptorFactory.fromBitmap(imagemRedimensionada);

        if (idTrilhaVisualizacao != -1) {
            buttonRegistrar.setVisibility(View.GONE);

            String nome = getIntent().getStringExtra("nome");
            int dataIni = getIntent().getIntExtra("data_inicio", 0);
            int horaIni = getIntent().getIntExtra("hora_inicio", 0);
            int dataFim = getIntent().getIntExtra("data_fim", 0);
            int horaFim = getIntent().getIntExtra("hora_fim", 0);

            float velMedia = getIntent().getFloatExtra("vel_media", 0f);
            float velMax = getIntent().getFloatExtra("vel_maxima", 0f);

            if (velMax < velMedia) {
                float temp = velMax;
                velMax = velMedia;
                velMedia = temp;
            }

            String dataStr = String.valueOf(dataIni);
            String dataFormatada = "00/00/0000";
            if (dataStr.length() == 8) {
                dataFormatada = dataStr.substring(6, 8) + "/" + dataStr.substring(4, 6) + "/" + dataStr.substring(0, 4);
            }

            String horaStr = String.format(Locale.getDefault(), "%06d", horaIni);
            String horaFormatada = "00:00:00";
            if (horaStr.length() == 6) {
                horaFormatada = horaStr.substring(0, 2) + ":" + horaStr.substring(2, 4) + ":" + horaStr.substring(4, 6);
            }

            int tempoSegundos = calcularDiferencaTempo(horaIni, horaFim);
            int h = tempoSegundos / 3600;
            int m = (tempoSegundos % 3600) / 60;
            int s = tempoSegundos % 60;
            String duracaoFormatada = String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);

            float tempoEmHoras = tempoSegundos / 3600f;
            float distanciaCalculadaKm = velMedia * tempoEmHoras;
            float distanciaEmMetros = distanciaCalculadaKm * 1000f;

            String distFormatada = distanciaEmMetros < 1000 ?
                    String.format(Locale.getDefault(), "%.0f m", distanciaEmMetros) :
                    String.format(Locale.getDefault(), "%.2f km", distanciaCalculadaKm);

            tvVelocidade.setText("Trilha: " + nome + "\nData: " + dataFormatada + " às " + horaFormatada);
            tvVelocidadeMax.setText(String.format(Locale.getDefault(), "Vel. Máx: %.1f km/h | Média: %.1f km/h", velMax, velMedia));
            tvDistancia.setText("Distância Total: " + distFormatada);
            tvCronometro.setText("Duração: " + duracaoFormatada);

            carregarRotaHistoricaNoMapa();
        } else {
            solicitarPermissaoEIniciarGps();
        }
    }

    private void solicitarPermissaoEIniciarGps() {
        if (idTrilhaVisualizacao != -1) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_UPDATES);
        } else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    private void carregarRotaHistoricaNoMapa() {
        if (mMap == null || idTrilhaVisualizacao == -1) return;

        ArrayList<Waypoint> listaWaypoints = trilhadb.consultarWaypointsDaTrilha(idTrilhaVisualizacao);
        ArrayList<LatLng> pontosMapa = new ArrayList<>();

        for (Waypoint wp : listaWaypoints) {
            pontosMapa.add(new LatLng(wp.getLatitude(), wp.getLongitude()));
        }

        if (!pontosMapa.isEmpty()) {
            PolylineOptions opcoesLinha = new PolylineOptions().addAll(pontosMapa).width(12f).color(Color.BLUE).geodesic(true);
            mMap.addPolyline(opcoesLinha);

            LatLng primeiroPonto = pontosMapa.get(0);
            mMap.addMarker(new MarkerOptions()
                    .position(primeiroPonto)
                    .title("Início do Percurso")
                    .icon(iconePersonalizado));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(primeiroPonto, 17f));
        } else {
            Toast.makeText(this, "Nenhuma coordenada de trajeto salva para esta trilha.", Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarPosicaoNoMapa(Location location) {
        if (mMap == null || idTrilhaVisualizacao != -1) return;

        LatLng coordenadasReais = new LatLng(location.getLatitude(), location.getLongitude());
        float precisaoReal = location.getAccuracy();

        if (marcadorUsuario == null) {
            marcadorUsuario = mMap.addMarker(new MarkerOptions().position(coordenadasReais).title("Sua Posição").icon(iconePersonalizado));
            circuloPrecisao = mMap.addCircle(new CircleOptions().center(coordenadasReais).radius(precisaoReal).strokeWidth(2f).strokeColor(0xFF007FFF).fillColor(0x33007FFF));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadasReais, 17f));
        } else {
            marcadorUsuario.setPosition(coordenadasReais);
            circuloPrecisao.setCenter(coordenadasReais);
            circuloPrecisao.setRadius(precisaoReal);
        }

        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        String tipoNavegacao = prefs.getString("tipo_navegacao", "northup");

        com.google.android.gms.maps.model.CameraPosition.Builder cameraBuilder = new com.google.android.gms.maps.model.CameraPosition.Builder().target(coordenadasReais).zoom(mMap.getCameraPosition().zoom);
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
            PolylineOptions opcoesLinha = new PolylineOptions().addAll(listaPontosTrilha).width(10f).color(Color.RED).geodesic(true);
            rotaTrilha = mMap.addPolyline(opcoesLinha);
        } else {
            rotaTrilha.setPoints(listaPontosTrilha);
        }

        float velocidadKmH = 0f;
        if (localizacaoAtual.hasSpeed() && localizacaoAtual.getSpeed() > 0) {
            velocidadKmH = localizacaoAtual.getSpeed() * 3.6f;
        } else if (localizacaoAnterior != null) {
            float distFatiada = localizacaoAnterior.distanceTo(localizacaoAtual);
            long deltaMilli = localizacaoAtual.getTime() - localizacaoAnterior.getTime();
            float tempoDiferencaSegundos = deltaMilli / 1000f;

            if (tempoDiferencaSegundos > 0.1f) {
                velocidadKmH = (distFatiada / tempoDiferencaSegundos) * 3.6f;
            }
        }

        if (velocidadKmH < 0.5f) {
            velocidadKmH = 0f;
        }

        tvVelocidade.setText(String.format(Locale.getDefault(), "Vel. Instantânea: %.1f km/h", velocidadKmH));

        if (velocidadKmH > velocidadeMaximaRegistrada) {
            velocidadeMaximaRegistrada = velocidadKmH;
            tvVelocidadeMax.setText(String.format(Locale.getDefault(), "Vel. Máxima: %.1f km/h", velocidadeMaximaRegistrada));
        }

        if (localizacaoAnterior != null) {
            float distanciaEntrePontos = localizacaoAnterior.distanceTo(localizacaoAtual);
            if (distanciaEntrePontos > 1.0f && distanciaEntrePontos < 50.0f) {
                distanciaTotalPercorrida += distanciaEntrePontos;
            }

            if (distanciaTotalPercorrida < 1000) {
                tvDistancia.setText(String.format(Locale.getDefault(), "Distância Total: %.0f m", distanciaTotalPercorrida));
            } else {
                tvDistancia.setText(String.format(Locale.getDefault(), "Distância Total: %.2f km", distanciaTotalPercorrida / 1000f));
            }
        }
        localizacaoAnterior = localizacaoAtual;
    }

    private void pararCronometro() {
        cronvalHandler.removeCallbacks(cronSimpleRunnable);
    }

    private final Handler cronvalHandler = new Handler(Looper.getMainLooper());
    private final Runnable cronSimpleRunnable = new Runnable() {
        @Override
        public void run() {
            if (!gravandoTrilha) return;
            segundosTranscorridos++;
            int horas = segundosTranscorridos / 3600;
            int minutos = (segundosTranscorridos % 3600) / 60;
            int secs = segundosTranscorridos % 60;
            tvCronometro.setText(String.format(Locale.getDefault(), "Tempo: %02d:%02d:%02d", horas, minutos, secs));
            cronvalHandler.postDelayed(this, 1000);
        }
    };

    private void resetarInterfaceEMapa() {
        gravandoTrilha = false;
        localizacaoAnterior = null;
        distanciaTotalPercorrida = 0f;
        velocidadeMaximaRegistrada = 0f;
        segundosTranscorridos = 0;
        listaPontosTrilha.clear();

        if (rotaTrilha != null) { rotaTrilha.remove(); rotaTrilha = null; }
        if (marcadorUsuario != null) { marcadorUsuario.remove(); marcadorUsuario = null; }
        if (circuloPrecisao != null) { circuloPrecisao.remove(); circuloPrecisao = null; }

        tvVelocidade.setText("Vel. Instantânea: 0,0 km/h");
        tvVelocidadeMax.setText("Vel. Máxima: 0,0 km/h");
        tvDistancia.setText("Distância Total: 0 m");
        tvCronometro.setText("Tempo: 00:00:00");
        buttonRegistrar.setText("Iniciar Registro");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_registrar) {
            if (!gravandoTrilha) {
                gravandoTrilha = true;
                localizacaoAnterior = null;
                distanciaTotalPercorrida = 0f;
                velocidadeMaximaRegistrada = 0f;
                segundosTranscorridos = 0;
                listaPontosTrilha.clear();

                dataInicioSalva = obterDataAtualComoInt();
                horaInicioSalva = obterHoraAtualComoInt();

                // ADICIONADO: Captura imediata da posição atual como 1º ponto para testes estáticos
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null && listaPontosTrilha.isEmpty()) {
                            LatLng pontoInicial = new LatLng(location.getLatitude(), location.getLongitude());
                            listaPontosTrilha.add(pontoInicial);
                        }
                    });
                }

                cronvalHandler.postDelayed(cronSimpleRunnable, 1000);

                buttonRegistrar.setText("Parar Registro");
                Toast.makeText(this, "Monitoramento iniciado!", Toast.LENGTH_SHORT).show();
            } else {
                gravandoTrilha = false;
                pararCronometro();

                final int dataFimSalva = obterDataAtualComoInt();
                final int horaFimSalva = obterHoraAtualComoInt();

                // AJUSTADO: Se mesmo com a injeção inicial a lista falhar, evita persistência vazia de segurança
                if (listaPontosTrilha.isEmpty()) {
                    Toast.makeText(this, "Aguardando sinal válido de GPS para gerar coordenadas.", Toast.LENGTH_LONG).show();
                    resetarInterfaceEMapa();
                    return;
                }

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setTitle("Salvar Trilha");
                builder.setMessage("Digite um nome para a sua trilha:");

                final android.widget.EditText inputNome = new android.widget.EditText(this);
                inputNome.setHint("Ex: Corrida Matinal");
                builder.setView(inputNome);

                builder.setPositiveButton("Salvar", (dialog, which) -> {
                    String nomeDigitado = inputNome.getText().toString().trim();
                    if (nomeDigitado.isEmpty()) {
                        nomeDigitado = "Trilha do dia " + dataFimSalva;
                    }

                    Trilha trilha = new Trilha();
                    trilha.setNomeTrilha(nomeDigitado);
                    trilha.setDataInicio(dataInicioSalva);
                    trilha.setHoraInicio(horaInicioSalva);
                    trilha.setDataFim(dataFimSalva);
                    trilha.setHoraFim(horaFimSalva);

                    float velocidadeMediaCalculada = 0f;
                    if (segundosTranscorridos > 0 && distanciaTotalPercorrida > 0) {
                        velocidadeMediaCalculada = (distanciaTotalPercorrida / 1000f) / (segundosTranscorridos / 3600f);
                    }
                    trilha.setVelocidadeMedia(velocidadeMediaCalculada);

                    if (velocidadeMaximaRegistrada < velocidadeMediaCalculada) {
                        velocidadeMaximaRegistrada = velocidadeMediaCalculada;
                    }
                    trilha.setVelocidadeMaxima(velocidadeMaximaRegistrada);

                    long longIdTrilha = trilhadb.salvarTrilha(trilha);
                    int idTrilhaGerada = (int) longIdTrilha;

                    for (LatLng latLng : listaPontosTrilha) {
                        Waypoint wp = new Waypoint(idTrilhaGerada, latLng.latitude, latLng.longitude);
                        trilhadb.salvarWaypoint(wp);
                    }

                    Toast.makeText(RegistrarTrilhaActivity.this, "Trilha e coordenadas salvas com sucesso!", Toast.LENGTH_SHORT).show();
                    resetarInterfaceEMapa();
                });

                builder.setNegativeButton("Descartar", (dialog, which) -> {
                    dialog.cancel();
                    resetarInterfaceEMapa();
                    Toast.makeText(RegistrarTrilhaActivity.this, "Trilha descartada.", Toast.LENGTH_SHORT).show();
                });

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
        if (idTrilhaVisualizacao == -1 && mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
            }
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
        return (c.get(java.util.Calendar.YEAR) * 10000) + ((c.get(java.util.Calendar.MONTH) + 1) * 100) + c.get(java.util.Calendar.DAY_OF_MONTH);
    }

    private int obterHoraAtualComoInt() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        return (c.get(java.util.Calendar.HOUR_OF_DAY) * 10000) + (c.get(java.util.Calendar.MINUTE) * 100) + c.get(java.util.Calendar.SECOND);
    }

    private int calcularDiferencaTempo(int inicio, int fim) {
        int h1 = inicio / 10000; int m1 = (inicio % 10000) / 100; int s1 = inicio % 100;
        int h2 = fim / 10000; int m2 = (fim % 10000) / 100; int s2 = fim % 100;

        int totalSegundosInicio = (h1 * 3600) + (m1 * 60) + s1;
        int totalSegundosFim = (h2 * 3600) + (m2 * 60) + s2;

        int diff = totalSegundosFim - totalSegundosInicio;
        return diff > 0 ? diff : 1;
    }
}