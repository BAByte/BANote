[toc]
# RemoveViews(跨进程的控件)
## 先说两句
这个RemoveViews就是我们常见的通知栏里面控制音乐播放的那个布局，也是桌面小控件的那个布局，能在两个软件之间共用一个布局，非常的神奇。一探究竟吧。
## 通知栏控制
我们先看看怎么在通知栏里面显示出我们想要的布局。下面代码是在服务里面的
~~~java
//这里引入了我们的布局文件，这里需要注意一下不是所有额度ViewGroup都能被显示出来的，比如coordin什么的那个布局，ConstraintLayout就不能被显示出来
    RemoteViews remoteViews =new RemoteViews(getPackageName(),R.layout.remove_views_layout);
    
    //这里是设置子View的属性，是不是很奇怪？这里不支持自定义View，也不支持直接访问到布局里面的View，后面应该会学到，先保留这个疑问
        remoteViews.setTextViewText(R.id.text,"环境开会");

//一般来世我们要显示一个自定义布局的通知，当软件退出的时候通知就会被删除，除非我们显示的是一个前台通知
        Notification notification=new NotificationCompat.Builder(this,"100")
                .build();

        //这个ID是有用的，在后面
        startForeground(1,notification);
        Log.d(TAG, "onCreate: ");

        //设置优先级什么的
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.priority=NotificationCompat.PRIORITY_MAX;
        //设置我们的removeView
        notification.contentView=remoteViews;
    
        //然后去刷新我们的前台服务
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        
        //这个id现在就用上了
        notificationManager.notify(1,notification);
~~~

### 小结
其实很简单，就设置一个removeViews，但是我们并没有处理点击事件，这里也是和平时不一样的，我们直接斜杠
## 桌面小部件
用AS快捷生成一个控件，和Activity一样的。在那么多个自动生成文件里面，我们先看res/xml/的文件，会有对于的小部件配置文件，我们看看
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    //这个小部件对应的布局文件
    android:initialKeyguardLayout="@layout/new_app_widget"
    android:initialLayout="@layout/new_app_widget"
    //最小宽高
    android:minHeight="40dp"
    android:minWidth="40dp"
    //在选择小插件的时候的预览图
    android:previewImage="@drawable/example_appwidget_preview"
    //代表在变化小部件大小的时候可以水平和垂直拉伸
    android:resizeMode="horizontal|vertical"
    //代表代表自动更新的周期
    android:updatePeriodMillis="86400000"
    android:widgetCategory="home_screen">
</appwidget-provider>
~~~

+ minWidth 和minHeight 

  它们指定了App Widget布局需要的最小区域。
  缺省的App Widgets所在窗口的桌面位置基于有确定高度和宽度的单元网格中。如果App Widget的最小长度或宽度和这些网格单元的尺寸不匹配，那么这个App Widget将上舍入（上舍入即取比该值大的最接近的整数——译者注）到最接近的单元尺寸。
  注意：app widget的最小尺寸，不建议比 “4x4” 个单元格要大。关于app widget的尺寸，后面在详细说明。

+ minResizeWidth 和 minResizeHeight 

  它们属性指定了 widget 的最小绝对尺寸。也就是说，如果 widget 小于该尺寸，便会因为变得模糊、看不清或不可用。 使用这两个属性，可以允许用户重新调整 widget 的大小，使 widget 的大小可以小于 minWidth 和 minHeight。
  注意：(01) 当 minResizeWidth 的值比 minWidth 大时，minResizeWidth 无效；当 resizeMode 的取值不包括 horizontal 时，minResizeWidth 无效。
           (02) 当 minResizeHeight 的值比 minHeight 大时，minResizeHeight 无效；当 resizeMode 的取值不包括 vertical 时，minResizeHeight 无效。

