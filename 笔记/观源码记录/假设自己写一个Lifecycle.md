[toc]

# 假设自己写一个Lifecycle

**本文分为两个部分**

1. 通过一个简单的例子去理解Lifecycle的实现，具体的风格、代码细节、结构设计，性能，异常都不要太过纠结。
2. 从源码分析Lifecycle的实现

# 开整

有下面一种常见的场景：

+ 场景 1：Activity    ->   加载数据   ->  数据加载完成  ->  Activity中展示
+ 场景 2：Activity    ->   加载数据   ->  Activity关闭

场景2 相对于场景1 的不同之处：数据还在加载时Activity就关闭了。面对这种情况，我们不需要继续加载数据，这意味着加载数据的地方需要知道Activity的生命周期。

假设加载数据的类为DataLoader，我们给他赋予监听Activity生命周期的能力：

# 设计一个Listener

~~~java
interface MyLifecycleListener {
    fun onDeliverStart()
    fun onDeliverResume()
    fun onDeliverPause()
    fun onDeliverStop()
    fun onDeliverDestroy()
}
~~~

# DataLoader 实现Listener

~~~java
class DataLoader : MyLifecycleListener, CoroutineScope by MainScope() {
     /**
     * 加载数据，挂起非阻塞函数
     * 这里暂时不用LiveData,使用callback
     * @param callback 加载到的数据返回
     */
    fun load(callback:(String) -> Unit) {
        launch(Dispatchers.IO) {
            //模拟加载数据
            delay(8 * 1_000)
            launch(Dispatchers.Main) {
                callback.invoke("data")
              	//这里打日志，等下可以观察日志判断是否有继续加载数据
                Log.d("DataLoader", "load data success")
            }
        }
    }

    /**
     * 在Activity销毁时取消DataLoader的所有协程，进而取消加载数据的任务
     */
    override fun onDeliverDestroy() {
        cancel()
    }
  
  	/**
  	 * 简化篇幅，下面这些我们暂时不考虑
  	 */
    override fun onDeliverStart() {
    }

    override fun onDeliverResume() {
    }

    override fun onDeliverPause() {
    }

    override fun onDeliverStop() {
    }
}
~~~

下面你可以运行程序，然后等个8秒，就是上文说的场景 1 。这时候观察日志有没有输出：load data success。

运行程序，然后马上按下返回键关闭程序，就是上文说的场景 2 。这时候日志没有输出：load data success。代表在Activity销毁的同时，我们取消了加载数据。

# 与Activity生命周期关联

其实不太需要知道onCreate事件，所以我这里没写

~~~java
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val loader = DataLoader()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //记得把
        textView.text = "loading"
        //加载数据
        loader.load {
            textView.text = it
        }
    }
  	//各个生命周期回调中调用，这里可能会阻塞线程，但不是本文重点，所以不管
    override fun onStart() {
        super.onStart()
        loader.onStart()
    }

    override fun onResume() {
        super.onResume()
        loader.onDeliverResume()
    }

    override fun onPause() {
        super.onPause()
        loader.onDeliverPause()
    }

    override fun onStop() {
        super.onStop()
        loader.onDeliverStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        loader.onDeliverDestroy()
    }
}
~~~

---

MainActitvity的布局代码

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
~~~

# 小结

看起来很low对不对，只能给一个类实现监听，我想给10个类赋予监听Activity生命周期的能力怎么办？对于熟读设计模式的大佬很容易就想到了观察者模式。

# 使用观察者模式简单优化一下

定义一个LifecycleStation，简单的管理所有的观察者

ps:可以把生命周期的各个阶段定义为事件进行分发，我这里不将生命周期的各个阶段定义为具体事件。

~~~java
class LifecycleStation:MyLifecycleListener{
    private val observers = mutableListOf<MyLifecycleListener>()

    //添加观察者
    fun addObserver(observer: MyLifecycleListener){
        observers.add(observer)
    }

  	//移除观察者
    fun removeObserver(observer: MyLifecycleListener) {
        observers.remove(observer)
    }

  	//生命周期事件的处理
    override fun onDeliverDestroy() {
        observers.forEach{
            it.onDestroy()
        }
        observers.clear()
    }
  
	 override fun onDeliverStart() {
        observers.forEach{
            it.onStart()
        }
    }
  
    override fun onDeliverResume() {
        TODO("Not yet implemented")
    }

    override fun onDeliverPause() {
        TODO("Not yet implemented")
    }

    override fun onDeliverStop() {
        TODO("Not yet implemented")
    }
}
~~~

改一下Activity的代码:

