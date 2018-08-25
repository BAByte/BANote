[TOC]

# EventBus的使用以及源码分析

## 先说两句

> 这个EventBus一直听说非常的牛逼，说好的要学，但是从暑假开始就一直拖着，就是懒呗，项目中有用到，不得不学，一开始还抱着那种感觉很难的心态，没想到，没想到，好简单啊！！！！草鸡简单！！！这篇笔记是看了这个大神的文章：**http://blog.csdn.net/itachi85/article/details/52205464**

## 为什么要用?

> EventBus是一款针对Android优化的发布/订阅事件总线。简化了应用程序内各组件间、组件与后台线程间的通信。优点是开销小，代码更优雅，以及将发送者和接收者解耦。如果Activity和Activity进行交互还好说，如果Fragment和Fragment进行交互着实令人头疼，我们会使用广播来处理，但是使用广播稍显麻烦并且效率也不高，如果传递的数据是实体类需要序列化，那么很显然成本会有点高。今天我们就来学习下EventBus3.0的使用方法。

##EventBus的三要素

来自 http://blog.csdn.net/itachi85/article/details/52205464

EventBus有三个主要的元素需要我们先了解一下：

- Event：事件，可以是任意类型的对象。
- Subscriber：事件订阅者，在EventBus3.0之前消息处理的方法只能限定于onEvent、onEventMainThread、onEventBackgroundThread和onEventAsync，他们分别代表四种线程模型。而在EventBus3.0之后，事件处理的方法可以随便取名，但是需要添加一个注解@Subscribe，并且要指定线程模型（默认为POSTING），四种线程模型下面会讲到。
- Publisher：事件发布者，可以在任意线程任意位置发送事件，直接调用EventBus的post(Object)方法。可以自己实例化EventBus对象，但一般使用EventBus.getDefault()就好了，根据post函数参数的类型，会自动调用订阅相应类型事件的函数。

##EventBus的四种ThreadMode（线程模型）

来自 http://blog.csdn.net/itachi85/article/details/52205464

EventBus3.0有以下四种ThreadMode：

- POSTING（默认）：如果使用事件处理函数指定了线程模型为POSTING，那么该事件在哪个线程发布出来的，事件处理函数就会在这个线程中运行，也就是说发布事件和接收事件在同一个线程。在线程模型为POSTING的事件处理函数中尽量避免执行耗时操作，因为它会阻塞事件的传递，甚至有可能会引起ANR。
- MAIN: 
  事件的处理会在UI线程中执行。事件处理时间不能太长，长了会ANR的。
- BACKGROUND：如果事件是在UI线程中发布出来的，那么该事件处理函数就会在新的线程中运行，如果事件本来就是子线程中发布出来的，那么该事件处理函数直接在发布事件的线程中执行。在此事件处理函数中禁止进行UI更新操作。
- ASYNC：无论事件在哪个线程发布，该事件处理函数都会在新建的子线程中执行，同样，此事件处理函数中禁止进行UI更新操作。

## 接收事件的优先级

​	EventBus对事件的处理是队列的，他会按照优先级来排列，你可以手动设置接收事件的优先级

~~~java
	//设置优先级
	@Subscribe(priority = 1)
    public void handleMessage(MyMessage message) {
        Log.d(TAG, "handleMessage: " + message.getmMessage());
    }
~~~



## 场景一

 这个EventBus说白了就是一个非常强大的通信官，专门用来应用程序内各组件间、组件与后台线程间的通信。说到通信，先来看看安卓哪里有通信

###最常用最简单的，Activity之间的通信

~~~java
//ActivityA要求ActivityB返回数据的常规写法

//ActivityA的代码
 startActivityForResult(intent,requestCode);

//处理ActivityB返回的信息
 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
~~~

####ActivityB的代码，返回信息的代码

```java
Intent intent = new Intent();
intent.putExtra("key", "values");
setResult(RESULT_OK, intent);
```
---

