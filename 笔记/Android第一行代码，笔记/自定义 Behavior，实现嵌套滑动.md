[toc]
# 自定义 Behavior，实现嵌套滑动、平滑切换周月视图的日历
## 先说两句
在此之前我就很想去知道那种嵌套滑动的空间是如何写出来的，但是一直没有去学习，直到看到了这篇文章：https://juejin.im/post/5ab9c553f265da237f1e5079

其实很久前就看到了，就是一直没学
## 需求
![image](https://user-gold-cdn.xitu.io/2018/3/27/16265aab5384eb19?imageslim)

---
## 必备知识点
在学第一行代码的时候就是了解过AppBarLayout和CoordinateLayout嵌套滑动实现炫酷ToolBar的demo，CoordinateLayout是一个协调子布局，让子布局实现滑动的联动效果的ViewGroup，他其实是个FramLayout布局。那AppBarLayout在和CoorDinateLayout配合的时候有哪些特别的地方呢？那就是他有个属性"app:layout_behavior"是不是没有印象？当然没有啊，因为AppBarLayout的这个属性不用你写，他在代码内部自己写好了，而你要做的就是给RecyclerView设置这个属性。

那这个属性有什么用？这个属性是设置了一个Behavior，设置给谁？学过自定义VIew的人就知道，当然是给CoordinateLayout的，只要前面是带了layout_xxx的属性，都是给父布局用的。

那Behavior又有什么用？就是用来协调子布局的类了，所以真正的学习重心出来了。就是Behavior要怎么去写！
## 需求分析
从上面的动图中可以看出，整个View其实是有两个重要部分组成，一个是头部的日历，一个是底部的可滑动控件，而折叠的其实是日历，（我们等下用随便的布局来代替），那我们可以写一个Behavior专门控制头部布局的显示位置，当然如果头部的显示位置变了RecyclerView的位置肯定也跟着改变。

那控制显示区域位置的Behavior有了，剩下的就是怎么拿到滑动距离咯。那肯定要给RecyclerView写一个Behavior来实现滑动的监听吧？其实不然，你给哪个View写Behovior写都可以。反正Behavior有父布局，你可以拿到所有布局，那你会问：那滑动监听怎么办？看下去

注意！这里我再强调一次，Behavior是给父布局协调所有子布局用的，一个子布局当然是对应一个Behavior。意思就是说，你的Behavior肯定有父布局的实例，然后你根据需求，用这个父布局的实例来处理子布局显示问题。

## 猜想Behavior原理
为什么说猜想？因为我没有时间去看源码，但是猜一下吧，我认为其实就是用了观察者模式，父布局是一个广播器，他直接就拦截了所有子控件的事件，为什么？因为这样他就可以让我们，注意，这里说的是我们（你不拿的话是系统根据你点击的位置去判断有没有View来分发的，当然你可以使用那些分发方法来拦截，但是扩展性很低，看下去你就知道了）。我们给View设置一个Behavior，这个其实就是一个Runnable，只要View设置了Behavior就代表了在父布局注册了观察者，当父布局拦截到触摸事件后，会去调用所有注册观察者的Behavior的那些回调方法，由Behavior去选择要不要消费事件，没有消费的话，父布局会将事件按照正常的分发下去。你看，Behavior是我们自己爱怎么写就怎么写，那扩展性还不高？？那是不是滑动监听其实每个Behavoir都会拿到？是的。 

我们如果我们直接用事件分发来实现这个，其实也可以，比如说你的RecyclerView就算不处理事件，返回给父布局，那父布局也很可以让头部的View拿到事件，来控制显示，而且RecyclerView也是可以拿到这个事件进行处理，这样一来就是父布局里面的两个VIew都处理了事件

注意！这里我再强调一次，Behavior是给父布局协调所有子布局用的，一个子布局当然是对应一个Behavior。意思就是说，你的Behavior肯定有父布局的实例，然后你根据需求，用这个父布局的实例来处理子布局显示问题。

## 自定义Behavior前的准备工作
看它 Behavior 源码发现，它继承了 ViewOffsetBehavior。ViewOffsetBehavior 的作用是方便改变控件的位置和获取偏移量。所以这里我再偷个懒，把源码里的 ViewOffsetBehavior 直接拷出来用了。
我们自定义两个 Behavior，列表控件的 CalendarScrollBehavior 和日历控件的 CalendarBehavior，都继承 ViewOffsetBehavior。

下面是两个源码类，直接copy
+ ViewOffsetHelper类
~~~java
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewParent;

/**
 * Copy from Android design library
 */
public class ViewOffsetHelper {

    private final View mView;

    private int mLayoutTop;
    private int mLayoutLeft;
    private int mOffsetTop;
    private int mOffsetLeft;

    public ViewOffsetHelper(View view) {
        mView = view;
    }

    public void onViewLayout() {
        // Now grab the intended top
        mLayoutTop = mView.getTop();
        mLayoutLeft = mView.getLeft();

        // And offset it as needed
        updateOffsets();
    }

    private void updateOffsets() {
        ViewCompat.offsetTopAndBottom(mView, mOffsetTop - (mView.getTop() - mLayoutTop));
        ViewCompat.offsetLeftAndRight(mView, mOffsetLeft - (mView.getLeft() - mLayoutLeft));

        // Manually invalidate the view and parent to make sure we get drawn pre-M
        if (Build.VERSION.SDK_INT < 23) {
            tickleInvalidationFlag(mView);
            final ViewParent vp = mView.getParent();
            if (vp instanceof View) {
                tickleInvalidationFlag((View) vp);
            }
        }
    }

    private static void tickleInvalidationFlag(View view) {
        final float y = ViewCompat.getTranslationY(view);
        ViewCompat.setTranslationY(view, y + 1);
        ViewCompat.setTranslationY(view, y);
    }

    /**
     * Set the top and bottom offset for this {@link ViewOffsetHelper}'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    public boolean setTopAndBottomOffset(int offset) {
        if (mOffsetTop != offset) {
            mOffsetTop = offset;
            updateOffsets();
            return true;
        }
        return false;
    }

    /**
     * Set the left and right offset for this {@link ViewOffsetHelper}'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    public boolean setLeftAndRightOffset(int offset) {
        if (mOffsetLeft != offset) {
            mOffsetLeft = offset;
            updateOffsets();
            return true;
        }
        return false;
    }

    public int getTopAndBottomOffset() {
        return mOffsetTop;
    }

    public int getLeftAndRightOffset() {
        return mOffsetLeft;
    }
}
~~~
---

+ ViewOffsetBehavior类
~~~java
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Copy from Android design library
 */
public class ViewOffsetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private ViewOffsetHelper mViewOffsetHelper;

    private int mTempTopBottomOffset = 0;
    private int mTempLeftRightOffset = 0;

    public ViewOffsetBehavior() {
    }

    public ViewOffsetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        // First let lay the child out
        layoutChild(parent, child, layoutDirection);

        if (mViewOffsetHelper == null) {
            mViewOffsetHelper = new ViewOffsetHelper(child);
        }
        mViewOffsetHelper.onViewLayout();

        if (mTempTopBottomOffset != 0) {
            mViewOffsetHelper.setTopAndBottomOffset(mTempTopBottomOffset);
            mTempTopBottomOffset = 0;
        }
        if (mTempLeftRightOffset != 0) {
            mViewOffsetHelper.setLeftAndRightOffset(mTempLeftRightOffset);
            mTempLeftRightOffset = 0;
        }

        return true;
    }

    protected void layoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        // Let the parent lay it out by default
        parent.onLayoutChild(child, layoutDirection);
    }

    public boolean setTopAndBottomOffset(int offset) {
        if (mViewOffsetHelper != null) {
            return mViewOffsetHelper.setTopAndBottomOffset(offset);
        } else {
            mTempTopBottomOffset = offset;
        }
        return false;
    }

    public boolean setLeftAndRightOffset(int offset) {
        if (mViewOffsetHelper != null) {
            return mViewOffsetHelper.setLeftAndRightOffset(offset);
        } else {
            mTempLeftRightOffset = offset;
        }
        return false;
    }

    public int getTopAndBottomOffset() {
        return mViewOffsetHelper != null ? mViewOffsetHelper.getTopAndBottomOffset() : 0;
    }

    public int getLeftAndRightOffset() {
        return mViewOffsetHelper != null ? mViewOffsetHelper.getLeftAndRightOffset() : 0;
    }
}
~~~

