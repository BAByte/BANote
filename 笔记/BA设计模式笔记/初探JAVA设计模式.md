[TOC]

# 初探JAVA设计模式

---

## 设计模式遵守的六大原则

​	首先先说明，这六大原则不是什么真理什么的，只是一个准则，你遵守了这个准则就会解决一些很普遍的问题，但是有时候又不得不违反六大原则，所以具体应用还是要看情况！！！！

### 单一职责原则

+ 网上的定义:不要存在多于一个导致类变更的原因。通俗的说，即一个类只负责一项职责，（或者一个方法只负责一项功能）。是不是通俗易懂！！！！~~例子也很简单，就那我写的计算器来说，我将加减乘除功能全部分开成4个类，为什么？一个类放那么多功能，我看着都累，，，而且当你在调试啊，更改啊，也会特别麻烦，有时候减法的功能模块出了问题，然后你想要改改，一不小心改到了除法。。。心态爆炸！！~~
+ 任何时候都要遵守吗？当然不是，去看看这个文章，地址  http://www.uml.org.cn/sjms/201211023.asp#1
+ 里面说，T负责两个功能A,B，按照单一原则，应该将T分成T1，T2，分别负责A,B功能。但是如果实现A，B功能的方法非常的简单，T里面方法函数比较少的时候，其实分开反而花销更大
+ 这篇文章的最重要的一个点：只有逻辑足够简单，才可以在方法级别上违反单一职责原则；只有类中方法数量足够少，才可以在类级别上违反单一职责原则

---

### 里氏替换原则

​	按照惯例先看名字，里氏：指提出这个原则的是一个MIT女学生姓里。这个原则针对是使用继承时存在的问题，替换，这两个字才是重点：任何父类实例出现的地方都能被其子类所替代。以前我是非常不理解这句话的，今天认真想想才知道。

+ 最容易理解错的就是，父类出现的地方，可以直接用子类替换。
~~~java
//父类
public abstract class View{
    public void draw();
}

//子类
public class TextView extends View{
    public void draw();
}

//子类
public class ImageView extends View{
    public void draw();
}

//调用类
public class Window{
    //父类出现的地方，可以用子类替换，是虽然不影响程序的正常运行，但是功能会不一样
    public void showView(View view){
        
    }
}
~~~
+ 复写父类方法是有规则的：复写抽象方法，在不破坏原来的方法功能时可以复写非抽象方法，这个所谓的功能其实很抽象的，说白了就是父类使用这个方法地方，用其子类代替也是不会影响程序的运行，比如toString方法，但是效果可能不一样
+ 问题由来：有一功能P1，由类A完成。现需要将功能P1进行扩展，扩展后的功能为P，其中P由原有功能P1与新功能P2组成。新功能P由类A的子类B来完成，则子类B在完成新功能P2的同时，有可能会导致原有功能P1发生故障。
+ 解决方案：当使用继承时，遵循里氏替换原则。类B继承类A时，除添加新的方法完成新增功能P2外，尽量不要重写父类A的方法，也尽量不要重载父类A的方法。

就是防止因一发而动全身，所以为了符合这原则，好像最常见的就是把最常用的方法抽象

---

### 依赖倒置原则

我就直接复制原文了：

定义：高层模块不应该依赖低层模块，二者都应该依赖其抽象；抽象不应该依赖细节；细节应该依赖抽象。

问题由来：类A直接依赖类B，假如要将类A改为依赖类C，则必须通过修改类A的代码来达成。这种场景下，类A一般是高层模块，负责复杂的业务逻辑；类B和类C是低层模块，负责基本的原子操作；假如修改类A，会给程序带来不必要的风险。

解决方案：将类A修改为依赖接口I，类B和类C各自实现接口I，类A通过接口I间接与类B或者类C发生联系，则会大大降低修改类A的几率。

依赖倒置原则基于这样一个事实：相对于细节的多变性，抽象的东西要稳定的多。以抽象为基础搭建起来的架构比以细节为基础搭建起来的架构要稳定的多。在java中，抽象指的是接口或者抽象类，细节就是具体的实现类，使用接口或者抽象类的目的是制定好规范和契约，而不去涉及任何具体的操作，把展现细节的任务交给他们的实现类去完成。

