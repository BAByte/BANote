[TOC]

# 自定义View之View的绘制过程

---

## 先说两句

看了一篇很棒的文章，也按照这篇文章记录一下我的理解。

---

## 理论知识（问答形式）

1. ###什么是View？

   View是一个用户交互的东西，我理解为用户和底层代码逻辑交互的桥梁，对一个软件的操作其实就是对View进行操作

2. ###View有哪些特征？

   + 位置
   + 大小
   + 可交互，比如触摸拖动长按等等
   + 视觉化反馈，比如一个按钮，被选中状态和正常状态所呈现的外观肯定是不一样的

3. ###View的上司有谁？

   + > **ViewGroup**
     >
     > >  其实就是五大布局，我们在用的时候不就在里面包含View吗？
     >
     > **那它和View组合起来又叫什么？**
     >
     > > 如果只是简单的布局嵌套，那当然还叫ViewGroup
     > >
     > > 如果是一个软件里面的所有View那就叫DecorView
     >
     > **什么是DecorView？**
     >
     > > 一个超级复合View，里面可以包括很多ViewGroup，一个ViewGroup里面又可以包括了很多View和很多ViewGroup， 说白了就是一个大集合而已
     >
     > **这个集合是谁的？**
     >
     > > 一套完整的界面，就是一个Window，安卓的一个视图就是一个Window，显然这个Window有一个草鸡复合View，就是一个大集合DecorView

4. ###安卓对View的管理，是怎样的？

   看了第三点估计也就知道了层级关系了吧，一个界面就是一个Window，Window有一个大集合DecorView，大集合包括了很多的ViewGroup和View

   ​

5. ###比喻

   安卓就是一个大森林，里面所有的树就是安卓的所有界面，一棵树就是一个Window，一棵树上所有的东西就是DecorView，一枝树杈上面的所有东西就是ViewGroup，这枝树杈上又有树叶，和其他树杈，树叶就是最基本的View

   ​

6. ###View如何确定大小？

   首先需要知道，所有的View真实大小其实都是矩形区域。

   在写布局时我们会写android:layout_width="***"android:layout_height="***"两个配置，就是开发者告诉ViewGroup，这个View的大小，一般有3种情况

   + ####精确的值：10dp，20dp。。。

     > 意思非常明确，开发者规定了你这个View，只能是这个大小

   + ####match_parent 

     > 开发者对ViewGroup说，你有多少屏幕区域，就给多少这个View

   + ####wrap_content

     > 开发者对ViewGroup说，如果你有足够的屏幕区域。这个View多大你就给它多大

   现在只是开发者和ViewGroup在讨论，这个View的大小，讨论结束后ViewGroup根据自身情况和开发者的要求，会出两个报告，就是两个MeasureSpec对象，一个确定高，一个确定宽。ViewGroup就将着这两个报告给View。

   View在onMeasure方法里面得到报告，然后阅读报告

   ​

   ~~~java
   int widthMode = MeasureSpec.getMode(widthMeasureSpec);
   int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
   int heightMode = MeasureSpec.getMode(heightMeasureSpec);
   int heightSize =  MeasureSpec.getSize(heightMeasureSpec);
   ~~~

   ​

   可以看到 MeasureSpec封装了重要的信息，一共有3种情况

   + Mode是EXACTLY ，精确值，那么Size就会等于开发者指定的值，View只能服从这个值，其实match_parent也是精确值，Size是ViewGroup的所占有的最大区域
   + AT_MOST，对应的是wrap_content,Size的值是ViewGroup的所有的屏幕区域，意思就是说，ViewGroup把所有的区域都给了View，用多少是View自己大大小决定的
   + UNSPECIFIED，View想要多大，就多大，一般很少有这个

   View阅读完后，根据自身的情况，决定了大小后，在onMeasure方法里面调用setMeasuredDimension方法，那么大小就确定啦！！！

   ​

