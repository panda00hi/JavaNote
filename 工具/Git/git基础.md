>本来这方面不打算整理了，实际开发中几乎只用三个单词，pull、commit、push。哈哈。但是呢，偶尔遇到个问题，比如文件提交失败、提交缓存、撤销提交之类的，这些小问题，强迫症受不了。所以，有必要较为系统的整理一下，同时结合项目中实际的使用，总结下。

## 简介
Git是一个开源的分布式版本控制系统，用于敏捷高效地处理任何或小或大的项目。（据说，是linus大神，为了更好的管理他自己开发的Linux系统，顺便写了这个版本控制工具。在此膜拜以下）  

好处是进行项目管理的时候，对于改动，不用像我们传统的修改论文那样，创建无数个副本文档，然后修改完了，再找某一个文档的某一处修改，依然很费劲，整体上就是非常杂乱。而通过git效果是这样的（工作中项目的日志记录）：  
![git日志记录](https://upload-images.jianshu.io/upload_images/5353735-ef69585e92ddcae7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 集中式与分布式
集中式版本控制系统，版本库是存放在中央服务器的，而干活的时候，用的都是自己的电脑，所以要先从中央服务器取得最新的版本，然后开始干活，干完活了，再把自己的活推送给中央服务器。中央服务器就好比是一个图书馆，你要改一本书，必须先从图书馆借出来，然后回到家自己改，改完了，再放回图书馆。

另外，集中式版本控制系统有很大的局限性————必须联网使用。局域网由于带宽一般较大，影响不明显，但是像互联网情境下，网速慢时，非常影响工作效率。  

而对于分布式系统，分布式版本控制系统根本没有“中央服务器”，每个人的电脑上都是一个完整的版本库，这样，你工作的时候，就不需要联网了，因为版本库就在你自己的电脑上。既然每个人电脑上都有一个完整的版本库，那多个人如何协作呢？比方说你在自己电脑上改了文件A，你的同事也在他的电脑上改了文件A，这时，你们俩之间只需把各自的修改推送给对方，就可以互相看到对方的修改了。  

和集中式版本控制系统相比，分布式版本控制系统的安全性要高很多，因为每个人电脑里都有完整的版本库，某一个人的电脑坏掉了不要紧，随便从其他人那里复制一个就可以了。而集中式版本控制系统的中央服务器要是出了问题，所有人都没法干活了。

在实际使用分布式版本控制系统的时候，其实很少在两人之间的电脑上推送版本库的修改，因为可能你们俩不在一个局域网内，两台电脑互相访问不了，也可能今天你的同事病了，他的电脑压根没有开机。因此，分布式版本控制系统通常也有一台充当“中央服务器”的电脑，但这个服务器的作用仅仅是用来方便“交换”大家的修改，没有它大家也一样干活，只是交换修改不方便而已。  

![选自廖雪峰前辈博客的示意图](https://www.liaoxuefeng.com/files/attachments/918921562236160/0)

另外，git还有分支管理等巨大优势。后边会提到。

## git安装
### 具体安装略
注：git支持跨平台。尽量到Linux中，安装使用很简洁。windows的话，只是刚开始不那么别扭。

### 基本概念：务必理解

工作区、暂存区和版本库概念

- 工作区：就是你在电脑里能看到的目录。
- 暂存区：英文叫stage, 或index。一般存放在"git目录"下的index文件（.git/index）中，所以我们把暂存区有时也叫作索引（index）。
- 版本库：工作区有一个隐藏目录.git，这个不算工作区，而是Git的版本库。

### 创建版本库

版本库又名仓库，英文名repository，可以简单理解成一个目录，这个目录里面的所有文件都可以被Git管理起来，每个文件的修改、删除，Git都能跟踪，以便任何时刻都可以追踪历史，或者在将来某个时刻可以“还原”。  

首先，创建一个版本库。在合适目录，创建一个空目录learnGit，进入该目录下，运行git init进行初始化，初始化后会生成隐藏文件.git，使用ls -a可查看。

```
panda@pandaHi MINGW64 /d/learnGit
$ git init
Initialized empty Git repository in D:/learnGit/.git/

panda@pandaHi MINGW64 /d/learnGit (master)
$ ls -a
./  ../  .git/
```

**接下来，把文件添加到版本库**  
说明一下，所有的版本控制系统，只能跟踪文本文件的改动，能具体到某个字符的增删，比如TXT、网页、程序源代码文件等等（不包括word）。而对于图片、视频这一类二进制文件，可以进行管理，但是，只能够直观上看只是看到大小的变化。

我们现在编写一个readme.txt文件（是不是很熟悉的文件名，几乎所有的项目都会有这样一个文件，目的是引导访问者，类似说明书）：

```
Git is a version control system.
Git is free software.
```

然后把这个文件放到learnGit目录下(或子目录)，放其他位置，git无法找到。

1. git add，把文件添加到本地仓库
```
$ git add readme.txt
```
执行后，没有其他显示。

2. git commit，把文件提交到仓库：
```
$ git commit -m   "wrote a readme file"
[master (root-commit) addc646] wrote a readme file
 1 file changed, 2 insertions(+)
 create mode 100644 readme.txt

```

解释一下git commit 命令，-m后边输入的是本次提交的说明，方便自己和团队成员了解改动的情况。  

另外，add每次可以提交多个文件，可以使用多此。然后，commit一起提交。

```
$ git add file1.txt
$ git add file2.txt file3.txt
$ git commit -m "add 3 files."
```

## git使用
### 添加、提交文件
- git status 
- git diff
- git add
- git commit -m ""

对readme.txt文件进行修改，体会git版本控制的效果。
将文件内容改为：

```
Git is a distributed version control system.
Git is free software.
```

运行git status查看状态：  

```
$ git status
On branch master
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

        modified:   readme.txt

no changes added to commit (use "git add" and/or "git commit -a")
```
说明，readme.txt被修改过了，但还没有准备提交的修改。  
通过git diff命令，可以查看具体修改的地方。

```
$ git diff
diff --git a/readme.txt b/readme.txt
index d8036c1..013b5bc 100644
--- a/readme.txt
+++ b/readme.txt
@@ -1,2 +1,2 @@
-Git is a version control system.
+Git is a distributed version control system.
 Git is free software.
\ No newline at end of file
```
结果正好表明了，增加了单词distributed单词。  
当确认了修改的内容后，提交修改和提交新文件步骤一样，先git add， 执行git commit，使用git status查看下状态。

```
panda@pandaHi MINGW64 /d/learnGit (master)
$ git add readme.txt

panda@pandaHi MINGW64 /d/learnGit (master)
$ git status
On branch master
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)

        modified:   readme.txt
```
提交：  
```
$ git commit -m "add word distributed"
[master 9f023f4] add word distributed
 1 file changed, 1 insertion(+), 1 deletion(-)

```
提交后，查看状态：
```
$ git status
On branch master
nothing to commit, working tree clean
```
结果表示，目前没有需求提交的修改，工作目录是干净的。

再修改文件内容，并提交一下。
文件内容为：
```
Git is a distributed version control system.
Git is free software distributed under the GPL.
```

执行命令：

```
$ git commit -m "add words distributed under the GPL."
```
所以，目前有三个版本，分别是：

- 版本1:wrote a readme file
- 版本2:add word distributed
- 版本3:add words distributed under the GPL

在实际工作中，这种修改版本将会是巨量的，通过git中的git log进行日志管理。

```
$ git log
commit f0ec9c4c7c00f7762d5ddcebfc14a43f29ce6e5f (HEAD -> master)
Author: pandahi
Date:   Wed Jul 31 16:51:45 2019 +0800

    add words distributed under the GPL.

commit 9f023f46af68149ecbc0ee58300c9be83ffd3617
Author: pandahi
Date:   Wed Jul 31 16:38:37 2019 +0800

    add word distributed

commit addc646b8f90a282de97aa1b3c7a1203da823f75
Author: pandahi
Date:   Wed Jul 31 16:19:37 2019 +0800

    wrote a readme file

```
可以看到，提交了三次，以及分别的备注。如果在idea或其他可视化工具中，开发者提交的记录能够连成带有节点的时间线。  

### 回退操作

- HEAD指向的版本就是当前版本，因此，Git允许我们在版本的历史之间穿梭，使用命令git reset --hard commit_id。  

- 穿梭前，用git log可以查看提交历史，以便确定要回退到哪个版本。

- 要重返未来，用git reflog查看命令历史，以便确定要回到未来的哪个版本。

当前版本回退到上个版本，即add word distributed。使用git reset 命令。

首先，Git必须知道当前版本是哪个版本，在Git中，用HEAD表示当前版本，比如我最新的提交f0ec9c...，上一个版本就是HEAD^，上上一个版本就是HEAD^^，当然往上100个版本写100个^比较容易数不过来，所以写成HEAD~100。
```
$ git reset --hard HEAD^
HEAD is now at 9f023f4 add word distributed
```
可见，已经处在上个版本，add word distributed 了。
查看本地的readme.txt 文件，发现内容也已经回退了。
（此处--hard参数，是git reset三种机制之一，与工作区、暂存区、版本库的概念有关，体现了不力度的回退能力）  

此时查看下版本库状态：

```
$ git log
commit 9f023f46af68149ecbc0ee58300c9be83ffd3617 (HEAD -> master)
Author: pandahi
Date:   Wed Jul 31 16:38:37 2019 +0800

    add word distributed

commit addc646b8f90a282de97aa1b3c7a1203da823f75
Author: pandahi
Date:   Wed Jul 31 16:19:37 2019 +0800

    wrote a readme file

```

此时，发现最新版本已经没有了。如果想要未来的版本，也可以实现，通过之前看到的commit id。如，此处通过看前边的记录，发现commit id为f0ec9……(这里的id只需前几位就可以)

```
$ git reset --hard f0ec9
HEAD is now at f0ec9c4 add words distributed under the GPL.
```
**回退原理**  
回退非常快是如何实现的，因为Git在内部有个指向当前版本的HEAD指针，当你回退版本的时候，Git仅仅是把HEAD从指向append GPL：

```
┌────┐
│HEAD│
└────┘
   │
   └──> ○ add words distributed under the GPL
        │
        ○ add word distributed
        │
        ○ wrote a readme file
```
改为指向add distributed：

```
┌────┐
│HEAD│
└────┘
   │
   │    ○ add words distributed under the GPL
   │    │
   └──> ○ add word distributed
        │
        ○ wrote a readme file
```
然后顺便把工作区的文件更新了。所以你让HEAD指向哪个版本号，你就把当前版本定位在哪。

如果意外关机等，可通过git reflog查看命令记录：

```
$ git reflog
f0ec9c4 (HEAD -> master) HEAD@{0}: reset: moving to f0ec9
9f023f4 HEAD@{1}: reset: moving to HEAD^
f0ec9c4 (HEAD -> master) HEAD@{2}: commit: add words distributed under the GPL.
9f023f4 HEAD@{3}: commit: add word distributed
addc646 HEAD@{4}: commit (initial): wrote a readme file

```


### 工作区和暂存区


