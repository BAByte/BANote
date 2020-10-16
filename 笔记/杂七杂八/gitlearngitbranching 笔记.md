[toc]



# 教程：https://www.liaoxuefeng.com/wiki/896043488029600/897884457270432

# 闯关：https://learngitbranching.js.org/

# learngitbranching 笔记

教程中的闯关有基于使用多少条命令来完成仓库管理的评分标准。由于可视化提交树的存在，导致我们处于上帝视角，我们感觉我们是整个仓库的管理员在管理所有分支，这会导致新手有过份追求：”完成目标所需要命令数量尽可能少“ 的现象出现。这不利于我们去理解现实中开发过程中处理仓库的场景。

建议：我们操作某个分支的时候想象自己是这个分支的维护者，以当前操作的分支为“主语”。不需要过分最求高级语法。

# 名言金句

+ Git跟踪并管理的是修改，而非文件。
+ 提交时记得用git status检查暂存区哦！

# 三区概念

+ 工作区：你正在用编辑器对仓库内的文件进行操作时的页面就是你的工作区，比如as使用的时候
+ 暂存区：使用了git add filename命令后 ，filename指定的文件或者文件夹会被加到暂存区
+ 本地仓库存储区（我不知道是不是这样叫）：git commit 命令后，暂存区的修改记录会提交到分支

上面三步如图所示：

**工作区 add到暂存区**

