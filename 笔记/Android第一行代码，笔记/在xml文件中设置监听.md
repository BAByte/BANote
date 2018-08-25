# 在xml文件中设置监听

```xml
<Button
    android:id="@+id/tan"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:onClick="onClick"
    android:textAllCaps="false">
```

倒数第二个就是设置了，这样在Activity中就不用向下面一样了，只要Activity实现监听接口，复写onClick方法即可

~~~java
Button B=(Button)findViewById(R.id.c);
        B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
~~~

