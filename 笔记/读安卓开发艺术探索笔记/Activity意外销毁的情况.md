[toc]
# Activity意外销毁的情况
## 先说两句
在以前其实就有学，而且难度很小。但是为什么还要再写一个笔记？因为在《第一行代码》中漏了一个方法。意外销毁的意思就是，Activity没有走完生命周期。
## 什么情况下会导致Avtivity意外销毁
### 系统资源内存不足（导致优先级低的软件会被杀死）
这个很难去模拟，比如说你开着一个软件（该软件没有前台服务），然后你去打开几个游戏，那么你手机肯定会因为内存不足而关闭你刚刚开着的软件，这里的关闭是直接杀死软件，连生命周期都不给走完。
### 资源相关的系统配置改变，Activity会被杀死并且重新创建
这种情况其实很常见，这些配置都有什么？

“mcc“ 移动国家号码，由三位数字组成，每个国家都有自己独立的MCC，可以识别手机用户所属国家。

“mnc“ 移动网号，在一个国家或者地区中，用于区分手机用户的服务商。

“locale“ 所在地区发生变化。

“touchscreen“ 触摸屏已经改变。（这不应该常发生。）

“keyboard“ 键盘模式发生变化，例如：用户接入外部键盘输入。

“keyboardHidden“ 用户打开手机硬件键盘

“navigation“ 导航型发生了变化。（这不应该常发生。）

“orientation“设备旋转，横向显示和竖向显示模式切换。

“fontScale“ 全局字体大小缩放发生改变


### 怎么不让某项系统配置改变后，Activity不被重建？

比如说旋转屏幕Activity不重建
+ 在AM文件中配置
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.ba.myflagdemo">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity"
        就是这一行
            android:configChanges="orientation|screenSize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
~~~

+ 在Activity中
~~~java
 /**
     * On configuration changed.该方法是在系统配置改变后自动调用的方法
     *
     * @param newConfig the new config
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //在该方法中做一些你要的处理
    }
~~~



