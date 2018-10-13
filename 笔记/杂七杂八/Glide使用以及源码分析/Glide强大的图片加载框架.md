[TOC]

# Glide强大的图片加载框架

## 先说两句

> 因为在以前学习的时候很少用到加载图片，所以没怎么研究过这个框架，今时也不同往日了！！！
>
> 最大的特点：简单到你无法想象
>
> 加载图片的方式支持很多种，比如从网上，本地，File文件，都可以

## 使用方法

> + ###需求：在一个ImageView上从不同的数据源加载图片
>
> + ###布局组成：ImageView,Button
>
>   ~~~xml
>       <Button
>           android:id="@+id/show"
>           android:layout_width="wrap_content"
>           android:layout_height="wrap_content"
>           android:text="show" />
>
>       <ImageView
>           android:id="@+id/image"
>           android:layout_width="match_parent"
>           android:layout_height="400dp" />
>   ~~~
>
>   ---
>
> + 直先看从网络上加载
>
> ~~~java
>     @Override
>     protected void onCreate(Bundle savedInstanceState) {
>         super.onCreate(savedInstanceState);
>         setContentView(R.layout.activity_main);
>
>         Button show=(Button)findViewById(R.id.show);
>         final ImageView imageView=(ImageView)findViewById(R.id.image);
>       
>       //即将要加载的图片地址
>       String url="http://img.taopic.com/uploads/allimg/120727/201995-120HG1030762.jpg";
>
>         show.setOnClickListener(new View.OnClickListener() {
>             @Override
>             public void onClick(View v) {
>                 
>               //加载啦，with代表上下文，这里传的你可以传任意一种，但是生命周期不同，那么加载图片的
>               //结束也不同
>                 Glide.with(MainActivity.this)
>                         .load(url) //从哪里加载图片
>                         .into(imageView); //加载到哪里
>             }
>         });
>     }
> ~~~
>
> ---
>
> + 从其他地方加载
>
> ~~~java
> // 加载本地图片
> File file = new File(getExternalCacheDir() + "/image.jpg");
> Glide.with(this).load(file).into(imageView);
>
> // 加载应用资源
> int resource = R.drawable.image;
> Glide.with(this).load(resource).into(imageView);
>
> // 加载二进制流
> byte[] image = getImageBytes();
> Glide.with(this).load(image).into(imageView);
>
> // 加载Uri对象
> Uri imageUri = getImageUri();
> Glide.with(this).load(imageUri).into(imageView);
> ~~~
>
> ---
>
> + 小总结
>
> > with(Context)上下文
> >
> > .load()从哪里加载
> >
> > .into()加载到哪里

## 占位图

> + 占位图意思就是在图片没有成功加载或者正在加载的时候，ImageView应该显示什么图片
>
> ~~~java
> Glide.with(MainActivity.this)
> .load(url)
> .placeholder(R.drawable.loading) //设置正在加载的时候显示的图片
> .error(R.drawable.loading) //设置加载失败时显示的图片
> .into(imageView);
> ~~~
>

## 加载动图！！！

> 牛逼，，可以直接加载动图！！直接将load()里面的地址改成gif的地址就可以了！！！，当然也可以强制设置当前View只显示静态图！说明里面是会自动识别应该加载什么格式的图片，当然你可以手动设置这个View只能加载什么格式的图片。load()方法的后面加入才行，看代码
>
> ~~~java
>                 Glide.with(MainActivity.this)
>                         .load(gifUrl)
>                   //设置只能显示静态图片，你设置的是Gif会只停留在第一帧，说明gif是一帧一帧加载的
>                         .asBitmap() 
>                   //设置只能加载gif图就不能加载静态图不然会出错
>                   		.asGif()
>                         .placeholder(R.drawable.loading)
>                         .diskCacheStrategy(DiskCacheStrategy.NONE) //取消图片缓存
>                         .error(R.drawable.loading)
>                         .into(imageView);
> ~~~
>

## 强制设置加载图片的大小

