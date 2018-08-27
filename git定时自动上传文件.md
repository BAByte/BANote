[TOC]

# git定时自动上传文件

## 先说两句

由于我用有道云笔记用的非常的不爽，于是我决定用该编译器，配合github来实现我的笔记存放仓库

## 取消每次push都要密码和账户

每次都要密码是因为你用了hppts协议传输的数据，但是如果你用SSH就不会了，这个在你从github复制clone连接的时候可以选择的，当你已经选了怎么办？也是可以改的。

教程地址：https://blog.csdn.net/whbing1471/article/details/52066688

## 自动传的脚本

写一个文本文件，记得更改路径，cd进入到你项目的路径就

~~~
@echo off
@title bat 交互执行git命令
D:
cd D:/git/test
git pull
git add .
git commit -m %date:~0,4%年%date:~5,2%月%date:~8,2%日
git push
~~~

pull命令是为了防止远程库和本地不一致导致提交失败

保存后改后缀为bat

## win10设置定时任务

小娜搜索任务计划，创建一个任务

教程：https://blog.csdn.net/u013788943/article/details/81629645
