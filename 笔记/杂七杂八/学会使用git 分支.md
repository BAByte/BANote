[TOC]

# 学会使用git 分支

在正式开发中，如果我们需要多人协作，或者保证开发版本不影响线上版本，会把项目拓展成多个分支，在看这篇笔记前你必须要会使用git工具！

## 一般的分支划分

+ 线上版分支：master分支

  当我们完成一版稳点版时会合并到这个线上版分支，一般为master分支，这个分支作为其他分支的基版分支

+ debug分支

  该分支的进度必须和master一致，就是用来修复bug的分支

+ 开发分支，也就是我们说的dev 分支

  当我们已经发布了一版稳定代码，但是公司要求我们继续开发新功能，为了不影响线上的代码，我们新键一条开发分支，专门用来开发新功能。

## 实际情况 1

+ 当我们开发第一个版本时，我们本来只有一个分支：master分支
+ 开发完第一个版本后，进行上线，我们需要新键另一个分支，叫做debug分支，该分支代码必须和master进度一致。
+ 突然线上的代码用户发现了我们的bug ，老板要求我们去修复这个bug ，为了保证线上的代码不被影响，我们不会直接在master分支直接修bug（其实也可以直接修，修错了可以回滚）。
+ 我们会切换到debug分支修复，当修复后，我们先把debug分支内容全部提交（注意！这里是指提交debug分支代码到仓库，也就是commit命令）。
+ 然后我们切换到线上的分支master分支，对debug代码进行合并。这样一来就修复了bug。

## 实际情况 2

你可能会觉得上面的操作都可以直接用一个分支，是的，也体现不了分支的强大，但是当我们需要进行新功能开发了呢？

+ 我们会从master分支进行扩展出一条dev 分支，注意！这个时候master分支和debug分支以及我们刚刚新键的dev分支进度是一样的
+ 然后我们在dev分支上进行开发新功能，突然我们又遇到了线上用户的反馈，但是新功能开发到一半，那怎么办？别忘了我们还有debug分支！他就是用来修复bug的呀！
+ 我们会把dev的修改commit一次，然后把代码切换到debug分支（具体操作后面教），然后对bug进行修复，修复完测试通过后，我们还是记得先提交debug分支的修改(也就是commit命令)。然后切换到master分支对debug进行合并，这样一来线上的代码就修复了呀！
+ 但是别忘了我们的dev分支还是有这个bug的哦！那我们再切换到dev分支进行合并debug分支的代码，这样一来我们就把bug修复了。
+ 然后继续愉快的在dev分支上开发新功能啦！当新功能开发完，测试通过后，我们就切到master分支把dev的代码合并到master分支上，新功能就上线，就做完啦！
+ 前面一直强调，debug分支进度必须和master分支一样吧！切换到debug分支，合并master分支的代码！完成！

## 另说

其实团队协作时肯定不止一个人去操作dev分支，这就是单个分支的多人协作了，这其实比较简单，自己百度看看吧

## 实际操作

我们直接用情况2进行操作，注意，切换到不同分支的时候，Android Studio的代码也是会自动切换到对应的分支的代码，我们可以从编译器的右下角看到当前所在的分支。

