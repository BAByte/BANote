[TOC]

# ButterKnife使用(简化代码利器)

## 先说两句

> 中文名字：黄油刀，第一次翻译出来的时候我还以为翻译错了。。。按照惯例也是先看看名字与功能的关系，外国人吃面包抹黄油，抹黄油麻烦，所以有了黄油刀，简单的说就是方便。
>
> 看看网上普遍说的方便在哪里：Butter Knife，专门为Android View设计的绑定注解，专业解决各种`findViewById`。重点！！！！意思就是说可以简化我们的findViewById操作？？？
>
> 功能：通过View的Id自动找到View，然后自动转换类型！！！还可以注册监听事件！！！简而言之就是将初始化View的过程简化了
>
> 项目地址：https://github.com/JakeWharton/butterknife
>
> 导入地址：(两个都要导入，不然出错)
>
> compile 'com.jakewharton:butterknife:8.8.1'annotationProcessor '
>
> com.jakewharton:butterknife-compiler:8.8.1'

## 大致原理

> 看到这个框架的功能自动转换类型时我第一反应就是，用反射吗？那性能吃的消吗？？？
>
> 官方解释是：与缓慢的反射相比，Butter Knife使用在编译时生成的代码来执行View的查找，因此不必担心注解的性能问题。调用`bind`来生成这些代码，你可以查看或调试这些代码。
>
> 说实话：在编译时生成的代码，还真的看懵逼了我，，

## 绑定View

> + 需求：初始化绑定TextView
> + 原来的代码
>
> ~~~java
> //在OnCreate方法
> TextView textView=(TextView)findViewById(R.id.id_text);
> ~~~
>
> +  使用ButterKnife
>
> ~~~java
> public class MainActivity extends AppCompatActivity {
>
>     //先声明我要绑定这个View
>     @BindView(R.id.id_text) TextView textView;
>
>     @Override
>     protected void onCreate(Bundle savedInstanceState) {
>         super.onCreate(savedInstanceState);
>         setContentView(R.layout.activity_main);
>
>         //正式开始绑定
>         ButterKnife.bind(this);
>
>       	//测试是否获取到了TextView
>         textView.setText("多少个合适的价格还是进口的复活节");
>     }
> }
> ~~~
>
> ---

##一次绑定多个View

> ~~~java
> //里面放View的数组，在onCreate里面再Bind一下就好
> @BindViews({ R.id.first_name, R.id.middle_name, R.id.last_name })
> List<EditText> nameViews;
> ~~~
>
> 

##一次对View设置多个属性

> ```java
> //apply函数，该函数一次性在列表中的所有View上执行一个动作：
> //将list里面的所有View设置属性
> public class MainActivity extends AppCompatActivity {
>
> 	//找到多个View
>    @BindViews({R.id.text_one,R.id.text_tow,R.id.text_three})
>     List<TextView> list;
>
>   	//设置一个Action，用来设置View变成可见
>     static final ButterKnife.Action<View> VISIBLE=new ButterKnife.Action<View>() {
>         @Override
>         public void apply(@NonNull View view, int index) {
>             view.setVisibility(View.VISIBLE);
>         }
>     };
>
>     	//设置一个Action，用来设置View变成不可见
>     static final ButterKnife.Action INVISIBLE=new ButterKnife.Action() {
>         @Override
>         public void apply(@NonNull View view, int index) {
>             view.setVisibility(View.INVISIBLE);
>         }
>     };
>
>     @Override
>     protected void onCreate(Bundle savedInstanceState) {
>         super.onCreate(savedInstanceState);
>         setContentView(R.layout.activity_main);
>
>         //正式开始绑定
>         ButterKnife.bind(this);
>
>       	//调用这个方法，传入刚刚的List，Action，
>       	//就会给迭代list里面的view，去调用上面action设置的apply方法
>         ButterKnife.apply(list,INVISIBLE);
>
>         ButterKnife.apply(list,VISIBLE);
>     }
> }
>
> ```

## 在非Activity绑定View

