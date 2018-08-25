[TOC]



# 理解Window和WindowManager

## 相关概念

* Window 表示一个窗口的概念，在日常开发中直接接触 Window 的机会并不多，但是在某些特殊时候我们需要在桌面上显示一个类似悬浮窗的东西，那么这种效果就需要用到 Window 来实现；
* Android 中所有的视图都是通过 Window 来呈现的，不管是 Activity 或 Dialog 或 Toast ，它们的视图实际上都是附加在 Window 上的，所以，Window 实际是 View 的直接管理者，从 View 的事件分发机制也可以知道，单击事件由Window 传递给 DecorView，再由 DecorView 传递给我们的View；
* Window 是一个抽象类，它的具体实现是 PhoneWindow ；
  * 我们要是想访问 Window ，我们需要借助 WindowManager；
  * `WMS`有时也需要向`ViewRootImpl`发送远程请求，比如，点击事件是由用户的触摸行为所产生的，因此它必须要通过硬件来捕获，跟硬件之间的交互自然是 Android 系统自己把握，Android 系统将点击事件交给`WMS`来处理。`WMS`通过远程调用将事件发送给`ViewRootImpl`，在`ViewRootImpl`中，有一个方法，叫做`dispatchInputEvent`，最终将事件传递给`DecorView`。作者：huachao1001链接：https://www.jianshu.com/p/028be994677f來源：简书著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
  * 要使应用程序进程可以请求 WMS 服务，必须在 WMS 服务这边创建一个类型为 Session 的 Binder 本地对象，同时应用程序这边获取 Session 的代理对象，通过该代理对象，应用程序进程就可以请求 WMS 服务了。
* 在 Android 中通过 Binder 进行 IPC 时，服务端会开启 Binder 线程来响应 客户端的请求；在 Android 的 java app进程被创建起来时，它就会去建立一个线程池，来专门处理那些 binder IPC事务

### 如何添加一个Window

* 为了分析 Window 的工作机制，我们需要了解如何使用 WindowManager 添加一个Window 。下面是个例子

  ~~~java
  mFloatingButton = new Button(this);
  mLayoutParams = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT,
                                                 LayoutParams,WRAP_CONTENT,
                                                 0,0, PixelFormat.TRANSPARENT);
  mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
  mLayoutParams.x = 100;
  mLayoutParams.y = 300;
  mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE
    					| LayoutParamx.FLAG_SHOW_WHEN_LOCKED;
  mWindowManager.addView(mFloatingButton,mLayoutParams);

  //这里说一点特殊点：由于安卓版本的不同，对于 系统Window （是系统Window）我们需要进行一些权限操作，包括在AM文件中申请权限
  public void buttonClick(View view) {
      if (Build.VERSION.SDK_INT >= 23) {
              if (Settings.canDrawOverlays(context)) {
              showFloatView();//即mWindowManager.addView(mFloatingButton,mLayoutParams);
          } else {//如果版本大于23，用户也还没有允许我们弹出系统Window，我们需要申请
              Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
              startActivity(intent);
          }
      } else {
          showFloatView();
      }
  }
  ~~~

  上面的代码没有什么读不懂的，就是 WindowManager.LayoutParams 对象中的flags 和type 这2个参数比较重要；

  * Flags 参数：表示 Window 的属性，可以控制 Window 的显示特性，如
    1. FLAG_NOT_FOCUSABLE:表示 Window 不需要获取焦点，也不需要接受各种输入时间，此标识会同时启用FLAG_NOT_TOUCH_MODAL，最终事件会直接传递给下层的具有焦点的 Window；
    2. FLAG_NOT_TOUCH_MODAL：在此模式下，系统会将当前 Window 区域以外的单击事件传递给底层的WIndow，当前 Window 区域以内的单击事件则自己处理，这个标记一般来说都需要开启，否则其他 Window将无法收到单击事件；
    3. FLAG_SHOW_WHEN_LOCKED：开启可以让 Window 显示在锁屏的界面上；
  * type 参数：表示 Window 的类型，Window由3种类型，分别是应用Window，子Window，系统Window；**应用Window对应着一个Activity**；子Window不能单独存在，它需要附属在特定的父Window中，比如常见的一些Dialog就是一个在Window；系统Window是先需要声明权限在再创建的Window，比如Toast和系统状态栏这些；
  * Window是分层的，每个 Window 都有对应的 z-ordered，层级大的会覆盖在层级小的 Window 上面；使用时指定WindowManager.LayoutParams 对象中的 type 属性即可；使用时自己查对应的层级范围就可以；

