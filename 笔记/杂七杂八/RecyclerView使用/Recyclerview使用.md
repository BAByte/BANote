[TOC]

# Recyclerview使用

## 需求

![Screenshot_1499153027](D:\Android第一行代码，笔记\RecyclerView使用\Screenshot_1499153027.png)



## 分析

> + RecyclerView是一个非常强大的滚动控件（废话），强大不是在上图，是在它可以实现不同的列表效果！！！这取决与你为RecyclerView设置怎样的布局管理器，常见的有
>
> + 瀑布流：new StaggerefGrifLayoutManager(列数，StaggerefGrifLayoutManager.方向)
>
> + 网格：new GridLayoutManager();
>
> + 用起来我觉得比ListView还方便，好理解。RecyclerView使用起来不同于ListView的最直观的地方大概就是适配器和布局管理器了，先看适配器RecyclerView中，布局的加载和控件的初始化，和数据的捆绑都是分开的，相对ListView没优化前的好处就是效率高了。
>
> + 组成依旧是，RecyclerView这个大容器，适配器，数据，布局管理器，RecycylerView主要写的就是ViewHolder
>
> + 记得导入包
>
> + ```xml
>   compile 'com.android.support:recyclerview-v7:25.3.1'
>   ```

## 具体实现步骤

> + 子布局文件我就不贴出来了，数据实体类也不贴，和listView是一样的


> +  适配器，代码
>
>    ~~~java
>    import android.support.v7.widget.RecyclerView;
>    import android.util.Log;
>    import android.view.LayoutInflater;
>    import android.view.View;
>    import android.view.ViewGroup;
>    import android.widget.TextView;
>    import android.widget.Toast;
>
>    import java.util.List;
>
>    /**
>    * Created by ljh99 on 2017/7/4 0004.
>    */
>      //继承看清楚
>    public class RecyclerAdapter extends  RecyclerView.Adapter<ViewHolder> {
>     List<Word> list;
>      
>      //缓冲器类。在这里初始化控件
>      class ViewHolder extends RecyclerView.ViewHolder{
>
>          TextView textView;
>          public ViewHolder(View v){
>              super(v);
>              textView=(TextView)v.findViewById(R.id.list_item);
>          }
>      }
>
>      public RecyclerAdapter(List<Word> list){
>          this.list=list;
>      }
>
>    //创建缓冲器，当子项被滚入屏幕内时会自动调用该方法，每项子项只加载一次，主要是加载布局，还有为控件设置监听
>      @Override
>      public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
>          View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
>          final ViewHolder viewHolder=new ViewHolder(view);
>          viewHolder.textView.setOnClickListener(new View.OnClickListener() {
>              @Override
>              public void onClick(View v) {
>                  Word w=list.get(viewHolder.getAdapterPosition());
>                  Toast.makeText(parent.getContext(), w.getContent(), Toast.LENGTH_SHORT).show();
>              }
>          });
>          return viewHolder;//返回这个估计就是系统用来存放已经加载过的子项，还有捆绑数据时用
>      }
>
>    //将数据与控件捆绑
>      @Override
>      public void onBindViewHolder(ViewHolder holder, int position) {
>          Word word=list.get(position);
>          holder.textView.setText(word.getContent()); 
>      }
>
>    //这个估计就是系统用来统计有多少子项的，用来判断子项有没有加载完的
>      @Override
>      public int getItemCount() {
>          return list.size();
>      }
>      
>        }
>    ~~~
>
> + 要使用的活动的布局文件这样引入
>
>   ~~~xml
>    	<android.support.v7.widget.RecyclerView
>           android:id="@+id/recyclerview"
>           android:layout_width="match_parent"
>           android:layout_height="match_parent">
>       </android.support.v7.widget.RecyclerView>
>   ~~~
>
>
> + 活动中这样使用
>
>   ~~~java
>   RecyclerView recyclerView =(RecyclerView)findViewById(R.id.recyclerview);
>   RecyclerAdapter recyclerAdapter =new RecyclerAdapter(list);
>
>   //这里我用的是垂直线性的布局管理器
>   LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
>
>   recyclerView.setAdapter(recyclerAdapter);
>
>   //设置布局管理器
>   recyclerView.setLayoutManager(linearLayoutManager);
>   ~~~
>
>   ---

