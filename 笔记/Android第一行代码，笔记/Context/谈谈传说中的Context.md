[TOC]

# 谈谈传说中的Context

## 先聊聊生活中的Context

> 在刚学安卓的时候啊，看到到处都有Context，创建Activity要，启动服务要，加载对象要，弹出对话框要，一开始我并不了解什么是Context，翻译过来就是上下文，然后我又不懂什么是上下文，问好似什么都懂的曾老师，可能境界不一样吧，他就一句话，系统环境。。。。说实话我现在都理解不了！！！抛开软件方面的Context，任何东西都是源于生活，上下文在生活中如何理解？我看到个新闻，一个人在对比市面上的智能语音助手，以前的语音助手，你问：北京下雨吗？答：晴天啊什么的，你再问：温度多少，语音助手就蒙蔽了。这里就有上下文了，一开始你问的是北京，然后你再一次提问，其实场景还是北京，但是当时的语音助手并没有根据之前的对话去回答你，因为它没有连接对话的上下文，来进行数据处理。再比如说，小时候我们刚开学，一拿到课本，就忍不住看语文书里面的文章(其实就是没有正式上课，又只能在课室，只有翻书了)，小时候不管太多，随便一翻到一课，然后随便看一段看看有没有意思，正好你读到了一段，范进高兴的掉到了河里，死了？不记得了，那就死了吧，心里就想，这货绝对是个傻逼，中个举就开心到这样，你觉得很有意思就从头开始读，读完后你发现，原来他尝试了那么多么，考了那么多次啊，难怪了，这里你有两次对范进的评价，第一次，显然你是没有读完整，所以你的评价是没有根据上下文来评价的，简单的说就是你不知道具体的情况，后来你读完后，根据上下文，就又做出了评价，这时候的你已经具体的了解了情况了，现在应该懂什么叫上下文了吧！！！

##安卓中什么是Context

> 源码的注释是，应用程序的全局信息，就是全局的上下文的意思，

+ 先看看继承结构

![20151022212109519](D:\Android第一行代码，笔记\Context\20151022212109519.png)

> + 这是Context的继承关系图，ContextWrapper是功能的封装类，ContextImpl是功能的实现类，前者一开始可能懵逼，一开始我也没想明白什么叫功能封装类，原来是使用组合的方式，在ContextWrapper里面提供功能的接口，事实上这个接口里面并没有实现功能的逻辑，而是应该用一个实现该功能的具体对象类的实例，说白了就是再加了一层封装，看看源码吧
>
> ~~~java
> //构造函数传入了一个Context，其实应该是Contextlmpl的实例，这里可能还看不出什么，我们继续往下看
> public ContextWrapper(Context base) {
>         mBase = base;
>     }
> ~~~
>
> ---
>
> ~~~java
> //这里就给两个吧，你可以清楚的看到， 全是直接调用刚刚传入的Context的方法。这下明白什么是把功能封装了吧
>     @Override
>     public AssetManager getAssets() {
>         return mBase.getAssets();
>     }
>
>     @Override
>     public Resources getResources() {
>         return mBase.getResources();
>     }
> ~~~
>
> ---
>
> + 在上面的图中可以看到，我们常用的Context一共有三种类型，分别是Application、Activity和Service，Activity继承ContextThemeWrapper。这是因为Activity有主题（Activity提供UI显示，所以需要主题），而Service是没有界面的服务。那么Context到底可以实现哪些功能呢？这个就实在是太多了，弹出Toast、启动Activity、启动Service、发送广播、操作数据库等等等等都需要用到Context，最重要的还是获取资源的时候。由于Context的具体能力是由ContextImpl类去实现的，因此在绝大多数场景下，Activity、Service和Application这三种类型的Context都是可以通用的。不过有几种场景比较特殊，比如启动Activity，还有弹出Dialog。出于安全原因的考虑，Android是不允许Activity或Dialog凭空出现的，一个Activity的启动必须要建立在另一个Activity的基础之上，也就是以此形成的返回栈。而Dialog则必须在一个Activity上面弹出（除非是System Alert类型的Dialog），因此在这种场景下，我们只能使用Activity类型的Context，否则将会出错(郭大说的)
>
> 我们来理一理，
>
> + 我们用的Context都是一个简单的功能封装类，真正实现的类其实是ContextImpl
> + Context的功能都是由一个由ContextImpl类实现的，那说明功能都差不多，Context大部分情况下可以通用
> + 特殊的情况就是，一个Activity的启动必须要建立在另一个Activity的基础之上，也就是以此形成的返回栈。而Dialog则必须在一个Activity上面弹出（除非是System Alert类型的Dialog）否则出错
> + 你启动一个Activity，使用了当前Activity作为Context吧，系统会知道谁启动了这个Activity，当前Activity在finish后就能返回到上一个Activity，重点就是，系统知道上一个Activity是谁！这不就是上下文咯，有的抽象吧，我也觉得。。。

