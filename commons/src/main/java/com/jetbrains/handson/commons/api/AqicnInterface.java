package com.jetbrains.handson.commons.api;

import com.jetbrains.handson.commons.api.AqicnDataResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Antonio Vanegas @hpsaturn on 12/31/19.
 *
 * The next API interface is based from:
 * https://aqicn.org/json-api/doc/
 */

public interface AqicnInterface {

    /**
     * get for the real-time Air Qualuty index for a given station.
     * @param token
     * @param city
     * @return
     */

    @GET("/feed/{city}/")
    Call <AqicnDataResponse> getDataFromCity(
            @Query("token") String token,
            @Path("city") String city
    );

    /**
     * get the nearest station close to the user location, based on the IP adress information.
     * @param token
     * @return
     */

    @GET("/feed/here/")
    Call <AqicnDataResponse> getDataFromHere(
            @Query("token") String token
    );

    /**
     * map bounds in the form lat1,lng1,lat2,lng2
     * @param token
     * @param latlng
     */

    @GET("/map/bounds/")
    Call <AqicnDataResponse> getDataFromMapBounds(
            @Query("token") String token,
            @Query("latlng") String latlng
    );

}