## 布局文件
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<layout>
<android.support.design.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.example.ba.behaviordemo.MainActivity">

<!--这是我自定义的布局，就是一个帧布局-->
    <com.example.ba.behaviordemo.TopLayout
        android:background="@color/colorAccent"
        android:id="@+id/calendar"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        app:layout_behavior="@string/calendar_behavior" />

    <android.support.v7.widget.RecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/calendar_scrolling_behavior" />


</android.support.design.widget.CoordinatorLayout>
</layout>
~~~

我们看到上面有这样的属性："   app:layout_behavior="@string/calendar_behavior" 这个String怎么写？这个属性可以直接指定完整的路径，也可以像安卓官方一样在string里面指定

## 在String声明我们的Behavior
~~~xml
<resources>
    <string name="app_name">BehaviorDemo</string>
       <!--完整的包名加类名，这个两个类我们还没写，后面会写-->
    <string name="calendar_behavior" translatable="false">com.example.ba.behaviordemo.TopLayoutBehavior</string>
    <string name="calendar_scrolling_behavior" translatable="false">com.example.ba.behaviordemo.BottomLayoutScrollBehavior</string>
</resources>
~~~

## BottomLayoutScrollBehavior
这个类我们准备用来控制子控件的显示以及位置
~~~java
/**
 * Created by BA on 2018/4/19 0019.
 * 这个类是给RecyclerView用的，他的功能其实不多，就是设置自己的显示位置
 */

