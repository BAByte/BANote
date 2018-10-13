[TOC]

# 布局性能优化之ViewStub

## 介绍

ViewStub：是一个储存View的栈，将要延迟加载的View放在这里面，下使用这个ViewStub的实例在代码中动态加载里面储存的View

## 问题

我们知道有时候在界面上我们不是一下子就需要所有的布局，但是如果我们在xml文件中写了的话，那么里面的布局会在启动时全部加载进来，这就有问题了，如果有一些布局是只有在使用的时候才显示的，但是用户不一定会用到的，那就延缓这个布局的载入，在使用到再加载

## 例子

+ Activity中有一个Button，点击后会显示一个布局控件
+ 用户不一定会点这个Button，可能就推出程序了，所以为那个布局控件延时加载，在点击到后再加载



~~~xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.example.ljh99.gitdemo.MainActivity"
    android:id="@+id/layout"
    android:orientation="vertical">

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/show"
        android:text="show"/>
<!--将点击按钮后要显示的布局声明在这个ViewStub中-->
    <ViewStub
        android:layout="@layout/test"
        android:id="@+id/stub"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

</LinearLayout>
~~~



将要显示的布局文件

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">

    <TextView
        android:layout_margin="10dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="显示了"/>

</LinearLayout>
~~~



点击后再加载

~~~java
        bu=(Button)findViewById(R.id.show);
        stub=(ViewStub)findViewById(R.id.stub);
        bu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stub.inflate();
            }
        });
~~~

