package hpsaturn.pollutionreporter.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hpsaturn.tools.Logger;
import retrofit2.Call;
import retrofit2.Callback;
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

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

        Logger.i(TAG,"[API] AQICN retrofit builder set to "+API_URL);
        Retrofit retrofit = new Retrofit.
                Builder().
                addConverterFactory(GsonConverterFactory.create(gson)).
                baseUrl(API_URL).
                build();

        service = retrofit.create(AqicnInterface.class);
    }

    public void getDataFromCity (String token, String city, Callback <AqicnDataResponse> callback){
        Call<AqicnDataResponse> call = service.getDataFromCity(city,token);
        call.enqueue(callback);
    }

    public void getDataFromHere (String token, Callback <AqicnDataResponse> callback){
        Call<AqicnDataResponse> call = service.getDataFromHere(token);
        call.enqueue(callback);
    }

    public void getDataFromMapBounds (String token, String latlng, Callback <AqicnDataResponse> callback){
        Call<AqicnDataResponse> call = service.getDataFromMapBounds(token,latlng);
        call.enqueue(callback);
    }

}
