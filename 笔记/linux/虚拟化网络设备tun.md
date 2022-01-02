[toc]

# 虚拟化网络设备tun

> 在[计算机网络](https://zh.wikipedia.org/wiki/计算机网络)中，**TUN**与**TAP**是操作系统内核中的虚拟网络设备。不同于普通靠硬件[网络适配器](https://zh.wikipedia.org/wiki/网络适配器)实现的设备，这些虚拟的网络设备全部用软件实现，并向运行于[操作系统](https://zh.wikipedia.org/wiki/操作系统)上的软件提供与硬件的网络设备完全相同的功能。
>
> **TAP**等同于一个[以太网](https://zh.wikipedia.org/wiki/以太网)设备，它操作[第二层](https://zh.wikipedia.org/wiki/OSI模型)数据包如[以太网](https://zh.wikipedia.org/wiki/以太网)数据帧。**TUN**模拟了[网络层](https://zh.wikipedia.org/wiki/网络层)设备，操作[第三层](https://zh.wikipedia.org/wiki/OSI模型)数据包比如[IP](https://zh.wikipedia.org/wiki/IP地址)数据包。

本文我们只学习tun。

# tun

我们用一个安卓设备连接无线网络，其无线网卡对应的网络接口是wlan0，其背后对应的是真实的物理网卡，当应用程序对外发送网络数据包时，数据流向如下：

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_2da42426-abcf-4433-91d3-f6ee3ecc39f1.png?raw=true)

当我们应用使用系统调用创建了一个tun虚拟网卡后，会得到这个设备的文件描述符，用于数据的读写。同时我们可以看到系统中会出现tun0这个网络接口，可以使用ifconfig或ipconfig查看：

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_97666ee0-1249-4ad1-a111-161934f35b21.png?raw=true)

tun0接口背后是创建该接口的应用程序，这个应用程序可以对流经tun0的数据进行读取，也可以向tun0写入数据，tun0会将写入的数据再丢到网络协议栈中。有了这些功能，开发者就可以做出很多花里胡哨的应用，最常见的有：vpn程序、抓包程序，我下面就统称为CaptureApp。

我们看看一个CaptureApp程序和tun0对原本网络通信干预的过程：

注：我这里先列出总体的流程，步骤的细节后面一点再看。

# 发送数据

当应用程序对外发送网络数据包时，数据流向如下：

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_234535e5-5c23-4322-b5a2-33edbe158aff.png?raw=true)

1. app通过socket接口发送数据。
2. 内核中将数据都路由到tun0接口。（为什么会路由到tun0？这里记为问题1 下文会讲到）
3. CaptureApp通过io模型中的其中一种模型，知道了tun0设备有数据可以读取，这一步将内核态的网络数据拷贝到了用户态的进程中。用户进程就可以对这些数据进行处理。
4. CaptureApp使用socket接口将数据丢回协议栈。
5. 数据被路由到Wlan0接口，并通过物联网卡与互联网进行通信。（为什么会路由到wlan0？下面会讲，这里记为问题2）
6. 数据包出去环游世界。

这里的流程一共经过两次协议栈！

# 接收数据

当通过CaptureApp程序发送的数据被外部服务器响应时，数据流向如下

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_88887169-6b1e-4404-8aa0-d5b9737b9f8e.png?raw=true)

1. 服务端响应后的数据通过物理网卡进来。
2. 流转到协议栈中。
3. 数据包通过协议栈流转到CaptureApp，因为请求包是由CaptureApp发出的，端口号对对应的进程是CaptureApp。CaptureApp处理数据
4. CaptureApp将处理后的数据写到tun0。
5. tun0将数据丢回到协议栈。（仿佛数据是从外部流入的一样）
6. 数据包流经协议栈流转到了App（这里很好理解，就是端口号指定了app），App并不知道CaptureApp处理过了数据。

这里的流程一共经过两次协议栈！

# 问题1-为什么会路由到tun0

在对上层应用的数据包进行分流时，采用的是多路由表的方式，我们可以定义多个路由策略，内核根据路由策略选择路由表。

## linux默认路由表的补充知识：