### WindowManager所提供的功能

* WindowManager用来在应用与window之间的管理接口，管理窗口顺序，消息等。

~~~java
public interface ViewManager{
  public void addView(View view,ViewGroup.LayoutParams params);
  public void updateViewLayout(View view,ViewGroup.LayoutParams params);
  public void removeView(View view);
}
//这里我为什么要写ViewManager，是因为WindowManager继承了它，作用显而易见，WindowManager常用的方法就是这3个方法，添加View，更新View（的布局，看上面的方法名），删除View；
~~~

从上面可以得到一个规律，WindowManager操作Window的过程更像是在操作 Window 中的 View；我们时常见到的可以拖动的Wondow效果，其实很好实现；

~~~java
//先给View设置哦那TouchListener： mFloatingButton。setOnTouchListener(this).然后重写onTouch方法
public boolean onTouch(View v,MotionEvent event){
   int rawX = (int) event.getRawX();
   int rawY = (int) event.getRawY();
   switch(event.getAction()){
  	   mLayoutParams.x = rawX;
       mLayoutParams.y = rawY;
       mWindowManager.updateViewLayout(mFolatingButton,mLayoutParams);//就是这里
   }
  ...
}
~~~

## Window的内部机制

* 前面说了Window 表示一个窗口的概念，这是一个抽象的概念，每个 Window 都对应着一个 View 和一个ViewRootImpl，ViewRootImpl 是 Window 和 View 联系的中间者；我们可以从上面 WindowManager 的定义可以看出，它提供的三个接口方法都是针对View的，这说明 View 才是 Window 存在的实体，因此Window并不是实际存在的，它是以 View 的形式存在；前面也说了，在实际使用中是无法直接访问 Window 的，对 Window 的访问必须通过WindowManager，为了分析 Window ,需要从 WindowManager 中去开始；

### Window的添加过程

* Window 的添加过程需要通过 WindowManager 的 addView 来实现，而 WindowManager 是一个接口，它的真正实现是 WindowManagerImpl 类，我们去 WindowManagerImpl 中找 Window 的操作：

  ~~~java
  @Override
  public void addView(View view,ViewGroup.LayoutParams params){
    mGlobal.add(view,params,mDisplay,mParentWindow);
  }
  //从上面就可以清楚地看出，WindowManagerImpl 并没有直接实现 Window 的操作，而是把工作都交给 WindowManagerGolbal 来处理，这种工作该模式就是典型的桥接模式，将所有的工作全部委托给 WindowManagerGlobal 来实现；

  ~~~

* WindowManagerGlobal 以单例的形式向外提供自己的实例，**也就是它只有一个对象，这点很重要**；