### EventBus在Activity中传递数据

   自定义一个实体类，用来作为传递的Event，（这类就类似于Bundle），可定制化非常的高

   ~~~java
   /**
    * Created by BA on 2017/11/1 0001.
    *
    * @Function : 用作EventBus传递的信息，你可以不用这个的，可以直接用Intent或者String，因为
    * 发送和接收，对消息的处理都是你自己决定的
    */

   public class MyMessage {
       private String mMessage;

       public MyMessage(String mMessage){
           this.mMessage=mMessage;
       }

       public String getmMessage(){
           return mMessage;
       }
   }
   ~~~

---

####ActivityA中的代码   

   ~~~java
 //接收到信息后的回调方法，类似于onActivityResult

//加上这个声明就是说要这个方法作为接收信息的回调方法，还可以设置线程类型，不设置就是默认，设置其他就像下面一样
// @Subscribe(threadMode = ThreadMode.MAIN) 
// @Subscribe(threadMode = ThreadMode.BACKGROUND)
// @Subscribe(threadMode = ThreadMode.ASYNC)

   @Subscribe 
   public void handleMessage(MyMessage message){
       Toast.makeText(this,message.getmMessage(), Toast.LENGTH_SHORT).show();
   }


   //要注册和注销
   @Override
   protected void onStart() {
       super.onStart();
       EventBus.getDefault().register(this);
   }

   @Override
   protected void onStop() {
       super.onStop();
       EventBus.getDefault().unregister(this);
   }
   ~~~

---

#### ActivityB中的写法

~~~java
//发送信息给ActivityA，这里可能会蒙蔽，他是怎样知道要返回给ActivityA的，或者说这个发送的方法就是全局发送的
//往下看就知道了，运行就你自己运行啦
EventBus.getDefault().post(new MyMessage("附件是东方航空是否"));
~~~

---

##场景二

###EventBus替代安卓中的本地广播

​	我们都知道在安卓中使用本地广播，无非就是用来通知自己程序的一些组件，而实现广播的方式也不复杂，但是直接回调和用广播，广播还要过滤一堆，而且没有封装好异步啊什么的，既然是用来观察状态然后通知观察者，无非就想到了观察者模式，也就是这里的EventBus了，而且EventBut还封装了多线程的问题

---

## 场景三(应该说是最常用的吧)

### EventBus替代Handler

​	在使用Handler时也就是涉及到了异步消息处理，ui线程和子线程之间的切换，事实上用handler实现起来也不太麻烦，而且用handler快速切换回主线程是特别的方便的，只需要post一下，但是不管是主线程通信子线程，子线程通信主线程，双方都需要有Handler，前者就比较麻烦，要为子线程绑定looper。但是使用EventBus就可以特别优雅的从子线程通信主线程，主线程通信子线程

----

## EventBus3.0粘性事件

来自 http://blog.csdn.net/itachi85/article/details/52205464

除了上面讲的普通事件外，EventBus还支持发送黏性事件，就是在发送事件之后再订阅该事件也能收到该事件，跟黏性广播类似。为了验证粘性事件我们修改以前的代码：说白了就是只要粘性事件发送了，不管你是发生前注册还是发送后注册都可收到粘性事件

#### **订阅粘性事件**

在MainActivity中我们将注册事件添加到button的点击事件中：

```java
  bt_subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //注册事件
                EventBus.getDefault().register(MainActivity.this);
            }
        });
```

#### **订阅者处理粘性事件**

在MainActivity中新写一个方法用来处理粘性事件：

```java
   @Subscribe(threadMode = ThreadMode.POSTING，sticky = true)
    public void ononMoonStickyEvent(MessageEvent messageEvent){
        tv_message.setText(messageEvent.getMessage());
    }
```

#### **发送黏性事件**

在SecondActivity中我们定义一个Button来发送粘性事件：

```java
   bt_subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new MessageEvent("粘性事件"));
                finish();
            }
        });
```

好了运行代码再来看看效果，首先我们在MainActivity中并没有订阅事件，而是直接跳到SecondActivity中点击发送粘性事件按钮，这时界面回到MainActivity，我们看到TextView仍旧显示着MainActivity的字段，这是因为我们现在还没有订阅事件。

![20160816165339217](D:\分析快快送项目期间学到的东西\EventBus使用与源码分析\20160816165339217.png)

接下来我们点击订阅事件，TextView发生改变显示“粘性事件”，大功告成。