> + 需求： 在RecyclerView和ListView的适配器里面的ViewHolder需要先绑定Item布局里面的子VIew，然后在数据绑定方法中才进行绑定数据
> ~~~java
>    static class ViewHolder {
>         TextView textView;
>      
>         public ViewHolder(View view) {
>             textView=(TextView)view.findById(R.id.textView);
>         }
>     }
> ~~~
> ---
> + 使用ButterKnife
>
> ~~~java
> static class ViewHolder {
>         @BindView(R.id.title)
>         TextView name;
>         @BindView(R.id.job_title) TextView jobTitle;
>
>         public ViewHolder(View view) {
>             ButterKnife.bind(this, view);
>         }
>     }
> ~~~
>
> 

## 设置监听事件

> ```java
> //这就相当于给id为 R.id.button设置了onClick事件监听
> @OnClick(R.id.button)
> public void setVisible(){
>     ButterKnife.apply(list,VISIBLE);
> }
>
> //设置onTouch事件
> @OnTouch(R.id.button)
> public void set(){
>      ButterKnife.apply(list,VISIBLE);
> }
>
> //为多个View设置处理同一个事件，说真的，我还没试过一个多个View设置同一个监听事件
> @OnClick({ R.id.door1, R.id.door2, R.id.door3 })
> public void pickDoor(DoorView door) {
>     if (door.hasPrizeBehind()) {
>         Toast.makeText(this, "You win!", LENGTH_SHORT).show();
>     } else {
>         Toast.makeText(this, "Try again", LENGTH_SHORT).show();
>     }
> }
> ```

> ---

## 在自定义View里面设置监听事件

> ~~~java
> public class FancyButton extends Button {
>     @OnClick
>     public void onClick() {
>         // TODO do something!
>     }
> }
> ~~~
>
> 

## 绑定资源

> 绑定资源到类成员上可以使用@BindBool、@BindColor、@BindDimen、@BindDrawable、@BindInt、@BindString。使用时对应的注解需要传入对应的id资源，例如@BindString你需要传入R.string.id_string的字符串的资源id。
>
> + 需求：获取Values下string.xml文件的字符串
> + 原来的代码
>
> ~~~java
>  textView.setText(R.string.app_name);
> ~~~
>
> + 使用ButterKnife
>
> ~~~java
> //这个写在全局位置，原因嘛，和实现的原理有关，可能自动转换类型是在类加载的时候就先确定类型了
> @BindString(R.string.app_name) String appName;
>
> //onCreate()方法里面的代码
> textView.setText(appName);
> ~~~
>
> ---

## 重置绑定

> Fragment的生命周期与Activity不同。在Fragment中，如果你在onCreateView中使用绑定，那么你需要在onDestroyView中设置所有view为null。为此，ButterKnife返回一个Unbinder实例以便于你进行这项处理。在合适的生命周期回调中调用unbind函数就可完成重置。
>
> ~~~java
> public class FancyFragment extends Fragment {
>     @BindView(R.id.button1) Button button1;
>     @BindView(R.id.button2) Button button2;
>     private Unbinder unbinder;
>
>     @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
>         View view = inflater.inflate(R.layout.fancy_fragment, container, false);
>         unbinder = ButterKnife.bind(this, view);
>         // TODO Use fields...
>         return view;
>     }
>
>     @Override public void onDestroyView() {
>         super.onDestroyView();
>         unbinder.unbind();
>     }
> }
> ~~~
>
> ---

## 使用Zelezny插件插件更加简化使用

> File/setting/Plugins/搜索Zelezny
>
> 在setContentView旁边的R.laytout.xxxx右键，然后，然后就会了，哈哈哈

## 使用时的一些问题

> ```
> Activity ButterKnife.bind(this);必须在setContentView();之后，且父类bind绑定后，子类不需要再bind
> Fragment ButterKnife.bind(this, mRootView);
> 属性布局不能用private or static 修饰，否则会报错
> setContentView()不能通过注解实现。
> ButterKnife已经更新到版本7.0.1了，以前的版本中叫做@InjectView了，而现在改用叫@Bind，更加贴合语义。
> 在Fragment生命周期中，onDestoryView也需要Butterknife.unbind(this)
> ButterKnife不能再你的library module中使用哦!!这是因为你的library中的R字段的id值不是final类型的，但是你自己的应用module中确是final类型的。针对这个问题，有人在Jack的github上issue过这个问题，他本人也做了回答，点击这里。
>
> 作者：CameloeAnthony
> 链接：http://www.jianshu.com/p/b6fe647e368b
> 來源：简书
> 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
> ```