* WindowManagerGlobal 的 addView 方法主要分为如下几步

  1. 检查参数是否合法，如果是子Window 那么还需要调整一些布局参数；

  2. 创建 ViewRootImpl 并将 View 添加到列表中；

     注意：这个列表指的是 WindowManagerGlobal 内部的表，再次说明这个类是单例：

     ~~~java
     private final ArrayList<View> mView = new ArrayList<View>();//储存所有Window所对应的View
     //储存所有 Window 所对应的 ViewRootImpl ；
     private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();
     //储存所有 Window 所对应的布局参数；
     private final ArrayList<WindowManager.LayoutParams> mParams = new ArrayList<WindowManager.LayoutParams>();
     //储存了那些正在被删除的View的对象，或者说是那些已经调用removeView方法但是删除操作还未完成的Window
     private final ArraySet<View> mDyingViews = new ArraySet<View>();
     ~~~

     ~~~java
     //在 WindowManagerGlobal 的 addView 方法中会通过如下方式将Window的一系列对象添加到列表中：
     root = new ViewRootImpl(view.getContext(),display);
     view.setLayoutParams(wparams);
     //开始将 Window 的一系列对象添加到 WindowManagerGlobal 的列表中；
     mViews.add(view);
     mRoots.add(root);
     mParams.add(wparams);
     ~~~

  3. 最后一步，通过 ViewRootImpl 来更新界面并完成 Window 的添加过程；

     我们之前已经知道了，Window 是借助 ViewRootImpl 和 View 联系的，那么 View 的最终呈现和添加也当然是ViewRootImpl 来完成；

     ~~~java
     //这个步骤由 ViewRootImpl 的 setView() 来完成；在 setView() 内部会通过 requestLayout() 来完成异步刷新请求。
     public void requestLayout(){
       if(!mHandlingLayoutInLayoutRequest){
       	checkThread();
         mLayoutRequested = true;
         scheduleTraversals();//这是View绘制的入口，在View的绘制篇应该有讲到过；
       }
     }
     ~~~

     View绘制的开始方法其实就是ViewRootImp里面的performTraversals，在创建 ViewRootImp 里面要注意下面这句代码：

     ~~~java
     mWindowSession = WindowManagerGlobal.getWindowSession();
     ~~~

     他会为我们创建一个IWindowSession对象，这个 IWindowSession 对象具体来讲的话是通过WindowManagerGlobal 的 getWindowSession 创建的，这段源码不算长，我们来看看：

      ~~~java
     public static IWindowSession getWindowSession() {
             synchronized (WindowManagerGlobal.class) {
                 if (sWindowSession == null) {
                     try {
                         InputMethodManager imm = InputMethodManager.getInstance();
                         IWindowManager windowManager = getWindowManagerService();//先拿到WMS
                         sWindowSession = windowManager.openSession(//再通过WMS创建Session
                                 imm.getClient(), imm.getInputContext());
                         float animatorScale = windowManager.getAnimationScale(2);
                         ValueAnimator.setDurationScale(animatorScale);
                     } catch (RemoteException e) {
                         Log.e(TAG, "Failed to open window session", e);
                     }
                 }
                 return sWindowSession;
             }
     }
      ~~~

     第6行看到通过 getWindowManagerService 创建了一个 WindowManagerService 对象出来，查看WindowManagerService 源码会发现实际上是通过 IPC 的方式创建出来的，学了 Binder 总算看懂一些了：

     ~~~java
     public static IWindowManager getWindowManagerService() {
            synchronized (WindowManagerGlobal.class) {
                if (sWindowManagerService == null) {
                    sWindowManagerService = IWindowManager.Stub.asInterface(
                            ServiceManager.getService("window"));
                }
                return sWindowManagerService;
            }
     }
     ~~~

     有了 WindowManagerService 对象之后，会利用该对象调用 openSession 方法创建出来一个 IWindowSession 对象，即一个 Session 对象；有了这个 Session 对象之后，随后的添加操作实际上就是 ViewRootImpl 通过这个Session 来进行添加的；

     接着会通过 WindowSession 最终完成 Window 的添加过程，在下面的代码中，mWindowSession 的类型是IWindowSession ,他是一个 Binder 对象，真正的实现类是 Session，也就是 Window 的添加过程是一个 IPC

     ~~~java
     res = mWindowSession.addToDisplay(mWindow,mSeq,mWindowAttributes,mAttachInfo.mContentInsets,mInputChannel);//这里 mWindowSession 就是上面说的 Binder 对象即 Session；这个方法的内部会利用我们在创建 ViewRootImpl 的时候创建的 WindowManagerService 对象，调用 WindowManagerService 对象的addWindow方法；
     ~~~

     在 Session 内部会通过 WindowManagerService  来实现 Window 的添加；

     ~~~java
     public int addToDisplay(IWindow window,int seq,WindowManager.LayoutParams attrs,int viewVisibility,int displayId,Rect outContentInsets,InputChannel outInputChannel){
       return   mService.addWindow(this,window,swq,attrs,viewVisibility,
                                   displayId,outContentInsets,outInputChannel);
     }
     ~~~

     这样一来，Window 的添加请求就交给 WindowManagerService 去处理了，在 WindowManagerService 内部会为每**一个应用保留**一个**单独的 Session** 。到这里Window的添加过程就告一段落；

     ​