依赖倒置原则的核心思想是面向接口编程，我们依旧用一个例子来说明面向接口编程比相对于面向实现编程好在什么地方。场景是这样的，母亲给孩子讲故事，只要给她一本书，她就可以照着书给孩子讲故事了。代码如下：

~~~java
class Book{
	public String getContent(){
		return "很久很久以前有一个阿拉伯的故事……";
	}
}

class Mother{
	public void narrate(Book book){
		System.out.println("妈妈开始讲故事");
		System.out.println(book.getContent());
	}
}

public class Client{
	public static void main(String[] args){
		Mother mother = new Mother();
		mother.narrate(new Book());
	}
}
~~~



运行结果：

妈妈开始讲故事

很久很久以前有一个阿拉伯的故事……

运行良好，假如有一天，需求变成这样：不是给书而是给一份报纸，让这位母亲讲一下报纸上的故事，报纸的代码如下：

~~~java
class Newspaper{
	public String getContent(){
		return "林书豪38+7领导尼克斯击败湖人……";
	}
}
~~~



这位母亲却办不到，因为她居然不会读报纸上的故事，这太荒唐了，只是将书换成报纸，居然必须要修改Mother才能读。假如以后需求换成杂志呢？换成网页呢？还要不断地修改Mother，这显然不是好的设计。原因就是Mother与Book之间的耦合性太高了，必须降低他们之间的耦合度才行。

我们引入一个抽象的接口IReader。读物，只要是带字的都属于读物：

~~~java
interface IReader{
	public String getContent();
} 
~~~



Mother类与接口IReader发生依赖关系，而Book和Newspaper都属于读物的范畴，他们各自都去实现IReader接口，这样就符合依赖倒置原则了，代码修改为：

~~~java
class Newspaper implements IReader {
	public String getContent(){
		return "林书豪17+9助尼克斯击败老鹰……";
	}
}
class Book implements IReader{
	public String getContent(){
		return "很久很久以前有一个阿拉伯的故事……";
	}
}

class Mother{
	public void narrate(IReader reader){
		System.out.println("妈妈开始讲故事");
		System.out.println(reader.getContent());
	}
}

public class Client{
	public static void main(String[] args){
		Mother mother = new Mother();
		mother.narrate(new Book());
		mother.narrate(new Newspaper());
	}
}
~~~



运行结果：

妈妈开始讲故事

很久很久以前有一个阿拉伯的故事……

妈妈开始讲故事

林书豪17+9助尼克斯击败老鹰……

这样修改后，无论以后怎样扩展Client类，都不需要再修改Mother类了。这只是一个简单的例子，实际情况中，代表高层模块的Mother类将负责完成主要的业务逻辑，一旦需要对它进行修改，引入错误的风险极大。所以遵循依赖倒置原则可以降低类之间的耦合性，提高系统的稳定性，降低修改程序造成的风险。

采用依赖倒置原则给多人并行开发带来了极大的便利，比如上例中，原本Mother类与Book类直接耦合时，Mother类必须等Book类编码完成后才可以进行编码，因为Mother类依赖于Book类。修改后的程序则可以同时开工，互不影响，因为Mother与Book类一点关系也没有。参与协作开发的人越多、项目越庞大，采用依赖导致原则的意义就越重大。现在很流行的TDD开发模式就是依赖倒置原则最成功的应用。

传递依赖关系有三种方式，以上的例子中使用的方法是接口传递，另外还有两种传递方式：构造方法传递(将接口或者抽象类作为构造函数的参数)和setter方法传递(就是作为set方法的参数)

在实际编程中，我们一般需要做到如下3点：

- 低层模块尽量都要有抽象类或接口，或者两者都有。
- 变量的声明类型尽量是抽象类或接口。
- 使用继承时遵循里氏替换原则。

