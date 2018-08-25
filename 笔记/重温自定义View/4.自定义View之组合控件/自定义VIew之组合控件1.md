# 自定义View之组合控件

[TOC]



## 前言

>  先来了解安卓里面的布局和控件结构
>
>  ​					   View
>
>  > TextView---------ImageView------------ViewGroup(所有布局的父类)  
>  >
>  >  可以看到，所有的控件都继承自View，布局都继承自ViewGroup，ViewGroup又继承自View

## 需求

![捕获](D:\Android第一行代码，笔记\自定义控件\捕获.PNG)

### 分析

+ 这里先讲引入布局，当你的界面中有很多布局，而这些布局在其他活动不一定会用到，你不想在活动的布局文件中写一堆，那就会用到引入布局
+ 先来看顶部，就是一个LinearLayout布局，包括了3个TextView控件

### 具体实现

> + 写一个布局文件，在里面写好布局
>
>   > ~~~xml
>   > <?xml version="1.0" encoding="utf-8"?>
>   > <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
>   >     android:layout_width="match_parent"
>   >     android:layout_height="?attr/actionBarSize" 
>   >     android:background="@color/colorPrimary"
>   >     android:orientation="horizontal">
>   >
>   >     <TextView
>   >         android:layout_width="0dp"
>   >         android:layout_height="wrap_content"
>   >         android:layout_weight="1"
>   >         android:id="@+id/title_back"
>   >         android:text="back"
>   >         android:textColor="#ffffff"
>   >         android:textStyle="bold"
>   >         android:layout_gravity="center_vertical"
>   >         android:gravity="left"
>   >         android:textSize="18sp"
>   >         android:padding="10dp"/>
>   >
>   >     <TextView
>   >         android:layout_width="0dp"
>   >         android:layout_height="wrap_content"
>   >         android:layout_weight="1"
>   >         android:id="@+id/title_title"
>   >         android:text="Title"
>   >         android:textColor="#ffffff"
>   >         android:textStyle="bold"
>   >         android:gravity="center"
>   >         android:layout_gravity="center_vertical"
>   >         android:textSize="18sp"
>   >         android:padding="10dp"/>
>   >
>   >     <TextView
>   >         android:layout_width="0dp"
>   >         android:layout_height="wrap_content"
>   >         android:layout_weight="1"
>   >         android:id="@+id/title_menu"
>   >         android:text="menu"
>   >         android:textColor="#ffffff"
>   >         android:textStyle="bold"
>   >         android:gravity="right"
>   >         android:layout_gravity="center_vertical"
>   >         android:textSize="18sp"
>   >         android:padding="10dp"/>
>   > </LinearLayout>
>   > ~~~
>
> + 在要使用的活动布局文件中引入布局
>
>   > ~~~xml
>   > <include layout="@layout/title"/>
>   > ~~~
>
> + 这就是引入布局，然后，在活动中正常使用就可以了

## 需求二

> 引入布局是方便，但是问题来了，我们只是引入了布局，但是布局中的控件并没有设置监听事件，如果有很多个活动都需要使用到这个布局，比如图中我们自定义的toolbar，就需要在用到的活动中都设置一遍监听，为了解决，接下来就进行自定义控件

### 分析

+ 布局在上面已经有了，那我们如何设置监听？每一个控件都是View的子类，看看上方布局中只有哟个LinearLayout布局，我们可以写一个类来直接管理我们Toolbar里面的控件，该类直接继承自LinearLayout布局，然后调用父类的构造器，然后加载进布局，接下来对里面的控件进行监听就好了。

### 具体实现

+ MyToolBar类来管理我们的ToolBar布局,加载布局等等的，看注释

  > ~~~java
  > import android.content.Context;
  > import android.util.AttributeSet;
  > import android.view.LayoutInflater;
  > import android.view.View;
  > import android.widget.LinearLayout;
  > import android.widget.TextView;
  > import android.widget.Toast;
  >
  > /**
  >  * Created by ljh99 on 2017/7/4 0004.
  >  */
  >
  > //继承自LinearLayout，并且进行全局事件监听
  > public class MyToolBar extends LinearLayout implements View.OnClickListener{
  >
  >   //调用父类的构造函数，进行成为控件必要的初始化
  >    public  MyToolBar (Context context , AttributeSet attrs){
  >         super(context,attrs);
  >
  >      	//加载布局，先获取布局加载器，然后调用inflate加载布局
  >         LayoutInflater.from(context).inflate(R.layout.title,this);
  >
  > 	  //获取布局中的控件     
  >        TextView back=(TextView) findViewById(R.id.title_back);
  >        TextView title=(TextView) findViewById(R.id.title_title);
  >        TextView menu=(TextView)findViewById(R.id.title_menu);
  > 		
  >        //设置监听
  >        back.setOnClickListener(this);
  >        title.setOnClickListener(this);
  >        menu.setOnClickListener(this);
  >     }
  >
  >     @Override
  >     public void onClick(View v) {
  >         switch (v.getId()){
  >             case R.id.title_back:
  >                 Toast.makeText(getContext(), "back", Toast.LENGTH_SHORT).show();break;
  >             case R.id.title_title:
  >                 Toast.makeText(getContext(), "title", Toast.LENGTH_SHORT).show();break;
  >             case R.id.title_menu:
  >                 Toast.makeText(getContext(), "menu", Toast.LENGTH_SHORT).show();break;
  >         }
  >     }
  > }
  >
  > ~~~

+ 接下来到使用我们的自定义控件，就是在需要用到的活动中像普通控件一样使用就好了,**需要注意的是，一定要写完整的包名**

  > ~~~xml
  > <com.example.ljh99.helpwangtext.MyToolBar
  >         android:layout_width="match_parent"
  >         android:layout_height="wrap_content"> </com.example.ljh99.helpwangtext.MyToolBar>
  > ~~~
  >
  > ​