package hpsaturn.pollutionreporter.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hpsaturn.tools.Logger;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Antonio Vanegas @hpsaturn on 12/31/19.
 */
public class AqicnApiManager {

    public static String TAG = AqicnApiManager.class.getSimpleName();
    public static final String API_URL = "https://api.waqi.info/";
    private static final boolean DEBUG = false;

    private static AqicnApiManager instance;
    private Context mContext;
    private AqicnInterface service;

    public static AqicnApiManager getInstance() {
        if (instance == null) {
            instance = new AqicnApiManager();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        Logger.i(TAG,"Aqicn API retrofit builder set to "+API_URL);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .build();
        service = retrofit.create(AqicnInterface.class);
    }

    

}
