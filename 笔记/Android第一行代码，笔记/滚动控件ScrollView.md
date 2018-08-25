# 滚动控件ScrollView

## 用法

一共有两种，用法都差不多

- ScrollView：垂直滚动
- HorizontalScrollView：水平滚动

注意的问题

里面只能有一个控件，当然你可以嵌套

不能和ListView一起使用

~~~xml
<ScrollView
        android:id="@+id/screen"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fadeScrollbars="false"
        android:scrollbars="none">

        <HorizontalScrollView
            android:id="@+id/hsv"
            android:fadeScrollbars="false"
            android:scrollbars="none"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <TextView
                android:id="@+id/text"
                android:layout_width="wrap_content"
                android:layout_height="match_parent"/>

        </HorizontalScrollView>

</ScrollView>
~~~



意思是 不现实滑动按钮和滑动条

~~~xml
android:fadeScrollbars="false"
android:scrollbars="none"
~~~



scrollView自动滚动

~~~java

    //垂直的
    private void scrollToBottom(final ScrollView scrollView, final View view) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (scrollView == null || view == null) {
                    return;
                }
                // offset偏移量。是指当textview中内容超出 scrollview的高度
                // 那么超出部分就是偏移量
                int offset = view.getMeasuredHeight()
                        - scrollView.getMeasuredHeight();
                if (offset < 0) {
                    offset = 0;
                }
                //scrollview开始滚动
                scrollView.scrollTo(0, offset);
            }
        });
    }

//水平的
    private void scrollToRight(final HorizontalScrollView scrollView, final View view) {
     //开启一个子线程来处理滚动，这个线程可以对ui操作
     //可能就是
      handler.post(new Runnable() {
            @Override
            public void run() {
                if (scrollView == null || view == null) {
                    return;
                }
                // offset偏移量。是指当textview中内容超出 scrollview的高度
                // 那么超出部分就是偏移量
                int offset = view.getMeasuredWidth()
                        - scrollView.getMeasuredWidth();
                if (offset < 0) {
                    offset = 0;
                }
                //scrollview开始滚动
                scrollView.scrollTo(offset, 0);
            }
        });
    }

// 跳转至开头  
scrollView.fullScroll(ScrollView.FOCUS_UP); 
// 跳转至结尾  
scrollView.fullScroll(ScrollView.FOCUS_DOWN); 
~~~

