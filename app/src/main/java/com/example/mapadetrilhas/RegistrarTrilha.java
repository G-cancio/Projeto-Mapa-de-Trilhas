package com.example.mapadetrilhas;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

public class RegistrarTrilha extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_LOCATION_UPDATES = 1;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private String nomeTrilha;
    private int dataInicio;
    private int dataFim;
    private int horaInicio;
    private int horaFim;
    private int velocidadeMedia;
    private int velocidadeMaxima;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_trilha);

        Button botaoRegistrar = findViewById(R.id.button_registrar);
        botaoRegistrar.setOnClickListener(this);

        Button botaoVoltar = findViewById(R.id.button_voltar_registrar);
        botaoVoltar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_registrar) {
            Intent i = new Intent(this, MapsActivity.class);
            startActivity(i);
        }

        if (id == R.id.button_voltar_registrar) {
            finish();
        }
    }
}