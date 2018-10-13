# FileProvider

该内容提供器主要提供的不是数据而是Uri

这个Uri封装了一个被共享出去的文件或者目录的绝对路径，

A程序需要将一个文件分享给B程序，直接传文件当然是不可以的，那A程序就告诉B程序被共享文件的绝对路径，B程序自己去取文件。

但是直接告诉绝对路径是不安全的，在安卓N后，所有想要分享绝对路径的操作都会报异常，为了安全的分享文件，就将文件的绝对路径用FileProvider封装在Uri中，设置访问Uri的权限，再传这个Uri给B程序。B程序拥有了相应的Uri后。由于A程序已经设置了对应的权限，那么就可以用Uri对文件进行操作了。

意思就是A程序要设置FileProvider，必须要注册

~~~xml
<provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="com.example.ljh99.getphotodemo.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
</provider>
~~~

name是固定的，authorities就是生产Uri中的authorities部分

但是在注册该FileProvider时设置了

android:exported="false"
android:grantUriPermissions="true"

第一行的意思是，该内容提供器无法被其他应用程序访问

看第二行，意思是临时的给平常无权对 FileProvider 封装过的Uri的访问进行授权 ，也就是说A程序要是不设置访问权限给B程序，B程序就算拿到Uri也没有办法去访问Uri中的封装的绝对路径下文件 ，怎么确定这个权限呢？可以在Uri时我们是将Uri放在intent中，同时用Intent设定权限（下面会讲）

再看看mate-data标签。名字是：android.support.FILE_PROVIDER_PATHS，意思是什么自己看，这个标签就是给FileProvider设置共享路径的，什么意思？？？就是说FileProvider只能为你设定好的共享路径下的子目录或者文件生产Uri，这就有意思了，就是说A程序可以指定哪些目录可以被分享出去（如果不能生成Uri，又不能直接传绝对路径，那就不能分享文件啦（别他TM和老子说用Intent传，当然可以，问题就出在只有你写的程序知道你用Intent传的是绝对路径，而这里的需求是和任意一个软件共享这个文件，人家知道你他妈用intent把传绝对路径过来？？）），那么这里可以看到里面引用了一个xml/file_paths文件，这个文件就是设置共享路径的地方，当然，这个文件是要自己写的，看下面



配置路径的文件，路径的<external-path 这一个标签其实有对应着很多类型的路径，我这里设置的就是将整个sd卡的下的目录都共享，以后就这样设置就好了，就不需要设定一堆标签，设定好共享目录后，我们的程序就能为目录下的所有文件啊目录啊生产Uri了，name属性是用来生成Uri时在里面加的子目录，你去打印下Uri就知道怎么回事了

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="images" path=""/>
</paths>
~~~



A程序已经设置好FileProvider了，这就意味着设置好共享的东西了（上面已经设置好共享目录了，要共享该目录下的啥东西都可以），接下来就是等B程序请求A程序共享一个文件了

来了啊！！！B程序通过隐式Intent启动了A程序的的一个选择文件的界面，这时！！B程序选中了一个文件，要求A程序共享出来，A程序一看，这个文件正好在我FileProvider设置的共享目录范围内。然后就生成该文件的Uri，设置权限，然后传给B程序

~~~java
uri= FileProvider.getUriForFile(Main2Activity.this
                                ,"com.example.ljh99.fileproviderdemo",file);
 Intent intent=new Intent();

//用来授予B程序对于Uri里面文件操作的临时权限，可以设置为 FLAG_GRANT_READ_URI_PERMISSION 
//FLAG_GRANT_WRITE_URI_PERMISSION 两个同时设置也是可以的
 intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

//设置Intent里面要传的数据，和数据类型
 intent.setDataAndType(uri,getContentResolver().getType(uri));

//传给B程序
 MainActivity.this.setResult(RESULT_OK,intent);
~~~



B程序大喜！！拿到了拿到了！！！乐极生悲，B程序发现自己这个Uri的操作权限只是临时的，还有就是查询Uri里面封装的文件信息都要通过A程序的FileProvide，，就连读取文件出来，都要经过A程序的FielProvider，什么意思呢，就是说，B程序想要知道文件MIME类型，A程序的FileProvider会告诉B程序，B程序相要去找文件，无奈只有Uri，不知道绝对路径，只能找A程序的FileProvoder获取文件，B程序全程没有接触过文件啊！！！都是A程序FileProvider这个中介在搞！！B程序：MMP！好了，看代码吧。

~~~java
getContentResolver().openInputStream(uri)//通过A程序的FileProvider从流中读取文件，至于怎么查类型，自己看API
~~~



那么哪里安全？？？

1. 绝对路径只有A程序有，B程序没有
2. B程序读取文件的权限只是临时的，一旦完成操作，权限会被去除
3. 只有持有封装了文件真实路径，带有权限的Uri，才能临时访问A程序的FileContent，没有持有的程序不能访问A程序的FileProvider，就无法访问到被共享的文件
4. 操作文件必须通过A程序的FileProvider，否则没门
5. 有意思不，哈哈哈哈

