[TOC]

# 自定义View实战1-饼状图

## 写在前面

> 跟着一篇文章写了这个View，其实就是继续巩固图形的基础绘制，以及为View添加触摸事件的实战，。这里并没有涉及到绘制的流程，以及测量，所以一旦大小改变，这个View其实会出问题的，这里就不管了

## 需求

+ 一个根据数据显示不同数据占比的饼状图，点击后会突出某一个饼状图

![捕获](D:\Android第一行代码，笔记\自定义View之路\自定义View之实战1饼状图\捕获.PNG)

---

## 技术分析

> + 根据数据绘制一个个扇形，这里的数据源使用Bean存储，应该包含扇形的颜色和数据大小
> + 一个要根据数据的数量和大小来计算数据的扇形面积，事实上在绘制扇形的时候我们需要的是起始点以及所占角度，所以要统计一下这些东西
> + 我们又知道画扇形时要用一个矩形去限制扇形的大小，所以这个矩形的大小也要设置
> + 然后开始画出一个个扇形，其实并不难，就是设置好画笔，然后用路径画扇形，每一次扇形的起始点要回坐标原点，而且每一次扇形的起始角度都应该是上一个扇形的结束的角度
> + 为了计算方便，将View的坐标原点移动到View的中心
> + 画完扇形后画直线，这个直线有意思了，每个直线的起点要在所属扇形边缘的中心，那么就要计算出这个点的位置了，屏幕的xy轴其实是正常的垂直翻转。所以取x的正半轴为0边，根据当前扇形的终边到0边的关系，再根据半径就可以算出点坐标，然后延长半径就再计算就可以得到终点的坐标，就画出来了
> + 然后到文字，文字的绘制重点就在起始坐标，和上面线的画法算法，但是半径要延长一点，不然就和线重合了，而且文字都是从左到右的，右边的肯定没问题，但是左边的显示就会有问题了，所以左边的需要偏移一点，就根据角度来决定是否偏移就好
> + 触摸事件，你不可能用onClick()决定点击哪个扇形的，这个扇形是一个VIew，所以要根据你点击的坐标来判断是否在这个扇形范围内，如何判断呢？用坐标显然是不合理的，因为扇形最容易得到的就是它所占的角度，根据你点击的坐标，算出在xy轴的度数，然后判断最后一个哪些扇形在这个范围内，最后一个肯定就是你点击的扇形，所以你要记录扇形的最终角度，以及换算点击坐标在坐标轴上的角度，然后判断哪一个位置的扇形被点击了
> + 点击后变突出，那就简单了，将限制的矩形变大一点，然后再画一次，被点击的扇形不就变大了？

### 代码

### Bean

~~~java
/**
 * Created by BA on 2017/10/1 0001.
 *
 * @Function : 作为自定义扇形的Bean
 */

public class ArcBean {
    private int color;
    private float percentage;