> 在 Linux 系统启动时，内核会为路由策略数据库配置三条缺省的规则：
>
> 0：匹配任何条件，查询路由表local(ID 255)，该表local是一个特殊的路由表，包含对于本地和广播地址的优先级控制路由。rule 0非常特殊，不能被删除或者覆盖。
>
> 匹配任何条件，查询路由表main(ID 254)，该表是一个通常的表，包含所有的无策略路由。系统管理员可以删除或者使用另外的规则覆盖这条规则。
>
> 匹配任何条件，查询路由表default(ID 253)，该表是一个空表，它是后续处理保留。对于前面的策略没有匹配到的数据包，系统使用这个策略进行处理，这个规则也可以删除。
>
> **注：**不要混淆路由表和策略：规则指向路由表，多个规则可以引用一个路由表，而且某些路由表可以策略指向它。如果系统管理员删除了指向某个路由表的所有规则，这个表没有用了，但是仍然存在，直到里面的所有路由都被删除，它才会消失。
>
> 来源：[linux网络知识：路由策略（ip rule，ip route） - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/144585950)



## 策略路由

策略路由是指主机存在多张网卡时，可以根据一些策略选择对应的路由表，进而决定使用哪张网卡进行网络通信。

选择的路由策略的过程：

1. 根据路由策略优先级选，先选优先级高的
2. 我们可以使用iptables的mangle规则对数据包的标记实现（安卓上略有不同，后面会讲到），而路由策略声明自己适合哪些标记的数据包
3. 但在安卓上的路由策略还增加了uid的范围匹配，因为安卓会使用uid区分应用

如果我们创建了tun0虚拟网卡，并将ip设置为10.1.10.1（这时系统会自动创建tun0设备对应名为tun0的路由表），并告诉系统只有uid = 10068的应用数据需要路由到tun0。我们看看操作后的路由策略：

**（注：ip rule list 是安卓查看路由策略的命令）**

~~~txt
XP12:/ # ip rule list
0:	from all lookup local
10000:	from all fwmark 0xc0000/0xd0000 lookup legacy_system
10500:	from all iif lo oif wlan0 uidrange 0-0 lookup wlan0
11000:	from all iif tun0 lookup local_network
12000:	from all fwmark 0x0/0x20000 iif lo uidrange 10068-10068 lookup tun0
12000:	from all fwmark 0xc0088/0xcffff lookup tun0
13000:	from all fwmark 0x10063/0x1ffff iif lo lookup local_network
13000:	from all fwmark 0x10072/0x1ffff iif lo lookup wlan0
13000:	from all fwmark 0x10088/0x1ffff iif lo uidrange 10068-10068 lookup tun0
13000:	from all fwmark 0x10088/0x1ffff iif lo uidrange 0-0 lookup tun0
14000:	from all iif lo oif wlan0 lookup wlan0
14000:	from all iif lo oif tun0 uidrange 10068-10068 lookup tun0
15000:	from all fwmark 0x0/0x10000 lookup legacy_system
16000:	from all fwmark 0x0/0x10000 lookup legacy_network
17000:	from all fwmark 0x0/0x10000 lookup local_network
19000:	from all fwmark 0x72/0x1ffff iif lo lookup wlan0
21000:	from all fwmark 0x88/0x1ffff lookup wlan0
22000:	from all fwmark 0x0/0xffff iif lo lookup wlan0
32000:	from all unreachable
~~~

第一列代表优先级，数字越小优先级越高，第一条代表的意思是：

所以所有的数据都先从名为local的路由表查找路由。接下来我们看看local路由表的内容：

~~~txt
XP12:/ # ip route list table local
local 10.1.10.1 dev tun0 proto kernel scope host src 10.1.10.1
broadcast 127.0.0.0 dev lo proto kernel scope link src 127.0.0.1
local 127.0.0.0/8 dev lo proto kernel scope host src 127.0.0.1
local 127.0.0.1 dev lo proto kernel scope host src 127.0.0.1
broadcast 127.255.255.255 dev lo proto kernel scope link src 127.0.0.1
broadcast 192.168.31.0 dev wlan0 proto kernel scope link src 192.168.31.141
local 192.168.31.141 dev wlan0 proto kernel scope host src 192.168.31.141
broadcast 192.168.31.255 dev wlan0 proto kernel scope link src 192.168.31.141
~~~

可以看到第一条就指向了tun0这张路由表，（上文我们提到过：当我们给虚拟网卡分配ip时就会自动创建对应的路由表）。ip地址前面声明了个local，意味着本机内进程的网络通信，目的地址为10.1.10.1的数据包，会从tun0路由表查找路由。

看看tun0路由表的内容：

