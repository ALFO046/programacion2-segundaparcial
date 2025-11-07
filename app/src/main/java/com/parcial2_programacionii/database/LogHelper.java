package com.parcial2_programacionii.database;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogHelper {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void registrarError(Context context, String descripcionError, String claseOrigen) {
        registrarEnDb(context, descripcionError, claseOrigen);
    }

    public static void registrarEvento(Context context, String descripcionEvento, String claseOrigen) {
        registrarEnDb(context, descripcionEvento, claseOrigen);
    }

    private static void registrarEnDb(Context context, String descripcion, String claseOrigen) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
                String fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                LogApp log = new LogApp(fechaHora, descripcion, claseOrigen);
                db.logAppDao().insertar(log);
            } catch (Exception e) {
                // Si falla el registro del log, imprimirlo en la consola para no perderlo
                e.printStackTrace();
            }
        });
    }
}
