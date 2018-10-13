[toc]
# View滑动事件的拦截
参考：http://blog.csdn.net/u012925323/article/details/50467513
## 先说两句
在实际开发的时候遇到了一个事件分发的问题，这里就不写了，直接把上面链接的内容复制一下

> 作为android开发人员，你一定遇到过滑动冲突问题，比如说，在水平ViewPager中嵌套ScrollView，然后在SrollView中嵌套一个水平可滑动的控件，这个时候就出现了滑动冲突，系统无法判断用户是左右滑动ViewPager还是水平滑动控件，而我们的需求是要求在操作水平控件的时候，其父容器不作出任何反应，也就是说，父容器不对点击事件做出拦截。那么怎么实现这个呢？在这里给大家提供两种方法，外部拦截和内部拦截。

## 需求
一个布局里面有按钮，该布局支持拖动，但是你会发现当你按着按钮拖动的时候，布局的拖动事件会被按钮消费掉，那怎么办？

## 外部拦截，不让拖动事件分发给按钮

复写该布局的onInterceptTouchEvent()方法
~~~java
@Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        boolean isIntercept = false;
        int action = e.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                startX = (int) e.getX();
                startY = (int) e.getY();
                downX = (int) e.getX();
                downY = (int) e.getY();
                isIntercept = false;//Down点击事件不拦截，继续下传分发给子控件
                break;
            case MotionEvent.ACTION_MOVE:
               //这里要给一点误差值，滑动范围超过多少才会拦截，如果不设置，你会发现按钮很难被点击到
                int Y = getMeasuredHeight() - bottomContent.getMeasuredHeight() + 45;
                int deltaY = downY - startY;
                int delaX = downX - startX;
                if(isMyRecycleView){
                    if (Math.abs(deltaY) > 1 && Math.abs(deltaY) > Math.abs(delaX)) {
                        isIntercept = true;
                    }
                }else{
                    if(!isPullUp){
                        if (Math.abs(deltaY) > 1 && Math.abs(deltaY) > Math.abs(delaX)) {
                            isIntercept = true;
                        }
                    }else if(startY < Y){
                        downX = (int) e.getX();
                        downY = (int) e.getY();
                        if (Math.abs(deltaY) > 1 && Math.abs(deltaY) > Math.abs(delaX)) {
                            isIntercept = true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isIntercept = false;//点击事件分发给子控件
                break;
        }
        return isIntercept;
    }
~~~

## 按钮内部判断是否需要消费这个事件
在复写按钮的dispatchTouchEvent()方法
~~~java
   @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);//父容器不拦截点击事件，子控件拦截点击事件
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - lastX;
                int deltaY = y - lastY;
                if(Math.abs(deltaY) - Math.abs(deltaX) > 0){//竖直滑动的父容器拦截事件
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        lastX = x;
        lastY = y;
        return super.dispatchTouchEvent(ev);
    }
~~~
