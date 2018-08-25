[toc]
# Window和WindowManager
## 先说两句
在学习安卓的时候我们只接触到了Activity这种界面的概念，那window是什么？Activity，Dialog，Toast，都是附在Window上的。那Window和View又什么联系？
## 如何添加VWindow
一般来说我们都是直接在Xml写View，不然就是在java代码中动态添加，一般动态添加用Inflat后，都会指定放在那个父布局上，但是我们并没有接触到Window这个东西，其实底层就是Window实现的。我们看看怎么添加一个Window
~~~java
public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);

        Button button=new Button(this);
        button.setText("按钮");

        //Button在父布局的参数，就是说xml对应的属性名字layout_xxx
        WindowManager.LayoutParams params=new WindowManager.LayoutParams();
        params.width= WindowManager.LayoutParams.WRAP_CONTENT;
        params.height= WindowManager.LayoutParams.WRAP_CONTENT;
        
        //这个对应的就是layout_gravity
        params.gravity=Gravity.CENTER;
        
        //这个后面会讲
        params.flags= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.format = PixelFormat.RGBA_8888;//设置窗口支持透明度的调整
        
        //这个是在gravity的基础上偏移哦
        params.x=100;//这里不是dp
        params.y=200;

        //获取Window服务
        WindowManager windowManager=(WindowManager)getSystemService(WINDOW_SERVICE);
        
        //添加到界面上
        windowManager.addView(button,params);
    }
}
~~~

看看params.flags有什么用，一般我们用到WindowMananger都是在悬浮窗，这个属性其实大部分是针对那些悬浮的view的

+ FLAG_NOT_FOCUSABLE
这个代表这个View不需要焦点，不需要输入事件
+ FLAG_NOT_TOUCH_MODAL
代表当前Window的区域有点击事件就处理，其他区域传到底下的View
+ FLAG_SHOW_WHEN_LOCKED
代表这个View可以显示在锁屏界面上


当然还有一个Type类型
~~~java
 params.type= WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
~~~

type就三种大类型：
+ 应用级别的Window
+ 子Window
不可单独存在。必须依赖其他Window
+ 系统的Window
需要声明权限，不然会报错，Toast和系统状态栏都是系统的Window

既然有这三种类型，那他们可能是有级别的，其实叫做层级，层级大的可以显示在层级小的上面。就是有一个z-odered属性，应用级别的：1-99；子window：1000-1999；系统级别：2000-2999


### 小结
是不是很奇怪？为什么说加的是Window，而这里加的确实View？Window其实就是一个抽象概念，是系统的一个服务在管理这些window，这些window对应着一个 View 和一个ViewRootImpl，所以说上面添加Window的过程，却变成了添加view的过程，因为Window是个抽象的概念，但是又不能说Window就是View，因为还有一个ViewRootImpl，这个ViewRootImpl是干嘛用的？可能我们需要看看WindowMananger的工作流程了
## WindowManager我们提供的接口
这个服务就是上面说的那个管理Window的服务，叫做WindowManager，你看看下面的代码
~~~java
public interface WindowManager extends ViewManager {
    ...
}
~~~

他继承了ViewManager，上面说的没错吧！Window是一个抽象概念，其实管理这些window就是在管理View，所以叫做ViewManager。我们言归正传，看看里面的方法
~~~java
public interface ViewManager
{
    public void addView(View view, ViewGroup.LayoutParams params);
    public void updateViewLayout(View view, ViewGroup.LayoutParams params);
    public void removeView(View view);
}
~~~
名字就很好理解，添加，删除，更新，猜你都猜的到，这几个就是主要的方法。我们一个一个分析，还是没有看到ViewRootImpl
### Window的添加过程
你有没有发现，windowManager是一个接口？上面的代码有，看看具体的实现类，自己找找源码，很容易的，10秒的事情。
~~~java
public final class WindowManagerImpl implements WindowManager {
    private final WindowManagerGlobal mGlobal = WindowManagerGlobal.getInstance();
    ...
    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }
}
~~~

又藏了一层：mGlobal，继续进去看看
WindowManagerGlobal 的 addView 方法主要分为如下几步

+ 检查参数是否合法，如果是子Window 那么还需要调整一些布局参数；

+ 创建 ViewRootImpl 
~~~java
ViewRootImpl root = new ViewRootImpl(view.getContext(), display);
root.setView(view, wparams, panelParentView);
~~~

