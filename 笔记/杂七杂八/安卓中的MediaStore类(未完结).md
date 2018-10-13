[toc]
# MediaStore
该类是安卓中用来查询所有媒体文件的表，简单的说就是用该类可以获取到所有或者指定类型的媒体文件的绝对路径
## 先说两句
这个类只要掌握用法，说白了就是查表，取数据的操作，会数据库就会这个！！！去哪里查表?系统会把这些媒体文件建表，所以当然是从系统管理这些媒体数据的软件查这些数据，向另一个软件获取数据那就要用到内容提供器，不记得可以去翻翻笔记，内容提供器需要一个Uri来进行查询，所以先要找到媒体文件的Uri

---
## 内容提供器复习
~~~java
//其实就是内容提供器的使用方法，下面是查询方法的源码，

    /* @param uri The URI, 
     *
     * @param projection 指定查询哪几列
     *         
     * @param selection 添加查询的一些限定条件
     * 
     * @param selectionArgs具体的限定条件
     *        
     * @param sortOrder
     *    排列顺序
     */
public final @Nullable Cursor query(@RequiresPermission.Read @NonNull Uri uri,
            @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder){}

~~~
## 各种媒体文件的Uri
+ 图片 
~~~java
//查询外部储存的所有图片类型媒体文件所要用的Uri
MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//查询内部储存的所有图片类型媒体文件所要用到的Uri
MediaStore.Images.Media.INTERNAL_CONTENT_URI
//目前的手机已经不用管外部内部了，直接用第一个就好
~~~
+ 音乐文件
~~~java
//查询所有艺术家，这个Uri可以得到媒体的所有艺术家的信息以及在该表中的一些属性
MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
MediaStore.Audio.Artists.INTERNAL_CONTENT_URI


//查询所有专辑，同上
MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
MediaStore.Audio.Albums.INTERNAL_CONTENT_URI

//查询所有音乐
MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
MediaStore.Audio.Media.INTERNAL_CONTENT_URI
~~~
---
## 获取所有图片实例
~~~java

//开始查询，这里可以设置只查jpge或者png结尾什么的，具体可以百度下
Cursor cursor=getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection,selection,selectionsArgs,sortOrder);

//只查询固定格式的图片，比如png和jpge
    Cursor mCursor = mContentResolver.query(mImageUri, null,
        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
~~~

## 获取所有音乐实例
~~~java
//获取音乐是这样，获取视频也是这样
Cursor cursor = context.getContentResolver().query(externalUri, null, selection, selectionArgs, null);
~~~

## 总结
这里写的比较笼统，但是其实就是用URI查询配合内容提供器去查表的操作，还是挺简单的