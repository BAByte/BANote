[TOC]

# 跟着郭大翻翻Glide源码

## 先说两句

> 读郭大的这个文章的开头我是很激动的，因为他说他要教我们分析源码！！！！我一直很想知道如何分析源码，其实在以前跟着他分析文章的时候思路也很清晰，无非就是追着一个功能使劲翻源码，然后理解主要的源码
>
> 看看原文：简单概括就是八个字：抽丝剥茧、点到即止。应该认准一个功能点，然后去分析这个功能点是如何实现的。但只要去追寻主体的实现逻辑即可，千万不要试图去搞懂每一行代码都是什么意思，那样很容易会陷入到思维黑洞当中，而且越陷越深。因为这些庞大的系统都不是由一个人写出来的，每一行代码都想搞明白，就会感觉自己是在盲人摸象，永远也研究不透。如果只是去分析主体的实现逻辑，那么就有比较明确的目的性，这样阅读源码会更加轻松，也更加有成效。

## 分析最简单的加载图片的实现

> Glide.with(this).load(url).into(imageView);
>
> 不用我说你也知道时建造者模式吧，那每一个的返回值应该是一个Builder咯，是这样吗？看看吧

## With()方法

> 先ctrl加点击进到get方法，你会发现一堆重载，看看代码吧
>
> ~~~java
> //每一句都那么认真注释，，屌爆了！！！先看返回值，返回一个RequestManager 对象，意味着调用with就是为了获取这个对象，注释写的意思大概是使用你传入的Context来进行获取传入这个COntext的生命周期。
>
> /**
>      * Begin a load with Glide that will be tied to the given {@link android.app.Activity}'s lifecycle and that uses the
>      * given {@link Activity}'s default options.
>      *
>      * @param activity The activity to use.
>      * @return A RequestManager for the given activity that can be used to start a load.
>      */
>     public static RequestManager with(Activity activity) {
>         RequestManagerRetriever retriever = RequestManagerRetriever.get();
>         return retriever.get(activity);
>     }
>
>     /**
>      * Begin a load with Glide that will tied to the give {@link android.support.v4.app.FragmentActivity}'s lifecycle
>      * and that uses the given {@link android.support.v4.app.FragmentActivity}'s default options.
>      *
>      * @param activity The activity to use.
>      * @return A RequestManager for the given FragmentActivity that can be used to start a load.
>      */
>     public static RequestManager with(FragmentActivity activity) {
>         RequestManagerRetriever retriever = RequestManagerRetriever.get();
>         return retriever.get(activity);
>     }
>
>     /**
>      * Begin a load with Glide that will be tied to the given {@link android.app.Fragment}'s lifecycle and that uses
>      * the given {@link android.app.Fragment}'s default options.
>      *
>      * @param fragment The fragment to use.
>      * @return A RequestManager for the given Fragment that can be used to start a load.
>      */
>     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
>     public static RequestManager with(android.app.Fragment fragment) {
>         RequestManagerRetriever retriever = RequestManagerRetriever.get();
>         return retriever.get(fragment);
>     }
>
>     /**
>      * Begin a load with Glide that will be tied to the given {@link android.support.v4.app.Fragment}'s lifecycle and
>      * that uses the given {@link android.support.v4.app.Fragment}'s default options.
>      *
>      * @param fragment The fragment to use.
>      * @return A RequestManager for the given Fragment that can be used to start a load.
>      */
>     public static RequestManager with(Fragment fragment) {
>         RequestManagerRetriever retriever = RequestManagerRetriever.get();
>         return retriever.get(fragment);
>     }
> ~~~
>
> ---
>
> + 上面注释有说，With方法在传这个Context时需要注意的就是图片加载的生命周期和传入的Context是一样的，然后当时我也有疑问的，加载过程中应该不需要Context(或者说不是必要的)，那为什么要传context来确定生命周期，又是怎么确定的?上面用的是RequestManagerRetriever的一个实例来获取RequestManager实例，在这里终于用到了我们传入的Context，那我们来看看retriever.get(fragment);里面干了什么
>
> ~~~java
> //先看这个，如果你传的是Context先是对传入的Context类型进行判断 然后调用对应的重载方法，有
> //什么重载方法这里就能看到，还有一个方法，我没复制出来，如果直接传入一个Application的Context那
> //加载图片的生命周期是和程序的生命周期是一致的
> public RequestManager get(Context context) {
>         if (context == null) {
>             throw new IllegalArgumentException("You cannot start a load on a null Context");
>         } else if (Util.isOnMainThread() && !(context instanceof Application)) {
>             if (context instanceof FragmentActivity) {
>                 return get((FragmentActivity) context);
>             } else if (context instanceof Activity) {
>                 return get((Activity) context);
>             } else if (context instanceof ContextWrapper) {
>                 return get(((ContextWrapper) context).getBaseContext());
>             }
>         }
>
>         return getApplicationManager(context);
>     }
>
> //你会看到，如果你是在非主线程使用Glide，那就不管传啥了，直接就用程序的生命周期了，文章里面没有解释为啥
>     public RequestManager get(FragmentActivity activity) {
>         if (Util.isOnBackgroundThread()) {
>             return get(activity.getApplicationContext());
>         } else {
>             assertNotDestroyed(activity);
>             FragmentManager fm = activity.getSupportFragmentManager();
>             return supportFragmentGet(activity, fm);
>         }
>     }
>
>     public RequestManager get(Fragment fragment) {
>         if (fragment.getActivity() == null) {
>             throw new IllegalArgumentException("You cannot start a load on a fragment before it is attached");
>         }
>         if (Util.isOnBackgroundThread()) {
>             return get(fragment.getActivity().getApplicationContext());
>         } else {
>             FragmentManager fm = fragment.getChildFragmentManager();
>             return supportFragmentGet(fragment.getActivity(), fm);
>         }
>     }
> ~~~
>
> ---
>
> + 里面都会先获取FragmentManager然又调用一个方法，我们再看看那个方法是干嘛的
>
> ~~~java
> //你会发现，他用刚刚传入的FragmentManager的实例获取SupportRequestManagerFragment实例，然后再实例化一个requestManager，这个就是我们整个With()方法要获取的东西，看看构造函数有一个 current.getLifecycle()，这个方法是获取碎片的生命周期，Glide就使用了添加隐藏Fragment的这种小技巧，因为Fragment的生命周期和Activity是同步的，如果Activity被销毁了，Fragment是可以监听到的，这样Glide就可以捕获这个事件并停止图片加载了。
> RequestManager supportFragmentGet(Context context, FragmentManager fm) {
>         SupportRequestManagerFragment current = getSupportRequestManagerFragment(fm);
>         RequestManager requestManager = current.getRequestManager();
>         if (requestManager == null) {
>             requestManager = new RequestManager(context, current.getLifecycle(), current.getRequestManagerTreeNode());
>             current.setRequestManager(requestManager);
>         }
>         return requestManager;
>     }
> ~~~
>
> ---

