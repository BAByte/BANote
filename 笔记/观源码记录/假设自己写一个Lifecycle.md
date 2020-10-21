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
        loader.onDeliverStart()
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

下面你可以运行程序，然后等个8秒,观察日志有输出：load data success。就是上文说的场景 1 。

运行程序，然后马上按下返回键关闭程序, 这时候日志没有输出：load data success。代表在Activity销毁的同时，我们取消了加载数据。就是上文说的场景 2 。

# 小结

看起来很low对不对，只能给一个类实现监听，我想给10个类赋予监听Activity生命周期的能力怎么办？

对于熟读设计模式的大佬很容易就想到了观察者模式。

# 使用观察者模式简单优化一下

定义一个LifecycleStation，简单的管理所有的观察者

ps:应该把生命周期的各个阶段定义为事件进行分发，我这里不将生命周期的各个阶段定义为具体事件。（我懒）

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
            it.onDeliverDestroy()
        }
        observers.clear()
    }
  
	 override fun onDeliverStart() {
        observers.forEach{
            it.onDeliverStart()
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

**最容易想到的方法就是写一个BaseActivity。**

这个想法没有问题，很合理！下面证明一下：我们现在使用的Activity都是androidx库中的，继承关系如下：

~~~java
class MainActivity : AppCompatActivity()
class AppCompatActivity extends FragmentActivity 
public class FragmentActivity extends ComponentActivity 
~~~

对于谷歌的开发者来说，BaseActivity就是ComponentActivity。lifecycle的实现就是在ComponentActivity里面

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

新加一个接口给Fragment获取 LifecycleStation

~~~
interface LifecycleStationOwner{
    fun getLifecycleListener():MyLifecycleListener
}
~~~

然后在Fragment关联activity生命周期，并使用LifecycleStation将事件分发给所有观察者：

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

碎片的生命周期和Activiyt已经进行了关联，DataLoader通过LifecycleStation也可以感知Activity的生命周期啦！

# 小结

假设我们再把DataLoader的load方法改成使用livedata，再把DataLoader换个名字：ViewModel。是不是就有jetpack的味道了？

Lifecycle的实现和上文中的差不多：使用Fragment可以感知Activity生命周期的特性，再结合观察者模式，实现一套生命周期事件分发机制。（只是我懒，直接把事件分发变成方法回调。）

Lifecycler还有注解等用法，

# 从源码分析

~~~java
package androidx.activity.ComponentActivity
  
//ComponentActivity实现了一个LifecycleOwner接口
public class ComponentActivity extends androidx.core.app.ComponentActivity implements
        LifecycleOwner... {
  						//观察者模式的事件分发中心
         			private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);
        }
~~~

上文说到androidX的继承关系，ComponentActivity就是BaseActivity。

看上方代码：ComponentActivity实现了一个LifecycleOwner接口:

~~~JAVA
/**
 * A class that has an Android lifecycle. These events can be used by custom components to
 * handle lifecycle changes without implementing any code inside the Activity or the Fragment.
 *
 * @see Lifecycle
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public interface LifecycleOwner {
    /**
     * Returns the Lifecycle of the provider.
     *
     * @return The lifecycle of the provider.
     */
    @NonNull
    Lifecycle getLifecycle();
}
~~~

LifecycleOwner和我们上面例子的LifecycleStationOwner功能差不多：获取观察者模式的订阅中心。我们上文的订阅中心是LifecycleStation。而源码的订阅中心是LifecycleRegistry。

# LifecycleRegistry

我们看看LifecycleRegistry的代码：

~~~JAVA
/**
 * An implementation of {@link Lifecycle} that can handle multiple observers.
 * <p>
 * It is used by Fragments and Support Library Activities. You can also directly use it if you have
 * a custom LifecycleOwner.
 */
public class LifecycleRegistry extends Lifecycle {
  ...
}
~~~

英文注释写的很清楚，他和上文写的LifecycleStation一样，被用在Fragment里面。我们先看Lifecycle定义了什么：

很长但是不复杂，和上文定义的MyLifecycleListener意义上差不多，区别是：Lifecycle把生命周期的阶段作为事件，不仅是Activity的生命周期，还有LifecycleOwner的生命周期（下文会说明原因）。

~~~java
public abstract class Lifecycle {
    /**
     * Adds a LifecycleObserver that will be notified when the LifecycleOwner changes
     * state.
     * <p>
     * The given observer will be brought to the current state of the LifecycleOwner.
     * For example, if the LifecycleOwner is in {@link State#STARTED} state, the given observer
     * will receive {@link Event#ON_CREATE}, {@link Event#ON_START} events.
     *
     * @param observer The observer to notify.
     */
    @MainThread
    public abstract void addObserver(@NonNull LifecycleObserver observer);

    /**
     * Removes the given observer from the observers list.
     * <p>
     * If this method is called while a state change is being dispatched,
     * <ul>
     * <li>If the given observer has not yet received that event, it will not receive it.
     * <li>If the given observer has more than 1 method that observes the currently dispatched
     * event and at least one of them received the event, all of them will receive the event and
     * the removal will happen afterwards.
     * </ul>
     *
     * @param observer The observer to be removed.
     */
    @MainThread
    public abstract void removeObserver(@NonNull LifecycleObserver observer);

    /**
     * Returns the current state of the Lifecycle.
     *
     * @return The current state of the Lifecycle.
     */
    @MainThread
    @NonNull
    public abstract State getCurrentState();

    @SuppressWarnings("WeakerAccess")
    public enum Event {
        /**
         * Constant for onCreate event of the {@link LifecycleOwner}.
         */
        ON_CREATE,
   ...
    }

