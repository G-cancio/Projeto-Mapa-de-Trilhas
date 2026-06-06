package com.example.mapadetrilhas;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConfiguracaoActivity extends AppCompatActivity implements View.OnClickListener {

    private Switch switchVetorial;
    private Switch switchSatelite;
    private Switch switchNorthUp;
    private Switch switchCourseUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        switchVetorial = findViewById(R.id.switch_vetorial);
        switchSatelite = findViewById(R.id.switch_satelite);
        switchNorthUp = findViewById(R.id.switch_northup);
        switchCourseUp = findViewById(R.id.switch_courseup);

        switchVetorial.setOnClickListener(this);
        switchSatelite.setOnClickListener(this);
        switchNorthUp.setOnClickListener(this);
        switchCourseUp.setOnClickListener(this);

        Button botaoSalvar = findViewById(R.id.button_salvar);
        Button botaoVoltar = findViewById(R.id.button_voltar_configuracao);

        botaoSalvar.setOnClickListener(this);
        botaoVoltar.setOnClickListener(this);

        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        String tipoMapa = prefs.getString("tipo_mapa", "vetorial");
        String tipoNavegacao = prefs.getString("tipo_navegacao", "northup");

        switchVetorial.setChecked(tipoMapa.equals("vetorial"));
        switchSatelite.setChecked(tipoMapa.equals("satelite"));
        switchNorthUp.setChecked(tipoNavegacao.equals("northup"));
        switchCourseUp.setChecked(tipoNavegacao.equals("courseup"));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.switch_vetorial) {

            if (switchVetorial.isChecked()) {
                switchSatelite.setChecked(false);
            } else {
                switchSatelite.setChecked(true);
            }
        }

        if (id == R.id.switch_satelite) {

            if (switchSatelite.isChecked()) {
                switchVetorial.setChecked(false);
            } else {
                switchVetorial.setChecked(true);
            }
        }

        if (id == R.id.switch_northup) {

            if (switchNorthUp.isChecked()) {
                switchCourseUp.setChecked(false);
            } else {
                switchCourseUp.setChecked(true);
            }
        }

        if (id == R.id.switch_courseup) {

            if (switchCourseUp.isChecked()) {
                switchNorthUp.setChecked(false);
            } else {
                switchNorthUp.setChecked(true);
            }
        }

        if (id == R.id.button_salvar) {

            SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String mapaParaSalvar = switchSatelite.isChecked() ? "satelite" : "vetorial";
            String navegacaoParaSalvar = switchCourseUp.isChecked() ? "courseup" : "northup";

            editor.putString("tipo_mapa", mapaParaSalvar);
            editor.putString("tipo_navegacao", navegacaoParaSalvar);
            editor.apply();

            Toast.makeText(this, "Configurações Salvas!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (id == R.id.button_voltar_configuracao) {
            finish();
        }
    }
}