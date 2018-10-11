package hpsaturn.pollutionreporter.api;

import android.support.annotation.NonNull;

import java.util.List;

import hpsaturn.pollutionreporter.BuildConfig;
import okhttp3.Credentials;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class GitHubApi {

    private static GitHubApi sharedInstance;

    @NonNull
    public static GitHubApi getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new GitHubApi();
        }

        return sharedInstance;
    }

    private final GitHubService service;

    private GitHubApi() {
        service = RetrofitWrapper.getRetrofitInstance().create(GitHubService.class);
    }


    public void createIssue(@NonNull final ResponseCallback<ResponseBody> callback, String body) {

        Call<ResponseBody> call = service.createIssue(new Issue("Reporter applicant 1", body),
                Credentials.basic("airqbot",BuildConfig.ghbot));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure();
            }
        });
    }

}
