# 使用ViewFlipper实现幻灯片效果

可以在里面直接添加ImageView，这里我们使用动态添加的方式

~~~xml
<ViewFlipper
        android:id="@+id/view_flipper"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    </ViewFlipper>
~~~



~~~java
        int[] id=new int[]{R.drawable.one,R.drawable.tow,R.drawable.three};

        flipper=(ViewFlipper)findViewById(R.id.view_flipper);

		//添加图片
        for (int i=0;i<id.length;i++){
            flipper.addView(getResourse(id[i]));
        }

        //设置图片出现和退出的动画，这里没有，里面是要传参数的
        //flipper.setInAnimation();
        //flipper.setOutAnimation();
	    //设置切换间隔
        //flipper.setFlipInterval(3000);
        //开始自动播放
        //flipper.startFlipping();

    }

//加载图片到ImageView中
    public ImageView getResourse(int id){
        ImageView imageView=new ImageView(this);
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),id));
        return imageView;
    }

//这是触摸事件的监听，实现的是向左向右滑动切换图片

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN: //按下时
                startX=event.getX(); //获取按下时手指的坐标
                break;
            case MotionEvent.ACTION_MOVE: //手指滑动时
                break;
            case MotionEvent.ACTION_UP: //手指抬起时
                if (startX-event.getX()<-100){
                    flipper.showPrevious(); //显示前一张图片
                }else if (startX-event.getX()>100)
                {
                    flipper.showNext();//下一张图片
                }
                break;
        }
        return super.onTouchEvent(event);
    }
~~~

