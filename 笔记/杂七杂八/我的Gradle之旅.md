[toc]

# 我的Gradle之旅

大部分内容是我摘录的，因为别人已经写的很简洁，描述的很准确了。为了更快的理解Gradle是什么？用来做什么？我把这些相关的知识和理解系统的组织在一起。

每一段都会有深入了解的链接，如果感兴趣，有时间，建议去看看。

# Gradle是什么

[Gradle官方文档](https://docs.gradle.org/current/userguide/userguide.html)

Gradle是专注于灵活性和性能的开源构建自动化工具。Gradle构建脚本是使用[Groovy](https://groovy-lang.org/)或[Kotlin](https://kotlinlang.org/) DSL 编写的。

# 自动化构建的目的

在软件系统开发的过程中，一个项目工程通常会包含很多的代码文件、配置文件、第三方文件、图片、样式文件等等，是如何将这些文件有效的组装起来最终形成一个可以流畅使用的应用程序的呢？答案是借助构建工具或策略。就好像一场大型音乐会上总指挥将不同的管弦乐有效的协调起来，完成一场精彩绝伦的演出。而如果在构建的过程中依赖人手工进行编译，工作起来会很繁琐，于是就有了自动化构建、自动化发布、部署的想法和探索，通过使用程序自动化的完成系列操作，将大大提升工作效率。[想要深入理解，这里详细的介绍了自动化构建的相关工具以及目的](https://zhuanlan.zhihu.com/p/103611151)

# 编译、链接和构建

这里引用windows开发的解释，我觉得讲的很好：

1. 编译的英文是*Compile*，指的是把源代码文件通过编译器转化为目标文件的过程。编译过程的输入文件是*C / CPP / H*等文本文件，输出是*OBJ*目标文件。
2. 链接的英文是*Link*，它指的是把多个*OBJ*目标文件、*LIB*库文件链接成一个可执行文件的过程。链接过程的输入是*OBJ / LIB*等库文件，输出是*EXE / DLL*等可执行文件。
3. 构建的英文是*Build*，构建指的是生成整个操作系统的过程。构建涵盖了对源代码的编译、对库文件的链接，还有可能包含映像文件打包等其它附加操作。在三个概念中，构建的范围是最大的。

# DSL是什么

DSL是 Domain Specific Language 的缩写，中文翻译为*领域特定语言*。它们的**表达能力有限**，只在特定领域解决特定任务。

**DSL 通过在表达能力上做的妥协换取在某一领域内的高效**。想要深入的理解请移步：[DSL是什么](https://draveness.me/dsl/)。

显然Groovy和Kotlin在本文所涉及的领域起到了高效的作用。

# Gradle的官网教程

了解完一些基本概念后就可以去跟着官网的相关教程了。[官网教程](https://gradle.org/guides/#getting-started) 

为了让跟着教程做的时候知道自己在干嘛，下是对各个教程内容的一些拙见，跟着教学走不明白的时候回头看看。

##  [Creating Build Scans](https://guides.gradle.org/creating-build-scans/)

Bulid Scans 被称为Gradle分析利器，Build Scans是用于开发和维护Gradle构建的重要工具。它为你提供了构建的详细信息，并为你识别构建环境、依赖或性能上存在的问题，同时可以帮你更全面地理解并提升构建过程，也便于与他人的合作。

​    在Gradle构建运行时，Build Scans插件会抓取数据，并将数据传送到Build Scans服务端。同时返回一个可被共享的链接，内部包含有用的构建信息。这些信息包含两大类：（1）**环境信息**，包括操作系统、Java版本和时区；（2）**构建相关信息**，包含使用的插件、任务、测试以及依赖信息。[来源](https://www.jianshu.com/p/646deb0010d1)

## [Creating a New Gradle Build](https://guides.gradle.org/creating-new-gradle-builds/)

在这一章将会学到如何通过Gradle做一些简单的构建，以及了解插件