## 进阶一(了解一些属性的设置)

```java
//这个属性设置后回有什么影响？这个属性的意思就是，item的改变不会引起RecyclerView的大小变化，为什么会说大小的变化呢？因为有时候在item被点击啊什么的，item可能会发生大小的变化，然后RecyclerView的大小也要跟着发生变化，这样的话RecyclerVIew就要重绘，所以说，当你的item大小是固定的时候，就可以设置成true，这样可以提高性能
recyclerview.setHasFixedSize(true);
```

---

## 进阶二(实现Item多布局加载)

看看需求先：

![捕获](D:\Android第一行代码，笔记\RecyclerView使用\捕获.PNG)

---

### 分析

什么时候将Item布局加载进来？就是创建ViewHolder的时候啊，recyclerView是可以有多个ViewHolder的

### 代码

两个不同的Item布局我就不写了

数据类看看吧

~~~java
/**
 * Created by BA on 2017/11/6 0006.
 *
 * @Function : 用来存放数据，和要展示的布局，这里不一定要这样写，你可以有不同的数据类，看下去吧
 */

public class MyData {

  //你可以用数据类的类型来判断需要那种布局类型，就可以不用这样保存该数据要用什么样的布局显示
    public final static int TYPE_SMALL=0;
    public final static int TYPE_BIG=1;

    private int itemType;

    private String text;

    public MyData(String text,int itemType){
        this.text=text;
        this.itemType=itemType;
    }

    public String getText() {
        return text;
    }
    public int getItemType(){
        return itemType;
    }
}
~~~

---

### adapter代码

