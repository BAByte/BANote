[toc]

# 我的Gradle之旅

大部分内容是我摘录的，因为别人已经写的很简洁，描述的很准确了。为了更快的理解Gradle是什么？用来做什么？我把这些相关的知识和理解系统的组织在一起。

每一段都会有深入了解的链接，如果感兴趣，有时间可以去看看。

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

# Project和tasks

 在grade中的两⼤重要的概念，分别是project和tasks。每⼀次构建都是有⾄少⼀个project来完成， 所以Android studio中的project和Gradle中的project不是⼀个概念。每个project有⾄少⼀个tasks。 每⼀个build.grade⽂件代表着⼀个project。tasks在build.gradle中定义。当初始化构建进程，gradle 会基于build⽂件，集合所有的project和tasks,⼀个tasks包含了⼀系列动作，然后它们将会按照顺序 执⾏，⼀个动作就是⼀段被执⾏的代码，很像Java中的⽅法。

# 构建的⽣命周期

 ⼀旦⼀个tasks被执⾏，那么它不会再次执⾏了，不包含依赖的Tasks总是优先执⾏，⼀次构建将会 经历下列三个阶段： 

1. 初始化阶段：project实例在这⼉创建，如果有多个模块，即有多个build.gradle⽂件，多个 project将会被创建。 
2.  配置阶段：在该阶段，build.gradle脚本将会执⾏，为每个project创建和配置所有的tasks。 
3. 执⾏阶段：这⼀阶段，gradle会决定哪⼀个tasks会被执⾏，哪⼀个tasks会被执⾏完全依赖开始构建时传⼊的参数和当前所在的⽂件夹位置有关，用的是链表管理这些顺序（因为task在初始化顺序时，一些task的顺序是由plugin和gradle默认的配置决定的，我们自己也可能插入task，所以更多的是插入，用链表合适）。



# 依赖管理

使用linux一定会有这样的一个经历：安装A程序，会提示你是否安装相关的依赖程序，选择yes会自动下载安装相关依赖程序。不需要用户自己去手动下载相关的依赖程序进行安装，很有意思对吧！

当然也会有这样的情况：A程序依赖B程序，所以需要下载B程序进行安装，但是当前设置的软件源没有B程序，包管理工具就没有办法自动帮你下载B程序。这时要么你上网找B程序自己装，要么增加一些软件源。

gradle有强大的依赖管理功能，无论是gradle插件还是java库，只需要添加一行代码，gradle就会自动帮你下载。

Gradle包含了两种依赖引用方式：远程仓库或者本地仓库引入。