    public int getColor() {
        return color;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
~~~

---

### 换算坐标的工具类

~~~java
/**
 * Created by BA on 2017/10/2 0002.
 *
 * @Function : 根据坐标获取在坐标系中的角度值
 *  为什么要加180？你不加，然后打印一下就知道了，下面的360同理
 */

public class MathUtil {

    public static float getAngle(float x, float y) {
            if (x < 0)
                return (float) Math.toDegrees(Math.atan(y / x)) + 180;
            else if (y < 0)
                return (float) Math.toDegrees(Math.atan(y / x)) + 360;

            return (float) Math.toDegrees(Math.atan(y / x));

    }
}

~~~

---

###View

~~~java
/**
 * Created by BA on 2017/10/1 0001.
 *
 * @Function : 自定义一个饼状图
 */

public class MyArcView extends View {

    private static final String TAG = "MyArcView";

    public MyArcView(Context context) {
        super(context);
        initPaint();
    }

    public MyArcView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public MyArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    //用来记录按下的坐标
    private float downX;
    private float downY;

    ///记下当前点击的扇形
    private int position = -1;

    /**
     * @return true代表消费了该事件。反之没有
     * @throws
     * @fuction 当用来处理点击某一个扇形，然后突出某一个扇形的函数
     * @parm 触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: //记录按下的坐标
                downX = event.getX();
                downY = event.getY();

                //将点击的点的坐标偏移到View的中心
                downY -= mH / 2;
                downX -= mW / 2;

                //先判断点击的是不是扇形，根据半径来算
                if (Math.sqrt(downX * downX + downY * downY) <= mW / 2) {

                    //算出当前点击在坐标系上所占的角度
                    float nowTouchAngle = MathUtil.getAngle(downX, downY);

                    //找出最后一个在这个角度范围内的扇形
                    for (int i = 0; i < myAngleArray.length; i++) {
                        if (nowTouchAngle >= myAngleArray[i]) {
                            position = i + 1;
                        }
                    }

                    //如果没有找到，说明是第一个扇形
                    if (position == -1)
                        position = 0;
                }

                //重新绘制
                if (position != -1) {
                    invalidate();
                    Log.d(TAG, "onTouchEvent: 重绘");
                }
                break;
        }
        return true;
    }


    //画笔
    Paint mPaint;

    //路径，等下用来画扇形
    Path mPath;

    /**
     * @return
     * @throws
     * @fuction 初始化画扇形的画笔，你也可在这里初始化线条，文字的画笔
     * @parm
     */
    public void initPaint() {
        Log.d(TAG, "initPaint: ");
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//设置消除锯齿
        mPaint.setStyle(Paint.Style.FILL);
        mPath = new Path();
    }

    /**
     * @return
     * @throws
     * @fuction 将在这里进行扇形图的绘制
     * @parm 画布
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //保存当前canvas的状态，这个状态一般指的是，画布的缩放，旋转，移动的状态
        //保存的原因是，等下移动后绘制完后一旦重绘，那就会在移动后的基础上继续移动
        // ，所以在移动完后都要把画布恢复到这个状态
        canvas.save();

        //移动画布的坐标系原点
        canvas.translate(mW / 2, mH / 2);

        //画饼图
        drawPieChart(canvas);

        //将画布恢复到上一次save的状态
        canvas.restore();
    }

    //记录所有扇形的最大角度的数组，用来判断点击的区域是否是当前扇形所占区域
    private float[] myAngleArray;

    /**
     * @return
     * @throws
     * @fuction 画扇形的具体函数, 顺便记录了扇形所占的面积
     * @parm
     */
    private void drawPieChart(Canvas canvas) {
        Log.d(TAG, "drawPieChart: ");
        float startAngle = 0;
        for (int i = 0; i < myList.size(); i++) {

            // 移动到0，0点，因为画完一次后起点就变成了扇形最后一个点，所以要
            //将中心点移动到0，0
            mPath.moveTo(0, 0);
            ArcBean pieCharBean = myList.get(i);

            // 绘制当前扇形区域颜色
            mPaint.setColor(myList.get(i).getColor()); //获取扇形颜色

            //获取扇形所占角度
            float sweepAngle = (pieCharBean.getPercentage() / dataCount) * 360;
            Log.d(TAG, "drawPieChart: " + sweepAngle + "::" + rectF.top + "::" + rectF.right);

            //如果当前扇形和被选中扇形是一致的话，就要画大点
            if (position != i) {
                //用路径画扇形
                mPath.arcTo(rectF, startAngle, sweepAngle - 1);

                //画线
                drawLine(canvas, startAngle, sweepAngle, rectF);

                //画文字
                drawText(canvas, startAngle, sweepAngle, rectF,
                         Math.round(sweepAngle / 360.0* 100) + "%");
            }
            else{
                RectF selectRectF = new RectF();
                selectRectF.left = rectF.left - 20;
                selectRectF.right = rectF.right + 20;
                selectRectF.top = rectF.top - 20;
                selectRectF.bottom = rectF.bottom + 20;
                mPath.arcTo(selectRectF, startAngle, sweepAngle - 1);
                drawLine(canvas, startAngle, sweepAngle, selectRectF);

                //四舍五入
                drawText(canvas, startAngle, sweepAngle, selectRectF,
                         Math.round(sweepAngle / 360.0 * 100) + "%");

                //重置的原因是因为需要判断是否点击的是第一个扇形
                position = -1;
            }

            // 绘制扇形
            canvas.drawPath(mPath, mPaint);

            //这里要记得将起始角度累加，不然全都在原来的起点绘制，会被覆盖的
            startAngle += sweepAngle;

            //记录当前扇形的最大的角度
            myAngleArray[i] = startAngle;

            //路径会对上一次的属性缓存，然后下一次设置就没用了，所以要重置
            mPath.reset();
        }
    }

    /**
     * @return
     * @throws
     * @fuction 画线条
     * @parm 后面两是是前面的所有扇形的角度之和，以及当前扇形的角度,最后一个是能画的区域
     */
    public void drawLine(Canvas canvas, float startAngle, float sweepAngle, RectF lineRecf) {

        //直线的起点，中点，以及角度
        float x, y, endX, endY, lineAngle;

        //用三角函数来计算每个扇形边中心的位置
        lineAngle = (float) ((startAngle + sweepAngle / 2) * Math.PI / 180);
        x = (float) Math.cos((double) lineAngle) * lineRecf.right;
        y = (float) Math.sin((double) lineAngle) * lineRecf.right;
        endX = (float) Math.cos((double) lineAngle) * (lineRecf.right + 30);
        endY = (float) Math.sin((double) lineAngle) * (lineRecf.right + 30);

        //画直线
        Paint linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setStrokeWidth(2);
        linePaint.setColor(Color.BLACK);
        canvas.drawLine(x, y, endX, endY, linePaint);
    }

    /**
     * @return
     * @throws
     * @fuction 绘制饼图的文字
     * @parm 倒数第二个是能画的区域，最后一个是具体的信息
     */
    public void drawText(Canvas canvas, float startAngle, 
                         float sweepAngle, RectF textRecF, String text) {

        //用三角函数来计算每个扇形边中心的位置，然后沿着半径偏移一段距离
        float lineAngle = (float) ((startAngle + sweepAngle / 2) * Math.PI / 180);
        float x = (float) Math.cos((double) lineAngle) * (textRecF.right + 50);
        float y = (float) Math.sin((double) lineAngle) * (textRecF.right + 50);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);

        //文字默认是左到右，那么在左边显示的时候就会出问题了，所以继续偏移
        if (startAngle + sweepAngle > 100 && startAngle + sweepAngle < 270) {
            //测量文字大小，
            float size = textPaint.measureText(text);
            canvas.drawText(text, x - size, y, textPaint);
        } else
            canvas.drawText(text, x, y, textPaint);
    }

    //扇形属性的数据源
    private List<ArcBean> myList = null;
    //用来统计数据的总大小，后面用来确定每个数据占的百分比
    private int dataCount = 0;

    /**
     * @return
     * @throws
     * @fuction 设置扇形的数据源
     * @parm 数据源集合
     */
    public void setMyList(List<ArcBean> list) {

        myList = list;

        //算出数据的总大小
        for (ArcBean a : list) {
            dataCount += a.getPercentage();
        }

        //初始化存储角度值的数组
        myAngleArray = new float[myList.size()];
        Log.d(TAG, "setMyList: " + dataCount);
    }

    //用来限制扇形的矩形区域,以及当前View的宽高
    private RectF rectF;
    private float mW;
    private float mH;

    /**
     * @return
     * @throws
     * @fuction 这个方法会在View打大小改变时被调用，我们在这里设置扇形所占的区域
     * @parm
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged:");
        float mySize = Math.min(w, h); //获取正方形的矩形作为扇形的外接矩形
        int mRadius = (int) (mySize * 0.7f / 2); //为了不让矩形直接占了整个屏幕宽度，所以乘的0.7

        //记录当前View的宽高
        mW = w;
        mH = h;

        //设置矩形区域，由于等下要将画布的坐标原点移动到屏幕中心，所以有-的
        rectF = new RectF();
        rectF.left = -mRadius;
        rectF.right = mRadius;
        rectF.top = -mRadius;
        rectF.bottom = mRadius;
    }
}
~~~

---

### Activity.xml

~~~xml
 <com.example.ba.attributesetdemo.myview.MyArcView
     android:layout_width="match_parent"
     android:layout_height="match_parent"
     android:id="@+id/myArcView"/>
~~~

---

### Activity

~~~java
public class MainActivity extends AppCompatActivity {

    //扇形的数据源
    private List<ArcBean> list;

    //扇形的颜色
    private int color[]={0xFFCCFF00,0xFF6495ED,
            0xFFE32636,0xFF800000,0xFF808000,0xFFFF8C69,
            0xFF808080,0xFFE68800,0xFF7CFC00};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyArcView arcView=(MyArcView)findViewById(R.id.myArcView);
        initList();
        arcView.setMyList(list);
    }

    public void initList(){
        list=new ArrayList<>();
        for (int i=0;i<color.length;i++){
            ArcBean arcBean=new ArcBean();
            arcBean.setColor(color[i]);
            arcBean.setPercentage(i+1);
            list.add(arcBean);
        }
    }
}

~~~

