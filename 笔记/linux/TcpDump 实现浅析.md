[toc]

# TcpDump 实现浅析

我前面学习了：

+  [BANote/网络通信中的五个钩子.md at master · BAByte/BANote (github.com)](https://github.com/BAByte/BANote/blob/master/笔记/linux/网络通信中的五个钩子.md)
+ [BANote/虚拟化网络设备tun.md at master · BAByte/BANote (github.com)](https://github.com/BAByte/BANote/blob/master/笔记/linux/虚拟化网络设备tun.md)

发现抓包和vpn技术都是在网络流转的过程中改变了数据包的流向。如果我们只是为了实现一个抓包应用，会有如下问题：

1. 不仅要改变数据包流向，还得把数据包还回去，相当于我们的抓包程序充当了一个中转，不管是流经三次协议栈还是两次，都必然会影响通信的性能，如果我们还对数据进行处理，产生的影响就更不用说了。
2. 如果数据包就是不流经tun0网口怎么办？还得改策略路由，很麻烦。

有没有可以指定监听网口，且不是拦截方法就能实现抓包呢？就是linux和安卓开发人员熟悉的tcpdump，而tcpdump又是基于libpcap实现的，所以我们先看看libpcap。

# libpcap

~~~txt

~~~