+ 将View，view的参数，viewRootImpl放到集合中管理
~~~java
private final ArrayList<View> mView = new ArrayList<View>();//储存所有Window所对应的View
//储存所有 Window 所对应的 ViewRootImpl ；
private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();
//储存所有 Window 所对应的布局参数；
private final ArrayList<WindowManager.LayoutParams> mParams = new ArrayList<WindowManager.LayoutParams>();
//储存了那些正在被删除的View的对象，或者说是那些已经调用removeView方法但是删除操作还未完成的Window
private final ArraySet<View> mDyingViews = new ArraySet<View>();
~~~

我们看看ViewRootImpl，他实例化后紧接着就掉了setView（）方法，我们看看这个方法里面有什么，因为你在后面找不到显示View的方法，所以可能是这个方法里面绘制了view，你看setView的参数，十有八九了。
~~~java
//调了一个方法
public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
    ...
    requestLayout();
}



 @Override
    public void requestLayout() {
        if (!mHandlingLayoutInLayoutRequest) {
        //这方法是用来判断当前的操作是不是和该实例创建时的线程一致，不一致就报错，所以不是说子线程不能刷新ui，要看ViewRootImpl在哪里创建
            checkThread();
            
            mLayoutRequested = true;
            scheduleTraversals();
        }
    }
    
    
    //看看 scheduleTraversals();
       void scheduleTraversals() {
       ...
        mChoreographer.postCallback(
                    Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
         
        }
    }
    
//看看    这个方法第二个参数mTraversalRunnable
 final class TraversalRunnable implements Runnable {
        @Override
        public void run() {
            doTraversal();
        }
    }
    
//继续,看到了熟悉的方法
    void doTraversal() {
        performTraversals();
    }

~~~

这个方法是绘制View的入口，也可以说是在这里进行VIew的测量，定位，绘制，但是注意！这个时候还是在处理View，还是没有涉及到Window，屏幕上显示的都是Window。

看看下面的代码
~~~java
private void performTraversals() {
    performConfigurationChange(mPendingMergedConfiguration, !mFirst,
                            INVALID_DISPLAY /* same display */);
   
    performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
    
    performLayout(lp, mWidth, mHeight);
    
    performDraw();
    
    ...
}
~~~

在这个performTraversals方法中其实是对View的绘制，这个流程我们就很亲切了，这就意味着ViewRootImpl是管理着View的绘制过程，那自然View的刷新删除都离不开他，这就像mvvm里面的，View和ViewModel，ViewRootImpl应该就是对应这个ViewModel，现在view和ViewRootImpl的关系也搞明白了。

但是这个时候还是没有碰到Window，也就是说Window还是没有显示在屏幕上。那什么时候显示在屏幕上？我们回到setView方法。

~~~java
public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
    ...
           requestLayout();
           ...
            res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
                            getHostVisibility(), mDisplay.getDisplayId(),
                            mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
                            mAttachInfo.mOutsets, mInputChannel);
    ...
}
~~~

这个mWindowSession.addToDisplay（）才是真正意义上的将window添加到屏幕上，在这个方法的参数里面，我们看到了mWindow这个东西，终于看到了Window！咦？他怎么自己跑出来了？window居然在ViewRootImpl里面，那他会不会就是我们通过setView传进来的View？我们看看这个mWindow是怎么来的！
~~~java
 mWindow = new W(this);
~~~
卧槽，直接实例化！！现在就很清晰了，window，view，viewRootImpl都是相互独立的实例，所以说Window不是view，那么三者的关系是什么？上面已经了解了View和ViewRootImpl的关系， 就差这个Window和他们的关系了！其实你也可以猜到，Window应该就是一个幌子，view依附在Window上才能被显示出来，ViewRootImpl就负责管理这个Window里面View的绘制过程。那我们看看Window怎么样和ViewRootImpl建立联系，事实上你在ViewRootImpl里面是找不到处理Window的代码，他都是当作参数传进去的，那我们看看window这个类
~~~java
static class W extends IWindow.Stub {
    ...
     private final WeakReference<ViewRootImpl> mViewAncestor;
        private final IWindowSession mWindowSession;