public class BottomLayoutScrollBehavior extends ViewOffsetBehavior<RecyclerView>  {
    private static final String TAG = "BottomLayoutScrollBehavior";
    //我们要缩小的是上方日历的显示区域，其实就是改变显示高度
    private int calendarHeight;

    public BottomLayoutScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //建立依赖，就是给当前Behavior和view建立依赖，这里只需要判断是不是你要的View即可
    //剩下系统会帮你搞定
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, RecyclerView child, View dependency) {
        //如果是日历控件就建立依赖
        return dependency instanceof TopLayout;
    }

    
     /** 
     * 控制View的显示
     * @param parent 父布局CoordinatorLayout 
     * @param abl 使用此Behavior的AppBarLayout 
     * @param layoutDirection 布局方向 
     * @return 返回true表示子View重新布局，返回false表示请求默认布局 
     */  
    @Override
    protected void layoutChild(CoordinatorLayout parent, RecyclerView child, int layoutDirection) {
        super.layoutChild(parent, child, layoutDirection);
        if (calendarHeight == 0) {
            final List<View> dependencies = parent.getDependencies(child);
            for (int i = 0, z = dependencies.size(); i < z; i++) {
                View view = dependencies.get(i);
                if (view instanceof TopLayout) {
                    calendarHeight = view.getMeasuredHeight();
                }
            }
        }

        //控制Recycler的显示
        child.setTop(calendarHeight);
        child.setBottom(child.getBottom() + calendarHeight);

        Log.d(TAG, "layoutChild: ");
    }
}
~~~

## TopLayoutBehavior
~~~java
/**
 * Created by BA on 2018/4/19 0019.
 * 用来监听滑动事件，并且做出相应的处理,注意，这滑动事件是指整个CoordinatorLayout内的滑动事件
 */
