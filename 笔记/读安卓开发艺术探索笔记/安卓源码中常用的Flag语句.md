[toc]
# 安卓源码中常用的Flag语句
## 先说两句
这个标志位就很有意思了，什么时候需要标志位？最常用的就是对于一个状态的判断，比如开关的判断，我们一般怎么写？
~~~java
int on=0;
int off=1;
int status;


void setStatus(int status){
    this.status=status;
}

int setStatus(){
    return status;
}
~~~

就像上面一样，直接get和set方法来设置和获取状态，这算是一种标志吧？

那还有什么标志呢？
## 常见的标志例子
比如大学生，我要求他有三种状态的各种组合，上课，睡觉，玩手机。就是说这个大学生可能是在上课时睡觉，也可能只是睡觉，等等这些组合，然后根据这些组合去做不同的处理，那我们平时是怎么写的？
~~~java

//学生的实体类，其实就是一堆状态属性，包括get和set方法
class student{
boolean sleep;
boolean inClass;
boolean playPhone;

//get所有get方法省略
//set方法也省略
}

这个是处理类
class dealUtil(){
    void deal(Student s){
        if(xxx){
            xxx
        }
    }
}

~~~

看起来代码不多，因为我省略了一堆get和set方法，那有没有什么办法把这些getset方法省略？有的，我们用二进制的与或运算来设置
## 用与或运算处理flag
### 必备知识

在Android源码中,包括一些比较规范的源码中,通常会出现flag(我理解为标志位)。

可以这么认为:

    a&~b:  清除标志位b;
    a|b:     添加标志位b;
    a&b:    取出标志位b;
    a^b:    取出a与b的不同部分;
    
通过Intent Flags对应的值，可以将多种标志通过“或运算”来进行组合，以下代码是Intent添加标志，使用到“或（|）”运算：
~~~java
mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED  
                | Intent.FLAG_ACTIVITY_SINGLE_TOP  
                );  
                
//或者
event.mFlags |= FLAG_CANCELED | FLAG_CANCELED_LONG_PRESS;  
~~~

二、判断Intent Flags是否包含某个标志，通过“与运算”代码如下：

  1)  
~~~java
if ((intent.getFlags()&Intent.FLAG_ACTIVITY_NEW_TASK) == 0)｛  
         //条件为真（即等于0），intent.getFlags()不包含NEW_TASK  
    ...  
    ｝  
~~~
 
  2)    
~~~java
// 判断该视图是否为disable 状态 这里ENABLED_MASK的值与 DISABLED的值一样  
       if ((viewFlags & ENABLED_MASK) == DISABLED) {  
    ...  
       ｝  
~~~
  3)  
~~~java
// 返回是否可点击  
     return (((viewFlags & CLICKABLE) == CLICKABLE ||  
                    (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE));  
~~~
三、清除某个值
 
~~~java
mFlags &= ~FLAG_START_TRACKING;     // 清除mFlags中的FLAG_START_TRACKING  
~~~

四、取出新旧标记的不同部分

~~~java
 
void setFlags(int flags, int mask) {  
       int old = mViewFlags;//将标记赋值给old  
       mViewFlags = (mViewFlags & ~mask) | (flags & mask);//mViewFlags清除mask后添加从flags中取出的mask标志  
  
       int changed = mViewFlags ^ old;//取出新旧标记的不同部分。  
       if (changed == 0) {  
           return;  
       }  

 ~~~
例子：
在源码View.java中：
~~~java
……  
 private static final int PRESSED                = 0x00004000;  
 int mPrivateFlags ;  
……  
  
  
     public void setPressed(boolean pressed) {  
        if (pressed) {  
            mPrivateFlags |= PRESSED;     // 添加PRESSED状态  
        } else {  
            mPrivateFlags &= ~PRESSED;    // 取消PRESSED状态  
        }  
        refreshDrawableState();  
        dispatchSetPressed(pressed);  
    }  
~~~
## 实战
现在你其实可以自己写上面那个关于学生状态的代码了，好处是什么呢？就是可以用一个整型记录很多种状态，这样就不用一堆get和set方法了
~~~java
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    //我需要的标志
    public static final int flag1=1;
    public static final int flage2=2;
    public static final int flage3=3;
    public static final int flage4=4;
    public static final int flage5=5;
    public static final int flage6=6;
    public static final int flage7=64234423;
    
    //用这个整型值来记录所有标志
    public int myFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //添加一些状态标志
        myFlag|=flag1;
        myFlag|=flage2;
        myFlag|=flag1;
        myFlag|=flage7;


        //判断有没添加成功
        if((myFlag&flag1)!=0){
            Log.d(TAG, "onCreate: 有flag1");
        }


        if ((myFlag&flage2)!=0){
            Log.d(TAG, "onCreate: 有flag2");
        }

        if ((myFlag&flage4)!=0){
            Log.d(TAG, "onCreate: 有flag4");
        }

        if ((myFlag&flage7)!=0){
            Log.d(TAG, "onCreate: 有flag7");
        }

        //去掉状态标志
        myFlag&=~flag1;

        if((myFlag&flag1)!=0){
            Log.d(TAG, "onCreate: 有flag1");
        }

        Log.d(TAG, "onCreate: myFlag="+myFlag);

    }
}
~~~

## 总结
有什么用？代码更加简洁，二进制运算，性能更加好，还很装逼！！！