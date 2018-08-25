[TOC]

# 使用Okhttp3保存Cookie实现自动登入

## 先说两句

> 实现原理非常简单
>
> 和网上一样，先讲讲本地和后台是怎样进行账户登入的吧，由于HTTP协议无状态的特性，在以前你访问互联网需要登入网页时需要登入啊什么的，你的账号信息是没有办法在后台保存的所以就产生了Cookie技术，Cookie是将这些信息保存到客户端，在你下一次访问时就将这些信息一起和请求提交到后台，可以直接将账号密码直接放在Cookie里面，不安全吧，所以后面又出现一种新的技术。
>
> Session，这是一种服务端技术。服务端将数据保存在Session中，仅仅将此Session的ID发送给客户端，客户端在请求该站点的时候，只需要将Cookie中的SESSIONID这个数据发送给服务端即可。这样一来就避免了用户信息泄露的尴尬。
>
> 简单的说，Cookie就是一段数据而已

## 安卓中使用Cookie思路分析

> Okhttp中写了个Cookie类来保存Cookie数据，就把数据和对象进行了映射咯，你也可以理解为，把数据存到Cookie这个类中进行管理
>
> Cookie在我目前接触到的两种数据
>
> - 用来记录后台返回的用户ID
>
>   > 这个ID是用来区别不同账户的，这个Cookie用来给后台确认这个账户是否可以自动登入，所以这个cookie需要持久化，不用担心怎样去判断这个Cookie需不需要持久化，因为在后台返回时这个Cookie会有一个persistent属性，就是用来确定是不是需要持久化的，后台喜欢存什么是后台的事情
>
> - 用来辨识当前Session的sessionID
>
>   > 这个cookie里面的Id是用来记录这一次会话中的内容的一些信息，由于Session每一次的ID是不一样的，在程序关闭后，这个回话就结束了，这个cookie就没有存在的必要了，所以这个Cookie是不需要持久化的。

## 客户端和后台使用Cookie实现自动登入流程
cookie在客户端，我们只负责存，合适的时候取出，中间是不进行处理的！

> - 第一次在客户端发起请求
>
>   > - 场景：手机内存中没有存有自动登入的Cookie，刚刚打开客户端就也没有SessionId
>   >
>   > - 由于手机内存没有自动登入的Cookie，所以要提交登入账户和密码，然后会返回一个自动登入的Cookie和这一次的SessionId
>   >
>   > - 自动登入的Cookie和SessionID的Cookie里面会有一些属性，比较重要的属性就是
>   >
>   >   > - persistent 这个Cookie是否需要持久化
>   >   > - expiresAt 这个Cookie的有效期是不是过期了，过期了后台是不会给你处理的
>   >   > - 是否需要加入请求头部的属性,这个是由多个属性决定的，okhttp3里面的Cookie类会有一个currentCookie.matches(url)方法来判断的
>   >
>   > - 然后持久化自动登入的Cookie，将自动登入的Cookie和SessionID放在一个作为缓冲区的对象里面
>
> - 在上一次发起请求后没有退出客户端的前提下，第二次在客户端发起请求
>
>   > 由于上一次后台已经返回了自动登入的Cookie和SessionID，在缓冲区也已经获取到了，你发起请求的时候其实会将自动登入的Cookie和SessionId一起放在请求头部的(前提是符合一些条件，下面看代码再说)，所以后台就不会返回Cookie了
>
> - 退出客户端，再打开客户端
>
>   > - 场景：本地内存已经有了自动登入的Cookie
>   > - 发起请求时要将自动登入的Cookie放在头部，然后直接发起登入请求，不上传账户和密码，后台判断是否放在请求头的cookie如果是自动登入的Cookie，后台就返回一个SessionID，和返回登入成功，然后后台就不会返回自动登入的Cookie了

## 客户端保持登入状态

这个其实是客户端在登入的时候就决定了，当前用户是否是在线状态，所以说是否在线是客户端处理的，和后台其实没什么关系，后台只是帮你远程判断这个账户是否存在而已。比如说，在你不提交账户和密码就直接发起请求，后台就会用你放在请求头的Cookie来判断你是否被允许不输入账户和密码而使用这个Cookie来登入，你登入时本来用的是账户和密码，现在用的是Cookie

## 退出

只要把本地持久化的Cookie删除了，然后改了客户端里面用来记录登入状态的变量，然后通知后台删除了自动化登入的界面就可以了

