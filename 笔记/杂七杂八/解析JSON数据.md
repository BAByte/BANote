[TOC]

# 解析JSON数据

## 比必备知识

> 先看格式：
>
> ~~~JSON
> [{"id":"1","name":"QQ"},
>  {"id":"2","name":"WX"}]
> ~~~
>
> 可以看到 [ ] 是数据的开始和结束，而一段数据是：{"id":"1","name":"QQ"}，其实不难理解，我们把这一整段数据 [ ]当作是一个JSONArray集合，集合里面 { }是一个个JSONObkect对象，对象里面包含了成员数据，就是像数据库的LitePal一样，建立对象映射关系

## JSONObject解析方法

### 用到的类

> + JSONArray
>
>   > 将String数据传入其构造函数，给该类会自动将String里面的数据转化成JSONObject，并且将这些对象装到JSONArray中
>
> + JSONObject

### 具体实现

> + 获取数据
>
> + 将数据传入
>
>   ~~~java
>       public void parseJSONWithJSNOObject(String data){
>           try{
>               JSONArray jsonArray=new JSONArray(data);
>               for (int i=0;i<jsonArray.length();i++){
>                   JSONObject object=jsonArray.getJSONObject(i);
>                   Log.d("ooooo",object.getString("id"));
>                   Log.d("ooooo",object.getString("name"));
>               }
>           }catch (JSONException e){
>               e.printStackTrace();
>           }
>       }
>   ~~~

## GSON解析方法

> + 一个库，导入地址：com.goolgle.code.gson:gson:2.7
> + 这个方法就是更明确的将数据映射成对象
> + 同样的写一个实体类，数据成员当然就是数据段中有什么了，类中包含了get and set成员方法

### 用到的类

> + Gson
>
>   > 他是主角啊，就是他用来将数据解析成一个对象或者一个List集合
>
> + TypeToken
>
>   > 既然我们指定解析完数据返回一个List集合，List毫无疑问我们是要指定泛型的，但是Gson解析对象时是不知道要返回什么类型的，所以在向Gson的解析方法传入数据时同时传入要返回的类型
>   >
>   > 通过  new TypeToken<List<T>>(){}.getType()  这个方法可以获得List<T>的类型

### 具体实现

> ~~~java
>     public void parseJSONWithGSON(String data){
>         Gson gson=new Gson();
>       
>       //因为我们里面的数据格式是一个集合，所以解析时就会要求返回一个集合，当然要解析一段数据时就是返回一个对应的实体类的对象，所以我们要设置要返回的数据类型，上面说了返回的数据类型设置就是用 new TypeToken<List<App>>(){}.getType());这个方法实现的
>         List<App> list=gson.fromJson(data,new TypeToken<List<App>>(){}.getType());
>         Log.d("oooo",new TypeToken<List<App>>(){}.getType().toString());
>         for (App app:list){
>             Log.d("ooooo",app.getId());
>             Log.d("ooooo",app.getName());
>         }
>     }
> ~~~
>
> 

