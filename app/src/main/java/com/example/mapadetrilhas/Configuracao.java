package com.example.mapadetrilhas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Configuracao extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        Switch switchVetorial = findViewById(R.id.switch_vetorial);
        switchVetorial.setOnClickListener(this);
        switchVetorial.setChecked(true);

        Switch switchSatelite = findViewById(R.id.switch_satelite);
        switchSatelite.setOnClickListener(this);
        switchSatelite.setChecked(false);

        Switch switchNorthUp = findViewById(R.id.switch_northup);
        switchNorthUp.setOnClickListener(this);
        switchNorthUp.setChecked(true);

        Switch switchCourseUp = findViewById(R.id.switch_courseup);
        switchCourseUp.setOnClickListener(this);
        switchCourseUp.setChecked(false);

        Button botaoSalvar = findViewById(R.id.button_salvar);
        botaoSalvar.setOnClickListener(this);

        Button botaoVoltar = findViewById(R.id.button_voltar_configuracao);
        botaoVoltar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.switch_vetorial) {

        }

        if (id == R.id.switch_satelite) {

        }

        if (id == R.id.switch_northup) {

        }

        if (id == R.id.switch_courseup) {

        }

        if (id == R.id.button_salvar) {
            Toast.makeText(this, "Configurações Salvas!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (id == R.id.button_voltar_configuracao) {
            finish();
        }
    }
}