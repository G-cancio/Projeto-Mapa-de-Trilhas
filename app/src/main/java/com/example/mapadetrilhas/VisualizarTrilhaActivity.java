package com.example.mapadetrilhas;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class VisualizarTrilhaActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ListView listViewTrilhas;
    private ArrayList<Trilha> listaTrilhas;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> nomesTrilhas;
    private TrilhasDB trilhasDB;
    private int posicaoSelecionada = -1;

    private int dataFiltroInicio = 0;
    private int dataFiltroFim = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_trilha);

        listViewTrilhas = findViewById(R.id.listViewTrilhas);

        findViewById(R.id.button_consultar).setOnClickListener(this);
        findViewById(R.id.button_apagar).setOnClickListener(this);
        findViewById(R.id.button_editar).setOnClickListener(this);
        findViewById(R.id.button_voltar_visualizar).setOnClickListener(this);

        trilhasDB = new TrilhasDB(this);
        listViewTrilhas.setOnItemClickListener(this);

        carregarListaTrilhas();
    }

    private void carregarListaTrilhas() {
        listaTrilhas = trilhasDB.consultarTrilha();
        nomesTrilhas = new ArrayList<>();

        for (Trilha t : listaTrilhas) {
            nomesTrilhas.add(t.getNomeTrilha());
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, nomesTrilhas);
        listViewTrilhas.setAdapter(adapter);
        listViewTrilhas.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        posicaoSelecionada = -1;
        listViewTrilhas.clearChoices();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        posicaoSelecionada = position;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_consultar) {
            if (posicaoSelecionada == -1) {
                Toast.makeText(this, "Por favor, selecione uma trilha na lista primeiro!", Toast.LENGTH_SHORT).show();
                return;
            }

            Trilha trilhaSelecionada = listaTrilhas.get(posicaoSelecionada);

            Intent intentMapa = new Intent(this, RegistrarTrilhaActivity.class);
            intentMapa.putExtra("id_trilha", trilhaSelecionada.getId());
            intentMapa.putExtra("nome", trilhaSelecionada.getNomeTrilha());
            intentMapa.putExtra("data_inicio", trilhaSelecionada.getDataInicio());
            intentMapa.putExtra("hora_inicio", trilhaSelecionada.getHoraInicio());
            intentMapa.putExtra("data_fim", trilhaSelecionada.getDataFim());
            intentMapa.putExtra("hora_fim", trilhaSelecionada.getHoraFim());
            intentMapa.putExtra("vel_media", trilhaSelecionada.getVelocidadeMedia());
            intentMapa.putExtra("vel_maxima", trilhaSelecionada.getVelocidadeMaxima());

            startActivity(intentMapa);
        }

        if (id == R.id.button_apagar) {
            String[] opcoesApagar = {
                    "Apagar trilha selecionada",
                    "Apagar trilhas por período (intervalo)",
                    "Apagar TODAS as trilhas"
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Opções de Exclusão");
            builder.setItems(opcoesApagar, (dialog, which) -> {
                if (which == 0) {
                    if (posicaoSelecionada == -1) {
                        Toast.makeText(this, "Selecione uma trilha na lista para apagar!", Toast.LENGTH_LONG).show();
                    } else {
                        Trilha alvo = listaTrilhas.get(posicaoSelecionada);
                        trilhasDB.apagarUmaTrilha(alvo.getId());
                        Toast.makeText(this, "Trilha removida com sucesso!", Toast.LENGTH_SHORT).show();
                        carregarListaTrilhas();
                    }
                } else if (which == 1) {
                    abrirDefinicaoPeriodo();
                } else if (which == 2) {
                    new AlertDialog.Builder(this)
                            .setTitle("Confirmação Absoluta")
                            .setMessage("Tem certeza que deseja limpar TODO o histórico de trilhas?")
                            .setPositiveButton("Sim, Apagar Tudo", (d, w) -> {
                                trilhasDB.apagarTodasTrilhas();
                                Toast.makeText(this, "Todo o banco de dados foi resetado!", Toast.LENGTH_SHORT).show();
                                carregarListaTrilhas();
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
        }

        if (id == R.id.button_editar) {
            if (posicaoSelecionada == -1) {
                Toast.makeText(this, "Selecione uma trilha para alterar o nome!", Toast.LENGTH_SHORT).show();
                return;
            }
            Trilha alvo = listaTrilhas.get(posicaoSelecionada);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Editar Nome");
            final android.widget.EditText input = new android.widget.EditText(this);
            input.setText(alvo.getNomeTrilha());
            builder.setView(input);

            builder.setPositiveButton("Alterar", (dialog, which) -> {
                String novoNome = input.getText().toString().trim();
                if (!novoNome.isEmpty()) {
                    android.content.ContentValues cv = new android.content.ContentValues();
                    cv.put("nomeTrilha", novoNome);
                    trilhasDB.getWritableDatabase().update("trilha", cv, "id = ?", new String[]{String.valueOf(alvo.getId())});
                    carregarListaTrilhas();
                    Toast.makeText(VisualizarTrilhaActivity.this, "Nome atualizado!", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
        }

        if (id == R.id.button_voltar_visualizar) {
            finish();
        }
    }

    private void abrirDefinicaoPeriodo() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerInicio = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            dataFiltroInicio = (year * 10000) + ((month + 1) * 100) + dayOfMonth;

            DatePickerDialog datePickerFim = new DatePickerDialog(VisualizarTrilhaActivity.this, (viewFim, yearFim, monthFim, dayOfMonthFim) -> {
                dataFiltroFim = (yearFim * 10000) + ((monthFim + 1) * 100) + dayOfMonthFim;

                if (dataFiltroFim < dataFiltroInicio) {
                    Toast.makeText(VisualizarTrilhaActivity.this, "Erro: A data final não pode ser menor que a inicial!", Toast.LENGTH_LONG).show();
                } else {
                    trilhasDB.apagarTrilhasPorPeriodo(dataFiltroInicio, dataFiltroFim);
                    Toast.makeText(VisualizarTrilhaActivity.this, "Trilhas do período selecionado foram apagadas!", Toast.LENGTH_SHORT).show();
                    carregarListaTrilhas();
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePickerFim.setTitle("Selecione a DATA FINAL do período");
            datePickerFim.show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerInicio.setTitle("Selecione a DATA INICIAL do período");
        datePickerInicio.show();
    }
}