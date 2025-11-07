package com.parcial2_programacionii.api;

import com.parcial2_programacionii.database.LogApp;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface ApiService {

    // Endpoint para sincronizar los logs de la app (Requerimiento 4)
    @POST
    Call<Void> sincronizarLogs(@Url String url, @Body List<LogApp> logs);

    // Endpoint para el formulario de cliente con fotos (Requerimiento 1)
    @Multipart
    @POST
    Call<Void> registrarCliente(
            @Url String url,
            @Part("cliente") RequestBody jsonCliente,
            @Part MultipartBody.Part fotoCasa1,
            @Part MultipartBody.Part fotoCasa2,
            @Part MultipartBody.Part fotoCasa3
    );


    // Endpoint para la carga múltiple de archivos comprimidos (Requerimiento 2)
    @Multipart
    @POST
    Call<Void> cargarArchivosZip(
            @Url String url,
            @Part("ci_cliente") RequestBody ciCliente,
            @Part MultipartBody.Part archivoZip
    );
}
