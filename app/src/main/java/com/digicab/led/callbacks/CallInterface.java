package com.digicab.led.callbacks;

import com.digicab.led.BuildConfig;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;


/**
 * Created by android on 11/11/2016.
 */

public interface CallInterface {

    @FormUrlEncoded
    @POST(BuildConfig.device_register)
    Call<String> device_register(@FieldMap Map<String, Object> parameters);

    @FormUrlEncoded
    @POST(BuildConfig.advertise_view)
    Call<String> advertise_view(@FieldMap Map<String, Object> parameters);

    @GET
    Call<ResponseBody> downloadMedia(@Url String url);
}



