# webview性能分析

# 背景

app采用混合开发模式，浏览器使用的是系统预置的webview。我们在一些低性能的机器上遇到了卡顿严重的问题，为了提升渲染优先级，我们打开了硬件加速，虽然很流畅，但又会出现花屏问题。

2021年的机器，性能再差，也不至于加载个网页都卡，我一直认为是软件没有兼容好，但一直没有解决思路。后来才知道原生的webview渲染性能极差。听说可以内置crosswalk替代系统原生webview，进而解决性能差的问题，就试试了，结果大喜！

# 原因分析

## 谷歌自己没意识到吗？

[Android4.4后谷歌以Chromium作为了webview的内核，提升了性能。

Android5.0后源码中抽出webview为单个应用程序，上架google play 以应用更新的方式给用户升级webview版本，解决各个厂商系统版本迭代慢，webview碎片化的问题。

从Android7.0系统开始，如果系统安装了Chrome (version>51)，那么Chrome将会直接为应用的WebView提供渲染，WebView版本会随着Chrome的更新而更新，用户也可以选择WebView的服务提供方（在开发者选项->WebView Implementation里），WebView可以脱离应用，在一个独立的沙盒进程中渲染页面（需要在开发者选项里打开）

从Android8.0系统开始，默认开启WebView多进程模式，即WebView运行在独立的沙盒进程中，独立进程好处就是不占用主进程的内存。 

可以谷歌为此付出了很大的努力。

摘录于：[来源](https://juejin.cn/post/6844903567497789453)



## webview版本差异导致的性能问题的根本原因吗？

国内是没有GMS和google play的，所以碎片化问题在国内依旧存在。

​		有些人就因此归咎于webview版本差异。一些远古贴子的作者他们确实处在Android4.4或Android5.0的过渡时期。但是有些新的文章为了强行解释版本低导致的性能差，是不对的！因为我手上的设备就是Andorid 7，我用了较新webview版本还是很卡。。同时我在其他机器上跑又很流畅，所以性能低的原因不仅是碎片化的问题，机器性能也是原因之一。

​		还是开篇的那句话：”2021年的机器，性能再差，也不至于加载个网页都卡“。所以我认为另一个原因是webview本身的问题。开篇说到我接入了crosswalk后，开硬件加速就不花屏了，crosswalk的内核就是Chromium的内核，只是有去做了一部分精简。不排除英特尔对这个Chromium进行了优化，但还是可以得出webview本身就有问题的结论。

# webview的渲染性能为什么这么差？

WebView本质上就是一个普通的View，和其他的View一样，都是依附在Window上，并只有一个Surface。在两年前我写过一篇笔记：[Surface系统]([https://github.com/BAByte/BANote/blob/master/%E7%AC%94%E8%AE%B0/%E8%A7%82%E6%BA%90%E7%A0%81%E8%AE%B0%E5%BD%95/10.Surface%20%E7%B3%BB%E7%BB%9F.md](https://github.com/BAByte/BANote/blob/master/笔记/观源码记录/10.Surface 系统.md)) ，绘制用的Canvas就是来自于Surface，问题就是虽然在Android L 