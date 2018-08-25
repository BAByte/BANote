[toc]
# 用ScrollTo()和ScrollBy()实现View的拖动
## 先说两句
View的拖动有很多种实现方法，
+ 根据你滑动的坐标去绘制View
+ 通过LayoutParams根据手指滑动坐标去改变view在屏幕的具体坐标
+ > 调用public void offsetLeftAndRight(int offset)用于左右移动方法或public void offsetTopAndBottom(int offset)用于上下移动。
 如：button.offsetLeftAndRignt(300)表示将button控件向左移动300个像素
+ 使用ViewGroup的scrollBy和scrollTo方法（可以配合使用，可以单独使用）


但是上面的前三种方法都是针对View的本身移动，而最后一种是针对ViewGroup的内容，也就是说是这个ViewGroup的内容在移动，这个ViewGroup不移动，该文章就来讲讲如何用这种方式实现View的拖动

## 必备知识 
+ 写ViewGroup必须记得写测量子View的方法，代码如下：
~~~java
 /**
     * 注意：如果你写的是一个ViewGroup，那就要去测量子View的大小，否则可能会不显示
     *
     * @param
     * @return
     * @throws
     * @author BA on 2018/2/5 0005
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            // 为ScrollerLayout中的每一个子控件测量大小
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
    }
~~~
+ 如果是直接继承自ViewGroup，还要根据你的需求去复写onLayout()方法
+ 事件分发（这个有笔记，自己查）
### scrollTo方法
这个方法的意思是用初始坐标点一直作为坐标起点，然后移动到指定的距离
### scrollBy
这个方法的意思是以当前坐标点为起点，移动到指定距离

### 注意
两个方法的参数都是指距离而不是坐标，一定要记住！！！

## 用ScrollTo实现简单的移动
~~~java
public class MyScrollerView extends LinearLayout {
    private static final String TAG = "MyScrollerView";
    private float downY; //记录按下的坐标
    private float parentHeight; //上方边界，y大于这个值当然就不能继续滑动
    
    //由于获取当前控件的高度时是以屏幕左上角开始计算的，所以要减去状态栏和actionbar的高度
    int actionBarHeight; 
    int statusBarHeight1 ;
    
    boolen isUp=false;//是否滑上的状态

    public MyScrollerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
     
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }


        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
        }
    }

    /**
     * 注意：如果你写的是一个ViewGroup，那就要去测量子View的大小，否则可能会不显示，另一点就
     * 是，如果是直接继承自ViewGroup，那子VIew的排列方法你也要确认，就是复写onLayout()方法
     *
     * @param
     * @return
     * @throws
     * @author BA on 2018/2/5 0005
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            // 为ScrollerLayout中的每一个子控件测量大小
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }

        int h=MeasureSpec.getSize(heightMeasureSpec)-actionBarHeight+statusBarHeight1;
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),h);
        parentHeight=getMeasuredHeight();

        Log.d(TAG, "onMeasure: "+h+"::"+actionBarHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent: down");
                //记录按下的坐标
                downY = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: move");
                //记录手指移动到的坐标
                float moveY = event.getRawY();
                //计算手指滑动的距离
                float distance = (moveY - downY);
                
                int toScroll = 0;

                //下滑，如果是已经在下面，就不能滑出最小边界
                if (distance > 0 && isUp) {
                    toScroll = (int) (parentHeight - distance);
                    //如果是在最上面，然后向下滑动，那就要改变状态
                    if (toScroll < 0)
                        isUp = false;
                    
                } else if (distance < 0) {
                //上滑，如果是在最上面，就不能滑出最大边界
                    if (isUp)
                        toScroll = (int) parentHeight;
                    else
                        //上滑
                        toScroll = -(int) distance;
                }

                //上滑的过程中不能滑出最大边界
                if (toScroll > parentHeight) {
                    Log.d(TAG, "onTouchEvent: st");
                    toScroll = (int) parentHeight;
                    isUp = true;
                }

                //开始滑动
                scrollTo(0, toScroll);
         
        }
        Log.d(TAG, "onTouchEvent: ");
        return super.onTouchEvent(event);
    }
}
~~~

### 小知识点
#### 使用getScrollY()来简化移动代码

getScrollY()和getScrollX(),方法会返回上一次用ScrollTo()或者ScrollBy()移动完成的位置，我们看看怎么用
~~~java
    case MotionEvent.ACTION_MOVE:
        Log.d(TAG, "onTouchEvent: move");
            float moveY = event.getRawY();
            
            //获取手指滑动的距离
            float distance = (moveY - downY);
            
            //计算出View需要滑动的距离，假设我们是第一次移动，那getScrollY就是0，那么移动距离等于我们手指滑动的距离，如果已经把Vew移动到最上方，那getScrollY就会是父布局的高度，减去我们手指滑动的距离正好是View初始位置到手指的距离
            int toScroll = (int) (getScrollY() - distance);

            //如果View已经处于下方，那么getScrollY的值将会是0，因为距离初始坐标值为0，那么自然不能让它滑下去
            if (toScroll < 0) {
                toScroll = 0;
            } else if (toScroll > parentHeight) {
            
            //不能让它继续滑上去
                Log.d(TAG, "onTouchEvent: st");
                toScroll = (int) parentHeight;
            }
            scrollTo(0, toScroll);
            downY = moveY;

            return true;
~~~

## 用ScrollBy实现简单滑动
ScrollBy就是在把移动后的位置做为下一次移动的起点，我们看看代码：只有下面这个地方不一样而已
~~~java
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent: down");
                downY = event.getRawY();
                return true; //一定要返回这个，不懂就看看事件分发笔记
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: move");
                float moveY = event.getRawY();
                float distance = (moveY - downY);
                float toScroll=0;

                if (getScrollY()-distance<parentHeight&&(getScrollY()!=0||distance<0)){
                    toScroll=-distance;
                }
                scrollBy(0,(int) toScroll);


                downY = moveY;
                return true;
~~~
是不是突然代码少了很多，其实思想是一样的

## 总结
只需要明白两者的差别，ScrollTo是一直以初始坐标为起点，而ScrollBy是以上一次移动后的位置作为下一次移动的起点，两者的参数都是代表要移动的距离，是距离！！！不是坐标，一定要记住，是距离！！！！