详细的内容请移步：[依赖管理](https://segmentfault.com/a/1190000004237922)

# Gradle的官网教程

了解完一些基本概念后就可以去跟着官网的相关教程了。[官网教程](https://gradle.org/guides/#getting-started) 

为了让跟着教程做的时候知道自己在干嘛，下是对各个教程内容的一些拙见，跟着教学走不明白的时候回头看看。

##  [Creating Build Scans](https://guides.gradle.org/creating-build-scans/)

Bulid Scans 被称为Gradle分析利器，Build Scans是用于开发和维护Gradle构建的重要工具。它为你提供了构建的详细信息，并为你识别构建环境、依赖或性能上存在的问题，同时可以帮你更全面地理解并提升构建过程，也便于与他人的合作。

​    在Gradle构建运行时，Build Scans插件会抓取数据，并将数据传送到Build Scans服务端。同时返回一个可被共享的链接，内部包含有用的构建信息。这些信息包含两大类：（1）**环境信息**，包括操作系统、Java版本和时区；（2）**构建相关信息**，包含使用的插件、任务、测试以及依赖信息。[来源](https://www.jianshu.com/p/646deb0010d1)

总而言之，Bulid Scans就是一个debug工具。

## [Creating a New Gradle Build](https://guides.gradle.org/creating-new-gradle-builds/)

在这一章将会学到如何通过Gradle做一些简单的构建，以及了解插件。gradle请自行去官网下载安装。

## 创建一个gradle的工作目录

创建一个目录，然后在该目录进行init：

~~~java
gradle init
~~~

是否找回了第一次使用git的感觉？git会帮你初始化仓库。gradle也会帮你初始化项目，在init过程中还会询问你需要初始化[什么类型的项目](https://docs.gradle.org/4.10.3/userguide/build_init_plugin.html#sec:build_init_types)，选择怎样的DSL语言，项目名称是什么：

~~~java
Select type of project to generate:
  1: basic
  2: application
  3: library
  4: Gradle plugin
Enter selection (default: basic) [1..4]

Select build script DSL:
  1: Groovy
  2: Kotlin
Enter selection (default: Groovy) [1..2]

Project name (default: basic-demo):
~~~

选择项目类型的时候，是否找回了第一次打开Android Studio，选择创建项目类型时的那种不知所措的感觉?

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_0551a086-1c60-4a3e-8593-1214648e9771.png?raw=true)

 很奇妙对吧？第一次都是一直回车（下一步）。初始化完成后可以看到生成的目录结构:

```groovy
├── build.gradle  // 用于配置当前项目的Gradle构建脚本
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar  //https://docs.gradle.org/4.10.3/userguide/gradle_wrapper.html 可执行JAR
│       └── gradle-wrapper.properties  //Gradle Wrapper配置属性
├── gradlew // 基于Unix的系统的Gradle Wrapper脚本
├── gradlew.bat  //适用于Windows的Gradle Wrapper脚本
└── settings.gradle  //用于配置Gradle构建的Gradle设置脚本
```

# Gradle Wrapper 

grade只是⼀个构建⼯具，版本总是在更迭，所以使⽤Gradle Wrapper将会是⼀个好的选择去避免由于gradle版本更新导致的问题。Gradle Wrapper提供了⼀个windows的batch⽂件和其他系统的 shell⽂件，当你使⽤这些脚本的时候，当前gradle版本将会被下载，并且会被⾃动⽤在项⽬的构建， 所以每个开发者在构建⾃⼰app的时候只需要使⽤Wrapper。所以开发者不需要为你的电脑安装任何 gradle版本，在mac上你只需要运⾏gradlew，⽽在windows上你只需要运⾏gradlew.bat。 你也可以利⽤命令⾏./gradlew -v来查看当前gradle版本。

可以看到⼀个bat⽂件针对windows系统，⼀个shell脚本针对mac系统，⼀个jar⽂件，⼀个配置⽂ 件。配置⽂件包含以下信息：

~~~java
 #Sat May 30 17:41:49 CEST 2015 
 distributionBase=GRADLE_USER_HOME 
 distributionPath=wrapper/dists 
 zipStoreBase=GRADLE_USER_HOME 
 zipStorePath=wrapper/dists
 distributionUrl=https\://services.gradle.org/distributions/gradle-2.4-all.zip 
~~~

你可以改变该url来改变你的gradle版本。

# 编写第一个构建任务

在linux中我们会写一些shell脚本，然后执行这个脚本就能完成一系列的"任务"。这种"任务"在gradle中称为"[task](https://guides.gradle.org/creating-new-gradle-builds/#create_a_task)"。编写在 build.gradle  文件内。

## Hello world

第一个task当然是输出 "Hello world" 了！

~~~java
task hello {
  doLast{
    println 'Hello Gradle Task!'
  }
}
~~~

执行单个任务

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_5e7bcad7-d0e7-4c67-be8f-3467d6247625.png?raw=true)

## 文件复制

接下来编写一个文件复制的task。乍一看和构建没有什么关系，事实上在构建项目时常常会有文件的生成，复制，删除等操作。 

+ 创建一个目录src并在该目录创建hello_world.txt文件。

+ 编写task

~~~java
/build.gradle 
  
task copy(type: Copy) {
   	println 'start cpoy!'
    from "src"
    into "dest"
}
~~~

执行该task后看文件是否复制到dest文件夹。

## task增加分组和描述

希望控制台输出按task组进行分类的信息

组名:Custom、描述：Copies sources to the dest directory

~~~java
task copy(type: Copy, group: "Custom", description: "Copies sources to the dest directory") {
    from "src"
    into "dest"
}
~~~

显示所有task

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_37e84f5b-e263-4199-8e60-8722c6bf4d48.png?raw=true)



# 使用Plugin

编写task，task的引入大致就三种：自定义类型、DefaultTask类型和Plugin类型。DefaultTask除了上方copy外还有许多：https://docs.gradle.org/3.3/dsl/index.html#N10347

Gradle具有很多Plugin，其中base插件核心type之一的zip就可以实现文件的压缩。

插件需要在文件的头部声明

**build.gradle**

~~~java
plugins {
    id "base"
}
~~~

编写task:把src目录压缩到build/distributions/basic-demo-1.0.zip中。

~~~java
task zip(type: Zip, group: "Archive", description: "Archives sources in a zip file") {
    from "src"
    archiveFileName = "basic-demo-1.0.zip"
}
~~~

执行一下这个task，就可以看到basic-demo-1.0.zip已经生成了。

---

# 创建多项目构建

Multi-project builds helps with modularization. It allows a person to concentrate on one area of work in a larger project, while Gradle takes care of dependencies from other parts of the project.

在Android项目中我们有多个个**build.gradle**，有根项目和app以及其他module。

## 使用gralde构建两个java项目

需求：一个根项目包含app和module两个子项目，然后在app项目引用module项目

1.初始化一个根项目

2.在根项目初始化一个app项目，选项如下：

gradle会自动帮你生成application的一些文件目录，很智能有没有！

~~~java
Select type of project to generate:
  1: basic
  2: application
  3: library
  4: Gradle plugin
Enter selection (default: basic) [1..4] 2

Select implementation language:
  1: C++
  2: Groovy
  3: Java
  4: Kotlin
  5: Swift
Enter selection (default: Java) [1..5] 3

Select build script DSL:
  1: Groovy
  2: Kotlin
Enter selection (default: Groovy) [1..2] 1

Select test framework:
  1: JUnit 4
  2: TestNG
  3: Spock
  4: JUnit Jupiter
Enter selection (default: JUnit 4) [1..4] 1

Project name (default: app):
Source package (default: app):
~~~

---

App项目为application，说明他是一个应用程序，可直接运行。

查看app/build.gradle可以看到相关配置的解释。

~~~java
/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java project to get you started.
 * For more details take a look at the Java Quickstart chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.5.1/userguide/tutorial_java_projects.html
 */

plugins {
    // Apply the java plugin to add support for Java
    id 'java'

    // Apply the application plugin to add support for building a CLI application.
    id 'application'
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    // This dependency is used by the application.
    implementation 'com.google.guava:guava:29.0-jre'

    // Use JUnit test framework
    testImplementation 'junit:junit:4.13'
}

application {
    // Define the main class for the application.
    mainClassName = 'app.App'
}

~~~

3.在根项目的settings.gradle导入app项目

~~~java
include 'app'
rootProject.name = 'gradle_demo'
~~~

4.查看一下tasks：

包含了Application 这个task，这是因为在app项目plugins里面声明了Application。

~~~java

 ~/gradle_demo  gradle tasks

> Task :tasks

------------------------------------------------------------
Tasks runnable from root project
------------------------------------------------------------

Application tasks
-----------------
run - Runs this project as a JVM application

~~~

提示了可以使用run命令运行这个application

~~~java
 ~/gradle_demo  gradle run

> Task :app:run
Hello world!.

BUILD SUCCESSFUL in 589ms
2 actionable tasks: 1 executed, 1 up-to-date
~~~

5.在根目录新建一个java库：hellow_lib 配置如下:

~~~java
 ~/gradle_demo/hellow_lib  gradle init

Select type of project to generate:
  1: basic
  2: application
  3: library
  4: Gradle plugin
Enter selection (default: basic) [1..4] 3

Select implementation language:
  1: C++
  2: Groovy
  3: Java
  4: Kotlin
  5: Scala
  6: Swift
Enter selection (default: Java) [1..6] 3

Select build script DSL:
  1: Groovy
  2: Kotlin
Enter selection (default: Groovy) [1..2] 1

Select test framework:
  1: JUnit 4
  2: TestNG
  3: Spock
  4: JUnit Jupiter
Enter selection (default: JUnit 4) [1..4] 1

Project name (default: hellow_lib):

Source package (default: hellow_lib):

~~~

记得在根项目include该项目！查看库项目的build.gradle文件

~~~java
/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java Library project to get you started.
 * For more details take a look at the Java Libraries chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.5.1/userguide/java_library_plugin.html
 */

plugins {
    // Apply the java-library plugin to add support for Java Library
    id 'java-library'
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api 'org.apache.commons:commons-math3:3.6.1'

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation 'com.google.guava:guava:29.0-jre'

    // Use JUnit test framework
    testImplementation 'junit:junit:4.13'
}
~~~

发现一些repositories和app项目的一样，能不能提到根项目去配置？可以的！

根项目的build.gradle添加下面的内容:

~~~java
//这是给所有项目指定了一个远程仓库 
allprojects {
     repositories {
         jcenter()
     }
 }
~~~

也可以只为子项目指定

~~~java
subprojects {
  repositories {
     jcenter()
  }
}
~~~

子项目就可以不写导入jcenter的代码了!!!!

gradle同样也为库生成了一些文件目录，在该库项目编写一个java类其他项目使用

~~~java
package hellow_lib;

public class Library {
    public String getHelloWorld() {
        return "Hello World form library";
    }
}
~~~

在app添加hellow_lib的依赖

~~~java
dependencies {
    implementation project(path: ':hellow_lib')
}
~~~

在app项目中使用Library类生成字符串

~~~java
package app;
//记得导入包
import hellow_lib.Library;
public class App {
    public String getGreeting() {
        return new Library().getHelloWorld();
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }
}
~~~

gradle run一下

~~~java
 ~/gradle_demo  gradle run

> Task :app:run
Hello World form library

BUILD SUCCESSFUL in 569ms
4 actionable tasks: 1 executed, 3 up-to-date
~~~

---

# 自定义Task

## 简单定义 

初始化一个base项目，编写build.gradle：

~~~java
tasks.register("hello") {
    group = "Welcome"
    description = "Produces a greeting"

    doLast {
        println("Hello, World")
    }
}
~~~

执行任务输出：

~~~java
 ~/gradle_demo  gradle hello

> Task :hello
Hello, World

BUILD SUCCESSFUL in 854ms
1 actionable task: 1 executed
~~~

在上面的举例中，是一个非正式的 task , 说非正式是因为创建的 task 里面没有 action 。task 本质上又是由一组被顺序执行的 Action 对象构成，Action其实是一段代码块，类似于Java中的方法。上方的doLast意思是往待执行的action的队列尾部添加一个action，关于task的依赖关系我们后面再详细看。

## 使用类定义

注意到前面自定义的task没有办法在使用时设置输出的内容，下面我们定义具体的处理类：

~~~java
//定义一个处理类，继承 DefaultTask
//定义一个处理类，继承 DefaultTask
class HelloWorldTask extends DefaultTask {
      String message = "I am BA"

     //这个注解代表：当task被执行时执行该方法
     @TaskAction
     void hello() {
         println "hello ${message}"
     }
 }

//指定task的type为：HelloWorldTask，名字为：hello
tasks.register("hello", HelloWorldTask) {
    group = 'Welcome' //组
    description = 'Produces a world' //描述
}

//指定task的type为：HelloWorldTask，名字为：hello2
tasks.register("hello2", HelloWorldTask) {
    group = 'Welcome'
    description = 'Produces a world'
    message = 'world !'
}
~~~

输出结果：

~~~java
~/gradle_demo  gradle hello

> Task :hello
hello I am BA
  
~/gradle_demo  gradle hello2

> Task :hello2
hello world !
~~~

上面是把class 直接放在了build.gradle里面，多了就不好管了。groovy默认的自定义task目录为：buildSrc/src/main/groovy/，编译时会去找这个文件夹下面的task。下面我们放到项目的   buildSrc/src/main/groovy/com/ex/HelloWorldTask.groovy    里面：

~~~java
//记得声明package
package com.ex
//导入TaskAction注解的包
import org.gradle.api.*
import org.gradle.api.tasks.*

//定义一个处理类，继承 DefaultTask
class HelloWorldTask extends DefaultTask {
      String message = "I am BA"

     //这个注解代表：当task被执行时执行该方法
     @TaskAction
     void hello() {
         println "hello ${message}"
     }
 }

~~~

再看使用：

~~~java
//导入包名
import com.ex.HelloWorldTask
//指定task的type为：HelloWorldTask，名字为：hello
tasks.register("hello", HelloWorldTask) {
    group = 'Welcome' //组
    description = 'Produces a world' //描述
}

//指定task的type为：HelloWorldTask，名字为：hello2
tasks.register("hello2", HelloWorldTask) {
    group = 'Welcome'
    description = 'Produces a world'
    message = 'world !'
}
~~~

运行结果和前面的一样，就不放出来了。

## 定义到其他项目

目前自定义Task还是在自己的项目内，定义到其他项目试试：

新建一个根项目，用kotlin作为dsl。

新建一个子项目：名为diy的groovy库。

创建： src/main/groovy/com/ex/Hello.groovy 。

~~~java
package com.ex

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input

class Hello extends DefaultTask {

    @Input
    String message = 'hello from GreetingTask'

    @TaskAction
    def sayHello() {
        println message
    }
}

~~~

diy项目的build.gradle：

~~~java
plugins {
    id 'groovy' //因为是groovy项目，导入groovy插件
    id 'maven' //我们的库可以打包后上传到指定仓库，所以需要导入maven插件
}

dependencies {
  //task实现的class类用到了gradle api的注解
    implementation gradleApi()
}

//组
group = 'com.ex'
//版本号
version = '1.0'
//导出的库会被重命名为hello，并且放在“包名/hello/”目录下
archivesBaseName = 'hello'
  
//执行这个task，就可以把库导出到上级目录的repo文件夹下
uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: uri('../repo')) //这里应该是指定服务器地址的，但是我们这里导出到本地
        }
    }
}
~~~

