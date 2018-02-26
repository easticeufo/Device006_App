package com.madongfang.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madongfang.api.ReturnApi;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 *
 * Created by madongfang on 17/9/6.
 */

public class HttpUtil {

    public static void get(String url, ResponseListener responseListener)
    {
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(disposeHttpResponse(responseListener));
    }

    public static <T> T get(String url, Class<T> type) throws IOException
    {
        Request request = new Request.Builder().url(url).build();

        Response response = client.newCall(request).execute();

        ResponseBody responseBody = response.body();
        if (responseBody == null)
        {
            throw new IOException("http body为空");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        if (response.code() == 200)
        {
            return objectMapper.readValue(responseBody.string(), type);
        }
        else
        {
            throw new IOException(responseBody.string());
        }
    }

    public static void get(String url, String username, String passowrd, ResponseListener responseListener)
    {
        String credential = Credentials.basic(username, passowrd);
        Request request = new Request.Builder().header("Authorization", credential).url(url).build();

        client.newCall(request).enqueue(disposeHttpResponse(responseListener));
    }

    public abstract static class ResponseListener {
        protected ResponseListener(Class<?> type) {
            this.type = type;
        }

        Class<?> getType() {
            return type;
        }

        public abstract void onSuccess(Object obj);

        public abstract void onFailure(ReturnApi returnApi);

        private Class<?> type;
    }

    private static final String TAG = "HttpUtil";
    private static OkHttpClient client = new OkHttpClient();
    private static Handler handler = new Handler(Looper.getMainLooper());

    private static Callback disposeHttpResponse(final ResponseListener responseListener)
    {
        return new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                HttpUtil.onFailure(responseListener, new ReturnApi(-100, "网络异常:"+e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                try {
                    ResponseBody responseBody = response.body();
                    if (responseBody == null)
                    {
                        HttpUtil.onFailure(responseListener, new ReturnApi(-102, "responseBody为空"));
                        return;
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    if (response.code() == 200)
                    {
                        if (responseListener.getType() == String.class)
                        {
                            HttpUtil.onSuccess(responseListener, responseBody.string());
                        }
                        else
                        {
                            HttpUtil.onSuccess(responseListener, objectMapper.readValue(responseBody.string(), responseListener.getType()));
                        }
                    }
                    else
                    {
                        HttpUtil.onFailure(responseListener, objectMapper.readValue(responseBody.string(), ReturnApi.class));
                    }
                } catch (IOException e) {
                    Log.w(TAG, "catch IOException:", e);
                    HttpUtil.onFailure(responseListener, new ReturnApi(-101, "http响应获取异常:"+e.getMessage()));
                }
            }
        };
    }

    private static void onSuccess(final ResponseListener responseListener, final Object obj)
    {
        handler.post(new Runnable() {
            @Override
            public void run() {
                responseListener.onSuccess(obj);
            }
        });
    }

    private static void onFailure(final ResponseListener responseListener, final ReturnApi returnApi)
    {
        handler.post(new Runnable() {
            @Override
            public void run() {
                responseListener.onFailure(returnApi);
            }
        });
    }
}