### Window的删除过程

* Window 的删除过程和添加过程是一样的，先是通过 WindowMangerImpl 后，再进一步通过 WindowManagerGlobal 来实现的；下面就直接到 WindowManagerGlobal  的 removeView() 中看看

  ~~~java
  public void removeView(View view,boolean immediate){
    ...
    synchronized(mLock){
    	int index = findViewLocked(view,true);//先找到删除的View的索引；
      View curView = mRoots.get(index).getView();
      removeViewLocked(index,immediate);//再调用它来做进一步的删除；
      if(curView == view){
    		return ;
  	}
    }
  }
  //那么 removeViewLocked 具体是怎么删除的，其实是通过 ViewRootImpl 操作的；
  public removeViewLocked(int index,boolean immediate){
      ViewRootImpl root = mRoots.get(index);//先找到删除的View的索引 对应的 ViewRootImpl
      View view = root.getView()://再通过 ViewRootImpl 来拿到 View
      if(view != null){
          InptMethodManager imm = InputMethodManager.getInstance():
          if(imm != null){
              imm.windowDismissed(mViews.get(index).getWindowToken()):
          }
      }
      //在 WindowManger 中提供了2种删除接口 removeView 和 removeViewImmediate ，它们分别异步删除和同步删除，其中一般不适用 同步删除，所以 这里主要说异步删除操作，即 removeView ，就是最上面那个方法，具体的删除操作由 ViewRootImpl 的 die()方法来完成；
      boolean deferred = root.die(immediate)://die方法只是发送了一个请求删除的消息后就立刻返回了，此时 View 并没有完成删除操作，所以最后会将其添加到 mDyingViews 中，mDyingViews表示的是待删除的View列表
      if(view != null){
          view.assignParent(null);
          if(deferred){
              mDyingViews.add(view);
          }
      }  
  }
  ~~~

  ~~~java
  boolean die(boolean immeiate){
      if(immediate && !mIsInTraversal){//判断是否为异步，如果不是则为同步，那么直接调用 doDie()
          doDie():
          return false;//然后就返回
      }
      //否则就为异步操做，这里省略一些，只写重点部分
      ...
      mHandler.sendEmptyMessage(MSG_DIE)://异步操作，就会发送一个MSG_DIE 的消息，ViewRootImpl 中的 Handler 会处理此消息并调用 doDie 方法；
      return true;
  }
  //那么 doDie 内部会做什么事情呢？
  ~~~

* doDie 内部会调用 dispatchDeachedFromWindow 方法，真正删除 View 的逻辑在 dispatchDetachedFromWindow 方法中实现；dispatchDeachedFromWindow 方法主要做4件事

  1. 垃圾回收 的工作，比如清楚数据和消息，移除回调；
  2. 这里最重要，通过 Session 的 remove 方法删除 Window :mWindowSession.remove(mWindow)，这同样是一个 IPC 过程，其实跟添加过程一样，最终当然也是会调用 WindowManagerService 的 removeWindow 方法；将删除 的操作交给 WMS 去做；
  3. 调用 View 的 dispatchDeachedFromWindow 方法，在内部会调用 View 的 onDetachedFromWindow() 以及 onDetachedFromWindowInternal() 。对于 onDetachedFromWindow()  就很熟悉了，这个日常开发中有时会用到，当View 从Window 中移除时，这个方法就会被调用，可以在这个方法中做一些资源回收的工作，比如终止动画，停止线程等；
  4. 调用 WindowManagerGlobal 的 doRemoveView 方法刷新数据，包括 mRoots，mParams ,以及 mDyingViews ，就是之前说过的这个类中的3个列表，需要将当前 Window 所关联的这三类对象从列表中删除；直接查看doRemoveView源码你会发现其实就是将当前 View 从 mViews 中移出，将当前 ViewRootImpl 从当前 mRoots 移出，将当前 LayoutParams 从 mParams 移出而已了，因为你在创建的时候添加进去了嘛，删除的时候就该移出出去啊！




