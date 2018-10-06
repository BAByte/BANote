[TOC]

# 安卓源码中的Builder模式

这个其实不难，我们平时使用Builder模式的时候会感觉比较繁琐，为什么这样说呢？假设我们有一个实例他有非常多的属性，对吧？在学习Builder的时候，我们习惯写成内部类，而且会把具体类的属性在Builer里面再写一次，这非常的麻烦，又或者是先实例化，然后在build()方法里面进行返回，安卓是如何实现一个有着良好结构的Builder的呢？

## 从AlertDialog说起

先看使用

~~~java
    AlertDialog dialog=new AlertDialog.Builder(this)
                .setIcon()
                .setTitle()
                .setMessage()
                .setCancelable()
                .setItems()
                .setView()
                .setNeutralButton()
                .setSingleChoiceItems()
                .setMultiChoiceItems()
                .show();
~~~

这样写还挺好看的，哈哈哈，没错！就是为了更好看，看看源码吧

~~~java
  public static class Builder {
        private final AlertController.AlertParams P;

        public Builder(Context context) {
            this(context, resolveDialogTheme(context, ResourceId.ID_NULL));
        }

     
        public Builder(Context context, int themeResId) {
            P = new AlertController.AlertParams(new ContextThemeWrapper(
                    context, resolveDialogTheme(context, themeResId)));
        }
      
     
        public Builder setRecycleOnMeasureEnabled(boolean enabled) {
            P.mRecycleOnMeasure = enabled;
            return this;
        }
      ...
          
         public AlertDialog create() {
            // Context has already been wrapped with the appropriate theme.
            final AlertDialog dialog = new AlertDialog(P.mContext, 0, false);
            P.apply(dialog.mAlert);
            dialog.setCancelable(P.mCancelable);
            if (P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.setOnCancelListener(P.mOnCancelListener);
            dialog.setOnDismissListener(P.mOnDismissListener);
            if (P.mOnKeyListener != null) {
                dialog.setOnKeyListener(P.mOnKeyListener);
            }
            return dialog;
        }
  }
~~~

很容易看出，谷歌工程师把属性给提取出来管理，这样一来Buider里面就不用放一堆烦人的属性了，直接操作AlertController.AlertParams对象就好，但是为什么要在这里设置监听？那看看AletDialog是怎样去使用AlertController.AlertParams的，有个apply方法，我们看看apply()方法干了什么事情

~~~java
public void apply(AlertController dialog){
    if(mTile!=null){
        dialog.setTitle(mTitle);
    }
    ...
}
~~~

这就好理解了，这些属性是交给了AlertController.AlertParams去为AlertDialog设置。那这个AlertController又是什么？那就要看看AlertDialog的代码了

~~~java
public class AlertDialog extends Dialog implements DialogInterface {
    private AlertController mAlert;
    
     AlertDialog(Context context, @StyleRes int themeResId, boolean createContextThemeWrapper) {
        super(context, createContextThemeWrapper ? resolveDialogTheme(context, themeResId) : 0,
                createContextThemeWrapper);

        mWindow.alwaysReadCloseOnTouchAttr();
        mAlert = AlertController.create(getContext(), this, getWindow());
    }
    
        @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mAlert.setTitle(title);
    }
}
~~~

可以看出AlertDialog是复责显示视图的属性。还是没有看出为什么要在外部设置监听，那先不管。属性设置好了，就要show出来了吧？AlertDialog并没有show方法，说明是父类Dialog有show方法。

