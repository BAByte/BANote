[TOC]

# ViewPager

## 写在前面

其实就是一个大的容器，能够装下布局或者碎片，并且通过左右滑动切换布局或者碎片，既然能装下很多布局，就必须需要有适配器，根据适配器的类型ViewPager会加载不数目的界面，一般ViewPager只加载3个页面，多的会自动销毁，比如ViewPager一次会创建3个Pagr，当你在第一页的时候，ViewPagr会创建前3页，当你滑到第3页的时候，第一页已经被自动销毁

## 适配器种类

+ PagerAdapter

  > 普通的适配器装的是View,带有自动销毁

+ FragmentPaperAdapter

  > 适配Fragment的适配器，不带有自动销毁


+ FragmentStatePaperAdapter

  > 适配Fragment的适配器，带有自动销毁

## xml代码

```xml
    <android.support.v4.view.ViewPager
        android:layout_gravity="center"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/view_pager">

        <android.support.v4.view.PagerTabStrip
            android:id="@+id/pts"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
        </android.support.v4.view.PagerTabStrip>
        
        <!--两种都可以，这是默认只有文字的，上面的默认有线条，两个都不用也可以，两个同时用有一个会失效
        <android.support.v4.view.PagerTitleStrip
            android:layout_gravity="bottom"
            android:id="@+id/ptitles"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content">
        </android.support.v4.view.PagerTitleStrip>
        -->
    </android.support.v4.view.ViewPager>
```



## 使用PagerAdapter适配器

+ 同样需要继承

  ~~~java
  public class MyPagerAdaptot extends PagerAdapter{
    //将从这里取出View  
      public MyPagerAdaptot(List<View> list,List<String> titleList){
          this.list=list;
          this.titleList=titleList;
      }

      @Override
      public Object instantiateItem(ViewGroup container, int position) {
           container.addView(list.get(position));
          return list.get(position);
      }

    //比如ViewPager一次会创建3个Pagr，当你在第一页的时候，ViewPagr会创建前3页，也就会自动调用这个方法将对应的View价加载进ViewPager中
      @Override
      public Object instantiateItem(ViewGroup container, int position) {
           container.addView(list.get(position));
          return list.get(position);//返回当前布局
      }

      @Override
      public int getCount() {
          return list.size();
      }

    //这个用来判断是不是从对象中获取View
      @Override
      public boolean isViewFromObject(View view, Object object) {
          return view==object;
      }

    //当滑动到第3页时，第一页会被杀掉
      @Override
      public void destroyItem(ViewGroup container, int position, Object object) {
          container.removeView(list.get(position));
      }
    
    //这是返回标题栏的文字
    @Override
      public CharSequence getPageTitle(int position) {
          return titleList.get(position);
      }
  }
  ~~~

  ​

~~~java
        title=new ArrayList<>();
        title.add("ONE");
        title.add("TOW");
        title.add("THREE");
        title.add("Four");

		View view1= LayoutInflater.from(this).inflate(R.layout.view1,null);
        View view2= LayoutInflater.from(this).inflate(R.layout.view2,null);
        View view3= LayoutInflater.from(this).inflate(R.layout.view3,null);
        View view4= LayoutInflater.from(this).inflate(R.layout.view4,null);

        list.add(view1);
        list.add(view2);
        list.add(view3);
        list.add(view4);

//将布局加载成View对象
        MyPagerAdaptot  adaptot=new MyPagerAdaptot(list，title);

        PagerTabStrip pts=(PagerTabStrip)findViewById(R.id.pts);
        pts.setBackgroundColor(Color.BLUE);//设置导航栏背景颜色
        pts.setTabIndicatorColor(Color.RED);//设置导航栏滑动下方小线条的颜色
        pts.setTextColor(Color.BLACK);
        pts.setDrawFullUnderline(false);//隐藏分割线

//设置适配器
        ViewPager viewPager=(ViewPager)findViewById(R.id.view_pager);
        viewPager.setAdapter(adaptot);
~~~



## 使用FragmentPagerAdpater

~~~java
public class MyFragmentPagerAdapeter extends FragmentPagerAdapter {
    List<Fragment> list;
    List<String> titleList;
    public MyFragmentPagerAdapeter(FragmentManager fm,List<Fragment> list,List<String> titleList) {
        super(fm);//碎片管理器用来开启碎片的
        this.list=list;
        this.titleList=titleList;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
}
//在Activity的内容我就不写了
~~~



## 使用FragmentStateAdapter

~~~java
public class MyFragmentStatePagerAdapeter extends FragmentStatePagerAdapter {
    List<Fragment> list;
    List<String> titleList;
    public MyFragmentStatePagerAdapeter(FragmentManager fm, List<Fragment> list, List<String> titleList) {
        super(fm);
        this.list=list;
        this.titleList=titleList;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }

  //使用这个适配器会进行自动销毁，所以要复写，但是不用修改
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
~~~



## 监听当前是第几页

~~~java
 viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
     @Override
     public void onPageScrolled(int position, float positionOffset,
                                int positionOffsetPixels) {
         
     }
     @Override
     public void onPageSelected(int position) {
         //这里可以监听当前是第几页
     }
     @Override
     public void onPageScrollStateChanged(int state) {
     }
 });
~~~

## 给ViewPage添加动画
设置方法
~~~java
viewPager.setPageTransformer(true, new DepthPageTransformer());
~~~

+ 第一个参数：代表动画是逆向的还是正向的，true表示正向
+ 第二个参数就是动画，直接看类(这个类要我们自己写)
~~~java
public class DepthPageTransformer implements ViewPager.PageTransformer {
    private static final String TAG = "DepthPageTransformer";

    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.5f;

    public void transformPage(View view, float position)
    {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();
        Log.e("TAG", view + " , " + position + "");
        if (position <= 1)
        {
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0)
            {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else
            {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }
            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        }
    }
}
~~~

我们只看transformPage方法
+ 第一个参数：ViewPage的布局控件，注意，这个View会是同一屏幕内的Page(意思是这个View会不断的切换)，他是把两个VIew的动画，放到了一个方法来处理
+ 第二个参数就有意思了，他的值域有[-1,1],特别在哪里呢？看图吧![image](http://img.my.csdn.net/uploads/201702/15/1487151188_2956.png)


假设现在有ABC三个 Page我们以Page的左边界为基准，B正在屏幕上显示，当你右滑的时候，transformPage方法会不断被调用。当参数的View是A时，position的变化是[-1,0],当View是B时，position的变化是[0,1]

注意：你不要觉得，是transformPage方法先把B的动画搞定再把A的动画搞定，其实不是的，他们几乎可以说是同时的，不过一下做A的动画一下做B的动画

