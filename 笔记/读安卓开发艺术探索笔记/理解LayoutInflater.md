[toc]
# 理解LayoutInflater
## 先说两句
这个系列的笔记是在我读《Android源码设计模式》这本书时，发现里面的源码分析起来还是有点难，尤其是只看着书，会觉得很乱，为了加强记忆，还是写写笔记吧
## 本文里面的重点
+ 安卓中的一个单例的应用
+ 安卓中代理模式
## 开始
+ LyaoutInflater 类是一个抽象的类，不信吗？？看看咯
~~~java
public abstract class LayoutInflater {
    //省略代码
}
~~~

里面还有具体的代码，现在当然不用看先。找到具体的实现类才是最重要的，下面这个静态方法是不是很熟悉？就是从这里获取的LayoutInflater实例

~~~java
   /**
     * Obtains the LayoutInflater from the given context.
     */
    public static LayoutInflater from(Context context) {
    //是个服务，那就要从这个context里面的具体方法分析了
        LayoutInflater LayoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (LayoutInflater == null) {
            throw new AssertionError("LayoutInflater not found.");
        }
        return LayoutInflater;
    }
~~~
Context是一个抽象类，我们知道的，所以又要找到这个Context的具体实现，是不是很烦。最怕就是翻源码的时候遇到接口。但这里不怕。上面我们一般传的是一个Activity作为Context，那就看看Activity里面的getSystemService方法不就好了，天真了吧，你会发现你一直翻啊翻啊，真的是翻啊翻啊，翻到Activity的父类中有下面的类
~~~java
    public class ContextWrapper extends Context {
        Context mBase;
        //代码省略
    ｝
~~~
这个类里面终于找到了getSystemService方法，看看这个方法
~~~java
  @Override
    public Object getSystemService(String name) {
        return mBase.getSystemService(name);
    }
~~~
内心一句 WC，mBases是什么鬼？往上翻
~~~java
public class ContextWrapper extends Context {
  Context mBase;

    public ContextWrapper(Context base) {
        mBase = base;
    }
}
~~~
诶？怎么又一个Context？Activity不是一个Context吗？怎么这里又放了一个Context，而且看看ContextWrapper类的所有代码你会发现，这是一个代理类，而且具体的实现不是Activity，而是mBase这个类，这意味着Activity里面还存放着一个Context，什么时候设置的？使用Activity的时候我们并，并没有设置，另一个Context，说明是在Activity创建时系统设置的 ，哇，Activity怎么创建的？
## ActivityThread
有没有这样的一个疑问，都说一个程序的入口是main函数，但在Activity中完全是没有Main函数的，只要找到了Main函数，就能知道Activity是什么时候创建的，怎么创建的，那main藏在了哪里？就在ActivityThread这个类里面，看看源码先
~~~java
//main方法，在SDK
中式找不到这个ActivityTHread类的，需要下载安卓的源码才有
public static final void main(String[] args) {
        SamplingProfilerIntegration.start();
       ……
        Looper.prepareMainLooper();
        if (sMainThreadHandler == null) {
            sMainThreadHandler = new Handler();
        }
        ActivityThread thread = new ActivityThread();
        thread.attach(false);
       ……
        Looper.loop();
       ……
        thread.detach();
        ……
        Slog.i(TAG, "Main thread of " + name + " is now exiting");
    }
~~~
就在里面进行一些操作，但最主要的还是实例化了一个ActivityThread类，然后调用了 thread.attach(false);，然后在这个方法里ActivityThread会和ActivityManagerService通信，然后调用一个方法,在这个方法里面会创建一个Context，然后和Activity绑定，然后就开始调用Activity的OnCreate方法了。所以就知道了Activity里面其实还有一个Context，这个Context才是真正的实现类，为什么要这样做？Activity本身就是一个Context，为什么还有绑定一个外部的Context，那就想想，什么情况下是实现同一个接口，然后一个实现类持有另一个实现类的实例？代理模式！！！既然知道了这个Context是怎么来了，那就回到LayoutInflater的分析上
## 看看ContextImpl类是怎样获取LayoutInfalte的
呵呵，sdk中还是不能看这个类的具体实现，书上有代码，我就简单说说咯，在ContextImpl有一块静态代码块，在该块中将系统所有必要的服务一次性初始化，然后放在一个HashMap中，说到这里不知道你有没有反应股过来，没有的话看看下面的代码
~~~java
LayoutInflater layoutInflater= (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
~~~
以前在获取一些系统服务的时候，是可以直接通过字符串获取的，这不就说明了服务是存在了一个HashMap表中吗？为什么要放在HashMap，为什么放在静态代码块中？简单啊，单例模式！！！这是一种通过容器的存储实现的单例模式，又是一个小插曲，安卓系统中这样实现单例的目的又是什么？所有服务都放在了一个集合中，方便管理呗。知道了服务在安卓中是怎样存储后我们回到LayoutInflater这个服务上，LayoutInflater是一个抽象类，前面说过，他的具体实现是PhoneLayoutInflater(没有源码我也给不了你看，反正你只要了解到这个就是具体实现就好了)
## 拿到LayoutInflater后怎么用
先看看具体实习类PhoneLayoutInflater里面有什么重要的代码
（源码给不了口述吧），重点字段：一段包名(类似：android.widget.)。该类中重点方法就一个：onCreateView()：根据参数传入String类型的的字符串(说白了就是View的名字)，和上面对应的字段拼接后就创建Viwe这里需要注意啦！！这就是为什么在布局文件中自定义VIew要完整包名的原因了。只有完整路径就可以生成对象，肯定是反射了。这里并没有解析xml数据的方法，就只是用来根据名字生成View对象。那解析XML的部分在哪里？那就要看看哪里传入xml咯！
## 解析XML布局
~~~java
 LayoutInflater.from(this).inflate(R.layout.activity_main,null);
 ~~~
 看到了吗？inflate()里面就传入了xml布局的id，下面的代码有点多，我就不翻了，其实就是解析出来，然后用PhoneLayoutInflater的方法去生成对应的View，绘制成View了但是还是不能显示在屏幕上，以及如何显示view到界面上，显示到屏幕上的工作不是LayoutInflater该做的事。他是Window的事情，具体看看window的笔记