## With()源码整体逻辑分析总结

> 主要为了获取RequestManager对象，这个实例里面有load()方法，这意味着这个方法返回Builder咯，但是在获取这个RequestManager实例的时候还做了一点小工作
>
> 那就是确定这一次加载图片的生命周期。根据传入的Context类型来确定生命周期，里面还用了一个小技巧，当Context类型是Activity时，你没有办法知道Activity的生命周期，于是在里面添加了一个隐藏的碎片，因为碎片的生命周期和Actvity是一样的，所以可以通过调用这个碎片的回调来知道Activity的生命周期，那如果是Application的Context类型呢？那还处理个屁啊，意味着图片加载的生命周期和应用程序是一致的，软件都关闭了，图片加载自然也会自动关闭

## load()方法

> ~~~java
>  /**
>      * Returns a request builder to load the given {@link java.lang.String}.
>      * signature.
>      *
>      * @see #fromString()
>      * @see #load(Object)
>      *
>      * @param string A file path, or a uri or url handled by {@link com.bumptech.glide.load.model.UriLoader}.
>      */
>     public DrawableTypeRequest<String> load(String string) {
>         return (DrawableTypeRequest<String>) fromString().load(string);
>     }
> ~~~
>
> 上面的注释也写的很清楚，一系列操作是为了获取一个Builder，但是有意思的是返回的是一个DrawableTypeRequest实例，这个实例又是什么？类解释的大概意思应该是说，这个类决定了接下来要加载的图片格式，记得上一篇文章里面说过Glide能够很神奇的自动识别你将要加载的图片格式，而且你要强制设置加载的格式也必须在load()方法的后面，原因就是因为决定格式的正是这一个类，，这个类继承自DrawableRequestBuilder，load()方法也是在这一个父类，需要注意的是，如果你没有设置格式，默认就是返回DrawableRequestBuilder对象
>
> ---
>
> 
>
> ~~~java
>  //如果设置了格式？看下面
>  
> /**
>      * Attempts to always load the resource as a {@link android.graphics.Bitmap}, even if it could actually be animated.
>      *
>      * @return A new request builder for loading a {@link android.graphics.Bitmap}
>      */
>     public BitmapTypeRequest<ModelType> asBitmap() {
>         return optionsApplier.apply(new BitmapTypeRequest<ModelType>(this, streamModelLoader,
>                 fileDescriptorModelLoader, optionsApplier));
>     }
>
> //返回的是一个BitmapTypeRequest对象，这个对象不就是用来加载静态图的咯
> ~~~
> ---
>
>  ~~~java
>
> //接下来看看怎么获取这个对象的，没多少句，跳到了loadGeneric方法
>     public DrawableTypeRequest<String> fromString() {
>         return loadGeneric(String.class);
>     }
>  ~~~
> ~~~java
> //loadGeneric方法
> //反正，，，看不太懂。。。但是这个方法是哪里的？？？是RequeatManage的！！刚刚这个实例是不是获取了加载图片的生命周期，看下面你就会看到，终于用到了，还有就是ModelLoader对象，又是什么？？？很烦吧，他就是一个接口，看后面获取的方法名字Stream啊！！！就知道，真正实现加载方法的，就是这个东东，看到return的后面的方法，DrawableTypeRequest其实是个接口类，封装了一堆功能，因为我还没看桥接模式，但是我觉得就是这个意思
>     private <T> DrawableTypeRequest<T> loadGeneric(Class<T> modelClass) {
>         ModelLoader<T, InputStream> streamModelLoader = Glide.buildStreamModelLoader(modelClass, context);
>         ModelLoader<T, ParcelFileDescriptor> fileDescriptorModelLoader =
>                 Glide.buildFileDescriptorModelLoader(modelClass, context);
>         if (modelClass != null && streamModelLoader == null && fileDescriptorModelLoader == null) {
>             throw new IllegalArgumentException("Unknown type " + modelClass + ". You must provide a Model of a type for"
>                     + " which there is a registered ModelLoader, if you are using a custom model, you must first call"
>                     + " Glide#register with a ModelLoaderFactory for your custom model class");
>         }
>     
>         return optionsApplier.apply(
>                 new DrawableTypeRequest<T>(modelClass, streamModelLoader, fileDescriptorModelLoader, context,
>                         glide, requestTracker, lifecycle, optionsApplier));
>     }
> ~~~
>
> ---

