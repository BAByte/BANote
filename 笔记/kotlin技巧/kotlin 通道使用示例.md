# kotlin Channel使用示例

在项目中我看到有使用Channel来做事件分发的实现，我称之为 “绝技” ！！！

~~~kotlin
package com.example.channeldemo

import android.util.Log
import junit.framework.TestCase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import org.junit.Test
import java.util.HashMap

class MainActivityTest : TestCase() {
    private val unit = 60

    @Test
    fun testChannelDisBuffer() = runBlocking {
        //设置一个有缓冲的通道 ，缓冲4个
        val event: Channel<Int> = Channel(4)
        launch {
            var count = 0
            while (true) {
                event.send(count++)
                Log.d("MainActivityTest testChannelDisBuffer send num", "$count")
            }
        }

      	//没有接受者，但是通道的缓冲区只有4个，所以只会发到4个就被挂起
        delay(1000)
        event.cancel()
        Unit
    }

    @Test
    fun testChannelDisBufferBUFFERED() = runBlocking {
        //设置一个有无限缓冲的通道
        val event: Channel<Int> = Channel(Channel.BUFFERED)
        launch {
            var count = 0
            while (true) {
                event.send(count++)
                Log.d("MainActivityTest testChannelDisBufferBUFFERED send num", "$count")
            }
        }

      	//猜猜结果？
        delay(1000)
        event.cancel()
        Unit
    }

  	//发送了一个，发送方会被挂起，接收方会接收到，然后接收方也被挂起，然后发送方继续发送
    @Test
    fun testChannelDis() = runBlocking {
        //设置一个没有缓冲的通道
        val event: Channel<Int> = Channel()
        launch {
            var count = 0
            while (true) {
                Log.d("MainActivityTest testChannelDis send befor num", "$count")
                event.send(count++)
                Log.d("MainActivityTest testChannelDis send end num", "$count")
            }
        }

        delay(1000)
        event.cancel()
        Unit
    }

  
   ....//这部分比较有意思，所以放在了后面
}

~~~



## 使用管道实现不同策略的定时启动

ps : 这是一个比较有意思的例子，分别使用异步流和管道实现，如果还没有学到异步流的，可以直接看管道的实现。

**需求**

+ 你出去玩，约了个xxx顺风车，你发现司机开的路线和你自己导航的不一样，你感到很害怕所以向你的好基友求助。基友让你每隔5分钟给他发一条确认安全的信息。
+ 你又把事情告诉了你爸妈，你爸妈要求你每10分钟发一条信息
+ 你报了警，警察让你每15分钟上报你的位置

面对很多的定时策略，可以这样写：

~~~kotlin
class MainActivityTest : TestCase() {
  		private val unit = 60 //用1代替60，就是1分钟等于60秒的意思
  ...
@ExperimentalCoroutinesApi
    @Test
    fun testChannel() = runBlocking {
        val cycleMap = mutableListOf<Int>(300, 600, 900) //这里分别是 5分钟，10分钟，15分钟

        for (index in cycleMap.indices) {
            cycleMap[index] = cycleMap[index] / unit //把他们全部换成分钟
        }

        val produceBeat = produceBeat()
        produceBeat.consumeEach {
            Log.d("MainActivityTest testChannel ", ">>>>>>>>>>>>>>>>>>>>>>>>>")
            Log.d("MainActivityTest testChannel it =", "$it")
            for (item in cycleMap) {
              //取模，比如时间过了10分钟，it的值会为10，具体策略的触发时间为item的值，
              //在第十分钟的时候，意味着应该给基友发第二条信息了 10 % 5 == 0，符合条件
              //还应该给你爸妈发信息了 10 % 10 == 0 符合条件
                if (it % item == 0) { 
                    Log.d("MainActivityTest testChannel action :", "$item min task working")
                }
            }
        }

        Log.d("MainActivityTest testChannel  :", "done")
        Unit
    }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.produceBeat(): ReceiveChannel<Int> = produce {
        var count = 0
        while (true) {
            count++
            send(count)
            delay(unit * 1000L) //一分钟心跳一次
        }
    }
  
}
~~~



## 使用异步流实现不同策略的定时启动

~~~kotlin
 @Test
    fun testFlow() = runBlocking {
        val cycleMap = mutableListOf<Int>(60, 120, 180, 340)
        for (index in cycleMap.indices) {
            cycleMap[index] = cycleMap[index] / unit
        }

        flow {
            var count = 0
            while (true) {
                count++
                emit(count) //发射值
                delay(unit * 1000L)
            }
        }.collect { 
          	//这里会收到上面发射的值，也就是下面的it
            Log.d("MainActivityTest testFlow ", "--------------------------")
            Log.d("MainActivityTest testFlow it =", "$it")
            for (item in cycleMap) {
                if (it % item == 0) {
                    Log.d("MainActivityTest testFlow action :", "$item min task working")
                }
            }
        }

        Log.d("MainActivityTest testFlow  :", "done")
        Unit
    }


   
~~~



# 总结

其实两钟都能实现，但是你发现没有，这种情况使用管道好像有点大材小用了，管道有个概念叫扇入：多个协程产生的事件，可以发送到管道中给一个协程去处理，说到这里你是否已经想到怎么去用管道优雅的替代Handler了吧？