---

那重点就是过滤出那个需要持久化的Cookie，然后持久化这个Cookie，往下看呗

## 使用Okhttp3保存Cookie

### 先来看看okhttp最基础的使用

~~~java
//get请求
OkHttpClient okHttpClient = new OkHttpClient();
//同步发起
        //通过Builder辅助类构建一个Request对象
        Request request = new Request.Builder()
          .get()
          .url(url)
          .build();

        //通过同步执行获取一个Response对象，也可以发起请求并且提供回调接口来处理请求发起后的逻辑
        Response response = okHttpClient.newCall(request).execute();
        //判断响应是否成功,如果成功的话,响应的内容会放在response.body()中
        if (response.isSuccessful()) {
            //字符串类型
            Log.i(TAG, "getData: " + response.body().string());
            //字节数组类型
            Log.i(TAG, "getData: " + response.body().bytes());
            //字节流类型
            Log.i(TAG, "getData: " + response.body().byteStream());
            //字符流类型
            Log.i(TAG, "getData: " + response.body().charStream());
        }
~~~

**是不是发现，就是简单的发起了一个获取数据的请求，但是并没有很明显的看到有对cookie处理的设置？确实是没有的，再来看看怎样发起会对Cookie进行处理**

---

### 设置Okhttp对cookie进行处理

设置对cookie的处理是在OkhttpClient设置的，所以发起http请求方法是一样的，看看怎么设置咯

~~~java
OkHttpClient client = new OkHttpClient.Builder()
    .cookieJar(new CookieJar(){
      ...
    }) //这里传一个Cookiejar接口或者其子类，设置好后Okhppt就能对cookie进行处理了。具体怎么处理，看看下面
    .build();
~~~

---

CookieJar是一个接口，显然就是给OkhttpClient进行回调的，那就看看接口中的方法

~~~java
  /**
   * Saves {@code cookies} from an HTTP response to this store according to this jar's policy.
   *
   * <p>Note that this method may be called a second time for a single HTTP response if the response
   * includes a trailer. For this obscure HTTP feature, {@code cookies} contains only the trailer's
   * cookies.
   */
  void saveFromResponse(HttpUrl url, List<Cookie> cookies);

  /**
   * Load cookies from the jar for an HTTP request to {@code url}. This method returns a possibly
   * empty list of cookies for the network request.
   *
   * <p>Simple implementations will return the accepted cookies that have not yet expired and that
   * {@linkplain Cookie#matches match} {@code url}.
   */
  List<Cookie> loadForRequest(HttpUrl url);
~~~

---

只有两个回调方法，可以直接翻译注释，

先看看saveFromResponse()这个方法会在什么你发起请求后得到respose时被调用，意思就是说，其实返回的数据都是被封装在response里面的，得到response后就提取出cookie，然后调用这个方法，你具体要怎么处理这个cookie就怎么处理一般是缓存Cookie还有进行cookie的持久化，

再看看loadForRequest()这个方法，先看看返回值，是一个cookie的集合，在你发起请求时需要将cookie放在请求的头部，你返回的这个cookie就是用来加入到请求头部的，意思就是怎么添加到请求头是不用我们管，只需要将你要提交的cookie返回就好

### 实现在两个回调方法中处理cookie的流程

okhttp把怎么处理cookie给程序员自己来定义，这样就可以定制化的对cookie进行处理了，其实也就是在上面的两个回调方法中该干嘛干嘛！！看看快快送里面是在什么时候处理这两个方法的，写一个CookieJar的子类来自定义在两个回调种处理Cookie的方法


~~~java
 public PersistentCookieJar(SetCookieCache cache, SharedPrefsCookiePersistor persistor) {
        this.cache = cache; //这个类你不用管先，只要知道是用来存储cookie的对象
        this.persistor = persistor; //也是不用管先，知道用来将cookie持久化的类，

   		//从内存中读取用户ID，然后存到缓存对象中(有就加载嘛)
        this.cache.addAll(persistor.loadAll());
    }

	//发起请求后自动调用这个方法
	@Override
    synchronized public List<Cookie> loadForRequest(HttpUrl url) {
      //用来放实时更新Cookie的垃圾箱(集合)
        List<Cookie> removedCookies = new ArrayList<>();
      //用来记录当前需要提交的Cookie
        List<Cookie> validCookies = new ArrayList<>();

      //这个cache对象是用来保存Cookie的，所以要读取，然后加载到请求头中
        for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); ) {
            Cookie currentCookie = it.next();
          
          //这里进行了一个判断，判断上一次保存的Cookie里面的用户ID和sessionID是不是已经过期了，是就直接删除了，怎么判断也不看先
            if (isCookieExpired(currentCookie)) {
                removedCookies.add(currentCookie);
                it.remove();

            //没过期就继续判断，是不是需要加到请求头中
            //具体怎么判断，我们不用管，okhttp封装好的matches()方法会处理
            } else if (currentCookie.matches(url)) {
                validCookies.add(currentCookie);
            }
        }

      //将过期的cookie删除
        persistor.removeAll(removedCookies);
      
      //返回要加入到请求头里面的Cookie
        return validCookies;
    }


