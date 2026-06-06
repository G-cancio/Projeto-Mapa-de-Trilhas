package com.example.mapadetrilhas;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String dbPath="app/kotlin+java/com.example.mapadetrilhas/TrilhasDB";
        try {
            db = SQLiteDatabase.openDatabase(dbPath,
                    null,SQLiteDatabase.CREATE_IF_NECESSARY);
            db.close();
        }
        catch (SQLiteException e) {
            System.out.println(e.getMessage());
        }

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
            Intent i = new Intent(this, VisualizarTrilhaActivity.class);
            startActivity(i);
        }

        if (id == R.id.button_tela_registrar) {
            Intent i = new Intent(this, RegistrarTrilhaActivity.class);
            startActivity(i);
        }

        if (id == R.id.button_tela_configuracao) {
            Intent i = new Intent(this, ConfiguracaoActivity.class);
            startActivity(i);
        }

        if (id == R.id.button_sair_aplicacao) {
            finishAffinity();
        }
    }
}