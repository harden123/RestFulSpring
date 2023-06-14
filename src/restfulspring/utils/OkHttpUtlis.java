package restfulspring.utils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtlis {
    private static OkHttpClient client;
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    static {
        client = new OkHttpClient.Builder()
                .sslSocketFactory(createSSLSocketFactory(), (X509TrustManager) new TrustManager[]{new TrustAllCerts()}[0])
//        .sslSocketFactory(createSSLSocketFactory())
                .connectTimeout(7, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
                .hostnameVerifier(new TrustAllHostnameVerifier()).build();
    }

    public static String doGet(Map<String,Object> params, Map<String,String> headers, String url){
        try {
            Request.Builder builder = new Request.Builder();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            if (CollectionUtils.isNotEmpty(params)) {
                params.forEach((key, value) -> {
                    urlBuilder.addQueryParameter(key, String.valueOf(value));
                });
            }
            if (CollectionUtils.isNotEmpty(headers)) {
                headers.forEach((key, value) -> {
                    builder.header(key, String.valueOf(value));
                });
            }
            builder.url(urlBuilder.build());
            Request request = builder.get()
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (SocketTimeoutException e){
            System.out.println(e.getMessage());
            return e.getMessage();
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static String doGet2(Map<String,Object> params, Map<String,String> headers, String url){
        try {
            Request.Builder builder = new Request.Builder();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            if (CollectionUtils.isNotEmpty(params)) {
                params.forEach((key, value) -> {
                    urlBuilder.addQueryParameter(key, String.valueOf(value));
                });
            }
            if (CollectionUtils.isNotEmpty(headers)) {
                headers.forEach((key, value) -> {
                    builder.header(key, String.valueOf(value));
                });
            }
            builder.url(urlBuilder.build());
            Request request = builder.get()
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("》》》》》》》》》》》》》》》》》》》 okhttp返回了 失败："+e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    System.out.println("》》》》》》》》》》》》》》》》》》》 okhttp返回了 成功："+response.body().toString());
                }
            });
            return "";
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static String doGet(Map<String,Object> params, String url){
        return doGet(params, null, url);
    }


    public static String doPost(Map<String,Object> params, Map<String,String> headers, String url){
        try {
            Request.Builder builder = new Request.Builder();
            MultipartBody.Builder multBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            params.forEach((key, value) -> {
                multBuilder.addFormDataPart(key, String.valueOf(value));
            });
            if (CollectionUtils.isNotEmpty(headers)) {
                headers.forEach((key, value) -> {
                    builder.header(key, String.valueOf(value));
                });
            }
            Request request = builder
                    .header("Content-Type","text/html; charset=utf-8;")
                    .url(url)
                    .post(multBuilder.build())
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (SocketTimeoutException e){
            System.out.println(e.getMessage());
            return "";
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static String doPost(Map<String,Object> params, String url){
        return doPost(params, null, url);
    }

    public static String doPostJSON(String json, Map<String,String> headers, String url){
        try {
            Request.Builder builder = new Request.Builder();
            RequestBody body = RequestBody.create(JSON_TYPE, json);
            if (CollectionUtils.isNotEmpty(headers)) {
                headers.forEach((key, value) -> {
                    builder.header(key, String.valueOf(value));
                });
            }
            Request request = builder
                    .header("Content-Type","application/json; charset=utf-8;")
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (SocketTimeoutException e){
            System.out.println(e.getMessage());
            return e.getMessage();
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static String doPostJSON(String json, String url){
        return doPostJSON(json, null, url);
    }

    public static byte[] doPostJSONAndReturnBytes(String json, Map<String,String> headers, String url){
        try {
            Request.Builder builder = new Request.Builder();
            RequestBody body = RequestBody.create(JSON_TYPE, json);
            if (CollectionUtils.isNotEmpty(headers)) {
                headers.forEach((key, value) -> {
                    builder.header(key, String.valueOf(value));
                });
            }
            Request request = builder
                    .header("Content-Type","application/json; charset=utf-8;")
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().bytes();
        }catch (SocketTimeoutException e){
            System.out.println(e.getMessage());
            return new byte[0];
        }catch (Exception e){
            e.printStackTrace();
            return new byte[0];
        }
    }
    public static String doPostForm(Map<String,Object> params, Map<String,String> headers, String url){
        Request.Builder builder = new Request.Builder();
        FormBody.Builder formEncodingBuilder = new FormBody.Builder();
        try {
            params.forEach((key, value) -> {
                formEncodingBuilder.add(key, String.valueOf(value));
            });
            if (CollectionUtils.isNotEmpty(headers)) {
                headers.forEach((key, value) -> {
                    builder.header(key, String.valueOf(value));
                });
            }
            Request request = builder
                    .header("Content-Type","application/x-www-form-urlencoded; charset=utf-8;")
                    .url(url)
                    .post(formEncodingBuilder.build())
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (SocketTimeoutException e){
            System.out.println(e.getMessage());
            return "";
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static String doPostForm(Map<String,Object> params, String url){
        return doPostForm(params, null, url);
    }


    public static String doDelete(Map<String,Object> params, Map<String,String> headers, String url){
        try {
            Request.Builder builder = new Request.Builder();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            params.forEach((key, value) -> {
                urlBuilder.addQueryParameter(key, String.valueOf(value));
            });
            if (CollectionUtils.isNotEmpty(headers)) {
                headers.forEach((key, value) -> {
                    builder.header(key, String.valueOf(value));
                });
            }
            Request request = builder
                    .url(url)
                    .delete()
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (SocketTimeoutException e){
            System.out.println(e.getMessage());
            return "";
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static String doDelete(Map<String,Object> params, String url){
        return doDelete(params,null,url);
    }

    public static String doPut(Map<String, Object> params, Map<String, String> headers, String url) {
//        log.info("url=" + url + "," + params);
        try {
            Request.Builder builder = new Request.Builder();
            FormBody.Builder multBuilder = new FormBody.Builder();
            params.forEach((key, value) -> {
                multBuilder.add(key, String.valueOf(value));
            });
            if (CollectionUtils.isNotEmpty(headers)) {
                headers.forEach((key, value) -> {
                    builder.header(key, String.valueOf(value));
                });
            }
            Request request = builder
                    .url(url)
                    .put(multBuilder.build())
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (SocketTimeoutException e){
            System.out.println(e.getMessage());
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String doPut(Map<String, Object> params, String url) {
        return doPut(params, null, url);
    }


    public static OkHttpClient getOkHttpClient(){
        return client;
    }

    /**
     * 默认信任所有的证书
     *
     * @return
     */
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory sSLSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return sSLSocketFactory;
    }


    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws java.security.cert.CertificateException {

        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws java.security.cert.CertificateException {

        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }


}