//请求完后会自动调用这个方法，然后你只需要在这里保存该保存的cookie，持久化该持久化的cookie就好
	@Override
    synchronized public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cache.addAll(cookies); //这个cache对象是用来保存Cookie的
        persistor.saveAll(cookies); //这个persistor对象是用来持久化Cookie的
    }
~~~

---

看看怎么判断过期吧，也就是简单判断一下Cookie里面的expiresAt属性和当前系统时间的关系

~~~java
   private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }
~~~

---

#### cookie处理之在运行内存中存储cookie

首先你要明白，这个内存是是指的是运行程序运行中的内存！其实就是放在集合中啦，也就是弄一个cache，最前的Cookie思路分析里面有说，自己翻上去看看

我们就可以写一个CookieCache类来进行缓存一次cookie，这里我就拿快快送的代码来分析一下

~~~java
//抽象了个接口，那就看看这两个方法
public interface CookieCache extends Iterable<Cookie> {

    /**
     * Add all the new cookies to the session, existing cookies will be overwritten.
     *
     * @param cookies
     */
    void addAll(Collection<Cookie> cookies);

    /**
     * Clear all the cookies from the session.
     * 清除这一次请求的cookie，这就更容易理解了，每一次提交的数据的Cookie可能都是不一样的
     * 所以要求是最新的Cookie
     */
    void clear();
}
~~~

---

无非就是保存cookie和删除cookie，接下来看看子类怎么实现保存和删除

~~~java
//是一个对临时缓存的cookies进行操作的类，他是用hashSet存一个List集合，集合里面再存Cookies
public class SetCookieCache implements CookieCache {

    private Set<IdentifiableCookie> cookies;

    //用哈希表来存储，hashSet的特点就是，不能存储重复对象，用hashSet的原因就是读取对象效率高
  	//而且一次对话的SessionID是一样的，为了不重复存储，你用List的话还是要自己写一个判断是否相同的方法
  	//在代码复杂度上是差不多的，所以性能上考虑还是选HashSet比较好
    // 所以在使用hashset的时候必须复写equals和hashCode方法
    public SetCookieCache() {
        cookies = new HashSet<>();
    }

    //前面说了Cookie类是okhppt3自己提供的，然后hashSet原理是hashMap，存的key不能重复，你又不能直接改Cookie的代码，所以自己要写一个自定义可以区别不同Cookies对象的IdentifiableCookie类，就是在里面自己复写equals和hashCode方法，反正等下取出，处理cookie的也是你，你只要再从IdentifiableCookie实例里面取出真正的Cookie就好
    @Override
    public void addAll(Collection<Cookie> newCookies) {
        updateCookies(IdentifiableCookie.decorateAll(newCookies));
    }

    /**
     * All cookies will be added to the collection, already existing cookies will be overwritten by the new ones.
     *
     * @param cookies
     */
    private void updateCookies(Collection<IdentifiableCookie> cookies) {
        this.cookies.removeAll(cookies);
        this.cookies.addAll(cookies);
    }

