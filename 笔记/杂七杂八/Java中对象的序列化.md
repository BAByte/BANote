[TOC]

# Java中对象的序列化

## 先说两句

​	这其实是非常容易的知识点，但是还是忘记了，在看快快送项目时又遇到了，百度了一下，就写写笔记吧

## 为什么要序列化？

​	懒得和你解释了，快往下看，来不及了

## 将对象序列化成二进制然后保存到文件中

+ 先写一个实体类，用来等下序列化，这里有一个小知识点了：Serializable接口，在java中只要实现了这个接口就能被序列化，否则不行！！！而且还不需要加任何的方法

~~~java
public class Student implements Serializable{
	
	int age;
	
	String name;
	
	public Student(String name,int age) {
		this.name=name;
		this.age=age;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name+"::"+age;
	}
}
~~~

---

+ 写出序列化和反序列化的两个方法

~~~java
public class SerializableTool {
  
  //这里的例子是序列化到文件中，所以设置下路径
	File file=new File("C://Users//BA//Documents//file//student.text");
	
  //序列化
	public void begin() {
      //等下被序列化的对象
	Student student=new Student("老夫子",100);
		
      //之前有文件就删除
		if(file.isFile())
			file.delete();
		
      //这个流就是用来将对象序列化成二进制的
		ObjectOutputStream oos=null;
		try {
          //参数也是一个流，这个参数是指把这些二进制放到哪里，可以是字节数组流，也可以是文件中
			oos=new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(student);//开始写入
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(oos!=null) {
				try {
					oos.close();
					System.out.println("序列化输出完毕");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
  //反序列化
	public void rever() {
		ObjectInputStream ois=null;
		try {
          //参数是将要反序列化的对象的数据来源
			ois=new ObjectInputStream(new FileInputStream(file));
			Student s=(Student)ois.readObject();
			System.out.println("反序列化："+s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(ois!=null)
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
  
}
~~~

---

结果：

序列化输出完毕
反序列化：老夫子::100

---

## 序列化进阶

###序列化机制

> ​    如果仅仅只是让某个类实现Serializable接口，而没有其它任何处理的话，则就是使用默认序列化机制。使用默认机制，在序列化对象时，不仅会序列化当前对象本身，还会对该对象引用的其它对象也进行序列化，同样地，这些其它对象引用的另外对象也将被序列化，以此类推。所以，如果一个对象包含的成员变量是容器类对象，而这些容器所含有的元素也是容器类对象，那么这个序列化的过程就会较复杂，开销也较大。

### 忽略对象中的某些字段进行序列化

> 当某个字段被声明为transient后，默认序列化机制就会忽略该字段。此处将Student类中的age字段声明为transient,举个例子

~~~java
public class Student implements Serializable{
	
	transient int age; //忽略这个属性
	
	String name;
	
	public Student(String name,int age) {
		this.name=name;
		this.age=age;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name+"::"+age;
	}
}
~~~

---

运行结果

序列化输出完毕
反序列化：老夫子::0

---

### 动态决定对象中的字段是否被序列化

在上面的age字段设置了transient属性后，就是声明，在序列化student对象时不需要序列化这个字段，一般有几种情况

+ 这个字段的信息无关紧要
+ 这个字段信息非常重要，序列化后如果需要持久化可能会泄露
+ 这个字段是一个不能被序列化的对象

这里我们说说最后一种情况：在上面的Student里面age是int型的，当然能被序列化，如果是一个不能被序列化的A对象，这个对象的实体类你又不能改，那你就会声明这个字段为transient属性，如果这个对象符合前两种情况，那就美滋滋，声明transient属性后就不用管了。

但是！！！如果这个A对象你必须要序列化后进行持久化，那怎么办？？简单啊，A对象不能序列化，我就序列化A对象里面的字段咯。看看优雅的写法吧

+ 首先要写一个不能被序列化的类

~~~java
public class Teacher { //没有实现序列化接口，所以不能被序列化
	
	 int age;
	
	String name;
	
	public Teacher(String name,int age) {
		this.name=name;
		this.age=age;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "姓名"+name+"::"+age;
	}

}
~~~

---

+ 改改Student类

~~~java
public class Student implements Serializable{
	
	int age;
	
	String name;
	
  //可以看到这里给这个字段设置了transient属性。因为他不能被序列化
  //要是不声明会出异常的
	transient Teacher t;
	
	public Student(String name,int age,Teacher t) {
		this.name=name;
		this.age=age;
		this.t=t;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "学生姓名："+name+"::"+age+"学生的老师："+t;
	}
}

~~~

---

看上面，如果你直接运行，是不会序列化teacher这个字段的，接下来就要去实现，让他进行序列化，给student类加上这两个方法，就可以很巧妙的实现了teacher的序列化。其实就是将teacher的一些字段序列化，然后仔反序列化的时候将这些字段读出来，new一个teacher对象

~~~java
	private void writeObject(ObjectOutputStream out) throws Exception {
		// TODO Auto-generated method stub
		out.writeObject(t.name);
		out.writeInt(t.age);
	}
	
	private void readObject(ObjectInputStream in)throws Exception {
		// TODO Auto-generated method stub
		t=new Teacher((String)in.readObject(), in.readInt());
	}
~~~

---

你会注意到上面两个方法都是私有的，这意味着这两个方法不是给我们调用的

+ 在序列化的时候，会先序列化student对象中所有可以序列化的字段，然后再自动调用writeObject方法
+ 在反序列化的时候，会先反序列化student中所有可以序列化的字段，然后自动调用readObject方法
+ 需要注意的是，两个方法中的数据顺序要对应！！！先写什么就会先读到什么

---

### 序列化时加密敏感数据

上面写的两个方法writeObject和readObject就是用来给你自己决定某一个字段的序列化方式，你其实可以在里面对你的一些敏感信息进行一些处理，比如加一，反转，什么的，然后再序列化，取出来的时候自己处理一下就好了

## 总结

> 总体不难，还有需要注意的点是，反序列Student对象的时候，是不走Stuent的构造函数的，所以你在另一个没有这个Student类的项目里面反序列化这个Student对象时候，会抛异常的。