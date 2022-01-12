[toc]

# TcpDump 实现浅析

我前面学习了：

+  [BANote/网络通信中的五个钩子.md at master · BAByte/BANote (github.com)](https://github.com/BAByte/BANote/blob/master/笔记/linux/网络通信中的五个钩子.md)
+ [BANote/虚拟化网络设备tun.md at master · BAByte/BANote (github.com)](https://github.com/BAByte/BANote/blob/master/笔记/linux/虚拟化网络设备tun.md)

发现抓包和vpn技术都是在网络流转的过程中改变了数据包的流向。如果我们只是为了实现一个抓包应用，会有如下问题：

1. 不仅要改变数据包流向，还得把数据包还回去，相当于我们的抓包程序充当了一个中转，不管是流经三次协议栈还是两次，都必然会影响通信的性能，如果我们还对数据进行处理，产生的影响就更不用说了。
2. 如果数据包就是不流经tun0网口怎么办？还得改策略路由，很麻烦。

有没有可以指定监听网口且不是拦截方法就能实现抓包呢？

# 数据的流动

我们先看正常情况下，网络数据在各部分的流动：（我们的设备可能会有多网卡的情况，这里就不画出来了）

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_5737c4de-c955-4349-aeb1-412703adb455.png?raw=true)

通过上面的两篇笔记，我们已经知道了大佬们在协议栈中能做的都做了，思路可能得转到更底层：网卡和驱动。

+ 物理网卡：物理网卡要是没有这样的设计，应该很难做到吧。
+ 网卡驱动：网卡驱动可以说是一个信息交换中心了，所以从网卡驱动入手应该是没问题的，但不同操作系统中驱动的实现会有差异，我们需要一个独立于平台hook驱动的api，而操作系统也需要对这些api提供实现，是的，要操作系统支持的。

有两个大佬选择了后者，设计出了libpcap库。

# libpcap

> Libpcap 是一个开源库，它为网络数据包捕获系统提供高级接口。它由加州大学伯克利分校劳伦斯伯克利国家实验室的研究人员 McCanne、Leres 和 Jacobson 于 1994 年创建，作为调查和改进 TCP 和 Internet 网关性能的研究项目的一部分。
> Libpcap 作者的主要目标是创建一个独立于平台的 API，以消除每个应用程序中依赖于系统的数据包捕获模块的需要，因为几乎每个操作系统供应商都实现了自己的捕获机制。
> libpcap API 旨在用于 C 和 C++。但是，有许多包装器允许在 Perl Python、Java、C# 或 Ruby 等语言中使用它。 Libpcap 在大多数类 UNIX 操作系统（Linux、Solaris、BSD、HP-UX...）上运行。还有一个名为 Winpcap 的 Windows 版本。今天，libpcap 由 Tcpdump Group 维护。完整的文档和源代码可从 tcpdump 的官方网站 http://www.tcpdump.org 获得。 （http://www.winpcap.org/ 对于 Winpcap)

libpcap是基于旁路机制实现的：

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_51f77fff-9a8f-408c-88a6-172d3e0b8c55.png?raw=true)

开篇提到：如果存在中间商，那上下游的数据传输会被我们打断。而这里是将驱动的数据拷贝了一份，接着给到了包过滤器进行处理，最后给到上层。这种不会破坏原本的网络数据包流转流程的机制，叫旁路机制。我们从细节上分析数据包的流向

+ 物理层：物理网卡在接收到数据后，会将数据给到网卡驱动并产生硬件中断
+ 链路层：驱动会将数据写到内核的数据接收队列中，然后产生软中断，操作系统会根据数据类型选择对应的程序回调函数，而libpcap在系统中注册了所有类型（ptype_all）的回调函数，所以libpcap可以接收到所有从网卡驱动来的数据（这里不懂的可以去看看io模型），然后给到过滤器，过滤器根据过滤规则决定要拷贝什么数据到应用缓存区。

所以libpcap工作的主要流程：旁路捕获数据，根据过滤机制决定拷贝什么数据，数据给到上层。

# 结

使用libpcap的api可以实现网络监控应用，TcpDump就是基于libpcap实现的命令行工具，他就是运行在用户空间的网络监控应用。虽然libpcap很强大，但它需要系统平台的支持。libpcap 的api文档：[Home | TCPDUMP & LIBPCAP](https://www.tcpdump.org/)

