package com.data5u.demo;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 测试无忧代理动态转发代理，本段代码支持请求HTTP和HTTPS协议的网址，比如http://www.example.com、https://www.example.com
 * @author www.data5u.com
 *
 */
@SuppressWarnings("all")
public class TestHttpsJsoup {
    
	public static void main(String[] args) {

		// 请求HTTPS网址务必加上下面这两行，否则会报错407
		System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
		
		// 固定为tunnel.data5u.com:56789
		final String httpsIp = "tunnel.data5u.com";
		final Integer httpsPort = 56789;
		final String order = "【把这里换成你的IP提取码】"; // 用户名
		final String pwd = "【把这里换成你的动态转发密码】"; // 密码
		final String targetUrl = "https://myip.ipip.net/"; // 要抓取的目标网址
		
		int requestTime = 5;
		
		for(int i = 0; i < requestTime; i++) {

			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {

						long startTime = System.currentTimeMillis();
						
						// 信任所有证书，当请求HTTPS网址时需要, 该部分必须在获取connection前调用
			            trustAllHttpsCertificates();
			            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			                public boolean verify(String urlHostName, SSLSession session) {
			                    return true;
			                }
			            });
			            
				    	Authenticator authenticator = new Authenticator() {
			    			public PasswordAuthentication getPasswordAuthentication() {
			    				return new PasswordAuthentication(order, pwd.toCharArray());
			    			}
			    		};
			    		Authenticator.setDefault(authenticator);
			    		
						Document document = Jsoup.connect(targetUrl)
				        		.proxy(httpsIp, httpsPort)
				        		.validateTLSCertificates(false)
								.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
								.timeout(10000)
				        		.get();
				        
				        long endTime = System.currentTimeMillis();
				        
						System.out.println(" [OK]" + "→→→→→" + (endTime - startTime) + "ms  " + "   " + document.title());
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println(" [ERR]" + "→→→→→" + e.getMessage());
					}
										
				}
			}).start();
			
		}
		
	}
	

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
	
}

		  
