package com.example.mapadetrilhas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class TrilhasDB extends SQLiteOpenHelper {

    private static final String DATABASE = "trilha_database";
    private static final int VERSION = 1;

    public TrilhasDB(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_trilha_points_table =
                "CREATE TABLE trilha(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "nomeTrilha TEXT NOT NULL, dataInicio INTEGER NOT NULL, dataFIM INTEGER NOT NULL," +
                        "horaInicio INTEGER NOT NULL, horaFim INTEGER NOT NULL, velocidadeMedia FLOAT NOT NULL," +
                        "velocidadeMaxima FLOAT NOT NULL);";
        db.execSQL(create_trilha_points_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String drop_trilha_table = "DROP TABLE IF EXISTS trilha";
        db.execSQL(drop_trilha_table);
        onCreate(db);
    }

    public void salvarTrilha(Trilha trilha) {
        ContentValues values = new ContentValues();
        values.put("nomeTrilha", trilha.getNomeTrilha());
        values.put("dataInicio", trilha.getDataInicio());
        values.put("dataFim", trilha.getDataFim());
        values.put("horaInicio", trilha.getHoraInicio());
        values.put("horaFim", trilha.getHoraFim());
        values.put("velocidadeMedia", trilha.getVelocidadeMedia());
        values.put("velocidadeMaxima", trilha.getVelocidadeMaxima());
        getWritableDatabase().insert("trilha", null, values);
    }

    public ArrayList<Trilha> consultarTrilha() {
        ArrayList<Trilha> trilhas = new ArrayList<>();

        String[] colums = {"id", "nomeTrilha", "dataInicio", "dataFim", "horaInicio", "horaFim", "velocidadeMedia", "velocidadeMaxima"};
        Cursor cursor = getWritableDatabase().query("trilha", colums, null, null,
                null, null, null, null);

        while (cursor.moveToNext()) {
            Trilha trilha = new Trilha();
            trilha.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            trilha.setNomeTrilha(cursor.getString(cursor.getColumnIndexOrThrow("nomeTrilha")));
            trilha.setDataInicio(cursor.getInt(cursor.getColumnIndexOrThrow("DataInicio")));
            trilha.setDataFim(cursor.getInt(cursor.getColumnIndexOrThrow("DataFim")));
            trilha.setHoraInicio(cursor.getInt(cursor.getColumnIndexOrThrow("HoraInicio")));
            trilha.setHoraFim(cursor.getInt(cursor.getColumnIndexOrThrow("HoraFim")));
            trilha.setVelocidadeMedia(cursor.getInt(cursor.getColumnIndexOrThrow("VelocidadeMedia")));
            trilha.setVelocidadeMaxima(cursor.getInt(cursor.getColumnIndexOrThrow("VelocidadeMaxima")));
            trilhas.add(trilha);
        }

        return trilhas;
    }

    public void apagarTrilha() {
        getWritableDatabase().execSQL("DELETE FROM trilha");
    }
}