+ updatePeriodMillis 

  它定义了 widget 的更新频率。实际的更新时机不一定是精确的按照这个时间发生的。建议更新尽量不要太频繁，最好是低于1小时一次。 或者可以在配置 Activity 里面供用户对更新频率进行配置。 实际上，当updatePeriodMillis的值小于30分钟时，系统会自动将更新频率设为30分钟！关于这部分，后面会详细介绍。
  注意: 当更新时机到达时，如果设备正在休眠，那么设备将会被唤醒以执行更新。如果更新频率不超过1小时一次，那么对电池寿命应该不会造成多大的影响。 如果你需要比较频繁的更新，或者你不希望在设备休眠的时候执行更新，那么可以使用基于 alarm 的更新来替代 widget 自身的刷新机制。将 alarm 类型设置为 ELAPSED_REALTIME 或 RTC，将不会唤醒休眠的设备，同时请将 updatePeriodMillis 设为 0。

+ initialLayout 

  指向 widget 的布局资源文件

+ configure

  可选属性，定义了 widget 的配置 Activity。如果定义了该项，那么当 widget 创建时，会自动启动该 Activity。

+ previewImage

  指定预览图，该预览图在用户选择 widget 时出现，如果没有提供，则会显示应用的图标。该字段对应在 AndroidManifest.xml 中 receiver 的 android:previewImage 字段。由 Android 3.0 引入。

+ autoAdvanceViewId 

  指定一个子view ID，表明该子 view 会自动更新。在 Android 3.0 中引入。

+ resizeMode 

  指定了 widget 的调整尺寸的规则。可取的值有: "horizontal", "vertical", "none"。"horizontal"意味着widget可以水平拉伸，“vertical”意味着widget可以竖值拉伸，“none”意味着widget不能拉伸；默认值是"none"。Android 3.1 引入。

+ widgetCategory 

  指定了 widget 能显示的地方：能否显示在 home Screen 或 lock screen 或 两者都可以。它的取值包括："home_screen" 和 "keyguard"。Android 4.2 引入。

+ initialKeyguardLayout 

  指向 widget 位于 lockscreen 中的布局资源文件。Android 4.2 引入。
  
## AM文件
~~~xml
    <receiver android:name=".NewAppWidget">
            <intent-filter>
            //必须加，不然无法显示在小部件列表
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>

            //指定配置文件
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/new_app_widget_info" />
    </receiver>
~~~
就是一个广播。
## 小部件的控制文件
里面会有几个复写的方法


+ onUpdate()

  当 widget 更新时被执行。
  同样，当用户首次添加 widget 时，onUpdate() 也会被调用，这样 widget 就能进行必要的设置工作(如果需要的话) 。但是，如果定义了 widget 的 configure属性(即android:config，后面会介绍)，那么当用户首次添加 widget 时，onUpdate()不会被调用；之后更新 widget 时，onUpdate才会被调用。


+ onDeleted(Context, int[])

  当 widget 被删除时被触发。

+ onEnabled(Context)

  当第1个 widget 的实例被创建时触发。也就是说，如果用户对同一个 widget 增加了两次（两个实例），那么onEnabled()只会在第一次增加widget时触发。

+ onDisabled(Context)

  当最后1个 widget 的实例被删除时触发。

