# 闭包的概念

闭包就是一个封装的意思，我在知乎上看到个很有意思的图片

![image](https://pic2.zhimg.com/80/20654942cb00c88356695f7d9bc8009b_hd.jpg)



java无处不存在闭包，比如我们的类

~~~java
class A{
  int b=100;
  public int add (){
    return ++b;
  }
}
~~~

这个就很熟悉了，add函数依赖了外部的一个自由变量b，而且处于含有外部变量环境中，还可以随便调用外部的局部变量b，那么class A不就构成了一个闭包吗？就是封装的意思嘛。

但是java中还有另一种有意思的闭包

~~~java
interface AnnoInner{
   change();
}
public class Outer { 
    
    final People p=new People("张三","男"); 
    
    return new AnnoInner(){ 
   
      public void change(){
      	//这个不会报错
        p.name="李四";
      } 
      
    }; 
}
~~~

是的，匿名内部类。这里我们就遇到一个有意思的地方了，就是final！我们在平时开发，AndroidStudio会给我们提示的，所以很多人就不太在乎这个问题。

# java的final

我们final相信大家都不陌生了，一些简单的使用就不去讲了。其中一个有意思的就是匿名内部类中如果使用外部局部变量，外部局部变量就必须定义为final。这是为什么呢？嗯，，，这个问题我是有仔细的想过，是为了防止匿名内部类中外部局部变量的引用被更改。导致内外部变量的数据不同步的问题。

其实我们是可以接受某个局部变量（假设是一个对象）里面字段的值被改，但是我们不期望这个局部变量的引用被在匿名内部类中被改动。也就是说，我希望匿名内部类是：capture-by-refrence。这样的话，我们的到的结果是外部类和匿名内部类的People是同步的！！我们看代码

~~~java
interface AnnoInner{
   change();
}
public class Outer { 
    
    final People p=new People("张三","男"); 
    
    return new AnnoInner(){ 
   
      public void change(){
      	//这个不会报错
        p.name="李四";
      } 
      
    }; 
}
~~~

是吧，匿名内部类对于外部的People感觉就是capture-by-refrence的！在匿名内部类中的p对象的字段改了，那外部类中的p对象的字段也改了，就是同步的嘛！

我们把上面的代码中的People p对象的引用看成：指针（所以下面用long来代替），那么这是我们写的代码

~~~java
interface AnnoInner{
   change();
}
public class Outer { 
    
    final long p=124324223l; 
    
    return new AnnoInner(){ 
   
      public void change(){
      	//这个不会报错
        p.name="李四";
      } 
      
    }; 
}
~~~

编译器处理的后的代码是这样的

~~~java
interface AnnoInner{
   change();
}
public class Outer { 
    
    final long p=124324223l; 
    
    return new AnnoInner(){ 
   
      public void change(){
        long copyP=p;
        
      	//这个不会报错
        copyP.name="李四";
      } 
      
    }; 
}
~~~

也就是说，对于对象的引用来说，其实就是capture-by-value，如果我们不给对象声明为final，显然内部类的p和外部的p指针就可能是不同步的。所以你可以看到为了同步，java直接搞个final不让你在内部类里面改p的值，这样就同步了！！是不是很骚？ 所以从对象的角度来看，java的匿名内部类和外部的局部变量看起来是capture-by-refrence，事实上实现还是capture-by-value的。



# 小结

java是不是很鬼？？立个flag是时候学习一波Lambda的使用和原理了。