        W(ViewRootImpl viewAncestor) {
            mViewAncestor = new WeakReference<ViewRootImpl>(viewAncestor);
            mWindowSession = viewAncestor.mWindowSession;
        }
    }
    ...
    //就是通过ViewRootImpl的方法来改变view的可视状态
    
       @Override
        public void dispatchAppVisibility(boolean visible) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchAppVisibility(visible);
            }
        }
    ...
}
~~~
其实就是一个Binder，难怪不在这里操作view而是将viewRootImpl当作window构造函数的参数传进来。这个Window是给系统的进程用的，ViewRootImpl中定义了View的各种刷新描绘方法，那只需要在这个Binder复写定义好的方法，系统拿到这个Binder的时候在合适的地方调用就好，那Window和VIewRootVImpl的联系就是在这里建立的，现在就很清楚了系统进程拿到window后其实是通过ViewRootImpl操作View。那系统进程如何拿到Window？既然都是Window是Binder了，那肯定是ipc，那我们就要看看mWindowSession.addToDisplay（）的这个mWindowSession是怎样来的，我们看看下面的代码
~~~java
    
    mWindowSession = WindowManagerGlobal.getWindowSession();
     
     
    public static IWindowSession getWindowSession() {
        synchronized (WindowManagerGlobal.class) {
            if (sWindowSession == null) {
                try {
                    InputMethodManager imm = InputMethodManager.getInstance();
                    IWindowManager windowManager = getWindowManagerService();
                    sWindowSession = windowManager.openSession(
                            new IWindowSessionCallback.Stub() {
                                @Override
                                public void onAnimatorScaleChanged(float scale) {
                                    ValueAnimator.setDurationScale(scale);
                                }
                            },
                            imm.getClient(), imm.getInputContext());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return sWindowSession;
        }
    }
~~~
那就要又要看看getWindowManagerService（）
~~~java
    public static IWindowManager getWindowManagerService() {
        synchronized (WindowManagerGlobal.class) {
            if (sWindowManagerService == null) {
                sWindowManagerService = IWindowManager.Stub.asInterface(
                        ServiceManager.getService("window"));
                try {
                    if (sWindowManagerService != null) {
                        ValueAnimator.setDurationScale(
                                sWindowManagerService.getCurrentAnimatorScale());
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return sWindowManagerService;
        }
    }
~~~
 看到这个ServiceManager.getService("window"));其实就一目了然了，系统的ServiceManager控制所有Service的IPC进程通信，通过这个东西就可以拿到sWindowManagerService进而拿到WindowSession，为什么不直接通过一次进程通信拿到WindowSession而非要通过sWindowManagerService？假设你有很多类型的进程通信，如果都写aidl是不是要很多AIDL类？所以直接用一个Manager来管理，你要什么Binder，你告诉Manager，Manager给你，这里类似于工厂模式。但是，通过这种方法进行IPC通信，说明sWindowManagerService他会是一个单例，你后面拿到的sWindowManagerService，其实是同一个，但是每个软件都不一样！都拿到一个sWindowManagerService，这类似于什么？你是总统，每个人一有问题就找到你本人！你觉得这样合理吗？所以！拿到这个sWindowManagerService后，我们不就开始了openSession嘛！这样一来相当于我们的app和WMS开启了一个会话，相当于打电话。我们的软件后面都用就通过这样的方式和wms进行交互。哇！这样一来就很容易理解为什么要用Session了。
 
 那Window就添加完啦！还要再具体的话，其实就是系统运行库层的东西了，这里面要下载安卓源码才能看，所以就到这里，其实代码走到这，界面上还是没有显示出我们的window的，还没到那一步，就不继续深入了！我们已经理解了window和VIewRootImpl和view的关系。还有为什么Window是抽象的，他就是用来IPC传输给系统回调用的，当然要是抽象的啦！我们还分析了下具体的和wms通信的过程，还猜想了为什么要用Session，知识点还是很多的，这里我们走到了安卓的那里？我们已经跨过了应用层，走到了FrameWork层（），还摸到了一点点Native层
 
 
 层级的图片我们很常见啦，Native层就是系统运行库层。倒数第二层
 ![image](http://img2.tgbusdata.cn/v2/thumb/jpg/MUU5NCw1ODAsMTAwLDQsMywxLC0xLDAscms1MCw2MS4xNTIuMjQyLjEx/u/android.tgbus.com/help/UploadFiles_4576/201108/2011081710043460.jpg)
 
### Window的删除
既然添加是WindowManager，那么删除当然也是！我们看看代码
~~~java
    @Override
    public void removeView(View view) {
        mGlobal.removeView(view, false);
    }
~~~

按例分析下去
~~~java
    public void removeView(View view, boolean immediate) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        }

        synchronized (mLock) {
        //第一步当然是移除View，找出View对应的索引
            int index = findViewLocked(view, true);
            
          
            //我们看看这方法
            removeViewLocked(index, immediate);
            if (curView == view) {
                return;
            }

            throw new IllegalStateException("Calling with view " + view
                    + " but the ViewAncestor is attached to " + curView);
        }
    }
~~~

~~~java
    private void removeViewLocked(int index, boolean immediate) {
        //取出对应的ViewRootImpl
        ViewRootImpl root = mRoots.get(index);
        View view = root.getView();

        ...
        
        //调用了一个方法，是不是删除View的方法？我们看看
        boolean deferred = root.die(immediate);
        
        
        //这里是将viewRootImpl加入到待删除的集合中
        if (view != null) {
            view.assignParent(null);
            if (deferred) {
                mDyingViews.add(view);
            }
        }
    }
~~~

~~~java
    /**
     * @param immediate True, do now if not in traversal. False, put on queue and do later.
     * @return True, request has been queued. False, request has been completed.
     */
    boolean die(boolean immediate) {
        // Make sure we do execute immediately if we are in the middle of a traversal or the damage
        // done by dispatchDetachedFromWindow will cause havoc on return.
        //如果是同步删除，就直接删除，这个一般不常有
        if (immediate && !mIsInTraversal) {
            doDie();
            return false;
        }

      ...
        
        //既然不是同步就是异步了，就发送消息去通知某个东西，让他去删除
        mHandler.sendEmptyMessage(MSG_DIE);
        return true;
    }
~~~


看看handle类做了什么
~~~java
   case MSG_DIE:
                doDie();
                break;
~~~
还是执行了doDie（）方法，那是不是很奇怪？为什么要执行同样的方法？下面这个例子就很好理解：
>    我们在做UI相关的代码时有时候会碰到WindowLeak，也就是所谓的窗体泄露，泄露的原因是因为androidUI操作在主线程中操作，但是我们会需要在一些线程或者异步任务中操作UI界面元素的需求，那么这个时候可能会出现类似问题。我在做浮动窗口的时候碰到了这个问题，浮动窗口需要用到WindowManager,windowManger又是一个activity的一个变量，它依存于Activity,当横竖屏切换或者activity销毁的时候这个变量会销毁。销毁的时候导致windowmanager通过AddView()方法添加的View没有依存，导致窗体泄露。那么问题来了，为什么这里会泄露了？

> 2.解决方法：我在onDestroy()里面调用了removeView方法，想要避免窗体泄露，但是这个方法并不管用，后来换成removeViewImmediate()就解决了这个问题，原因就是两个方法设计到线程同步问题，removeViewImmediate()是通知View立刻调用View.onDetachWindow()，这说明这个方法是通过一个监听或者观察者来实现的，因为线程的同步跟异步问题导致activity销毁了，但view还没有被remove完，于是就产生了所谓的窗体泄露。说到这里，我想大家也能明白这两个方法的区别了。

那我们看看doDie
~~~java

void doDie() {
        ...
        
        //这个方法就是真正删除View的方法，他的步骤：
       
        /*
         垃圾回收 的工作，比如清楚数据和消息，移除回调；
         
        这里最重要，通过 Session 的 remove 方法删除 Window :mWindowSession.remove(mWindow)，这同样是一个 IPC 过程，其实跟添加过程一样，最终当然也是会调用 WindowManagerService 的 removeWindow 方法；将删除 的操作交给 WMS 去做；

        调用 View 的 dispatchDeachedFromWindow 方法，在内部会调用 View 的 onDetachedFromWindow() 以及 onDetachedFromWindowInternal() 。对于 onDetachedFromWindow() 就很熟悉了，这个日常开发中有时会用到，当View 从Window 中移除时，这个方法就会被调用，可以在这个方法中做一些资源回收的工作，比如终止动画，停止线程等；
    
        调用 WindowManagerGlobal 的 doRemoveView 方法刷新数据，包括 mRoots，mParams ,以及 mDyingViews ，就是之前说过的这个类中的3个列表，需要将当前 Window 所关联的这三类对象从列表中删除；直接查看doRemoveView源码你会发现其实就是将当前 View 从 mViews 中移出，将当前 ViewRootImpl 从当前 mRoots 移出，将当前 LayoutParams 从 mParams 移出而已了，因为你在创建的时候添加进去了嘛，删除的时候就该移出出去啊！
        */
       
        dispatchDetachedFromWindow();
       
       
       //这个方法是去刷新数据，将三个集合的数据处理掉
        WindowManagerGlobal.getInstance().doRemoveView(this);
    }
~~~

#### 小结
删除过程就简单了，其实主要就是那个窗体溢出的地方，其他没什么好说的。
### Window更新
照例写一下而已

更新的操作其实也和之前的一样，只是一些改变而已；我们还是从 WindowManagerGlobal 的 updateViewLayout 方法看，updateViewLayout 做的事情就比较简单了，
~~~java

public void updateViewLayout(View view,ViewGroup.LayoutParams params){
	//忽略一些源码，我们只看重要的部分；
    final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams)params;
    view.setLayoutParams(wparams);//首先先更新 View 的 LayoutParams并替换掉老的 LayoutParams
    synchronized(mLock){
        int index = findViewLocked(view,true);
        ViewRootImpl root = mRoots.get(index);
        mParams.remove(index);//更新WindowManagerGlobal 中的index位置的View的参数列表
        mParams.add(index,wParams);
        root.setLayoutParams(wparams,false);//这是最重要的一步，就是通过 view 所对应的ViewRootImpl对象来更新视图，通过 setLayoutParams 来实现，其中会通过scheduleTraversals 方法来对 View 重新布局，想想都知道包括测量，布局，重绘这3个过程；除了 View 本身的重绘以外， ViewRootImpl 还会通过 WindowSession 来更新 Window 的视图，这个过程最终是由 WindowManagerService 的 relayoutWindow()来实现，同样，它是一个 IPC 过程；
    }
}
~~~
在 ViewRootImpl 的 setLayoutParams 方法里面你会看到执行了 scheduleTraversals 方法，这个方法就会开启我们从DecorView 的视图重绘工作，接着呢，还需要更新我们的 Window 视图呀，具体是通过 scheduleTraversals 调用performTraversals 方法之后，在 performTraversals 方法里面执行的，在 performTraversals方法里面执行了ViewRootImpl 的 relayoutWindow 方法，而在 relayoutWindow 里面就会执行 Session 的 relayout 方法了，很可爱，我们又见到了 Session 啦，下一步不用想肯定就是执行的 WindowManagerService 的 relayoutWindow 方法来更新Window ！

## Window的创建过程
我是有点疑问的，为什么还讲Window的创建过程？上面不是就有一个Window的创建过程？但是我们一开头就说了，Window管理视图，我们上面的例子。是添加悬浮窗的Window，但是Window其实是有不同类型的，比如开头说的Activity，Dialog，Toast，这些视图的Window又是怎么创建的？


### Activity的Window
这里涉及的安卓的源码，你是定位不到的，只能看陈南泰写的了！

既然要找到Activity的Window。那就要找Activity的创建过程，这篇笔记不分析Activiy的创建过程，这里你只需要找到Activity的Window，我们看看呗

~~~java
//在 Acitvity 的 attach 方法里，系统会创建 Activity 所属的 Window 对象，并为其设置回调接口，由于Activity 实现了 Window 的 Callback 接口，因此当 Window 接受到外界的状态改变时就会回调 Activity 的方法。这里我给你列举几个我们比较熟悉的 接口方法，如onAttachedToWindow ，onDetachedFromWindow， dispatchTouchEvent等； 
mWindow = PolicyManager.makeNewWindow(this); //Window 对象的创建是通过 PolicyManager 的 makeNewWindow 方法实现的；可以看出，Activity的 Window 是由 PolicyManager 的一个工厂方法来创建的，
mWindow.setCallback(this);
mWindow.setOnWindowDismissedCallback(this);
if(info.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED){
    mWindow.setSoftInputMode(info.softInputMode);
}
if(info.uiOptions != 0){
    mWindow.setUiOptions(info.uiOptions);
}
//PolicyManager 实现了 Ipolicy 的接口；
public interfaceIPolicy IPolicy{
    public Window makeNewWindow(COntext context);
    public LayoutInflater makeNewLayoutInflater(Context context);
    public WindowManagerPolicy makeNewWindowManager();
    public FallbackEventHandler makeNewFallbackEventHandler(Context context);
}
//但是在实际调用中，PolicyManager 的真正实现是 Policy 类，Policy 类中的 makeNewWindow
public  Window makeNewWindow（Context context）{
    return new PhoneWindow(context);//这里就验证了 Window 的真正实现是 PhoneWindow 
}
//关于策略类 PolicyManager 是如何关联到 Policy 上面的，这个无法从源码中得出，书里猜测是由编译环节动态控制。
//那么 Window 创建好了之后，Activity 的 View 是如何附属到 Window 上的，由于 Activity 的视图由 setContentView 方法提供，我们看看
public void setContentView(int layoutResID){
    getWindow().setContentView(layoutResId);//这里可以看到 Activity 将具体实现交给了 Window 处理，而 Window 的具体实现是 PhoneWindow，所以我们需要了解 PhoneWindow 怎么做的
    initWindowDecotActionBar();
}
~~~

PhoneWindow 的 setContentView 方法 分为以下几个步骤


~~~java
//1. 如果还没有 DecorView，那么就创建它；我们很早就知道 DecorView 是一个 FrameLayout ，DecorView 是Activity 的顶层 Veiw，一般来说 它的内部包含标题栏和内部栏，之所以说一般，是因为会随着主题的变换而变换；不管如何变换，内容栏是一直存在的，并且内容栏具体的id 就是 “content”，完整id是“anndroid.R.id.content”有没有觉得很熟悉，我们平时在Activity 中使用的 setCOntentView()，就是把 View 设置到内容栏里面;DecorView 的创建过程由 installDecor 方法来完成，在方法内部会通过 generateDecor 方法来直接创建 DecorView ，这个时候 DecorView 还只是一个空白的 FrameLayout；
protected DecorView generateDecor(){
    return new DecorView(getContext(),-1);
}
//为了初始化 DecorView 的结构，PhoneWindow 还需要通过 generateLayout 方法来加载具体的布局文件到 DecorView 中；
View in = mLayoutInflater.inflate(layoutResource,null);//这里只是把 DecorView 的大框架给它，即 标题栏 和 内容栏，但是里面是空白的；用户指定的 View 还没有加载进去；
decor.addView(in,new VeiwGroup.LayoutParams(MATCH_PARENT,MATCH_PARENT));
mContentRoot = （ViewGroup）in；
ViewGroup contentParent = (ViewGroup)findViewById(ID_ANDROID_CONTENT);
//2. 将 View 添加到 DecorView 的 mContentParent 中
mLayoutInflater.inflate(layoutResID,mContentParent);
//到这里我们就可以理解在 Activity 中设置布局的方法为什么不叫 setView ，而是叫做 setContentView ，因为 Activity 的布局文件只是被添加到 DecorView 的 mContentParent 中，因此叫 setContentView 更合适；
//3. 回调 Activity 的 onContentChanged 方法通知 Activity 视图已经发生改变，通知后 Activity 才会去做接下来的事情，例如生命周期的继续等等；由于 Activity 实现了 Window 的 Callback 接口，所以 直接调用即可，Activity 的 onContentChanged 方法是个空实现，我们去 子Activity 中处理这个回调，
final Callback cb = getCallback();
if(cb != null && !isDestroyed){
    cb.onContentChanged();
}
~~~

在上面的代码中你就可以看出PhoneWindow是在组装DecorView，那我们传给系统的WMS时是不是就是传这个Window？其实不然，Activity 的生命周期就是这样设定的，在 onCreate 时Window 并不被外界观察到；在 ActivityThread 的 handleResumeActivity 方法中，首先会调用 Activity 的 onResume 方法，接着会调用 Activity 的 makeVisible()，这个方法看看就懂

~~~java
void makeVisible(){
    if(!mWindowAdded){
        ViewManager vm = getWindowManager();//拿到 WindowManager
        wm.addView(mDecor,getWindow().getAttributes());//这里就用 WindowManager 真正添加 Window 
        mWindowAdded = true;
    }
    mDecor.setVisibility(View.VISIBLE);//到这里 Activity 的视图才真正被用户看到
}
~~~
你会发现还是我们一开始添加Window的方法，那PhoneWindow拿来干嘛？应该就是用来组装DecorView的，为什么要它来组装？因为DecorView它是有标题和内容，如果直接传给系统的WMS，那么不就和其他View的添加不一样？应该是为了代码复用，还有解耦，特地先用一个PhoneWindow来处理DecorView，或者叫管理DecorView。所以这个 PhoneWindow 无法提供具体的功能。Activity的Window创建就到这里，我们再看看其他的Window创建过程。

### Dialog 的 Window 创建过程

* 由于 Dialog 的 Window 的创建过程和 Activity 类似，所以下面是简单的讲解过程

  1. 创建 Window ，

  2. 初始化 DecorView 并将 Dialog 的视图添加到 DecorView 中；

  3. 通过 WindowManager 将 DecorView 添加到 Window 中显示；

  4. 当 Dialog 被关闭时，通过 WIndowManager 来移除 DecorView

     ~~~java
     mWindowManager.removeViewImmediate(mDecor);
     ~~~

* Dialog有一点不同需要说明：普通的 Dialog 必须采用 Activity 的 Context，如果采用 Application 的 Context 就会报错；普通的 Dialog 一般是由 Activity 发出的，需要一个东西是 应用token ，而它一般只有 Activity 有，所以这里需要用 Activity 作为 Context 来显示对话框即可；另外，系统 Window 比较特殊，他不需要 token ,可以指定 Window 为系统 WIndow 即可正常弹出，即指定 Window 的 Type 参数；

  ~~~java
  dialog.getWindow().setType(LayoutParams.TYPE_SYSTEM_ERROR);
  //之前也讲过了，系统 WIndow 需要在 AM 文件中注册权限
  <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
  ~~~
---

### Toast的Window
上面的两个Window，都是复制南泰大佬的笔记，懒得写，但是他的逻辑结构我个人不太喜欢，还是自己写吧。

Toast的Window其实和添加悬浮窗是一样的，不过他的级别是系统级的，既然是一样那就是windowManager的addView方法咯？说是这么说，但是我们在使用的时候Toast大多是有默认视图。而且显示位置固定，为了保证每条Toast都能显示，那系统是如何处理大量的Toast请求？再有Toast是定时显示的，他又是怎样的机制去控制定时显示？那意思就是说，如何让添加Toast的Window的过程我们没有必要再学一次了，我们只需要搞懂他的其他工作流程，带着上面的疑问，我们开始走进Toast。


Toast它是有自定义视图的，我们可以自定义视图。
~~~java
Toast.makeText(MainActivity.this, "网络错误",Toast.LENGTH_SHORT).setView("这里传视图").show();
~~~


setView对应的内部成员我们可以看看
~~~java
 /**
     * Set the view to show.
     * @see #getView
     */
    public void setView(View view) {
        mNextView = view;
    }
~~~


那接下来就可以看看显示Toast的方法了，
~~~java
   /**
     * Show the view for the specified duration.
     */
    public void show() {
        if (mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }

        //一次Ipc通信，获取到通知服务
        INotificationManager service = getService();
        String pkg = mContext.getOpPackageName();
        
        //这个东西是Binder，我们等下再介绍它
        TN tn = mTN;
        tn.mNextView = mNextView;

        try {
            //将我们当前显示Toast请求入队，这个时候已经是交给了NMS去排队显示了
            //这里就可以解释了，当有大量请求显示Toast的时候就能保证可以显示出来
            service.enqueueToast(pkg, tn, mDuration);
        } catch (RemoteException e) {
            // Empty
        }
    }
~~~


好像没有看到用WindwoMnanager来addView，其实这很容易想到，人家要排队的，没有那么快就能显示，当然看不到addView，我们可以注意到代码中是把Toast的VIew给了TN，我们看看TN是什么
~~~java
 private static class TN extends ITransientNotification.Stub {
       ...

        TN(String packageName, @Nullable Looper looper) {
          ...
            if (looper == null) {
                // Use Looper.myLooper() if looper is not specified.
                looper = Looper.myLooper();
                if (looper == null) {
                    throw new RuntimeException(
                            "Can't toast on a thread that has not called Looper.prepare()");
                }
            }
            
            mHandler = new Handler(looper, null) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case SHOW: {
                            IBinder token = (IBinder) msg.obj;
                            handleShow(token);
                            break;
                        }
                        case HIDE: {
                            handleHide();
                            // Don't do this in handleHide() because it is also invoked by
                            // handleShow()
                            mNextView = null;
                            break;
                        }
                        case CANCEL: {
                            handleHide();
                            // Don't do this in handleHide() because it is also invoked by
                            // handleShow()
                            mNextView = null;
                            try {
                                getService().cancelToast(mPackageName, TN.this);
                            } catch (RemoteException e) {
                            }
                            break;
                        }
                    }
                }
            };
        }
        