  //清空缓存中原来的Cookies
    @Override
    public void clear() {
        cookies.clear();
    }

   
~~~

---

##### 小重点-为什么要用hashMap来存放Cookie？

> cookie在每一次发起请求的时候都要用到，每一次都要从cache对象里面拿cookie，为了保证对查询的快速响应，会将cookie放到HashSet或者是HashMap中，以提供快速查询

---

#### 将Cookie持久化

也还是保存cookie和删除cookie，但是是保存到内存中，接下来看看子类怎么实现保存和删除

使用的是SharedPreferences，这个也是使用键值对来存储数据的，所以要为Cookie生成键，SharePreference是存储普通的字符串，布尔等基本数据类型，不能直接存对象，所以要将对象序列化成二进制。具体的关于对象的序列化我就不再详解，可以翻翻对象序列化的笔记。

先看看快快送项目中的需要持久化的实体类是怎么写的，其实快快送项目中想要序列化的是Cookie这个类的实例，但是这个类是okhttp框架里面提供的，他是没有办法被复写的，所以写了个SerializableCookie，实现Cookie能被序列化，这种嫁接的方式，上面也有，我觉得应该是一种设计模式来的

~~~java
//我删除了一部分代码，因为接下来的才是序列化对象的主要方法
public class SerializableCookie implements Serializable {
   ...

     //声明为transient，意味这个Cookie不能被序列化，但是我们现在就是要序列化这个对象
     //按照序列化对象的常规写法就要写出下面两个方法了
    private transient Cookie cookie;
  
    private static long NON_VALID_EXPIRES_AT = -1L;

  //将cookie中的字段序列化
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(cookie.name());
        out.writeObject(cookie.value());
        out.writeLong(cookie.persistent() ? cookie.expiresAt() : NON_VALID_EXPIRES_AT);
        out.writeObject(cookie.domain());
        out.writeObject(cookie.path());
        out.writeBoolean(cookie.secure());
        out.writeBoolean(cookie.httpOnly());
        out.writeBoolean(cookie.hostOnly());
    }

  //将流中取出cookie的字段，用来new一个cookie
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Cookie.Builder builder = new Cookie.Builder();

        builder.name((String) in.readObject());

        builder.value((String) in.readObject());

        long expiresAt = in.readLong();
        if (expiresAt != NON_VALID_EXPIRES_AT) {
            builder.expiresAt(expiresAt);
        }

        final String domain = (String) in.readObject();
        builder.domain(domain);

        builder.path((String) in.readObject());

        if (in.readBoolean())
            builder.secure();

        if (in.readBoolean())
            builder.httpOnly();

        if (in.readBoolean())
            builder.hostOnlyDomain(domain);

        cookie = builder.build();
    }

}
  
~~~

---

上面也没什么难度，现在看看是怎样实现序列化的，这个项目把代码这部分放在了SerializableCookie类中，就是上面代码的那个类啦，这个类其实就是个帮Cookie实现序列化的类，上面是把Cookie实现了可序列化，那具体的序列化操作呢？看下面的代码

~~~java
 //序列化对象， 返回值是String，字符串！因为用的是sharepreference存储好像不支持数组，所以就序列化成字符串
    public String encode(Cookie cookie) {
        this.cookie = cookie;
		
      //这里采用的方法是先序列化成字节数组，然后转成字符串
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;

        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            Log.d("wwwww", "IOException in encodeCookie", e);
            return null;
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    Log.d("wwwww", "Stream not closed in encodeCookie", e);
                }
            }
        }
      
      //这里去调用了byteArrayToHexString方法，这个方法就是将字节数组转成字符串的
        return byteArrayToHexString(byteArrayOutputStream.toByteArray());
    }

	//但是是转成16进制的字符串，这里是因为序列化的时候的编码问题，直接存字符串可能会乱码
	//查了很久，应该是这个意思，所以就记住是这样写就好了
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }

    //反序列化对象
    public Cookie decode(String encodedCookie) {

      //将16进制字符串转成字节数组
        byte[] bytes = hexStringToByteArray(encodedCookie);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                bytes);

        Cookie cookie = null;
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SerializableCookie) objectInputStream.readObject()).cookie;
        } catch (IOException e) {
            Log.d(TAG, "IOException in decodeCookie", e);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "ClassNotFoundException in decodeCookie", e);
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, "Stream not closed in decodeCookie", e);
                }
            }
        }
        return cookie;
    }

//将16进制字符串转成字节数组，具体算法不用看的
    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

~~~

---

## 自动管理Cookie结束

在前面，简单的说，我们设置了一个回调接口给Okhttp，让这个框架在发起请求，接收数据时能在本地实现对cookie的自动化管理，这个自动化管理的具体实现是需要我们自己写的。其实会管理到很多Cookie，但是需要持久化的就一个自动登入的Cookie，上面也做了处理。

## 总结

就是用一个特殊的Cookie来代替用户名和密码实现登入而已，具体判断是否是登入状态其实是客户端本地记录的，怎么记录？在发起登入请求的时候记录呗，后台返回说登入成功就记录当前是登入状态咯，上面的代码其实是可以一直用的，你只需要了解里面干了什么就好。

