[toc]
# 高效加载Bitmap(多线程)
## 先说两句
我最近状态非常不对，再也没有了那股一开始学编程的热情，线程池也是拖了好久才去学习，这就是我落后于人的原因吧。在学习线程池时现了解下为什么要使用线程池技术。参考文章：https://www.cnblogs.com/Steven0805/p/6393443.html

平时我们使用多线程的时候都是这样写
~~~java
    new Thread(new Runnable() {
            @Override
            public void run() {
                
            }
        }).start();
~~~

如果只是一次性的使用，完全没有问题。如果不需要对该线程管理，只要求直接完成任务，完全没有问题，但是但是，如果软件需要频繁的使用线程，还要对线程进行管理，那就很有必要用到线程池了，因为系统创建线程和释放线程是很耗费性能的。我们从实际应用出发吧。

## 需求：
从手机获取所有图片，然后用RecyclerView显示出来

## 技术分析：
获取图片是个耗时操作，如果在主线程中获取，然后再显示就会导致主线程阻塞，那很自然的想到用另一个线程去获取图片，然后再到主线程中显示。确实没错，为了性能，获取图片当然是当我们RecyclerView滚到时才显示。这样就要求在recyclerView的public void onBindViewHolder()方法中使用另一线程去加载，问题就出来了，该方法只要是滚动就一直会调用，，，那如果你是直接用new Thread(new Runnable());那就GG了，这会很耗费性能，这时候如果用线程池就完美了！！！