### Window的更新过程

* 更新的操作其实也和之前的一样，只是一些改变而已；我们还是从 WindowManagerGlobal 的 updateViewLayout 方法看，updateViewLayout 做的事情就比较简单了，

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

* 在 ViewRootImpl 的 setLayoutParams 方法里面你会看到执行了 scheduleTraversals 方法，这个方法就会开启我们从DecorView 的视图重绘工作，接着呢，还需要更新我们的 Window 视图呀，具体是通过 scheduleTraversals 调用performTraversals 方法之后，在 performTraversals 方法里面执行的，在 performTraversals方法里面执行了ViewRootImpl 的 relayoutWindow 方法，而在 relayoutWindow 里面就会执行 Session 的 relayout 方法了，很可爱，我们又见到了 Session 啦，下一步不用想肯定就是执行的 WindowManagerService 的 relayoutWindow 方法来更新Window ！

## 多种类的Window的创建过程

* 通过上面的分析，View 是 Andorid 中的视图的呈现方式，也知道了 View 不能单独 存在，它必须附着在 Window 这个抽象的概念上面，因此，有视图的地方就有 WIndow，因此 Activity ， Dialog ，Toast 等视图都对应着一个 Window 。下面将分析这3种 Window 的创建过程


### Activity 的 Window 的创建过程

* 先总结好理解：先创建 Window，再使用 WindowManage r添加 Window ；WindowManager用来在应用与window之间的管理接口，管理窗口顺序，消息等。