## 实战
项目地址：https://github.com/BACodeLab/AutoLogin
需求；实现自动登录功能

### 准备工作：
+ 怎么自动化管理Cookie的类其实就是上面的东西，可以直接用，项目里面对应的包是：com.example.ba.jpushdemo.cookie_tool;
+ 密码加密的代码在项目里面的包是： com.example.ba.jpushdemo.encryption_tool;
+ 上面两个你去上面的项目下载，然后放到你项目里面
+ okhttp库自己导入
+ 权限自己声明

### 实例一个全局的Okhtpp对象
在接下来的这个软件的网络请求都由这个对象来发起（这样才能每次发起请求都带Cookie），我懒得写单例，就放在了Application里面
~~~java
import okhttp3.OkHttpClient;

/**
 * Created by BA on 2018/6/20 0020.
 */

public class MyApplication extends Application {
    public static OkHttpClient client;
    @Override
    public void onCreate() {
        super.onCreate();

        //设置cookie保持自动登陆
        PersistentCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.cookieJar(cookieJar);
        client = builder.build();

    }
}
~~~

### Activity布局
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.example.ba.jpushdemo.MainActivity">

 <Button
     android:id="@+id/a"
     android:layout_width="wrap_content"
     android:layout_height="wrap_content"
     android:text="手动登录"/>

    <Button
    android:id="@+id/b"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="自动登录"/>

    <Button
        android:id="@+id/c"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="清除Cookie"/>
</LinearLayout>
~~~

### Activity
~~~java
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.ba.jpushdemo.cookie_tool.PersistentCookieJar;
import com.example.ba.jpushdemo.encryption_tool.EncryptionFactory;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    String IP = "http://120.24.95.57";
    /**
     * The constant LOGIN.登录接口，这个是我们点餐系统的登录接口，这里只是给你测试* 而已，别外传
     * 参数：status:1 一家厨房开始接单，status:0 一家厨房拒绝接单
     * 返回：失败时返回:{"status":1,"message":"厨房状态修改失败"} ，成功时返回:{"status":0,"message":"修改成功!"}
     */
    String LOGIN = IP + "/order/index.php/Android/Login/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button aLogin=(Button)findViewById(R.id.a);
        aLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        Button bLogin=(Button)findViewById(R.id.b);
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoLogin();
            }
        });

        Button c=(Button)findViewById(R.id.c);
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersistentCookieJar persistentCookieJar=(PersistentCookieJar) MyApplication.client.cookieJar();
                persistentCookieJar.clear();
                Log.d(TAG, "onClick: 清除成功");
            }
        });

    }

    /**
     * Login.
     */
    public void login() {
      

        /*
         * 加密，这里用MD5,还有一种是SHA，这里没有用到，加密方法自己选
         */
        String hexPasswordText = new EncryptionFactory().getClient(EncryptionFactory.TYPE_MD5).getResult("123456");

        FormBody body = new FormBody.Builder()
                .add("mobile", "13729358695")
                .add("password", hexPasswordText)
                .add("RegistrationID", "13065ffa4e5bc7d1172") //这个参数你不用管，我们某个功能需要而已
                .build();

        Request request = new Request.Builder()
                .post(body)
                .url(LOGIN)
                .build();

        MyApplication.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //可能是网络问题，服务器问题，你可以判断后告诉用户
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            //我只是简单打印，实际开发你需要解析出数据，然后做出相应的处理
                String text = response.body().string();

                Log.d(TAG, "onResponse: " + text);
            }
        });

    }


//自动登入一般不用传参数，直接访问就好，因为你如果请求是带cookie的，服务器会让你自动登录，否则不给登录
    public void autoLogin() {
        FormBody body = new FormBody.Builder()
                .build();
        Request request = new Request.Builder()
                .post(body)
                .url(LOGIN)
                .build();
        MyApplication.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String text=response.body().string();
                Log.d(TAG, "onResponse: "+text);
            }
        });

    }
}

~~~





## 总结
自动登录不仅可以 Cookie实现，还能用token，这里就介绍Cookie，我们可以看到，其实向后台发起网络请求，后台确认你身份后会返回数据给你，具体怎么解析数据（json），你就要自己查了，这很简单的。其实和后台交互就是合适的时候向后台请求数据，拿到数据后我们自己处理。