![img](https://www.liaoxuefeng.com/files/attachments/919020037470528/0)



**暂存区提交到分支**

![img] (https://www.liaoxuefeng.com/files/attachments/919020100829536/0)

# HEAD需要注意的地方

要明确的知道，HEAD到底指向哪里再进行操作。



# git checkout  <HashA> 

~~~
git checkout <HashA> 
~~~

+ 目的：可以更改HEAD的指向单独某个提交
+ ps: 以下内容默认为HEAD指向选定分支

# git checkout -b totallyNotMaster o/master

+ 目的:创建一个名为 `totallyNotMaster` 的分支，它跟踪远程分支 `o/master`。

# git checkout "分支名"

+ 目的：切换分支



# git checkout <filename>

# git checkout <SHA> -- <filename>

+ 目的：撤销对工作区文件的修改

+ 缺点: 如果没有add 或者commit过的文件进行checkout，又想要回到自己的修改，git做不到，但是可以通过idea的history功能找回

+ 例子：命令`git checkout -- readme.txt`意思就是，把`readme.txt`文件在工作区的修改全部撤销，这里有两种情况：

  一种是`readme.txt`自修改后还没有被放到暂存区，现在，撤销修改就回到和版本库一模一样的状态；

  一种是`readme.txt`已经添加到暂存区后，又作了修改，现在，撤销修改就回到添加到暂存区后的状态。

  总之，就是让这个文件回到最近一次`git commit`或`git add`时的状态。



# git commit --amend

~~~
git commit --amend
~~~

+ 目的：基于该提交更改该次提交内容，例如更改提交信息

# cherry-pick

~~~
git cherry-pick <HashA> 
git cherry-pick <HashA> <HashB>


git cherry-pick A..B （不包含A）
git cherry-pick A^..B 
~~~

+ 目的：从其他分支选择任意一个或者几个或者一段提交，合并到HEAD所指定的位置

# merge

git merge "要合并的分支名"

+ 目的：合并分支
+ 优点：会有各个分支的详细提交记录
+ 缺点：太详细了
+ 原理：

# rebase 

git rebase ‘要合并的分支名’

+ 目的：合并分支

+ 优点：合并后有干净漂亮的提交记录
+ 缺点：细枝末节的提交记录不清晰
+ 原理：

# git rebase -i HEAD~2

+ 目的: 对提交记录做个排序（当然你也可以删除某些提交）

# git reset (用于本地修改)

~~~
git reset HEAD~2 向上两个
git reset HEAD^ 向上1个
~~~



+ 目的: 通过把分支记录回退几个提交记录来实现撤销改动。你可以将这想象成“改写历史”。`git reset` 向上移动分支，原来指向的提交记录就跟从来没有提交过一样。

+ 缺点: 虽然在你的本地分支中使用 `git reset` 很方便，但是这种“改写历史”的方法对大家一起使用的远程分支是无效的哦！

+ 原理：该命令其实仅仅是吧HEAD的指向改变到你指定的commit，然后顺便把工作区的文件更新了

+ 注意：该命令使用后，再使用git log查看的话，就看不到回退之前的commit了，事实上commit还是存在的，你可以使用下面的git reflog命令查看

  

  # git reset HEAD <file>

  + 目的：可以把暂存区的修改撤销掉（unstage），重新放回工作区

# git revert （一般用于远程更改）

~~~
git revert HEAD^ 向上1个
~~~

+ 目的: 为了撤销更改并**分享**给别人，我们需要使用 `git revert`。

  

  场景：

  你已经执行**git push**,把你的修改推送到远程的仓库，现在你意识到之前推送的**commit**中有一个有些错误，想要撤销该commit。

  #### 方案：

  ```
  git revert <SHA>
  ```

  #### 原理：

  **git revert** 会创建一个新的**commit**，它和指定SHA对应的**commit**是相反的（或者说是反转的）。如果原型的commit是“物质”，那么新的**commit**就是“反物质”。

  任何从原来的commit里删除的内容都会再新的**commit**里被加回去，任何原来的**commit**中加入的内容都会在新的commit里被删除。

  这是Git中最安全、最基本的撤销场景，因为它并不会改变历史。所以你现在可以**git push**新的**“反转”commit**来抵消你错误提交的commit。



# git reflog

+ 目的: 在撤销“本地修改”之后再恢复,注意，这里只是查询，而且commit会被定时清理所以不一定会查得到
+ 注意：查到后用reset或者checkout处理

# 加参数的push

git push "仓库名字" "本地分支名字"

+ 目的: 无需考虑HEAD指向，把指定的分支名字推送到远程分支

# git fetch

git fetch

+ 目的:下载远程仓库中该分支的提交到 “本地的远程分支中”，但不合并

git pull = git fetch + git merge

git pull --rebase = git fetch + git rebase

# git fetch "仓库名" <source>:<destination>

+ 目的：把远程 source的分支下载到指定的destination
+ p s: 如果本地没有destination，会在本地自动创建destination分支



# git push "仓库名"  <source>:<destination>

+ 目的：把本地 source的分支上传到指定远程仓库的destination
+ ps: 如果指定远程仓库没有destination，会自动创建destination分支



# git pull "仓库名"  <source>:<destination>

目的: 先下载后合并

# 不指定source会怎样

+ fetch 在本地会新建分支
+ push会在远程删除分支



# git stash

+ 目的：把当前工作区的修改压入栈中，工作区恢复为暂存区或者最新的commit，用来不想提交，但是又想零时保存工作内容的情况

+ 扩展： git stash list 查看栈里面的内容,需要恢复一下，有两个办法：

  一是用`git stash apply`恢复，但是恢复后，stash内容并不删除，你需要用`git stash drop`来删除；

  另一种方式是用`git stash pop`，恢复的同时把stash内容也删了



# git branch -D <name>

+ 目的：强制删除分支

# git branch -d <name>

+ 目的：删除分支



# git tag v1.0

+ 目的：为当前HEAD指定的commit创建标签v1.0

# git tag v0.9 f52c633

+ 目的：为当前f52c633的commit指定的commit创建标签v1.0

- 命令`git push origin <tagname>`可以推送一个本地标签；
- 命令`git push origin --tags`可以推送全部未推送过的本地标签；
- 命令`git tag -d <tagname>`可以删除一个本地标签；
- 命令`git push origin :refs/tags/<tagname>`可以删除一个远程标签。

