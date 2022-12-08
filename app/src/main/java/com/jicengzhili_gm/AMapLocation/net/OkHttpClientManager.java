package com.jicengzhili_gm.AMapLocation.net;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jicengzhili_gm.AMapLocation.util.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class OkHttpClientManager {
    // 提交json数据
    private static final MediaType JSON = MediaType
            .parse("application/json;charset=utf-8");
    public final static int CONNECT_TIMEOUT = 60;
    public final static int READ_TIMEOUT = 100;
    public final static int WRITE_TIMEOUT = 60;
    public static final MediaType STREAM = MediaType.parse("application/octet-stream");

    private static OkHttpClientManager mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler handler;
    private boolean isRepeat = false;

    private OkHttpClientManager() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .hostnameVerifier(new HostnameVerifier() {

                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        Log.i("jackietu","hostname:"+hostname);
                        //强行返回true 即验证成功
//                        return true;
                        if("zhilitong".equals(hostname)){
                            return true;
                        } else {
                            HostnameVerifier hv =
                                    HttpsURLConnection.getDefaultHostnameVerifier();
                            return hv.verify(hostname, session);
                        }
                    }
                }).build();
        handler = new Handler(Looper.getMainLooper());// 主线程处理
    }

    public synchronized static OkHttpClientManager getInstance() {
        if (mInstance == null) {
            mInstance = new OkHttpClientManager();
        }
        return mInstance;
    }

    public void getRequest(String url, final ResultCallback callback) {
        Request request = new Request.Builder().url(url).build();
        deliveryResult(callback, request);
    }


    /**
     * post异步请求 josn参数
     *
     * @param url
     * @param callback
     * @param json
     */
    public void postRequest(String url, final HttpCallBack callback,String json) {

        Request request = buildPostRequest(url, json);

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                //先去解析是否过期
                try {
                    Log.e("alen","onResponse call back");
                    ResponseBody responseBody = response.body();
                    BufferedSource source = responseBody.source();
                    source.request(Long.MAX_VALUE); // request the entire body.
                    Buffer buffer = source.buffer();
                    // clone buffer before reading from it
                    String res = buffer.clone().readString(Charset.forName("UTF-8"));
                    JSONObject responseobj=new JSONObject(res);
                    Log.e("alen","接口返回："+res);

                    String code = responseobj.getString("code");
                    boolean isSuc=responseobj.getBoolean("flag");
                    if(code.equals("401")&&!isSuc){//登录过期
                        //去调用登录接口
//                        if(!isRepeat) {//避免一直重复调用
//                            RequestBody formBody = new FormBody.Builder()
//                                    .add("grant_type", "client_credentials")
//                                    .add("client_id", "trace-upload")
//                                    .add("client_secret", "szzt#2020")
//                                    .build();
//                            Request request2 = new Request.Builder().url(Constant.REQUEST_URL + "/zty-oauth-server/oauth/token").post(formBody).build();
//                            mOkHttpClient.newCall(request2).enqueue(new Callback() {
//                                @Override
//                                public void onFailure(Call call2, IOException e2) {
//                                }
//
//                                @Override
//                                public void onResponse(Call call2, Response response2)
//                                        throws IOException {
//                                    try {
//                                        final String res = response2.body().string();
//                                        JSONObject responseobj = new JSONObject(res);
//                                        String access_token = responseobj.getString("token_type") + " " + responseobj.getString("access_token");
//                                        Log.e("alen","access_token: "+access_token);
//                                        Constant.token=access_token;
//                                    } catch (IOException e) {
//                                        // TODO Auto-generated catch block
////                                        e.printStackTrace();
//                                    }catch (JSONException e) {
////                                        e.printStackTrace();
//                                    }
//                                }
//                            });
//                        }
//
//                        isRepeat = !isRepeat;
                    }

                } catch (IOException e) {

                    Log.e("alen","onResponse IOException back");
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }catch (JSONException e) {
                    Log.e("alen","onResponse JSONException back");
                    e.printStackTrace();
                }

                Log.e("alen","postRequest 6");
                callback.onSuccess(response);
            }
        });
    }

    private void deliveryResult(final ResultCallback callback, Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                sendFailCallback(callback, e);
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                try {
                    sendSuccessCallBack(callback, response);
                } catch (final Exception e) {
                    sendFailCallback(callback, e);
                }
            }
        });
    }

    /**
     * 异步请求
     *
     * @param url
     * @param callBack
     */
    public void getUrl(String url, final HttpCallBack callBack) {
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("token",String.valueOf(System.currentTimeMillis()))
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                callBack.onSuccess(response);

            }
        });
    }


    /**
     * 上传文件
     *
     * @param url
     * @param file
     * @param callback
     * @throws IOException
     */
    public void postFile(String url, File file, final HttpCallBack callback) throws IOException {
        FileInputStream fileIS = new FileInputStream(file);
        byte[] byt = new byte[fileIS.available()];
        fileIS.read(byt);

        RequestBody fileBody = RequestBody.create(STREAM, byt);

        Request requestPostFile = new Request.Builder()
                .url(url)
                .post(fileBody)
                .build();

        fileIS.close();

        mOkHttpClient.newCall(requestPostFile).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                callback.onSuccess(response);
            }
        });
    }


    /**
     * json参数
     *
     * @param url
     * @param json
     * @return
     */
    private Request buildPostRequest(String url, String json) {
//        if (Constant.isNewInterface) {
//            RequestBody formBody = new FormBody.Builder()
//                    .add("tracks", json)
//                    .build();
////		RequestBody requestBody = RequestBody.create(JSON, json);
//            return new Request.Builder().url(url).post(formBody).build();
//        } else {
        RequestBody requestBody = RequestBody.create(JSON, json);
        Log.e("alen","Authorization: "+ Constant.token);
        Log.e("alen","uid: "+Constant.userUid);
        Log.e("alen","token: "+String.valueOf(System.currentTimeMillis()));
        return new Request.Builder()
                .header("Authorization",Constant.token)
                .header("token",String.valueOf(System.currentTimeMillis()))
                .url(url).post(requestBody).build();
//        }
    }

    private void sendFailCallback(final ResultCallback callback,
                                  final Exception e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    private void sendSuccessCallBack(final ResultCallback callback,
                                     final Response response) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }
        });
    }


    /**
     * http请求回调类,回调方法在UI线程中执行
     */
    public static abstract class ResultCallback {

        /**
         * 请求成功回调
         *
         * @param response
         */
        public abstract void onSuccess(Response response);

        /**
         * 请求失败回调
         *
         * @param e
         */
        public abstract void onFailure(Exception e);
    }

    /**
     * post请求参数类
     */
    public static class OkHttpParam {

        String key;
        String value;

        public OkHttpParam() {
        }

        public OkHttpParam(String key, String value) {
            this.key = key;
            this.value = value;
        }

    }

    /**
     * 请求回调接口
     */
    public interface HttpCallBack {
        void onSuccess(Response response);

        void onFailure(Exception e);
    }
}