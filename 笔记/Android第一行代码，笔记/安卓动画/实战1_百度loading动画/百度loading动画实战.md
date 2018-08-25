[TOC]

# 实现百度Loading效果



## 先说两句

> 学习安卓动画后第一次尝试实战，其实我以前一直有一个很大的疑问，一个动画，是应该在onDraw()方法中画出来，还是用动画写出来。两种方法都可以实现动画效果，而且两种都可以使用图片或者代码绘制。在onDraw()中是将图片直接就绘制出来了，形状就更不用说，用ObjectAnimator对象实现的动画，图片你只能inflate进来，而且其实底层还是要绘制一次，所以会不会影响性能？至于很普通的形状当然可以用xml的**<Shape>**标签画出来。所以下面我想要将一个动画用两种方法实现一下

## 需求

> ![baiduloading](D:\Android第一行代码，笔记\安卓动画\实战1_百度loading动画\baiduloading.gif)

## 分析

> + 3个不同颜色的圆形，可以用图片，也可以用xml画，如果是onDraw()方法就直接画圆，或者绘制图片咯
> + 然后再分析运动的情况，是水平来translation一次，而且在来回的过程是先减速后加速的，插值器就是上一篇文章写的先减速后加速的Interpolator
> + 然后在到中间点的时候啊，会换颜色，而且换颜色也是有规律的，我们把三种颜色的圆放在数组里，然后将第一个颜色不断往后移，移到最后一个后，再将当前处于第一个的颜色再往后移动，这样的循环移动，就可以实现有规律的变化
> + 颜色怎么换？？那就看你用什么方法实现这个动画了，下面会在不同的方法中写出来

## 简单的用ObjectAnimator实现

