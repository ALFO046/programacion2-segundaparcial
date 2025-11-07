package com.parcial2_programacionii.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "logs_app")
public class LogApp {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String fechaHora;
    private String descripcionError;
    private String claseOrigen;

    // Constructor vacío
    public LogApp() {
    }

    // Constructor con parámetros
    public LogApp(String fechaHora, String descripcionError, String claseOrigen) {
        this.fechaHora = fechaHora;
        this.descripcionError = descripcionError;
        this.claseOrigen = claseOrigen;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDescripcionError() {
        return descripcionError;
    }

    public void setDescripcionError(String descripcionError) {
        this.descripcionError = descripcionError;
    }

    public String getClaseOrigen() {
        return claseOrigen;
    }

    public void setClaseOrigen(String claseOrigen) {
        this.claseOrigen = claseOrigen;
    }
}