* 这里并不是讲 Activity 的创建全过程，Activity 的启动过程很复杂，而是讲Activity 的 Window 的创建过程，最终会由 ActivityThread 中的 performLaunchActivity() 来完成整个启动过程，在这个方法内部会通过类加载器创建 Activity 的实例对象，并调用其 attach 方法为其关联运行所依赖的一系列上下文环境变量；

  ~~~java
  java.lang.ClassLoader cl = r.packageInfo.getClassLoader();//类加载器
  activity = mInstrumenttation.newActivity(cl,component.getClassName(),r.intent);//创建 Activity 的实例对象
  ...
  if (activity != null){
     Context appContext = createBaseContextForActivity(r,activity);
      CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
      Configuration config = new Configuration(mCompatConfiguration);
      if(DEBUG_CONFIGURATION){
          ...
          //调用其 attach 方法为其关联运行所依赖的一系列上下文环境变量；
          activity.attach(appContext,this,getInstrumentation(),r.token,r.ident,app,r.intent,      					r.activityInfo,title,r.parent,r.embeddedID,r.
                          lastNonConfigurationInstances,congig,r.voiceInteractor);
          ...
      }
  }  
  ~~~

  ~~~java
  //在 Acitvity 的 attach 方法里，系统会创建 Activity 所属的 Window 对象那个，并未其设置回调接口，由于Activity 实现了 Window 的 Callback 接口，因此当 Window 接受到外界的状态改变时就会回调 Activity 的方法。这里我给你列举几个我们比较熟悉的 接口方法，如onAttachedToWindow ，onDetachedFromWindow， dispatchTouchEvent等，； 
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
  ~~~

  ~~~java
  //但是在实际调用中，PolicyManager 的真正实现是 Policy 类，Policy 类中的 makeNewWindow
  public  Window makeNewWindow（Context context）{
      return new PhoneWindow(context);//这里就验证了 Window 的真正实现是 PhoneWindow 
  }
  //关于策略类 PolicyManager 是如何关联到 Policy 上面的，这个无法从源码中得出，书里猜测是由编译环节动态控制。
  ~~~

  ~~~java
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
  ~~~

  ~~~java
  //2. 将 View 添加到 DecorView 的 mContentParent 中
  mLayoutInflater.inflate(layoutResID,mContentParent);
  //到这里我们就可以理解在 Activity 中设置布局的方法为什么不叫 setView ，而是叫做 setContentView ，因为 Activity 的布局文件只是被添加到 DecorView 的 mContentParent 中，因此叫 setContentView 更合适；
  ~~~

  ~~~java
  //3. 回调 Activity 的 onContentChanged 方法通知 Activity 视图已经发生改变，通知后 Activity 才会去做接下来的事情，例如生命周期的继续等等；由于 Activity 实现了 Window 的 Callback 接口，所以 直接调用即可，Activity 的 onContentChanged 方法是个空实现，我们去 子Activity 中处理这个回调，
  final Callback cb = getCallback();
  if(cb != null && !isDestroyed){
      cb.onContentChanged();
  }
  ~~~

  经过上面的步骤，DecorView 已经创建并初始化完毕了， Activity 的布局文件也已经成功添加到了 DecorView 的 mContentParent 中，但是这个时候 DecorView 还没有被 WindowManager 正式添加到 Window 中，虽然说早在 Activity 的 attach 方法中 Window 就已经被创建了，但是这个时候由于 DecorView 没有被 WindowManager 识别并添加到 WMS 中去所以接受不到外界的任何消息，所以这个 Window 无法提供具体的功能；这也正常，Activity 的生命周期就是这样设定的，在 onCreate 时Window 并不被外界观察到；在 ActivityThread 的 handleResumeActivity 方法中，首先会调用 Activity 的 onResume 方法，接着会调用 Activity 的 makeVisible()，这个方法看看就懂

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

  到这里，Activity 的 WIndow 的创建就告一段落；

  ​

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

### Toast 的 Window 创建过程

* Toast 就和 Dialog 不一样了，Toast 它会定时消失，系统采用了 Handler；

* 在 Toast 的内部有2类 IPC 过程

  1. Toast 访问 NotificationManagerService

  2. NotificationManagerService 回调 Toast 里的 TN 接口；

     ​

* 首先，Toast 属于系统 Window ；这一点很重要；

