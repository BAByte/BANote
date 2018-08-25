[toc]
# IntentFilter的匹配原则
## 先说两句
这个东西，也是学过的，但是还是再学一次，很久没用过了，或者说根本没有用过，什么情况会用到这个？隐式启动Activity的时候。比如你写一个音乐播放器，如果我在文件管理器点击一个音乐，就会弹出选择用什么软件来打开这个类型的文件，如果你的音乐播放器有配置IntentFliter，并且符合匹配规则的话，那就会出现在选项中。估计不说也知道，这篇笔记主要是记一下匹配规则
## 过滤信息
所有的软件中会有个AndroidManifest.xml，系统会根据所有软件的AndroidManifest.xml生成一个信息树，然后你用Intent显示或者隐式启动活动的时候，系统会根据AndroidManifest.xml，里面有Ativity设置的规则，和你启动时用的Intent设置的规则进行过滤筛选，进而选出你要启动的Activity，直接说规则可能比较费解，这个InterFilter就是声明我这个Activity可以对某种数据类型做合理的事情。
## 匹配规则
一共有3个部分需要匹配,这里我们慢慢解释，但是要先看看这个东西在哪里出现

~~~xml
 <activity android:name=".MainActivity"
            android:configChanges="orientation|screenSize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
                <data android:scheme="http"/>
            </intent-filter>
</activity>
~~~

看到上面了吗，你需要给Activity设置一个IntentFilter，这个有三个节点

### action
这个就是他的动作的意思，这个Activity的动作，这个你可以随便取的，比如我这个Activity可以相应录音这个操作，那你可以直接用包名加拼音（录音）。这个action可以设置很多个，那么是不是全部都要匹配上才算匹配成功呢？其实不然，只要有一个匹配成功就好，比如你会唱歌，跳舞，演戏，那我需要个会唱歌的人，你会唱歌，好就是你了！！但是如果你一旦设置IntentFilter，那就必须至少有一个action，而且还区分大小写
### category
这个是进一步来筛选Activity的，我们现在搞个处理音频的软件，现在有两个Activity，一个处理视频一个处理音乐，action假设都是声明了可以处理media，那这两个Activity其实都是可以被匹配到，但是如果真的把音乐给处理视频的Activity肯定出问题呀，那怎么再细分？
~~~xml
//这个给那个专门用来处理音乐的Activity
<intent-filter>
    <action android:name="xxx.intent.action.dealwith.media" />
    <category android:name="xxx.intent.category.music" />
    <data android:scheme="http"/>
</intent-filter>

//这个给那个专门用来处理视频的Activity
<intent-filter>
    <action android:name="xxx.intent.action.dealwith.media" />
    <category android:name="xxx.intent.category.video" />
    <data android:scheme="http"/>
</intent-filter>
~~~

其实相对于action来说就是再细分，再分类别的意思，那匹配规则是否也一样呢？我们思考一下，假设你会吃，这个当成一个action，但是有些人他就很挑食，比如不吃榴莲（榴莲这么美味的东西都不吃，就是挑食，不接受反驳）虽然你匹配到了吃这个action，但是，如果一个吃苹果，吃香蕉，但是不吃榴莲，你叫他吃苹果，没问题，但是叫去吃榴莲，他当然不同意，这里就可以看出category的匹配规则是和action不一样的，你的intent的category必须是activity里面IntentFilter的子集，空集是任何集合的子集哦！！

在我们调用startActivity或者startActivityForResult的时候，系统会自动为我们的Intent加上一个category
~~~xml
 <category android:name="android.intent.category.DEFAULT"/>
~~~

所以我们的Activity要是想要设置intentFilter来实现隐式启动Activity，就必须加上上面那个默认的category。
category可以设置多个，区分大小写

### data
这个就好理解了，这个就是继续进一步细分筛选，可以处理什么类型的数据。但是data节点比较特别，我们看看他的模板
~~~xml
<data
    android:scheme="string"
    android:host="string"
    android:path="string"
    android:pathPattern="string"
    android:pathPrefix="string"
    android:mimeType="string"/>
~~~
其实就是URI加mimeType的格式，
mimeType指媒体类型，比如image/jpge，video/MP4


至于URI，这个就很常用了吧。。。

+ scheme：URI的模式，如果没有指定这个，其他就是指定了，这个URI还是无效
+ host：端口号，不指定也是整个URI无效
+ port：端口号
+ 后面三个代表路径信息，
~~~
注意，“*”要想表示 '*',那就要这样写 '\\*'
~~~

匹配规则呢？他是和action一样的，一个有就好

那看看写法吧
+ 写法1
~~~xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
    <data android:mimeType="image/*" android:scheme="http"/>
</intent-filter>
~~~

### 给我们的Intent设置过滤规则
~~~java
        Intent intent=new Intent("action");//只能设置一个
        
        //可以设置多个
        intent.addCategory("");
        intent.addCategory("");
        
        //同时设置URI和mimeType
        intent.setDataAndType(Uri.parse("http://www.baidu.com"),"image");
        
        //单个设置，那么URI会被清空，就变成了只设置了mimeType，没有设置URI那么默认的URI是content和file
        intent.setType("");
        
        //单个设置，mimeType会被清空
        intent.setData("");
        
~~~

## 使用的隐式启动的时候需要注意的地方
当我们隐式启动Activity的时候容易出现一个问题，你的Intent匹配到了一个不能接收到隐式Intent的Activity，为什么这么说？因为你的Intent是可能没有手动添加category，那他就不去管这个category匹配不匹配的上，那只要action和data都匹配上，系统就会用这个Activity来启动，但是这个Activity如果没有设置那个默认的category，那就不能接收到隐式Intetnt，就会报错，所以我们要让隐式启动时只匹配那些带有默认category的Activity，怎么做？
~~~java
   if (intent.resolveActivity(getPackageManager())!=null){
            startActivity();
    }
~~~