![20160816165645143](D:\分析快快送项目期间学到的东西\EventBus使用与源码分析\20160816165645143.png)

---

## EventBus用法总结

给我的感觉就是把本地广播，Handler，Activity三种通信的方法进行了统一，而且非常简单

---

## EventBus源码分析

**内部其实用的是观察者模式，不记得的话，设计模式的笔记里面有** 

### 从订阅事件说起

~~~java
//该方法就是将当前类的实例进行注册观察者，注册后，传进去的实例就成了观察者，先看看getDefault()方法
EventBus.getDefault().register(obj);
~~~

---

~~~java
    /** Convenience singleton for apps using a process-wide EventBus instance. */
    public static EventBus getDefault() {
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new EventBus();
                }
            }
        }
        return defaultInstance;
    }
~~~

很明显的单例，获取了EventBus的实例，看看EventBus的无参构造又干了什么

~~~java
    /**
     * Creates a new EventBus instance; each instance is a separate scope in which events are delivered. To use a
     * central bus, consider {@link #getDefault()}.
     */
    public EventBus() {
        this(DEFAULT_BUILDER);
    }
~~~

其实这里我就觉得有点奇怪 ，为什么很多都喜欢在无参构造里面再调用另一个有参构造，但是认真想想，其实这个有参的构造可能本来就是根据传入的参数而决定实例化一个对象的，当我就是要一个标准的实例的时候，我难道还要在实例化的时候传入参数？或者说我要在这个无参构造再写实例化的具体方法？应该是出于这样的目的才这样写吧。

~~~java
//可以看到上面传入的原来是一个Builder，这个应该就是构造标准的实例的建造者了   
EventBus(EventBusBuilder builder) {
        logger = builder.getLogger();//获取了一个记录器
  
  		//存订阅类型的集合？
        subscriptionsByEventType = new HashMap<>(); 
  		//存观察者（订阅者的集合）？
        typesBySubscriber = new HashMap<>();
  		//粘性事件的集合
        stickyEvents = new ConcurrentHashMap<>();
  
  		//主线程，其实就是返回一个有主线程的Looper工具类的实例，通过这个实例可以拿到一个持有主线程Looper的HandlerPoster实例，这个就是用来发布消息的嘛，后面再遇到再继续分析
        mainThreadSupport = builder.getMainThreadSupport();
        mainThreadPoster = mainThreadSupport != null ? mainThreadSupport.createPoster(this) : null;
        backgroundPoster = new BackgroundPoster(this);
        asyncPoster = new AsyncPoster(this);
  
  //这个集合用来存什么的我目前也不知道，继续分析看看后面会不会解决
        indexCount = builder.subscriberInfoIndexes != null
          ? builder.subscriberInfoIndexes.size() : 0;
  
  //这个就是用来查找订阅者里面订阅方法的实例，传入的就是上面的subscriberInfoIndexes集合，所以这个集合是不是用来存放订阅者的
        subscriberMethodFinder = new SubscriberMethodFinder(builder.subscriberInfoIndexes,
                builder.strictMethodVerification, builder.ignoreGeneratedIndex);
  //下面几个都是异常了
        logSubscriberExceptions = builder.logSubscriberExceptions;
        logNoSubscriberMessages = builder.logNoSubscriberMessages;
        sendSubscriberExceptionEvent = builder.sendSubscriberExceptionEvent;
        sendNoSubscriberEvent = builder.sendNoSubscriberEvent;
        throwSubscriberException = builder.throwSubscriberException;
  //者两个干嘛的我也不知道，翻译是事件的继承，是一个boolen型默认为true，会不会是用来判断是否继续分发事件的
        eventInheritance = builder.eventInheritance;
  
        executorService = builder.executorService; 
    }
~~~

---

其实就是进行了初始化操作，很正常，有三处疑问

+ subscriptionsByEventType这个集合存的是什么
+ typesBySubscriber 这个集合存的是什么
+ mainThreadSupport 为什么要自己获取一个主线程的Looper，难道说切换回主线程的时候还需要自己设置Looper？可是在以前不是直接post()???

继续看看register()的方法里面在干嘛

