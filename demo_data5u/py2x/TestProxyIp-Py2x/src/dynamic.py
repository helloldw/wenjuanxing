# -*- coding: UTF-8 -*- 

'''
Python 2.X
无忧代理IP Created on 2017年08月21日
描述：本DEMO演示了使用无忧代理IP请求网页的过程，代码使用了多线程
逻辑：每隔5秒从API接口获取IP，对于每一个IP开启一个线程去抓取网页源码
@author: www.data5u.com
'''
#coding=utf-8

import time
import threading
import requests
import sys

# 解决编码报错问题
reload(sys)
sys.setdefaultencoding('utf8')

ips = []

# 爬数据的线程类
class CrawlThread(threading.Thread):
    def __init__(self,proxyip):
        super(CrawlThread, self).__init__()
        self.proxyip=proxyip
    def run(self):
        # 开始计时
        start = time.time()

        # 请求头
        headers = {
            'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36'
        }
        
        #使用代理IP请求网址，注意第三个参数verify=False意思是跳过SSL验证（可以防止报SSL错误）
        html=requests.get(
            url=targetUrl, 
            proxies={"http" : "http://" + self.proxyip, "https" : "https://" + self.proxyip}, 
            verify=False, 
            timeout=15,
            headers=headers
        ).content.decode()
        
        # 结束计时
        end = time.time()
        # 输出内容
        print ( threading.current_thread().getName() +  "耗时 " + str(end - start) + "毫秒 " + self.proxyip + " 获取到如下HTML内容：\n" + html + "\n*************" )

# 获取代理IP的线程类
class GetIpThread(threading.Thread):
    def __init__(self,fetchSecond):
        super(GetIpThread, self).__init__()
        self.fetchSecond=fetchSecond
    def run(self):
        global ips
        while True:
            # 获取IP列表
            res = requests.get(apiUrl).content.decode()
            # 按照\n分割获取到的IP
            ips = res.split("\n")
            # 利用每一个IP
            for proxyip in ips:
                
                if proxyip.strip()=='' :
                    continue
                
                print proxyip
                # 开启一个线程
                CrawlThread(proxyip).start()
            # 休眠
            time.sleep(self.fetchSecond)   

if __name__ == '__main__':
    # 获取IP的API接口
    apiUrl = "http://api.ip.data5u.com/dynamic/get.html?order=【把这里换成你的IP提取码】&sep=3"
    # 要抓取的目标网站地址
    targetUrl = "http://myip.ipip.net"
    # 获取IP时间间隔，建议为5秒
    fetchSecond = 5
    # 开始自动获取IP
    GetIpThread(fetchSecond).start()