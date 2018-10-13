[TOC]

# Button点击效果

## 分析

Button有3个状态

+ 没有被点击和没有焦点

  > 普通状态

+ 没有被点击有焦点

  > 就是手指滑过按钮的时候

+ 被点击



## 具体实现

res文件夹新建一个xml文件

代码

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
<!--意思是被点击的时候显示哪张图片，下面同理-->
<item android:drawable="@drawable/press" android:state_pressed="true"/>
<item android:drawable="@drawable/bu" android:state_focused="false"          android:state_pressed="false"/>
<item android:drawable="@drawable/press"  android:state_focused="true"/>
<item android:drawable="@drawable/bu" android:state_focused="false"/>

</selector>
~~~



使用的时候 

```xml
android:background="指定你刚刚设置的xml文件
```