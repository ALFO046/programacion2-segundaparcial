package com.parcial2_programacionii.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.parcial2_programacionii.database.AppDatabase;
import com.parcial2_programacionii.database.LogApp;
import com.parcial2_programacionii.network.ApiService;
import com.parcial2_programacionii.network.RetrofitClient;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class SincronizarLogsWorker extends Worker {

    private static final String TAG = "SincronizarLogsWorker";

    public SincronizarLogsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        List<LogApp> logs = database.logAppDao().obtenerTodos();

        if (logs == null || logs.isEmpty()) {
            Log.d(TAG, "No hay logs para sincronizar.");
            return Result.success();
        }

        Log.d(TAG, "Iniciando sincronización de " + logs.size() + " logs.");
        ApiService apiService = RetrofitClient.getApiService();

        try {
            Response<ResponseBody> response = apiService.enviarLogs(logs).execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Logs sincronizados con éxito. Eliminando de la base de datos local.");
                database.logAppDao().eliminarTodos();
                return Result.success();
            } else {
                Log.e(TAG, "Error en la sincronización: " + response.code());
                return Result.retry(); // Reintentar en el futuro
            }
        } catch (IOException e) {
            Log.e(TAG, "Fallo en la conexión al sincronizar logs", e);
            return Result.retry(); // Reintentar en el futuro
        }
    }
}