7. ### 那么View是如何确定在屏幕上的位置？

   - View会有一个方法onLayout()在这里进行位置确定


   - 我们需要知道，在加载界面的时候，会有一个setContentView（）方法，这个方法会将ViewGroup加载到DecorView里面的一个FrameLayout里面，这个FrameLayout代表了屏幕所剩下的空间，ViewGroup加载进来后会使用这空间，然后给里面的View分配位置。具体就是，DecorView在屏幕这个场地（FrameLayout）举办了一个活动，ViewGroup是一个学校，被邀请去参加这个活动。DecorView提供场地（FrameLayout），至于学生（View）坐哪里就交给了学校（ViewGroup）来安排了。

     ​

   - 怎么安排？我们在写布局的时候，每一个View都可以通过layout_**来确定View的一些属性，这个方法就是用来确定View位置，大小。但是需要注意！这个属性不是View自己的，在加载（inflate）布局文件的时候，其实这个属性是被ViewGroup加载的，然后会生成LayoutParams对象，将这个View的位置，大小的参数存进去，然后把这个Layoutparames存到对应的View对象中，ViewGroup就可以根据每个View的LayoutParames里面的信息来确定位置，意思就是说View的位置是由ViewGroup来管理的，不同的ViewGroup会有不同的定位方式，比如LinearLayout有水平和垂直。

     ​

   - 说白了就是开发者设置位置，ViewGroup根据开发者的设置来确定位置

   ​

