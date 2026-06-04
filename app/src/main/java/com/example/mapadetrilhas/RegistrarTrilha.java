package com.example.mapadetrilhas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrarTrilha extends AppCompatActivity implements View.OnClickListener {

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

        if (v.getId() == R.id.button_registrar) {

        }

        if (v.getId() == R.id.button_voltar_registrar) {
            Intent i = new Intent(this, Configuracao.class);
            startActivity(i);
        }
    }
}