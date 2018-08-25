[toc]

# Matrix详解
## 先说两句
其实就是用法了。

## 方法表
方法类别|	相关API	|摘要
--|--|--
基本方法|	equals hashCode toString toShortString	|比较、 获取哈希值、 转换为字符串
数值操作|	set reset setValues getValues	|设置、 重置、 设置数值、 获取数值
数值计算|	mapPoints mapRadius mapRect mapVectors	|计算变换后的数值
设置(set)|	setConcat setRotate setScale setSkew setTranslate	|设置变换
前乘(pre)|	preConcat preRotate preScale preSkew preTranslate	|前乘变换
后乘(post)|	postConcat postRotate postScale postSkew postTranslate	|后乘变换
特殊方法|	setPolyToPoly setRectToRect rectStaysRect setSinCos	|一些特殊操作
矩阵相关|	invert isAffine isIdentity	|求逆矩阵、 是否为仿射矩阵、 是否为单位矩阵 …

### 构造方法
无参构造，实例化的数组是[1,0,0,0,1,0,0,0,1]
~~~java
Matrix ();
~~~


有参构造,类似于复制一份
~~~java
Matrix (Matrix src)
~~~

### 基本方法
+ equals
比较两个矩阵是否相等

+ toString
将Matrix转换为字符串: Matrix{[1.0, 0.0, 0.0][0.0, 1.0, 0.0][0.0, 0.0, 1.0]}

+ toShortString
将Matrix转换为短字符串: [1.0, 0.0, 0.0][0.0, 1.0, 0.0][0.0, 0.0, 1.0]


### 数值操作方法
1.set
void set (Matrix src)

没有返回值，有一个参数，作用是将参数Matrix的数值复制到当前Matrix中。如果参数为空，则重置当前Matrix，相当于reset()。

2.reset
~~~
void reset ()
~~~
重置当前Matrix(将当前Matrix重置为单位矩阵)。

3.setValues
~~~
void setValues (float[] values)
~~~
setValues的参数是浮点型的一维数组，长度需要大于9，拷贝数组中的前9位数值赋值给当前Matrix。

4.getValues
~~~
void getValues (float[] values)
~~~
很显然，getValues和setValues是一对方法，参数也是浮点型的一维数组，长度需要大于9，将Matrix中的数值拷贝进参数的前9位中。

### 数值计算
1.mapPoints
~~~
void mapPoints (float[] pts)

void mapPoints (float[] dst, float[] src)

void mapPoints (float[] dst, int dstIndex,float[] src, int srcIndex, int pointCount)
~~~
计算一组点基于当前Matrix变换后的位置，(由于是计算点，所以参数中的float数组长度一般都是偶数的,若为奇数，则最后一个数值不参与计算)。

它有三个重载方法:

(1) void mapPoints (float[] pts)

方法仅有一个参数，pts数组作为参数传递原始数值，计算结果仍存放在pts中。

示例:
~~~
// 初始数据为三个点 (0, 0) (80, 100) (400, 300) 
float[] pts = new float[]{0, 0, 80, 100, 400, 300};

// 构造一个matrix，x坐标缩放0.5
Matrix matrix = new Matrix();
matrix.setScale(0.5f, 1f);

// 输出pts计算之前数据
Log.i(TAG, "before: "+ Arrays.toString(pts));

// 调用map方法计算
matrix.mapPoints(pts);

// 输出pts计算之后数据
Log.i(TAG, "after : "+ Arrays.toString(pts));
~~~

结果:
~~~
before: [0.0, 0.0, 80.0, 100.0, 400.0, 300.0]
after : [0.0, 0.0, 40.0, 100.0, 200.0, 300.0]
~~~


(2) void mapPoints (float[] dst, float[] src) 
src作为参数传递原始数值，计算结果存放在dst中，src不变。

如果原始数据需要保留则一般使用这种方法。

示例:
~~~
// 初始数据为三个点 (0, 0) (80, 100) (400, 300)
float[] src = new float[]{0, 0, 80, 100, 400, 300};
float[] dst = new float[6];

// 构造一个matrix，x坐标缩放0.5
Matrix matrix = new Matrix();
matrix.setScale(0.5f, 1f);

// 输出计算之前数据
Log.i(TAG, "before: src="+ Arrays.toString(src));
Log.i(TAG, "before: dst="+ Arrays.toString(dst));

