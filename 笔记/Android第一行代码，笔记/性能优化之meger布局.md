[TOC]

# 性能优化之meger布局

## 为什么要使用

当我们遇到Activity布局文件里面要放很多东西的时候，就会将一部分布局写到另一个文件中，然后在Activity布局文件里面include进来，比如里面有超级多个TextView，是垂直排列的，那么一般这样写

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:layout_margin="10dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/status"/>

    <TextView
        android:layout_margin="10dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/progress"/>
  <!--这里省略-->

</LinearLayout>
~~~



然后就Include到Activity中

~~~xml
  <?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"    xmlns:app="http://schemas.android.com/apk/res-auto"    xmlns:tools="http://schemas.android.com/tools"   
android:layout_width="match_parent"    
android:layout_height="match_parent"    tools:context="com.example.ljh99.gitdemo.MainActivity"    
android:orientation="vertical">   

  <include layout="@layout/test"/>

</LinearLayout>
~~~

乍一看没什么问题，但是，xml布局文件都是使用LayoutInflater加载进来的，所以要解析xml文件，而我们又知道解析xml文件关键就是节点，看看那个有很多TextView的布局，确实是写出来了。但是TextView的最外层居然又要加一个LinearLayout布局，而在我们include进Activity布局时，外面已经有一个LinearLayout了，这就导致了多了一层的布局，解析时就更加的耗时间



## 解决方法

既然那个有很多TextView的布局文件，不需要那个LinearLayout布局那就不加，将标签该成meger，这样就可以了，这就不用在外面加东西了，具体原因看下面

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<merge xmlns:android="http://schemas.android.com/apk/res/android">

    <TextView
        android:layout_margin="10dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/status"/>

    <TextView
        android:layout_margin="10dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/progress"/>

</merge>
~~~



## Meger逻辑分析

其实按照正常逻辑来说，因为Activity内容太多，所以我要放一点在其他文件，那就将一部分放在另一个文件中，根本不需要为这些放出去的东西再弄一个根布局，因为很明确的是，等一下就是要用在刚刚的Activity中，应该把拿出去的布局，在使用时重新合并进来就好了，因此就出现了Meger标签，使用了这个标签后，该标签里面的属性是无效的，他不是一个控件或者布局，他只是一个标志，标志着在include进Acitivity里面时应该是合并这个文件里面的控件，这样一来就不用在外面添加一个布局了，解析自然也少了一个节点，加载UI的性能也就高了

