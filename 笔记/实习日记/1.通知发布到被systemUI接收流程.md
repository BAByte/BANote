# 需求

+ hook android原生的通知栏
+ 支持remoteviews自定义view
+ 全局悬浮

# 大致思路

+ 首先要想办法收集到所有的通知
+ 实现一个全局悬浮窗，里面放个列表或者scrollView
+ 拿到通知后获取到客户端设置的通知布局添加到消息列表中
+ 接下来进行对通知的删除，优先级排序，筛选进行进一步的优化
+ 按照优先级弹出横幅进行通知，默认全弹出

# 实现监听应用通知的思路

+ 如果是使用sdk的api，可以定义一个继承于NotificationListenerService服务，启动后的NotificationListenerService会被注册到NotificationManagerService服务中，当有新通知来的时候我们可以在onNotificationPosted（StatusBarNotification sbn）里面拿到notification

# SystemServer启动NotificationManagerService

+ 直接在SystemServer进程的主线程进行启动
+ NMS初始化了 mHandler = new WorkerHandler(); 
+ NotificationManagerService，初始化了一个NotificationListeners用来分发notification

# 通知发布到NotificationListenerService流程

+ app使用构建Notification
+ 通过NotificationManager进行notify
+ 通过binder进入到systemServer进程打包一个runnable：EnqueueNotificationRunnable，使用WorkerHandler进行post
+ EnqueueNotificationRunnable会执行 mListeners.notifyPostedLocked(n, oldSbn);对notification进行分发
+ mListeners存储的对象具体是：NotificationListeners类型，notifyPostedLocked最终会回到NMS的notifyPosted方法

~~~java
private void notifyPosted(final ManagedServiceInfo info, final StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate) {
  //这个listener具体的对象就是NotificationListenerService
    final INotificationListener listener = (INotificationListener)info.service;
    StatusBarNotificationHolder sbnHolder = new StatusBarNotificationHolder(sbn);
    try {
      
        listener.onNotificationPosted(sbnHolder, rankingUpdate);
    } catch (RemoteException ex) {
        ...
    }
}
~~~

而SystemUI中就需要向NotificationListenerService注册！！

# systemUI启动流程

+ 因为要看到他是怎么去注册自己的NotificationListenerService，所以要看启动流程
+ 也是由SystemServer启动的，是启动SystemUIService这个服务

~~~java
static final void startSystemUi(Context context) {
    Intent intent = new Intent();
    intent.setComponent(new ComponentName("com.android.systemui",
                "com.android.systemui.SystemUIService"));
    intent.addFlags(Intent.FLAG_DEBUG_TRIAGED_MISSING);
  
    context.startServiceAsUser(intent, UserHandle.SYSTEM);
}
~~~

+ 在SystemUIService里面会去

~~~java
public class SystemUIService extends Service {

    public void onCreate() {
        super.onCreate();
  
  //在    SystemUIApplication有个数组，里面有一堆服务，我看了下，通知相关的是SystemBars
        ((SystemUIApplication) getApplication()).startServicesIfNeeded();
    }
    ...
}
~~~

+ SystemBars启动后会根据一个config去启动PhoneStatusBar
+ 而PhoneStatusBar的父类是BaseStatusBar，我在里面终于见到了实例化NotificationListenerService的过程，并且会去向NMS注册

~~~java
 public void start() { 
		mNotificationListener.registerAsSystemService(mContext,new ComponentName(mContext.getPackageName()
                   	,getClass().getCanonicalName()),
                    UserHandle.USER_ALL);
 }

  @SystemApi
    public void registerAsSystemService(Context context, ComponentName componentName,
            int currentUser) throws RemoteException {
        if (mWrapper == null) {
            mWrapper = new NotificationListenerWrapper();
        }
        mSystemContext = context;
      //就是这里向NMS进行注册，最后会放在serviceManager里面了
        INotificationManager noMan = getNotificationInterface();
        mHandler = new MyHandler(context.getMainLooper());
        mCurrentUser = currentUser;
        noMan.registerListener(mWrapper, componentName, currentUser);
    }
~~~

+ registerAsSystemService在sdk中是没有的，需要在framework层调

+ 当NMS在调用listener分发通知时就会分发到systemUI的BaseStatusBar的NotificationListenerService，onNotificationPosted进行处理
+ systemUI通过onNotificationPosted回调拿到StatusBarNotification
+ StatusBarNotification里面就带有我们发出的Notification，通过这个Notification我们就可以拿到用户设置的内容，布局等。

# 无法监听的问题的解决方案

在经过查看NotificationListenerService从启动到被绑定的过程后解决了无法接收到通知消息的问题，下面是解决思路

+ 不需要我们手动启动NotificationListenerService，当数据库更改，或者有应用卸载后，会自动进行更新订阅

+ NotificationListenerService该服务不能复写这个方法onBind，否则会启动失败，必须要重启系统才可以

# 监听成功后遇到的问题1

- 在服务一旦关闭后再重新连接，会发现接收不到回调，说明NotificationManagerService并没有通知到我们重新打开的NotificationListenerService，这需要去看看NotificationListenerService是怎么针对已经启动过又关闭的NotificationListenerService进行处理，我猜这里可能是为了防止同一个NotificationListenerService服务订阅多次？

# 尝试解决问题1的思路

- 我们需要主动的去让NMS重新增加订阅者，所以只需要触发对应的条件就好了。

# 小结

看完后发现一个问题，NotificationListenerService在sdk的版本是不需要手动启动的，而在android7.0源码的framework层的system api中，NotificationListenerService是需要手动启动的。在源码中可以看到framework层的NotificationListenerService是一个INotificationListener对象，直接传入NMS等待通知的到来。

而sdk中的只是四大组件之一的service，而其他进程想要和这个service进行IPC的话，需要使用binder机制，而不管怎样，我们的NotificationListenerService都需要和NMS进行IPC，所以我觉得这也是在使用sdk开发时我们不用手动启动NotificationListenerService的原因之一，你可以尝试复写service的onBinder方法，return个null。你会发现我们的NotificationListenerService无法和NMS进行通信了。这也为我解决我写的无法监听通知的思路！

通知系统其实是一个观察者模式，系统在第一次启动时会对所有NotificationListenerService进行存储（使用sdk的api都是被动订阅），而framework层的systemUI是自己去注册的（我看源码是这样的！）。为什么systemUI是可以自己手动注册呢？有些时候systemUI是崩溃的，假设在Binder绑定的过程中crash了，如果是sdk的api，所有NotificationListenerService都不会被重新订阅，因为和四大组件的service绑定时为了防止订阅者重复，会在绑定前加一个tag（这和我写imageLoad时因为异步需要处理view复用问题，给view打tag是一个意思）。绑定成功后会解除这个tag。所以当绑定时crash了，那么就不会被释放tag，系统就一直以为这个订阅者还在绑定。所以必须要系统重新启动！而systemUI由于可以直接注册，所以可能不需要经过tag的问题，有空再再看具体的区别。











