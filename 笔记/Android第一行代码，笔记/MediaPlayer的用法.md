[TOC]



# MediaPlayer的用法（要权限）

## 用到的类

> MediaPlayer
>
> > + 该类就是安卓默认播放音频的类，包含了10个方法来对音频进行操作
> >
> > + setDataSource()
> >
> >   > 设置要播放的音频位置，接收的参数是音频文件的地址
> >
> > + prepare()
> >
> >   > 在开始播放之前调用这个方法完成准备工作
> >
> > + start()
> >
> > + pause()
> >
> > + reset()
> >
> >   > 该方法是将MediaPlayer恢复到刚刚创建的状态，就是说要播放另一首歌，就不用再new一个MediaPlayer对象了
> >
> > + stop()
> >
> >   > 这个方法有个需要注意的地方就是，一旦调用，该MediaPlayer实例将无法再播放，再需要用到MediaPlayer实例就重新new
> >
> > + release()
> >
> >   > 释放资源
> >
> > + isPlaying()
> >
> > + getDuration()
> >
> >   > 获取播放时长