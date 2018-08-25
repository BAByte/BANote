[toc]
# 安卓中的Drawable
## 先说两句
这个东西完全是靠记忆的，记住有什么作用，用法就好了，其实就是xml文件。
## level-list
这个东西很有意思，他可以根据不同的等级来显示不同的图片，类似于电量图标，他其实很多张，不同电量显示不同的图片，用这个东西是完全可以实现的，当然不建议这样实现，代码量太高。
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<level-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:drawable="@drawable/ic_blue_systemprompt"
        android:maxLevel="2"/>
    <item
        android:drawable="@drawable/ic_green_systemprompt"
        android:maxLevel="4"/>
</level-list>
~~~

使用
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<layout>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.example.ba.mydrawabledemo.MainActivity">

    <ImageView
        android:scaleType="centerCrop"
        android:id="@+id/imageView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/bitmap_drawable"/>

</android.support.constraint.ConstraintLayout>
</layout>
~~~

activity
~~~java
public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        //通过该方法更改等级就会自动的切换图片
        binding.imageView.setImageLevel(2);
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imageView.setImageLevel(3);
            }
        });
    }
~~~

## TransitionDrawable
这个可以实现图片显示的时候的渐变，具体效果你自己跑一下，
### xml实现
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<transition xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:drawable="@drawable/ic_blue_systemprompt" />
    <item
        android:drawable="@drawable/ic_green_systemprompt" />
</transition>
~~~

activity代码
~~~java
public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               TransitionDrawable drawable= (TransitionDrawable) binding.imageView.getDrawable();
               drawable.startTransition(1000);
            }
        });
    }
}
~~~

### java代码直接实现
因为用xml的话就写死了，用代码就可以动态切换图片
~~~java
public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);

        final TransitionDrawable transitionDrawable=new TransitionDrawable(
                new Drawable[]{getDrawable(R.drawable.ic_blue_systemprompt)
                        ,getDrawable(R.drawable.ic_green_systemprompt)});

        binding.imageView.setImageDrawable(transitionDrawable);
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionDrawable.startTransition(1000);
            }
        });
    }
}
~~~

## clipDrawable
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<clip xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/timg"
    android:gravity="center"
    android:clipOrientation="vertical|horizontal">
</clip>
~~~

~~~java
binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final ClipDrawable drawable= (ClipDrawable) binding.imageView.getDrawable();
                ValueAnimator animator=ValueAnimator.ofInt(10000).setDuration(500);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        drawable.setLevel((int)animation.getAnimatedValue());
                    }
                });
                animator.start();
            }
        });
~~~

通过代码设置也是可以的，这里就省略了
