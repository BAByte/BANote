[TOC]

# 安卓异步消息处理

## 分析

> UI控件的操作，安卓中规定线程A只能操作在线程A中创建的Ui控件，但是在使用过程中比如，下载是个耗时任务，如果你在主线程中进行下载任务，后果就是：1.用户只能等下载完后才能操作软件   2.服务器还没反应过来，下载的代码已经执行完了，所以我们要开一个线程A专门用来下载，但是下载过程中需要在界面上显示进度条啊，如果进度条时是主线程创建的，这时你要是在线程A中刷新进度条，就会报错，那怎么办？？？
>
> 
>
> 异步消息处理主要用到4个类
>
> 主线程的处理耗时任务的UI管理员：Handler类
>
> > 这是一个专门用来给线程之间进行通信的工具
> >
> > 方法：
> >
> > + handleMessage(Message msg); 接收信息的方法
> > + sendMessage(); 发送信息的方法
> > + post(); 切换到主线程刷新ui的方法
> > + postDelayed（）; 延迟刷新ui的方法
>
> 消息：Message类
>
> 具有的成员变量
>
> 1. arg1 arg2 整数类型，是setData的低成本替代品。传递简单类型
>
>
> 2. Object 类型 obj
> 3. what  用户自定义的消息代码，这样接受者可以了解这个消息的信息。每个handler各自包含自己的消息代码，所以不用担心自定义的消息跟其他handlers有冲突。
>
> 消息队列：MessageQueue
>
> 取出消息的工具：Looper
>
> 当你实例化Handler的时候，Handler内部会实例化一个Looper，Looper被实例化后，Looper内部继续实例化一个MessageQueue。MessageQueue里面很显然就是用来存放消息的

---



## 子线程联系主线程

> 主线程有一个Handler实例，里面有接收Message的方法
>
> ```java
> final Handler handler = new Handler() {
>   //在这里接收信息
>     @Override
>     public void handleMessage(Message msg) {
>         Log.d("ssss", msg.arg1 + "");
>     }
> };
> ```

> 子线程有该Handler实例，用来给主线程发送Message
>
> ```Java
> //子线程
> new Thread(new Runnable() {
>     @Override
>     public void run() {
>         Message message = new Message();
>         message.arg1 = 11;//传输整型
>         message.obj = new Object();//这里是传输对象
>         handler.sendMessage(message);//发送信息
>     }
> }).start();
> ```

> 上面就实现了子线程给主线程发送消息的功能，但是，有没有发现是他们用的是同一个Handler，意思其实就是用Handler发送消息给Handler自己，对吧。再继续深入分析一下，Handler里面有一个Looper实例，该实例有一个Looper方法，是的，你没有看错就叫Looper，，，上面不是说MessageQueue也在Looper里面吗？然后消息又在MessageQueue里面，Looper就会一直调用Looper方法不断的从MessageQueue里面取出Message，Message里面又有一个存储地址变量，Looper会根据这个地址，然后将这个Message发送给对应的Hanlder .(其实这个地址就是Handler本身) 。哇，意思就是Hanlder实例将Message放到内部的Looper的MessgaeQueue中，然后Looper不断的取 ，取出来后又发给Handler自己，就是这样。

---



## 主线程联系子线程（麻烦）

不就是在主线程，用一个与子线程关联的Hanlder发消息，接收消息咯，但是哦！！！在安卓里面，如果实例化Handler没指定一个Looper与这个Handler绑定，系统会自动将这个Handler与创建的线程绑定，看代码！！

### 用SDK中的Handler线程