    /**
     * Lifecycle states. You can consider the states as the nodes in a graph and
     * {@link Event}s as the edges between these nodes.
     */
    @SuppressWarnings("WeakerAccess")
    public enum State {
        /**
         * Destroyed state for a LifecycleOwner. After this event, this Lifecycle will not dispatch
         * any more events. For instance, for an {@link android.app.Activity}, this state is reached
         * <b>right before</b> Activity's {@link android.app.Activity#onDestroy() onDestroy} call.
         */
        DESTROYED,

   ...
     
    }
}

~~~

回到LifecycleRegistry

~~~java
public class LifecycleRegistry extends Lifecycle {
    /**
     * 支持在遍历时移除观察者，实现的放应该就是设置观察者的生命周期
     * Custom list that keeps observers and can handle removals / additions during traversal.
     * 
     * 根据观察者的生命周期去决定要不要把Activity的生命周期事件分发给观察者
     * Invariant: at any moment of time for observer1 & observer2:
     * if addition_order(observer1) < addition_order(observer2), then
     * state(observer1) >= state(observer2),
     */
    private FastSafeIterableMap<LifecycleObserver, ObserverWithState> mObserverMap =
            new FastSafeIterableMap<>();
		@Override
    public void addObserver(@NonNull LifecycleObserver observer) {
        State initialState = mState == DESTROYED ? DESTROYED : INITIALIZED;
        ObserverWithState statefulObserver = new ObserverWithState(observer, initialState);
      	//存入观察者对象已经观察者对象的状态，原因看上方注释
        ObserverWithState previous = mObserverMap.putIfAbsent(observer, statefulObserver);
    }
}
~~~

上文说的：根据观察者的生命周期去决定要不要把Activity的生命周期事件分发给观察者。是我的猜想，正确性待考证。本文重点是了解lifecycle的设计结构，而不是纠结在这些具体细节。

接下来我们看看Fragment是怎么使用LifecycleRegistry，并将事件分发给观察者们的。

在此之前，我们要先看Fragment和Activity的绑定的代码：

~~~java
public class ComponentActivity extends androidx.core.app.ComponentActivity implements
        LifecycleOwner { 
  ...
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       //重点是这里
        ReportFragment.injectIfNeededIn(this);
    }
~~~

所以是由ReportFragment负责关联Activity的生命周期，这里和我们上文的例子一样：在BaseActivity调用Fragment的静态方法把“自己”添加到Activity中。下面看看injectIfNeededIn方法：

~~~java
  public static void injectIfNeededIn(Activity activity) {
        if (Build.VERSION.SDK_INT >= 29) {
            // On API 29+, we can register for the correct Lifecycle callbacks directly
            activity.registerActivityLifecycleCallbacks(
                    new LifecycleCallbacks());
        }
        // Prior to API 29 and to maintain compatibility with older versions of
        // ProcessLifecycleOwner (which may not be updated when lifecycle-runtime is updated and
        // need to support activities that don't extend from FragmentActivity from support lib),
        // use a framework fragment to get the correct timing of Lifecycle events
        android.app.FragmentManager manager = activity.getFragmentManager();
        if (manager.findFragmentByTag(REPORT_FRAGMENT_TAG) == null) {
            manager.beginTransaction().add(new ReportFragment(), REPORT_FRAGMENT_TAG).commit();
            // Hopefully, we are the first to make a transaction.
            manager.executePendingTransactions();
        }
    }
~~~

和我们上文写的例子一样吧！！把自己添加到Activity中，再看看Fragment碎片的回调方法：

~~~java
public class ReportFragment extends Fragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
     		//这是分发给谁，我就不细纠了
        dispatchCreate(mProcessListener);
      	//分发给观察者
        dispatch(Lifecycle.Event.ON_CREATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        dispatchStart(mProcessListener);
        dispatch(Lifecycle.Event.ON_START);
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatchResume(mProcessListener);
        dispatch(Lifecycle.Event.ON_RESUME);
    }
  
    
  static void dispatch(@NonNull Activity activity, @NonNull Lifecycle.Event event) {
    		//有两种的原因为了适配android.support.v7.app.AppCompatActivity
        if (activity instanceof LifecycleRegistryOwner) {
          	//从Activity中获取到LifecycleRegistry，然后进行事件分发
            ((LifecycleRegistryOwner) activity).getLifecycle().handleLifecycleEvent(event);
            return;
        }

     		//这里的acticity是androidx的Activity，从Activity中获取到LifecycleRegistry，然后进行事件分发
        if (activity instanceof LifecycleOwner) {
            Lifecycle lifecycle = ((LifecycleOwner) activity).getLifecycle();
            if (lifecycle instanceof LifecycleRegistry) {
                ((LifecycleRegistry) lifecycle).handleLifecycleEvent(event);
            }
        }
    }
  
    private void dispatchCreate(ActivityInitializationListener listener) {
        if (listener != null) {
            listener.onCreate();
        }
    }

    private void dispatchStart(ActivityInitializationListener listener) {
        if (listener != null) {
            listener.onStart();
        }
    }

    private void dispatchResume(ActivityInitializationListener listener) {
        if (listener != null) {
            listener.onResume();
        }
    }
}
~~~

# 总结

实现的总体思路：

利用Fragment能关联Activity生命周期的特性，在ComponentActivity中将ReportFragment添加到“自己”里面，ComponentActivity有LifecycleRegistry（观察者订阅中心），所有的观察者只需要在ComponentActivity的LifecycleRegistry订阅生命周期事件。