~~~
可以看到出现了很多show的字样， 猜都猜到这个Binder就是添加Window的地方啦，但是它很有意思的地方就是它先判断有没有Looper，还有Handle来处理信息，学过Ipc的话，这个其实很好理解，系统通过TN这个Binder来回调方法控制Toast的显示，我们看看是不是

~~~java
        /**
         * schedule handleShow into the right thread
         */
        @Override
        public void show(IBinder windowToken) {
            if (localLOGV) Log.v(TAG, "SHOW: " + this);
            mHandler.obtainMessage(SHOW, windowToken).sendToTarget();
        }

        /**
         * schedule handleHide into the right thread
         */
        @Override
        public void hide() {
            if (localLOGV) Log.v(TAG, "HIDE: " + this);
            mHandler.obtainMessage(HIDE).sendToTarget();
        }

        public void cancel() {
            if (localLOGV) Log.v(TAG, "CANCEL: " + this);
            mHandler.obtainMessage(CANCEL).sendToTarget();
        }
        
~~~

看！没错吧，系统回调这些方法，然后就用Handle发了信息，然后在handler去控制添加Window(这个代码等下再给)，那我们来思考一下，为什么要用Handle？

Binder是运行在Binder线程池，系统在远程调用，那我们是不是需要切回到Toast创建的线程来显示Toast？所以一开始就先判断我们线程有没有Looper，没有就直接报错了，因为没有Looper是无法完成线程切换的，所以要注意一下。

