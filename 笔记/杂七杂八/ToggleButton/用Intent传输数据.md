# 用Intent传输数据

[TOC]

## 需求

A活动传递数据给B活动，并且要求B活动销毁时返回数据给A活动



## 分析

+ 在A活动启动B活动设置数据，启动B活动时要求B返回数据

+ B活动接到数据，取出数据，

+ B活动销毁时返回数据给A活动

+ A活动接收数据

  ​

## 具体实现

1. 步骤1，A活动中的代码

   >~~~java
   > Intent intent=new Intent(MainActivity.this,Main2Activity.class);
   > intent.putExtra("hi","Hi! secondActivity!!!");//设置数据的键和值
   >//启动B活动，请求码为1，用来等下接收返回数据时区分是哪一个活动返回的数据
   > startActivityForResult(intent,1); 
   >~~~
   >
   >​

   ​

2. 步骤2，B活动的代码

   >~~~java
   > xxxxxxxxxx Intent intent =getIntent();Toast.makeText(this, intent.getStringExtra("hi"), Toast.LENGTH_SHORT).show(); //取出数据并且打印
   >~~~
   >
   >​

   ​

3. 步骤3，B活动的代码

   >~~~java
   > @Override
   >    public void onBackPressed() { //按下返回键后销毁B活动，返回数据
   >        Intent intent=new Intent();
   >        intent.putExtra("hi","Hi!!! one"); //设置数据的键和值
   >        setResult(RESULT_OK,intent);  //设置返回的结果
   >        finish();
   >    }
   >~~~
   >
   >​

   ​​

4. 步骤4，A活动的代码

   >~~~java
   > @Override
   >    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
   >        switch (requestCode){ //判断是哪一个活动返回的
   >            case 1:
   >                if(resultCode==RESULT_OK) //确定返回结果
   >                    Toast.makeText(this, data.getStringExtra("hi"),  								   Toast.LENGTH_SHORT).show();
   >                break;
   >        }
   >    }
   >~~~
   >
   >​

   ​