package restfulspring.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;

import lombok.SneakyThrows;

public class OkHttpUtlis {
//    private static OkHttpClient client;
//    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
//    static {
//        client = new OkHttpClient.Builder()
//                .sslSocketFactory(createSSLSocketFactory(), (X509TrustManager) new TrustManager[]{new TrustAllCerts()}[0])
////        .sslSocketFactory(createSSLSocketFactory())
//                .connectTimeout(7, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
//                .hostnameVerifier(new TrustAllHostnameVerifier()).build();
//    }

//	private static HostnameVerifier trustAllHostnameVerifier;

	static {
	    TrustManager[] trustAllCerts = new TrustManager[]{
		    new X509TrustManager() {
		        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
		        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
		        public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
		    }
		};
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			
			@Override
			public boolean verify(String var1, SSLSession var2) {
				return true;
			}
		});
	}

	@SneakyThrows
    public static String doPostJSON(String json, Map<String,String> headers, String url){
		try {
			URL apiUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(5000); // 设置连接超时时间为 5 秒
			connection.setReadTimeout(30000);
			connection.setRequestProperty("Content-Type", "application/json; utf-8"); // 设置请求头为 JSON 格式
			if (CollectionUtils.isNotEmpty(headers)) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					String key = entry.getKey();
					String val = entry.getValue();
					connection.addRequestProperty(key, val);
				}
			}
			
			connection.setDoOutput(true); // 允许向服务器输出内容
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
			writer.write(json);
			writer.flush();
			
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
			    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			    String line;
			    StringBuilder response = new StringBuilder();
			    while ((line = in.readLine()) != null) {
			        response.append(line);
			    }
			    in.close();
			    String responseBody = response.toString();
				return responseBody;
			} else {
				// 处理响应错误
				String errorMsg = getErrorMsg(connection);
				return responseCode+":"+errorMsg;
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}
    
    public static String doGet(Map<String,Object> params, Map<String,String> headers, String url){
		try {
			if (CollectionUtils.isNotEmpty(params)) {
				String initGetParam = TextUtil.initGetParam(params);
				if (StringUtils.contains(url, "?")) {
					url+="&"+StringUtils.removeStart(initGetParam, "?");
				}else {
					url+=initGetParam;
				}
			}
			URL apiUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
			connection.setRequestProperty("Content-Type", "application/json; utf-8"); // 设置请求头为 JSON 格式
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000); // 设置连接超时时间为 5 秒
			connection.setReadTimeout(30000);
			if (CollectionUtils.isNotEmpty(headers)) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					String key = entry.getKey();
					String val = entry.getValue();
					connection.addRequestProperty(key, val);
				}
			}
			
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
			    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			    String line;
			    StringBuilder response = new StringBuilder();
			    while ((line = in.readLine()) != null) {
			        response.append(line);
			    }
			    in.close();
			    String responseBody = response.toString();
				return responseBody;
			} else {
				// 处理响应错误
				String errorMsg = getErrorMsg(connection);
				return responseCode+":"+errorMsg;
			}
		} catch (Exception e) {
			return e.getMessage();
		}
    }

    
	private static String getErrorMsg(HttpURLConnection connection) throws IOException {
		InputStream errorStream = connection.getErrorStream();
	    if (errorStream != null) {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
	        String line;
	        StringBuilder builder = new StringBuilder();
	        while ((line = reader.readLine()) != null) {
	            builder.append(line);
	            builder.append(System.getProperty("line.separator"));
	        }
	        return builder.toString();
	    }
		return null;
	}
	
	public static void main(String[] args) {
		String doGet = doGet(null, null, "https://www.baidu.com");
		System.out.println(doGet);
	}
}
