[toc]

# RecyclerView自定义滑动一段距离的速度
参考：http://blog.csdn.net/lixpjita39/article/details/78777436
## 先说两句
RecyclerView本来就有指定滑动到某个位置，有两种方法：
~~~java
//无动画
scrollToPosition(int position)

//有动画
smoothScrollToPosition(int position)
~~~
## 需求
我要求是有动画的滑动，而且滑动的速度我要自己自定义
## 思路
选择有动画的那个方法，按照文章说的，看看该方法是怎么实现的，简单的说就是找源码，这是一个很好的思路
~~~java
//smoothScrollToPosition的源代码
    public void smoothScrollToPosition(int position) {
        if (mLayoutFrozen) {
            return;
        }
        if (mLayout == null) {
            Log.e(TAG, "Cannot smooth scroll without a LayoutManager set. " +
                    "Call setLayoutManager with a non-null argument.");
            return;
        }
        mLayout.smoothScrollToPosition(this, mState, position);
    }
~~~

---
看最后一行，有个mLayout，滑动的实际操作是他来实现的，这个mLayout是什么鬼？其实就是下面这个代码设置的
~~~java
mRecyclerView.setLayoutManager(mLinearLayoutManager); 
~~~

再看看这个mLayout.smoothScrollToPosition(this, mState, position);"代码的源码
~~~java
   @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
            int position) {
        LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
~~~
---
又跑到LinearSmoothScroller，看看LinearSmoothScroller的源码咯，找到有关速度的代码，简单撒，搜索一下就出来了
~~~java
   /**
     * Calculates the scroll speed.
     *
     * @param displayMetrics DisplayMetrics to be used for real dimension calculations
     * @return The time (in ms) it should take for each pixel. For instance, if returned value is
     * 2 ms, it means scrolling 1000 pixels with LinearInterpolation should take 2 seconds.
     */
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
    }
~~~

看看displayMetrics，这个是获取屏幕大小的，所以MILLISECONDS_PER_INCH这个就是我们可以改的，但是怎么改？
## 解决
我们可以继承LinearLayoutManager，复写smoothScrollToPosition方法，里面用到LinearSmoothScroller（计算速度的方法在这个类，前面有说），唯一能动态设置的就是LinearLayoutManager，我们只能继承它，然后复写smoothScrollToPosition方法，在里面再想办法复写计算速度的方法，有两种，第一是再继承LinearSmoothScroller然后复写里面的方法，第二是匿名内部类，直接复写，这里我们看第二种
~~~
public class ScrollLinearLayoutManager extends LinearLayoutManager {
    private static final float MILLISECONDS_PER_INCH = 25f;
    public ScrollLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, final int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext())
        {

            @Nullable
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return ScrollLinearLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}
~~~