package com.parcial2_programacionii.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LogAppDao {

    @Insert
    void insertar(LogApp log);

    @Query("SELECT * FROM logs_app ORDER BY id DESC")
    List<LogApp> obtenerTodos();

    @Query("DELETE FROM logs_app")
    void eliminarTodos();

    @Query("SELECT COUNT(*) FROM logs_app")
    int contarLogs();
}