## load()方法分析总结 

> 就是使用已经确定了图片生命周期的RequestManager实例，获取DrawableTypeRequest对象，然后用这个对象调用真正的load()方法。期间也做了一些小工作，DrawableTypeRequest是一个加载图片的功能类，这里可以决定加载的图片类型，前面获取的生命周期也是用在这个实例的构造函数里面，当然，这里说的load()方法是网络加载，本地啊，什么的也大同小异，那接下来就看看获取的这个DrawableTypeRequest的父类DrawableRequestBuilder！！！开玩笑的。，，，里面一堆方法，，看个鬼，看看下面用到的Into()方法吧,前面都只是准备过程，，真正开始加载图片其实是在into方法里面。

## Into()方法

> 先来看看源码，这个方法实际上实现的过程非常非常复杂，希望自己有点耐心吧
>
> ~~~java
> //DrawableTypeRequest里面的into方法,继续跳吧
>     @Override
>     public Target<GlideDrawable> into(ImageView view) {
>         return super.into(view);
>     }
> ~~~
>
> ---
>
> ~~~java
>     /**
>      * Sets the {@link ImageView} the resource will be loaded into, cancels any existing loads into the view, and frees
>      * any resources Glide may have previously loaded into the view so they may be reused.
>      *
>      * @see Glide#clear(android.view.View)
>      *
>      * @param view The view to cancel previous loads for and load the new resource into.
>      * @return The {@link com.bumptech.glide.request.target.Target} used to wrap the given {@link ImageView}.
>      */
>     public Target<TranscodeType> into(ImageView view) {
>         Util.assertMainThread();
>         if (view == null) {
>             throw new IllegalArgumentException("You must pass in a non null View");
>         }
>
>         if (!isTransformationSet && view.getScaleType() != null) {
>             switch (view.getScaleType()) {
>                 case CENTER_CROP:
>                     applyCenterCrop();
>                     break;
>                 case FIT_CENTER:
>                 case FIT_START:
>                 case FIT_END:
>                     applyFitCenter();
>                     break;
>                 //$CASES-OMITTED$
>                 default:
>                     // Do nothing.
>             }
>         }
>
>         return into(glide.buildImageViewTarget(view, transcodeClass));
>     }
> ~~~
>
> ---
>
> + 一堆代码，我也看不懂，先看看返回值Target是什么，下面是类的解释
>
> ~~~java
> /**
>  * An interface that Glide can load a resource into and notify of relevant lifecycle events during a load.
>  *
>  * <p>
>  *     The lifecycle events in this class are as follows:
>  *     <ul>
>  *         <li>onLoadStarted</li>
>  *         <li>onResourceReady</li>
>  *         <li>onLoadCleared</li>
>  *         <li>onLoadFailed</li>
>  *     </ul>
>  */
> ~~~
>
> + 这个接口，可以加载资源，用这个东西可以监听生命周期。郭大又说是用来最终展示图片用的，再回到Into方法，看看是怎样获取这个实例的，glide.buildImageViewTarget(view, transcodeClass)就是这个方法，看看源码
>
> ~~~java
>     <R> Target<R> buildImageViewTarget(ImageView imageView, Class<R> transcodedClass) {
>         return imageViewTargetFactory.buildTarget(imageView, transcodedClass);
>     }
> ~~~
>
> + 又是一脸蒙蔽吧，没有关系的，因为只是继续调用，没有多大意义，反正还是继续到下一个方法
>
> ~~~java
> /**
>  * A factory responsible for producing the correct type of {@link com.bumptech.glide.request.target.Target} for a given
>  * {@link android.view.View} subclass.
>  */
> public class ImageViewTargetFactory {
>
>     @SuppressWarnings("unchecked")
>     public <Z> Target<Z> buildTarget(ImageView view, Class<Z> clazz) {
>         if (GlideDrawable.class.isAssignableFrom(clazz)) {
>             return (Target<Z>) new GlideDrawableImageViewTarget(view);
>         } else if (Bitmap.class.equals(clazz)) {
>             return (Target<Z>) new BitmapImageViewTarget(view);
>         } else if (Drawable.class.isAssignableFrom(clazz)) {
>             return (Target<Z>) new DrawableImageViewTarget(view);
>         } else {
>             throw new IllegalArgumentException("Unhandled class: " + clazz
>                     + ", try .as*(Class).transcode(ResourceTranscoder)");
>         }
>     }
> }
> ~~~
>
> + 终于到底了！！！！终于到底了！！！这个工厂，注释的大概意思是，生成正确的图片格式的Target对象咯,，。回想下前面的步骤，在哪一步确定图片格式？？在这个获取这个对象DrawableTypeReques时已经确定了？？
>
> + 再看看郭大怎么说
>
>   > 可以看到，在buildTarget()方法中会根据传入的class参数来构建不同的Target对象。那如果你要分析这个class参数是从哪儿传过来的，这可有得你分析了，简单起见我直接帮大家梳理清楚。这个class参数其实基本上只有两种情况，如果你在使用Glide加载图片的时候调用了asBitmap()方法，那么这里就会构建出BitmapImageViewTarget对象，否则的话构建的都是GlideDrawableImageViewTarget对象。至于上述代码中的DrawableImageViewTarget对象，这个通常都是用不到的，我们可以暂时不用管它。也就是说，通过glide.buildImageViewTarget()方法，我们构建出了一个GlideDrawableImageViewTarget对象。那现在回到刚才into()方法的最后一行，可以看到，这里又将这个参数传入到了GenericRequestBuilder另一个接收Target对象的into()方法当中了。我们来看一下这个into()方法的源码：
>
> + 然后我们设置的ImageVIew也终于用上了，我翻了下源码，好像并没有真的设置图片到ImageVIew中，只是在测量ImageView的大小。那现在回到刚才into()方法的最后一行，可以看到，这里又将这个参数传入到了GenericRequestBuilder另一个接收Target对象的into()方法当中了。我们来看一下这个into()方法的源码：
>
> ~~~java
>  public <Y extends Target<TranscodeType>> Y into(Y target) {
>         Util.assertMainThread();
>         if (target == null) {
>             throw new IllegalArgumentException("You must pass in a non null Target");
>         }
>         if (!isModelSet) {
>             throw new IllegalArgumentException("You must first set a model (try #load())");
>         }
>
>         Request previous = target.getRequest();
>
>         if (previous != null) {
>             previous.clear();
>             requestTracker.removeRequest(previous);
>             previous.recycle();
>         }
>
>         Request request = buildRequest(target);
>         target.setRequest(request);
>         lifecycle.addListener(target);
>         requestTracker.runRequest(request);
>
>         return target;
>     }
> ~~~
>
> + 看看重点的地方就是这个Request对象，这个对象是用来发起加载请求的，我擦，终于到了发起网络请求的地了，我就一直纳闷了，前面一共有两个说可以加载的对象，ModelLoader，另一个是Target，那就有意思了，前面两个对象所谓的加载是加载什么？？？？先留着这个疑问，看看这个怎么构建这个实例
>
> ~~~java
> //先在这里又调用了一个方法
> private Request buildRequest(Target<TranscodeType> target) {
>         if (priority == null) {
>             priority = Priority.NORMAL;
>         }
>         return buildRequestRecursive(target, null);
>     }
>
> //这个方法就复杂了，90%的代码都是在处理缩略图的。所以还是直接看返回语句又调用了一个方法
>     private Request buildRequestRecursive(Target<TranscodeType> target, ThumbnailRequestCoordinator parentCoordinator) {
>         if (thumbnailRequestBuilder != null) {
>             if (isThumbnailBuilt) {
>                 throw new IllegalStateException("You cannot use a request as both the main request and a thumbnail, "
>                         + "consider using clone() on the request(s) passed to thumbnail()");
>             }
>             // Recursive case: contains a potentially recursive thumbnail request builder.
>             if (thumbnailRequestBuilder.animationFactory.equals(NoAnimation.getFactory())) {
>                 thumbnailRequestBuilder.animationFactory = animationFactory;
>             }
>
>             if (thumbnailRequestBuilder.priority == null) {
>                 thumbnailRequestBuilder.priority = getThumbnailPriority();
>             }
>
>             if (Util.isValidDimensions(overrideWidth, overrideHeight)
>                     && !Util.isValidDimensions(thumbnailRequestBuilder.overrideWidth,
>                             thumbnailRequestBuilder.overrideHeight)) {
>               thumbnailRequestBuilder.override(overrideWidth, overrideHeight);
>             }
>
>             ThumbnailRequestCoordinator coordinator = new ThumbnailRequestCoordinator(parentCoordinator);
>             Request fullRequest = obtainRequest(target, sizeMultiplier, priority, coordinator);
>             // Guard against infinite recursion.
>             isThumbnailBuilt = true;
>             // Recursively generate thumbnail requests.
>             Request thumbRequest = thumbnailRequestBuilder.buildRequestRecursive(target, coordinator);
>             isThumbnailBuilt = false;
>             coordinator.setRequests(fullRequest, thumbRequest);
>             return coordinator;
>         } else if (thumbSizeMultiplier != null) {
>             // Base case: thumbnail multiplier generates a thumbnail request, but cannot recurse.
>             ThumbnailRequestCoordinator coordinator = new ThumbnailRequestCoordinator(parentCoordinator);
>             Request fullRequest = obtainRequest(target, sizeMultiplier, priority, coordinator);
>             Request thumbnailRequest = obtainRequest(target, thumbSizeMultiplier, getThumbnailPriority(), coordinator);
>             coordinator.setRequests(fullRequest, thumbnailRequest);
>             return coordinator;
>         } else {
>             // Base case: no thumbnail.
>             return obtainRequest(target, sizeMultiplier, priority, parentCoordinator);
>         }
>     }
>
>
> //这个方的return又调用一个方法，郭大说，注意这个obtain()方法需要传入非常多的参数，而其中很多的参数我们都是比较熟悉的，像什么placeholderId、errorPlaceholder、diskCacheStrategy等等。因此，我们就有理由猜测，刚才在load()方法中调用的所有API，其实都是在这里组装到Request对象当中的。所以在前面的Builder时只是进行图片加载前的一些初始化设置，然后全部组装到Request中，
>     private Request obtainRequest(Target<TranscodeType> target, float sizeMultiplier, Priority priority,
>             RequestCoordinator requestCoordinator) {
>         return GenericRequest.obtain(
>                 loadProvider,
>                 model,
>                 signature,
>                 context,
>                 priority,
>                 target,
>                 sizeMultiplier,
>                 placeholderDrawable,
>                 placeholderId,
>                 errorPlaceholder,
>                 errorId,
>                 fallbackDrawable,
>                 fallbackResource,
>                 requestListener,
>                 requestCoordinator,
>                 glide.getEngine(),
>                 transformation,
>                 transcodeClass,
>                 isCacheable,
>                 animationFactory,
>                 overrideWidth,
>                 overrideHeight,
>                 diskCacheStrategy);
>     }
> }
> ~~~
>
> + 看看这个GenericRequest.obtain()方法
>
> ~~~java
>     public static <A, T, Z, R> GenericRequest<A, T, Z, R> obtain(
>             LoadProvider<A, T, Z, R> loadProvider,
>             A model,
>             Key signature,
>             Context context,
>             Priority priority,
>             Target<R> target,
>             float sizeMultiplier,
>             Drawable placeholderDrawable,
>             int placeholderResourceId,
>             Drawable errorDrawable,
>             int errorResourceId,
>             Drawable fallbackDrawable,
>             int fallbackResourceId,
>             RequestListener<? super A, R> requestListener,
>             RequestCoordinator requestCoordinator,
>             Engine engine,
>             Transformation<Z> transformation,
>             Class<R> transcodeClass,
>             boolean isMemoryCacheable,
>             GlideAnimationFactory<R> animationFactory,
>             int overrideWidth,
>             int overrideHeight,
>             DiskCacheStrategy diskCacheStrategy) {
>         @SuppressWarnings("unchecked")
>         GenericRequest<A, T, Z, R> request = (GenericRequest<A, T, Z, R>) REQUEST_POOL.poll();
>         if (request == null) {
>             request = new GenericRequest<A, T, Z, R>();
>         }
>         request.init(loadProvider,
>                 model,
>                 signature,
>                 context,
>                 priority,
>                 target,
>                 sizeMultiplier,
>                 placeholderDrawable,
>                 placeholderResourceId,
>                 errorDrawable,
>                 errorResourceId,
>                 fallbackDrawable,
>                 fallbackResourceId,
>                 requestListener,
>                 requestCoordinator,
>                 engine,
>                 transformation,
>                 transcodeClass,
>                 isMemoryCacheable,
>                 animationFactory,
>                 overrideWidth,
>                 overrideHeight,
>                 diskCacheStrategy);
>         return request;
>     }
> ~~~
>
> + 也就那样，先new一个 GenericRequest，然后进行init()然后返回，意思就是load()方法后的Builder的设置，最后是被封装到 GenericRequest这个实例里面。再看看这个request怎么用
>
> ~~~java
>    /**
>      * Starts tracking the given request.
>      */
>     public void runRequest(Request request) {
>         requests.add(request);
>         if (!isPaused) {
>             request.begin();
>         } else {
>             pendingRequests.add(request);
>         }
>     }
> ~~~
>
> 
>
> + 可以看到，开始了加载 ，进去看看begin()方法
>
> ~~~java
> @Override
>     public void begin() {
>         startTime = LogTime.getLogTime();
>         if (model == null) {  //这里啊，这个model就是我们传入的url，如果是null，那是不是要设置占位图？去看看
>             onException(null);
>             return;
>         }
>
>         status = Status.WAITING_FOR_SIZE;
>         if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
>             onSizeReady(overrideWidth, overrideHeight);
>         } else {
>             target.getSize(this);
>         }
>
>         if (!isComplete() && !isFailed() && canNotifyStatusChanged()) {
>             target.onLoadStarted(getPlaceholderDrawable());
>         }
>         if (Log.isLoggable(TAG, Log.VERBOSE)) {
>             logV("finished run method in " + LogTime.getElapsedMillis(startTime));
>         }
>     }
> ~~~
>
> + ​        if (model == null) {  //这个model就是我们传入的url，如果是null，那是不是要设置占位图？去看看
>
> ~~~java
>  @Override
>     public void onException(Exception e) {
>         if (Log.isLoggable(TAG, Log.DEBUG)) {
>             Log.d(TAG, "load failed", e);
>         }
>
>         status = Status.FAILED;
>         //TODO: what if this is a thumbnail request?
>         if (requestListener == null || !requestListener.onException(e, model, target, isFirstReadyResource())) {
>             setErrorPlaceholder(e);
>         }
>     }
> ~~~
>
> +  继续到这个setErrorPlaceholder(e);方法
>
> ~~~java
>   private void setErrorPlaceholder(Exception e) {
>         if (!canNotifyStatusChanged()) {
>             return;
>         }
>
>     //注意注意。注意注意！！！看到这里了吗！！！去获取占位图了！！，这个方法是request里面的
>         Drawable error = model == null ? getFallbackDrawable() : null;
>         if (error == null) {
>           error = getErrorDrawable();
>         }
>         if (error == null) {
>             error = getPlaceholderDrawable();
>         }
>     
>     //然后调用Target的方法，估计是去设置图片啦，但是需要注意，Target是一个抽象类，记得前面我们获取Target时也就两种类型。挑一个看看
>         target.onLoadFailed(e, error);
>     }
> ~~~
>
> ~~~java
> //就是在这里设置错误的图片的  
> public void onLoadFailed(Exception e, Drawable errorDrawable) {
>         view.setImageDrawable(errorDrawable);
>     }
> ~~~
>
> + 前面说的Target可以回调加载过程的一些状态接口，还能加载图片，这里不就是这样吗？所以说Target所谓的加载是这个意思？只要找到发起网络请求的地方问题就解决了，回到begin方法
>
> ~~~java
> //先判断有没有设置大小，没有就去测量测量后还是要调用onSizeReady()，所以看看这个方法
> if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
>             onSizeReady(overrideWidth, overrideHeight);
>         } else {
>             target.getSize(this);
>         }
> ~~~
>
> ---
>
> 
>
> 
>
> 
>
> 



















