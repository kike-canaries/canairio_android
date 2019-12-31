package hpsaturn.pollutionreporter.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by Antonio Vanegas @hpsaturn on 12/31/19.
 */
public interface AqicnInterface {

    @GET("/feed/{city}/")
    Call <AqicnDataResponse> getDataFromCity(
            @Path("city") String city,
            @Query("token") String token
    );

}
