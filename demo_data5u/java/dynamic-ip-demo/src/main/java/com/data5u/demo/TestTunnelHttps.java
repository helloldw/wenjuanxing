package com.data5u.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * 测试无忧代理动态转发代理，本段代码支持请求HTTP和HTTPS协议的网址，比如http://www.example.com、https://www.example.com
 * @author www.data5u.com
 *
 */
public class TestHttps {

    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
    
    static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }
    
	public static void main(String[] args) {

		// 如果爬虫请求HTTPS网址，必须加入这两行
		System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
	    
		// 固定为tunnel.data5u.com:56789
		final String httpsIpport = "tunnel.data5u.com:56789";
		final String order = "【把这里换成你的IP提取码】"; // 用户名
		final String pwd = "【把这里换成你的动态转发密码】"; // 密码
		final String targetUrl = "https://myip.ipip.net/"; // 要抓取的目标网址
		
		int requestTime = 5;
		for(int i = 0; i < requestTime; i++) {
			final int x = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {

						long startTime = System.currentTimeMillis();

			    		// 如果爬虫请求HTTPS网址，必须加入这两行
			    		System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
			    		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
			    		
						// 信任所有证书，当请求HTTPS网址时需要
						// 该部分必须在获取connection前调用
			            trustAllHttpsCertificates();
			            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			                public boolean verify(String urlHostName, SSLSession session) {
			                    return true;
			                }
			            });

				    	URL link = new URL(targetUrl);
			    		
						Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress((httpsIpport.split(":"))[0], Integer.parseInt((httpsIpport.split(":"))[1])));
						HttpURLConnection connection = (HttpURLConnection)link.openConnection(proxy);
						
						// Java系统自带的鉴权模式，请求HTTPS网址时需要
			    		Authenticator.setDefault(new Authenticator() {
			    			public PasswordAuthentication getPasswordAuthentication() {
			    				return new PasswordAuthentication(order, pwd.toCharArray());
			    			}
			    		});
			    		
						connection.setRequestMethod("GET");
				    	connection.setDoInput(true);
				    	connection.setDoOutput(true);
				    	connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36");
						
				    	connection.setUseCaches(false);
				    	connection.setConnectTimeout(60000);
			    		
			    		connection.connect();
			    		
				        String line = null;
				        StringBuilder html = new StringBuilder();
				        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
				        while((line = reader.readLine()) != null){
				        	html.append(line);
				        }
				        try {
							if (reader != null) {
								reader.close();
							}
						} catch (Exception e) {
						}
				        
				        connection.disconnect();
				        
				        long endTime = System.currentTimeMillis();
				        
						System.out.println(x + " [OK]" + "→→→→→" + targetUrl + "  " + (endTime - startTime) + "ms  " + connection.getResponseCode() + "   " + html.toString());
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println(x + " [ERR]" + "→→→→→" + e.getMessage());
					}					
				}
			}).start();
		}
		
	}
	
}

		  