> + 一开始我是去ps画3个圆出来，然后弄成.9文件，真是蠢到家了啊，后来才想到，直接用xml画啊，啊啊啊好蠢
> + 既然用的是ObjeceAnimator实现，我们可以直接针对一个view实现移动动画，你看上面有三个圆，我就用三个imageVIew的帧布局将这三个圆形设置成背景，事实上ImageVIew的位置是不变的，只是背景颜色变了，看下面代码
> + 形状的xml代码，这里只给一个
>
> ~~~xml
> <?xml version="1.0" encoding="utf-8"?>
> <shape xmlns:android="http://schemas.android.com/apk/res/android"
>     android:shape="oval">
>
>     <solid
>         android:color="@color/loadingRed"/>
>
>     <size
>         android:height="20dp"
>         android:width="20dp"/>
>
> </shape>
> ~~~
>
> 
>
> ---
>
> + 布局文件的代码
>
> ~~~xml
> <?xml version="1.0" encoding="utf-8"?>
> <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
>     android:layout_width="match_parent"
>     android:layout_height="match_parent">
>
>     <ImageView
>         android:layout_gravity="center"
>         android:id="@+id/red"
>         android:layout_width="wrap_content"
>         android:layout_height="wrap_content"
>         android:src="@drawable/red"/>
>     <ImageView
>         android:layout_gravity="center"
>         android:id="@+id/green"
>         android:layout_width="wrap_content"
>         android:layout_height="wrap_content"
>         android:src="@drawable/blue"/>
>     <ImageView
>         android:layout_gravity="center"
>         android:id="@+id/blue"
>         android:layout_width="wrap_content"
>         android:layout_height="wrap_content"
>         android:src="@drawable/gray"/>
>     
> </FrameLayout>
> ~~~
>
> ---
>
> + 为什么要写这个布局？？傻啊，我们要写一个View啊，写这个布局后不就可以直接在类里面直接inflate了吗，还有！！！这里有很有意思的地方。以前我一直以为在自定义VIew中只有在继承控件来自定义View的时候才会在将类继承ViewGroup，今天才知道，当你的View需要加载布局，还是必须继承ViewGroup，为啥？？只有ViewGroup能添加子布局啊，重点!!!下面直接看View的代码吧，很简单的。
>
> ~~~java
> /**
>  * Created by BA on 2017/10/10 0010.
>  *
>  * @Function : 仿百度刷新loading
>  */
>
> public class MyLoading extends FrameLayout {
>
>     //记录资源的位置,其实就是将这几张图片按顺序放在数组里面，等下变颜色的时候就操作数组
>     //为什么操作数组？在ObjectAnimator中你确实可以操作颜色变化，但是这个颜色变化是指原本的
>     //View有这个属性，而且还有达到效果，显然你要改的是imageView显示的下面的图片，所以说ImageView
>     //只是负责移动，变颜色就是将显示的图片资源改变而已
>     private int[] id = new int[]{R.drawable.red, R.drawable.blue, R.drawable.gray};
>
>     //用来存放ImageView的集合，有没有也没关系，只是不用的话，你的三个ImageView
>     //就要设置成全局变量，你喜欢咯
>     private List<ImageView> list;
>
>     //球水平移出去的圆
>     private float rudias = 150f;
>
>     //这个就是用来记录在完成一次完整的移动的过程中被移动的颜色位置
>     private int center = 0;
>   
>    //动画集合
>     private AnimatorSet set;
>
>     public MyLoading(Context context) {
>         super(context);
>         init();
>     }
>
>     public MyLoading(Context context, @Nullable AttributeSet attrs) {
>         super(context, attrs);
>         init();
>     }
>
>     public MyLoading(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
>         super(context, attrs, defStyleAttr);
>         init();
>     }
>
>     /**
>      *@fuction 加载布局，然后从布局里面找到ImageView的实例，初始化后直接开启动画
>      *@parm
>      *@return
>      *@exception
>      */
>     public void init() {
>         LayoutInflater.from(getContext()).inflate(R.layout.myloading, this, true);
>         findImage();
>         startAnim();
>     }
>
>     /**
>      *@fuction 找到三个ImageView的实例
>      *@parm
>      *@return
>      *@exception
>      */
>     private void findImage() {
>         list = new ArrayList<>();
>         list.add((ImageView) findViewById(R.id.red));
>         list.add((ImageView) findViewById(R.id.blue));
>         list.add((ImageView) findViewById(R.id.green));
>     }
>
>     /**
>      *@fuction 开启动画，具体看里面
>      *@parm
>      *@return
>      *@exception
>      */
>     private void startAnim() {
>         //左边移动的动画，先跑过去，然后跑回来
>         ObjectAnimator animatorLeft = ObjectAnimator.ofFloat
>                 (list.get(0), "translationX", list.get(0).getTranslationX()
>                         , -rudias, list.get(0).getTranslationX());
>         animatorLeft.setDuration(800);
>         animatorLeft.setRepeatCount(-1); //一直重复
>         animatorLeft.setInterpolator(new MyDAInterpolator()); //前面说的插值器，先减速后加速
>
>         //右边的动画，显然有一个是不用动的
>         ObjectAnimator animatorRight = ObjectAnimator.ofFloat
>                 (list.get(2), "translationX", list.get(2).getTranslationX()
>                         , rudias, list.get(2).getTranslationX());
>         animatorRight.setDuration(800);
>         animatorRight.setRepeatCount(-1);
>         animatorRight.setInterpolator(new MyDAInterpolator());
>
>         //将两个动画同时开始
>         set = new AnimatorSet();
>         
>         //设置动画监听，在重复一次动画后，就切换颜色
>         //这里注意一下了，前面为啥要跑出去再跑回来，就是为了在回来的时候
>         //为一次动画的结束，正好就可以切颜色啦
>         animatorLeft.addListener(new AnimatorListenerAdapter() {
>             @Override
>             public void onAnimationRepeat(Animator animation) {
>                 sweep(); //切颜色的函数
>             }
>         });
>
>         set.play(animatorLeft).with(animatorRight);
>         set.start();
>     }
>
>     /**
>      *@fuction 改变数组中的颜色顺序后，重新给ImageView设置背景颜色
>      *@parm
>      *@return
>      *@exception
>      */
>     private void sweep() {
>         if (center < 2) {
>             int t = id[center];
>             id[center] = id[center+1];
>             id[center+1] = t;
>             center++;
>
>             //重新设置背景
>             list.get(0).setImageResource(id[0]);
>             list.get(1).setImageResource(id[1]);
>             list.get(2).setImageResource(id[2]);
>         }else {
>             center=0;
>             sweep(); //等2说明一次完整的移动已经完成，重新开始呗
>         }
>     }
>   
>   //记得销毁动画
>    @Override
>     protected void onDetachedFromWindow() {
>         super.onDetachedFromWindow();
>         set.cancel();
>     }
>
> }
> ~~~
>
> ---

##onDraw()里面直接绘制实现

