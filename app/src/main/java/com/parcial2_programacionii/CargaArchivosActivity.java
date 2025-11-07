package com.parcial2_programacionii;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.parcial2_programacionii.api.ApiClient;
import com.parcial2_programacionii.api.ApiService;
import com.parcial2_programacionii.database.LogHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CargaArchivosActivity extends AppCompatActivity {

    private static final String WEBHOOK_URL = "https://webhook.site/695e2ebc-3e21-4fd1-9146-bb45acc042a9/"; // Reemplaza con tu URL

    private EditText etCi;
    private TextView tvContador;
    private List<Uri> urisSeleccionados = new ArrayList<>();

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    abrirSelectorArchivos();
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    urisSeleccionados.clear();
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            urisSeleccionados.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        urisSeleccionados.add(result.getData().getData());
                    }
                    tvContador.setText("Archivos seleccionados: " + urisSeleccionados.size());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carga_archivos);
        setTitle("Cargar Archivos de Cliente");

        etCi = findViewById(R.id.et_ci_carga);
        Button btnSeleccionar = findViewById(R.id.btn_seleccionar_archivos);
        Button btnEnviar = findViewById(R.id.btn_enviar_zip);

        btnSeleccionar.setOnClickListener(v -> solicitarPermisosYAbrirSelector());
        btnEnviar.setOnClickListener(v -> comprimirYEnviar());
    }

    private void solicitarPermisosYAbrirSelector() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
        } else {
             permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            abrirSelectorArchivos();
        } else {
            requestPermissionLauncher.launch(permissions);
        }
    }

    private void abrirSelectorArchivos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(Intent.createChooser(intent, "Seleccione archivos"));
    }

    private void comprimirYEnviar() {
        String ci = etCi.getText().toString().trim();
        if (ci.isEmpty()) {
            Toast.makeText(this, "Ingrese el CI del cliente", Toast.LENGTH_SHORT).show();
            return;
        }
        if (urisSeleccionados.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos un archivo", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File zipFile = crearArchivoZip(ci);
            enviarZip(ci, zipFile);
        } catch (IOException e) {
            LogHelper.registrarError(this, "Error al crear archivo ZIP: " + e.getMessage(), "CargaArchivosActivity");
            Toast.makeText(this, "Error al comprimir archivos", Toast.LENGTH_SHORT).show();
        }
    }

    private File crearArchivoZip(String ci) throws IOException {
        File zipFile = new File(getCacheDir(), "archivos_" + ci + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            byte[] buffer = new byte[1024];
            for (Uri uri : urisSeleccionados) {
                try (InputStream fis = getContentResolver().openInputStream(uri)) {
                    String fileName = new File(uri.getPath()).getName(); // Simple name extraction
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
            }
        }
        return zipFile;
    }

    private void enviarZip(String ci, File zipFile) {
        RequestBody ciBody = RequestBody.create(ci, MediaType.parse("text/plain"));
        RequestBody fileBody = RequestBody.create(zipFile, MediaType.parse("application/zip"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("archivoZip", zipFile.getName(), fileBody);

        ApiService apiService = ApiClient.getClient();
        Call<Void> call = apiService.cargarArchivosZip(WEBHOOK_URL, ciBody, filePart);

        Toast.makeText(this, "Enviando archivo ZIP...", Toast.LENGTH_SHORT).show();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CargaArchivosActivity.this, "Archivos enviados con éxito", Toast.LENGTH_LONG).show();
                    LogHelper.registrarEvento(CargaArchivosActivity.this, "Archivos ZIP para CI: " + ci + " enviados.", "CargaArchivosActivity");
                    zipFile.delete(); // Limpiar el archivo temporal
                    finish();
                } else {
                    handleApiError(response.code(), "enviarZip");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                handleApiError(-1, "enviarZip");
            }
        });
    }
    
    private void handleApiError(int errorCode, String fromMethod) {
        String errorMsg = "Error en la comunicación con el servidor (código: " + errorCode + ")";
        Toast.makeText(CargaArchivosActivity.this, errorMsg, Toast.LENGTH_LONG).show();
        LogHelper.registrarError(CargaArchivosActivity.this, errorMsg, "CargaArchivosActivity - " + fromMethod);
    }
}