在diy项目导出库：

~~~java
 ~/gradle_demo/diy$ gradle uploadArchives

Deprecated Gradle features were used in this build, making it incompatible with Gradle 7.0.
Use '--warning-mode all' to show the individual deprecation warnings.
See https://docs.gradle.org/6.5.1/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 573ms
3 actionable tasks: 2 executed, 1 up-to-date
~~~

可以看到根项目生成了repo文件夹，接下来在根项目使用：

build.gradle.kts  (这里我用的是kotlin为dsl)

~~~java
//buildscript方法是定义了全局的相关属性,gradle是由groovy语言编写的，支持groovy语法，可以灵活的使用已有的各种ant插件、基于jvm的类库，

//这也是它比maven、 ant等构建脚本强大的原因。虽然gradle支持开箱即用，但是如果你想在脚本中使用一些第三方的插件、类库等，

//就需要自己手动添加对这些插件、类库的 引用。而这些插件、类库又不是直接服务于项目的，而是支持其它build脚本的运行。

//所以你应当将这部分的引用放置在buildscript代码块中。 gradle在执行脚本时，会优先执行buildscript代码块中的内容，

//然后才会执行剩余的build脚本。
buildscript {
    repositories {
        maven {
          	//刚刚我们导出到本地了,所以仓库应该是我们的本地repo目录
            url = uri("./repo")
        }
    }
    dependencies {
      //（组：库名：版本号）
      //该classpath声 明说明了在执行其余的build脚本时，
      //class loader可以使用这些你提供的依赖项
      classpath("com.ex:hello:1.0")
    }
}