![image](https://ws1.sinaimg.cn/mw690/006ev5f6gy1fzfe1buabgj30rq02t0sw.jpg)



如果没有显示说明你 可能只有一个默认主分支，如果你有多个分支，但是没有显示，那就点击下图这个按钮从新编译以下就有了

![image](https://ws1.sinaimg.cn/mw690/006ev5f6gy1fzfe3ttjt2j30pc02qmx8.jpg)

+ 当然如果你打开了git工具并且进入到你项目的目录就直接从git工具可以看到当前所在分支

+ 新键一个项目，我们默认只有master分支,下面那个蓝色的字就是我们目前所在的分支

  ![image](https://ws1.sinaimg.cn/mw690/006ev5f6gy1fzfdye12bzj30rc01v0sp.jpg)

+ 查看所有分支的git 命令,带星号的说明是当前所在分支

  ~~~xml
  git branch
  ~~~


![image](https://ws1.sinaimg.cn/mw690/006ev5f6gy1fzfe71yysaj30ut0563yo.jpg)

+ 假设我们第一版做完了，开始做新功能了，是不是开始在master分支下新键多两个分支？



  ~~~java
  //新键分支命令格式，注意，你在哪个分支下使用该命令就是从哪个分支进行扩展分支
  git branch [分支名]
  
  //示例：新键debug分支
  git branch debug
  
  //示例：新键dev分支
  git branch dev
  ~~~

+ 新键完后你可以查看所有分支了，那我们进行新功能的开发，是不是要切换到dev分支？切换分支的命令

  ~~~java
  //命令格式
  git checkout [分支名]
  
  //示例
  git checkout dev
  ~~~

+ 切换完后看看Android Studio有没有自动切换到dev分支下，如果没有，按照我们前面说的去做（重新编译）

+ 现在我们开发到一半（你可以自己随便加点代码），突然线上有个bug，是不是要切换到debug分支下？是的，但是我们首先要先对我们的开发到一半的dev分支进行提交，命令你一定知道吧

  ~~~java
  git add .
  git commit -m "提交信息"
  
  //如果你要提交到仓库就使用下面的命令，注意：有了多分支后，提交代码到仓库最好指明分支名，不要直接git puh，最后面的dev就是我们的分支名
  git push origin dev
  ~~~

+ 切换到debug分支，命令你一定知道吧？那我就不说啦，一定要记得要确认Android Studio编译器有没有切换到对应的分支

+ 当我们修复了这个bug ，是不是要给另外两个分支合并？别急还是要先把debug的代码先提交到本地仓库才行，也就是上面的命令

  ~~~java
  git add .
  git commit -m "提交信息"
  
  //如果你要提交到仓库就使用下面的命令，注意：有了多分支后，提交代码到仓库最好指明分支名，不要直接git puh，最后面的dev就是我们的分支名
  git push origin dev
  ~~~

+ 然后我们切换到主分支master，切换命令你也知道吧？一定要记得要确认Android Studio编译器有没有切换到对应的分支

+ 合并debug分支

  ~~~java
  //合并命令
  git merge [分支名]
  //示例
  git merge debug
  ~~~

+ 合并主分支，一半会跳出一个要你输入本次合并的原因的界面，学过Linux的你肯定知道是按 i  切换成插入模式吧？然后输入对应信息，按下esc键输入 :wq 保存退出，就ok啦，记得把主分支代码提交（命令前面讲过）。

+ 别忘了我们的dev分支，切回dev分支。一定要记得要确认Android Studio编译器有没有切换到对应的分支。然后也是合并分支命令，嗯然后记得提交，然后再继续新功能开发

+ 当我们开发完新功能后，记得把dev分支代码提交（命令前面讲过），然乎切回主分支进行合并，然后把主分支代码提交，然后切回debug分支，把主分支合并到debug分支，记得提交代码！这样一来，所有分支的代码进度又同步了！

## 会遇到的小问题

一般就是忘了提交代码就想合并分支，会提示合并失败自己去切换对应分支，先提交再合并。又或者是合并分支遇到同个文件被修改，git会有如下标签，和其他开发人员讨论好留下哪部分代码就好了

~~~xml
 <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
    <<<<<<HEAD
        android:text="修复第二个bug"
  debug  ==========
    android:text="哈哈哈哈"
    >>>>>></HEAD>
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
~~~

大概就是这样的HEAD标签，第一个text代表你当前分支写的代码，=======下面代表debug分支中的代码。

## 最后

上传到线上代码仓库，比如coding或者guthub，上面可以查看到不同分支的文件修改情况，自己可以实际操作看看。记得！多分支上传最好明确上传的分支名

~~~java
git push origin [分支名]
~~~

