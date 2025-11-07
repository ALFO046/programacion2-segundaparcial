package com.parcial2_programacionii.network;

import com.parcial2_programacionii.database.LogApp;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    // Endpoint para enviar datos del cliente con fotos
    @Multipart
    @POST("/") // Webhook.site usará la URL base
    Call<ResponseBody> enviarCliente(
            @Part("datos") RequestBody datos,
            @Part MultipartBody.Part fotoCasa1,
            @Part MultipartBody.Part fotoCasa2,
            @Part MultipartBody.Part fotoCasa3
    );

    // Endpoint para enviar archivo ZIP
    @Multipart
    @POST("/")
    Call<ResponseBody> enviarArchivos(
            @Part("ci") RequestBody ci,
            @Part MultipartBody.Part archivoZip
    );

    // Endpoint para enviar logs
    @POST("/")
    Call<ResponseBody> enviarLogs(@Body List<LogApp> logs);
}
