package hpsaturn.pollutionreporter.api;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GitHubService {
    String contentType = "Content-Type: application/json";




    @POST("repos/kike-canaries/dapp-airquality/issues")
    @Headers({
            contentType,
    })
    Call<ResponseBody> createIssue(@Body Issue issue,  @Header("Authorization") String authHeader);
}
