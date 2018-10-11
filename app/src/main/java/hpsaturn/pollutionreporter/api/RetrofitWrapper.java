package hpsaturn.pollutionreporter.api;

import android.support.annotation.NonNull;

import javax.net.ssl.SSLContext;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

final class RetrofitWrapper {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://api.github.com/";

    private RetrofitWrapper() {
    }

    @NonNull
    static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getClient())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    @NonNull
    private static OkHttpClient getClient() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder().addInterceptor(interceptor).build();
    }


    @NonNull
    private static OkHttpClient getSecureClient(SSLContext context) {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder client = new OkHttpClient.Builder().sslSocketFactory(context.getSocketFactory())
                .addInterceptor(interceptor);
        return client.build();
    }


}
