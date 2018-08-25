# 隐式Intent的用法

[TOC]



## 用法一：

使用隐式Intent启动程序中的活动

### 分析  

+ 什么是隐式Intent？？？

  > 使用隐式Intent，设置好action和category，系统会自动找出能够同时满足Intent中设定的action和category的活动,隐式Intent和显示启动活动的对比，先看显示，显示Intent就是指名道姓的需要打开哪一个活动，隐式Intent是不知道具体活动的名字，所以就要去筛选，怎么筛选呢，如果一个活动想要给其他活动隐式启动就会标签action和category，action标签呢是声明活动主要能干什么，category就是活动还具备什么特点，所以在使用隐式Intent时就要指定标签进行筛选了

+ 如何启动

  > 在活动中使用隐式Intent的代码


  > ~~~java
  > //传入的参数是action，只能指定一个
  > Intent intent=new Intent("com.exmple.ljh998.MY_INTENT_ACTION");
  > //添加category，可以添加多个
  > intent.addCategory("com.exmple.ljh998.MY_INTENT_CATEGORY");
  > startActivity(intent);
  > ~~~
  >
  > ​

+ 如何为自己的Activity指定能响应的action和category？？？

  >在AndroidManifest.xml文件中，需要注意 `<category android:name="android.intent.category.DEFAULT"/>`这一行，这是必需要加的默认category，但是在启动时在活动中是不用添加这个category的，可以多个action和category


  > ~~~xml
  >  <activity android:name=".Main2Activity">
  >             <intent-filter>
  >                 <action android:name="com.exmple.ljh998.MY_INTENT_ACTION"/>
  >                 <category android:name="com.exmple.ljh998.MY_INTENT_CATEGORY"/>
  >                 <category android:name="android.intent.category.DEFAULT"/>
  >             </intent-filter>
  >         </activity>
  > ~~~
  >
  > ​



## 用法二

根据当前Intent正在操作的数据来启动其他程序的活动比如，拨号，打开网页

![捕获](E:\Android第一行代码，笔记\隐式Intent的用法\捕获.PNG)

### 分析

~~~java
Intent intent=new Intent(Intent.ACTION_VIEW); //这是安卓系统内置动作   
//参数要求传入一个uri对象，这里传入字符串，然后解析成uri对象
intent.setData(Uri.parse("http://www.baidu.com")); 
startActivity(intent);
~~~



## 用法三

让自己的活动也可以相应打开网页的操作

![捕获2](E:\Android第一行代码，笔记\隐式Intent的用法\捕获2.PNG)

### 分析

+ 可以知道AndroidManifest.xml文件中，activity的标签里，可以加action和category，当然可以添加data响应对应的数据类型

  ~~~xml
  <activity android:name=".Main2Activity">
              <intent-filter>
                  <action android:name="android.intent.action.VIEW"/>
                  <category android:name="android.intent.category.DEFAULT"/>
                  <data android:scheme="http"/>
              </intent-filter>
  </activity>
  ~~~

  ​

+ 上面的代码中**<data>**标签中多了个'<data android:scheme="http"/>'意思就是该活动能响应的数据类型有http

+ 具体还能设置有如下

  ​

  >| scheme   | 用于指定数据的协议部分，比如http部分         |
  >| :------- | ---------------------------- |
  >| host     | 用于指定数据的主机名部分，比如www.baidu.cmo |
  >| port     | 用于指定数据的端口部分，一般跟在主机名后         |
  >| path     | 指定主机名和端口后的部分                 |
  >| mimeType | 指定可以处理的数据类型，可以用通配符           |

  ​