+ onReceive(Context, Intent)

  接收到任意广播时触发，并且会在上述的方法之前被调用。上面的所有方法，其实都是在这个方法里面调用的，你可以看看源码，意味着我们也可以在这里分发事件，这里的事件只是通过Intent的action来区分的，比如我们可以添加点击事件，下面我们看看
  
  首先你要在AM文件加一个action，用来区分我们的点击事件
  ~~~xml
    <receiver android:name=".NewAppWidget">
            <intent-filter>
            //这个
                <action android:name="com.ba.appwidget.action.CLICK"/>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>

            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/new_app_widget_info" />
        </receiver>
  ~~~
  
  然后再看看我们小部件的控制文件怎么写
  ~~~java
  /**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {
    private static final String APP_WIDGET_CLICK="com.ba.appwidget.action.CLICK";

    //这个方法被onUpdate调用，是具体更新小控件的方法
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //其实就是更新小部件的RemoveViews
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        //设置点击事件，这个每一次都要重新设置，removeView实例不一样，必须要设置
        Intent clickIntent=new Intent();
        clickIntent.setAction(APP_WIDGET_CLICK);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,clickIntent,0);
        views.setOnClickPendingIntent(R.id.appwidget_text,pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    //当到了一个更新周期就会用这个方法去更新小部件
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

//第一次创建小部件的时候就会调用这个方法
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

//当最后一个我们的小部件被删除后会被调用
    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    //作为一个广播。这个方法才是最重要的，在这个方法中，系统通过action来进行分发，我这里就做了一个被点击后改变文字
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action=intent.getAction();
        if (APP_WIDGET_CLICK.equals(action)){
            其实就是改变RemoveViews
            RemoteViews remoteViews=new RemoteViews(context.getPackageName(),R.layout.new_app_widget);
            remoteViews.setTextViewText(R.id.appwidget_text,"被点击了！");
            Intent clickIntent=new Intent();
            clickIntent.setAction(APP_WIDGET_CLICK);
            PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,clickIntent,0);
            remoteViews.setOnClickPendingIntent(R.id.appwidget_text,pendingIntent);
            AppWidgetManager manager=AppWidgetManager.getInstance(context);
            manager.updateAppWidget(new ComponentName(context,NewAppWidget.class),remoteViews);
        }
    }
}
~~~

## 小结
小部件的显示就是通过RemoveViews实现的，所以每次更新小部件，也是换一个RemoveViews，上面只是一些基本使用，我们可以具体到给RemoveViews里面的随便一个部件设置点击监听，上面没有写，我们看看这个PendingIntent，是个什么东西
## pendingIntent
看书，p228
## RemoveViews
如果你懂IPC，那你就会很好理解RemoveViews，我们可以猜想，我们的view肯定不是运行在我们的进程中的，这个是肯定的，所以我们用RemoveViews的时候直接findViewById是没有意义的，说明我们的插件和通知栏的view肯定是由系统的一个服务去维护，那其实相当于我们的软件其实是一个提供布局文件，提供资源文件的控制端，我们把展示布局文件和小部件要做的事情托管给了系统的那个服务。应该是这个意思。

那问题又来了，比如我们要给小部件的一个textView设置内容，按照ipc来说，那是不是我们的Binder要设置一个和textView设置内容一样的接口？然后系统的那个管理小部件的服务再复写这个接口，然后执行对应的方法去设置textView？这样一来不就可以实现了？事实上这样完全可以实现，但是会有问题，view的方法那么多，这样一来两边的重复代码会很多，而且大量的IPC会很耗费性能，那肯定想到设计模式吧？嗯，没错，既然view的方法那么多，我们不可能在系统的服务去写出对应的方法，那就在我们软件设置好这些命令，到系统只需要遍历执行，而且我们可以一次设置好很多命令，然后直接通过一次IPC通信，系统服务拿到后就直接遍历执行。这才对！如果还是不懂，那我们看看源码

从这个方法开始
~~~java
   remoteViews.setTextViewText(R.id.appwidget_text,"被点击了！");
~~~

看下去，没做什么事继续看
~~~java
   /**
     * Equivalent to calling {@link TextView#setText(CharSequence)}
     *
     * @param viewId The id of the view whose text should change
     * @param text The new text for the view
     */
    public void setTextViewText(int viewId, CharSequence text) {
        setCharSequence(viewId, "setText", text);
    }
~~~


这个方法中出现了个ReflectionAction
~~~java
   /**
     * Call a method taking one CharSequence on a view in the layout for this RemoteViews.
     *
     * @param viewId The id of the view on which to call the method.
     * @param methodName The name of the method to call.
     * @param value The value to pass to the method.
     */
    public void setCharSequence(int viewId, String methodName, CharSequence value) {
        //他把view的Id和view设置这个属性的方法名放进了他的构造函数，说不定后面要用到反射，其实看到 ReflectionAction你就知道是反射了！哈哈哈
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.CHAR_SEQUENCE, value));
    }
