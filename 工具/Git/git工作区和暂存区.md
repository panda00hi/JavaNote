## 工作区和暂存区

我们电脑本地创建的存放要管理的文件夹就是工作区。如，learnGit文件夹就是。

### 版本库(Repository)

工作区有一个隐藏目录.git，这个不算工作区，而是Git的版本库。

Git的版本库里存了很多东西，其中最重要的就是称为stage（或者叫index）的暂存区，还有Git为我们自动创建的第一个分支master，以及指向master的一个指针叫HEAD。

![指针示意图](https://www.liaoxuefeng.com/files/attachments/919020037470528/0)

我们把文件往Git版本库里添加的时候，是分两步执行的：

第一步是用git add把文件添加进去，实际上就是把文件修改添加到暂存区；

第二步是用git commit提交更改，实际上就是把暂存区的所有内容提交到当前分支。

因为我们创建Git版本库时，Git自动为我们创建了唯一一个master分支，所以，现在，git commit就是往master分支上提交更改。

你可以简单理解为，需要提交的文件修改通通放到暂存区，然后，一次性提交暂存区的所有修改。

注意：多处修改，记得修改完要先通过git add 提交到暂存区，然后才git commit。即，第一次修改 -> git add -> 第二次修改 -> git add -> git commit  
比如：第一次修改 -> git add -> 第二次修改 -> git commit。那么git commit 只负责把暂存区的修改提交了，也就是第一次的修改被提交了，第二次的修改不会被提交。  
提交后，用git diff HEAD -- readme.txt 命令可以查看工作区和版本库里面最新版本的区别。

### 撤销修改 
主要有这三个场景：

- 场景1：当你改乱了工作区某个文件的内容，想直接丢弃工作区的修改时，用命令git checkout -- file。

- 场景2：当你不但改乱了工作区某个文件的内容，还添加到了暂存区时，想丢弃修改，分两步，第一步用命令git reset HEAD <file>，就回到了场景1，第二步按场景1操作。

- 场景3：已经提交了不合适的修改到版本库时，想要撤销本次提交，参考版本回退一节，不过前提是没有推送到远程库。


【场景1】  
修改了文档，但是后来想恢复修改之前的状态：

```
Git is a distributed version control system.
Git is free software distributed under the GPL.
tracks changes of files.
tracks changes.
this is error line.
```
使用git status查看状态：

```
$ git status
On branch master
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)

        modified:   readme.txt

Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

        modified:   readme.txt
```
提示可以使用<git checkout -- file>丢弃工作区的修改：
```
$ git checkout -- readme.txt
```

cat命令查看新加的一行已丢弃。

```
$ cat readme.txt
Git is a distributed version control system.
Git is free software distributed under the GPL.
tracks changes of files.
tracks changes.
```
命令git checkout -- readme.txt意思就是，把readme.txt文件在工作区的修改全部撤销，这里有两种情况：

一种是readme.txt自修改后还没有被放到暂存区，现在，撤销修改就回到和版本库一模一样的状态；

一种是readme.txt已经添加到暂存区后，又作了修改，现在，撤销修改就回到添加到暂存区后的状态。

总之，就是让这个文件回到最近一次git commit或git add时的状态。  

【场景2】  
修改了文件内容，并使用<git add>添加到了暂存区，进行commit之前相应撤销。

```
$ cat readme.txt
Git is a distributed version control system.
Git is free software distributed under the GPL.
tracks changes of files.
tracks changes.
[new line to test 'reset']
```
查看状态：

```
$ git status
On branch master
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)

        modified:   readme.txt
```
提示使用命令git reset HEAD <file>可以把暂存区的修改撤销掉（unstage），重新放回工作区(git reset命令既可以回退版本，也可以把暂存区的修改回退到工作区。当我们用HEAD时，表示最新的版本)：

```
$ git reset head readme.txt
Unstaged changes after reset:
M       readme.txt
```
再次查看状态，现在暂存区是干净的，工作区有修改：

```
$ git status
On branch master
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

        modified:   readme.txt

no changes added to commit (use "git add" and/or "git commit -a")

```
此时可以使用checkout丢弃工作区修改：

```
$ git checkout readme.txt
Updated 1 path from the index

panda@panda00hi MINGW64 /d/learnGit (master)
$ git status
On branch master
nothing to commit, working tree clean

```
已经彻底撤销。

【场景3】如果已经commit了，那么需要参考回退操作。

### 删除文件
先添加一个新文件test.txt。git add -> git commit  

使用rm命令删除。这时，Git会检测到工作区和版本库不一致。

```
$ rm test.txt

panda@panda00hi MINGW64 /d/learnGit (master)
$ git status
On branch master
Changes not staged for commit:
  (use "git add/rm <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

        deleted:    test.txt

no changes added to commit (use "git add" and/or "git commit -a")

```

此时可以进行两种操作:
1. 确实要从版本库中删除该文件，用命令<git rm>删掉，并且提交<git commit>。

```
$ git rm test.txt
rm 'test.txt'

panda@panda00hi MINGW64 /d/learnGit (master)
$ git commit -m "remove test.txt"
[master eb6331e] remove test.txt
 1 file changed, 1 deletion(-)
 delete mode 100644 test.txt

```

2. 误删除，因为版本库里还有，所以可以把误删的文件恢复到最新版本。使用<git checkout -- file>命令。 

```
$ git checkout -- test.txt
```

git checkout其实是用版本库里的版本替换工作区的版本，无论工作区是修改还是删除，都可以“一键还原”。  

**==注意==**：从来没有被添加到版本库就被删除的文件，是无法恢复的！