~~~java
    /**
     * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Object)} once they
     * are no longer interested in receiving events.
     * <p/>
     * Subscribers have event handling methods that must be annotated by {@link Subscribe}.
     * The {@link Subscribe} annotation also allows configuration like {@link
     * ThreadMode} and priority.
     */
    public void register(Object subscriber) {
      //这里是对注解Subscribe，通过反射的方式解析，这里先不用管，以后学到注解的时候再来研究
      //只要先知道是获取订阅的类
        Class<?> subscriberClass = subscriber.getClass(); 
      
      //前面说过这个实例是用来根据类来查找所有被注解为Subscribers的方法
      //SubscriberMethod（订阅方法）类中，主要就是用保存订阅方法的Method对象、线程模式、事件类型、优先级、是否是粘性事件等属性。
        List<SubscriberMethod> subscriberMethods = 
          subscriberMethodFinder.findSubscriberMethods(subscriberClass);
      
      //然后遍历循环调用subscribe方法，将观察者和观察者中的回调方法传入
        synchronized (this) {
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                subscribe(subscriber, subscriberMethod);
            }
        }
    }

    // Must be called in synchronized block
    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
      //获取事件的类型
        Class<?> eventType = subscriberMethod.eventType;
     //将观察者和回调方法封装成一个Subscription
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
      
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions == null) {
          //subscriptions是一个读写分离的共享数据集合，操作这个集合不用加锁(不知道也没关系)
            subscriptions = new CopyOnWriteArrayList<>();
          //可以看到它将事件的类型做为key，是为了在后面取出，因为post方法传入的就是你要发生的事件
          //就可以根据事件类型来找到对应的subscriptions(就可以获取到观察者和回调方法了)
            subscriptionsByEventType.put(eventType, subscriptions);
        } else {
            if (subscriptions.contains(newSubscription)) {
                throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "
                        + eventType);
            }
        }

      //subscriptions原来就是一个根据回调方法优先级放入刚刚封装的subscription的集合
        int size = subscriptions.size();
        for (int i = 0; i <= size; i++) {
            if (i == size || subscriberMethod.priority
                > subscriptions.get(i).subscriberMethod.priority) {
                subscriptions.add(i, newSubscription);
                break;
            }
        }

      //根据当前注册的观察者获取对应的观察者类型，
        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if (subscribedEvents == null) {
            subscribedEvents = new ArrayList<>();
          //键是观察者，值是包含了事件类型的集合
            typesBySubscriber.put(subscriber, subscribedEvents);
        }
        subscribedEvents.add(eventType);

      //如果当前观察者的回调方法是粘性的，就从stickyEvents集合中获取粘性事件
      //然后调用这个方法checkPostStickyEventToSubscription()，在最下面
        if (subscriberMethod.sticky) {
            if (eventInheritance) {
                // Existing sticky events of all subclasses of eventType have to be considered.
                // Note: Iterating over all events may be inefficient with lots of sticky events,
                // thus data structure should be changed to allow a more efficient lookup
                // (e.g. an additional map storing sub classes of super classes: Class -> List<Class>).
                Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
                for (Map.Entry<Class<?>, Object> entry : entries) {
                    Class<?> candidateEventType = entry.getKey();
                    if (eventType.isAssignableFrom(candidateEventType)) {
                        Object stickyEvent = entry.getValue();
                        checkPostStickyEventToSubscription(newSubscription, stickyEvent);
                    }
                }
            } else {
                Object stickyEvent = stickyEvents.get(eventType);
                checkPostStickyEventToSubscription(newSubscription, stickyEvent);
            }
        }
    }


//就是这个，你可以看到在这里发送了一次，这就是粘性事件的实现方法
    private void checkPostStickyEventToSubscription(Subscription newSubscription, Object stickyEvent) {
        if (stickyEvent != null) {
            // If the subscriber is trying to abort the event, it will fail (event is not tracked in posting state)
            // --> Strange corner case, which we don't take care of here.
            postToSubscription(newSubscription, stickyEvent, isMainThread());
        }
    }
~~~

---

register()方法主要做的事情就是(下面都是说第一次初始化EventBus的流程)

