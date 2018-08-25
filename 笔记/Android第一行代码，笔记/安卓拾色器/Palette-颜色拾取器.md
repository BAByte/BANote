[toc]
# Palette-颜色拾取器
参考：忘记了是哪篇文章
## 先说两句
颜色拾取其实在写状态了变色的时候就想学了，后来没时间就放着，一直拖到开发播放器的播放控制栏背景颜色变色的时候，才去学。
## 需求
从图片的某个区域或者整张图片获取颜色
## 发现的问题
它获取颜色也是很有意思，它不能获取白色和黑色，这个它好像视为无颜色，而且他是根据色调来获取的

Palette可以提取的颜色如下:

+ Vibrant （有活力的）
+ Vibrant dark（有活力的 暗色）
+ Vibrant light（有活力的 亮色）
+ Muted （柔和的）
+ Muted dark（柔和的 暗色）
+ Muted light（柔和的 亮色）

## 使用Palette方法
### 导入库
~~~java
compile 'com.android.support:palette-v7:25.3.1'
~~~
### 简单使用
它使用了建造者模式
~~~java
//直接从位图获取
 Palette.Builder builder = Palette.from(Bitmap);
 
 //配置选取颜色的一些参数
 builder.maximumColorCount(16) // 构建Palette时使用的最大颜色数，默认是16，风景图推荐取值8-16，人脸图像推荐取值24-32（值越大，花费的时间越长，可选择的色彩越多）
                    .setRegion(0, 0, bitmap.getWidth(), 10); // 设置Palette颜色分析的图像区域
                    


~~~

得到Palette对象后，就可以拿到提取到的颜色色样，没错就是色样。

~~~java
Palette.getVibrantSwatch()
Palette.getDarkVibrantSwatch()
Palette.getLightVibrantSwatch()
Palette.getMutedSwatch()
Palette.getDarkMutedSwatch()
Palette.getLightMutedSwatch()
~~~


使用颜色，上面get方法中返回的是一个 Swatch 样本对象，这个样本对象是Palette的一个内部类，它提供了一些获取最终颜色的方法。

~~~java
getPopulation();// 样本中的像素数量
getRgb(); //颜色的RBG值
getHsl();// 颜色的HSL值
getBodyTextColor();// 主体文字的颜色值
getTitleTextColor(); //标题文字的颜色值
~~~

这个色样的好处就是很智能，它可以直接帮你选出一些你需要用的颜色，比如什么颜色适合在这个图片上面的文字颜色，你直接用Swatch查一下他的接口就知道了

我们来看看getRgb(); //颜色的RBG值,就是如何从里面提取颜色
~~~java
int rgb = darkMutedSwatch.getRgb();
int red = Color.red(rgb);
int green = Color.green(rgb);
int blue = Color.blue(rgb);
int alpha = Color.alpha(rgb);
color = Color.argb(alpha, red, green, blue);
~~~

### 如何直接获取颜色？
这里我只放出一个
~~~java
// 获得 暗、柔和 的色值，参数为获取失败的默认值，
color = palette.getDarkMutedColor(Color.BLACK);
~~~

## 总结
不难，工具类我已经写好，在文件夹下