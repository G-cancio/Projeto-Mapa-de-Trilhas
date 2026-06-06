package com.example.mapadetrilhas;

import java.util.List;

public class Trilha {
    private int id;
    private String nomeTrilha;
    private int dataInicio;
    private int dataFim;
    private int horaInicio;
    private int horaFim;
    private float velocidadeMedia;
    private float velocidadeMaxima;

    public Trilha(String nomeTrilha, int dataInicio, int dataFim, int horaInicio, int horaFim, float velocidadeMedia, float velocidadeMaxima) {
        this.nomeTrilha = nomeTrilha;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.velocidadeMedia = velocidadeMedia;
        this.velocidadeMaxima = velocidadeMaxima;
    }

    public Trilha() { }

    public int getId() {
        return id;
    }

    public String getNomeTrilha() {
        return nomeTrilha;
    }

    public int getDataInicio() {
        return dataInicio;
    }

    public int getDataFim() {
        return dataFim;
    }

    public int getHoraInicio() {
        return horaInicio;
    }

    public int getHoraFim() {
        return horaFim;
    }

    public float getVelocidadeMedia() {
        return velocidadeMedia;
    }

    public float getVelocidadeMaxima() {
        return velocidadeMaxima;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNomeTrilha(String nomeTrilha) {
        this.nomeTrilha = nomeTrilha;
    }

    public void setDataInicio(int dataInicio) {
        this.dataInicio = dataInicio;
    }

    public void setDataFim(int dataFim) {
        this.dataFim = dataFim;
    }

    public void setHoraInicio(int horaInicio) {
        this.horaInicio = horaInicio;
    }

    public void setHoraFim(int horaFim) {
        this.horaFim = horaFim;
    }

    public void setVelocidadeMedia(float velocidadeMedia) {
        this.velocidadeMedia = velocidadeMedia;
    }

    public void setVelocidadeMaxima(float velocidadeMaxima) {
        this.velocidadeMaxima = velocidadeMaxima;
    }
}