~~~java
/**
 * Created by BA on 2017/11/6 0006.
 *
 * @Function : 
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<MyData> datas;
    private Context mContext;

    public MyRecyclerViewAdapter(Context context,List<MyData> datas){
        mContext=context;
        this.datas=datas;
    }

  //第一种布局的viewHolder
    public static class mBigViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public TextView text;
        public mBigViewHolder(View itemView) {
            super(itemView);
            cardView=(CardView) itemView.findViewById(R.id.car_view);
            text=(TextView)itemView.findViewById(R.id.text);
        }
    }

  //第二种布局的viewholder
    public static class mSmallViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public mSmallViewHolder(View itemView) {
            super(itemView);
            cardView=(CardView) itemView.findViewById(R.id.car_view);
        }
    }
  
  //这个方法会在调用onCreateViewHolder前调用，返回值就是onCreateViewHolder方法的第二个参数
    @Override
    public int getItemViewType(int position) {
      //前面说了，你可以根据数据类的类型来判断，而不是像我这样特地去记录
        return datas.get(position).getItemType();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder=null;
      //根据返回的View类型来决定加载什么样式的布局
        if (viewType==MyData.TYPE_BIG){
            View view= LayoutInflater.from(mContext).inflate
              (R.layout.recyclerview_itemlayout_big,parent,false);
            viewHolder =new mBigViewHolder(view);
        }else {
            View view= LayoutInflater.from(mContext).inflate
              (R.layout.recyclerview_itemlayout_small,parent,false);
            viewHolder =new mSmallViewHolder(view);
        }
        return viewHolder;
    }

...
~~~

## 使用时遇到的坑
看到上面代码中onCreateViewHolder()方法，这个方法，我统计了下，它好像并不会为每一个Item调用这个方法，onCreateViewHolder只有在没有RecyclerView可重用的现有ViewHolder者时才创建新的视图持有者。因此，例如，如果您一次RecyclerView可以显示5个项目，则会创建5-6个项目ViewHolders，然后每次调用时自动重新使用它们onBindViewHolder。所以在使用的时候要注意一下，比如里面有一个View，它设置了Visibility属性，那另外的Item可能会用用到这个ViewHolder，那就GG了，会导致你的View的Visibility属性不符合你的预期

---

## 添加FootView和HeadView，内部再嵌套的横向展示的RecyclerView

在ListView是有两个方法**addHeaderView，addFooterView，**用来添加头部列表的View和底部View

先来看看什么是HeaderView和FootView吧，算了，我懒得截图了，就是在List列表的最后显示，一行字没有数据了什么的，或者一次不加载太多数据，把当前数据加载完后就继续加载，就在最底部提示正在加载数据。就是用上面的原理，判断是不是最底部啊，或者最顶部啊，就加载不同的布局嘛，主要还是改Adapter的方法，先根据不同布局写出不同的ViewHolder，然后在Adapter处理一下

~~~java
  /**
     * @return
     * @throws
     * @fuction 根据View类型来初始化ViewHolder
     * @parm
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if (viewType == FootViewHolder.TYPE_FOOT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_itemlayout_foot, parent, false);
            viewHolder = new FootViewHolder(view); //自己写
        } else if (viewType == HeadViewHolder.TYPE_HEAD) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_itemlayout_head, parent, false);
            viewHolder = new HeadViewHolder(view); //自己写
        } else if (viewType == HorizonViewHolder.TYPE_HORIZON) {
            Log.d("aaaaa", "onCreateViewHolder: horizon");
            View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_itemlayout_horizon_list, parent, false);
            viewHolder = new HorizonViewHolder(view); //自己写
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_itemlayout, parent, false);
            viewHolder = new NormalViewHolder(view); //自己写
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //根据View的类型来初始化数据
        int viewType = getItemViewType(position);
        if (viewType == HorizonViewHolder.TYPE_HORIZON) {
            ((HorizonViewHolder) holder).setData(mContext, datas.get(position).getList());
        } else if (viewType == FootViewHolder.TYPE_FOOT) {
            ((FootViewHolder) holder).textView.setText("正在加载");//如果你需要其他状态，可以自己写
        } else if (viewType != HeadViewHolder.TYPE_HEAD) {
            ((NormalViewHolder) holder).textView.setText(datas.get(position).getText());
        }
    }



    /**
     * @return
     * @throws
     * @fuction 根据数据类型确定ViewHolder的类型
     * @parm 根据位置判断
     */
    @Override
    public int getItemViewType(int position) {
        Log.d("sssss", "getItemViewType: " + position + "---" + getItemCount());
        if (position == 0)
            return HeadViewHolder.TYPE_HEAD; //头部
        else if (position == (getItemCount() - 1))
            return FootViewHolder.TYPE_FOOT; //尾部
        else if (datas.get(position).getList() != null) {
            Log.d("aaaa", "getItemViewType: Type_horizon");
            return HorizonViewHolder.TYPE_HORIZON; //水平嵌套一个水平滑动的RecyclerView
        }

        return TYPE_NORMAL;
    }
//项目地址：https://github.com/ljh998/BABestRecyclerViewDemo
~~~

---

## 实现加载更多(下拉刷新就不写了，直接嵌套下拉刷新的布局就好)
有时候需要分段加载数据，其实和RecyclerView是没什么关系的，反正你只要在一个合适的时候给数据，对吧，看看代码
~~~java

//一定要在不滑动的时候加载数据，不然会抛出异常
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //如果不是在滑动，而且最后一个可见的View的位置加一等于数据总数，那就是说明滑到了最后一个，需要看看有没有数据加载了
                if (newState==RecyclerView.SCROLL_STATE_IDLE&&lastVisiablePosition+1==adapter.getItemCount()){
                    initList();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    },1500);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisiablePosition=linearLayoutManager.findLastVisibleItemPosition();
            }
        });
    }
    