依赖倒置原则的核心就是要我们面向接口编程，理解了面向接口编程，也就理解了依赖倒置。

看了这个接口后，我终于明白为什么使用安卓官方的一些方法要new这么多对象，然后再传入了

### 先来个小结
有没有觉得，里氏原则和依赖倒置原则很像？是的都是抽象出公用的东西，但是抽象在Java中是分为抽象类和接口，显然里氏替换是针对继承，也就是抽象类，但是依赖导致原则针对的是耦合问题，因为依赖具体实现，耦合性太高了，所以需要抽象接口来解耦，显然依赖倒置原则是抽象出接口

---

### 接口隔离原则

​	以前我有遇到一个问题，我建立一个接口，用来为接下来的实现了打好框架，但是我居然想写了这样的一个接口，接口里面大部分抽象方法A类和B类都要用，但是写出一两个A类不需要但是B类需要的抽象方法，这样带来的问题就是，B类根本不需要这个方法，却一定要复写，这问题就很大了，那搭建这个框架还有什么意义？这就违背了接口隔离原则。

解决方法就是，把B类不需要的方法，从接口中细化出来，再写一个接口给A这样的实现类。

采用接口隔离原则对接口进行约束时，要注意以下几点：

- 接口尽量小，但是要有限度。对接口进行细化可以提高程序设计灵活性是不挣的事实，但是如果过小，则会造成接口数量过多，使设计复杂化。所以一定要适度。
- 为依赖接口的类定制服务，只暴露给调用的类它需要的方法，它不需要的方法则隐藏起来。只有专注地为一个模块提供定制服务，才能建立最小的依赖关系。
- 提高内聚，减少对外交互。使接口用最少的方法去完成最多的事情。

运用接口隔离原则，一定要适度，接口设计的过大或过小都不好。设计接口的时候，只有多花些时间去思考和筹划，才能准确地实践这一原则。

---



###迪米特法则（最少知道原则）

以下为原文：写的实在是太好了

定义：一个对象应该对其他对象保持最少的了解。

问题由来：类与类之间的关系越密切，耦合度越大，当一个类发生改变时，对另一个类的影响也越大。

解决方案：尽量降低类与类之间的耦合。

自从我们接触编程开始，就知道了软件编程的总的原则：低耦合，高内聚。无论是面向过程编程还是面向对象编程，只有使各个模块之间的耦合尽量的低，才能提高代码的复用率。低耦合的优点不言而喻，但是怎么样编程才能做到低耦合呢？那正是迪米特法则要去完成的。

迪米特法则又叫最少知道原则，最早是在1987年由美国Northeastern University的Ian Holland提出。通俗的来讲，就是一个类对自己依赖的类知道的越少越好。也就是说，对于被依赖的类来说，无论逻辑多么复杂，都尽量地的将逻辑封装在类的内部，对外除了提供的public方法，不对外泄漏任何信息。迪米特法则还有一个更简单的定义：只与直接的朋友通信。首先来解释一下什么是直接的朋友：每个对象都会与其他对象有耦合关系，只要两个对象之间有耦合关系，我们就说这两个对象之间是朋友关系。耦合的方式很多，依赖、关联、组合、聚合等。其中，我们称出现成员变量、方法参数、方法返回值中的类为直接的朋友，而出现在局部变量中的类则不是直接的朋友。也就是说，陌生的类最好不要作为局部变量的形式出现在类的内部。

举一个例子：有一个集团公司，下属单位有分公司和直属部门，现在要求打印出所有下属单位的员工ID。先来看一下违反迪米特法则的设计。

~~~java

 //总公司员工
class Employee{
	private String id;
	public void setId(String id){
		this.id = id;
	}
	public String getId(){
		return id;
	}
}

//分公司员工
class SubEmployee{
	private String id;
	public void setId(String id){
		this.id = id;
	}
	public String getId(){
		return id;
	}
}

