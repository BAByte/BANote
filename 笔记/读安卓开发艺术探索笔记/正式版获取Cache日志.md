[toc]
# 正式版获取Cache日志
Android应用正式版后，测试员测试或者是用户使用时程序发生崩溃后，开发人员无法进行异常日志的查看，最好的解决办法就是将异常捕捉到本地，下一次软件打开的时候再上传到服务器。
## 具体实现
### 日志文件的管理
~~~java
/**
 * Created by BA on 2018/8/5 0005.配置接口，我们需要一个存着手机，程序，后台服务器地址，本地日志文件保存地址的类，这个类就是
 */

public class ConfigInf {
    private static final String TAG = "ConfigInf";
    //上传服务器的IP
    private String IP;
    //详细地址
    private String upUri;
    //本地位置
    private String localLocation;
    //手机厂商
    private String Manufacturer;
    //手机型号
    private String model;
    //安卓版本
    private String OSVersion;
    //app名字
    private String appName;
    //app版本
    private String appVersion;
    private Context context;

    public ConfigInf(Context context) {
        this.context=context;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            //获取本地缓存
            localLocation = context.getExternalCacheDir() + "/crash/";
            Log.d(TAG, "ConfigInf: " + localLocation);
            appName = packageInfo.packageName;
            appVersion = packageInfo.versionName;
            OSVersion = Build.VERSION.SDK_INT + "";
            Manufacturer = Build.MANUFACTURER;
            model = Build.MODEL;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getUpUri() {
        return upUri;
    }

    public void setUpUri(String upUri) {
        this.upUri = upUri;
    }

    public String getLocalLocation() {
        return localLocation;
    }

    public void setLocalLocation(String localLocation) {
        this.localLocation = localLocation;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        Manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOSVersion() {
        return OSVersion;
    }

    public void setOSVersion(String OSVersion) {
        this.OSVersion = OSVersion;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return
                "软件包名=" + appName + '\n' +
                        "软件版本=" + appVersion + '\n' +
                        "安卓版本=" + OSVersion + '\n' +
                        "手机厂商=" + Manufacturer + '\n' +
                        "手机型号=" + model + '\n' + '\n'
                ;

    }
}
~~~

---

~~~java
/**
 * Created by BA on 2018/8/6 0006.日志管理类，这个类负责日志的存放和上传和删除，读取
 */

public class MyLogManager {
    private static final String TAG = "MyLogManager";
    public static void init(ConfigInf configInf){
        //一初始化就检测有没有日志没有上传的
        File fileDir = new File(configInf.getLocalLocation());
        if (fileDir.exists()) {
            save2Remote(configInf,null);
        }
    }
    
    public static void save2Local(ConfigInf configInf, Throwable ex){
        new SaveLog2local().save(configInf,ex);
    }

    public static void deleteLog(final ConfigInf configInf){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File dir = new File(configInf.getLocalLocation());//清空文件夹
                File[] files = dir.listFiles();
                if(null != files){
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        boolean b = file.delete();
                    }
                }
            }
        }).start();
    }

    public static  void save2Remote(ConfigInf configInf, Throwable ex){
        new SaveLog2Remote().save(configInf,ex);
    }

    public static File[] getLog(ConfigInf configInf){
        File dir = new File(configInf.getLocalLocation());//清空文件夹
        File[] files = dir.listFiles();
        return files;
    }
}

~~~

---

~~~java
/**
 * Created by BA on 2018/8/5 0005.保存日志到本地的具体实现类
 */

public class SaveLog2local implements SaveLogInf {
    private static final String TAG = "SaveLog2local";

    @Override
    public void save(ConfigInf configInf, Throwable ex) {
        long currentTimeMillis = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss").format(new Date(currentTimeMillis));
        File fileDir = new File(configInf.getLocalLocation());
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }


        File file = new File(configInf.getLocalLocation() + configInf.getAppName()+":" + configInf.getAppVersion()+":" + time + ".log");
        try {
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            printWriter.println("时间=" + time);
            printWriter.print(configInf.toString());
            ex.printStackTrace(printWriter);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
~~~

---

~~~java
/**
 * Created by BA on 2018/8/5 0005.上传到服务器，但是不是马上上传，是定时上传
 * 上传日志文件的具体实现类
 */

public class SaveLog2Remote implements SaveLogInf{
    private static final String TAG = "SaveLog2Remote";
    @Override
    public void save(ConfigInf configInf, Throwable ex) {
        Log.d(TAG, "save: 上传了日志"+MyLogManager.getLog(configInf).length);
        //上传后把日志删除
        MyLogManager.deleteLog(configInf);
    }
}
~~~


### 捕捉程序异常
关于异常保存到本地的类上面已经搞定了，现在的工作就是，在异常发生的时候进行捕捉然后保存，而异常那么多，我们需要进行全局捕捉，安卓是有提供方法的，我们叫异常处理器
~~~java
/**
 * Created by BA on 2018/8/6 0006.你需要实现 Thread.UncaughtExceptionHandler接口
 */

public abstract class CrashHandlerInf implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandlerInf";
    ConfigInf configInf;
    Thread.UncaughtExceptionHandler systemHandler;
    AlertDialog dialog;

    public  void init(ConfigInf configInf, AlertDialog alertDialog){
        //初始化日志管理器
        MyLogManager.init(configInf);
        this.configInf = configInf;
        this.dialog = alertDialog;
        //看看系统有没有自己定义默认的log处理器
        systemHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将我们的处理器设置
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    public  void init(ConfigInf configInf){
        init(configInf,null);
    }


    //复写该方法，当程序发生异常时会回调该方法，我们可以在这里进行文件的保存，但是下面代码你没有看到，因为这个类是抽象类，我用来做父类的，具体实现不是这里
    //这里只是去判断系统有没有自己的处理类，有就交给系统默认处理器来处理，我们可以尝试弹出个提示框再退出软件，但是我测试中是大部分给了系统默认处理器处理
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (systemHandler != null) {
            Log.d(TAG, "uncaughtException: 系统");
            systemHandler.uncaughtException(t, e);
        } else {
            Log.d(TAG, "uncaughtException: 非系统");
            if (dialog == null) {
                new DefaultDialog().show(configInf.getContext());
            } else {
                dialog.show();
            }
        }
    }
}
~~~

---

~~~java
/**
 * Created by BA on 2018/8/6 0006.具体实现
 */

public class DefaultCrashHandler extends CrashHandlerInf{
    private static CrashHandlerInf handler = new DefaultCrashHandler();

    private DefaultCrashHandler(){
    }

    public static CrashHandlerInf getInstance() {
        return handler;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        MyLogManager.save2Local(configInf,e);//保存到本地
        super.uncaughtException(t, e);
    }
}
~~~

### 让系统使用我们的处理器
上面写好了日志的管理器，异常处理器。并且把异常处理器和日志管理器组合一起写了完整的日至管理系统，那接下来就是让系统再发生异常的时候使用我们的日志管理系统了
~~~java
/**
 * Created by BA on 2018/8/6 0006.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandlerInf crashHandlerInf= DefaultCrashHandler.getInstance();
        //这里要设置上传的服务器地址，我没有设置是因为后台还没有
        ConfigInf configInf=new ConfigInf(this);
        crashHandlerInf.init(configInf);
    }
}
~~~