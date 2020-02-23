package hpsaturn.pollutionreporter;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import hpsaturn.pollutionreporter.api.AqicnApiManager;
import hpsaturn.pollutionreporter.api.AqicnDataResponse;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Antonio Vanegas @hpsaturn on 1/2/20.
 */

@RunWith(MockitoJUnitRunner.class)
public class AqicnApiManagerUnitTest {

    private static final String TAG = AqicnApiManagerUnitTest.class.getSimpleName();

    @Mock
    Context context;

    @Mock
    AqicnApiManager api;

    @Captor
    ArgumentCaptor<Callback<AqicnDataResponse>> cb;

    @Before
    public void Setup(){
        AqicnApiManager.getInstance().init(context);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void readDataFromApi() {

        Mockito.verify(api).getDataFromHere(cb.capture());

        AqicnDataResponse aqicnDataResponse = new AqicnDataResponse();
        aqicnDataResponse.status="ok";
        Response<AqicnDataResponse> response = Response.success(aqicnDataResponse);
        cb.getValue().onResponse(null,response);

    }


}