~~~
## 进阶二 给Item加点装饰
在ListView中每一个项是有分割线的，RecyclerView要加也可以，直接在子布局文件里面加一个高度为1px的View就好，但是，但是，逼格这么低的事情，是吧，那安卓官方当然会提供方法吧，看下面
~~~java
//recyclerView有这个方法，看中文名字就知道，叫子项装饰，
recyclerView.addItemDecoration(RecyclerView.ItemDecoration  dd);
~~~
现在知道了是这个方法实现，但是要传一个参数啊，参数怎么来？自己写一个类呗，这个类要继承自RecyclerView.ItemDecoration 类，先看看这个类有主要的方法
~~~java
//该方法和View的一样，只要屏幕内容有变化就会不断调用，而且这个方法绘制的不是item的背景，是recyclerView的背景
public void onDraw(Canvas c, RecyclerView parent, State state)

//同上，但是绘制的是RecyclerView的前景
public void onDrawOver(Canvas c, RecyclerView parent, State state)

//这个就是针对Item的边距，但是不会绘制东西的，和加了padding属性一样，该方法在View进入屏幕时会被调用
public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state)

作者：小武站台
链接：http://www.jianshu.com/p/b46a4ff7c10a
來源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
~~~
先看3幅图，绿色代表Item里面的View，红色代表空白部分
![image](http://upload-images.jianshu.io/upload_images/186157-1ab2f9304503506c.png?imageMogr2/auto-orient/strip%7CimageView2/2)

+ 第一幅图对应的是getItemOffsets(),就像是padding属性。加边框而已，这个在使用瀑布流的时候可能需要
+ 第二幅图，对应的是onDraw()方法，画的是背景
+ 第三就对应的是onGrawOver((),画的是前景

### 来个栗子吧
给Item添加标签，会了这个，分割线也就不难了
~~~java
public class LeftAndRightTagDecoration extends RecyclerView.ItemDecoration {
    private int tagWidth;
    private Paint leftPaint;
    private Paint rightPaint;

    public LeftAndRightTagDecoration(Context context) {
        leftPaint = new Paint();
        leftPaint.setColor(context.getResources().getColor(R.color.colorAccent));
        rightPaint = new Paint();
        rightPaint.setColor(context.getResources().getColor(R.color.colorPrimary));
        tagWidth = context.getResources().getDimensionPixelSize(R.dimen.tag_width);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(child);
            boolean isLeft = pos % 2 == 0;
            if (isLeft) {
                float left = child.getLeft();
                float right = left + tagWidth;
                float top = child.getTop();
                float bottom = child.getBottom();
                c.drawRect(left, top, right, bottom, leftPaint);
            } else {
                float right = child.getRight();
                float left = right - tagWidth;
                float top = child.getTop();
                float bottom = child.getBottom();
                c.drawRect(left, top, right, bottom, rightPaint);

            }
        }
    }
}

~~~

## 小结
ItemDecoration 是可以叠加的，其实这里就是和自定义View一样，都是在绘制，很难的一个点，反正那个HeadSelect的例子我是没写对，，，

## 给Item加点动画
RecyclerView是默认实习了动画的，你可以在删除一个Item或者添加一个Item时候可以看到，但是如果我们不满足这个效果呢？Recycler是允许你自己定义动画的，怎么定义？别急，先看看设置自定义动画的方法
~~~java
recyclerView.setItemAnimator(ItemAnimator animator);
~~~
也就是说Item动画是ItemAnimator这个类实现了，ItemAnimotor是抽象类，源码如下，那又要找具体实现了。哎！！前面不是说有默认的动画吗？找啊！！找啊找啊找啊找，并不难，哈哈哈，下面是默认Item动画的实现，
~~~java
public class DefaultItemAnimator extends SimpleItemAnimator {
   ...
}
~~~

继承了SimpleItemAnimator类意味着我们自定义动画时，也要继承这个类，然后复写一些方法，其实这里又体会到了RecyclerView设计的优雅之处，要实现就直接自己写，然后热拔插一样，直接传入，屌不屌？拿就看看需要复写什么方法吧
~~~java
 @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        return false;
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        return false;
    }
    
    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        return false;
    }


    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop) {
        return false;
    }
    
    @Override
    public void runPendingAnimations() {

    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {

    }

    @Override
    public void endAnimations() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
~~~
都是一些回调方法，就像Activity的生命周期回调方法一样，看看那些方法是什么时候被调用的，但在此之前，先看看触发Item动画有什么情况
+ 添加Item
+ 删除Item
+ 当一个Item删除的或者添加的时候，其他Item就要移动，这时候也有动画
+ Item有变化时需要更新的动画

一共四个，也就是上面的前四个方法。看看这个四个方法在实现默认动画的那个类里面是怎样写的，但是！！！4个啊，。我们就先看添加Item的动画是怎么实现的
~~~java
    @Override
    public boolean animateAdd(final ViewHolder holder) {
        //重置动画，不然如果动画在进行，那就不能设置动画的
        resetAnimation(holder);
        //设置动画初始值
        ViewCompat.setAlpha(holder.itemView, 0);
        //设置完后待命
        mPendingAdditions.add(holder);
        //返回true，说明给这个Item开始播放动画做了准备
        return true;
    }
~~~
看看resetAnimation方法中的代码
~~~java
 private void resetAnimation(ViewHolder holder) {
        //清除动画效果
        AnimatorCompatHelper.clearInterpolator(holder.itemView);
        //结束后复原所有当前正在执行动画View的原来状态
        endAnimation(holder);
    }
~~~

清除动画效果，然后复原状态，这个endAnimation(holder);方法也是那几个抽象类之一，我们看看默认动画类是怎样处理的(其实就是将View状态复原，)

~~~java

    @Override
    public void endAnimation(ViewHolder item) {
        final View view = item.itemView;
        // this will trigger end callback which should set properties to their target values.
        ViewCompat.animate(view).cancel();
        // TODO if some other animations are chained to end, how do we cancel them as well?
        for (int i = mPendingMoves.size() - 1; i >= 0; i--) {
            MoveInfo moveInfo = mPendingMoves.get(i);
            if (moveInfo.holder == item) {
                ViewCompat.setTranslationY(view, 0);
                ViewCompat.setTranslationX(view, 0);
                //调用父类的方法通知系统，这个动画完成了
                dispatchMoveFinished(item); 
                mPendingMoves.remove(i);
            }
        }
        endChangeAnimation(mPendingChanges, item);
        if (mPendingRemovals.remove(item)) {
            ViewCompat.setAlpha(view, 1);
            dispatchRemoveFinished(item);
        }
        if (mPendingAdditions.remove(item)) {
            ViewCompat.setAlpha(view, 1);
            dispatchAddFinished(item);
        }

        for (int i = mChangesList.size() - 1; i >= 0; i--) {
            ArrayList<ChangeInfo> changes = mChangesList.get(i);
            endChangeAnimation(changes, item);
            if (changes.isEmpty()) {
                mChangesList.remove(i);
            }
        }
        for (int i = mMovesList.size() - 1; i >= 0; i--) {
            ArrayList<MoveInfo> moves = mMovesList.get(i);
            for (int j = moves.size() - 1; j >= 0; j--) {
                MoveInfo moveInfo = moves.get(j);
                if (moveInfo.holder == item) {
                    ViewCompat.setTranslationY(view, 0);
                    ViewCompat.setTranslationX(view, 0);
                    dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        mMovesList.remove(i);
                    }
                    break;
                }
            }
        }
        for (int i = mAdditionsList.size() - 1; i >= 0; i--) {
            ArrayList<ViewHolder> additions = mAdditionsList.get(i);
            if (additions.remove(item)) {
                ViewCompat.setAlpha(view, 1);
                dispatchAddFinished(item);
                if (additions.isEmpty()) {
                    mAdditionsList.remove(i);
                }
            }
        }
~~~
再回到animateAdd()方法中，所以目前是进行了一些Item开始动画之前的准备工作，注意上面待命的注释，他把这个itemView存到了一个集合里面，看集合名字就知道了：即将开始动画的view，接下来就看看什么时候去用咯，返回true后系统会调用五个抽象中的runPendingAnimations();方法，看看这个方法，又干了什么
~~~java
@Override
    public void runPendingAnimations() {

        //进行判断当前需要执行什么类型动画
        boolean removalsPending = !mPendingRemovals.isEmpty();
        boolean movesPending = !mPendingMoves.isEmpty();
        boolean changesPending = !mPendingChanges.isEmpty();
        boolean additionsPending = !mPendingAdditions.isEmpty();
        
        //都没有就算了呗，下面的代码都是有省略的，我们只看添加的动画
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return;
        }
        
        ...
    
        //这里先不看
        for (ViewHolder holder : mPendingRemovals) {
            animateRemoveImpl(holder);
        }
        mPendingRemovals.clear();
       
       
       ...
       
       //这里就是去实现添加动画的咯
        if (additionsPending) {
            //搞一个集合的副本
            final ArrayList<ViewHolder> additions = new ArrayList<>();
            additions.addAll(mPendingAdditions);
            
            //将在等待执行动画的View集合，放在正在展示的集合中
            mAdditionsList.add(additions);
            
            //清除
            mPendingAdditions.clear();
            
            //开始去实现动画了
            Runnable adder = new Runnable() {
                @Override
                public void run() {
                    for (ViewHolder holder : additions) {
                        //这里，这里这个方法实现具体动画
                        animateAddImpl(holder);
                    }
                    additions.clear();
                    mAdditionsList.remove(additions);
                }
            };
            
            //如果现在是在进行删除操作就要到删除动画完成后才执行当前动画
            if (removalsPending) {
                View view = moves.get(0).holder.itemView;
                ViewCompat.postOnAnimationDelayed(view, mover, getRemoveDuration());
            } else {
                mover.run();
            }
        }
    }
