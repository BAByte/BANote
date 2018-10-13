[TOC]

# 导航栏使用返回上一层Activity按钮

## 效果图

![actionbar-up](E:\Android第一行代码，笔记\在actionBar上使用返回到上一层Activity按钮\actionbar-up.png)



## 具体实现一

---

~~~java

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      // true代表 给左上角图标的左边加上一个返回的图标 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){ //这是这个返回按钮的默认图标id
            case R.id.home:
                finish();
                break;
        }
        return true;
    }
   
}

~~~



## 实现二

---

+ 如果自己不设置点击事件，但是又想返回上一层就要为当前Acitvity指定一个上层的Activity，

  ~~~xml
  <activity android:name=".Main2Activity"
              android:parentActivityName=".MainActivity">
    </activity>
  ~~~

+ 然后在activity中只要有一句就好

  ~~~java
  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // true代表 给左上角图标的左边加上一个返回的图标 
  ~~~

  ​