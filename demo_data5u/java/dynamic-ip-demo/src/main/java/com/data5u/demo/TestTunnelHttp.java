package com.data5u.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 测试无忧代理动态转发代理，本段代码仅支持请求HTTP协议的网址，比如http://www.example.com
 * @author www.data5u.com
 *
 */
public class TestHttp {

	public static void main(String[] args) {
		
		final int requestTime = 5;
		
		// 要抓取的网址
		final String targetUrl = "http://myip.ipip.net/";
		
		// 固定为tunnel.data5u.com:56789
		String httpsIpport = "tunnel.data5u.com:56789";
		
		// Proxy-Authorization的值，在用户中心-中查看动态转发订单，点击接入按钮查看
		String proxyPass = "【把这里换成你的动态转发串】" ;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		
		for(int i = 0; i < requestTime; i++) {
			
			final int reqNo = i ;
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					
					try {
						long startTime = System.currentTimeMillis();
						
				    	HttpURLConnection connection = null;
				    	URL link = new URL(targetUrl);
				    	
				    	String type = "HTTP";
						Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress((httpsIpport.split(":"))[0], Integer.parseInt((httpsIpport.split(":"))[1])));
			    		connection = (HttpURLConnection)link.openConnection(proxy);
				    	
				    	connection.setDoOutput(true);
				    	connection.setRequestProperty("Proxy-Authorization", "Basic " + proxyPass);
				    	connection.setUseCaches(false);
				    	connection.setConnectTimeout(6000);
				    	
				        String line = null;
				        StringBuilder html = new StringBuilder();
				        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
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
				        
						System.out.println(reqNo + "-" + sdf.format(new Date()) + " [OK]" + "→→→→→" + type + "  " + targetUrl + "  " + (endTime - startTime) + "ms  " + connection.getResponseCode() + "   " + html);
					} catch (Exception e) {
						System.err.println(reqNo + "-" + sdf.format(new Date()) + " [ERR]" + "→→→→→" + targetUrl + "  " + e.getMessage());
					}
				}
			}).start();
			
		}
		
	}
	
}


		  
