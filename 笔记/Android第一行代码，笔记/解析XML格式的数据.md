[TOC]



# 解析XML格式的数据

## 必备知识

> ~~~xml
> <apps>
> 	<app>
> 		<id>1</id>
> 		<name>QQ</name>
> 	</app>
> 	<app>
> 		<id>2</id>
> 		<name>WX</name>
> 	</app>
> </apps>
> ~~~
>
> 上面的数据是我们这一次需要解析的，我们想要获取应用的id名字
>
> 可以看到XML数据都是一个一个的节点组成的数据树，要怎么解析就看是用的是什么方法了

## Pull解析方式

### 分析

> 这种解析方法是根据节点的类型，节点的名字来解析的
>
> XmlPullParser类可以给我们提供一段XML数据中的节点类型和节点名字和节点对应的值
>
> 但是要注意的是，一开始不会马上读第一个<apps>,而是要进行一次判断，是不是开始解析文件
>
> 还有就是换行符也是会读的，像我们这里，有换行符，那就有意思了。读完第一个<apps>后不会马上读<app>,而是读一个换行符

### 用到的类

> + XmlPullParseFactory
>
>   > 用来获取XmlPullParser实例
>
> + XmlPullParser
>
>   > 用来储存XML数据的类，提供了一系列的从XML数据中判断是否是节点，以及提取节点类型的方法
>
> + StringReader
>
>   > 将字符串转换成字节流

### 具体实现

> + 用OkHttp对象向服务器请求对象
>
>   ~~~java
>                   OkHttpClient client = new OkHttpClient();
>                   Request request = new Request.Builder()
>                           .url("http://10.0.2.2/app.xml")//我这里是本地作为服务器
>                           .build();
>                   try {
>                       Response response = client.newCall(request).execute();
>                       String data = response.body().string();
>                       Log.d("ssss",data);
>                       parseXMLWithPull(data);//用来解析的方法，看下面
>                   } catch (IOException e) {
>                       e.printStackTrace();
>                   }
>   ~~~
>
> + 定义一个方法，借助XmlPullParseer用来解析Xml，其实里面的类型是很复杂的，我们是只针对app，id，name，这样的节点，类型也只管开始和结束，就可以不用管那么多了
>
> + 具体点就是，判断是不是节点的开始，是的话，判断是不是id或者name，是的话储存起来，然后下继续读下一个节点。判断是不是结束，是结束的话是不是一个app节点的结束，是的话说明前面我们已经把一个app的数据记录下来了。打印出来
>
>   ~~~java
>    public void parseXMLWithPull(String data){
>           try{
>             
>             //获取器XmlPullParser的工厂
>               XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
>             //获取XmlPullParser实例
>               XmlPullParser pullParser=factory.newPullParser();
>             //将字符串转换成字节流，传入XmlPullParser对象中
>               pullParser.setInput(new StringReader(data));
>
>             //获取节点类型
>               int eventType=pullParser.getEventType();
>               String id="";
>               String name="";
>
>             //如果节点类型是不是数据的结尾
>               while(eventType!=XmlPullParser.END_DOCUMENT){
>                 //获取节点的名字，有可能是null的，所以后面要注意
>                   String nodeName=pullParser.getName();
>                   switch (eventType){
>                       //如果是节点类型是开始就判断节点的名字是否是我们需要的
>                       //其实这里就连<apps>这个节点都会读的，但是不是我们想要的
>                       case XmlPullParser.START_TAG:
>                           if("id".equals(nodeName))
>                            //如果是我们想要的就取出节点名对应的值
>                               id=pullParser.nextText();
>                           else if("id".equals(nodeName))
>                               name=pullParser.nextText();
>                           break;
>                      //这里的节点结束就算是</id>这样的节点也是结束，也不是我们想要的
>                       //但是在上面取对应值的过程中会自动跳过取了值的结束标签
>                       case XmlPullParser.END_TAG:
>                      //最终的app的节点结束才是我们想要的
>                           if(nodeName.equals("app")){
>                               Log.d("应用id=",id);
>                               Log.d("应用name=",name);
>                           }
>                           break;
>                   }
>                 //获取下一个节点的类型
>                   eventType=pullParser.next();
>               }
>           }catch (Exception e){
>               e.printStackTrace();
>           }
>       }
>   ~~~
>
>   ​

## SAX解析方式

### 分析

> 其实还是根据节点来判断，原理一样。但是不用我们自己去拼命的读从流中读节点，解析节点，而是利用一个封装好的工具来解析

### 用到的类

> + DefaultHandler
>
>   > 这个类中有五个方法，所以我们要取继承这个类
>   >
>   > ~~~java
>   > public void startDocument() throws SAXException {
>   >        //在开始解析数据时，会自动调用这个方法
>   >     }
>   >
>   >     @Override
>   >     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
>   >         //开始解析节点时，自动调用该方法
>   >     }
>   >
>   >     @Override
>   >     public void characters(char[] ch, int start, int length) throws SAXException {
>   >        // 当节点中有值时，自动调用该方法，取到的值在第一个参数
>   >     }
>   >
>   >     @Override
>   >     public void endElement(String uri, String localName, String qName) throws SAXException {
>   >         //结束一个节点，自动调用
>   >     }
>   >
>   >     @Override
>   >     public void endDocument() throws SAXException {
>   >         super.endDocument();
>   >     }
>   > ~~~
>
> + StringReader
>
> + SAXParserFactory
>
>   > 用来获取解析SAXParser的实例
>
> + XMLReader
>
>   > 用来解析XML的对象，通过SAXParser获得，需要设置解析的操作方式（上面的Handler），还有xml数据
>
> + InputSource
>
>   > SAX规范流，由于SAX解析的数据要求输入的数据需要一些规范，还有数据的码表，该方类可以将输入的流自动设置编码

### 具体实现

~~~java
public class MyXmlHandler extends DefaultHandler {

    String nodeName;
    StringBuilder id;
    StringBuilder name;
    @Override
    public void startDocument() throws SAXException {
        id=new StringBuilder();
        name=new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        nodeName=localName;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if("id".equals(nodeName)){
            id.append(ch,start,length);
        }else if("name".equals(nodeName)){
            name.append(ch,start,length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if("app".equals(localName)){
            Log.d("llll",id.toString().trim());
            Log.d("llll",name.toString().trim());
            id.setLength(0);
            name.setLength(0);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }
}

~~~

~~~java
public void parseXMLWithSAX(String data){
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader xmlReader = factory.newSAXParser().getXMLReader();
            MyXmlHandler myXmlHandler = new MyXmlHandler();
            xmlReader.setContentHandler(myXmlHandler);
            xmlReader.parse(new InputSource(new StringReader(data)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
~~~