~~~java
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val lifecycleStation = LifecycleStation()
    private val loader = DataLoader()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleStation.addObserver(loader)
        textView.text = "loading"
       	loader.load {
            textView.text = it
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleStation.onDeliverStart()
    }

    override fun onResume() {
        super.onResume()
        lifecycleStation.onDeliverResume()
    }

    override fun onPause() {
        super.onPause()
        lifecycleStation.onDeliverPause()
    }

    override fun onStop() {
        super.onStop()
        lifecycleStation.onDeliverStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleStation.onDeliverDestroy()
    }
}
~~~

---

# 小结

现在只需要实现MyLifecycleListener接口的类，在LifecycleStation进行订阅，就可以关联Activity的生命周期啦。

但是问题也有：我们需要在每一个Activity生命周期回调方法中使用lifecycleStation。是不是很烦？重复的写模板代码是每个开发最不愿意做的事情。

最容易想到的方法就是写一个BaseActivity。这个没有问题，很合理！

我们现在使用的Activity都是androidx库中的，继承关系如下：

~~~java
class MainActivity : AppCompatActivity()
class AppCompatActivity extends FragmentActivity //FragmentActivity 与navigation有关
public class FragmentActivity extends ComponentActivity //与LifecycleOwner和按键分发有关
~~~

对于谷歌的开发者来说，BaseActivity就是ComponentActivity。

# 使用BaseActivity

~~~java
abstract class BaseActivity :  AppCompatActivity(){
    val lifecycleStation = LifecycleStation()
    override fun onStart() {
        super.onStart()
        lifecycleStation.onDeliverStart()
    }

    override fun onResume() {
        super.onResume()
        lifecycleStation.onDeliverResume()
    }

    override fun onPause() {
        super.onPause()
        lifecycleStation.onDeliverPause()
    }

    override fun onStop() {
        super.onStop()
        lifecycleStation.onDeliverStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleStation.onDeliverDestroy()
    }
}
~~~

MainActivity的代码改一下

~~~java
class MainActivity : BaseActivity(), CoroutineScope by MainScope() {
    private val loader = DataLoader()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleStation.addObserver(loader)
        setContentView(R.layout.activity_main)
        textView.text = "loading"
        loader.load {
            textView.text = it
        }
    }
}
~~~

# 小结

MainActivity的代码一下子就清爽了，但是还是存在问题，假设我们的BaseActivity还有做其他的任务，那么从设计原则考虑，就破坏了单一职责。我们知道Fragment的生命周期是与Activity绑定的，能不能把LifecycleStation的功能放在一个Fragment里面？这个Fragment不需要界面，只需要监听Activity的生命周期。

# 使用Fragment监听Activity的生命周期

新加一个接口用来获取 LifecycleStation

~~~
interface LifecycleStationOwner{
    fun getLifecycleListener():MyLifecycleListener
}
~~~

在Fragment监听事件：

~~~java

class LifeCycleFragment : Fragment() {
    companion object {
        private const val LIFECYCLE_FRAGMENT_TAG = "com.example.lifecycle"
				//和Activity生命周期进行绑定
        @JvmStatic
        fun registerLifeCycleFragment(activity: AppCompatActivity) {
            activity.supportFragmentManager.run {
                //将lifeCycleFragment与activity的生命周期绑定,防止多次添加
                if (findFragmentByTag(LifeCycleFragment.LIFECYCLE_FRAGMENT_TAG) == null) {
                    beginTransaction()
                        .add(
                            LifeCycleFragment(),
                            LifeCycleFragment.LIFECYCLE_FRAGMENT_TAG
                        )
                        .commit()
                    executePendingTransactions()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as LifecycleStationOwner ).getLifecycleListener().onDeliverStart()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as LifecycleStationOwner ).getLifecycleListener().onDeliverResume()
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as LifecycleStationOwner ).getLifecycleListener().onDeliverPause()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as LifecycleStationOwner ).getLifecycleListener().onDeliverStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as LifecycleStationOwner ).getLifecycleListener().onDeliverDestroy()
    }
}
~~~

baseActivity的代码也改一下

~~~java
abstract class BaseActivity : AppCompatActivity(), LifecycleStationOwner {
    val lifecycleStation: LifecycleStation = LifecycleStation()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //注册一下
        LifeCycleFragment.registerLifeCycleFragment(this)
    }

    override fun getLifecycleListener(): MyLifecycleListener {
        return lifecycleStation
    }
}
~~~

碎片的生命周期就和Activiyt进行关联，而我们的DataLoader通过LifecycleStation也可以感知Activity的生命周期啦！

# 小结

假设我们把DataLoader换个名字：ViewModel。是不是就有点jetpack味道了？假设我们再把DataLoader的load方法改成使用livedata，是不是就更有jetpack的味道了？

lifecycle的实现并和上文中的差不多。只是我懒，直接把事件变成回调了。

他还有注解的用法，但是思想和上面一样：使用Fragment本来就可以感知Activity生命周期的特性，再结合观察者模式，实现的一套生命周期事件分发机制。

# 从源码分析

先欠着