## ThreadPoolExecutor
这个就是线程池的具体实现，他的构造函数我们可以看看
~~~java
 /**
     * @param corePoolSize 核心线程数，核心线程一般来世不会被回收，但是当ThreadPoolExecutor的allowCoreThreadTimeOut属性设置为True的时候，核心线程也会被回收
     * @param maximumPoolSize 最大线程数当活动线程到了这个值后，后续的任务会被阻塞
     * @param keepAliveTime 非核心线程闲置的超时时长，超过这个时长的非核心线程会被回收，但是当ThreadPoolExecutor的allowCoreThreadTimeOut属性设置为True的时候，核心线程也会被回收
     * @param unit 用来指定KeepAliveTime的单位，是一个枚举类型
     * @param 你的Runnable对象都是放在这里
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }
~~~

### 常见的构建规则
+ 如果线程数没有达到核心线程数的限制，就会启动一个新的线程
+ 如果线程数量达到线程数量限制，那么新任务会排队
+ 如果任务队列已经满了，线程数也没有达到最大值，会立刻启动一个非核心线程去处理这个任务
+ 如果队列满了，然后线程数达到了限制值，那就会报错

通过这些属性的设置，可以构建出不同的线程池（不同的线程管理方式），一般有4种

### 选取线程池
哈？选取线程池？在安卓，线程池一共有4种，每种对线程的管理方式都不一样，我们看看应该挑哪种

+ newCachedThreadPool创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。 我们看看他的创建方式

~~~java
   public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
~~~

可以看到核心线程数为0，这意味着都是非核心线程，他设置了60秒超时，如果没有任务，所有的线程闲置时间超过60秒就会被回收，这一来就不会占用系统资源，如果有任务就会马上开一个线程执行，这样一来根本不需要排队，所以队列用的是SynchronousQueue(该线程是非常的特殊的队列，一般情况下是无法插入的队列)。他的线程限制是无限，这意味着可以迅速的执行大量的短时任务。


+ newFixedThreadPool 
创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。 

~~~java
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }
~~~

这个线程池只有核心线程，因为核心线程数和线程限制数是一样的，超时是0秒，这意味着核心线程会不被回收，直到线程池被销毁，只要所有线程都处于活跃状态就插入队列，而且队列是无限大的，这个线程池是比较好控制的



+ newScheduledThreadPool 创建一个定长线程池，支持定时及周期性任务执行。 
~~~java
    public ScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize, Integer.MAX_VALUE,
              DEFAULT_KEEPALIVE_MILLIS, MILLISECONDS,
              new DelayedWorkQueue());
    }
~~~

他是有核心线程数，但是线程数量无限制，非核心线程一空闲就马上回收。


+ newSingleThreadExecutor 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。

我会选第二种，原因嘛，就是想选第二种，第二种已经可以满足我的需求了，那接下来看看怎么创建这个线程池
~~~java
//创建一个线程池，参数是设置最大线程数量
 ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 10; i++) {
            final int index = i;
            
            //执行任务
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d("ssssssss", "run: "+index+"-----"+Thread.currentThread().getName());
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }

//把线程队列里面的任务执行完后就关闭线程，关闭后就不能启动了！
fixedThreadPool.shutdown();

//也可以选这个，马上关闭线程池种所有线程，关闭后就不能启动了！
fixedThreadPool.shutdownNow();
~~~

## 开始动手写
在写加载图片的程序前，先分析一下，在以前我就自己去写过，但是效果是极度的不理想，虽然他是可以流畅加载，但是总是慢一点，就觉得很奇怪，然后就一直拖着，拖着，直到看到了这本书，我们先分析一下Bitmap

### Bitmap
就是一个位图，4种加载方法就不讲了，主要是讲如何高效的加载，现在一张图片都是非常大的，假设有一张1024x1024的图片，但是我们的ImageView的大小没那么大，那这样一来图片的大小那么大不就浪费了?我们就要想办法去压缩Bitmap。

### 了解采样率
使用BitmapFactory.Option可以进行压缩，他有一个inSampleSize参数，这个参数代表采样率，1的时候是原始大小，2的时候，长宽为原图的1/2，像素数为原图的1/4，占用的内存也为原图的1/4。

假设一个图片分辨率为：1024x1024，采用ARG8888格式存储，那他的内存是1024x1024x4，就是4mb，如果采样率为2，那就会变成512x512x4，就是1mb。只要采样率大于1才会有缩放效果，缩放比例就是像素缩放比例，为1/（采样率的平方）。

采样率一般是2的指数，1，2，4，8，如果不为2的指数，系统会向下取整(不是所有安卓系统都是这样)。

### 计算出采样率
+ 将BitmapFactory.Option的inJustDecodeBounds参数设置为true。(设置了系统不会去真的加载图片，而是去获取图片的边界)
+ 从Option获取图片的宽高
+ 结合View去算出采样率
+ 将BitmapFactory.Option的inJustDecodeBounds参数设置为true。从新加载图片

### 压缩Bitmap的代码
~~~java
    /**
     * Decode sampled bitmap bitmap.
     *
     * @param resources the resources R文件
     * @param resId     the res id 资源ID
     * @param rW        the r w 目标宽
     * @param rH        the r h 目标高
     * @return the bitmap
     */
    public static Bitmap decodeSampledBitmap(Resources resources,int resId,int rW,int rH){
        final BitmapFactory.Options options=new BitmapFactory.Options();
        
        //设置只获取边界，不真正加载图片
        options.inJustDecodeBounds=true;
        
        //加载图片，注意：这里是不真正加载图片
        BitmapFactory.decodeResource(resources,resId,options);
        
        options.inSampleSize=calculateInSampleSize(options,rW,rH);
        
        options.inJustDecodeBounds=false;
        
        return BitmapFactory.decodeResource(resources,resId,options);
    }
    
    public static int calculateInSampleSize(BitmapFactory.Options options,int rW,int rH){
        
        //拿出图片宽高
        final int h=options.outHeight;
        final int w=options.outWidth;
        int inSampleSize=1;
        
        //当大小超过View的大小才去压缩
        if (h>rH||w>rW){
            
            final int halfH=h/2;
            final int halfW=w/2;
            
            while (halfH/inSampleSize>=rH && halfW/inSampleSize>=rW){
                inSampleSize*=2;
            }
        }
        
        return inSampleSize;
    }
~~~

### 缓存
缓存我就不做过多解释，缓存是需要获取添加删除的，添加和获取是正常的操作流程，但是删除呢？假设你的缓存满了，但是又有新的缓存要放进来，那怎么办？是不是要删除一些缓存进而容纳新的内容？那删除的策略是什么？(有很多种具体自己百度一下，我们这里就列举两种常用的算法)
+ 最后修改时间
删除最后被修改时间是很久以前的缓存(这种缓存算法不完美)
+ 近期使用最少LRU
删除最近使用次数最少的缓存

我们选第二种

系统已经提供了缓存类
+ 内存缓存
LruCache
+ 存储设备缓存类
DiskLruCache

#### LruCache
这个是一个泛型类，内部实现比较简单。他是线程安全的，我们一般初始化的代码在下面
~~~java
//算出系统分配给在该进程我们的可用的运行内存，这里除1024是换算成kb
int maxMemory =(int) (Runtime.getRuntime().maxMemory()/1024)

