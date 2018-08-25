[TOC]

# 手动新建xml时的一些属性分析

+ select

  > 这个很熟悉，用来改变当一个View在获取焦点，没获取焦点，被选中，长按的状态的文件，用<Item>标签指定不同状态下对应的图片资源，所以要先了解View的几种状态

+ View常用的几种状态

  > + Enable
  >
  >   > 用来表示View是否可用的状态，通过setEnable属性设置，最大的区别在于不可用的不能响应onTouch事件
  >
  > + focused
  >
  >   > 表示当前的View是否获得焦点，具体我也没了解焦点这个概念，但是平常用来处理输入事件的，比如如果你让一个Editor失去焦点，那么键盘不会自动弹出的，你点击一个View的时候这个View也会获取到焦点
  >
  > + window_focused
  >
  >   > 表示当前窗口是否处于交互状态，不能自己改的
  >
  > + selected
  >
  >   > 表示当前视图是否处于选中状态
  >
  > + pressed
  >
  >   > 表示是否处于按下状态，可以手动设置，调用setPressed()方法，但是一般是系统自动赋值的，
  >
  > + 目前遇到的用处就是，根据视图状态改变视图的状态（就是改变显示的图片啦）

  示例

  ~~~xml
  <?xml version="1.0" encoding="utf-8" ?>     
  <selector xmlns:Android="http://schemas.android.com/apk/res/android">   
  <!-- 默认时的背景图片-->    
  <item Android:drawable="@drawable/pic1" />      
  <!-- 没有焦点时的背景图片 -->    
  <item 
     Android:state_window_focused="false"      
     android:drawable="@drawable/pic_blue" 
     />     
  <!-- 非触摸模式下获得焦点并单击时的背景图片 -->    
  <item 
     Android:state_focused="true" 
     android:state_pressed="true"   
     android:drawable= "@drawable/pic_red" 
     />   
  <!-- 触摸模式下单击时的背景图片-->    
  <item 
     Android:state_focused="false" 
     Android:state_pressed="true"   
     Android:drawable="@drawable/pic_pink" 
     />    
  <!--选中时的图片背景-->    
  <item 
     Android:state_selected="true" 
     android:drawable="@drawable/pic_orange" 
     />     
  <!--获得焦点时的图片背景-->    
  <item 
     Android:state_focused="true" 
     Android:drawable="@drawable/pic_green" 
     />     
  </selector> 
  ~~~

  ​

  ##### **1.Shape**

  ###### 简介

  > **作用**：XML中定义的几何形状
  >
  > **位置**：res/drawable/文件的名称.xml

  ###### 使用的方法：

  > **Java代码中**：R.drawable.文件的名称
  >
  > **XML中**：Android:background="@drawable/文件的名称"

  ###### 属性：

  > <shape>  Android:shape=["rectangle" | "oval" | "line" | "ring"]
  >
  > *其中rectagle矩形，oval椭圆，line水平直线，ring环形*
  >
  > **<shape>中子节点的常用属性：**
  >
  > **<gradient>**  *渐变*
  >
  > Android:startColor  
  >
  > 起始颜色
  >
  > Android:endColor  
  >
  > *结束颜色            * 
  >
  > Android:angle  
  >
  > *渐变角度，0从左到右，90表示从下到上，数值为45的整数倍，默认为0；*
  >
  > Android:type  
  >
  > *渐变的样式 liner线性渐变 radial环形渐变 sweep*
  >
  > **<solid >**  内部*填充*
  >
  > Android:color  
  >
  > *填充的颜色*
  >
  > **<stroke >***描边*
  >
  > Android:width 
  >
  > *描边的宽度*
  >
  > Android:color 
  >
  > *描边的颜色*
  >
  > Android:dashWidth
  >
  >  *表示'-'横线的宽度*
  >
  > Android:dashGap 
  >
  > *表示'-'横线之间的距离*
  >
  > **<corners >***圆角*
  >
  > Android:radius  
  >
  > *圆角的半径 值越大角越圆*
  >
  > Android:topRightRadius  
  >
  > *右上圆角半径*
  >
  > Android:bottomLeftRadius 
  >
  > *右下圆角角半径*
  >
  > Android:topLeftRadius 
  >
  > *左上圆角半径*
  >
  > Android:bottomRightRadius 
  >
  > *左下圆角半径*
  >
  > **<padding >边界***填充*
  >
  > android:bottom="1.0dip" 
  >
  > *底部填充*
  >
  > android:left="1.0dip" 
  >
  > *左边填充*
  >
  > android:right="1.0dip" 
  >
  > *右边填充*
  >
  > android:top="0.0dip" 
  >
  > *上面填充*

  ##### 3.layer-list   

  ###### 简介：

  > 将多个图片或上面两种效果按照顺序层叠起来

  ###### 例子：

  **[html]** [view plain](http://blog.csdn.net/brokge/article/details/9713041/#) [copy](http://blog.csdn.net/brokge/article/details/9713041/#)

  1. <?xml version="1.0" encoding="utf-8"?>  
  2. <layer-list xmlns:android="http://schemas.android.com/apk/res/android">  
  3. ​    <item>  
  4. ​      <bitmap android:src="@drawable/android_red"  
  5. ​        android:gravity="center" />  
  6. ​    </item>  
  7. ​    <item android:top="10dp" android:left="10dp">  
  8. ​      <bitmap android:src="@drawable/android_green"  
  9. ​        android:gravity="center" />  
  10. ​    </item>  
  11. ​    <item android:top="20dp" android:left="20dp">  
  12. ​      <bitmap android:src="@drawable/android_blue"  
  13. ​        android:gravity="center" />  
  14. ​    </item>  
  15. </layer-list>  

  **[html]** [view plain](http://blog.csdn.net/brokge/article/details/9713041/#) [copy](http://blog.csdn.net/brokge/article/details/9713041/#)

  1. <ImageView  
  2. ​    android:layout_height="wrap_content"  
  3. ​    android:layout_width="wrap_content"  
  4. ​    android:src="@drawable/layers" />  