8. ###View的生命周期，这里主要写常用的方法，

   + ####onFinishInflate()

     > View被加载完后，会回调这个方法，可以在这里进行初始化操作

   + #### onLayout()

     > 在这里去确定位置

   + ####onMeasure()

     > 在Infalte时会调用这个方法确定View的大小

   + #### onDraw()

     > 绘制View

   + ####onWindowVisibilityChanged方法

     >  如果你的View所属的Window可见性发生了变化，系统会回调该View的onWindowVisibilityChanged方法，你也可以根据需要，在该方法中完成一定的工作，比如，当Window显示时，注册一个监听器，根据监听到的广播事件改变自己的绘制，当Window不可见时，解除注册，因为此时改变自己的绘制已经没有意义了，自己也要跟着Window变成不可见了。

   + ####onSizeChanged方法

     >  确定View的方法，当ViewGroup中的子View数量增加或者减少，导致ViewGroup给自己分配的屏幕区域大小发生变化时，系统会回调View的onSizeChanged方法，该方法中，View可以获取自己最新的尺寸，然后根据这个尺寸相应调整自己的绘制。

   + ####OnTouchEvent方法

     > 处理触摸事件的

     ####onAttachedToWindow()

     > 当View被添加到屏幕时，但是需要注意的地方就是，不是说界面变成可见了就会调用，而是在一开始创建界面的时候调用，比如跳到后台，再跳回来，就不会调用

     #### onDetachedFromWindow()

     > 当View被从Window移除时，不是说看不见了就调用，也是在完全退出程序后才调用，比如跳到后台就不会调用

   + #### onWindowVisibilityChanged(int)

     > 这里要注意了

     ###生命周期，这里写绘制的过程

     > + onFinishInflate()，加载完View时会调用该方法
     > + 构造函数 。构造函数走一个
     > + 然后到onAttachedToWindow()  做好加载到屏幕的准备时会调用该方法
     > + onMeasure() 然后测量大小
     > + onSizeChange() 确定Viwe大小，上面也说了在View大小改变时也会调用该方法
     > + onLayout( )确定View的位置
     > + onDraw() 开始绘制啦
     > + 退出时onDetachedFromWindow()

   ### 绘制View里面的内容

   + onDraw()

     > 在这个方法中，绘制图片资源啊，什么的，一般需要配合的类有
     >
     > + #### Canvas
     >
     >   > 画布，这个画布的大小默认是View的大小，如果View被缩放了，那么在这里可以对一些图片资源进行缩放的设置，当然还有旋转，移动，所以这个做动画也是可以的吧.
     >   >
     >   > Canvas提供了几个方法，让我们可以方便的对Canvas的状态进行更改和还原。
     >   > 这些方法是：`save()`、`restore()`、`restoreToCount(int saveCount)`。
     >   >
     >   > 我们在对Canvas进行平移、旋转、放大等操作时候，可以调用`save()`方法，将当前修改过的Canvas状态进行保存，调用`restore()` 方法后，会将Canvas还原成最近的一个`save()` 的状态。
     >   >
     >   > `save()`方法还会有一个返回值，我们也可以调用`restoreToCount(int saveCount)`方法，将这个返回值作为参数传递进去，就可以将Canvas还原成某一个特定的`save()`状态。
     >
     >   ~~~java
     >   /** 
     >    * 画布向（100，50）方向平移 
     >    *  
     >    * 参数1: 向X轴方向移动100距离 
     >    * 参数2: 向Y轴方向移动50距离   
     >    */
     >    canvas.translate(100, 50);
     >
     >   /** 
     >    * 在X轴方向放大为原来2倍，Y轴方向方大为原来的4倍 
     >    * 参数1: X轴的放大倍数 
     >    * 参数2: Y轴的放大倍数 
     >    */
     >   canvas.scale(2, 4);
     >
     >   /** 
     >    * 在X轴方向放大为原来2倍，Y轴方向方大为原来的4倍 
     >    * 参数1: X轴的放大倍数 
     >    * 参数2: Y轴的放大倍数 
     >    * 参数3: 原点X坐标
     >    * 参数4: 原点Y坐标
     >    */
     >   canvas.scale(2, 4，100,100);
     >
     >   /** 
     >    * 原点为中心，旋转30度（顺时针方向为正方向 ）
     >    * 参数: 旋转角度 
     >    */
     >   canvas.rotate(30);
     >
     >   /** 
     >    * 以（100,100）为中心，旋转30度，顺时针方向为正方向 
     >    * 参数: 旋转角度 
     >    */
     >   canvas.rotate(30,100,100);
     >   ~~~
     >
     >
     >   ~~~java
     >
     >   #### Paint
     >
     >   > 画笔，用来配合画布画一些东西，看下面
     >
     >   ~~~java
     >   * 参数2：文本的x轴的开始位置 
     >    * 参数2：文本Y轴的结束位置 
     >    * 参数3：画笔对象 
     >    */  
     >   canvas.drawText("开始写字了！",50, 50, p);// 画文本  
     >
     >   /** 
     >    * 参数2：要从第几个字开始绘制 
     >    * 参数3：要绘制到第几个文字 
     >    * 参数4：文本的x轴的开始位置 
     >    * 参数5：文本Y轴的结束位置 
     >    * 参数6：画笔对象 
     >    */  
     >   canvas.drawText("开始写字了！",2,5, 50, 50, p);// 画文本，结果为：“写字了”  
     >   /** 
     >    * 参数2：路径 
     >    * 参数3：距离路径开始位置的偏移量 
     >    * 参数4：距离路径上下的偏移量（可以为负数） 
     >    * 参数5：画笔对象 
     >    */  
     >   canvas.drawTextOnPath("1234567890101123123", path, 0, -50, p);
     >
     >
     >
     >   /**
     >    * 参数1：圆心X 
     >    * 参数2：圆心Y 
     >    * 参数3：半径R 
     >    * 参数4：画笔对象 
     >    */           
     >   canvas.drawCircle(200, 200, 100, p);
     >
     >
     >   画线
     >
     >   /* 
     >    * 参数1：startX 
     >    * 参数2：startY 
     >    * 参数3：stopX 
     >    * 参数4：stopY 
     >    * 参数5：画笔对象 
     >    */   
     >   canvas.drawLine(100, 100, 300, 300, p);// 画线  
     >   /* 
     >    * 同时绘制多条线。 
     >    * 参数1：float数组：每四个一组为一条线。最后不足四个，就忽略那些值。 
     >    * 参数2：画笔对象 
     >    */  
     >   canvas.drawLines(new float[]{100,100,200,200,200,100,300,100}, p);
     >
     >   /* 
     >    *  参数1：float left 
     >    *  参数2：float top 
     >    *  参数3：float right 
     >    *  参数4：float bottom 
     >    */  
     >   RectF oval = new RectF(150, 200, 500, 400);// 画一个椭圆  
     >   canvas.drawOval(oval, p);
     >
     >
     >   /**
     >    *  画圆弧
     >    *  参数1：RectF对象。 
     >    *  参数2：开始的角度。（水平向右为0度顺时针反向为正方向） 
     >    *  参数3：扫过的角度 
     >    *  参数4：是否和中心连线 
     >    *  参数5：画笔对象 
     >    */  
     >   canvas.drawArc(oval, 20, 180, false, p);
     >
     >
     >
     >   /** 
     >    *  矩形 
     >    *  参数1：float left 
     >    *  参数2：float top 
     >    *  参数3：float right 
     >    *  参数4：float bottom 
     >    */  
     >   canvas.drawRect(100,100, 200, 200, p);  
     >
     >   //画圆角矩形    
     >   RectF oval3 = new RectF(80, 260, 200, 300);// 设置个新的长方形    
     >   canvas.drawRoundRect(oval3, 20, 5, p);//第二个参数是x半径，第三个参数是y半径
     >
     >
     >
     >   /**  
     >    * Path类封装复合(多轮廓几何图形的路径  
     >    * 由直线段*、二次曲线,和三次方曲线，也可画以油画。drawPath(路径、油漆),要么已填充的或抚摸  
     >    * (基于油漆的风格),或者可以用于剪断或画画的文本在路径。  
     >    */   
     >   Path path = new Path();  // 路径对象  
     >   path.moveTo(80, 200);// 此点为多边形的起点    
     >   path.lineTo(120, 250);    
     >   path.lineTo(80, 250);    
     >   //....  可以添加多个点。构成多边形  
     >   path.close(); // 使终点和起点链接，构成封闭图形   
     >           canvas.drawPath(path, p);
     >
     >
     >   p.setStyle(Style.STROKE);  
     >   Path path2=new Path();    
     >   path2.moveTo(100, 100);//设置Path的起点   
     >   /** 
     >    * 参数1、2：x1，y1为控制点的坐标值 
     >    * 参数3、4：x2，y2为终点的坐标值 
     >    */  
     >   path2.quadTo(300, 100, 400, 400); //设置贝塞尔曲线的控制点坐标和终点坐标    
     >   path2.quadTo(500, 700, 800, 800);  
     >   canvas.drawPath(path2, p);//画出贝塞尔曲线
     >
     >
     >   /** 
     >    * 参数1、2：点的x、y坐标 
     >    */  
     >   canvas.drawPoint(60, 390, p);//画一个点    
     >   /** 
     >    * 参数1：多个点，每两个值为一个点。最后个数不够两个的值，忽略。 
     >    */  
     >   canvas.drawPoints(new float[]{60,400,65,400,70,400}, p);//画多个点
     >
     >
     >   Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);   
     >   /** 
     >    * 参数1：bitmap对象 
     >    * 参数2：图像左边坐标点 
     >    * 参数3：图像上边坐标点 
     >    */  
     >   canvas.drawBitmap(bitmap, 200,300, p);
     >   ~~~

   + 调用onDraw的方法

     > ```java
     > invalidate();
     > ```

