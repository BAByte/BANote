# 问题描述
当从一个Fragment滑到另一页时，另一页如果有RecyclerView的话会自动滑动一点
# 解决方法
其实是recyclerView获取了焦点，只需要在recyclerView的上一个View设置一下，下面是代码
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout 
...>
    <TextView
       
       下面这两句
        android:focusableInTouchMode="true"
        android:focusable="true"/>
     
    <android.support.v7.widget.RecyclerView
        android:id="@+id/aritist_page_recycle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
    </android.support.v7.widget.RecyclerView>

</LinearLayout>
~~~