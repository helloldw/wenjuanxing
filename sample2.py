import requests
import pymongo
from lxml.html import etree
from multiprocessing import Pool


class SelfIpProxy():
    def __init__(self):  # 设置区域
        self.depth = 10
        self.collection = pymongo.MongoClient()['Proxies']['free2']
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36',
            'Referer': 'https://www.xicidaili.com/nn/2'}

    def get_ip(self):  # 从网站获取ip
        urls = [
            'https://www.xicidaili.com/nn/{}'.format(i) for i in range(1, self.depth + 1)]
        for url in urls:
            html = requests.get(url, headers=self.headers, timeout=30)
            html.encoding = 'utf-8'
            e_html = etree.HTML(html.text)
            ips = e_html.xpath('//table[@id="ip_list"]/tr/td[2]/text()')
            ports = e_html.xpath('//table[@id="ip_list"]/tr/td[3]/text()')
            modes = e_html.xpath('//table[@id="ip_list"]/tr/td[6]/text()')
            for ip, port, mode in zip(ips, ports, modes):
                item = dict()
                item[mode.lower()] = '{}://{}:{}'.format(mode.lower(), ip, port)
                yield item

    def store_ip(self):
        for i in self.get_ip():
            self.collection.insert_one(i)

    def anti_duplicate(self): # 去重
        demo = self.collection.find({}, {'_id': 0}, no_cursor_timeout=True)
        l = []
        for i in demo:
            if i not in l:
                l.append(i)
        demo.close()
        self.collection.drop()
        for i in l:
            self.collection.insert_one(i)


def check_ip(proxy):
    url = {'http': "http://www.baidu.com", 'https': "https://www.baidu.com"}
    for key, value in proxy.items():
        try:
            html = requests.get(url[key], proxies={key: value}, timeout=10)
            html.encoding = 'utf-8'
            html.raise_for_status()
            print('***************************当前ip测试通过,当前ip为{}***************************\n'.format(value))
            pymongo.MongoClient()['Proxies']['checked'].insert_one(proxy)
        except:
            print('当前ip测试失败,当前ip为{}'.format(value))

if __name__ == '__main__':
    # 设置内容在class内部__init__()方法内部
    my_ip = SelfIpProxy()
    my_ip.store_ip()  # 获取存储ip到MongoDB中，已经成功, 很快，不需要多线程

    proxies = [] # 将库里的ip转成列表收集，以便多进程处理
    demo = my_ip.collection.find({}, {'_id': 0}, no_cursor_timeout=True) # 手动打开库，是因为库长度较长，防止时间过长，引起指针报错。
    for i in demo:
        proxies.append(i)
    my_ip.collection.drop()
    demo.close # 手动关闭库

    pool = Pool(8) # 开始多进程处理模式
    for i in range(len(proxies)):
        pool.apply_async(check_ip, args=(proxies[i], ))
    pool.close()
    pool.join()
    # my_ip.anti_duplicate() # 去重