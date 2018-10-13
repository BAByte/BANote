[TOC]



###注意！！！在活动布局文件中使用碎片时。必须设置id

## 碎片和活动之间的通信

### 1.直接在一个Fragment中调用另外一个Fragment中的方法

> 我们可以直接在一个Fragment中调用另外一个Fragment的公开方法，前提是要先拿到另外一个Fragment的实例，我们先来看看怎样在左边的Fragment中拿到右边Fragment的实例：
>
> ~~~java
> ContentFragment cf = (ContentFragment) getActivity()  
>                             .getFragmentManager().findFragmentById(  
>                                     R.id.content_fg);  
> ~~~
>
> 这里就有碎片和活动的通信了

## 碎片的生命周期



![1354170699_6619](E:\Android第一行代码，笔记\Fragment使用\1354170699_6619.png)



![微信图片_20170707152457](E:\Android第一行代码，笔记\Fragment使用\微信图片_20170707152457.jpg)





## 打了一天，突然就没了。所以写的简单点，动态加载碎片基本用法

> ```
> public void showRightFragment(Fragment fragment){
>
>     //碎片管理器者
>     FragmentManager fragmentManager=getSupportFragmentManager();
>
>     //打开事务
>     FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
>
>     //声明事务
>   //  fragmentTransaction.replace(R.id.right_fragment,fragment);
>
>     //将碎片放到返回栈中
>     fragmentTransaction.addToBackStack(null);
>
>     //提交事务
>     fragmentTransaction.commit();
> }
> ```







## 动态加载布局

> 动态加载布局算是常见，但是这里的动态加载不是指运行过程中添加布局，而是在 启动时更设备屏幕大小决定加载怎样的布局，这样就可以不用写两个软件来适配不同屏幕的设备了
>
> ### 需求
>
> ![微信图片_20170707145240](E:\Android第一行代码，笔记\Fragment使用\微信图片_20170707145240.jpg)



> ### 分析
>
> > 其实不难理解，有两种布局，一种为小屏幕设备的布局，一种为大屏幕设备的布局，将要区分的布局放在包含限定符的文件夹，系统就会自动的根据设备屏幕大小来适配
> >
> > 
>
> ### 具体实现
>
> + 将左右的碎片布局及管理类写好
>
> + 在res文件夹下新建layout-large目录-后面加的就是限定符了
>
> + 将要区分设备大小的活动布局放在不同的layout文件夹下，注意是活动布局，比如，都有碎片，但是碎片的主要显示布局还是在活动的布局文件里面设置的，所以只要相同的碎片布局啊什么的都放在layout文件夹下，将不同布局的活动布局文件放在不同限定符的layout文件夹下就好了
>
> + 下面就是一些限定符
>
>   | 屏幕特征 | 限定符    | 描述          |
>   | ---- | ------ | ----------- |
>   |      | small  | 提供给小屏设备的资源  |
>   |      | normal | 提供给中等屏设备的资源 |
>   |      | large  | 提供给大屏设备的资源  |
>   |      | xlarge | 提供给超大屏设备的资源 |
>
> | 分辨率  | ldpi   | 提供给低分辨率设备的资源（120dpi以下）   |
> | ---- | ------ | ------------------------ |
> |      | mdpi   | 提供给中等分辨率设备的资源（160dpi以下）  |
> |      | hdpi   | 提供给高分辨率设备的资源（240dpi以下）   |
> |      | xhdpi  | 提供给超高分辨率设备的资源（320dpi以下）  |
> |      | xxhdpi | 提供给超超高分辨率设备的资源（480dpi以下） |

| 方向   | land | 横屏   |
| ---- | ---- | ---- |
|      | port | 竖屏   |



### 更加灵活的限定符

-sw(dpi)

比如：屏幕宽度大于600dpi的设备资源文件就放在 layout-sw600文件夹