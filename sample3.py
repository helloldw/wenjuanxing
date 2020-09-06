from sample import ip_list

# print(ip_list)
from urllib import request
import random
proxy_list = [
  {"https":"171.35.171.154:9999"}
#   {"http":"61.135.217.7:80"},
#   {"http":"42.231.165.132:8118"}
]
proxy = random.choice(proxy_list)                    #随机选择一个ip地址
httpproxy_handler = request.ProxyHandler(proxy)
opener = request.build_opener(httpproxy_handler)
request = request.Request("http://www.baidu.com/")
response =opener.open(request)
print(response.read())