tasks.register<com.ex.Hello>("hello") {
    group = "hello"
    message = "howdy!"
}

~~~

执行一下task hello：

~~~java
 ~/gradle_demo  gradle hello

> Task :hello
howdy!

BUILD SUCCESSFUL in 493ms
1 actionable task: 1 executed
~~~



## Task的依赖

task的主要核心是action，根据action队列顺序执行。在添加action时可以指定位置插入队列，如头部，尾部，指定的某个task之后。



# 自定义Plugin

上文有说过：**task的导入方式之一就是通过Plugin。** 与task自定义一样，有三种方式。

## 简单定义

创建一个gradle_demo项目。编写build.gradle文件内容：

~~~java
//导入插件
apply plugin: SayHelloWorld

//插件apply到项目时，Gradle将创建插件类的实例，并调用该实例的Plugin.apply（）
//指定泛型为Project，也就是指定类型为Gradle项目
class SayHelloWorld implements Plugin<Project> {
  	//项目实例被当做参数，插件可使用这个参数对项目进行配置
    void apply(Project project) {
     		//插件本提供了一个名为hello的task
        project.task('hello') {
            doLast {
                println 'Hello from the plugin'
            }
        }
    }
}
~~~

现在可以在gradle_demo项目中执行hello的task：

~~~java
~/gradle_demo gradle hello