public class TopLayoutBehavior extends ViewOffsetBehavior<TopLayout> {
   private static final String TAG = "TopLayoutBehavior";
    //我们可以移动的头部大小
    private int maxOffsetH;
    //头部的大小
    private int topLayoutH;

    private boolean canAutoSlide=true;
    private boolean isUp;

    private Scroller scroller;
    public TopLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller=new Scroller(context);
    }


    //onStartNestedScr 的返回值决定是否接收嵌套滑动事件。我们判断，只要是上下滑动，就接收
    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull TopLayout child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    //onNestedPreScroll 这个方法是在准备滚动之前调用的，它带有滚动偏移量 dy。我们在这里消费掉dy。
    //dy是什么？就是上一次View的Top减去你滑动后的View的Top。由于滑动时我们会滑动一段但是停下（手指没有抬起）
    //然后又继续滑动，那么dy的值肯定是波动的，因为只要有一点点的停顿，Top就一直变，
    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull TopLayout child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        //滑到最顶部时，返回false，意思是不能下拉了
        // 列表未滑动到顶部时，不处理
        if (target.canScrollVertically(-1)) {
            return;
        }

        //获取头部的高度
        if (topLayoutH == 0) {
            topLayoutH = child.getMeasuredHeight();
            //设置顶部会留下的最小高度，这里我要他完全隐藏
            maxOffsetH = topLayoutH-100;
        }

        // MathUtils.clamp方法就是限定范围取值。一共三个参数，第一个是你的值，第二个和第三个是你要取的值域
        //你的值在值域内就返回你的值，当你的值大于就会返回值域的最大值，反之亦然
        //这个是根据dy值算出头部布局应该偏移的量，getTopAndBottomOffset()是拿到上一次View停下的Top来计算，这样算出的值
        //不会上下波动
        int scrollerOffset = MathUtils.clamp(
                getTopAndBottomOffset() - dy, -maxOffsetH, 0);

        //这个方法就是用来控制View偏移，正值就下移，负值就上移
        setTopAndBottomOffset(scrollerOffset);


        //我们不仅仅要头部布局移动，我们还要底部的布局跟着移动，这里就是实现联动的关键
        final CoordinatorLayout.Behavior behavior =
                ((CoordinatorLayout.LayoutParams) target.getLayoutParams()).getBehavior();
        if (behavior instanceof BottomLayoutScrollBehavior) {
            final BottomLayoutScrollBehavior listBehavior = (BottomLayoutScrollBehavior) behavior;

            //这个方法就是用来控制View偏移，正值就下移，负值就上移
            listBehavior.setTopAndBottomOffset(scrollerOffset);

            //在嵌套滑动机制中，我们判断，如果是上滑且顶部控件未完全隐藏，则消耗掉dy，即consumed[1]=dy;
            // 如果是下滑且内部View已经无法继续下拉，则消耗掉dy，即consumed[1]=dy，
            // 消耗掉的意思由CoordinatorLayout去消耗事件（其实就是我们写这个这个behavior消耗了dy）
            // 那么当然事件就不会分发给头布局和底下的RecyclerView
            //所以你之前也许有这样的疑问：他是怎么判断事件该分发给谁？其实就是你有没有设置这个consumed[1] = dy;
            //设置了当然代表我behavior已经消耗了这个事件。那头布局和RecyclerVIew是接收不到事件了
            if (scrollerOffset > -maxOffsetH && scrollerOffset < 0) {
                consumed[1] = dy;
            }
        }
    }
    
}
~~~



