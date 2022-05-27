# WebRTC连接过程

# 背景

webRtc就是一个集大成者，其使用了很多现有的标准协议进行组合，为浏览器和移动应用提供p2p或relay实时通信的api，支持在对等方之间发送视频、语音和通用数据，常用在实时视频、共享桌面、多人游戏等场景。

所以webrtc主要围绕在建立对等连接、音视频的采集、流式传输、音视频的处理几个方面。

本文主要梳理建立对等连接部分的流程，这部分在webrtc中是比较简单的，你可直接去阅读文末的相关文档。



注：阅读本文前需要提前了解NAT和内网穿透的相关知识，由于这些知识属于计算机网络的基础内容，本文就不赘述了。

# p2p或c\s：

如实时视频聊天的场景下，如果使用c\s架构：

1. 数据会经过中继服务器的处理和转发。

2.中继服务器决定了通信质量的好坏和规模。

3.中继服务器的流量成本高。

为了尽可能的优化上述问题，webRtc设计了优先p2p的策略。为什么说是优先呢？由于nat的存在，p2p对等连接不一定能建立成功，如果建立失败，会采用中继转发（c\s）的方式建立两端的连接，关于nat的相关知识请自行学习（比较简单）。

# 总体流程

所以建立连接的关键步骤就是，client们需要（client:建立连接的双方）：

1.找到本地网卡ip：例如host、内网ip等。需要host的原因：可能是两个本地应用进行连接的建立。

2.找到client的公网ip和端口，和中继服务器的中继通道ip和端口。

3.上述的几个信息发送给对方。

4.对方使用上方的信息尝试建立连接。

# ICE

webrtc使用Trickle ICE完成上述流程，Trickle ICE 是ICE的优化版本，但大部分特性是一致的，所以都要了解一下，不过在了解ICE前，我们先简单的了解这个过程中出现的角色：

1.client：主动发起方和被动发起连接方，在ice中定义为控制方和受控方。（双方都可以是连接的发起者，所以可能在过程中可能会有角色冲突的问题但本文不提及，感兴趣自己查）。

2.信令服务器：在未建立对等连接前，client就是通过信令服务器进行通信的，该服务器需要自己部署，具体的通信协议不做要求，但webrtc有定义传输的数据结构：SessionDescription（sdp）。

3.STUN:会话穿越服务，client可以通过STUN知道确认自己的Nat类型，进而确认是否支持建立对等连接，该服务器需要自己部署。

4.TURN：中继服务，当无法建立p2p对等连接后，直接使用中继服务器进行连接的建立，该服务器需要自己部署。

注：
1. ice：Interactive Connectivity Establishment 

2.ice类型：有full ice和lite ice，本文的ice都指full ice。

## “传统”ICE

ICE：应答式的交互式连接协议，这是在webrtc之前就有的协议，它是串行的，这可以保证建立连接过程的可靠性（关于计算机网络中对可靠性的定义请自己查），但牺牲了性能（建立连接的时间长），所以webrtc并没有采用这协议，流程如下图：

offer：webrtc用来描述音视频信息的数据。