~~~java
  public void show() {
        if (mShowing) {
            if (mDecor != null) {
                if (mWindow.hasFeature(Window.FEATURE_ACTION_BAR)) {
                    mWindow.invalidatePanelMenu(Window.FEATURE_ACTION_BAR);
                }
                mDecor.setVisibility(View.VISIBLE);
            }
            return;
        }

        mCanceled = false;

        if (!mCreated) {
            dispatchOnCreate(null);
        } else {
            final Configuration config = mContext.getResources().getConfiguration();
            mWindow.getDecorView().dispatchConfigurationChanged(config);
        }

        onStart();
        mDecor = mWindow.getDecorView();

        if (mActionBar == null && mWindow.hasFeature(Window.FEATURE_ACTION_BAR)) {
            final ApplicationInfo info = mContext.getApplicationInfo();
            mWindow.setDefaultIcon(info.icon);
            mWindow.setDefaultLogo(info.logo);
            mActionBar = new WindowDecorActionBar(this);
        }

        WindowManager.LayoutParams l = mWindow.getAttributes();
        if ((l.softInputMode
                & WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION) == 0) {
            WindowManager.LayoutParams nl = new WindowManager.LayoutParams();
            nl.copyFrom(l);
            nl.softInputMode |=
                    WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION;
            l = nl;
        }

        mWindowManager.addView(mDecor, l);
        mShowing = true;

        sendShowMessage();
    }
~~~

这里是用WindowManager去显示和绘制View了。那我们肯定要找sertContentView方法了（具体可以看看我关于Window和WindowManager的笔记），我们看到一个onStart()方法，就是说调用AlertDialog的一系列生命周期的方法了，由于我们目前还是没有找到Window的setContentView()方法，从生命周期方法下手看看，我们看看Dialog的dispatchOnCreate方法

~~~java
    // users to call through to super in onCreate
    void dispatchOnCreate(Bundle savedInstanceState) {
        if (!mCreated) {
            onCreate(savedInstanceState);
            mCreated = true;
        }
    }
~~~

继续

~~~java
protected void onCreate(Bundle savedInstanceState) {
    }
~~~

没有看错，是一个空方法，既然父类是空方法，那就看看子类AlertDialog的onCreate方法

~~~java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlert.installContent();
    }
~~~

看到了吗？   mAlert.installContent();！！！说明这个方法里面就是去Window里面setContentView了。

~~~java
public void installContent(){
    ...
        mWindow.setContentView(mAlterDialogLayout)
        ...
}
~~~

好了，我们小结一下，从Builder里面看到，某些属性使用的是AlertController.AlertParams对象存储和设置，但是比如监听，图标等等，却和AlertController.AlertParams无关。而AlertController确只是展示了一部分View，AlertDialog里面居然有生命周期，这是不是很熟悉？这不就是一个小的Activity？AlertController负责展示ContentView，AlertDialog负责管理一些其他的东西，那其他的东西很自然就可能是标题了，但是我们看到上面Builder的creat方法他是直接设置了确认和取消按钮的监听，这说明确认和取消也是由AlertDialog管理的，那其实就是下面这一幅图

![image](http://www.2cto.com/uploadfile/Collfiles/20140425/2014042508554699.jpg)

明显的三段式的结构：标题，内容，按钮

## 总结

那和我们的Builder有什么关系呢？有！你没发现上面的Buidler很灵活吗？这个Builder不仅仅是构建了AlertDialog，也构建了AlertDialog内部的AlertController，但是Builder却不会很臃肿。其实关键就是AlertController.AlertParams，有了他，一来属性管理方便了，二来Builder简洁了。

还有一个非常重要的点，如果我们不分析源码，也没人告诉你我们平时用的Dialog是三段式结构,分别由不同的东西管理，而且就是个小Activity，你会想到吗?你就只会用吧？这就说明了Builder很好的把构建对象的过程给隐藏了，用户只需要关注如何使用公式化的代码去构建出我们想要的东西就好了，这就是抽象的力量，把内部很复杂的东西，通过抽象。变成用户易于理解的东西。

假设不用Builder，我们大概会这样去使用

~~~java
AlertDialog a=new AlertDialog；
a.setTiltle();
a.setIcon();

AlertController.AlertParams p=AlertController.AlertParams();
p.set(xxx);
p.set(xxx);
p.set(xxx);
p.set(xxx);
p.set(xxx);
p.set(xxx);

a.setContentView(new AlertController(p));

a.setOKButton(new OnClickOnListener{
    onClick(){
        
    }
});

a.setCancleButton(new OnClickOnListener{
    onClick(){
        
    }
});
~~~

麻烦吗?这样用起来也太复杂了吧？还不好理解，设置标题和图标为什么不是AlertController.AlertParams？用户就觉得很奇怪，对吧？嗯，就到这里。