[toc]
# Activity切换动画
参考：http://blog.csdn.net/zhuhai__yizhi/article/details/44491757
## 先说两句
这个东西我想写很久了，无奈一直拖着，每个View都可以实现动画，Activity当然也可以，其实Activity动画在安卓2.0就支持了，但是在安卓5.0后开始出现了共享内容的动画，这个真的很炫酷，但是这篇文章讲的不是这个内容。
## 切入正题
你有两个Activity：A,B。你从A进入B时，你希望A向上滑出，B从底部向上滑入。要是View的话，确实简单，但是Activity怎么实现？其实更简单了。在此之前你需要有去了解安卓动画，以及用xml文件编写简单的动画，这里我就不写这些东西。
## 开启动画的方法
Activity中有一个函数
~~~java
overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);

//参数1：R.anim.push_bottom_in：进入的Activity的动画
//参数2：R.anim.push_bottom_out：离开的Activity的动画

//注意：两个参数的动画文件需要你自己去写，放在：res/anim/xx.xml
~~~

## 如何使用该方法
~~~java
//A对应的动画是(R.anim.push_bottom_in)
//B对应的动画是(R.anim.push_bottom_out)
Intent intent = new Intent(getActivity(),  SigninActivity.class);  
startActivity(intent);   //一定要在这个方法后面才会有用
overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out); 



//如果你只有一个Activity，你想给它一个退出动画，那就必在这个方法后面使用 
finish(); 
overridePendingTransition(0,R.anim.push_bottom_out);
~~~

## 注意
内部类中无法使用方法，匿名内部类也不行，EventBus的处理函数里面使用也不行，这个时候可以用Handler来处理