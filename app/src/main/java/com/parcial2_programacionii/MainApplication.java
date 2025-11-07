package com.parcial2_programacionii;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.work.Configuration;

public class MainApplication extends Application implements Configuration.Provider {

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        // Proporciona una configuración personalizada para WorkManager para evitar
        // problemas de inicialización que puedan causar errores de cifrado.
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }
}
