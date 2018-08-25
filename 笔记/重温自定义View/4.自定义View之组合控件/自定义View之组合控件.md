[TOC]

# 自定义View之组合控件

---

## 写在前面

> + 在第一行代码中最常见的就是ToolBar,我们自定义ToolBar的时候就是利用布局组合,然后在class文件继承ViewGroup,然后在里面进行对布局的加载和逻辑处理

## 具体案例

> + 需求
>
>   > 写一个ToolBar,包含一个Button,TextView
>   >
>   > Button功能:退出
>   >
>   > TextView功能: Title
>
> + 分析
>
>   > 继承LinearLayout
>   >
>   > 监听Button按钮,finish()

## 代码

+ ToolBar布局

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
             android:layout_width="match_parent"
             android:layout_height="?attr/actionBarSize"
             android:background="@color/colorPrimary"
             android:orientation="horizontal">

    <TextView
        android:id="@+id/exit"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical"
        android:layout_margin="16dp"
        android:text="Exit"
        android:textColor="#ffffff"
        android:textSize="20sp"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="Title"
        android:textColor="#ffffff"
        android:textSize="20sp"/>

</FrameLayout>
~~~

---

+ 组合控件的管理类代码

~~~java
package com.chenzicong.diyviewgroupdemo;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by 82023 on 2017/9/10.
 * function: 自定义ToolBar,学习组合控件
 */

public class MyToolBar extends FrameLayout {

    /**
     * @param context
     * @funtion initLayout
     * @Exception 抛出加载异常时, 肯可能是没有复写对应的构造函数
     */
    public MyToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater layoutInflater = LayoutInflater.from(context);//获取LayoutInflate服务
        layoutInflater.inflate(R.layout.my_toolbar, this);//加载布局文件

        TextView exit = (TextView) findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ((Activity) getContext()).finish();
            }
        });
    }
}
~~~

---

+ Activity布局文件中使用

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:layout_width="match_parent"
              android:layout_height="match_parent"
              android:orientation="vertical">
  
<!--一定要有完整包名,在自定义View时有自定义属性AttributeSet时,还需要定义命名空间-->
    <com.chenzicong.diyviewgroupdemo.MyToolBar
        android:id="@+id/action0"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
    </com.chenzicong.diyviewgroupdemo.MyToolBar>

</LinearLayout>
~~~

---

## 总结

> + 用处:
>
>   > 当一个布局需要复用,而且布局中的VIew很多的监听事件需要处理时
>
> + 注意的地方
>
>   > 构造函数,最好复写4个构造函数,因为你不知道调用哪一个
>
> + 遇到的问题
>
>   > ​在测试时我尝试用Activity的setContentView()方法直接加载布局文件,这很显然与我初衷不符,因为这样该布局的管理类是完全没有作用的,原因非常的简单,setContentView()方法在以前就分析过,内部也是使用了LayoutInflate这个服务来加载布局,意思是直接加载布局文件在Activity中,所以就脱离了我们之前写的管理类