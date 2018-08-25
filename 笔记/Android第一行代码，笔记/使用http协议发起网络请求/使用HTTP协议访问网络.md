[TOC]

# 使用HTTP协议访问网络

## 必备知识

> 首先我们要知道其实Http协议是一种客户端与服务器端交流的协议，http被叫做超文本，网址(URL)的协议部分就是http，就是告诉程序这是网址啦
>
> URL和Uri的区别：
>
> Uri是资源的唯一标示符：每个资源都可以有唯一的Uri，就像身份证号码
>
> URL是资源定位符：就像你的家庭住址

## 使用官方API

+ ### 使用到的类

  > +  URL 
  >
  >   >  用来指定网址，返回（操作网络）HttpURLConection实例
  >
  > + HttpURLConection
  >
  >   > 设置访问链接的一些属性，比如是向服务器请求数据还是上传数据
  >   >
  >   > 加载连接的最大时长，读取数据的最大时长，等等等等，设置完后就可以用该实例打开InputStream或者OutputStream

+ ### 具体实现

  > ~~~java
  > URL url = new URL("https://www.baidu.com");
  > HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
  > httpURLConnection.setConnectTimeout(8000);
  > httpURLConnection.setReadTimeout(8000);
  > httpURLConnection.setRequestMethod("GET");
  > InputStream in = httpURLConnection.getInputStream();
  >
  > //如果是上传数据就setRequestMethod("POST");
  > //上传时数据与数据之间用&分开
  > ~~~
  >
  > **需要注意的是，请求返回数据其实是耗时任务，所以要在线程里面进行由于启动的新的线程，那么想要在该线程中操作UI控件，就必须回到主线程中，安卓规定的**
  >
  > ~~~java
  >  runOnUiThread(new Runnable() {
  >             @Override
  >             public void run() {
  >                 //在这里操作UI
  >             }
  >         });
  > ~~~
  >