//我们取1/8来作为我们加载图片的缓存
int cacheSize=maxMemory/8;

LruCache<String ,Bitmap> lruCache=new LruCache(<String,Bitmap>(cacheSize){

    //重写这个方法是为了提供计算每个缓存对象大小
    @override
    protexted int sizeOf(String key,Bitmap bitmap){
        //最后要除1024换算成kb，要和上面统一单位
        return bitmap.getRowBytes()*bitmap.getHeight()/1024;
    }
}

~~~

我们上面只复写了sizeOf方法，一些很特殊的情况还要复写entryRemove()方法，这个方法在移除旧缓存的时候会被调用，你可以在这里进行一些资源回收工作。

至于获取删除，插入这些方法我就不写了。

### DiskLruCache
导入地址
~~~xml
implementation 'com.jakewharton:disklrucache:2.0.2'
~~~

实现磁盘缓存的具体流程
~~~java
    
    //缓存大小
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;//50MB
   

    //待下载图片的uri
    private String uri = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1532493239752&di=86c65fc83773bc57dcefb6dfff7265d2&imgtype=0&src=http%3A%2F%2Fimgs.ebrun.com%2Fresources%2F2014_10%2F2014_10_31%2F201410312811414721244781.jpg";

    //用来显示图片
    private ImageView imageView;

    //磁盘缓存的对象
    private   DiskLruCache cache;

    //切换线程
    private Handler handler;

    /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化
        imageView=(ImageView)findViewById(R.id.img);
        handler=new Handler();
        
        //由于我们这里是从网络上下载图片，然后放到内存中，然后再从内存中取出图片
        new Thread(new Runnable() {
            @Override
            public void run() {
                
                //为了不直接暴露uri，要加密
                String key = hashKeyForDisk(uri);
                //获取磁盘缓存的实例
              cache = open();
                try {
                    if (cache != null) {
                        //这个是任务编辑器，他才是真正用来操作磁盘缓存的对象
                        DiskLruCache.Editor editor = cache.edit(key);
                        if (editor != null) {
                            //打开一个输入流
                            OutputStream outputStream = editor.newOutputStream(0);
                            
                            //去下载图片，注意，这里是如果下载成功，才用editor提交才会真的去将图片保存到磁盘
                            if (downloadUrlToStream(uri, outputStream)) {
                                editor.commit();
                                
                                //保存到磁盘后就去取图片，当然你可以直接用
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //设置图片
                                        imageView.setImageBitmap(getCache(cache));
                                        Log.d(TAG, "run:设置图片成功");
                                    }
                                });
                            } else {
                                //写入失败
                                editor.abort();
                            }
                        }
                        
                        //释放资源
                        cache.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
~~~

+ 加密uri的方法
~~~java
    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        Log.d(TAG, "hashKeyForDisk: 获取Key");
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        Log.d(TAG, "bytesToHexString: MD5加密");
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
~~~

+ 获取磁盘缓存实例的方法
~~~java
    public DiskLruCache open() {
        File diskCacheDir = getCacheDir();
        DiskLruCache diskLruCache = null;
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        try {
            Log.d(TAG, "open: 打开磁盘缓存");
            //打开磁盘缓存
            diskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diskLruCache;
    }
~~~

+ 从网络下载图片的方法
~~~java
   /**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString The URL to fetch
     * @return true if successful, false otherwise
     */
    private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        Log.d(TAG, "downloadUrlToStream: 下载图片");
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), 1024 * 1024);
            out = new BufferedOutputStream(outputStream, 1024 * 1024);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
            }
        }
        return false;
    }
~~~

+ 从磁盘缓存获取缓存的方法
~~~java
    private Bitmap getCache(DiskLruCache cache) {
        Log.d(TAG, "getCache: 从磁盘获取图片");
        try {
            String key = hashKeyForDisk(uri);
            DiskLruCache.Snapshot snapshot = cache.get(key);
            if (snapshot != null) {
                InputStream in = snapshot.getInputStream(0);
                return BitmapFactory.decodeStream(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
~~~

## 跟着书本打造一个图片加载框架
需求：
+ 图片压缩
+ 内存缓存
+ 磁盘缓存
+ 网络拉取
+ 异步加载
+ 同步加载

