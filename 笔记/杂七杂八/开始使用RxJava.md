[TOC]

# 开始使用RxJava

这个东西的原理是观察者模式，一想到观察者模式我就想到EventBus，然后我就会把他们混为一谈，EventBus的主要功能是用来方便各个组件间的通信，偶尔我会用来处理异步，但是用的比较多的还是组件之间的通信，原因就是因为懒得写Java callBack。然后我就会用一种先入为主的思维去看待RxJava，这东西不会也是用来实现组件通信 的吧？嗯，好像不是这样的，

> RxJava 在 GitHub 主页上的自我介绍是 "a library for composing asynchronous and event-based programs using observable sequences for the Java VM"（一个在 Java VM 上使用可观测的序列来组成异步的、基于事件的程序的库）。这就是 RxJava ，概括得非常精准。
>
> 然而，对于初学者来说，这太难看懂了。因为它是一个『总结』，而初学者更需要一个『引言』。
>
> 其实， RxJava 的本质可以压缩为异步这一个词。说到根上，它就是一个实现异步操作的库，而别的定语都是基于这之上的。

哦，他就是让我们可以偷懒不用写Handler，AsyncTask...的库。假如没有Handler没有AsyncTask，那我们手动去处理线程之间的切换，嗯，不太现实，那为什么不用Handler和AsyncTask，要改用RxJava？嗯...他们说是因为它能让代码更加的漂亮，逻辑更加清晰。

## 使用教程

https://gank.io/post/560e15be2dca930e00da1083