class SubCompanyManager{
	public List<SubEmployee> getAllEmployee(){
		List<SubEmployee> list = new ArrayList<SubEmployee>();
		for(int i=0; i<100; i++){
			SubEmployee emp = new SubEmployee();
			//为分公司人员按顺序分配一个ID
			emp.setId("分公司"+i);
			list.add(emp);
		}
		return list;
	}
}

class CompanyManager{

	public List<Employee> getAllEmployee(){
		List<Employee> list = new ArrayList<Employee>();
		for(int i=0; i<30; i++){
			Employee emp = new Employee();
			//为总公司人员按顺序分配一个ID
			emp.setId("总公司"+i);
			list.add(emp);
		}
		return list;
	}
	
	public void printAllEmployee(SubCompanyManager sub){
		List<SubEmployee> list1 = sub.getAllEmployee();
		for(SubEmployee e:list1){
			System.out.println(e.getId());
		}

		List<Employee> list2 = this.getAllEmployee();
		for(Employee e:list2){
			System.out.println(e.getId());
		}
	}
}

public class Client{
	public static void main(String[] args){
		CompanyManager e = new CompanyManager();
		e.printAllEmployee(new SubCompanyManager());
	}
} 
~~~

现在这个设计的主要问题出在CompanyManager中，根据迪米特法则，只与直接的朋友发生通信，而SubEmployee类并不是CompanyManager类的直接朋友（以局部变量出现的耦合不属于直接朋友），从逻辑上讲总公司只与他的分公司耦合就行了，与分公司的员工并没有任何联系，这样设计显然是增加了不必要的耦合。按照迪米特法则，应该避免类中出现这样非直接朋友关系的耦合。修改后的代码如下:

~~~java
class SubCompanyManager{
	public List<SubEmployee> getAllEmployee(){
		List<SubEmployee> list = new ArrayList<SubEmployee>();
		for(int i=0; i<100; i++){
			SubEmployee emp = new SubEmployee();
			//为分公司人员按顺序分配一个ID
			emp.setId("分公司"+i);
			list.add(emp);
		}
		return list;
	}
	public void printEmployee(){
		List<SubEmployee> list = this.getAllEmployee();
		for(SubEmployee e:list){
			System.out.println(e.getId());
		}
	}
}

class CompanyManager{
	public List<Employee> getAllEmployee(){
		List<Employee> list = new ArrayList<Employee>();
		for(int i=0; i<30; i++){
			Employee emp = new Employee();
			//为总公司人员按顺序分配一个ID
			emp.setId("总公司"+i);
			list.add(emp);
		}
		return list;
	}
	
	public void printAllEmployee(SubCompanyManager sub){
		sub.printEmployee();
		List<Employee> list2 = this.getAllEmployee();
		for(Employee e:list2){
			System.out.println(e.getId());
		}
	}
}
~~~

修改后，为分公司增加了打印人员ID的方法，总公司直接调用来打印，从而避免了与分公司的员工发生耦合。

迪米特法则的初衷是降低类之间的耦合，由于每个类都减少了不必要的依赖，因此的确可以降低耦合关系。但是凡事都有度，虽然可以避免与非直接的类通信，但是要通信，必然会通过一个“中介”来发生联系，例如本例中，总公司就是通过分公司这个“中介”来与分公司的员工发生联系的。过分的使用迪米特原则，会产生大量这样的中介和传递类，导致系统复杂度变大。所以在采用迪米特法则时要反复权衡，既做到结构清晰，又要高内聚低耦合。

---

###设计模式六大原则（6）：开闭原则 

以下为原文：写的实在是太好了

定义：一个软件实体如类、模块和函数应该对扩展开放，对修改关闭。

问题由来：在软件的生命周期内，因为变化、升级和维护等原因需要对软件原有代码进行修改时，可能会给旧代码中引入错误，也可能会使我们不得不对整个功能进行重构，并且需要原有代码经过重新测试。

解决方案：当软件需要变化时，尽量通过扩展软件实体的行为来实现变化，而不是通过修改已有的代码来实现变化。

