package com.example.mapadetrilhas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class Configuracao extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        Button botaoTelaConsultar = findViewById(R.id.button_tela_consultar);
        botaoTelaConsultar.setOnClickListener(this);

        Button botaoTelaRegistrar = findViewById(R.id.button_tela_registrar);
        botaoTelaRegistrar.setOnClickListener(this);

        Button botaoSair = findViewById(R.id.button_sair_aplicacao);
        botaoSair.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.switch_vetorial) {

        }

        if (v.getId() == R.id.switch_satelite) {

        }

        if (v.getId() == R.id.switch_northup) {

        }

        if (v.getId() == R.id.switch_courseup) {

        }

        if (v.getId() == R.id.button_tela_consultar) {
            Intent i = new Intent(this, VisualizarTrilha.class);
            startActivity(i);
        }

        if (v.getId() == R.id.button_tela_registrar) {
            Intent i = new Intent(this, RegistrarTrilha.class);
            startActivity(i);
        }

        if (v.getId() == R.id.button_sair_aplicacao) {
            finish();
        }
    }
}