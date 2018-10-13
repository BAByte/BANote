[toc]
# 用ViewPage时遇到的问题
由于我们有三个主要的Fragment，而且都需要一直保持不被销毁，在没有更改任何ViewPage 的一些方法时，ViewPage 会自动销毁或者重新加载Fragment，一但Fragment里面有多层View或者很多图的时候，左右滑动会卡顿
# 解决方法
复写适配器的销毁方法，把super去掉
~~~java
  /**
     * 复写这个方法去掉super，禁止销毁碎片，这样滑动不会卡顿
     * @author BA on 2018/1/26 0026
     * @param
     * @return
     * @exception
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }
    ~~~