package com.example.mapadetrilhas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button botaoTelaConsultar = findViewById(R.id.button_tela_consultar);
        botaoTelaConsultar.setOnClickListener(this);

        Button botaoTelaRegistrar = findViewById(R.id.button_tela_registrar);
        botaoTelaRegistrar.setOnClickListener(this);

        Button botaoConfiguracao = findViewById(R.id.button_tela_configuracao);
        botaoConfiguracao.setOnClickListener(this);

        Button botaoSair = findViewById(R.id.button_sair_aplicacao);
        botaoSair.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_tela_consultar) {
            Intent i = new Intent(this, VisualizarTrilha.class);
            startActivity(i);
        }

        if (id == R.id.button_tela_registrar) {
            Intent i = new Intent(this, RegistrarTrilha.class);
            startActivity(i);
        }

        if (id == R.id.button_tela_configuracao) {
            Intent i = new Intent(this, Configuracao.class);
            startActivity(i);
        }

        if (id == R.id.button_sair_aplicacao) {
            finishAffinity();
        }
    }
}