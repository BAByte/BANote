# Android中的数据持久化技术

[TOC]

## 文件储存

> + 就是用java中的流来对数据进行操作
> + 文件储存是将数据原封不动的存储
> + 一般用于对简单的文本，二进制进行存储
> + 如果你需要用文本储存方式存储比较复杂的数据，那就自己要有格式，方便解析
> + 默认放在内存卡data/data/<package-name>/file/

### 具体用法

> + Context中提供了openFileOutput和openFileInput方法，
>
> + 直接看代码吧
>
> + 需求：用文件储存的方式储存一段文本，然后用文本储存的方式读出文本
>
> + 需要注意的一点，在output流没有关闭时，是无法用input流读取output中的数据的
>
> + 原因就是，只有流关闭后，才会保存数据到储存设备中
>
>   ~~~java
>    //文件流，主要将数据原封不动的存储，所以底层还是字节流
>           FileOutputStream out;
>           FileInputStream in;
>
>           //字符流缓冲区
>           BufferedWriter bw=null;
>           BufferedReader br=null;
>           String data ="This is ma data";
>
>           try{
>               //获取文件流，第一个参数是文件名，第二个是操作模式
>               //MODE_PRIVATE：同名文件覆盖，表示只有当前应用可以读，可以理解为文件内容是private的无法访问，
>               // 就不能追加，所以就直接覆盖
>               //MODE_APPEND：由权限名可知，append应用到结尾，意思是有同名文件
>               //就在结尾追加
>               out=openFileOutput("data",MODE_APPEND);
>
>               //将文件流传到转换流中，再用转换流将二进制流转换成字符流
>               //因为这里存储的都是字符，所以转换后用带有字符缓冲区的流来操作
>               bw=new BufferedWriter(new OutputStreamWriter(out));
>               bw.write(data);
>           }catch (IOException e){
>               e.printStackTrace();
>           }finally {//一定会执行的代码块
>                   try{
>                       if(bw!=null) //记得关闭流
>                           bw.close();
>                   }catch (IOException e){
>                       e.printStackTrace();
>                   }
>           }
>
>   		//读
>           try{
>               in=openFileInput("data");
>               br=new BufferedReader(new InputStreamReader(in));
>
>               String line="";
>               StringBuffer sb=new StringBuffer();
>
>               while((line=br.readLine())!=null){
>                   sb.append(line);
>               }
>
>               Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
>           }catch (IOException e){
>               e.printStackTrace();
>           }finally {
>               if(br!=null)
>                   try {
>                       br.close();
>                   }catch (IOException e){
>                       e.printStackTrace();
>                   }
>           }
>       }
>   ~~~
>

## SharedPreferences储存方案

> + SharedPreferences是以键值对的形式存储数据的存储方案，内部是用Xml格式进行数据的管理
> + 这方案支持的数据形式很多，比如整型，布尔型，字符型，意思是你存的是什么类型，取出来也是什么类型
> + 默认放在内存卡data/data/<package name>/shared_prefs/

### 具体用法

> + 存数据
>
> > + 获取SharedPreferences实例
> >
> >   ~~~java
> >   //一共有3种方法
> >
> >   //调用Activity的方法
> >   //这种方法只接收一个参数，设置权限，目前也只有一种权限
> >   //该方法会自动将文件以活动名称命名
> >   SharedPreferences sp=getPreferences(MODE_PRIVATE);
> >
> >   //调用Context方法
> >   //接收两个参数，文件名，权限
> >   SharedPreferences sp=getSharedPreferences("data",MODE_PRIVATE);
> >
> >   //调用PreferencesManager类的静态方法，传入Context
> >   SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
> >   ~~~
> >
> > + 获取Editor，添加数据，保存数据
> >
> >   ~~~java
> >   SharedPreferences.Editor e;
> >   e= sp.edit();//获取实例
> >   e.putBoolean("check",true);                        e.putString("account","jj";
> >   e.putString("password","fd");
> >   e.apply();//将数据保存
> >   ~~~
>
> + 取数据
>
>   > + 用SharedPreferences实例直接通过键取值
>   >
>   > ~~~java
>   > sp.getString("account","");//第二个参数是没有取到值后的默认值
>   > ~~~