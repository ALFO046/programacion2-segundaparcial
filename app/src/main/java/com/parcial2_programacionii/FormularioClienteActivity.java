package com.parcial2_programacionii;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.parcial2_programacionii.api.ApiClient;
import com.parcial2_programacionii.api.ApiService;
import com.parcial2_programacionii.database.LogHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormularioClienteActivity extends AppCompatActivity {

    private static final String WEBHOOK_URL = "https://webhook.site/695e2ebc-3e21-4fd1-9146-bb45acc042a9/cliente";

    private EditText etCi, etNombre, etDireccion, etTelefono;
    private ImageView ivFoto1, ivFoto2, ivFoto3;
    private Uri uriFoto1, uriFoto2, uriFoto3;
    private int fotoSeleccionada;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                Boolean cameraGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false);
                if (cameraGranted) {
                    abrirCamara();
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    switch (fotoSeleccionada) {
                        case 1: ivFoto1.setImageURI(uriFoto1); break;
                        case 2: ivFoto2.setImageURI(uriFoto2); break;
                        case 3: ivFoto3.setImageURI(uriFoto3); break;
                    }
                } else {
                    // Limpiar la URI si el usuario cancela
                    switch (fotoSeleccionada) {
                        case 1: uriFoto1 = null; break;
                        case 2: uriFoto2 = null; break;
                        case 3: uriFoto3 = null; break;
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_cliente);
        setTitle("Registrar Cliente");

        initViews();
        setupListeners();
    }

    private void initViews() {
        etCi = findViewById(R.id.et_ci);
        etNombre = findViewById(R.id.et_nombre_completo);
        etDireccion = findViewById(R.id.et_direccion);
        etTelefono = findViewById(R.id.et_telefono);
        ivFoto1 = findViewById(R.id.iv_foto_casa_1);
        ivFoto2 = findViewById(R.id.iv_foto_casa_2);
        ivFoto3 = findViewById(R.id.iv_foto_casa_3);
    }

    private void setupListeners() {
        findViewById(R.id.btn_capturar_foto_1).setOnClickListener(v -> handleFotoClick(1));
        findViewById(R.id.btn_capturar_foto_2).setOnClickListener(v -> handleFotoClick(2));
        findViewById(R.id.btn_capturar_foto_3).setOnClickListener(v -> handleFotoClick(3));
        findViewById(R.id.btn_enviar_formulario).setOnClickListener(v -> enviarFormulario());
    }

    private void handleFotoClick(int fotoNum) {
        this.fotoSeleccionada = fotoNum;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
        }
    }

    private void abrirCamara() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Foto Cliente " + fotoSeleccionada + "_" + System.currentTimeMillis());
        Uri tempUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        
        switch (fotoSeleccionada) {
            case 1: uriFoto1 = tempUri; break;
            case 2: uriFoto2 = tempUri; break;
            case 3: uriFoto3 = tempUri; break;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        cameraLauncher.launch(intent);
    }

    private void enviarFormulario() {
        String ci = etCi.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (ci.isEmpty() || nombre.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos de texto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uriFoto1 == null || uriFoto2 == null || uriFoto3 == null) {
            Toast.makeText(this, "Por favor, capture las tres fotos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear JSON
        JsonObject clienteJson = new JsonObject();
        clienteJson.addProperty("ci", ci);
        clienteJson.addProperty("nombreCompleto", nombre);
        clienteJson.addProperty("direccion", direccion);
        clienteJson.addProperty("telefono", telefono);
        RequestBody jsonBody = RequestBody.create(clienteJson.toString(), MediaType.parse("application/json"));

        // Crear partes Multipart
        try {
            MultipartBody.Part partFoto1 = createPartFromUri("fotoCasa1", uriFoto1);
            MultipartBody.Part partFoto2 = createPartFromUri("fotoCasa2", uriFoto2);
            MultipartBody.Part partFoto3 = createPartFromUri("fotoCasa3", uriFoto3);

            // Enviar con Retrofit
            ApiService apiService = ApiClient.getClient();
            Call<Void> call = apiService.registrarCliente(WEBHOOK_URL, jsonBody, partFoto1, partFoto2, partFoto3);
            
            Toast.makeText(this, "Enviando datos...", Toast.LENGTH_SHORT).show();
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(FormularioClienteActivity.this, "Datos enviados con éxito", Toast.LENGTH_LONG).show();
                        LogHelper.registrarEvento(FormularioClienteActivity.this, "Formulario Cliente enviado para CI: " + ci, "FormularioClienteActivity");
                        finish();
                    } else {
                        handleApiError(response.code(), "registrarCliente");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                     handleApiError(-1, "registrarCliente");
                }
            });

        } catch (IOException e) {
            LogHelper.registrarError(this, "Error al crear partes de archivo: " + e.getMessage(), "FormularioClienteActivity");
            Toast.makeText(this, "Error al procesar las imágenes", Toast.LENGTH_SHORT).show();
        }
    }

    private MultipartBody.Part createPartFromUri(String partName, Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        RequestBody requestFile = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(getContentResolver().getType(uri));
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (Source source = Okio.source(inputStream)) {
                    sink.writeAll(source);
                }
            }
        };

        return MultipartBody.Part.createFormData(partName, new File(uri.getPath()).getName(), requestFile);
    }
    
    private void handleApiError(int errorCode, String fromMethod) {
        String errorMsg = "Error en la comunicación con el servidor (código: " + errorCode + ")";
        Toast.makeText(FormularioClienteActivity.this, errorMsg, Toast.LENGTH_LONG).show();
        LogHelper.registrarError(FormularioClienteActivity.this, errorMsg, "FormularioClienteActivity - " + fromMethod);
    }
}