~~~

下面来看看具体动画的实现方法"animateAddImpl(holder)"的源码
~~~java
 void animateAddImpl(final ViewHolder holder) {
        final View view = holder.itemView;
        //获取操作动画的对象
        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(view);
        //这个集合是用来判断当前是否用动画在执行
        mAddAnimations.add(holder);
        animation.alpha(1).setDuration(getAddDuration()).
                setListener(new VpaListenerAdapter() {
                    @Override
                    public void onAnimationStart(View view) {
                        dispatchAddStarting(holder);
                    }
                    @Override
                    public void onAnimationCancel(View view) {
                        ViewCompat.setAlpha(view, 1);
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        //当动画完成时动画监听设置为null
                        animation.setListener(null); 
                        //添加动画完成成后调用该方法
                        dispatchAddFinished(holder);
                        //完成了就移除集合嘛
                        mAddAnimations.remove(holder);
                        //去判断是否所有的动画都已经完成
                        dispatchFinishedWhenDone();
                    }
                }).start();
    }
    ~~~

在上面代码中的动画Listener中的onAnimatorEnd方法中会做一些完成动画后需要做的一些工作，看看第一步是设置动画监听为null，因为该动画已经完成了，然后调用父类的dispatchAddFinished(holder);通知系统，我们已经完成了添加动画的执行，然后从集合中移除，最后调用了一个 dispatchFinishedWhenDone();这个方法是默认动画类的，我们看看
~~~java
  /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call {@link #dispatchAnimationsFinished()} to notify any
     * listeners.
     */
    void dispatchFinishedWhenDone() {
        //要判断是否所有动画都完成了，是就调用父类的方法通知系统，所有动画已经完成
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }
~~~

isRunning()的代码
~~~java
  @Override
    public boolean isRunning() {
        return (!mPendingAdditions.isEmpty() ||
                !mPendingChanges.isEmpty() ||
                !mPendingMoves.isEmpty() ||
                !mPendingRemovals.isEmpty() ||
                !mMoveAnimations.isEmpty() ||
                !mRemoveAnimations.isEmpty() ||
                !mAddAnimations.isEmpty() ||
                !mChangeAnimations.isEmpty() ||
                !mMovesList.isEmpty() ||
                !mAdditionsList.isEmpty() ||
                !mChangesList.isEmpty());
    }
~~~
最后看看上面用到的集合，谷歌使用三种区分状态的集合，看下面代码
~~~java
    //这组是记录正在准备好执行动画的View集合
    private ArrayList<ViewHolder> mPendingRemovals = new ArrayList<>();
    private ArrayList<ViewHolder> mPendingAdditions = new ArrayList<>();
    private ArrayList<MoveInfo> mPendingMoves = new ArrayList<>();
    private ArrayList<ChangeInfo> mPendingChanges = new ArrayList<>();

    //这组是正在处理动画的view的集合
    ArrayList<ArrayList<ViewHolder>> mAdditionsList = new ArrayList<>();
    ArrayList<ArrayList<MoveInfo>> mMovesList = new ArrayList<>();
    ArrayList<ArrayList<ChangeInfo>> mChangesList = new ArrayList<>();

    //这组是正在的执行动画
    ArrayList<ViewHolder> mAddAnimations = new ArrayList<>();
    ArrayList<ViewHolder> mMoveAnimations = new ArrayList<>();
    ArrayList<ViewHolder> mRemoveAnimations = new ArrayList<>();
    ArrayList<ViewHolder> mChangeAnimations = new ArrayList<>();
~~~
这样在使用过程中就很好的区分动画事件的状态

上面的所讲的是一个Add动画的实现过程，
1. 初始化执行动画前的状态
2. 开始给当前View设置动画
3. 给当前View执行动画
4. 完成动画后的处理工作

但是如果动画被系统取消了呢？那那些动画执行到一半界面肯定混乱那就要像上面的endAnimation()方法一样，对View进行复原。别急，上面不是还有一个复写父类的方法还没有用到嘛
 
~~~java

//当动画需要被取消的时候回调该方法,在这里将View还原为原来的样子
      @Override
    public void endAnimations() {
        int count = mPendingMoves.size();
        ...
        count = mPendingAdditions.size();
        for (int i = count - 1; i >= 0; i--) {
            ViewHolder item = mPendingAdditions.get(i);
            View view = item.itemView;
            ViewCompat.setAlpha(view, 1);
            dispatchAddFinished(item);
            mPendingAdditions.remove(i);
        }
        count = mPendingChanges.size();
        for (int i = count - 1; i >= 0; i--) {
            endChangeAnimationIfNecessary(mPendingChanges.get(i));
        }
        mPendingChanges.clear();
        if (!isRunning()) {
            return;
        }

      
        listCount = mAdditionsList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<ViewHolder> additions = mAdditionsList.get(i);
            count = additions.size();
            for (int j = count - 1; j >= 0; j--) {
                ViewHolder item = additions.get(j);
                View view = item.itemView;
                ViewCompat.setAlpha(view, 1);
                dispatchAddFinished(item);
                additions.remove(j);
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions);
                }
            }
        }
        
       ...

        cancelAll(mRemoveAnimations);
        cancelAll(mMoveAnimations);
        cancelAll(mAddAnimations);
        cancelAll(mChangeAnimations);

        dispatchAnimationsFinished();
    }

    void cancelAll(List<ViewHolder> viewHolders) {
        for (int i = viewHolders.size() - 1; i >= 0; i--) {
            ViewCompat.animate(viewHolders.get(i).itemView).cancel();
        }
    }

~~~


最基本的也是最简单的Add动画讲完了，那下面我们来看看比较难的Move动画，Move动画是指当一个Itme被删除或者添加的时候，其他Item动画需要执行对应的动画，比如删除后就向上补齐，添加前就向下退位
~~~java
 @Override
    public boolean animateMove(final ViewHolder holder, int fromX, int fromY,
            int toX, int toY) {
        final View view = holder.itemView;
        fromX += ViewCompat.getTranslationX(holder.itemView);
        fromY += ViewCompat.getTranslationY(holder.itemView);
        resetAnimation(holder);
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        //没移动就直接返回
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder);
            return false;
        }
        
        //设置
        if (deltaX != 0) {
            ViewCompat.setTranslationX(view, -deltaX);
        }
        if (deltaY != 0) {
            ViewCompat.setTranslationY(view, -deltaY);
        }
        mPendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY));
        return true;
    }
~~~