> Task :hello
Hello from the plugin

BUILD SUCCESSFUL in 509ms
1 actionable task: 1 executed
~~~



## 插件定义在BuildSrc文件夹

创建：buildSrc/src/main/groovy/com/ex/SayHelloWorld.groovy

内容：

~~~java
package com.ex
  
import org.gradle.api.Plugin
import org.gradle.api.Project

class SayHelloWorld implements Plugin<Project> {
    void apply(Project project) {
        project.task('hello') {
            doLast {
                println 'Hello from the plugin'
            }
        }
    }
}
~~~

builde.gradle中导入插件 :

~~~java
apply plugin: com.ex.SayHelloWorld
~~~

运行后结果是一样的



## 使用扩展属性

使用扩展属性实现可配置内容：由使用该插件的项目设置输出内容。

新建：buildSrc/src/main/groovy/com/ex/Message.groovy：

~~~java
package com.ex

class Message {
  String value = 'default value'
}
~~~

然后在plugin类声明Message类为自己的扩展属性：

~~~java
package com.ex

import org.gradle.api.Plugin
import org.gradle.api.Project

class SayHelloWorld implements Plugin<Project> {
    void apply(Project project) {
      	//声明一个扩展属性，别名为word
        def extension = project.extensions.create('word', Message)
        project.task('hello') {
            doLast {
             		//使用扩展属性输出
                println "Hello ${extension.value}"
            }
        }
    }
}

