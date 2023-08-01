package restfulspring.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.alibaba.fastjson.JSON;

import lombok.SneakyThrows;
import restfulspring.constant.RestConstant;

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
	static Map<String, String> host2CookieMap = new HashMap<String, String>();
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
	private static String desktop = System.getProperty("user.home")+"\\Desktop";
	private static Pattern fileNamePattern = Pattern.compile("filename=([^=;]+)");


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
			
		    String domainPort = addCookie(apiUrl, connection);
			
			connection.setDoOutput(true); // 允许向服务器输出内容
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
			writer.write(json);
			writer.flush();
			
			int responseCode = connection.getResponseCode();
			
			if (responseCode == HttpURLConnection.HTTP_OK) {
				boolean isFile = false;
				String fileName = "attachment"+DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
				Map<String, List<String>> respHeaders = connection.getHeaderFields();
		        for (Map.Entry<String, List<String>> entry : respHeaders.entrySet()) {
		            String headerName = entry.getKey();
		            List<String> headerValues = entry.getValue();
		            if (StringUtils.equalsIgnoreCase(headerName, "Set-Cookie")) {
		            	for (String headerValue : headerValues) {
							if (StringUtils.containsIgnoreCase(JSON.toJSONString(headerValue), "delete")) {
								continue;
							}
			                host2CookieMap.put(domainPort, headerValue);
						}
					}else if (StringUtils.equalsIgnoreCase(headerName, "Content-Type")){
						for (String headerValue : headerValues) {
							if (StringUtils.containsIgnoreCase(headerValue, "application/octet-stream")) {
								isFile=true;
							}
						}
					}else if(StringUtils.equalsIgnoreCase(headerName, "Content-Disposition")){
						for (String headerValue : headerValues) {
							Matcher matcher = fileNamePattern.matcher(headerValue);
							if (matcher.find()) {
								fileName = matcher.group(1);
							}
						}
					}
		        }
			        
		        if (isFile) {
		            String savePath = desktop+"\\"+fileName; 
					InputStream inputStream = connection.getInputStream();
					BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
					FileOutputStream fileOutputStream = new FileOutputStream(savePath);
					byte[] buffer = new byte[512];
					int bytesRead;
					while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
						fileOutputStream.write(buffer, 0, bytesRead);
					}
					fileOutputStream.close();
					bufferedInputStream.close();
					return savePath;
				}else {
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				    String line;
				    StringBuilder response = new StringBuilder();
				    while ((line = in.readLine()) != null) {
				        response.append(line);
				    }
				    in.close();
				    String responseBody = response.toString();
					return responseBody;
				}
			} else {
				// 处理响应错误
				String errorMsg = getErrorMsg(connection);
				return responseCode+":"+errorMsg;
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private static String addCookie(URL apiUrl, HttpURLConnection connection) {
		String domainPort = apiUrl.getHost()+apiUrl.getPort();
		String cookie = host2CookieMap.get(domainPort);
		if (StringUtils.isNotBlank(cookie)) {
			connection.addRequestProperty("Cookie", cookie);
		}
		return domainPort;
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
			addCookie(apiUrl, connection);
			
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				boolean isFile = false;
				String fileName = "attachment"+DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
				Map<String, List<String>> respHeaders = connection.getHeaderFields();
		        for (Map.Entry<String, List<String>> entry : respHeaders.entrySet()) {
		            String headerName = entry.getKey();
		            List<String> headerValues = entry.getValue();
		            if (StringUtils.equalsIgnoreCase(headerName, "Content-Type")){
						for (String headerValue : headerValues) {
							if (StringUtils.containsIgnoreCase(headerValue, "application/octet-stream")) {
								isFile=true;
							}
						}
					}else if(StringUtils.equalsIgnoreCase(headerName, "Content-Disposition")){
						for (String headerValue : headerValues) {
							Matcher matcher = fileNamePattern.matcher(headerValue);
							if (matcher.find()) {
								fileName = matcher.group(1);
							}
						}
					}
		        }
		        if (isFile) {
		            String savePath = desktop+"\\"+fileName; 
					InputStream inputStream = connection.getInputStream();
					BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
					FileOutputStream fileOutputStream = new FileOutputStream(savePath);
					byte[] buffer = new byte[512];
					int bytesRead;
					while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
						fileOutputStream.write(buffer, 0, bytesRead);
					}
					fileOutputStream.close();
					bufferedInputStream.close();
					return savePath;
				}else {
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				    String line;
				    StringBuilder response = new StringBuilder();
				    while ((line = in.readLine()) != null) {
				        response.append(line);
				    }
				    in.close();
				    String responseBody = response.toString();
					return responseBody;
				}
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
	            builder.append(RestConstant.lineSeparator);
	        }
	        return builder.toString();
	    }
		return null;
	}
	
	public static void main(String[] args) {
//		String doGet = doGet(null, null, "https://www.baidu.com");
//		System.out.println(doGet);
//		String doPostJSON = doPostJSON("{\"loginId\":\"sysadmin4\",\"password\":\"123456\"}", null, "https://wit-plat-testb-browser.gogen.cn/biz/login");
//		System.out.println(doPostJSON);
//		String doGet = doGet(null, null, "https://wit-plat-testb-browser.gogen.cn/biz/getUserDetailByUserUuid");
//		System.out.println(doGet);
	}
}