> + 其实里面用到的还是ValuesAnimator，圆的移动不就是位置的改变嘛，具体就看代码吧，也不复杂，当然这里的位置是封装在PositionBean里面的，所以用到了之前写的TypeEvalutor
>
> ~~~java
> /**
>  * Created by BA on 2017/10/10 0010.
>  *
>  * @Function : 仿百度loading动画
>  */
>
> public class MyLoadingByDraw extends View {
>
>     //画笔
>     private Paint paintLeft, paintCenter, paintRight;
>
>     //颜色数组
>     private int[] color = new int[]{getResources().getColor(R.color.loadingRed)
>             , getResources().getColor(R.color.loadingBlue)
>             , getResources().getColor(R.color.loadingGray)};
>
>     //圆的位置
>     private PositionBean leftPosition, centerPosition, rightPosition;
>
>     //动画结束的位置
>     PositionBean leftPositionEnd, rightPositionEnd;
>
>     //圆的半径
>     private float radius = 30f;
>
>     //为了将坐标原点移到View的中心
>     private float myW, myH;
>
>     //用来记录一颜色循环的次数
>     private int center;
>
>
>     public MyLoadingByDraw(Context context) {
>         super(context);
>         init();
>     }
>
>     public MyLoadingByDraw(Context context, @Nullable AttributeSet attrs) {
>         super(context, attrs);
>         init();
>     }
>
>     public MyLoadingByDraw(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
>         super(context, attrs, defStyleAttr);
>         init();
>     }
>
>     @Override
>     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
>         super.onSizeChanged(w, h, oldw, oldh);
>         myW = w;
>         myH = h;
>     }
>
>     public void init() {
>         if (paintLeft==null) {
>             paintLeft = new Paint();
>             paintCenter = new Paint();
>             paintRight = new Paint();
>
>             paintLeft.setAntiAlias(true);
>             paintCenter.setAntiAlias(true);
>             paintRight.setAntiAlias(true);
>         }
>
>         paintLeft.setColor(color[0]);
>         paintCenter.setColor(color[1]);
>         paintRight.setColor(color[2]);
>     }
>
>     /**
>      *@fuction 先判断位置有没有初始化，没有的话肯定没有开启动画，
>      * 有的话说明已经开启了动画，直接就绘制就好了
>      *@parm 画布
>      *@return
>      *@exception
>      */
>     @Override
>     protected void onDraw(Canvas canvas) {
>         //在绘制的笔记中有写
>         canvas.save();
>         canvas.translate(myW/2, myH/2);
>         if(leftPosition==null){
>             initPosition();
>             drawCircle(canvas);
>             startAnim();
>         }else {
>             drawCircle(canvas);
>         }
>         canvas.restore();
>     }
>
>     /**
>      *@fuction 初始化位置，初始位置全都放在中心
>      *@parm
>      *@return
>      *@exception
>      */
>     public void initPosition(){
>         //移动的距离
>         float myTranslationX = 150f;
>
>         //初始化中间圆的位置
>         centerPosition = new PositionBean();
>         centerPosition.setX(0);
>         centerPosition.setY(0);
>
>         //初始化未开启动画的位置，初始一个就够了，因为时对称的，右边的直接加个负号
>         leftPosition = new PositionBean();
>         leftPosition.setX(0);
>         leftPosition.setY(0);
>
>         //结束的位置
>         leftPositionEnd = new PositionBean();
>         leftPositionEnd.setX(- myTranslationX);
>         leftPositionEnd.setY(0);
>     }
>
>     /**
>      * @return
>      * @throws
>      * @fuction 画圆
>      * @parm
>      */
>     private void drawCircle(Canvas canvas) {
>         canvas.drawCircle
>           (leftPosition.getX(), leftPosition.getY(), radius, paintLeft);
>         canvas.drawCircle
>           (centerPosition.getX(), centerPosition.getY(), radius, paintCenter);
>         canvas.drawCircle
>           (-leftPosition.getX(), -leftPosition.getY(), radius, paintRight);
>     }
>
>     /**
>      *@fuction 动画值的过渡只需要过渡一个position对象，左右是对称的
>      *@parm
>      *@return
>      *@exception
>      */
>     private void startAnim() {
>         ValueAnimator animatorLeft = ValueAnimator
>                 .ofObject(new PositionEvaluetor(), leftPosition
>                           , leftPositionEnd, leftPosition);
>         animatorLeft.setDuration(800);
>         animatorLeft.setRepeatCount(-1);
>         animatorLeft.setInterpolator(new MyDAInterpolator());
>
>         animatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
>             @Override
>             public void onAnimationUpdate(ValueAnimator animation) {
>                 leftPosition = (PositionBean) animation.getAnimatedValue();
>                 invalidate(); //数据更新后重新绘制
>             }
>         });
>
>         //重新开始一个动画后切换颜色
>         animatorLeft.addListener(new AnimatorListenerAdapter() {
>             @Override
>             public void onAnimationRepeat(Animator animation) {
>                 sweep();
>             }
>         });
>
>         //开始动画
>         animatorLeft.start();
>     }
>
>     /**
>      *@fuction 改变数组中的颜色顺序后，重新给ImageView设置背景颜色
>      *@parm
>      *@return
>      *@exception
>      */
>     private void sweep() {
>         if (center < 2) {
>             int t = color[center];
>             color[center] = color[center+1];
>             color[center+1] = t;
>             center++;
>
>             //重新设置背景
>             init();
>         }else {
>             center=0;
>             sweep(); //等2说明一次完整的移动已经完成，重新开始呗
>         }
>     }
> }
> ~~~
>
> ---

## 总结

> + 一对比就知道，显然是ObjectAniamator简单，但是两种方法其实都不难，看你喜欢哪个咯
> + 在继承控件自定义VIew时就继承已经有的，如果你的View要加载布局就继承ViewGroup。否则就继承View
> + 先分析有什么形状，图形，然后分析动画的运动，最后才到颜色什么的，总的来说这次实战还是比较简单的