~~~



## 在使用插件时可以指定属性的值：

~~~java
apply plugin: com.ex.SayHelloWorld

//不指定的话就是默认值
word {
  value = 'I AM BA!'
}
~~~

执行task结果：

~~~java
 ~/gradle_demo gradle hello

> Task :hello
Hello I AM BA!

BUILD SUCCESSFUL in 571ms
1 actionable task: 1 executed
~~~



使用Project对象处理文件

上面说Plugin可以通过Project对象对项目做一些配置，下面使用Project对象对项目输出文件：

定义一个扩展属性：buildSrc/src/main/groovy/com/ex/Dir.groovy

~~~java
package com.ex

class Dir {
  String dir
}
~~~



定义一个写文件的Plugin：buildSrc/src/main/groovy/com/ex/OutPutHelloFile.groovy

~~~java
package com.ex

import org.gradle.api.Plugin
import org.gradle.api.Project

class OutPutHelloFile implements Plugin<Project> {
    void apply(Project project) {
        def extension = project.extensions.create('dir', Dir)
        project.task('output') {
            doLast {
              	//对使用该Plugin的project的文件目录进行操作
                def file = project.file(extension.dir)
                file.parentFile.mkdirs()
                file.write 'Hello I AM BA'
            }
        }
    }
}
~~~



在build.gradle使用：

~~~java
apply plugin: com.ex.OutPutHelloFile

//声明要输出的目录
dir {
  dir = 'build/output/hello.txt'
}

//写一个task读文件，当然也可以在plugin中定义一个读该文件的task
task readFile {
  doLast {
    println file('build/output/hello.txt').text
  }
}
~~~

执行task：

~~~java

 ~/gradle_demo  gradle output

BUILD SUCCESSFUL in 513ms
1 actionable task: 1 executed
 ~/gradle_demo  gradle readFile

> Task :readFile
Hello I AM BA

BUILD SUCCESSFUL in 536ms
1 actionable task: 1 executed
~~~



## 插件独立项目

创建一个dsl为groovy，名为gradle_demo的base项目, 再创建名为diy 的子项目,配置如下:

~~~java
 ~/gradle_demo/diy  gradle init
Starting a Gradle Daemon, 1 stopped Daemon could not be reused, use --status for details

Select type of project to generate:
  1: basic
  2: application
  3: library
  4: Gradle plugin
Enter selection (default: basic) [1..4] 4

Select implementation language:
  1: Groovy
  2: Java
  3: Kotlin