// 调用map方法计算
matrix.mapPoints(dst,src);

// 输出计算之后数据
Log.i(TAG, "after : src="+ Arrays.toString(src));
Log.i(TAG, "after : dst="+ Arrays.toString(dst));
~~~

结果:
~~~
before: src=[0.0, 0.0, 80.0, 100.0, 400.0, 300.0]
before: dst=[0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
after : src=[0.0, 0.0, 80.0, 100.0, 400.0, 300.0]
after : dst=[0.0, 0.0, 40.0, 100.0, 200.0, 300.0]
~~~

(3) void mapPoints (float[] dst, int dstIndex,float[] src, int srcIndex, int pointCount) 

可以指定只计算一部分数值。

参数|	摘要
--|--
dst	|目标数据
dstIndex|	目标数据存储位置起始下标
src|	源数据
srcIndex|	源数据存储位置起始下标
pointCount|	计算的点个数

示例:
~~~
将第二、三个点计算后存储进dst最开始位置。

// 初始数据为三个点 (0, 0) (80, 100) (400, 300)
float[] src = new float[]{0, 0, 80, 100, 400, 300};
float[] dst = new float[6];

// 构造一个matrix，x坐标缩放0.5
Matrix matrix = new Matrix();
matrix.setScale(0.5f, 1f);

// 输出计算之前数据
Log.i(TAG, "before: src="+ Arrays.toString(src));
Log.i(TAG, "before: dst="+ Arrays.toString(dst));

// 调用map方法计算(最后一个2表示两个点，即四个数值,并非两个数值)
matrix.mapPoints(dst, 0, src, 2, 2);

// 输出计算之后数据
Log.i(TAG, "after : src="+ Arrays.toString(src));
Log.i(TAG, "after : dst="+ Arrays.toString(dst));
~~~

结果:
~~~
before: src=[0.0, 0.0, 80.0, 100.0, 400.0, 300.0]
before: dst=[0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
after : src=[0.0, 0.0, 80.0, 100.0, 400.0, 300.0]
after : dst=[40.0, 100.0, 200.0, 300.0, 0.0, 0.0]
~~~


2.mapRadius
float mapRadius (float radius)

测量半径，由于圆可能会因为画布变换变成椭圆，所以此处测量的是平均半径。

示例:
~~~
float radius = 100;
float result = 0;

// 构造一个matrix，x坐标缩放0.5
Matrix matrix = new Matrix();
matrix.setScale(0.5f, 1f);

Log.i(TAG, "mapRadius: "+radius);

result = matrix.mapRadius(radius);

Log.i(TAG, "mapRadius: "+result);
~~~

结果:
~~~
mapRadius: 100.0
mapRadius: 70.71068
~~~


3.mapRect
测量矩形变换后位置。
~~~
boolean mapRect (RectF rect)
boolean mapRect (RectF dst, RectF src)
~~~


(1) boolean mapRect (RectF rect) 测量rect并将测量结果放入rect中，返回值是判断矩形经过变换后是否仍为矩形。

示例：
~~~
RectF rect = new RectF(400, 400, 1000, 800);

// 构造一个matrix
Matrix matrix = new Matrix();
matrix.setScale(0.5f, 1f);
matrix.postSkew(1,0);

Log.i(TAG, "mapRadius: "+rect.toString());

boolean result = matrix.mapRect(rect);

Log.i(TAG, "mapRadius: "+rect.toString());
Log.e(TAG, "isRect: "+ result);
~~~

结果：
~~~
mapRadius: RectF(400.0, 400.0, 1000.0, 800.0)
mapRadius: RectF(600.0, 400.0, 1300.0, 800.0)
isRect: false
由于使用了错切，所以返回结果为false。
~~~

(2) boolean mapRect (RectF dst, RectF src) 

测量src并将测量结果放入dst中，返回值是判断矩形经过变换后是否仍为矩形,和之前没有什么太大区别，此处就不啰嗦了。

4.mapVectors
测量向量。
~~~
void mapVectors (float[] vecs)

void mapVectors (float[] dst, float[] src)

void mapVectors (float[] dst, int dstIndex, float[] src, int srcIndex, int vectorCount)
~~~
mapVectors 与 mapPoints 基本上是相同的，可以直接参照上面的mapPoints使用方法。


而两者唯一的区别就是mapVectors不会受到位移的影响，这符合向量的定律，如果你不了解的话，请找到以前教过你的老师然后把学费要回来。

区别:
~~~
float[] src = new float[]{1000, 800};
float[] dst = new float[2];

// 构造一个matrix
Matrix matrix = new Matrix();
matrix.setScale(0.5f, 1f);
matrix.postTranslate(100,100);

// 计算向量, 不受位移影响
matrix.mapVectors(dst, src);
Log.i(TAG, "mapVectors: "+Arrays.toString(dst));

// 计算点
matrix.mapPoints(dst, src);
Log.i(TAG, "mapPoints: "+Arrays.toString(dst));
~~~

结果:
~~~
mapVectors: [500.0, 800.0]
mapPoints: [600.0, 900.0]
~~~


### 特殊方法
实现PS中的自由变换的方法
1.setPolyToPoly
~~~
boolean setPolyToPoly (
        float[] src,    // 原始数组 src [x,y]，存储内容为一组点
        int srcIndex,   // 原始数组开始位置
        float[] dst,    // 目标数组 dst [x,y]，存储内容为一组点
        int dstIndex,   // 目标数组开始位置
        int pointCount) // 测控点的数量 取值范围是: 0到4
~~~

效果图
![image](http://ww1.sinaimg.cn/large/005Xtdi2jw1f71ppx7q0lg30go0b44ga.gif)

#### 测控点选取位置?

测控点可以选择任何你认为方便的位置，只要src与dst一一对应即可。不过为了方便，通常会选择一些特殊的点： 图形的四个角，边线的中心点以及图形的中心点等。不过有一点需要注意，测控点选取都应当是不重复的(src与dst均是如此)，如果选取了重复的点会直接导致测量失效，这也意味着，你不允许将一个方形(四个点)映射为三角形(四个点，但其中两个位置重叠)，但可以接近于三角形。。


2.setRectToRect
这个应该就是ImageView那个图片缩放的基本方法，简单来说就是将源矩形的内容填充到目标矩形中，然而在大多数的情况下，源矩形和目标矩形的长宽比是不一致的，到底该如何填充呢，这个填充的模式就由第三个参数 stf 来确定。

ScaleToFit 是一个枚举类型，共包含了四种模式:


![image](https://note.youdao.com/yws/api/personal/file/EE90738FC01B4D119804E9EB49E5B0C8?method=download&shareKey=572464ee5c90046bead17d76d9720f4f)


代码：
~~~java
public class MatrixSetRectToRectTest extends View {

    private static final String TAG = "MatrixSetRectToRectTest";

    private int mViewWidth, mViewHeight;

    private Bitmap mBitmap;             // 要绘制的图片
    private Matrix mRectMatrix;         // 测试etRectToRect用的Matrix

    public MatrixSetRectToRectTest(Context context) {
        super(context);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rect_test);
        mRectMatrix = new Matrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF src= new RectF(0, 0, mBitmap.getWidth(), mBitmap.getHeight() );
        RectF dst = new RectF(0, 0, mViewWidth, mViewHeight );

        // 核心要点
        mRectMatrix.setRectToRect(src,dst, Matrix.ScaleToFit.CENTER);

        // 根据Matrix绘制一个变换后的图片
        canvas.drawBitmap(mBitmap, mRectMatrix, new Paint());
    }
}
~~~


3.rectStaysRect
判断矩形经过变换后是否仍为矩形，假如Matrix进行了平移、缩放则画布仅仅是位置和大小改变，矩形变换后仍然为矩形，但Matrix进行了非90度倍数的旋转或者错切，则矩形变换后就不再是矩形了，这个很好理解，不过多赘述，顺便说一下，前面的mapRect方法的返回值就是根据rectStaysRect来判断的。

4.isIdentity
判断是否为单位矩阵

## Matrix Camera
我建议还是自己去网站看看：
http://www.gcssloop.com/customview/matrix-3d-camera

这个东西很有意思，我们上面的变换都是指在二D平面的变换，但是Camera却是指在3d空间的变换，至于为什么叫Camera？这个就是一门学问了。他把一个物体放到一个虚拟的空间直角坐标系，但是他要怎么让我们看起来这个物体是立体的呢？他就只需要拍下人眼在各个位置看到物体的图像放给我们看，其实我们人眼就感觉是立体的，所以叫Camera，看看下面的一幅图

![image](http://ww4.sinaimg.cn/large/005Xtdi2jw1f7q71yek4wg308c058go5.gif)

Android 上面观察View的摄像机默认位置在屏幕左上角，而且是距屏幕有一段距离的，假设灰色部分是手机屏幕，白色是上面的一个View，摄像机位置看起来大致就是下面这样子的(为了更好的展示摄像机的位置，做了一个空间转换效果的动图)。


那他的空间坐标系是怎样的？有两种，安卓是第一种

![image](http://ww3.sinaimg.cn/large/005Xtdi2jw1f7mruav2nhj308c05iglp.jpg)


摄像机的位置默认是 (0, 0, -576)。其中 -576＝ -8 x 72，虽然官方文档说距离屏幕的距离是 -8, 但经过测试实际距离是 -576 像素，当距离为 -10 的时候，实际距离为 -720 像素。不过这个数值72我也不明白是什么东西，我使用了3款手机测试，屏幕大小和像素密度均不同，但结果都是一样的，知道的小伙伴可以告诉我一声。

### 基本方法
基本方法就有两个save 和restore，主要作用为保存当前状态和恢复到上一次保存的状态，通常成对使用，常用格式如下:

~~~java
camera.save();		// 保存状态
... 			// 具体操作
camera.retore();	// 回滚状态
~~~

### 常用方法
这两个方法是Camera中最基础也是最常用的方法。

+ getMatrix
~~~
void getMatrix (Matrix matrix)
~~~
计算当前状态下矩阵对应的状态，并将计算后的矩阵赋值给参数matrix。

+ applyToCanvas
~~~
void applyToCanvas (Canvas canvas)
~~~
计算当前状态下单矩阵对应的状态，并将计算后的矩阵应用到指定的canvas上。

### 平移
平移的话因为坐标系和2D的不一样，注意一下就好了，我也不写了，xy轴的平移和Matrix是一样的，所以我们看看Z轴， 

沿z轴平移

这个不仅有趣，还容易蒙逼，上面两种情况再怎么闹腾也只是在2D平面上，而z轴的出现则让其有了空间感。

当View和摄像机在同一条直线上时: 此时沿z轴平移相当于缩放的效果，缩放中心为摄像机所在(x, y)坐标，当View接近摄像机时，看起来会变大，远离摄像机时，看起来会变小，近大远小。

当View和摄像机不在同一条直线上时: 当View远离摄像机的时候，View在缩小的同时也在不断接近摄像机在屏幕投影位置(通常情况下为Z轴，在平面上表现为接近坐标原点)。相反，当View接近摄像机的时候，View在放大的同时会远离摄像机在屏幕投影位置。

我知道，这样说你们肯定是蒙逼的，话说为啥远离摄像机的时候会接近摄像机在屏幕投影位置(´･_･`)，肯定觉得我在逗你们玩，完全是前后矛盾，逻辑都不通，不过这个在这里的确是不矛盾的，因为远离是在3D空间里的情况，而接近只是在2D空间的投影，看下图。


![image](http://ww3.sinaimg.cn/large/005Xtdi2jw1f7qerbksn4g30dc0ct7vt.gif)


结论：

平移|	重点内容
--|--
x轴|	2D 和 3D 相同。
y轴|	2D 和 3D 相反。
z轴|	近大远小、视线相交。


### 旋转
~~~java
// (API 12) 可以控制View同时绕x，y，z轴旋转，可以由下面几种方法复合而来。
void rotate (float x, float y, float z);

// 控制View绕单个坐标轴旋转
void rotateX (float deg);
void rotateY (float deg);
void rotateZ (float deg);
~~~

![image](http://ww4.sinaimg.cn/large/005Xtdi2jw1f7sg375gbgg308c0eak4l.gif)

![image](http://ww2.sinaimg.cn/large/005Xtdi2jw1f7sgtl0nryg308c0eatm8.gif)

![image](http://ww4.sinaimg.cn/large/005Xtdi2jw1f7sgtxp6awg308c0eah5h.gif)

---

**旋转中心**

旋转中心默认是坐标原点，对于图片来说就是左上角位置。

而上图我们把坐标原点移动到了图片的中心


