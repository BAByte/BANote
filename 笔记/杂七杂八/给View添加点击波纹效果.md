[toc]
# 给View添加点击波纹效果
参考：http://blog.csdn.net/jdsjlzx/article/details/50615077

http://blog.csdn.net/lucifervsme/article/details/51023826

## 使用官方属性
可以通过如下代码设置波纹的背景：
~~~xml
波纹有边界

android:background="?android:attr/selectableItemBackground"

波纹超出边界
android:background="?android:attr/selectableItemBackgroundBorderless"
~~~

设置需要注意的地方
必须配合这个属性使用

android:clickable="true"
~~~xml
  <LinearLayout
       android:background="?android:attr/selectableItemBackgroundBorderless"
       android:clickable="true"
       android:layout_width="match_parent"
       android:layout_height="48dp">

       <TextView
           android:text="ssssssss"
           android:layout_width="wrap_content"
           android:layout_height="wrap_content" />
   </LinearLayout>
~~~

## 存在的问题一
CarView使用方法不同，你会发现CarView本身是不能设置背景的，只能设置背景颜色，解决方法是设置前景
~~~java
android:foreground="?android:attr/selectableItemBackgroundBorderless"
~~~

## 存在问题二
都知道水波纹效果是安卓5.0出来的，所以上面的这种写法在安卓5.0以上才有用。
## 存在问题三
如果我的Button有背景，那么就不能设置了！！！！怎么办？

## 解决方法
res/drawable-v21/建一个文件，用来适配5.0以上的，内容在下面
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<ripple
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="@color/colorAccent">//点击时波纹的颜色
    <item android:drawable="@drawable/playsong_bdg"/>//未点击时控件的背景（可以是图片，可以是颜色，也可以是drawable里的xml背景（比如圆角））
</ripple>
~~~

再建一个在res/drawable用来适配5.0以下的，但是不是水波纹效果。具体效果你可以去看看我关于selected标签的笔记，这里我就给代码了
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //按压的效果
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="#18ffc400"/>
        </shape>
    </item>
    //获取焦点的效果
    <item android:state_focused="true" android:state_enabled="true">
        <shape android:shape="rectangle">
            <solid android:color="#0f000000"/>
        </shape>
    </item>

    //没有点击的效果
    <item
        android:drawable="@drawable/playsong_bdg">
    </item>

</selector>


