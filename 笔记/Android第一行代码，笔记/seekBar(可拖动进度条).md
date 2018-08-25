[TOC]

#seekBar(可拖动进度条)

---

## 写在前面

> 一开始没在意这个东西，后来在手势软件的开发中用到，所以研究一下

## 基本用法

+ xml，有些参数不懂不要紧

~~~xml
<SeekBar
       android:layout_width="match_parent"
       android:layout_height="wrap_content"
       android:maxHeight="10dp"   
       android:max="100"
       android:secondaryProgress="30"
       android:progress="12"
       android:progressDrawable="@drawable/progress_bar"
       android:thumb="@drawable/seek_bar_button"
       android:background="@color/xxx"/>
~~~

+ 有些地方需要注意

  > 原生的seekbar我把它分了5层来理解。我们这里将最下面的那层叫第一层
  >
  > + 第一层，我把它叫做背景层，是整个View的背景，并不是进度条的背景
  > + 第二层，未达到的进度条的背景，音乐放到一半，前面的一半是有颜色的进度，后面是没颜色的进度，没颜色的那层就是第二层
  > + 第三层和第四层，就是进度条的颜色层了，为什么有两层？不知道你有没有看过一个下载进度条，是有两个颜色进度的，慢的是未加速的下载进度，快的是加速后的，所以有两条进度条，就两层呗
  > + 第五层就是进度条上那个圆形的拖动按钮啦

+ 属性分析

  > android:layout_width="match_parent"
  >
  > android:layout_height="wrap_content"
  >
  > android:background="@color/xxx"
  >
  > 这些都是View的大小，背景属性，并不是进度条的颜色
  >
  > ​
  >
  > ```xml
  >  < android:maxHeight="10dp"     //这个是进度条的高度，当然也有宽度的属性设置，这里没写，
  >    android:max="100"    //最大进度
  >    android:secondaryProgress="30"  //第二进度的预设值
  >    android:progress="12"   //第一进度的预设值
  >    android:progressDrawable="@drawable/progress_bar"   //这个就是设置进度条颜色的属性
  >    android:thumb="@drawable/seek_bar_button"  //滑块，可以是xml（select）也可以是图片
  >  />
  > ```

+ progressDrawable属性

  > 当你想要小小的更改进度条的样式时就可以使用这个属性，先建一个xml,文件，根节点是layer-list，因为说了嘛，是几层叠在一起的，这里设置的正是进度条的3层样式

  ~~~xml
  <?xml version="1.0" encoding="utf-8"?>
  <layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!--进度条背景层-->
      <item android:id="@android:id/background">
        <!--画一个矩形-->
          <shape>
            <!--设置矩形圆角，也可以单独设置每一个角的圆角-->
              <corners android:radius="100dp" />
            <!--矩形的颜色填充，这里是整个矩形的填充-->
              <solid android:color="#d8d8d8" />
          </shape>
      </item>
      <!--第二进度条层-->
      <item android:id="@android:id/secondaryProgress">
          <clip>
              <shape>
                  <corners android:radius="100dp" />
              </shape>
          </clip>
      </item>
      <!--第一背景层-->
      <item android:id="@android:id/progress">
          <clip>
              <shape>
                  <corners android:radius="100dp" />
                  <solid android:color="@color/seekBarBg" />
              </shape>
          </clip>
      </item>
  </layer-list>
  ~~~



+ thumb属性

  > 当你不爽默认的滑块时，想要换掉，就用这个属性
  >
  > 可以直接指定图片，也可以xml文件，select作为根节点

+ 一系列回调方法

  ~~~java
      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main);

          SeekBar seekBar = (SeekBar) findViewById(R.id.seek_bar);
          //设置第一进度初始值
          seekBar.setProgress(10);
          //第二进度初始值
          seekBar.setSecondaryProgress(20);
          //最大值
          seekBar.setMax(100);

          //拖动滑块的监听
          seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

              //进度发生变化时被调用
              @Override
              public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                  Log.d("ssss","进度改变"+progress);
              }

              //开始拖动的时候调用
              @Override
              public void onStartTrackingTouch(SeekBar seekBar) {
                  Log.d("ssss","开始拖动"+seekBar.getProgress());
              }

              //停止拖动时调用
              @Override
              public void onStopTrackingTouch(SeekBar seekBar) {
                  Log.d("ssss","停止拖动"+seekBar.getProgress());
              }
          });
  ~~~

  ​