## Application

> 不管是学安卓还是JAVAWEB，都有Application这个概念，是不是说软件工程都有这个概念，下面来看看安卓里面的Appliction，在使用了那么多次Context，是不是这个类太强大了，可以全局的使用还实现了一堆的系统级别的功能。这个所谓的Application类型的Context，其实就是一个最大的全局变量，记录了一些全局信息和方法，说这里，就会有错觉Application就是整个程序的全局工具类？比如我要写一个全局的工具类，专门全局获取Context，为什么要这样？很多时候在Class里面，我们需要获取Context，这是我们自己写的Class，根本没有什么getApplication方法，我们自己写一个Application的子类，试试？？？
>
> ~~~java
> /**
>  * Created by BA on 2017/10/13 0013.
>  *
>  * @Function : 自定义一个Application，用来全局获取Context
>  */
>
> public class MyApplication extends Application {
>     
>     //创建时存住
>     private static Application a;
>
>     public MyApplication(){
>         super();
>         a=this;
>     }
>     
>     /**
>      *@fuction 获取有效的全局Context
>      *@parm
>      *@return 返回程序一开始运行就创建的那个Application
>      *@exception
>      */
>     public static Application getApplication(){
>         return a;
>     }
> }
> ~~~
>
> ---
>
> ~~~xml
>    <!--在name这个属性，指定应用启动时创建哪一个Application-->
> 	<application
>         android:name=".MyApplication"
>         android:allowBackup="true"
>         android:icon="@mipmap/ic_launcher"
>         android:label="@string/app_name"
>         android:roundIcon="@mipmap/ic_launcher_round"
>         android:supportsRtl="true"
>         android:theme="@style/AppTheme">
> ~~~
>
> ---
>
> ~~~java
> /**
>  * Created by BA on 2017/10/13 0013.
>  *
>  * @Function : 一个简单的class
>  */
>
> public class test {
>     public test(){
>         MyApplication application=(MyApplication)MyApplication.getApplication();
>         Toast.makeText(application, "哈哈哈哈", Toast.LENGTH_SHORT).show();
>     }
> }
> ~~~
>
> ---
>
> ~~~java
> //测试
> public class MainActivity extends AppCompatActivity {
>     private static final String TAG = "MainActivity";
>     @Override
>     protected void onCreate(Bundle savedInstanceState) {
>         super.onCreate(savedInstanceState);
>         setContentView(R.layout.activity_main);
>
>         MyApplication a=(MyApplication)getApplication();
>
>       //打印出来，看看是不是同一个Application对象
>         Log.d(TAG, "onCreate: "+a);
>         test t=new test();
>     }
> }
> ~~~
>
> ---
>
> ~~~java
> //打印结果，Toast也显示了
> D/MainActivity: onCreate: com.example.ba.contextdemo.MyApplication@ebc9909
> D/ContentValues: test: com.example.ba.contextdemo.MyApplication@ebc9909
> ~~~
>
> ---
>
> + 有没有感觉似成相识？？在使用LitePal框架时是不是要求设置成它指定的application，前面也说了，操作数据库需要context，然后实现操作数据库的类里面不能像Activity这种。有直接的getApplication()方法,所以才这样写，很多都这样写，符不符合标准谁管？反正实现了功能。

## 官方获取Application的两种方法

> + getApplicationContext()
> + getApplication()
> + 获取的实例是同一个，为什么要两种？
> + 前者是使用任何一个Context对象就能调用该方法，进而获取到应用程序级别的Application实例
> + 后者只能在Service和Activity中调用
> + 区别就是作用域，其实没多大关系，喜欢哪个用哪个。。。而且一般都是传Context

## 最后总结

> + 知道Application是包含应用程序的信息，Application的生命周期是和应用程序一起，但是Activity和service就有自己的生命周期
> + 知道这些Context都能用来获取资源，而且是同样的资源
> + 不是任何时候都能用Application代替Activity作为context，比如启动Activity和AlterDialog
> + 不是任何时候都可以用Activity和service代替应用程序级别的context，在操作数据库时，要使用context，但是如果你传的不是Application的，那么当ACTIVITY或者service被销毁时就会出现内存泄露，这也就是LitePal要用Application的原因

