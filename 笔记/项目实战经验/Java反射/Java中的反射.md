[toc]
# Java反射
## 先说两句
以前学反射的时候就知道反射是java的精髓，但是我当时不以为然，觉得以后用到在学，终于用到了,参考文章：http://blog.csdn.net/hp910315/article/details/50527507
## 主要的类
+ java.lang.Class 
+ java.lang.reflect.Constructor 
+ java.lang.reflect.Field 
+ java.lang.reflect.Method 
+ java.lang.reflect.Modifier

作用： 
1. 反编译 .class –> .java 
2. 通过反射机制访问java对象的属性、方法、构造方法等。这个在android中经常用到。

## 具体操作
反射的第一步，就是获取实例的Class对象，然后通过这个对象就可以进行一系列的强大操作！！！！！看看怎么获取这个Class对象吧
### Class.forName(className) 
它就是将className加载到JVM的过程，得到堆中加载的class字节码的引用，类中的静态块会被执行，className必须为全名：包名+类名
![image](http://img.blog.csdn.net/20160116131213646)
---
### 类名.class 
java每个类型都有class属性，类中的静态块不会被执行

---
### 实例对象.getClass() 
得到实例对象的运行时类，类中的静态块会被执行
### 获取到后api就在该笔记的文件夹内
### 代码使用示例
~~~java
public class MainClass {

	public static void main(String[] args) {
		MyBean bean = new MyBean("张哈哈", 19, 50.1f);

		// 获取Class对象
		Class<? extends MyBean> clazz = bean.getClass();

		// 获取字段
		getField(clazz, bean);
		
		//获取所有方法
		getMethod(clazz, bean);

	}
	
	public static void getMethod(Class<? extends MyBean> clazz, MyBean bean) {
		System.out.println("获取所有方法");
		//获取所有方法
		Method[] methods=clazz.getDeclaredMethods();
		for(Method method:methods) {
			System.out.println(method.getName());
		}
		
		System.out.println("调用公有方法来设置值");
		
		try {
			//指定名字和参数类型获取方法
			Method methodSetPrivate=clazz.getMethod("setWeight", float.class);
			//通过动态代理执行具体bean这个具体实现类中的方法，第二个是参数
			methodSetPrivate.invoke(bean, 80f);
			
			System.out.println("调用私有方法获取值");
			Method methodGetPrivate=clazz.getDeclaredMethod("getWeight");
			methodGetPrivate.setAccessible(true); //当获取的是私有方法时，需要设置这个，不然无法调用
			System.out.println(methodGetPrivate.invoke(bean));
			
			
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getField(Class<? extends MyBean> clazz, MyBean bean) {
		// 获取所有字段，包括私有
		Field[] fieldsPrivate = clazz.getDeclaredFields();
		System.out.println("下面是所有字段");
		for (Field f : fieldsPrivate) {
			f.setAccessible(true);
			try {
				System.out.println(f.getName() + "===" + f.get(bean)); // 获取字段的值，要是字段是静态的话，可以传null
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// 获取共有字段
		Field[] fildsPublic = clazz.getFields();
		System.out.println("下面是所有共有字段");
		for (Field f : fildsPublic) {
			f.setAccessible(true);//当获取的是私有字段时，需要设置这个，不然无法调用
			try {
				System.out.println(f.getName() + "===" + f.get(bean)); // 获取字段的值
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// 获取单一字段
		System.out.println("下面是获取私有单一字段");
		try {
			Field f = clazz.getDeclaredField("weight");
			f.setAccessible(true);
			System.out.println(f.getName() + "==" + f.get(bean));

			f.set(bean, 60f);
			System.out.println("设置值后");
			System.out.println(f.getName() + "==" + f.get(bean));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

~~~
---
## 总结
拿到的是Class对象，但是需要知道的是，这个Class对象和那个实例是不一样的概念，这个Class对象只是用来调用反射的方法而已，如果你的Class是通过一个实例获得的，那你这个Class对象是和这个实例绑定的，比如说这个实例里面有个字段在运行时被赋值1，你通过这个Class对象是可以拿到这个运行时动态变化的值；

最强大的是，可以直接调用一个类里面的私用方法，还能获取方法中的参数