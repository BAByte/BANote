[TOC]

#使用OkHttp库

- 导入库的地址：com.squareup.okhttp3:okhttp:3.4.1

  ##使用到的类

  > - OkHttpClient
  >
  >   > 访问网络的操作器，用来开启网络的连接
  >
  > - Request
  >
  >   > 用来处理请求的类，用了Builder模式，可以用来设置访问链接的一些属性
  >   >
  >   > 比如设置网址
  >
  > - Response
  >
  >   > 如果是请求服务器返回数据，那么就会返回该对象，里面包含了返回的数据
  >   >
  >   > Response.boby().string()可以得到字符串形式的数据

  ##具体实现

  ### 发起get请求

  > ```java
  >       
  > OkHttpClient  okHttpClient=new OkHttpClient();
  >
  > //同步发起
  >         //通过Builder辅助类构建一个Request对象
  >         Request request = new Request.Builder()
  >           .get()
  >           .url(url)
  >           .build();
  >
  >         //通过同步执行获取一个Response对象，也可以发起请求并且提供回调接口来处理请求发起后的逻辑
  >         Response response = okHttpClient.newCall(request).execute();
  >         //判断响应是否成功,如果成功的话,响应的内容会放在response.body()中
  >         if (response.isSuccessful()) {
  >             //字符串类型
  >             Log.i(TAG, "getData: " + response.body().string());
  >             //字节数组类型
  >             Log.i(TAG, "getData: " + response.body().bytes());
  >             //字节流类型
  >             Log.i(TAG, "getData: " + response.body().byteStream());
  >             //字符流类型
  >             Log.i(TAG, "getData: " + response.body().charStream());
  >         }
  >
  >
  > //异步发起
  > 		//通过Builder辅助类构建一个Request对象
  >         Request request = new Request.Builder()
  >           .get()
  >           .url(url)
  >           .build();
  >
  >         //发起请求并且提供回调接口来处理请求发起后的逻辑，通过入队的方式,进行异步操作
  >         okHttpClient.newCall(request)
  >           .enqueue(new Callback() {
  >             @Override
  >             public void onFailure(Call call, IOException e) {
  >                 Log.i(TAG, "onFailure: 请求失败的时候调用该方法!");
  >             }
  >             @Override
  >             public void onResponse(Call call, Response response) throws IOException {
  >                 //字符串类型
  >                 Log.i(TAG, "getData: " + response.body().string());
  >                 //字节数组类型
  >                 Log.i(TAG, "getData: " + response.body().bytes());
  >                 //字节流类型
  >                 Log.i(TAG, "getData: " + response.body().byteStream());
  >                 //字符流类型
  >                 Log.i(TAG, "getData: " + response.body().charStream());
  >             }
  >         });
  > ```
  >
  > ------

  ###发起post请求

  > ```java
  >
  >     /**
  >      * 使用post方式提交json字符串
  >      *
  >      * @param url     提交的路径
  >      * @param content 提交的内容
  >      */
  >     public void postString(String url, String content) {
  >         //构建一个RequestBody对象,,因为提交的是json字符串需要添加一个MediaType为"application/json",
  >         // 普通的字符串直接是null就可以了
  >         RequestBody requestBody = RequestBody.create(
  >             MediaType.parse("application/json"), content);
  >         Request request = new Request.Builder()
  >                 .url(url)
  >                 .post(requestBody)
  >                 .build();
  >         okHttpClient.newCall(request).enqueue(new Callback() {
  >             @Override
  >             public void onFailure(Call call, IOException e) {
  >                 Log.i(TAG, "onFailure: " + e.getMessage());
  >             }
  >
  >             @Override
  >             public void onResponse(Call call, Response response) throws IOException {
  >                 Log.i(TAG, "onResponse: " + response.body().string());
  >             }
  >         });
  >     }
  >
  >     /**
  >      * 提交单个键值对
  >      *
  >      * @param url
  >      * @param key
  >      * @param value
  >      */
  >     public static void postKeyValuePaire(String url, String key, String value) {
  >         //提交键值对需要用到FormBody,因为FormBody是继承RequestBody的,所以拥有RequestBody的一切属性
  >         FormBody formBody = new FormBody.Builder()
  >                 //添加键值对
  >                 .add(key, value)
  >                 .build();
  >         Request request = new Request.Builder()
  >                 .post(formBody)
  >                 .url(url)
  >                 .build();
  >         okHttpClient.newCall(request).enqueue(new Callback() {
  >             @Override
  >             public void onFailure(Call call, IOException e) {
  >
  >             }
  >
  >             @Override
  >             public void onResponse(Call call, Response response) throws IOException {
  >                 Log.i(TAG, "onResponse: " + response.body().string());
  >             }
  >         });
  >     }
  >
  >     /**
  >      * 提交多个键值对
  >      * @param url 提交的路径
  >      * @param map 用来放置键值对,map的key对应键,value对应值
  >      */
  >     public static void postKeyValuePaires(String url, Map<String, String> map) {
  >         FormBody.Builder build = new FormBody.Builder();
  >         if (map != null) {
  >             //增强for循环遍历
  >             for (Map.Entry<String, String> entry : map.entrySet()) {
  >                 build.add(entry.getKey(), entry.getValue());
  >             }
  >         }
  >         FormBody formBody = build.build();
  >
  >         Request request = new Request.Builder()
  >                 .post(formBody)
  >                 .url(url)
  >                 .build();
  >         okHttpClient.newCall(request).enqueue(new Callback() {
  >             @Override
  >             public void onFailure(Call call, IOException e) {
  >
  >             }
  >
  >             @Override
  >             public void onResponse(Call call, Response response) throws IOException {
  >                 Log.i(TAG, "onResponse: " + response.body().string());
  >             }
  >         });
  >     }
  > ```
  >
  > ​