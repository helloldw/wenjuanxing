package com.data5u.demo;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * 测试无忧代理限量和不限量套餐
 * @author www.data5u.com
 *
 */
public class TestDynamicIp {
	public static List<String> ipList = new ArrayList<>();
	public static boolean gameOver = false;
	public static void main(String[] args) {
		
		System.out.print(System.currentTimeMillis());
		
		// 提取IP的时间间隔
		long fetchIpSeconds = 5;
		int testTime = 5;
		
		String apiUrl = "http://api.ip.data5u.com/dynamic/get.html?order=【把这里换成你的IP提取码】&sep=3";
		// 改为你要抓的目标网址
		String targetUrl = "http://myip.ipip.net/";
		// 是否加载JS，加载JS会导致速度变慢
		boolean useJS = false;
		// 请求超时时间，单位毫秒，默认20秒
		int timeOut = 20000;
		
		System.out.println(">>>>>>>>>>>>>>动态IP测试开始<<<<<<<<<<<<<<");
		System.out.println("***************");
		System.out.println("提取IP间隔 " + fetchIpSeconds + " 秒 ");
		System.out.println("超时时间 " + timeOut + " 毫秒 ");
		System.out.println("爬虫目标网址  " + targetUrl);
		System.out.println("***************\n");
		TestDynamicIp tester = new TestDynamicIp();
		
		// 定时获取IP
		new Thread(tester.new GetIP(fetchIpSeconds * 1000, testTime, apiUrl, targetUrl, useJS, timeOut)).start();
	
		while(!gameOver){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		System.out.println(">>>>>>>>>>>>>>动态IP测试结束<<<<<<<<<<<<<<");
		System.exit(0);
	}
    
	// 采集线程类
	public class Crawler extends Thread{
		@Override
		public void run() {
			webParseHtml(targetUrl);
		}
		
		long sleepMs = 200;
		boolean useJs = false;
		String targetUrl = "";
		int timeOut = 5000;
		String ipport = "";
		
		public Crawler(long sleepMs, String targetUrl, boolean useJs, int timeOut, String ipport) {
			this.sleepMs = sleepMs;
			this.targetUrl = targetUrl;
			this.useJs = useJs;
			this.timeOut = timeOut;
			this.ipport = ipport;
		}
		public String webParseHtml(String url) {
			String html = "";
			BrowserVersion[] versions = {BrowserVersion.BEST_SUPPORTED, BrowserVersion.CHROME, BrowserVersion.EDGE, BrowserVersion.FIREFOX_45};
			WebClient client = new WebClient(versions[(int)(versions.length * Math.random())]);
			try {
				client.getOptions().setThrowExceptionOnFailingStatusCode(false);
				client.getOptions().setJavaScriptEnabled(useJs);
				client.getOptions().setCssEnabled(false);
				client.getOptions().setThrowExceptionOnScriptError(false);
				client.getOptions().setTimeout(timeOut);
				client.getOptions().setAppletEnabled(true);
				client.getOptions().setGeolocationEnabled(true);
				client.getOptions().setRedirectEnabled(true);
				client.getOptions().setUseInsecureSSL(true);
				
				// 此处可以设置Referer等Header的值
				client.addRequestHeader("Referer", "http://www.example.com/");
				
				if (ipport != null) {
					ProxyConfig proxyConfig = new ProxyConfig((ipport.split(",")[0]).split(":")[0], Integer.parseInt((ipport.split(",")[0]).split(":")[1]));
					client.getOptions().setProxyConfig(proxyConfig); // 此处设置代理IP
				}else {
					System.out.print(".");
					return "";
				}
			
				long startMs = System.currentTimeMillis();
				
				Page page = client.getPage(url);
				WebResponse response = page.getWebResponse();
				
				/** 输出header信息
				List<NameValuePair> headers = response.getResponseHeaders();
				for (NameValuePair nameValuePair : headers) {
					System.out.println(nameValuePair.getName() + "-->" + nameValuePair.getValue());
				}
				**/
				
				if (response.getContentType().equals("application/json")) {
					html = response.getContentAsString();
				}else if(page.isHtmlPage()){
					html = ((HtmlPage)page).asXml();
				}
				
				long endMs = System.currentTimeMillis();
				
				Document doc = Jsoup.parse(html);
				System.out.println(getName() + " " + ipport.trim() + " 用时 " + (endMs - startMs) + "毫秒 ：" + doc.select("title").text()); // 这里输出页面的title内容
			
			} catch (Exception e) {
				System.err.println(ipport + ":" + e.getMessage());
			} finally {
				client.close();
			}
			return html;
		}
		
	}
	
	// 定时获取动态IP
	public class GetIP implements Runnable{
		long sleepMs = 1000;
		int maxTime = 3;
		String apiUrl = "";
		String targetUrl;
		boolean useJs;
		int timeOut;
		
		public GetIP(long sleepMs, int maxTime, String apiUrl, String targetUrl, boolean useJs, int timeOut) {
			this.sleepMs = sleepMs;
			this.maxTime = maxTime;
			this.apiUrl = apiUrl;
			this.targetUrl = targetUrl;
			this.useJs = useJs;
			this.timeOut = timeOut;
		}
		
		@Override
		public void run() {
			int time = 1;

			while(!gameOver) {
				if(time >= 4){
					gameOver = true;
				}
				try {
					java.net.URL url = new java.net.URL(apiUrl);
					
			    	HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			    	connection.setConnectTimeout(3000);
			    	connection = (HttpURLConnection)url.openConnection();
			    	
			        InputStream raw = connection.getInputStream();  
			        InputStream in = new BufferedInputStream(raw);  
			        byte[] data = new byte[in.available()];
			        int bytesRead = 0;  
			        int offset = 0;  
			        while(offset < data.length) {  
			            bytesRead = in.read(data, offset, data.length - offset);  
			            if(bytesRead == -1) {  
			                break;  
			            }  
			            offset += bytesRead;  
			        }  
			        in.close();  
			        raw.close();
					String[] res = new String(data, "UTF-8").split("\n");
					
					for (String ip : res) { // 获取到的每个IP都开一个新线程抓取
						new Crawler(100, targetUrl, useJs, timeOut, ip).start();
					}
				} catch (Exception e) {
					System.err.println(">>>>>>>>>>>>>>获取IP出错, " + e.getMessage());
				}
				try {
					Thread.sleep(sleepMs);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		
		}
	}
	
}
