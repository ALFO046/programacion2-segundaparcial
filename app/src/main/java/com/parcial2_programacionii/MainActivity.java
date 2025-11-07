package com.parcial2_programacionii;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.parcial2_programacionii.database.AppDatabase;
import com.parcial2_programacionii.database.LogApp;
import com.parcial2_programacionii.database.LogHelper;
import com.parcial2_programacionii.workers.SincronizarLogsWorker;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String SINCRO_LOGS_TAG = "sincronizar_logs";
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executorService = Executors.newSingleThreadExecutor();

        // Inicializar vistas
        Button btnFormularioCliente = findViewById(R.id.btnFormularioCliente);
        Button btnCargaArchivos = findViewById(R.id.btnCargaArchivos);
        Button btnVerLogs = findViewById(R.id.btnVerLogs);

        // Configurar listeners
        btnFormularioCliente.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FormularioClienteActivity.class));
        });

        btnCargaArchivos.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CargaArchivosActivity.class));
        });

        btnVerLogs.setOnClickListener(v -> mostrarLogs());

        // Inicializar WorkManager para sincronización periódica
        inicializarSincronizacionPeriodica();

        // Registrar evento de inicio de la app usando el Helper
        LogHelper.registrarEvento(this, "Aplicación iniciada", "MainActivity");
    }

    private void inicializarSincronizacionPeriodica() {
        // Crear restricciones (solo se ejecuta si hay conexión a internet)
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Crear la tarea periódica.
        // NOTA: Android limita la ejecución a un mínimo de 5 minutos para ahorrar batería.
        PeriodicWorkRequest sincronizarWorkRequest = new PeriodicWorkRequest.Builder(
                SincronizarLogsWorker.class,
                5, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        // Encolar el trabajo como único para evitar duplicados
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                SINCRO_LOGS_TAG, 
                ExistingPeriodicWorkPolicy.KEEP, // Mantiene el trabajo si ya existe
                sincronizarWorkRequest
        );

        Toast.makeText(this, "Sincronización de logs activada (cada 5 min)", Toast.LENGTH_LONG).show();
    }

    private void mostrarLogs() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                List<LogApp> logs = db.logAppDao().obtenerTodos();

                runOnUiThread(() -> {
                    if (logs.isEmpty()) {
                        Toast.makeText(this, "No hay logs registrados", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (LogApp log : logs) {
                        sb.append("ID: ").append(log.getId()).append("\n");
                        sb.append("Fecha: ").append(log.getFechaHora()).append("\n");
                        sb.append("Clase: ").append(log.getClaseOrigen()).append("\n");
                        sb.append("Descripción: ").append(log.getDescripcionError()).append("\n");
                        sb.append("────────────────────\n");
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Logs Registrados (" + logs.size() + ")")
                            .setMessage(sb.toString())
                            .setPositiveButton("Cerrar", null)
                            .setNegativeButton("Borrar Todos", (dialog, which) -> borrarTodosLosLogs())
                            .show();
                });

            } catch (Exception e) {
                LogHelper.registrarError(this, "Error al mostrar logs: " + e.getMessage(), "MainActivity");
            }
        });
    }

    private void borrarTodosLosLogs() {
        executorService.execute(() -> {
            try {
                AppDatabase.getInstance(this).logAppDao().eliminarTodos();
                runOnUiThread(() -> Toast.makeText(this, "Logs eliminados", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                LogHelper.registrarError(this, "Error al eliminar logs: " + e.getMessage(), "MainActivity");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