那我们就看看添加Window和隐藏Window的部分了，很长，你只要找到WMS的addView方法就ok了。
~~~java
        public void handleShow(IBinder windowToken) {
          ...
                    mWM.addView(mView, mParams);
          ...
              
        }


        public void handleHide() {
           mWM.removeViewImmediate(mView);
           mView = null;
        }

~~~

我们知道了系统对大量请求显示Toast的处理，是通过队列实现了确实可以保证所有Toast都有显示的可能，但是还是有问题的，假设我一个软件循环请求无数个Toast，那其他的软件不是排不到队？所以系统限制了一个软件可以在队列排队的的最大Toast数量，非系统应用最大是50个，没有代码，这是源码部分，没有下载翻不到。

---

还有最后一个问题，Toast的定时显示。他是如何计时？在哪里计时？我们刚刚看Toast代码的时候并没有翻到任何有关计时的地方，那我们就要去NMS找了！那肯定是显示Toast的时候去开始计时，所以我们要找到处理队列的代码

~~~java
//当 enqueueToast 将 Toast 请求封装为 ToastRecord 对象并将其添加到 mToastQueue
//后，NMS 就会通过 showNextToastLocked 方法来显示当前的 Toast ；
void showNextToastLocked(){
    ToastRecord record = mToastQueue.get(0);//拿到排在第一个的 ToastRecord
    while(record != null){
        try{
            //这个Callback就是TN
            record.callback.show();
            
            //这个方法是用来发送一条延时消息，具体时间取决于 Toast 的时长；应该也猜到了这个方法用来做什么了吧，就是用来让 Toast 消失的；
            scheduleTimeoutLocked(record);
            return ;
        }catch(RemoteException e){
            ...
        }
    }
}
~~~

咦！找找了，我们看看这个方法
~~~java
private void scheduleTImeoutLocked(ToastRecord r){
    mHandler.removeCallbacksAndMessages(r);
    Message m = Message.obtain(mHandler,MESSAGE_TIMEOUT,r);
    long delay = r.duraion ==Toast.LENGTH_LONG ? LONG_DELAY:SHORT_DELAY;
    mHandler.sendMessageDelayed(m,delay);
}
~~~

就发送一个延时消息实现了计时，我们平时写的时候就可以这样写，不要傻子一样自己去计时，在发送延时消息时，handle内部就实现了计时，这样其实就把计时器解耦了。关于Windwo，就学习到这里啦！