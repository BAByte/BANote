# ListView使用

[TOC]



# 需求

![Screenshot_1499153027](E:\Android第一行代码，笔记\ListView使用\Screenshot_1499153027.png)

## 分析

> 我们用ListView做出一个简单的列表，从界面上看主要的组成：数据，显示每一个数据的布局，背后的容器，每一项数据都是和一个子项一起的，如何把数据和界面绑在一起就需要一个适配器了，就是说ListView只是需要一个适配器，我们简单使用时只要需要简单的写一个适配器，或者直接用安卓官方提供的适配器即可，适配器中直接操作的是view，convertview里面放view,view里面放缓存器，缓存器里面放控件，其实相当于两个缓存器，原因很简单，listview的适配器中加载布局和初始化控件和绑定数据是同步的，这样会导致效率底，所以我们要加一堆缓存器

## 具体实现

> 数据实体类
>
> ~~~java
> public class Word {
>     private int id;
>     private String content;//内容
>
>     public void setId(int id){
>         this.id=id;
>     }
>
>     public void setContent(String content){
>         this.content=content;
>     }
>
>     public int getId(){
>         return id;
>     }
>
>     public String getContent(){
>         return content;
>     }
> }
> ~~~
>
> 子项布局
>
> ~~~xml
> <?xml version="1.0" encoding="utf-8"?>
> <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
>     android:orientation="vertical" android:layout_width="match_parent"
>     android:layout_height="wrap_content">
>
>     <TextView
>         android:layout_width="wrap_content"
>         android:layout_height="wrap_content"
>         android:padding="10dp"
>         android:id="@+id/list_item"/>
>
> </LinearLayout>
> ~~~
>
> 适配器
>
> ~~~java
> import android.support.annotation.NonNull;
> import android.support.annotation.Nullable;
> import android.view.LayoutInflater;
> import android.view.View;
> import android.view.ViewGroup;
> import android.widget.ArrayAdapter;
> import android.widget.TextView;
>
> import java.util.List;
>
> /**
>  * Created by ljh99 on 2017/7/4 0004.
>  */
>
> //控件缓存器
> class ViewHolder{
>         TextView textView ;
>     }
>
> public class WordAdapter extends ArrayAdapter<Word> {
>
>     int itemId; //储存子项布局Id
>     List<Word> list; //储存数据实例的集合
>     public WordAdapter(Context context , int itemId, List<Word> list){
>         super(context,itemId,list);//调用父类构造函数进行必要的初始化
>         this.itemId=itemId;
>         this.list=list;
>     }
>
>   //复写getView方法，每一个子项滚到屏幕里面时都会自动调用该方法
>     @NonNull
>     @Override
>     public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
>       
>         //获取数据实例，position是当前数据所在的位置，这个位置就是刚才在父类构造函数
>       //里传入的list集合中，当前滚入屏幕的数据实例的位置，所以这和我们传进来的list同一个
>         Word word = list.get(position);
>     
>         View view;
>         ViewHolder viewHolder;
>       
>       //convertView,存储了上一个子项，是自动的，主要是为了提高效率，加载过的布局
>       //已经初始化过的控件不再加载和初始化
>         if(convertView==null) {
>             view = LayoutInflater.from(getContext()).inflate(itemId, parent, false);
>           
>           //viewHolder，是用来存储控件的，也是为了提高效率，为了初始化过的控件不再初始化
>             viewHolder= new ViewHolder();
>             viewHolder.textView = (TextView) view.findViewById(R.id.list_item);
>           
>           //将viewHolder存到view中，在将数据显示出来时，会自动将viewHoder里面的控件取出来显示
>           //就是说系统本来是显示view里面的textView现在加了ViewHolder后，系统会自动将
>           //view的ViewHolder里面的TextView取出来
>             view.setTag(viewHolder);
>         }
>         else {
>             view = convertView;
>           
>           //这里取出viewHolder是为了下面设置数据
>             viewHolder=(ViewHolder)view.getTag();
>         }
>         viewHolder.textView.setText(word.getContent());
>         return view;
>     }
>
>     
> }
> ~~~
>
> 活动的布局只要添加一个LisrtView
>
> ~~~xml
> 	<ListView
>         android:id="@+id/list_view"
>         android:layout_width="match_parent"
>         android:layout_height="match_parent">
>     </ListView>
> ~~~
>
> 在活动中使用
>
> ~~~java
> ListView listView=(ListView)findViewById(R.id.list_view);
>
> //初始化适配器
> WordAdapter adapter=new WordAdapter(this,R.layout.item,list);
>
> //为ListViwe设置适配器
> listView.setAdapter(adapter);	
>
> //为每一项子项设置监听事件
> listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
>             @Override
>             public void onItemClick(AdapterView<?> parent, View view, int position, long 				id) {
>                 Toast.makeText(MainActivity.this,
>                         list.get(position).getContent(), Toast.LENGTH_SHORT).show();
>             }
>         });
> ~~~
>
> 