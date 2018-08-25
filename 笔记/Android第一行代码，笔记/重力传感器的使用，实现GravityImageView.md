[toc]
# 重力传感器的使用，实现GravityImageView
## 代码
~~~java
  SensorManager sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener listener=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float xx= event.values[SensorManager.DATA_X];
                float yy= event.values[SensorManager.DATA_Y];
                float zz= event.values[SensorManager.DATA_Z];

             
                x.setText(xx+"");
                y.setText(yy+"");
                z.setText(zz+"");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_UI );
        //sensorManager.unregisterListener();
    }
~~~

## 解释
其实挺简单的，就是获取系统服务，然后设置监听，但是设置监听时有一个参数需要了解一下
~~~java
SensorManager的频率总共分为4等，分别是：

SENSOR_DELAY_FASTEST最灵敏，快的然你无语
SENSOR_DELAY_GAME游戏的时候用这个，不过一般用这个就够了
SENSOR_DELAY_NORMAL比较慢。
SENSOR_DELAY_UI最慢的
~~~

## 实战
### 需求
我们来写一个可以根据移动手机会移动ImageView的功能
### 分析
其实不难，ImageView内部的图片设置为中心放大，然后通过计算ImageView和内部图片的大小，就可以算出我们可以移动多少距离
### 难点
你会发现重力传感器三个轴的数值是-9.8到9.8的浮点数(这个要看你哪里的重力加速度，其实是会影响的，你可以减去你当地的重力加速度)，而且这个数值高位变化很快，这样就会造成图片一直抖动，那我们把获取的值变小，那么高位的变化就会慢，另一种方法就是，由于我们获取的是加速度，你可以计算成速度来使用，（v=v0+at），t是传感器两次变化的时间差值是纳秒级别的，要转化成毫秒使用比较好，那样我们要用的数值高位就不会说猛的变化，看看处理这个数值的代码吧
~~~java
/**
 * Created by BA on 2018/2/25 0025.
 *
 * @Function : 观察者的管理，用来监听传感器变化，计算出需要移动的大小
 */

public class MySensorObserver implements SensorEventListener {
    private static final String TAG = "MySensorObserver";
    private SensorManager mSensorManager;

    // 纳秒转换成秒
    private static final float NS2S = 1.0f / 1000000000.0f;

    //传感器上一次获取加速度的时间
    private long mLastTimestamp;

    // Y轴的速度
    private double mRotateRadianY;

    // X轴的速度
    private double mRotateRadianX;

    //初始化，注册传感器
    public void register(Context context) {
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);

        mLastTimestamp = 0;
        mRotateRadianY = mRotateRadianX = 0;
    }


    //取消注册传感器
    public void unregister() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
    }


    //监听传感器数值
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mLastTimestamp == 0) {
            mLastTimestamp = event.timestamp;
            return;
        }

        //先获取加速度
        float rotateX = Math.abs(event.values[0]);
        float rotateY = Math.abs(event.values[1]);
        float rotateZ = Math.abs(event.values[2]);

        //计算出时间差值
        final float dT = (event.timestamp - mLastTimestamp) * NS2S;
        
        //计算出这个时候的速度,你会发现这个值变化不快，可以使用
        mRotateRadianY += event.values[1] * dT;
        mRotateRadianX += event.values[0] * dT;
        
        ...
      
        mLastTimestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
~~~

### 写一个ImageView的来配合上面传感器获取到的值进行移动图像
~~~java
/**
 * Created by BA on 2018/2/25 0025.
 *
 * @Function : 实现重力感应imageView
 */

public class MyGravityImageView extends android.support.v7.widget.AppCompatImageView {


    //图像宽高
    private int mDrawableWidth;
    private int mDrawableHeight;

    // ImageView的宽高
    private int mWidth;
    private int mHeight;

    // x和y轴最大移动距离
    private float mMaxOffsetX, mMaxOffsetY;

    // 从传感器传来的x，y的数值
    private float mPX, mPY;


    public MyGravityImageView(Context context) {
        this(context, null);
    }

    public MyGravityImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

//设置图片的缩放类型为中心放大
    public MyGravityImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setScaleType(ScaleType.CENTER_CROP);
    }


//这个开放接口是给传感器传值进来的
    void updateProgress(float pX, float pY) {
        mPX = pX;
        mPY = pY;
        invalidate();
    }

//在这个方法种计算出xy轴可以移动的大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        mHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        if (getDrawable() != null) {
            mDrawableWidth = getDrawable().getIntrinsicWidth();
            mDrawableHeight = getDrawable().getIntrinsicHeight();

            float imgScaleY = (float) mWidth / (float) mDrawableWidth;
            float imgScaleX = (float) mHeight / (float) mDrawableHeight;

            mMaxOffsetX = Math.abs((mDrawableWidth * imgScaleX - mWidth) * 0.5f);
            mMaxOffsetY = Math.abs((mDrawableHeight * imgScaleY - mHeight) * 0.5f);
        }
    }


//其实就是调整画布移动，因为画布里面就是图像
    @Override
    protected void onDraw(Canvas canvas) {
        //算出要移动的位置，又由于mPx和mPY其实是小数，不会比1大多少，所以移动自然就没问题
        float currentOffsetX = mMaxOffsetX * mPX;
        float currentOffsetY = mMaxOffsetY * mPY;
        canvas.save();
        canvas.translate(currentOffsetX, currentOffsetY);
        super.onDraw(canvas);
        canvas.restore();
    }
}
~~~

## 疑问
不计算速度，如何平滑加速度的值？

其实很简单，除几个数，让加速度变小就好

## 总结
难点就两个：
+ 平滑传感器的数值
+ 如何移动imageView里面的图像
+ 上面代码不完全，不可直接复制使用

