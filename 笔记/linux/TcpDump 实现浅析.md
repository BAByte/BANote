[toc]

# TcpDump 实现浅析

在了解了:

+  [BANote/网络通信中的五个钩子.md at master · BAByte/BANote (github.com)](https://github.com/BAByte/BANote/blob/master/笔记/linux/网络通信中的五个钩子.md)
+ [BANote/虚拟化网络设备tun.md at master · BAByte/BANote (github.com)](https://github.com/BAByte/BANote/blob/master/笔记/linux/虚拟化网络设备tun.md)

发现抓包和vpn技术都是在网络流转的过程中改变了数据包的流向。如果我们只是为了实现一个抓包应用，不仅要改变数据包流向，还得把数据包还回去，相当于我们的抓包程序充当了一个中转，这必然会影响原本数据包流动的性能，有没有其他不是拦截方法拿到数据包呢？