开闭原则是面向对象设计中最基础的设计原则，它指导我们如何建立稳定灵活的系统。开闭原则可能是设计模式六项原则中定义最模糊的一个了，它只告诉我们对扩展开放，对修改关闭，可是到底如何才能做到对扩展开放，对修改关闭，并没有明确的告诉我们。以前，如果有人告诉我“你进行设计的时候一定要遵守开闭原则”，我会觉的他什么都没说，但貌似又什么都说了。因为开闭原则真的太虚了。

在仔细思考以及仔细阅读很多设计模式的文章后，终于对开闭原则有了一点认识。其实，我们遵循设计模式前面5大原则，以及使用23种设计模式的目的就是遵循开闭原则。也就是说，只要我们对前面5项原则遵守的好了，设计出的软件自然是符合开闭原则的，这个开闭原则更像是前面五项原则遵守程度的“平均得分”，前面5项原则遵守的好，平均分自然就高，说明软件设计开闭原则遵守的好；如果前面5项原则遵守的不好，则说明开闭原则遵守的不好。

其实笔者认为，开闭原则无非就是想表达这样一层意思：用抽象构建框架，用实现扩展细节。因为抽象灵活性好，适应性广，只要抽象的合理，可以基本保持软件架构的稳定。而软件中易变的细节，我们用从抽象派生的实现类来进行扩展，当软件需要发生变化时，我们只需要根据需求重新派生一个实现类来扩展就可以了。当然前提是我们的抽象要合理，要对需求的变更有前瞻性和预见性才行。

说到这里，再回想一下前面说的5项原则，恰恰是告诉我们用抽象构建框架，用实现扩展细节的注意事项而已：单一职责原则告诉我们实现类要职责单一；里氏替换原则告诉我们不要破坏继承体系；依赖倒置原则告诉我们要面向接口编程；接口隔离原则告诉我们在设计接口的时候要精简单一；迪米特法则告诉我们要降低耦合。而开闭原则是总纲，他告诉我们要对扩展开放，对修改关闭。

最后说明一下如何去遵守这六个原则。对这六个原则的遵守并不是是和否的问题，而是多和少的问题，也就是说，我们一般不会说有没有遵守，而是说遵守程度的多少。任何事都是过犹不及，设计模式的六个设计原则也是一样，制定这六个原则的目的并不是要我们刻板的遵守他们，而需要根据实际情况灵活运用。对他们的遵守程度只要在一个合理的范围内，就算是良好的设计。

---

## 设计模式的分类

+ ###创建型模式   ： 用来创建一些比较复杂的对象，特点就是将创建对象的过程隐藏，使用者不需要new，而是使用对应的接口来获取对象实例

  > + 工厂模式（Factory Pattern）
  > + 抽象工厂模式（Abstract Factory Pattern）
  > + 单例模式（Singleton Pattern）
  > + 建造者模式（Builder Pattern）
  > + 原型模式（Prototype Pattern）

+ ###结构型模式   ：多用在优化软件结构上，比如降低一些类的耦合，或者将一些对象在不同情况下的组合可以实现不同的功能，这里用的最多的就是抽象和接口了

  > - 适配器模式（Adapter Pattern）
  > - 桥接模式（Bridge Pattern）
  > - 过滤器模式（Filter、Criteria Pattern）
  > - 组合模式（Composite Pattern）
  > - 装饰器模式（Decorator Pattern）
  > - 外观模式（Facade Pattern）
  > - 享元模式（Flyweight Pattern）
  > - 代理模式（Proxy Pattern）

+ ### 行为模式  ：用来处理对象之间的通信

  > - 责任链模式（Chain of Responsibility Pattern）
  > - 命令模式（Command Pattern）
  > - 解释器模式（Interpreter Pattern）
  > - 迭代器模式（Iterator Pattern）
  > - 中介者模式（Mediator Pattern）
  > - 备忘录模式（Memento Pattern）
  > - 观察者模式（Observer Pattern）
  > - 状态模式（State Pattern）
  > - 空对象模式（Null Object Pattern）
  > - 策略模式（Strategy Pattern）
  > - 模板模式（Template Pattern）
  > - 访问者模式（Visitor Pattern）