```java

/**{有和自己绑定的子线程的Handler}*/
    Handler thread_handler;

    /**{主线程的Handler}*/
    Handler handler;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("ssss", msg.arg1 + "");
            Message message = new Message();
            message.arg1 = 1;
            thread_handler.sendMessageDelayed(message, 1000);
        }
    };

  //已经和handler绑定的子线程
    HandlerThread handlerThread = new HandlerThread("handlerThread");
    handlerThread.start();

  //这里将子线程中的Looper与Handler绑定
    thread_handler = new Handler(handlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Log.d("ssss", msg.arg1 + "");
            Message message = new Message();
            message.arg1 = 2;
            message.what=1;
            handler.sendMessageDelayed(message, 1000);
        }
    };

    Button send=(Button)findViewById(R.id.send);
    send.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            handler.sendEmptyMessage(1);
        }
    });

    Button stop=(Button)findViewById(R.id.stop);
    stop.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            handler.removeMessages(1);
        }
    });
}
```



### 自定义有自己Handler的子线程

```java
    /**
     * {主线程的Handler}
     */
    Handler handler;

	//子线程
    MyThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d("ssss", msg.arg1 + "");
                Message message = new Message();
                message.arg1 = 1;
                thread.myHandler.sendMessageDelayed(message, 1000);
            }
        };

        thread = new MyThread(handler);
        thread.start();


        Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(1);
            }
        });

        Button stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeMessages(1);
            }
        });
    }

//自定义的子线程类
    class MyThread extends Thread {

        /**
         * 子线程自己的Handler
         */
        Handler myHandler;

        /**
         * 主线程的handler
         */
        Handler mainHanlder;

        public MyThread(Handler handler) {
            mainHanlder = handler;
        }

        @Override
        public void run() {
          //初始化Looper，这个线程有了自己的Looper，这样在这里创建的Handler就不会与主线程的ooper绑定了
            Looper.prepare();
            myHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.d("ssss", msg.arg1 + "");
                    Message message = new Message();
                    message.arg1 = 2;
                    message.what = 1;
                    mainHanlder.sendMessageDelayed(message, 1000);
                }
            };
          //开始不断读取
            Looper.loop();
        }
    }
}
```

---



## 用来在子线程中更新UI

~~~java
handler.post(new Runnable() {
    @Override
    public void run() {
      这里执行ui更新
    }
~~~



### 为什么要用Handler刷新UI

原因也很简单，多个线程同时控制一个ui，如果不加锁，会出现像卖票一样，多个地方卖出了同一个号码的票。如果加锁，在性能上又会有很大问题，所以安卓使用了一个Hanlder工具，专门用来刷新ui，一共有两种

第一种
~~~java
//主线程中定义
private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    mTestTV.setText("This is handleMessage");//更新UI
                    break;
            }
        }
    };
    
//子线程发消息，跳回主线程
new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);//在子线程有一段耗时操作,比如请求网络
                    mHandler.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
~~~

第二种
~~~java
private Handler mHandler;//全局变量
@Override
protected void onCreate(Bundle savedInstanceState) {
    mHandler = new Handler();
    new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);//在子线程有一段耗时操作,比如请求网络
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mTestTV.setText("This is post");//更新UI
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
}
~~~

其他更新Ui的方法
~~~java
//这个是Activity中的方法
runOnUiThread(new Runnable() {
    @Override
    public void run() {
        
    }
});
~~~

### Handler最常用的场景

作为计时器

每隔多少秒发送一次消息即可

作为延迟发送消息

设置延迟多少秒发送即可

### 定义内部类Handler的时候需要注意的地方

需要把自己的Handler类定义为内部类，具体看看这篇文章：http://blog.csdn.net/darkerfans/article/details/51149583