## 嵌套滑动各个回调方法
~~~java
  /** 
     * 当CoordinatorLayout的子View尝试发起嵌套滚动时调用 
     * 
     * @param parent 父布局CoordinatorLayout 
     * @param child 使用此Behavior的AppBarLayout 
     * @param directTargetChild CoordinatorLayout的子View，或者是包含嵌套滚动操作的目标View 
     * @param target 发起嵌套滚动的目标View(即AppBarLayout下面的ScrollView或RecyclerView) 
     * @param nestedScrollAxes 嵌套滚动的方向 
     * @return 返回true表示接受滚动 
     */  
    @Override  
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes) {  
        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes);  
    }  
  
    /** 
     * 当嵌套滚动已由CoordinatorLayout接受时调用 
     * 
     * @param coordinatorLayout 父布局CoordinatorLayout 
     * @param child 使用此Behavior的AppBarLayout 
     * @param directTargetChild CoordinatorLayout的子View，或者是包含嵌套滚动操作的目标View 
     * @param target 发起嵌套滚动的目标View(即AppBarLayout下面的ScrollView或RecyclerView) 
     * @param nestedScrollAxes 嵌套滚动的方向 
     */  
    @Override  
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes) {  
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);  
    }  
  
    /** 
     * 当准备开始嵌套滚动时调用 
     * 
     * @param coordinatorLayout 父布局CoordinatorLayout 
     * @param child 使用此Behavior的AppBarLayout 
     * @param target 发起嵌套滚动的目标View(即AppBarLayout下面的ScrollView或RecyclerView) 
     * @param dx 用户在水平方向上滑动的像素数 
     * @param dy 用户在垂直方向上滑动的像素数 
     * @param consumed 输出参数，consumed[0]为水平方向应该消耗的距离，consumed[1]为垂直方向应该消耗的距离 
     */  
    @Override  
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed) {  
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);  
    }  
  
    /** 
     * 嵌套滚动时调用 
     * 
     * @param coordinatorLayout 父布局CoordinatorLayout 
     * @param child 使用此Behavior的AppBarLayout 
     * @param target 发起嵌套滚动的目标View(即AppBarLayout下面的ScrollView或RecyclerView) 
     * @param dxConsumed 由目标View滚动操作消耗的水平像素数 
     * @param dyConsumed 由目标View滚动操作消耗的垂直像素数 
     * @param dxUnconsumed 由用户请求但是目标View滚动操作未消耗的水平像素数 
     * @param dyUnconsumed 由用户请求但是目标View滚动操作未消耗的垂直像素数 
     */  
    @Override  
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {  
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);  
    }  
  
    /** 
     * 当嵌套滚动的子View准备快速滚动时调用 
     * 
     * @param coordinatorLayout 父布局CoordinatorLayout 
     * @param child 使用此Behavior的AppBarLayout 
     * @param target 发起嵌套滚动的目标View(即AppBarLayout下面的ScrollView或RecyclerView) 
     * @param velocityX 水平方向的速度 
     * @param velocityY 垂直方向的速度 
     * @return 如果Behavior消耗了快速滚动返回true 
     */  
    @Override  
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY) {  
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);  
    }  
  
    /** 
     * 当嵌套滚动的子View快速滚动时调用 
     * 
     * @param coordinatorLayout 父布局CoordinatorLayout 
     * @param child 使用此Behavior的AppBarLayout 
     * @param target 发起嵌套滚动的目标View(即AppBarLayout下面的ScrollView或RecyclerView) 
     * @param velocityX 水平方向的速度 
     * @param velocityY 垂直方向的速度 
     * @param consumed 如果嵌套的子View消耗了快速滚动则为true 
     * @return 如果Behavior消耗了快速滚动返回true 
     */  
    @Override  
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {  
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);  
    }  
  
    /** 
     * 当定制滚动时调用 
     * 
     * @param coordinatorLayout 父布局CoordinatorLayout 
     * @param abl 使用此Behavior的AppBarLayout 
     * @param target 发起嵌套滚动的目标View(即AppBarLayout下面的ScrollView或RecyclerView) 
     */  
    @Override  
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout abl, View target) {  
        super.onStopNestedScroll(coordinatorLayout, abl, target);  
    }  
~~~


---

## 总结
其实处理滑动事件主要是处理好dy，你要明白dy的含义，我本来想实现滑动到一半松手后自动折叠头部的，但是没成功，以后再写吧
