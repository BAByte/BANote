[toc]
 # Scoller简单使用
 参考：http://blog.csdn.net/guolin_blog/article/details/48719871
 ## 先说两句
 在写悬浮窗拖动的时候就遇到一个问题，为什么别人的拖动都有一个很平滑的拖动效果，但是我的没有？于是乎我就觉得，可以用类式动画里面的插值器来实现平滑的移动效果，但是！！！还好我没有自己写，，，，
 ## 主角登场
 Scoller原理其实没有多复杂，只是实现了从一个值到另一个值的过渡，具体怎么过渡，嗯，举个例子吧，从0变化到1，其实有很多种变法，比如x=y，匀速从0变到1，又比如sinX=y，（0=<x<=pi/2)，减速从0变到1，学过动画的你肯定很熟悉吧？这xxx不就是插值器的功能吗？对的，这就是插值器的功能，只是这个类封装起来更好用了，不信你去翻翻源码看看，是不是用了插值器去实现值的变换，又或者去参考这个文章:http://blog.csdn.net/xiaanming/article/details/17483273
 ## 经常用的接口
 ### 设置初始值以及结束值的接口
 ~~~java
 //没有设置滑动时间的接口，会用默认的时间
 public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }
 
 //有时间的接口
   public void startScroll(int startX, int startY, int dx, int dy, int duration) {
       ...
    }
    
看看参数的意思，带有start的代表初始值，带有d的代表变化量，什么意思呢?最后过渡出来的结果是start+d，就是一个数学公式。end-start=d；d代表变化量
 ~~~
 
 ### 获取过渡值的接口
 ~~~java
 //获取过渡的x
  public final int getCurrX() {
        return mCurrX;
    }
    
     public final int getCurrY() {
        return mCurrY;
    }
 ~~~
 
 ### 判断是否过渡完的接口
 ~~~java
  public boolean computeScrollOffset() {
        ...
    }
~~~

## 应用
看了接口后有没有很开心？是不是很少，而且逻辑很明确？那接下来就来使用了，我们来看看需求吧，
### 需求
我的要求是一个View默认在下方，然后向上滑动滑到一半距离就松开手指，然后View会自动滑到顶部。
### 分析
其实有两部分，第一部分是手指还在滑动的时候，第二部分是手指松开的时候，而要View自动平滑的滚动的地方就在第二部分。先看看第一部分怎么去实现
### 实现一
在另一篇笔记有写，叫《用ScrollTo()和ScrollBy()实现View的拖动》
### 实现二
我直接拿代码出来分析，但是你要去把搞懂《实现一》
~~~java
public class MyScrollerView extends LinearLayout {
    private static final String TAG = "MyScrollerView";
    private float downY;
    private Scroller scroller;
    private float parentHeight; //上方边界，y大于这个值当然就不能继续滑动
    int actionBarHeight;
    int statusBarHeight1;
    private boolean isUp = false;

    public MyScrollerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //初始化
        scroller = new Scroller(context);

    ...


...

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            ...
            case MotionEvent.ACTION_UP:
               //重点出现了，如果已经滑动的距离大于某个阈值就自动平滑的向上移动View，这里取的是父布局高度的3分之一
                if (getScrollY() >= (parentHeight) / 3) {
                    Log.d(TAG, "onTouchEvent: up");
                    
                    //设置初始值
                    scroller.startScroll(0, getScrollY(), 0, (int) parentHeight - getScrollY(), 500);
                    
                    //刷新界面，调用这个后系统会调用computeScroll()方法，下面会讲
                    invalidate();
                } else {
                 //重点出现了，如果已经滑动的距离大于某个阈值就自动平滑的向下移动View，这里取的是父布局高度的3分之一
                    scroller.startScroll(0, getScrollY(), 0, -getScrollY(), 500);
                    invalidate();
                }
                return true;
        }
        Log.d(TAG, "onTouchEvent: ");
        return super.onTouchEvent(event);
    }


//这个方法也是重点，如何平滑的过渡，就是在这里设置的，不过这个方法写法一般不会变
    @Override
    public void computeScroll() {
    //判断是否过渡完成
        if (scroller.computeScrollOffset()) {
            //从Scroller取出过渡值，然后移动
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            Log.d(TAG, "computeScroll: " + scroller.getCurrY());
            
            //继续过渡
            invalidate(); 
        }
    }
}
~~~

## 总结，没什么好总结的，总结什么的不存在的