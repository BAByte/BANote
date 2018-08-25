[TOC]

# AutoCompleteTextView 单个匹配

## 简介

这是一个在输入文本框时根据已有数据自动补全的控件，用处就是看图吧，注意只有自动补全，没有搜索

![捕获](E:\Android第一行代码，笔记\捕获.PNG)



## 用法

~~~xml
<AutoCompleteTextView
        android:id="@+id/tv"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:completionThreshold="2"/>
<!--最后一句的意思是在输入到两个字符时进行搜索-->
~~~

~~~java
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] s = new String[]{
                "beijin", "tianjin", "shanghai"};

        AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.tv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, s);
        tv.setAdapter(adapter);
    }
~~~



# MultiAutoCompleteTextView 多个自动匹配

看图

![捕获2](E:\Android第一行代码，笔记\TextView输入自动匹配\捕获2.PNG)

~~~xml
<MultiAutoCompleteTextView
        android:id="@+id/tv"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:completionThreshold="2"/>
~~~



~~~java
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] s = new String[]{
                "beijin", "tianjin", "shanghai"};

        MultiAutoCompleteTextView tv = (MultiAutoCompleteTextView) findViewById(R.id.tv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, s);
        tv.setAdapter(adapter);
        //设置分隔符，这里是逗号
        tv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

~~~

