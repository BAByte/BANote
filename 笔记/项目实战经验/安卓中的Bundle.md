[TOC]

# 安卓中的Bundle

## 先说两句

> 这个类没有什么特别的地方，就是一个内部利用了HashMap实现了键值存储功能的类
>
> 目前我遇到的有两种使用场景
>
> + Activity之间传递数据(不如直接Intent来的痛快，而且这样的传输方式使用场景也有限，看下去你就知道为毛了)
> + 在系统内存不足时，有的Activity会直接被杀死，这就可能会导致Activity上的数据丢失，就会用Bundle来存储这些数据

## 用来在Activity之间传递数据

~~~java
//直接点吧,将数据放在bundle中然后返回
Bundle bundle=new Bundle();
Intent resultIntent = new Intent();
bundle.putString("result", rawResult.getText());
resultIntent.putExtras(bundle);
this.setResult(RESULT_OK, resultIntent);

//接收后读取数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK) {
                Bundle bundle = data.getExtras();
			   bundle.getString("result");
            }
        }
    }
~~~

---

你是不是很懵逼，明明Intent就可以传数据，不记得的可以翻翻 **用Intent传输数据** 这篇笔记，用Intent传，代码更加简洁，那两种有没有区别呢？给你看看源码吧

~~~java
/**
     * Add extended data to the intent.  The name must include a package
     * prefix, for example the app com.android.contacts would use names
     * like "com.android.contacts.ShowAll".
     *
     * @param name The name of the extra data, with package prefix.
     * @param value The Bundle data value.
     *
     * @return Returns the same Intent object, for chaining multiple calls
     * into a single statement.
     *
     * @see #putExtras
     * @see #removeExtra
     * @see #getBundleExtra(String)
     */
    public Intent putExtra(String name, Bundle value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putBundle(name, value);
        return this;
    }
~~~

---

有没有说卧槽，，，其实里面Intent就是用了一个bundle，所以这两种方法没有区别！！！！看你喜欢咯

---

## 用来保存Activity被不正常销毁时的数据

###需求

> A活动中有输入框，要输入验证码，我在上面输入了一半，但是不记得了，然后我打开短信查看验证码,返回到A活动时发现之前输入的没了！！！原因是A活动由于系统空间不足被销毁了，现在要在A活动被销毁前保存重要的数据

###具体实现

- 在A活动被销毁前，告诉系统，这里有重要的数据，并且进行保存

> ```java
> @Override
>     protected void onSaveInstanceState(Bundle outState) { //数据将保存在Bundle的对象中
>         super.onSaveInstanceState(outState);
>         String data="可能丢失的数据";
>         outState.putString("data_key",data);
>     }
> ```

- 在创建A活动时判断有没有被保留下来的数据

  > ```java
  > @Override
  >     protected void onCreate(Bundle savedInstanceState) {
  >         super.onCreate(savedInstanceState);
  >         setContentView(R.layout.activity_main);
  >         if(savedInstanceState!=null){
  >             savedInstanceState.getString("data_key");
  >         }
  >     }
  > ```

## 