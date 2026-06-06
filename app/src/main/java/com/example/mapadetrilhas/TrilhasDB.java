package com.example.mapadetrilhas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class TrilhasDB extends SQLiteOpenHelper {

    private static final String DATABASE = "trilha_database";
    private static final int VERSION = 2; // Incremetado devido a nova estrutura de tabela

    public TrilhasDB(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabela 1: Armazena os metadados da Trilha
        String create_trilha_table =
                "CREATE TABLE trilha(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "nomeTrilha TEXT NOT NULL, dataInicio INTEGER NOT NULL, dataFim INTEGER NOT NULL," +
                        "horaInicio INTEGER NOT NULL, horaFim INTEGER NOT NULL, velocidadeMedia FLOAT NOT NULL," +
                        "velocidadeMaxima FLOAT NOT NULL);";
        db.execSQL(create_trilha_table);

        // Tabela 2: Armazena os pontos geográficos mapeados (Relacionamento 1 para Muitos)
        String create_waypoint_table =
                "CREATE TABLE waypoint(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "idTrilha INTEGER NOT NULL," +
                        "latitude REAL NOT NULL, longitude REAL NOT NULL," +
                        "FOREIGN KEY(idTrilha) REFERENCES trilha(id) ON DELETE CASCADE);";
        db.execSQL(create_waypoint_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS waypoint");
        db.execSQL("DROP TABLE IF EXISTS trilha");
        onCreate(db);
    }

    // Salva a trilha e retorna o ID gerado para salvarmos os pontos vinculados a ela
    public long salvarTrilha(Trilha trilha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nomeTrilha", trilha.getNomeTrilha());
        values.put("dataInicio", trilha.getDataInicio());
        values.put("dataFim", trilha.getDataFim());
        values.put("horaInicio", trilha.getHoraInicio());
        values.put("horaFim", trilha.getHoraFim());
        values.put("velocidadeMedia", trilha.getVelocidadeMedia());
        values.put("velocidadeMaxima", trilha.getVelocidadeMaxima());
        return db.insert("trilha", null, values);
    }

    // Método para salvar os pontos de localização individualmente no percurso
    public void salvarWaypoint(Waypoint waypoint) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idTrilha", waypoint.getIdTrilha());
        values.put("latitude", waypoint.getLatitude());
        values.put("longitude", waypoint.getLongitude());
        db.insert("waypoint", null, values);
    }

    public ArrayList<Trilha> consultarTrilha() {
        ArrayList<Trilha> trilhas = new ArrayList<>();
        String[] columns = {"id", "nomeTrilha", "dataInicio", "dataFim", "horaInicio", "horaFim", "velocidadeMedia", "velocidadeMaxima"};
        Cursor cursor = getWritableDatabase().query("trilha", columns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            Trilha trilha = new Trilha();
            trilha.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            trilha.setNomeTrilha(cursor.getString(cursor.getColumnIndexOrThrow("nomeTrilha")));
            trilha.setDataInicio(cursor.getInt(cursor.getColumnIndexOrThrow("dataInicio")));
            trilha.setDataFim(cursor.getInt(cursor.getColumnIndexOrThrow("dataFim")));
            trilha.setHoraInicio(cursor.getInt(cursor.getColumnIndexOrThrow("horaInicio")));
            trilha.setHoraFim(cursor.getInt(cursor.getColumnIndexOrThrow("horaFim")));
            trilha.setVelocidadeMedia(cursor.getFloat(cursor.getColumnIndexOrThrow("velocidadeMedia")));
            trilha.setVelocidadeMaxima(cursor.getFloat(cursor.getColumnIndexOrThrow("velocidadeMaxima")));
            trilhas.add(trilha);
        }
        cursor.close();
        return trilhas;
    }

    // Consulta os Waypoints salvos de uma determinada trilha para redesenhar no mapa
    public ArrayList<Waypoint> consultarWaypointsDaTrilha(int idTrilha) {
        ArrayList<Waypoint> pontos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM waypoint WHERE idTrilha = ? ORDER BY id ASC", new String[]{String.valueOf(idTrilha)});

        while (cursor.moveToNext()) {
            Waypoint wp = new Waypoint();
            wp.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
            wp.setIdTrilha(cursor.getInt(cursor.getColumnIndexOrThrow("idTrilha")));
            wp.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
            wp.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
            pontos.add(wp);
        }
        cursor.close();
        return pontos;
    }

    public void apagarUmaTrilha(int idTrilha) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("waypoint", "idTrilha = ?", new String[]{String.valueOf(idTrilha)});
        db.delete("trilha", "id = ?", new String[]{String.valueOf(idTrilha)});
    }

    public void apagarTodasTrilhas() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM waypoint");
        db.execSQL("DELETE FROM trilha");
    }

    // Método novo para apagar trilhas em um intervalo de datas específico (pelo período)
    public void apagarTrilhasPorPeriodo(int dataInicio, int dataFim) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Primeiro deletamos os caminhos (waypoints) das trilhas que estão no período
        String queryWaypoints = "DELETE FROM waypoint WHERE idTrilha IN " +
                "(SELECT id FROM trilha WHERE dataInicio >= ? AND dataInicio <= ?)";
        db.execSQL(queryWaypoints, new Object[]{dataInicio, dataFim});

        // Depois deletamos as trilhas do período
        db.delete("trilha", "dataInicio >= ? AND dataInicio <= ?", new String[]{String.valueOf(dataInicio), String.valueOf(dataFim)});
    }
}