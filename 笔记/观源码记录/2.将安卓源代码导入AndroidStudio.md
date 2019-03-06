[TOC]

# 将安卓源代码导入AndroidStudio

这一步的目的就是为了可以很方便的查看源码，并且可以支持断点调试

参考文章：	https://www.jianshu.com/p/4ab864caefb2

按照这篇文章说的，我们需要先使用谷歌提供的工具：

~~~java
源码目录/developement/tools/idegen
~~~

# 具体步骤

使用方法：先进到你源码目录然后执行下列命令：

~~~java

#初始化命令工具
soruce build/envsetup.sh 
#编译 idegen 模块，生成idegen.jar
mmm development/tools/idegen/
#生成针对 Android 开发工具的配置文件 
sudo ./development/tools/idegen/idegen.sh
~~~

文章是说在源码根目录生成三个文件:

![](https://upload-images.jianshu.io/upload_images/1727036-8712fdf1565e434f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/482/format/webp)

但是我的是生成在当前目录，所以假设生产的三个文件不在源码目录，你要手动复制到源码的目录下。

这三个文件的用处：

~~~java

android.ipr：工程相关的设置，比如编译器配置、入口，相关的libraries等。

android.iml：描述了modules，比如modules的路径,依赖关系等。

android.iws：包含了一些个人工作区的设置。
~~~

+ 第一个非常重要，我们后面会用到

+ 第二个我们后面可以在编译器里面自己配置，他就是配置了一下你等下要导入什么文件啊。你源码依赖的jdk是哪个版本啊。对应的是安卓几的sdk啊，但是这些我们等下可以在androidStudio自己配置！！ 但是为了导入源码时能够快一点，还是进行排除一下一些我们不需要的目录(不然真的很慢！)。看看安卓源码的目录结构，你就能选择哪些导入哪些不导入了（下面有！）
+ 第三个就是个人工作区的设置了，这个不用管！

# 安卓源码目录结构

~~~java
|-- Makefile
|-- abi （applicationbinary interface，应用程序二进制接口，生成libgabi++.so相关库文件）
|-- art （google在4.4后加入用来代替Dalvik的运行时）
|-- bionic (Android的C library，即C库文件)
|-- bootable （启动引导相关代码）
|-- build （存放系统编译规则及generic等基础开发配置包）
|-- cts （Android兼容性测试套件标准）
|-- dalvik （dalvik JAVA虚拟机）
|-- developers (开发者用，存放几个例子)
|-- development （开发者需要的一些例程及工具）
|-- device (设备相关代码，这是各厂商需要配置和修改的代码)
|-- docs (介绍开源相关文档)
|-- external （android使用的一些开源的模组）
|-- frameworks （核心框架——java及C++语言）
|-- hardware （部分厂家开源的硬解适配层HAL代码）
|-- kernel (驱动内核相关代码)
|-- libcore (核心库相关)
|-- libnativehelper (JNI用到的库)
|-- ndk (ndk相关)
|-- out （编译完成后的代码输出目录）
|-- packages （应用程序包）
|-- pdk (google用来减少碎片化的东西)
|-- prebuilt （x86和arm架构下预编译的一些资源）
|-- sdk （sdk及模拟器）
|-- tools （工具）
|-- system （底层文件系统库、应用及组件——C语言）
|-- vendor （厂商定制代码）
~~~

# 排除目录

由于Android系统源码比较庞大，导入比较耗时，建议先修改android.iml排除一些代码，语法如下：

```
<excludeFolder url="file://$MODULE_DIR$/.repo" />
```

 比如，我只关注framework中的源码，我就可以把其他目录排除（其他目录只是以文件方式导入，其中的类之间不存在链接）。把下列内容加到content便签内。

```
<excludeFolder url="file://$MODULE_DIR$/.repo" />
<excludeFolder url="file://$MODULE_DIR$/abi" />
<excludeFolder url="file://$MODULE_DIR$/art" />
<excludeFolder url="file://$MODULE_DIR$/bionic" />
<excludeFolder url="file://$MODULE_DIR$/bootable" />
<excludeFolder url="file://$MODULE_DIR$/build" />
<excludeFolder url="file://$MODULE_DIR$/cts" />
<excludeFolder url="file://$MODULE_DIR$/dalvik" />
<excludeFolder url="file://$MODULE_DIR$/developers" />
<excludeFolder url="file://$MODULE_DIR$/development" />
<excludeFolder url="file://$MODULE_DIR$/device" />
<excludeFolder url="file://$MODULE_DIR$/docs" />
<excludeFolder url="file://$MODULE_DIR$/external" />
<excludeFolder url="file://$MODULE_DIR$/hardware" />
<excludeFolder url="file://$MODULE_DIR$/libcore" />
<excludeFolder url="file://$MODULE_DIR$/libnativehelper" />
<excludeFolder url="file://$MODULE_DIR$/ndk" />
<excludeFolder url="file://$MODULE_DIR$/out" />
<excludeFolder url="file://$MODULE_DIR$/packages" />
<excludeFolder url="file://$MODULE_DIR$/pdk" />
<excludeFolder url="file://$MODULE_DIR$/prebuilt" />
<excludeFolder url="file://$MODULE_DIR$/prebuilts" />
<excludeFolder url="file://$MODULE_DIR$/sdk" />
<excludeFolder url="file://$MODULE_DIR$/system" />
<excludeFolder url="file://$MODULE_DIR$/tools" />
```



# 正式开始导入

打开androidStudio，选择打开一个项目，然后找到并选择android.ipr文件，就等他导入完

![](https://upload-images.jianshu.io/upload_images/1727036-5ec44e404b7a66c7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/431/format/webp)

---

加载完后你可以看到目录栏里还是有很多目录，红色的目录代表被 exclude 排除了（这些红色的你一样可以查看的，但是他不会有链接，他只是个文件，比如你有个类，虽然你能在目录里面找到他。但是他是红色的，你就不能在代码里面实现按住ctrl 点击跳转）。

在加载完源码后，我们也可以在 Project Structure 中的 Module 选项中右键 exclude 来排除不需要加载的源码目录，如图：

![](https://upload-images.jianshu.io/upload_images/1727036-7d1aae0686d7340b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/291/format/webp)



![](https://upload-images.jianshu.io/upload_images/1727036-7c73a8daa082bb42.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

最下面那个红框框的那个选项（Excluded），前面要是有勾，说明这个目录被排除了！！

#　配置源码对应的jdk和sdk依赖

我们只是导入了源码，但是源码所对应的sdk和jdk版本还是没有配置好，或者说不一定配置对了，这是为了阅读和调试代码的时候能够保证代码跳转正确。

首先是 AOSP 源码的跳转，我们通过 `File -> Project Structure` 打开 Module，然后选中 Dependencies， 保留 JDK 跟 Module Source 项，并添加源码的 external 和 frameworks 依赖，如图：注意啦！！你的不一定是1.8的jdk！！！选择自己编译源码时安装的jdk版本！！！而最后的两个依赖目录。要在最右边那个绿色的加号，选第一个，然后会让你选目录的，你自己操作下就知道了！

![](https://upload-images.jianshu.io/upload_images/1727036-8353f81e90acf3da.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)



然后是 SDK 的设置，确保关联对应版本的 SDK 于系统版本一致,我下载的是安卓7.1.1的源码。对应的是25，但是文章作者用的是24，因为安卓7.0和7.1都是安卓N，你可以试试24或25，应该没有问题。

![](https://upload-images.jianshu.io/upload_images/1727036-1e0aa9ce3dc0435c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)



![](https://upload-images.jianshu.io/upload_images/1727036-7892371ccd55120a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)



# 调试源码（java）

在前面，我们进行了源码的导入，可以看到源码啦！！但是我们想要断点调试怎么办？

+ 你可能需要设置一下androidStudio让他支持adb调试

![](https://upload-images.jianshu.io/upload_images/1727036-ee0a255bb050308b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

+ 打开你的模拟器，注意是运行着我们编译好源码的模拟器
+ 这里假设我们要调试 Android 自带浏览器的源码，如图，我们在它的入口文件 WebViewBrowserActivity 中的 loadUrlFromUrlBar 方法中打上断点。

![](https://upload-images.jianshu.io/upload_images/1727036-25bee7948c7ea5b7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

+ 在模拟器点击 WebViewBrowser 打开 app
+ 打开之后，点击 attach to Android process 按钮打开 choose Process，可以看到 webViewBrowser 运行的进程，选中，ok

![](https://upload-images.jianshu.io/upload_images/1727036-293320d72f5bf7d2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

# 其他知识点

如果你不知道上面命令中的：mmm 代表什么。

这里我建议你先去看看安卓编译工具的一些指令的笔记或者搜索一下相关的资料























































