---

## 实战

自定义一个时钟

![捕获](E:\Android第一行代码，笔记\自定义View之路\捕获.PNG)

### 分析

+ 3张背景图
+ 时间变化：系统时间广播
+ View的大小确定
+ View里面资源图像的绘制
+ 当Window中的View数量被改变后，是否需要缩放View

### 代码

~~~java
/**
 * Created by ljh99 on 2017/8/21 0021.
 * function ：
 */

public class MyClock extends View {

    //存放3张图片资源
    private Drawable dialRes;
    private Drawable hourRes;
    private Drawable minRes;

    //记录当前时间
    private Calendar myCalendar;

    //记录最大的图片的大小，用来确定最后View的大小
    private int mDialWith;
    private int mDialHeight;

    //记录View被确定大小后的大小,中心
    int viewWidth;
    int viewHeight;
    int x, y;

    //还记得onWindowVisibilityChanged()方法吗？
    //因为View如果不在当前Window上，我们就不监听时间的变更所以要判断
    private boolean onWindow = false;

    //记录小时，分钟
    private float mHour;
    private float mMin;

    //onSizeChanged方法
    //当Window里面的View数量发生改变时，可能需要缩放我们的View
    private boolean mChange = false;


    //更新秒针的计时器
    Handler hanler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("hhhh", "计时器：" + mHour);
            mHour++;
            sendEmptyMessageDelayed(1, 1000);
            invalidate();
        }
    };

    public MyClock(Context context) {
        super(context);
    }

    public MyClock(Context context,  AttributeSet attrs) {
        super(context, attrs,0);
        init(context);
    }

    public MyClock(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyClock(Context context,  AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("ssss", "OnFni");
        //init(getContext());
    }

    /**
     * function :在里面进行图片资源，时间，等等等的初始化
     * param : 不用管
     * return :
     * exception :
     */
    public void init(Context context) {
        //初始化图片资源
        if (dialRes == null)
            dialRes = context.getDrawable(R.drawable.kedu);
        if (hourRes == null)
            hourRes = context.getDrawable(R.drawable.hour);
        if (minRes == null)
            minRes = context.getDrawable(R.drawable.minute);

        //初始化时间对象
        myCalendar = Calendar.getInstance();

        //获取刻度图片的初始大小
        mDialHeight = dialRes.getIntrinsicHeight();
        mDialWith = dialRes.getIntrinsicWidth();
        Log.d("ssss", "构造2");
    }


    /**
     * function :确定View的大小,在某些情况下我们需要对View进行scale
     * 当View大小大于ViewGroup给的大小时，我们需要对我们的View进行缩放
     * 防止缩放后被拉伸，当然要等比例缩放,除非你规定View的宽高比例，否则可以不复写
     * param : 两个参数包含了View宽高的大小和模式
     * return :
     * exception :
     *
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Log.d("ssss", "测量");
        //View阅读报告
        int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int specHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        //记录宽高需要缩放的倍数
        float wscale = 1.0f;
        float hscale = 1.0f;

        if (specWidthMode != MeasureSpec.UNSPECIFIED && specWidthSize < mDialWith)
            wscale = (float) specWidthSize / mDialWith; //其实是缩放到刚刚好适应给定的大小
        if (specHeightMode != MeasureSpec.UNSPECIFIED && specHeightSize < mDialHeight)
            hscale = (float) specHeightSize / mDialHeight;

        //比如说给的区域是高1宽2，但是你的View确是2x2的，那当然要缩放到1x1
        //这样就不会被拉伸过度了，所以这里的计算方法你可以思考一下
        float scale = wscale < hscale ? wscale : hscale;

        Log.d("ssss", "onMeasure: w" + wscale);
        Log.d("ssss", "onMeasure: h" + hscale);
        Log.d("ssss", "onMeasure: s" + scale);

        //提交最后的View大小
        setMeasuredDimension((int) (mDialWith * scale), (int) (mDialHeight * scale));
    }*/

    /**
     * function : 当View的大小被动态改变时会自动调用该方法
     * param :
     * return :
     * exception :
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d("ssss", "大小改变");
        super.onSizeChanged(w, h, oldw, oldh);
        mChange = true;
    }

    /**
     * function : 当时间发生改变时用来刷新时间，并且对View进行重新绘制
     * param :
     * return :
     * exception :
     */
    private void onTimeChange() {
        long time=System.currentTimeMillis();
        myCalendar.setTimeInMillis(time);
        Log.d("ssss", "时间改变");

        //获取更新的时间
        mHour = myCalendar.get(Calendar.HOUR_OF_DAY);
        mMin = myCalendar.get(Calendar.MINUTE);
        mChange = true;
        Log.d("ssss", "：" + mMin + ":" + mHour);

        //对View进行重新绘制
        invalidate();
    }

    //开启计时器
    public void onSecondChange() {
        hanler.sendEmptyMessage(1);
    }

    //停止计时器
    public void stopScondChange() {
        hanler.removeMessages(1);
    }

    /**
     * function :全局的广播接收器，用来监听时间变化
     * param :
     * return :
     * exception :
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ssss", "广播接收器");
            onTimeChange();
        }
    };

    /**
     * function : 当Vew被加载到Window时会自动调用该方法i，我们在这里注册广播
     * param :
     * return :
     * exception :
     */
    @Override
    protected void onAttachedToWindow() {
        Log.d("ssss", "添加到屏幕");
        super.onAttachedToWindow();
        if (!onWindow) {
            onWindow = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            getContext().registerReceiver(receiver, filter);
        }
        onTimeChange();
        // onSecondChange();
    }

    /**
     * function :当View被从Window移除时会自动调用该方法，我们可以在这里取消注册广播
     * 停止绘制View
     * param :
     * return :
     * exception :
     */
    @Override
    protected void onDetachedFromWindow() {
        Log.d("ssss", "移除");
        super.onDetachedFromWindow();
        if (onWindow) {
            getContext().unregisterReceiver(receiver);
            onWindow = false;
        }
        //stopScondChange();//停止秒数计时器
    }

    /**
     * function :这个方法啊，会在你的View可见性改变时调用，但是需要注意的是，如果你是让软件到后台，这个会改两次
     * param : View里面的3个可见性的常量
     * return :
     * exception :
     */
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mChange = true;//只要调用了这个方法，就有可能跑到后台了，所以要求重新缩放
    }

    /**
     * function :绘制View中的图片资源或者其他
     * param : Canvas就是画布的意思，画布大小是整个View，调用一次就会给一个新的画布
     * return :
     * exception :
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("ssss", "绘制:" + canvas.toString());

        //获取最大的图片资源文件的尺寸，
        // 当View的大小比资源的大小还要小的时候需要对资源进行缩放
        int width = dialRes.getIntrinsicWidth();
        int height = dialRes.getIntrinsicHeight();

        if (mChange) {
            //获取View大小
            viewWidth = super.getRight() - getLeft();
            viewHeight = super.getBottom() - getTop();

            //获取View的中心
            x = viewWidth / 2;
            y = viewHeight / 2;

            //记录宽高需要缩放的倍数
            float wscale = 1.0f;
            float hscale = 1.0f;

            //和onMeasure方法的缩放一样
            if (viewWidth < width)
                wscale = (float) viewWidth / width;
            if (viewHeight < width)
                hscale = (float) viewHeight / height;

            float scale = wscale < hscale ? wscale : hscale;

            //这里是对绘制的资源进行缩放，而不是Vewi，
            //因为等下都要用到这个来绘制，所以这样做
            //的好处就是所有资源图片都缩放了
            canvas.scale(scale, scale, x, y);

            //继续监听是否被改变
            mChange = false;
        }

        //绘制资源图片的时候，需要设置在View中绘制的区域，
        // 这里的计算方法其实就是，在Viwe中规定一个和图片
        //一样大小的矩形，setBounds就是设置边界的意思，4条边嘛
        dialRes.setBounds(x - (width / 2), y - (height / 2), x + (width / 2), y + (height / 2));
        dialRes.draw(canvas);//这个方法就是绘制啦，在画布绘制这个图片
        canvas.save();//保存当前画布的属性，比如画布是否缩放，旋转移动了

        width = minRes.getIntrinsicWidth();
        height = minRes.getIntrinsicHeight();
        minRes.setBounds(x - (width / 2), y - (height / 2), x + (width / 2), y + (height / 2));
        canvas.rotate((mMin / 60f) * 360f, x, y);//画布旋转的意思
        minRes.draw(canvas);

        canvas.restore();//将画布恢复到上一次sava的属性，如果不恢复，上面的旋转，移动都会被叠加
        width = hourRes.getIntrinsicWidth();
        height = hourRes.getIntrinsicHeight();
        hourRes.setBounds(x - (width / 2), y - (height / 2), x + (width / 2), y + (height / 2));
        canvas.rotate(mHour / 12f * 360f + mMin / 2f, x, y);
        hourRes.draw(canvas);
    }
}
~~~



## 小结

+ 先要知道View的原理
+ 需要复写哪些方法？那些方法有什么作用
+ 区分开View大小和绘制图片的大小，两者不一样
+ 当你的VIew里面有过时的方法，那么在xml预览中将无法显示出来
+ 我的建议是在onFinishinflate()方法里面进行初始化，因为你不知道会调用哪一个构造函数，当然如果需要用到构造函数里的一些参数才能初始化的，除了context，其他还是在构造函数初始化，因为该方法居然比构造函数先跑
+ onMeasure()方法，如果你没有很特殊的要求，比如规定View只能是正方形，那就不需要重写的