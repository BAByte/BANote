# 项目文件中build.gradle文件详解

[TOC]



## 外层build.gradle文件

```java
buildscript { 
    repositories {
        jcenter() //声明了代码托管仓库是Jcenter ，有了这行配置后就可以在项目中引用jcenter上的开源项目
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.2' //声明了构建项目的插件版本

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        jcenter()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```



## 内层bulid.gradle文件

~~~java
//这里声明了这是一个应用程序模块
//com.android.library表明这是一个库模块
//两者的差别就是，应用程序模块是直接运行的，库模块是作为代码库依附于程序运行的
apply plugin: 'com.android.application' 

android {
    compileSdkVersion 25 //指定编译的版本
    buildToolsVersion "25.0.2" //指点编译工具的版本
    defaultConfig {
        applicationId "com.example.ljh99.helpwangtext" //指定包名
        minSdkVersion 15  //指定最低兼容的系统版本  
        targetSdkVersion 25  //声明已经在指定的版本中进行过了稳定测试，这样才会开放该版本的特有功能
        versionCode 1  //项目版本号
        versionName "1.0" //版本名字
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false  //指定是否进行代码混淆
              
              //指定混淆文件，proguard-android.txt是默认的混淆规则
              //proguard-rules.pro存放在根目录下，可以编写自己的规则
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {//该闭包指定当前所有项目的依赖关系
    compile fileTree(dir: 'libs', include: ['*.jar']) //指定本地依赖，将libs的所有依赖库导入到项目中
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    compile 'com.android.support:appcompat-v7:25.3.1' //指定远程依赖
    compile 'com.android.support.constraint:constraint-layout:1.0.2'
    //compile 'project(':helper') 添加名为helper库的依赖
    testCompile 'junit:junit:4.12'
}
~~~

