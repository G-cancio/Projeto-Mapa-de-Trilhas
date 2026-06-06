package com.example.mapadetrilhas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class VisualizarTrilhaActivity extends AppCompatActivity implements View.OnClickListener {

    TrilhasDB trilhadb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_trilha);

        Button botaoConsultar = findViewById(R.id.button_consultar);
        botaoConsultar.setOnClickListener(this);

        Button botaoEditar = findViewById(R.id.button_editar);
        botaoEditar.setOnClickListener(this);

        Button botaoApagar = findViewById(R.id.button_apagar);
        botaoApagar.setOnClickListener(this);

        Button botaoVoltar = findViewById(R.id.button_voltar_visualizar);
        botaoVoltar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_consultar) {

        }

        if (id == R.id.button_editar) {

        }

        if (id == R.id.button_apagar) {

        }

        if (id == R.id.button_voltar_visualizar) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }
}