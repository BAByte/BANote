# Broadcast Receiver使用

[TOC]



## 必备知识

### 什么是Broadcast Receiver？有什么用？

>  顾名思义，广播接收器，广播机制在安卓中是非常常见的，比如，在系统开机后，会发出一条开机广播，网络发生了改变，就发出一条网络改变的广播，所以就像实际生活中一样，广播就是消息通知，通过接收到的广播(消息通知)，我们可以做出对应的举措。检测某种状态时用的

### 安卓中广播的机制

>1. 标准广播：在广播发出后，所有的接收器几乎会同时接收到，没有先后顺序，所以不能在中途截断
>2. 有序广播: 在广播发出后，一次只能由一个广播接收器接收，当这个广播接收器里面的逻辑执行完后，广播才会传给下一个广播接收器，接收优先级高的接收器会先接收到广播，所以就可能在任意一个接收器中中断这条广播



### 怎样自定义广播接收器

>### 新建我们的广播接收器类，继承自BroadcastReceiver，必须复写onReceive（）



> ~~~java
> import android.content.BroadcastReceiver;
> import android.content.Context;
> import android.content.Intent;
> import android.widget.Toast;
>
> /**
>  * Created by ljh99 on 2017/7/8 0008.
>  */
>
> public class MyNetWorkBroadcastReceiver extends BroadcastReceiver {
>   
>   //该方法在接收到广播后会自动被调用，所以我们可以在这里执行我们的操作逻辑
>     @Override
>     public void onReceive(Context context, Intent intent) {
>         Toast.makeText(context, "Network Change", Toast.LENGTH_SHORT).show();
>     }
> }
> ~~~





## 如何指定我们的广播接收器接收什么样的广播

### 动态注册

>用到才在代码中注册
>
>> 这里和书上一样接收系统的网络状态广播，首先先要明白，系统会发出，一堆广播，而我们的广播接收器只接收一条，发出广播是用Intent发出的，所以我们要有Intent过滤器，设置好过滤器后，还要有我们的广播接收器实例，然后将我们的广播接收器和过滤器提交给系统注册
>>
>> ~~~java
>>  //广播是通过Intent来发送的，我们要获取一个IntentFilter（过滤器）
>>         //来设置设置我们接收哪个广播
>>         IntentFilter intentFilter=new IntentFilter();
>>
>>         //这里的参数action就是网络情况发生变化后，系统发出的广播
>>         intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
>>
>>         //获取我们自定义用来检测网络情况的Broadcast Receiver实例
>>         mybr=new MyNetWorkBroadcastReceiver();
>>
>>         //注册接收器
>>         registerReceiver(mybr,intentFilter);
>> ~~~
>>
>> **当然还要取消注册我们的广播接收器，当然在什么时候取消就是你的事了**
>>
>> ~~~java
>>  		//一定要取消注册
>>         unregisterReceiver(mybr);
>> ~~~
>>
>



### 静态注册

>静态注册就可以让我们程序中的静态注册的广播接收器可以在程序未启动就监听接收广播，比如我们监听开机广播，然后在onReceive()方法中启动服务，然后在服务中启动程序。



>#### 这里我们实现静态注册，达到监听开机广播的效果，
>
>> + 静态的广播接收器必须要在AndroidManifest文件中注册，所以用as的快捷创建广播接收器，这样我们就不用在AndroidManifest文件中注册了，注册的代码下面贴出来，在<application>标签中，下面的代码自己参悟哈。
>>
>>   ~~~xml
>>   		<receiver
>>               android:name=".MyReceiver"
>>               android:enabled="true"
>>               android:exported="true">
>>           </receiver>
>>   ~~~
>>
>> + 接下来就是设置过滤器了，我们知道，在AndroidManifest文件中在注册的控件里面可以设置过滤器的，比如活动，我们这里要监听开机的广播，而监听开机的广播是需要权限的，所以为了可以监听到开机广播，需要在AndroidManifest文件中声明权限，看代码，这段代码插在<manifest>标签中
>>
>>   ~~~xml
>>   <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
>>   ~~~
>>
>> + 接下来和动态一样，在广播接收器的类的onReceive()方法中设置我们要执行的逻辑即可
>



## 使用广播接收器需要注意的问题

>> 在广播接收器的onReceive()方法中，不能开启线程，所以在这里不能进行耗时任务，不然程序会崩溃
>
>### 广播接收器优先级（在接收有序广播中有用）
>
>都是在过滤器中设定的
>
>+ 动态注册的广播接收器的优先级
>
>  ~~~java
>  intentFilter.setPriority(200);
>  ~~~
>
>+ 静态注册的优先级设置
>
>  ~~~xml
>  			<intent-filter android:priority="100"> <--!这就是-->
>                  <action android:name="android.intent.action.MY_BROADCAST"/>
>              </intent-filter>
>  ~~~
>
>  ​

# 自定义发送广播

## 全局广播

### 标准广播

~~~java
Intent i=new Intent("android.intent.action.MY_BROADCAST");//自定义的action
sendBroadcast(i);
~~~

### 有序广播

~~~java
Intent i=new Intent("android.intent.action.MY_BROADCAST");
sendOrderedBroadcast(i, null);//第二个参数是和权限有关的
~~~

### 有序广播的截断

~~~java
abortBroadcast();//广播接收器类的截断方法
~~~



## 本地广播

### 分析

> 其实就是在程序内部发送广播，前面用的注册广播方法都是安卓sdk默认的api发送的都是全局广播
>
> 为了在程序内部发送广播和接收广播，就是让程序有自己的广播系统，用程序本身的广播站来注册，发送广播

### 具体实现

> + 获取程序本地的广播管实例，就是本地的广播站啦
>
>   ~~~java
>    localBroadcasrManager=LocalBroadcastManager.getInstance(this); 
>   //一看就知道使用了单例模式，因为管理本地的广播站只能有一个
>   ~~~
>
> + 用程序本身的广播站来注册广播接收器，就可以接收到本地的广播了
>
>   ~~~java
>   	    IntentFilter intentFilter=new IntentFilter();
>           intent.addAction("android.intent.action.MY_BROADCAST");
>           localBroadcasrManager.registerReceiver(广播接收器实例,intentFilter);
>
>   		//取消注册
>   		localBroadcasrManager.unregisterReceiver(广播接收器实例);
>   ~~~
>
> + 发送本地广播
>
>   ~~~java
>   Intent i=new Intent("android.intent.action.MY_BROADCAST");
>                   localBroadcasrManager.sendBroadcast(i);
>   ~~~
>
>   ​