Enter selection (default: Java) [1..3] 1

Select build script DSL:
  1: Groovy
  2: Kotlin
Enter selection (default: Groovy) [1..2] 1

Project name (default: diy):
Source package (default: diy):

> Task :init
Get more help with your project: https://guides.gradle.org?q=Plugin%20Development

BUILD SUCCESSFUL in 13s
2 actionable tasks: 2 executed
~~~

然后可以看到：diy/src/main/groovy/diy/DiyPlugin.groovy

~~~java
/*
 * 已经定义好了一个hello world程序，我们这次就直接用吧
 */
package diy

import org.gradle.api.Project
import org.gradle.api.Plugin

/**
 * A simple 'hello world' plugin.
 */
public class DiyPlugin implements Plugin<Project> {
    public void apply(Project project) {
        // Register a task
        project.tasks.register("greeting") {
            doLast {
                println("Hello from plugin 'diy.greeting'")
            }
        }
    }
}

~~~

修改diy项目的build.gradle,增加maven插件，因为我们要将插件发布到本地 :

~~~java
plugins {
		...
    // 该插件提供gradlePlugin  task
    id 'java-gradle-plugin'

    // Apply the Groovy plugin to add support for Groovy
    id 'groovy'
      
    //添加maven插件
    id 'maven'
}

//组
group = 'com.ex'
//版本号
version = '1.0'
//导出的库会被重命名为hello，并且放在“包名/hello/”目录下
archivesBaseName = 'hello'

//执行这个task，就可以把库导出到上级目录的repo文件夹下
uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: uri('../repo')) //这里应该是指定服务器地址的，但是我们这里导出到本地
        }
    }
}

// 会自动帮我们关联id和具体实现类到:
//diy/build/resources/main/META-INF/gradle-plugins/diy.greeting.properties
gradlePlugin {
    // Define the plugin
    plugins {
        greeting {
          	//这个id用来给其他项目使用
            id = 'diy.greeting'
            implementationClass = 'diy.DiyPlugin'
        }
    }
}
~~~

在根项目的build.gradle指定classpath：

~~~java
buildscript {
    repositories {
        jcenter()
        maven {
          	//刚刚我们导出到本地了,所以仓库应该是我们的本地repo目录
            url = uri("./repo")
        }
    }
    dependencies {
      //（组：库名：版本号）
      //该classpath声 明说明了在执行其余的build脚本时，
      //class loader可以使用这些你提供的依赖项
      classpath("com.ex:hello:1.0")
    }
}
~~~

新建一个app子项目

~~~java
 ~/gradle_demo  mkdir app
 ~/gradle_demo  cd app
 ~/gradle_demo/app  gradle init

Select type of project to generate:
  1: basic
  2: application
  3: library
  4: Gradle plugin
Enter selection (default: basic) [1..4] 2

Select implementation language:
  1: C++
  2: Groovy
  3: Java
  4: Kotlin
  5: Swift
Enter selection (default: Java) [1..5]

Select build script DSL:
  1: Groovy
  2: Kotlin
Enter selection (default: Groovy) [1..2]

Select test framework:
  1: JUnit 4
  2: TestNG
  3: Spock
  4: JUnit Jupiter
Enter selection (default: JUnit 4) [1..4]

Project name (default: app):
Source package (default: app):

> Task :init
Get more help with your project: https://docs.gradle.org/6.5.1/userguide/tutorial_java_projects.html

BUILD SUCCESSFUL in 11s
2 actionable tasks: 2 executed
~~~

然后在根项目的settings.gradle文件导入app项目：

~~~java
include 'app'
rootProject.name = 'gradle_demo'
~~~

接下来在app项目导入插件:

~~~java
plugins {
    // Apply the java plugin to add support for Java
    id 'java'

    // Apply the application plugin to add support for building a CLI application.
    id 'application'

    //导入我们自定义的插件
    id 'diy.greeting'
}
~~~

在根项目中执行task --all 列出所有的task：

~~~java
 ~/gradle_demo  gradle task --all
...

Other tasks
-----------
app:greeting  //可以看到app项目已经导入了我们的自定义plugin
 