+ 使用SubscriberMethodFinder通过被注册的观察者获取所有的回调方法，然后开始遍历所有回调方法
+ 在遍历的时候先获取当前事件的类型，然后将当前被遍历到的回调方法和观察者封装成一个Subscription实例
+ 然后从SubscriptionsByEventType获取根据回调方法优先级排列Subscriptions集合，
+ 然后再获取当前回调方法的优先级，根据回调方法的优先级按顺序将前面封装的Subscription实例放入上面获取的Subscriptions
+ 然后再将当前的事件类型作为key， Subscriptions集合作为values，放入SubscriptionsByEventType集合
+ 然后根据观察者先获取到当前观察者所有要传递的事件类型，并且放在一个arrayList集合中，然后用当前观察者作为key，刚刚的arrayList集合作为values，放入到typesBySubscriber集合中，这个集合其实就是用来存事件类型的
+ 既然传递事件类型也搞定了，就还要判断当前回调方法是不是监听的粘性事件，是的话就再post一次所有的粘性事件，这就是实现粘性事件的最重要的部分
+ 然后进入下一次循环，继续对下一个当前观察者订阅的回调方法进行上面的处理

上面就是register()方法做的所有事情，其实也就作完EventBus的大部分工作了，现在就来看看上次留下的问题，能不能解答：

- subscriptionsByEventType这个集合存的是什么？

  > 这个集合里面的内容是：事件类型作为key，根据回调方法优先级封装了subscription对象（封装了观察者和一个回调方法）的subscriptions集合作为values
  >
  > subscriptionsByEventType的里面的分类就是根据传递事件的类型分类

- typesBySubscriber 这个集合存的是什么

  > 内容：前观察者作为key，包含了当前观察者中所有要传递的事件的事件类型的集合作为values
  >
  > 有什么用？我还不知道

  ![20160821151026460](D:\分析快快送项目期间学到的东西\EventBus使用与源码分析\20160821151026460.png)


---

### 发送事件

~~~java
//也就这一行代码，主要就看看post()方法,先看看参数吧，传入的就是一个EventBus
EventBus.getDefault().post(new MyMessage("xxx"));
~~~

~~~java
    /** Posts the given event to the event bus. */
    public void post(Object event) {
      
      //获取一个包含线程状态的实例
        PostingThreadState postingState = currentPostingThreadState.get();
      
      //获取事件的队列
        List<Object> eventQueue = postingState.eventQueue;
      
      //将当前事件添加到事件的队列中
        eventQueue.add(event);

      //判断当前是否在post事件。如果是的话就不用手动在启动一次分发，只需要将事件加入到队列
        if (!postingState.isPosting) {
          //判断当前是否在主线程post
            postingState.isMainThread = isMainThread();
          //现在处于post状态
            postingState.isPosting = true;
            if (postingState.canceled) {
                throw new EventBusException("Internal error. Abort state was not reset");
            }
            try {
              
              //开始post，关键方法还是在里面
                while (!eventQueue.isEmpty()) {
                    postSingleEvent(eventQueue.remove(0), postingState);
                }
            } finally {
                postingState.isPosting = false;
                postingState.isMainThread = false;
            }
        }
    }
~~~

---

目前做好的工作就是将当前事件加入到了事件队列中，并且对线程状态进行了一些设置

~~~java
 
private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
  //获取当前事件的类型，不是说传了个对象就能判断的，用反射的方法就可以判断是什么类
  //而且在上面存到typesBySubscriber时，values其实就是这个类型
        Class<?> eventClass = event.getClass();
  
  //判断有没找到观察者的回调方法
        boolean subscriptionFound = false;
  
  //这个eventInheritance用到了，前面说他是个Boolen型，用在这里的意思就是：是否向上查找它所有父类的事件
        if (eventInheritance) {
            List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
            int countTypes = eventTypes.size();
          //遍历去post这个事件以及它父类，然后返回看看有没有post成功
            for (int h = 0; h < countTypes; h++) {
                Class<?> clazz = eventTypes.get(h);
                subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
            }
        } else {
          //如果只是post当前传进来的event，直接post，所以还要看看这个方法
            subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
        }
        if (!subscriptionFound) {
            if (logNoSubscriberMessages) {
                logger.log(Level.FINE, "No subscribers registered for event " + eventClass);
            }
            if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class &&
                    eventClass != SubscriberExceptionEvent.class) {
                post(new NoSubscriberEvent(this, event)); //事件全部分发完后发送这个，意思是该发的已经发了
            }
        }
    }
