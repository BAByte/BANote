# ToggleButton

一个开关按钮

![捕获](E:\Android第一行代码，笔记\ToggleButton\捕获.PNG)

![捕获2](E:\Android第一行代码，笔记\ToggleButton\捕获2.PNG)

~~~xml
<ToggleButton
        android:id="@+id/bu"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:checked="true"
        android:textOff="关"
        android:textOn="开"
         />

<!--android:checked="true"就是用来记录是否被选中啦-->
~~~

~~~java
ToggleButton b=(ToggleButton)findViewById(R.id.bu);
        b.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
          //里面的参数可以判断状态
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
~~~