BUILD SUCCESSFUL in 530ms
1 actionable task: 1 executed
 ~/gradle_demo 
~~~

执行一下：

~~~java
 ~/gradle_demo  gradle app:greeting

> Task :app:greeting
Hello from plugin 'diy.greeting'

BUILD SUCCESSFUL in 530ms
1 actionable task: 1 executed
 ~/gradle_demo 
~~~

# [新的Maven发布插件](https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven)

 [Maven 翻译为"专家"、"内行"，是 Apache 下的一个纯 Java 开发的开源项目。基于项目对象模型（缩写：POM）概念，Maven利用一个中央信息片断能管理一个项目的构建、报告和文档等步骤。Maven 是一个项目管理工具，可以对 Java 项目进行构建、依赖管理。](https://www.runoob.com/maven/maven-tutorial.html)

在上文中我们使用maven插件对自定义插件进行了构建和发布。除了gradle plugin外，在实际开发中我们也会写一些java jar开源库。所以学会使用gradle的maven插件也是很重要的。

## [Maven pom ](https://docs.gradle.org/current/dsl/org.gradle.api.publish.maven.MavenPom.html)

Maven pom 可以当成是gradle的build.gradle文件理解。有关maven pom 的详细内容请点击上方标题。

## Maven Publish Plugin

上文中使用的Maven插件是旧版的。

下面使用新版的maven插件：将上文的“插件独立项目”插件发布到本地：

~~~java
//引入插件
plugins {    id 'maven-publish' }

publishing {
    publications {
        hello(MavenPublication) {
        //组
        groupId = 'com.ex'
        artifactId = 'hello'
        //版本号
        version = '1.0'
        from components.java
        }
    }

    repositories {
        maven {
          //发布到hello仓库
            name = 'hello'
          //将包发布到上级目录的repo文件夹
            url = uri("../repo")
        }
    }
}
~~~

运行gradle task --all 看一下该插件有哪些task

~~~java
Publishing tasks
----------------
diy:generateMetadataFileForGreetingPluginMarkerMavenPublication 
  - Generates the Gradle metadata file for publication 'greetingPluginMarkerMaven'.
diy:generateMetadataFileForHelloPublication 
  - Generates the Gradle metadata file for publication 'hello'.
diy:generateMetadataFileForPluginMavenPublication 
  - Generates the Gradle metadata file for publication 'pluginMaven'.
diy:generatePomFileForGreetingPluginMarkerMavenPublication 
  - Generates the Maven POM file for publication 'greetingPluginMarkerMaven'.
diy:generatePomFileForHelloPublication 
  - Generates the Maven POM file for publication 'hello'.
diy:generatePomFileForPluginMavenPublication
  - Generates the Maven POM file for publication 'pluginMaven'.
diy:publish - Publishes all publications produced by this project.
diy:publishAllPublicationsToHelloRepository 
  - Publishes all Maven publications produced by this project to the hello repository.
diy:publishGreetingPluginMarkerMavenPublicationToHelloRepository 
  - Publishes Maven publication 'greetingPluginMarkerMaven' to Maven repository 'hello'.
diy:publishGreetingPluginMarkerMavenPublicationToMavenLocal 
  - Publishes Maven publication 'greetingPluginMarkerMaven' to the local Maven repository.
diy:publishHelloPublicationToHelloRepository 
  - Publishes Maven publication 'hello' to Maven repository 'hello'.
diy:publishHelloPublicationToMavenLocal 
  - Publishes Maven publication 'hello' to the local Maven repository.
diy:publishPluginMavenPublicationToHelloRepository 
  - Publishes Maven publication 'pluginMaven' to Maven repository 'hello'.
diy:publishPluginMavenPublicationToMavenLocal 
  - Publishes Maven publication 'pluginMaven' to the local Maven repository.
diy:publishToMavenLocal 
  - Publishes all Maven publications produced by this project to the local Maven cache.
~~~

注释写的很清楚了，我需要的是publishHelloPublicationToHelloRepository：

~~~java
 ~/gradle_demo  gradle publishHelloPublicationToHelloRepository

BUILD SUCCESSFUL in 1s
7 actionable tasks: 7 executed
~~~

根项目就多了个repo文件夹啦，就可以在其他项目愉快地使用了。