~~~

---

上面做了的工作就是判断是否需要post该事件以及它的父类，还有就是最后发送完事件的处理也是在该方法，那哪里才是真的发生事件？还有就是如何用传入的事件类型去调用是该类型的回调方法

~~~java
private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
  		//这个集合是不很熟悉？它被存在subscriptionsByEventType集合中，而且是优先级顺序的，取出的key是事件的类型
        CopyOnWriteArrayList<Subscription> subscriptions;
        synchronized (this) {
            subscriptions = subscriptionsByEventType.get(eventClass);
        }
  
  //postingState这个类是用来记录post的时候需要用到的状态和一些对象
        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                postingState.event = event;
                postingState.subscription = subscription;
                boolean aborted = false;
                try {
                  //这个就是开始post，又跳方法，，，真是累啊
                    postToSubscription(subscription, event, postingState.isMainThread);
                    aborted = postingState.canceled;
                } finally {
                    postingState.event = null;
                    postingState.subscription = null;
                    postingState.canceled = false;
                }
                if (aborted) {
                    break;
                }
            }
            return true;
        }
        return false;
    }
~~~

在上面的方法做了也是很简单的事，就是方法名的意思，post一个事件，在上面的循环就是进行了一个事件post所应该处理的东西。一次一次的循环，一个一个的post，是不是很符合方法名的意思

---

~~~java
//终于到了啊！！！！   
private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
        switch (subscription.subscriberMethod.threadMode) { //判断要怎样的线程去post事件
            case POSTING:
                invokeSubscriber(subscription, event);
                break;
            case MAIN:
                if (isMainThread) {
                    invokeSubscriber(subscription, event);
                } else {
                    mainThreadPoster.enqueue(subscription, event);
                }
                break;
            case BACKGROUND:
                if (isMainThread) {
                    backgroundPoster.enqueue(subscription, event);
                } else {
                    invokeSubscriber(subscription, event);
                }
                break;
            case ASYNC:
                asyncPoster.enqueue(subscription, event);
                break;
            default:
                throw new IllegalStateException("Unknown thread mode: " 
                                                + subscription.subscriberMethod.threadMode);
        }
    }
~~~

---

![20160821171434514](D:\分析快快送项目期间学到的东西\EventBus使用与源码分析\20160821171434514.png)

---

至于怎样在线程中处理，就先留着，以后有空再说，因为我的目的其实已经达到了，看看观察者模式的实际应用

## 总结

+ 来看观察者模式的实际使用

  > + 观察者：在这里就是所有要注册的类的实例
  > + 管理观察者的类：在这里就是EventBus这个类

+ 注册观察者的方法：EventBus.getDefault().register(obj);

+  取消注册观察者的方法：EventBus.getDefault().unregister(obj);

+ 在EventBus中由于传入的事件类型都不一样，所以就要进行区分，这里用的是传递事件的类型来区分，所以存储的集合是subscriptionsByEventType，但是在EventBus中还需要判断回调方法的优先级和线程模式，这些东西都是放在了一个叫subscriberMethods类，所以订阅者和订阅方法在这里其实是分开来封装的，很有意思吧。所以EventBus里面是用subscription来封装观察者和回调方法的，然后还在调用这些个回调方法时还要根据事件优先级(这里其实还是一个观察者的回调方法，因为它考虑的是可能会有很多个回调方法(因为线程可能不一样嘛))，这里是在注册的时候就将这个顺序排列好的，放在subscriptions集合里面。所以subscriptionsByEventType集合真的存的其实是subscriptions集合

+ 最后一步就是事件的分发了，就是调用观察者的回调方法嘛，代码是**EventBus.getDefault().post(new MyMessage("xx"));**,可以看到传入的是事件嘛，那怎么知道要调用哪些观察者的方法，在存观察者的时候就说了，key是事件类型，获取到的values就是subscriptions集合，这个集合存了所有监听这个事件类型的观察者，然后再判断下是不是当前线程接收信息，然后就回调啊啊啊啊