~~~txt
XP12:/ # ip route list table tun0
//所有数据包发送到tun0设备
default dev tun0 proto static scope link
//目的地址为10.1.10.1的数据包也发到tun0设备
10.1.10.1 dev tun0 proto static scope link
~~~

我们现在就知道了：tun0路由表会把所有数据包都发到tun0接口，进而提供数据给连接tun0端的CaptureApp。

那和目的地址不为10.1.10.1的数据包又是怎么匹配到tun0路由表的呢？ 路由策略还有其他的匹配规则，我们先一一看下规则含义

1. form all？

   没有明确指定网卡ip的数据包，form eth0是明确的指定了eth0的数据包，form 192.168.21.1是明确指定ip为192.168.21.1的网卡

2. mark是什么？

   数据包会带有一些标记用对数据包做追踪，而设计者为了让标记灵活性和扩展性更加灵活，允许对标记进行与或运算而实现多重标记功能，这种标记位的使用很常见（可以看我几年前写的笔记：[BANote/安卓源码中常用的Flag语句.md at master · BAByte/BANote (github.com)](https://github.com/BAByte/BANote/blob/master/笔记/读安卓开发艺术探索笔记/安卓源码中常用的Flag语句.md)），而策略路由中需要配合fwmark使用。

3. fwmark是什么？

   在路由策略中对数据包mark进行匹配的匹配规则，路由策略上的fwmark通常格式有两种：值、值/掩码。如果只有值就只关心数据包的标记是否和路由策略声明的一致；有掩码的情况下，需要将数据包的标记与掩码相与，再将相与的结果和策略的值比较，如果一致就是命中。例子：标记一般是十六进制，但这里我用二进制举例子，数据包携带的标记二进制为111，路由策略上声明的是：100/100，本例中可以命中。

4. 16进制mark的含义？

   这个得看场景而定，不同人使用mark时对其各个位的功能定义不一样，安卓使用了17位，从低位到高位的定义如下：

   ~~~c++
   //安卓标记：
   //http://aospxref.com/android-9.0.0_r61/xref/system/netd/include/Fwmark.h?fi=FWMARK_NET_ID_MASK#FWMARK_NET_ID_MASK
   union Fwmark {
   25      uint32_t intValue;
   26      struct {
   27          unsigned netId          : 16; //网卡id
   28          bool explicitlySelected :  1; //数据包是否指定了网卡
   29          bool protectedFromVpn   :  1; //是否绕过vpn接口
   30          Permission permission   :  2; //数据包发出应用的权限，11代表系统应用，且有网络权限，10代表有网络权限的应用
   31          bool uidBillingDone     :  1;  // 我也不晓得啥意思
   32      };
   ~~~

5. uidrange 10068-10068是什么意思？

   安卓中对路由策略的匹配新增了一个uid匹配的条件，这里是必须是uid范围在 10068-10068的应用数据包才能会被匹。

6. iif和oif是什么意思？

   表明数据是从哪个网口输入的，那个网口输出的。

了解了这些知识后我们再次看看各路由策略的含义

~~~txt
XP12:/ # ip rule list
0:	from all lookup local
//没有指定网口ip，并有网络权限的应用查找legacy_system表
10000:	from all fwmark 0xc0000/0xd0000 lookup legacy_system

//没有指定网口ip，指定输出到wlan0网口的root权限应用查找wlan0
10500:	from all iif lo oif wlan0 uidrange 0-0 lookup wlan0

//没有指定网口ip，从tun0网口输入的查找local_network
11000:	from all iif tun0 lookup local_network

//没有指定网口ip，没有标记，且从lo网口输入，uid是10068发出的数据包查找tun0路由表
12000:	from all fwmark 0x0/0x20000 iif lo uidrange 10068-10068 lookup tun0

//没有指定网口ip，指定网络id为88的数据包，查找tun0路由表
12000:	from all fwmark 0xc0088/0xcffff lookup tun0

//没有指定网口ip，从lo网口输入，查找local_network路由表
13000:	from all fwmark 0x10063/0x1ffff iif lo lookup local_network

//没有指定网口ip，从lo网口输入，查找wlan0路由表
13000:	from all fwmark 0x10072/0x1ffff iif lo lookup wlan0

//没有指定网口ip，从lo网口输入，查找tun0路由表
13000:	from all fwmark 0x10088/0x1ffff iif lo uidrange 10068-10068 lookup tun0

//没有指定网口ip，从lo网口输入，由root应用发出的数据包，查找tun0路由表
13000:	from all fwmark 0x10088/0x1ffff iif lo uidrange 0-0 lookup tun0

//没有指定网口ip，从lo网口输入，从wlan0网口输出的数据包，查找wlan0路由表
14000:	from all iif lo oif wlan0 lookup wlan0

//没有指定网口ip，从lo网口输入，从tun0网口输出，uid是10068发出的数据包，查找tun0路由表
14000:	from all iif lo oif tun0 uidrange 10068-10068 lookup tun0

//没有指定网口ip，查找legacy_system、legacy_network、local_network路由表
15000:	from all fwmark 0x0/0x10000 lookup legacy_system
16000:	from all fwmark 0x0/0x10000 lookup legacy_network
17000:	from all fwmark 0x0/0x10000 lookup local_network

//没有指定网口ip，指定网络id为72，从lo接口输入的数据包，查找wlan0路由表
19000:	from all fwmark 0x72/0x1ffff iif lo lookup wlan0

//没有指定网口ip，这里的指定网卡标记位不管是0还是1，只要网络id为88，查找wlan0路由表
21000:	from all fwmark 0x88/0x1ffff lookup wlan0

//没有指定网口ip，没有标记，从lo输入的数据包，查找wlan0路由表
22000:	from all fwmark 0x0/0xffff iif lo lookup wlan0
32000:	from all unreachable
~~~

至此，我们就知道：

1. 路由策略优先级优先将目的地址为10.1.10.1通过tun0路由表选择路由
2. 指定网络id为88的数据包，查找tun0路由表
3. 没有指定网口ip，没有标记，且从lo网口输入，uid是10068发出的数据包查找tun0路由表
4. 没有指定网口ip，从lo网口输入，由root应用发出的数据包，查找tun0路由表
5. 没有指定网口ip，从lo网口输入，从tun0网口输出，uid是10068发出的数据包，查找tun0路由表

# 问题2-CaptureApp发出的数据包为什么会从wlan0出去

在问题一中其实已经解答了：通过策略路由。而安卓中的策略路由比较有意思，有关tun0路由表的路由策略都要求protectedFromVpn = 0，所以protectedFromVpn=1是不会被路由到wlan0的。

# 数据包标记是什么时候打上的？

+ linux上使用iptables的规则链打，一般是通过数据包的源地址和目的地址打上不同的标记，或者直接通过数据包的源地址和目的地址设置选择路由的策略，或者端口等，可以自行查看iptables相关内容。

+ 安卓并没有用的iptables的规则链打标记的方式实现策略路由，安卓是基于socket来打标记的，FwmarkService负责对监听和标记数据包，细节我就不分析了（安卓策略路由实现我目前还很模糊，我用了很多天才发现安卓的策略路由和linux不一样，所以我会另开一篇笔记进行分析，本篇主要是讲虚拟网卡tun0）。

# CaptureApp如何处理数据

开篇也提到，其实是有两个常见虚拟设备tun和tap，所以CaptureApp获取到的数据是ip数据报或者帧。而socket接口只支持发送tcp、udp包：

+ 假设CaptureApp是一个抓包app

  可以将其恢复成各协议层的数据格式，并将各层的数据展示给用户，然后可以直接使用udp将数据发出去。

+ 假设CaptureApp是一个vpnApp

  你需要对数据包进行加密，然后转发给代理服务器。

对于ip数据包的处理，常见有两种实现，可以自行去了解：OpenVPN 和 tun2socks.so。

# 结

这篇笔记涉及到虚拟网络设备tun，策略路由，vpn的相关知识。不会很具体也不会有实操，只是简单的谈谈整体思路。

我本意是想知道tun是什么，能干什么，但在了解策略路由时我钻了牛角尖，花了很大力气去研究安卓的策略路由所以浪费了很多时间。而tun2socks.so这些库本身也是比较有意思和复杂的东西，本笔记要是写太多就会很杂乱。

现在我们已经知道了常用的抓包工具是通过代理（看这个：[BANote/网络通信中的五个钩子.md at master · BAByte/BANote (github.com)](https://github.com/BAByte/BANote/blob/master/笔记/linux/网络通信中的五个钩子.md)）和vpn实现，那linux中的tcpdump工具又是怎么实现抓包的呢？请看：（todo 写了后贴链接在这里）