~~~

我再看看addAction()
~~~java
 /**
     * Add an action to be executed on the remote side when apply is called.
     *
     * @param a The action to add
     */
    private void addAction(Action a) {
        if (hasLandscapeAndPortraitLayouts()) {
            throw new RuntimeException("RemoteViews specifying separate landscape and portrait" +
                    " layouts cannot be modified. Instead, fully configure the landscape and" +
                    " portrait layouts individually before constructing the combined layout.");
        }
        if (mActions == null) {
            mActions = new ArrayList<Action>();
        }
        mActions.add(a);

        // update the memory usage stats
        a.updateMemoryUsageEstimate(mMemoryUsageCounter);
    }

~~~

你看，果然是放到一个集合里面。然后一次性把所有的Action传给系统吧！那我们系统拿到Action后干了些什么，那部分的源码我不知道在哪里找，这里就直接按照书上写的，通知栏的removeView是NotificationManager管理，小部件是AppWidgetManager管理，这个两个其实内部有服务都是SystemServer进程的对应服务，所以就有了跨进程通信，他们通过Binder拿到对应的Action集合后会遍历调用Action的这个方法
~~~java

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final View view = root.findViewById(viewId);
            if (view == null) return;

            Class<?> param = getParameterType();
            if (param == null) {
                throw new ActionException("bad type: " + this.type);
            }

            try {
            //反射执行view对应的方法就完成了一次刷新
                getMethod(view, this.methodName, param).invoke(view, wrapArg(this.value));
            } catch (ActionException e) {
                throw e;
            } catch (Exception ex) {
                throw new ActionException(ex);
            }
        }
~~~

所以说，你在软件设置了view的属性，在小部件其实不是同步的。只是执行的快，所以感觉不出来。至于为什么要反射？，是可以直接findViewById的，因为你看上面确实是findViewById了一次，但是要强转，就要确定View类型，就很麻烦。其实有些View的属性设置方法参数需要两个，就不能用这个反射了，就是通过获取到View的实例来改，这个时候就不能用普通的Action来，因为不知道View类型，不能转换，解决方法简单，直接弄一个特别的Action就好。
 
 ## 实践
 我们模拟一下我们的软件将我们的布局文件给系统小部件或者通知栏服务然后显示在桌面或者通知栏的过程
 
 我们的客户端
 ~~~java
 public class MainActivity extends AppCompatActivity {
    private static final String REMOVE_VIEW_ACTION="com.ba.action.ADD_REMOVE_VIEW";
    int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button=(Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoteViews remoteViews=new RemoteViews(getPackageName(),R.layout.remove_views_layout);
                remoteViews.setTextViewText(R.id.text, i+"");
                Intent intent=new Intent(REMOVE_VIEW_ACTION);
                intent.putExtra(REMOVE_VIEW_ACTION,remoteViews);
                sendBroadcast(intent);
                i++;
            }
        });
    }
}
 ~~~
 
 模拟系统的管理View的服务
 ~~~java
 public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String REMOVE_VIEW_ACTION="com.ba.action.ADD_REMOVE_VIEW";
   


    MyReceiver receiver;
    //你必须声明是哪种布局，不然addView会出问题
     LinearLayout viewGroup;
    RemoteViews remoteViews;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewGroup= findViewById(R.id.viewGroup);

        receiver=new MyReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
              remoteViews=intent.getParcelableExtra(REMOVE_VIEW_ACTION);
                View view1 =remoteViews.apply(context,viewGroup);
                Log.d(TAG, "onReceive: v:"+view1);
                viewGroup.addView(view1);
                Log.d(TAG, "onReceive: 收到更新通知");
            }
        };

        IntentFilter intentFilter=new IntentFilter(REMOVE_VIEW_ACTION);
        registerReceiver(receiver,intentFilter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
 ~~~
 ## 总结
 就是进程通信而已，但是比较特殊的就是，共享了资源文件。
 

