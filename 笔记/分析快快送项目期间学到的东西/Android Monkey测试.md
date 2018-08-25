# 安卓Monkey测试
## 先说两句
翻译一下，Monkey嘛，猴子的意思，猴子嘛，搞怪滑稽咯。猴子测试？其实不是这个意思。在开发完软件的时候，我们需要对软件进行测试，我们怎么测试？就拿起来随便按啊，猛按啊，各种地方点一点啊，但是我们会漏很多地方，所以安卓官方就出了个调试工具，专门用来模拟屏幕点击，而且是随机的，而且角度还非常刁钻。
## 先看看怎么用
### adb常用命令
~~~
adb devices 获取所有连接ADB的模拟器或者真机 
adb install c:/xxx.apk 安装自己的apk到设备上 
adb uninstall apk包名 从设备上卸载apk 
adb -s emulator-5556 uninstall apk包名 指定某设备卸载apk 
adb start-server 重启adb 
adb kill-server 杀死adb 
adb shell pm list packages 获取所有应用的包名
~~~
### cmd下使用adb直接调试
你要先配置环境变量：
+ 第一步先找到adb.exe的路径,我的是这个：C:\Users\BA\AppData\Local\Android\sdk\platform-tools
+ 第二步就开始配置，找到系统变量Path，新建一个路径，路径为你adb.exe的路径
+ 第三步就是打开命令行 先测试下adb有没有配置好：在cmd输入 adb version 如果有adb的版本信息就是配置成功了
+ 然后就是使用对应的命令了

### 使用android studio的控制台进行调试(建议，因为直接可以看到程序崩溃的log)
+ 配置adb和上面的一样
+ 在android studio工具的最下面Terminal控制台，点出来后就可以直接使用monkey对应的命令了

### monkey的命令
在使用git的时候，都是有固定语法，git xxx，那这个monkey当然也有命令语法
~~~
adb shell monkey –help (获取帮助命令) 
adb shell monkey 1000 (随机执行1000个模拟事件) 
adb shell monkey -p xxxx 1000 (指定某个应用随机执行1000个模拟事件,xxx填应用包名) 
adb shell monkey (参数) 100 (带参数执行100个模拟事件)
~~~

我们来看看带参数的模拟事件，这也是我们很常用的，看看参数列表：参考：https://www.cnblogs.com/summer-sun/p/5743851.html
### 测试Log获取
~~~
用Monkey test测试，为了方便分析问题，可以在命令monkey命令后面加上 “|logcat -v time”，这样就能边测试边打印Log（记得用终端保存Log文本）例如：

 adb shell monkey –p com.dianping.XXX  –v 20000|logcat -v time

 但是这个组合在monkey测试完成后，logcat仍然在执行，测试时需要注意

 如果测试monkey碰到crash或者anr（程序强制性退出或不响应）现象时，在抓取log的同时需要提供traces.txt，

 步骤如下：{ adb pull /data/anr/traces.txt .}

 1. 测试版本需要具备root权限

 2. 进入data/anr目录下面

 3. 将traces.txt文件拷贝到TF卡中，然后拷贝出来发给软件分析即可  eg. trace view
 4. 
~~~

### 保存monkey log以及手机log到sdcard
一、首先在cmd或终端中输入adb shell

二、再执行monkey，比如

  monkey -p com.dianping.XXX --throttle 500 --ignore-crashes --ignore-timeouts --ignore-security-exceptions --ignore-native-crashes --monitor-native-crashes -v -v -v 100 >/mnt/sdcard/monkey.txt & logcat -v time >/mnt/sdcard/logcat.txt

 NOTE：如果不想生成logcat文件，删除红色标记（& logcat -v time >/mnt/sdcard/logcat.txt）中即可

三、终端内打开存放日志地址，比如

 cd /Users/wengyb/Documents

 四、拖日志到电脑上，比如

 adb pull /mnt/sdcard/monkey.txt 

六、手动停止Monkey
adb shell ps（查找进程PID）

adb shell kill pid （杀掉进程）

## 实战
小米应用审核是这样测试的
~~~
adb shell monkey -p com.waibao.team.cityexpressforsend -v --throttle 300 --pct-touch 30
--pct-motion 20 --pct-nav 20 --pct-majornav 15 --pct-appswitch 5 --pct-anyevent 5 --pct-trackball 0 --pct-syskeys 0 1000
~~~
分析：看最后，1000 就是一千个随机事件，那么这个1000前面的就都是参数咯，一个一个来
~~~

指定只测试这个软件
-p com.waibao.team.cityexpressforsend 

反馈信息级别为1
-v

每个事件相差300毫秒
--throttle 300

触摸事件占的百分比为30
--pct-touch 30

滑动事件占百分之20
--pct-motion 20

基本导航按键事件占20，就是上下左右的按键输入
--pct-nav 20 

主要导航按键，就是返回，菜单这些按键
--pct-majornav 15

调整启动Activity的百分比
--pct-appswitch 5

它包罗了所有其它类型的事件，如：按键、其它不常用的设备按钮等等
--pct-anyevent 5

轨迹事件由一个或几个随机的移动组成，有时还伴随有点击
--pct-trackball 0

这些按键通常被保留，由系统使用，如Home、Back、Start Call、End Call及音量控制键
--pct-syskeys 0
~~~

上面就是小米商城的测试，我们可以根据具体需求在上面参数中更改我们想要的事件占比

## 总结
这就是压力测式，但是只能测出程序异常，但是处理逻辑异常这里测试不出来！！！