~~~java
/**这里会报警告：
     *  关于Android“This Handler class should be static or leaks might occur”警告的处理方法
     * 出现警告的原因？
     *  ADT20以后加入了一条新的检查规则：确保类内部的handler不含有对外部类的隐式引用
     *  为什么Handle要定义成静态的？
     * static class 就是为了断开MyHandler  和外部class的联系，
     *  让内部类和外部类不产生任何联系就是static class的目的，防止GC时因为handle有引用导致，    activity不能被回收，循环泄漏
     *  第一种解决方案： @SuppressLint("HandlerLeak")加入注释（不推荐）
     *  第二种解决方案：把Handler定义成static，然后用post方法把Runnable对象传送到主线程
     *  private static Handler handler;handle.post();适用于只有一个消息要发送的情形，如果有多个消息要发送可以采用第三种方法
     *  第三种解决方案（推荐）这里我们可以采用弱引用的方式来解决问题，我们先定义一个static的内部类MyHandler，然后让它持有Activity的弱引用，这样问题就得到了解决
     * 你需要修复您的处理程序声明如下：声明的处理程序作为一个静态类；
     * 在外部类中，实例化一个WeakReference类的外把这个对象处理程序当你实例化处理；
     * 使所有引用成员外部类使用WeakReference对象。
     */
    MyHandler handler = new MyHandler(this);
    static class MyHandler extends Handler{
        //注意下面的“”类是MyHandler类所在的外部类，即所在的activity或者fragment
        WeakReference<MilkListFragment> mFragment;
        MyHandler(MilkListFragment fragment) { mFragment = new WeakReference<MilkListFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            MilkListFragment fragment=mFragment.get();
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    //这里可以改变activity或者fragment中的UI控件的状态
                    fragment.adapter.notifyDataSetChanged();
                    fragment.mListView.onRefreshComplete();
                    break;
                case 2:
                    //这里可以改变activity或者fragment中的UI控件的状态
                    fragment.mListView.onRefreshComplete();
                    break;
            }
        }
    }
~~~



---



## 最常用的异步消息处理方式(AysncTask)

> 该类很好的封装了子线程切换到主线程的方法，使用时要继承该类
>
> ```java
> //第一个参数是你等下在耗时任务中将要用的数据的类型，第二个在你从子线程切换到主线程时想要从子线程带什么类型的数据给主线程，第三个声明是耗时任务结束后返回值的类型，根据需求指定
> public class DownloadTask extends AsyncTask<String, Integer, Integer> 
> ```
>
> 主要的方法
>
> ~~~java
> //该方法默认开启线程，可以在里面进行耗时任务,
> protected Integer doInBackground(String... params){}
> ~~~
>
> 

> ```java
> //当在上面的方法调用了父类的publishProgress();方法后，会自动调用下面这个方法
> //意思就是publishProgress();告诉程序要切换到主线程，下面的方法是主线程调用的，可以控制UI
> protected void onProgressUpdate(Integer... values){}
> ```

> ```java
> //这个方法在doInBackground方法结束后会自动调用该方法，这个方法也是主线程调用的
> protected void onPostExecute(Integer integer) {}
> ```

## AsyncTask的线程池
AsyncTask提供了两种线程池

1．    THREAD_POOL_EXECUTOR, 异步线程池
2．    SERIAL_EXECUTOR，同步线程池

那我们怎么选呢？上面我们使用的时候也没有选什么线程池啊，在使用的时候我们是这样的

~~~java
 AsyncTask a=new MyAsncTast();
 a.execute(0);
~~~
上面，是默认的使用方法，该方法默认使用了SERIAL_EXECUTOR线程池，同步线程池，其实就是顺序用线程执行任务，
~~~java
asynct.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
~~~
上面是使用了异步线程池，也就是找出有空闲的线程去执行，都不空闲就加到队列中，队列满了就新建线程，THREAD_POOL_EXECUTOR如果添加的任务过多，没有及时处理的话，会导致程序崩溃，它的队列size是128；它的调度规则是核心池大小，队列大小，以及最大线程数和异常处理Handler来决定的。线程池还是用默认的就可以满足了

> 总结起来就是，在doInBackground里面有线程，当需要根据里面的状态刷新UI时，就调用publishProgress();这个方法通知系统你要刷新控件，然后系统自动用主线程调用onProgressUpdate这个方法，你可以在这里刷新控件了，当耗时任务执行完成后会自动用主线程调用onPostExecute这个方法，通知你子线程结束