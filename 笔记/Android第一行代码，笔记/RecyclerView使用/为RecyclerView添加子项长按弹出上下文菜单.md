[toc]
# 为RecyclerView添加子项长按弹出上下文菜单
参考：https://ask.helplib.com/android/post_1630683
## 先说两句
在ListView添加和在RecyclerView的方法不太一样，前者百度一堆，后者比较难找，所以自己写篇笔记
## 效果就是微信最近联系人长按后会出现一个悬浮菜单
## 实现
谷歌本来就提供了长按上下文菜单的方法，第一件事就是为RecyclerView的Item设置长按弹出菜单的子项
~~~java
//让ViewHolder实现View.OnCreateContextMenuListener这个接口，然后复写onCreateContextMenu来创建菜单
   static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
      ...

        public ViewHolder(View itemView) {
            super(itemView);
            ...
            
            //这里是为这个View设置长按菜单监听
            itemView.setOnCreateContextMenuListener(this);
        }

        //该方法用来设置菜单子项
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        
        //param：菜单里面子项组id（里面的item可以分组）
        //param： 当前添加的子项id
        //param： 不知道
        //param: 子项标题
            menu.add(0,NEXT_PLAY_ID,0,"下一首播放");
            menu.add(0,ADD_TO_LIST_ID,0,"添加到播放列表");
            menu.add(0,ALWAYS_PLAY_ID,0,"单曲循环");
        }
    }
~~~

---
接下来就是处理菜单子项点击事件了，在Fragment或者Activity复写下面这个方法
~~~java
 @Override
    public boolean onContextItemSelected(MenuItem item) {
    
    //注意这里有个获取被长按的子项在RecyclerView的位置，如果在ListView，系统是会提供的，但是RecyclerView不一样，要自己去监听，下面会讲如何监听
        int position=albumSongsAdapter.getLongPassPosition();
        
        //根据id处理对应的点击事件
        switch (item.getItemId()){
            case AlbumSongsAdapter.NEXT_PLAY_ID:
                Toast.makeText(this, "下一首播放", Toast.LENGTH_SHORT).show();
                return true;
            case AlbumSongsAdapter.ADD_TO_LIST_ID:
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                return true;
            case AlbumSongsAdapter.ALWAYS_PLAY_ID:
                Toast.makeText(this, "单曲循环播放", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onContextItemSelected(item);
    }
}
~~~

---
监听RecyclerView被长按的Item的位置,就是这么简单，只要
~~~java
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
       ...

        //设置长按监听获取Item位置
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
               
               //这个值等下再提供个方法给外界获取就好 longPassPosition=holder.getAdapterPosition();
                return false;
            }
        });
    }
~~~

## 总结
其实可以在res/menu/目录下自己写一个布局，然后像下面这样写就好
~~~java
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.list_item_context, menu);
    }
~~~

注意：菜单的子项id一定要是唯一的，这个应用程序内所有的上下文菜单的子项id必须是唯一的不然会乱分配点击事件