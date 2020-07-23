[toc]





# 说明

以下只列出常用的命令，更多命令请参考:

+ [https://juejin.im/entry/57c00fe4c4c971006179838a#%E6%9F%A5%E7%9C%8B%E6%97%A5%E5%BF%97](https://juejin.im/entry/57c00fe4c4c971006179838a#查看日志) 
+ adb --help"
+ adb shell am --help
+ adb shell pm --help
+ adb shell input --help
+ https://www.runoob.com/w3cnote/linux-common-command-2.html
+ 搜索引擎

# adb 常用指令

## 多个设备在连接着adb的时候，将命令指定给具体设备

~~~xml
adb -s 172.17.197.51:5555 command
~~~

目的：在172.17.197.51:5555这个设备执行command

## 查询已经连接的设备

~~~xml
adb devices 

展示详细信息
adb devices -l
~~~

## 连接设备

~~~xml
adb connect <host>[:<port>]  
例：adb connect 192.168.1.105:1379
~~~

注意：Port 5555 is used by default if no port number is specified.

## 断开设备

 ~~~xml
adb disconnect [<host>[:<port>]]

adb disconnect 192.168.1.105:1379
 ~~~

注意：Port 5555 is used by default if no port number is specified.

## 推送文件到指定设备的指定目录

~~~xml
 adb push <local>... <remote>
~~~

## 指定设备下载文件到本地

~~~xml
adb pull [-a] <remote>... <local>
~~~

## 查看设备所有程序的日志

~~~xml
 不过滤
 adb logcat 
 
 过滤出CIoTService的日志
 adb logcat | grep [tag]
 
 
 按级别过滤日志
Android 的日志分为如下几个级别：

V —— Verbose（最低，输出得最多）
D —— Debug
I —— Info
W —— Warning
E —— Error
F —— Fatal
S —— Silent（最高，啥也不输出）
按某级别过滤日志则会将该级别及以上的日志输出。

比如，命令：

adb logcat *:W
会将 Warning、Error、Fatal 和 Silent 日志输出。

按 tag 和级别过滤日志
比如，命令：

adb logcat ActivityManager:I MyApp:D *:S
表示输出 tag ActivityManager 的 Info 以上级别日志，输出 tag MyApp 的 Debug 以上级别日志，及其它 tag 的 Silent 级别日志（即屏蔽其它 tag 日志）。
~~~



## 安装软件

~~~xml
adb install [-lrtsdg] <file>
push this package file to the device and install it
                                 (-l: forward lock application)
                                 (-r: replace existing application)
                                 (-t: allow test packages)
                                 (-s: install application on sdcard)
                                 (-d: allow version code downgrade (debuggable packages only))
                                 (-g: grant all runtime permissions)
~~~



## 卸载软件

~~~xml
adb uninstall [-k] <package> - remove this app package from the device
                                 ('-k' means keep the data and cache directories)
~~~

## 以管理员权限使用adb

adb 的运行原理是 PC 端的 adb server 与手机端的守护进程 adbd 建立连接，然后 PC 端的 adb client 通过 adb server 转发命令，adbd 接收命令后解析运行。

所以如果 adbd 以普通权限执行，有些需要 root 权限才能执行的命令无法直接用 `adb xxx` 执行。这时可以 `adb shell` 然后 `su` 后执行命令，也可以让 adbd 以 root 权限执行，这个就能随意执行高权限命令了。

~~~xml
adb root
adb remount
~~~



## 启动 adb server 命令：

```xml
adb start-server
```

（一般无需手动执行此命令，在运行 adb 命令时若发现 adb server 没有启动会自动调起。）

## 停止 adb server 命令：

```xml
adb kill-server
```

---



# adb shell 常用命令

## 查询当前显示的Activity

~~~xml
详细显示
adb shell dumpsys activity top
简略显示
adb shell dumpsys activity top | grep ACTIVITY
~~~

## 查询当前展示的window

~~~hxml
adb shell dumpsys window windows | grep mCurrent
~~~

## 根据包名查询进程号

~~~xml
adb shell ps | grep com.cvte.ciot.service
~~~

## adb启动某个activity

~~~xml
adb shell am start -n com.cvte.ciot.service/com.cvte.ciot.service.ui.activity.MainActivity
~~~

## adb 启动某个服务

~~~xml
adb shell am startservice -n com.example.test/com.example.test.MyService
~~~

## adb 发送广播

~~~xml
$ adb shell
$ am broadcast -a <action>
am broadcast 后面的参数有：
[-a <ACTION>]

[-d <DATA_URI>]

[-t <MIME_TYPE>]

[-c <CATEGORY> [-c <CATEGORY>] ...]

[-e|--es <EXTRA_KEY> <EXTRA_STRING_VALUE> ...]

[--ez <EXTRA_KEY> <EXTRA_BOOLEAN_VALUE> ...]

[-e|--ei <EXTRA_KEY> <EXTRA_INT_VALUE> ...]

[-n <COMPONENT>]

[-f <FLAGS>] [<URI>]

例如：

am broadcast -a MyAction --es city "shenzhen" --ei year 2014 --ez flag true

 说明：红色为EXTRA_KEY，绿色为VALUE，分别为String类型，int类型，boolean类型


~~~

## 查看所有已经安装软件

~~~xml
adb shell pm list packages -f

//查看是否已经有安装
adb shell pm list packages -f | grep  com.cvte.ciot.service
~~~

 

## 查看设备属性

~~~xml
 adb shell getprop
 
 可以添加过滤
 adb shell getprop persist.sys.spider.debug
~~~

## 添加和设置设备属性

~~~xml
有就设置，没有会添加并设置
adb shell setprop persist.sys.spider.debug true
~~~

## 模拟按键

~~~xml
adb shell input keyboard [--longpress] <key code number or name>

例：
adb shell input keyevent 4
~~~

~~~xml
keycode	含义
3	HOME 键
4	返回键
5	打开拨号应用
6	挂断电话
24	增加音量
25	降低音量
26	电源键
27	拍照（需要在相机应用里）
64	打开浏览器
82	菜单键
85	播放/暂停
86	停止播放
87	播放下一首
88	播放上一首
122	移动光标到行首或列表顶部
123	移动光标到行末或列表底部
126	恢复播放
127	暂停播放
164	静音
176	打开系统设置
187	切换应用
207	打开联系人
208	打开日历
209	打开音乐
210	打开计算器
220	降低屏幕亮度
221	提高屏幕亮度
223	系统休眠
224	点亮屏幕
231	打开语音助手
276	如果没有 wakelock 则让系统休眠
~~~

