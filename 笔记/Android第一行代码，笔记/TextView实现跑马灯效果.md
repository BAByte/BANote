# TextView实现跑马灯效果

ps：很多方法已经被弃用，所以有错误别怕，能运行的

其实还挺简单

+ 继承TextView，看代码

  ~~~java
  package com.example.ljh99.gitdemo;

  import android.content.Context;
  import android.util.AttributeSet;
  import android.widget.TextView;

  /**
   * Created by ljh99 on 2017/8/3 0003.
   */

  public class MarqueeTextView extends TextView {
      public MarqueeTextView(Context context) {
          super(context);
      }

      public MarqueeTextView(Context context,  AttributeSet attrs) {
          super(context, attrs);
      }


      public MarqueeTextView(Context context,  AttributeSet attrs, int defStyleAttr) {
          super(context, attrs, defStyleAttr);
      }

    //这个方法要是不复写啊，当布局里面有两个textView时就不能自动跑了
      @Override
      public boolean isFocused() {
          return true;
      }
  }
  ~~~

+ 布局文件

~~~xml
 <!--从id开始的下面就是关键-->
 
<com.example.ljh99.gitdemo.MarqueeTextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/one"
        android:ellipsize="marquee"  
        android:focusable="true"
        android:focusableInTouchMode="true"
        android:textSize="30sp"
        android:maxLines="1"
        android:text="货到付款 货到付款 货到付款 货到付款 货到付款"/>

~~~