* Toast 内部的视图由2种方式指定，一种是系统默认的样式，另一种是通过 setView 方法来指定一个自定义 View ，不管如何，它们都对应 Toast 的一个 View 类型的内部成员 mNextView。Toast 提供了 show 和 cancel 用于显示和隐藏，内部当然是 IPC 过程；

  ~~~java
  public void show(){
      if(mNextView == null){
          throw new RuntimeException("setView must have been called");
      }
      INotificationManager service = getService();//拿到 NMS 
      String pkg = mContext.getOpPackageName();
      TN tn = mTN;//TN 是个 Binder 类，供 NSM 调用，在 Toast 和 NMS 进行 IPC 的过程中，当 NMS 处理 Toast 的显示或隐藏请求时会跨进程回调 TN 中的方法，这个时候由于 TN 运行在 Binder 线程池中，所以需要通过 Handler 将其切换到 发送 Toast 请求所在的线程中；
      tn.mNextView = mNextView;//当然需要有视图
      try{
          service.enqueueToast(pkg,tn,mDuration);//调用 NMS 的 enqueueToast 方法；第一个参数为当前应用的包名，第二个 tn 表示远程回调，第3个表示 Toast 的时长；enqueueToast 首先会将 Toast 请求封装为 ToastRecord 对象并将其添加到一个名为 mToastQueue （它其实是一个 ArrayList）的队列中。对于非系统应用来说，mToastQueue 中最多只能同时存在 50 个 ToastRecord；之所以这么做，是因为防止通过大量的循环去连续弹出 Toast ，就将会导致其他应用没有机会弹出 Toast ，那么对于其他应用的 Toast 请求，系统的行为就是拒绝服务，这就是拒绝服务攻击的含义，常用于网络攻击中；
      }catch(RemoteException e){        
      }
  }
  public void cancel(){
      mTN.hide();
      rty{
          getService().cancelToast(mContext.getPackageName(),mTN);
      }catch(RemoteException e){        
      }
  }
  //从上面的代码可以看出，显示和隐藏 Toast 都需要通过 NMS 来实现，由于 NMS 运行在系统的进程种，所以只能通过远程调用的方式来显示和隐藏 Toast，
  ~~~

  ~~~java
  //当 enqueueToast 将 Toast 请求封装为 ToastRecord 对象并将其添加到 mToastQueue 后，NMS 就会通过 showNextToastLocked 方法来显示当前的 Toast ；
  void showNextToastLocked(){
      ToastRecord record = mToastQueue.get(0);//拿到排在第一个的 ToastRecord
      while(record != null){
          try{
              record.callback.show();//注意，这是 NSM 调用的，那它调用的当然是 Toast 传过来的 TN 对象的远程 Binder ；这个 record.callback 就是它，通过 callback 来访问 TN 中的方法是当然是需要 IPC 完成，最终被调用的 TN 中的方法会运行在 Toast 请求的应用的 Binder 线程池中；
              scheduleTimeoutLocked(record);//这个方法是用来发送一条延时消息，具体时间取决于 Toast 的时长；应该也猜到了这个方法用来做什么了吧，就是用来让 Toast 消失的；
              return ;
          }catch(RemoteException e){
              ...
          }
      }
  }
  private void scheduleTImeoutLocked(ToastRecord r){
      mHandler.removeCallbacksAndMessages(r);
      Message m = Message.obtain(mHandler,MESSAGE_TIMEOUT,r);
      long delay = r.duraion ==Toast.LENGTH_LONG ? LONG_DELAY:SHORT_DELAY;
      mHandler.sendMessageDelayed(m,delay);
  }
  //延迟相应的时候后，NMS 会通过 cancelToastLocked 方法来隐藏 Toast 并将其从 mToastQueue 中移除，这个时候如果 mToastQueue 还有其他 Toast ,那么 NMS 就继续显示其他 Toast；
  ~~~

  ~~~java
  //Toast 的隐藏也是通过 ToastRecord 的 callback 来完成的；
  try{
      record.callback.hide();
  }
  ~~~

* 通过上面的分析，其实可以知道 Toast 的显示和影响过程实际上是 Toast 中的 TN 这个类来实现的，上面调用了它2个方法，下面说明，由于这两个方法是被 NMS 以跨进程的方式调用的，因此它们运行在发出 Toast 请求的客户端的 Binder 线程池中（在Android中通过 Binder 进行 IPC 时，服务端会开启 Binder 线程来响应 客户端的请求；在android的java app进程被创建起来时，它就会去建立一个线程池，来专门处理那些binder IPC事务）为了将执行环境切换到 Toast 请求所在的线程中，内部使用了 Handler；

  ~~~java
  @Override
  public void show(){
  	if(localLOGV) Log.v(TAG,"SHOW:"+this);
      mHandler.post(mShow);
  }
  //隐藏 Toast 的操作也一样；
  ~~~

  在上述代码中，mShow 是一个 Runnable ,它内部调用了 handleShow ;由此可见，handleShow 才是真正显示 Toast 的地方。TN 的 handleShow 会将 Toast 的视图添加到 Window 中；

  ~~~java
  mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
  mWM.addView(mView,mParams);
  ~~~

  ​

##终于结束了:cry:

* 每学习一个新的东西都会头皮发麻，不过总算到了这里，理解了之前很多困惑；