candidate：包含ip和端口信息。

 ![img](https://github.com/BAByte/pic/blob/master/image2022-5-24_9-35-17.png?raw=true)



ice流程：

1.clientA采集candidate的信息然后和offer信息一起，通过信令服务器发送给clientB，clientB收到携带candidate和offer的sdp后才进行candidate的采集。

2.最后再answer clientA。

3.双方使用candidate尝试连接。

4.建立连接成功。

## Trickle ICE：

流程如下图所示，蓝色，黄色和红色框，紫色的部分在是并行的，这是为了提升连接的建立速度，但需要开发者自行保证流程的可靠性，webRtc采用了这种协议：

![img](https://github.com/BAByte/pic/blob/master/image2022-5-24_9-38-46.png?raw=true)



1.candidate开始采集的时机：双方client一旦createOffer完成 或 收到offer成功，就会立即开始采集candidate，同时进行offer的发送，查询到candidate后就立即发送给对端，双方立即进行连通性测试。

2.使用candidate的时机：在wetrtc中是要求必须具备offer才能添加candidate，但由于offer和candidate是分开发送的，是否需要保证可靠性，取决于你的信令服务器：

[pc/sdp_offer_answer.cc - src - Git at Google (googlesource.com)](https://webrtc.googlesource.com/src/+/refs/heads/main/pc/sdp_offer_answer.cc)

![img](https://github.com/BAByte/pic/blob/master/image2022-5-24_10-21-2.png?raw=true)

注：
0.上图中的sdp特指携带offer的sdp。
1.蓝色，黄色，紫色和红色框的部分在trickle ICE中是并行的。
2.黄框和红框，紫色框会进行多次。

缺点：
1.需要增加交换candidate的sdp信息。
2.sdp和candidate是分开发送的，如果信令服务器无法保证这两次传输的可靠性，可能会导致连接建立失败。

# 连接建立的核心部分

所以无论是ICE还是Trickle ICE，他们的核心都是：尽可能的收集更多的candidate。

1. candidate里面有啥？

2. 采集candidate的过程是怎样的？

3.如何使用candidate建立连接？

## 1. candidate里面有啥？

1.candidate里面有啥？
主要信息：建立连接所需要的信息：ip地址、端口、传输协议等。

这些ip地址包含了网卡地址、公网地址、中继地址，candidate的地址类型。

candidate的常用地址类型：
1.host：本地网卡的地址。
2. srflx：公网地址。
3.relay：中继服务器地址。

(具体内容可以看文末ice的rfc文档)

## 2. 采集candidate的过程是怎样的？

收集candidate的方式和过程是怎是怎样的？

1.对于host类型的candidate：
直接使用网卡的地址：包括内网地址，host地址，回环地址。

2.对于srflx类型的candidate：
  1.使用stun。
  2.使用turn。
注：turn是stun的一个补充，所以turn服务器是有stun的功能的，turn使用的协议是stun的协议，所以任何一个turn服务器是可以用作stun的。

3.对于relay类型的candidate：
使用turn。

### stun可以做什么？

1.可以获取请求方的公网ip和端口，流程如图：

![img](https://github.com/BAByte/pic/blob/master/image2022-5-24_10-33-45.png?raw=true)



2.可以帮助请求方确认nat类型。

- 这里的nat是指离stun最近的那一个，因为请求方可能处在多个nat后。
- 探测nat类型的方式有很多种，这取决于你的stun服务遵循哪种探测协议，常见的有：rfc3489，rfc5780，rfc3478等等，所以这里我就不写了，而且在webrtc的w3c标准中的末尾有说用了哪些。

### turn可以做什么？

1.做stun能做的事。

2.作为两个client进行通信的中继，如图：

![img](https://github.com/BAByte/pic/blob/master/image2022-5-24_10-38-11.png?raw=true)
1.client请求绑定，和stun一样，这一步会获取到client的公网地址。

2.请求分配一个中继端口。

3.将中继的ip和端口通过信令服务器发送给clientB。

4.类似心跳包，刷新中继端口的存活时间。

5.如果ice最终选择了中继的方式，那就为clientB创建权限，否则clientB无法使用这个relay的中继地址。

6.连通性测试，这里测试联通性不是使用binding requet和binding response。

7.建立了通信的channel。



## 3.1如何使用candidate建立连接？

首先要知道这一步是双方同时进行的，分为控制方和被控制方。（角色冲突的问题但本文不提及，感兴趣自己查）。ICE和Trickle ICE在这里的设计会有点不一样。

### ICE

1.使用信令发送和接收双方的candidate。

2.ice会对这些candidate进行配对，比如：clientA有三个candidate，clientB有三个candidata，那就会有9对candidate。

3. 按优先级排序candidate对，形成检查表。有一个优先级算法，可以看rfc8445。

4.对这些candidate进行连通性检测，通过的会成为提名候选地址对。

提名候选有两种模式：regular，aggressive。

- regular：要求所有的candidate都进行了连通性测试完成后才使用优先级最高的进行连接。显然在webrtc中不会使用这种模式，这种模式会影响连接速度。
- aggressive：激进模式，只要有一个candidate 对Connection的连通性检测通过就会提名，有流要传输时会立即使用这个Connection进行流媒体的传输，所有处与联通性检测的candidate会被清理掉，这就可能导致本次连接不是最优的。

### Trickle ICE

1.一边接收对方的candidate，同时进行采集，一旦采集到立即发送给对方。

2.采集到立即进行候选者的配对，排序，然后进行连通性测试，一旦测试通过则立即建立连接。很像ice的aggressive模式对吧，其实webrtc是支持aggressive的：如果发起者是webrtc端，就强制使用trickle ice模式，如果发起者不是webrtc端，就会使用aggressive模式。

3.正是因为这种来一个处理一个的候选人提名机制，所以也是可能出现本次连接不是最优的情况，举个例子吧：

从上面的candidate采集流程来看：

1.采集的顺序一定是：
host > srflx > relay

2.采集成功的顺序：
因为是本地调用，理论上host类型的采集速度>其他两个，但是不能保证一定是这样。而srflx一定会在relay前面，不理解可以看看turn和stun的部分。

3.对方收到candidate的顺序：webrtc没有规定信令服务器的实现，所以无法保证传递candidate是可靠的（传输的可靠性定义可以看计算机网络的相关知识）

所以很有可能relay类型的candidate会比其他类型早到，你可以这样测试：
1.过滤掉host和srflx类型的candidate，再发送candidate给对端。
2. 过滤掉对端发送来的host和srflx类型candidate，抓个包你就会发现是使用中继模式了。



## 3.2candidate的提名过程（补充）

这里是对3.1的第二步的补充。从开始到现在，一直讲的是一对一的连接过程，可是一个client可能会与多个对端建立数据流通道！所以ice要解决的不仅是一对一的连接问题，还解决一对多的问题，它是怎么设计的？

注：
候选者选择机制可能在不同的团队会有不同的实现，例如虎牙和网易在这部份可能就有专门的团队在设计，我听说是一年减少几千万，所以这部分是ice中最值得看的部分，也是比较复杂的部分，可以不看。

1.对一个连接的多个candidate对排序后，会形成检查表，检查表的长度有限制且内容不重复，如果candidate对的长度超过了限制，会丢弃最低优先级的。candidate，candidate对，检查表的关系如下：

```
 +--------------------------------------------+
  |                                            |
  | +---------------------+                    |
  | |+----+ +----+ +----+ |   +Type            |
  | || IP | |Port| |Tran| |   +Priority        |
  | ||Addr| |    | |    | |   +Foundation      |
  | |+----+ +----+ +----+ |   +Component ID    |
  | |      Transport      |   +Related Address |
  | |        Addr         |                    |
  | +---------------------+   +Base            |
  |             Candidate                      |
  +--------------------------------------------+
  *                                         *
  *    *************************************
  *    *
+-------------------------------+
|                               |
| Local     Remote              |
| +----+    +----+   +default?  |
| |Cand|    |Cand|   +valid?    |
| +----+    +----+   +nominated?|
|                    +State     |
|                               |
|                               |
|          Candidate Pair       |
+-------------------------------+
*                              *
*                  ************
*                  *
+------------------+
|  Candidate Pair  |
+------------------+
+------------------+
|  Candidate Pair  |
+------------------+
+------------------+
|  Candidate Pair  |
+------------------+
```

2.多个连接就会有多个检查表，放在一起称为检查表集，它这些检查表会有几个状态：运行、完成、失败。

注：在trickle ice中，由于candidate一旦形成对就会立即进行连通性检测，所以我个人认为一个连接可能也会有多个检查表。

3.检查表创建成功后会去计算candidate对的状态，状态类型如下：

```
+-----------+
|           |
|           |
|  Frozen   |
|           |
|           |
+-----------+
      |
      |unfreeze
      |
      V
+-----------+         +-----------+
|           |         |           |
|           | perform |           |
|  Waiting  |-------->|In-Progress|
|           |         |           |
|           |         |           |
+-----------+         +-----------+
                            / |
                          //  |
                        //    |
                      //      |
                     /        |
                   //         |
         failure //           |success
               //             |
              /               |
            //                |
          //                  |
        //                    |
       V                      V
+-----------+         +-----------+
|           |         |           |
|           |         |           |
|   Failed  |         | Succeeded |
|           |         |           |
|           |         |           |
+-----------+         +-----------+
```

4.ice 代理（可以理解为client中的ice服务）给每一个检查表都维护一个FIFO队列，这个队列称为触发队列。

5.所有数据都准备完成后，就会进行连通性检测，触发连通性检测有两种方式：（第一种应该是为了主动的去检测连通性，第二种应该是为了快速建立连接考虑，这有点像重和快重传的设计）

- 普通检测：由定时器触发的检测。
- 触发检测：对端要求进行连通性检测。



6.连通性检测通过的candidate对会被加入有效列表中，rfc8445中说只有一个数据流的所有组件都匹配到有效的candidate，连通性检查才会结束，这个组件其实是指一些协议例如rtp，rtcp等，我想这是因为不同的协议可能是tcp或者udp，而candidate是区分传输协议的，所有要匹配到对应协议的candidate才行。

7. 经过连通性检测后，nat的映射表中就有记录了，也就是我们说的打洞成功（这里特指p2p），那双方其实已经可以根据Candidate的ip和端口进行通信了。

# 相关文档：

https://w3c.github.io/webrtc-pc/#update-the-ice-gathering-state

https://webrtchacks.com/trickle-ice/

[RFC 8445 - Interactive Connectivity Establishment (ICE): A Protocol for Network Address Translator (NAT) Traversal (ietf.org)](https://datatracker.ietf.org/doc/html/rfc8445)

[RFC 5780 - NAT Behavior Discovery Using Session Traversal Utilities for NAT (STUN) (ietf.org)](https://datatracker.ietf.org/doc/html/rfc5780)

[RFC 5245 - Interactive Connectivity Establishment (ICE): A Protocol for Network Address Translator (NAT) Traversal for Offer/Answer Protocols (ietf.org)](https://datatracker.ietf.org/doc/html/rfc5245)

[RFC 8838 - Trickle ICE: Incremental Provisioning of Candidates for the Interactive Connectivity Establishment (ICE) Protocol (ietf.org)](https://datatracker.ietf.org/doc/html/rfc8838)

[RFC 8656 - Traversal Using Relays around NAT (TURN): Relay Extensions to Session Traversal Utilities for NAT (STUN) (ietf.org)](https://datatracker.ietf.org/doc/html/rfc8656)

[RFC 5389 - Session Traversal Utilities for NAT (STUN) (ietf.org)](https://datatracker.ietf.org/doc/html/rfc5389)

[RFC 3489 - STUN - Simple Traversal of User Datagram Protocol (UDP) Through Network Address Translators (NATs) (ietf.org)](https://datatracker.ietf.org/doc/html/rfc3489)

[RFC 3478 - Graceful Restart Mechanism for Label Distribution Protocol (ietf.org)](https://datatracker.ietf.org/doc/html/rfc3478)