> 实际上，使用Glide在绝大多数情况下我们都是不需要指定图片大小的。
>
> 在学习本节内容之前，你可能还需要先了解一个概念，就是我们平时在加载图片的时候很容易会造成内存浪费。什么叫内存浪费呢？比如说一张图片的尺寸是1000*1000像素，但是我们界面上的ImageView可能只有200*200像素，这个时候如果你不对图片进行任何压缩就直接读取到内存中，这就属于内存浪费了，因为程序中根本就用不到这么高像素的图片。
>
> 而使用Glide，我们就完全不用担心图片内存浪费，甚至是内存溢出的问题。因为Glide从来都不会直接将图片的完整尺寸全部加载到内存中，而是用多少加载多少。Glide会自动判断ImageView的大小，然后只将这么大的图片像素加载到内存当中，帮助我们节省内存开支。
>
> 当然，Glide也并没有使用什么神奇的魔法，它内部的实现原理其实就是上面那篇文章当中介绍的技术，因此掌握了最基本的实现原理，你也可以自己实现一套这样的图片压缩机制。
>
> 也正是因为Glide是如此的智能，所以刚才在开始的时候我就说了，在绝大多数情况下我们都是不需要指定图片大小的，因为Glide会自动根据ImageView的大小来决定图片的大小。
>
> 不过，如果你真的有这样的需求，必须给图片指定一个固定的大小，Glide仍然是支持这个功能的。修改Glide加载部分的代码。注意！！！这个大小是指加载的分辨率大小适合ImageView，如果你的ImageVIew设置的图片显示模式是默认的话，就会很模糊的，具体你可以试试
>
> ~~~java
> Glide.with(this)
>      .load(url)
>      .placeholder(R.drawable.loading)
>      .error(R.drawable.error)
>      .diskCacheStrategy(DiskCacheStrategy.NONE)
>      .override(100, 100) //强制设置加载100*100像素的图片
>      .into(imageView);
> ~~~
## 设置加载过程的监听
有时候需要监听加载图片成功或者失败，看看代码
~~~java
Glide.with(ShowImgActivity.this)
     .load(urlString)
     .centerCrop()
     .error(R.drawable.failed)
     .crossFade()
     .into(new GlideDrawableImageViewTarget(imageView) {
 @Override
     public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
     super.onResourceReady(drawable, anim);
     //在这里添加一些图片加载完成的操作
    }
)}；
~~~

## 又或者是你需要获取的是Bitmap
~~~java
 Glide.with(context).load(uri).asBitmap().override(size,size).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (loadListener != null) {
                    loadListener.completed(resource);
                    loadListener = null;
                }
            }
        }); //方法中设置asBitmap可以设置回调类型
~~~

## 遇到的问题

> + 在强制指定图片大小的时候，为什么一定要在load()方法后面
>
> ~~~java
> //这是load方法，你会看到返回的是一个 DrawableTypeRequest类型的对象，他是强转来的，那就看看未转型前是什么
> public DrawableTypeRequest<String> load(String string) {
>         return (DrawableTypeRequest<String>) fromString().load(string);
>     }
>
>
> //看这里的源码就知道，返回一个DrawableRequestBuilder对象，然后看看其他方法返回什么
>  @Override
>     public DrawableRequestBuilder<ModelType> load(ModelType model) {
>         super.load(model);
>         return this;
>     }
>
>
> //返回的也是一个DrawableRequestBuilder对象
>  @Override
>     public BitmapRequestBuilder<ModelType, TranscodeType> placeholder(int resourceId) {
>         super.placeholder(resourceId);
>         return this;
>     }
>
> //那意思就很明显了，DrawableTypeRequest其实是DrawableRequestBuilder的子类，这个类的注释是
> // A class for creating a load request that loads either an animated GIF drawable or a Bitmap drawable directly, or adds an {@link com.bumptech.glide.load.resource.transcode.ResourceTranscoder} to transcode the data into a resource type other than a {@link android.graphics.drawable.Drawable}.
>
> //反正就是用来自动识别将要加载什么图片格式，或者强制设置加载格式的类，而DrawableRequestBuilder里面是没有这些方法的
> ~~~
>
> ---
>
> + 在使用占位图的时候，如果占位图的尺寸和要加载的尺寸有一定的差别后，在加载完图片时显示会被拉伸(待解决)
>
>
> + 在强制设置大小的时候，设置要加载的是不是刚刚好符合ImageView的大小，如果ImageView大小不等于你强制设置的大小后会出问题(待解决)

## 手动清除缓存

我就直接复制别人写的啦，

~~~java
/**
 * 描述：Glide缓存工具类
 * Created by 
 */


public class GlideCacheUtil {
    private static GlideCacheUtil inst;

    public static GlideCacheUtil getInstance() {
        if (inst == null) {
            inst = new GlideCacheUtil();
        }
        return inst;
    }

    /**
     * 清除图片磁盘缓存
     */
    public void clearImageDiskCache(final Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(context).clearDiskCache();
// BusUtil.getBus().post(new GlideCacheClearSuccessEvent());
                    }
                }).start();
            } else {
                Glide.get(context).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除图片内存缓存
     */
    public void clearImageMemoryCache(Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除图片所有缓存
     */
    public void clearImageAllCache(Context context) {
        clearImageDiskCache(context);
        clearImageMemoryCache(context);
        String ImageExternalCatchDir=context.getExternalCacheDir()+ ExternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR;
        deleteFolderFile(ImageExternalCatchDir, true);
    }

    /**
     * 获取Glide造成的缓存大小
     *
     * @return CacheSize
     */
    public String getCacheSize(Context context) {
        try {
            return getFormatSize(getFolderSize(new File(context.getCacheDir() + "/"+ InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取指定文件夹内所有文件大小的和
     *
     * @param file file
     * @return size
     * @throws Exception
     */
    private long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 删除指定目录下的文件，这里用于缓存的删除
     *
     * @param filePath filePath
     * @param deleteThisPath deleteThisPath
     */
    private void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (File file1 : files) {
                        deleteFolderFile(file1.getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {
                        file.delete();
                    } else {
                        if (file.listFiles().length == 0) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 格式化单位
     *
     * @param size size
     * @return size
     */
    private static String getFormatSize(double size) {

        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);

        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }
}
~~~

