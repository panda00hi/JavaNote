在分支管理之前，需要先了解远程仓库的基本知识。
## 远程仓库

**添加仓库**  
 
Github创建仓库、关联本地仓库等  
具体过程参考链接：  
https://www.liaoxuefeng.com/wiki/896043488029600/898732864121440

- 要关联一个远程库，使用命令git remote add origin git@server-name:path/repo-name.git；

- 关联后，使用命令git push -u origin master第一次推送master分支的所有内容；

- 此后，每次本地提交后，只要有必要，就可以使用命令git push origin master推送最新修改；

**从远程仓库克隆**  

```
git clone 地址链接
```

# 分支管理

分支在实际中的意义：假设你准备开发一个新功能，但是需要两周才能完成，第一周你写了50%的代码，如果立刻提交，由于代码还没写完，不完整的代码库会导致别人不能干活了。如果等代码全部写完再一次提交，又存在丢失每天进度的巨大风险。

现在有了分支，就不用怕了。你创建了一个属于你自己的分支，别人看不到，还继续在原来的分支上正常工作，而你在自己的分支上干活，想提交就提交，直到开发完毕后，再一次性合并到原来的分支上，这样，既安全，又不影响别人工作。

## 创建与合并分支
**相关命令**
```
查看分支：git branch

创建分支：git branch <name>

切换分支：git checkout <name>

创建+切换分支：git checkout -b <name>

合并某分支到当前分支：git merge <name>

删除分支：git branch -d <name>
```

每次提交，Git都把它们串成一条时间线，这条时间线就是一个分支。截止到目前，只有一条时间线，在Git里，这个分支叫主分支，即master分支。HEAD严格来说不是指向提交，而是指向master，master才是指向提交的，所以，HEAD指向的就是当前分支。  
一开始的时候，master分支是一条线，Git用master指向最新的提交，再用HEAD指向master，就能确定当前分支，以及当前分支的提交点：  

![image](https://static.liaoxuefeng.com/files/attachments/919022325462368/0)  

每次提交，master分支都会向前移动一步，这样，随着你不断提交，master分支的线也越来越长。

当我们创建新的分支，例如dev时，Git新建了一个指针叫dev，指向master相同的提交，再把HEAD指向dev，就表示当前分支在dev上：

![image](https://static.liaoxuefeng.com/files/attachments/919022363210080/0)  

从现在开始，对工作区的修改和提交就是针对dev分支了，比如新提交一次后，dev指针往前移动一步，而master指针不变：  

![image](https://static.liaoxuefeng.com/files/attachments/919022387118368/0)  

假如我们在dev上的工作完成了，就可以把dev合并到master上。Git怎么合并呢？最简单的方法，就是直接把master指向dev的当前提交，就完成了合并：  

![image](https://static.liaoxuefeng.com/files/attachments/919022412005504/0)  

所以Git合并分支也很快！就改改指针，工作区内容也不变！

合并完分支后，甚至可以删除dev分支。删除dev分支就是把dev指针给删掉，删掉后，我们就剩下了一条master分支：

![image](https://static.liaoxuefeng.com/files/attachments/919022479428512/0)  

示例：  

首先，我们创建dev分支，然后切换到dev分支：

```
$ git checkout -b dev
Switched to a new branch 'dev'
```
git checkout命令加上-b参数表示创建并切换，相当于以下两条命令：
```
$ git branch dev
$ git checkout dev
Switched to branch 'dev'
```
然后，用git branch命令查看当前分支：

```
$ git branch
* dev
  master
```
git branch命令会列出所有分支，当前分支前面会标一个*号。  

然后，我们就可以在dev分支上正常提交，比如对readme.txt做个修改，加上一行：

```
Creating a new branch is quick.
```
然后提交：
```
$ git add readme.txt 
$ git commit -m "branch test"
[dev b17d20e] branch test
 1 file changed, 1 insertion(+)
```

现在，dev分支的工作完成，我们就可以切换回master分支：

```
$ git checkout master
Switched to branch 'master'

```
切换回master分支后，再查看一个readme.txt文件，刚才添加的内容不见了！因为那个提交是在dev分支上，而master分支此刻的提交点并没有变： 
![image](https://static.liaoxuefeng.com/files/attachments/919022533080576/0)  

现在，我们把dev分支的工作成果合并到master分支上：

```
$ git merge dev
Updating d46f35e..b17d20e
Fast-forward
 readme.txt | 1 +
 1 file changed, 1 insertion(+)
```
git merge命令用于合并指定分支到当前分支。合并后，再查看readme.txt的内容，就可以看到，和dev分支的最新提交是完全一样的。

注意到上面的Fast-forward信息，Git告诉我们，这次合并是“快进模式”，也就是直接把master指向dev的当前提交，所以合并速度非常快。

当然，也不是每次合并都能Fast-forward，我们后面会讲其他方式的合并。

合并完成后，就可以放心地删除dev分支了：

```
$ git branch -d dev
Deleted branch dev (was b17d20e).
```
删除后，查看branch，就只剩下master分支了：
```
$ git branch
* master
```
因为创建、合并和删除分支非常快，所以Git鼓励你使用分支完成某个任务，合并后再删掉分支，这和直接在master分支上工作效果是一样的，但过程更安全。

## 解决冲突
在提交代码时，多个分支代码合并时，可能会遇到冲突之类的。  
创建新的分支：

```
$ git checkout -b feature1
Switched to a new branch 'feature1'
```
修改readme.txt最后一行，改为：
```
Creating a new branch is quick AND simple.
```
在feature1分支上提交：
```
$ git add readme.txt

$ git commit -m "AND simple"
[feature1 14096d0] AND simple
 1 file changed, 1 insertion(+), 1 deletion(-)
```
切换到master分支：
```
$ git checkout master
Switched to branch 'master'
Your branch is ahead of 'origin/master' by 1 commit.
  (use "git push" to publish your local commits)
```
Git还会自动提示我们当前master分支比远程的master分支要超前1个提交。

在master分支上把readme.txt文件的最后一行改为：
```
Creating a new branch is quick & simple.
```
现在，master分支和feature1分支各自都分别有新的提交，变成了这样：

![image](https://static.liaoxuefeng.com/files/attachments/919023000423040/0)

这种情况下，Git无法执行“快速合并”，只能试图把各自的修改合并起来，但这种合并就会有冲突：

```
$ git merge feature1
Auto-merging readme.txt
CONFLICT (content): Merge conflict in readme.txt
Automatic merge failed; fix conflicts and then commit the result.
```
Git告诉我们，readme.txt文件存在冲突，必须手动解决冲突后再提交。git status也可以告诉我们冲突的文件：

```
$ git status
On branch master
Your branch is ahead of 'origin/master' by 2 commits.
  (use "git push" to publish your local commits)

You have unmerged paths.
  (fix conflicts and run "git commit")
  (use "git merge --abort" to abort the merge)

Unmerged paths:
  (use "git add <file>..." to mark resolution)

	both modified:   readme.txt

no changes added to commit (use "git add" and/or "git commit -a")
```
也可以直接查看readme.txt的内容：

```
Git is a distributed version control system.
Git is free software distributed under the GPL.
Git has a mutable index called stage.
Git tracks changes of files.
<<<<<<< HEAD
Creating a new branch is quick & simple.
=======
Creating a new branch is quick AND simple.
>>>>>>> feature1
```
Git用<<<<<<<，=======，>>>>>>>标记出不同分支的内容，我们修改如下后保存：

```
Creating a new branch is quick and simple.
```
再提交：

```
$ git add readme.txt 
$ git commit -m "conflict fixed"
[master cf810e4] conflict fixed
```
现在，master分支和feature1分支变成了下图所示：  
```
https://static.liaoxuefeng.com/files/attachments/919023031831104/0
```
用**git log --graph**也可以看到分支的合并情况：

```
$ git log --graph --pretty=oneline --abbrev-commit
*   cf810e4 (HEAD -> master) conflict fixed
|\  
| * 14096d0 (feature1) AND simple
* | 5dc6824 & simple
|/  
* b17d20e branch test
* d46f35e (origin/master) remove test.txt
* b84166e add test.txt
* 519219b git tracks changes
* e43a48b understand how stage works
* 1094adb append GPL
* e475afc add distributed
* eaadf4e wrote a readme file
```

最后，删除feature1分支：

```
$ git branch -d feature1
Deleted branch feature1 (was 14096d0).
```

## 分支管理策略
通常合并分支时，如果可能，git会用Fast forward模式，但这种模式下，删除分支后，会丢掉分支信息。

如果要强制禁用Fast forward模式，Git就会在merge时生成一个新的commit，这样，从分支历史上就可以看出分支信息。

通过实例，体验下--no-ff方式的git merge  

首先，创建并切换dev分支：
```
$ git checkout -b dev
Switched to a new branch 'dev'
```
修改readme.txt文件，并提交一个新的commit：

```
$ git add readme.txt 
$ git commit -m "add merge"
[dev f52c633] add merge
 1 file changed, 1 insertion(+)
```
切换回master：
```
$ git checkout master
Switched to branch 'master'

```
准备合并dev分支，请注意--no-ff参数，表示禁用Fast forward：
```
$ git merge --no-ff -m "merge with no-ff" dev
Merge made by the 'recursive' strategy.
 readme.txt | 1 +
 1 file changed, 1 insertion(+)
```
因为本次合并要创建一个新的commit，所以加上-m参数，把commit描述写进去。

合并后，我们用git log看看分支历史：


```
$ git log --graph --pretty=oneline --abbrev-commit
*   e1e9c68 (HEAD -> master) merge with no-ff
|\  
| * f52c633 (dev) add merge
|/  
*   cf810e4 conflict fixed
```
可以看到，不使用Fast forward模式，merge后就像这样：

![image](https://static.liaoxuefeng.com/files/attachments/919023225142304/0)

**分支策略小结：**  

在实际开发中，我们应该按照几个基本原则进行分支管理：

首先，master分支应该是非常稳定的，也就是仅用来发布新版本，平时不能在上面干活；

那在哪干活呢？干活都在dev分支上，也就是说，dev分支是不稳定的，到某个时候，比如1.0版本发布时，再把dev分支合并到master上，在master分支发布1.0版本；

你和你的小伙伴们每个人都在dev分支上干活，每个人都有自己的分支，时不时地往dev分支上合并就可以了。

合并分支时，加上--no-ff参数就可以用普通模式合并，合并后的历史有分支，能看出来曾经做过合并，而fast forward合并就看不出来曾经做过合并。

所以，团队合作的分支看起来就像这样：

![image](https://static.liaoxuefeng.com/files/attachments/919023260793600/0)


## Bug分支

软件开发中，bug就像家常便饭一样。有了bug就需要修复，在Git中，由于分支是如此的强大，所以，每个bug都可以通过一个新的临时分支来修复，修复后，合并分支，然后将临时分支删除。

当你接到一个修复一个代号101的bug的任务时，很自然地，你想创建一个分支issue-101来修复它，但是，等等，当前正在dev上进行的工作还没有提交：

```
$ git status
On branch dev
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)

	new file:   hello.py

Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	modified:   readme.txt
```
并不是你不想提交，而是工作只进行到一半，还没法提交，预计完成还需1天时间。但是，必须在两个小时内修复该bug，怎么办？

幸好，Git还提供了一个stash功能，可以把当前工作现场“储藏”起来，等以后恢复现场后继续工作：  

```
$ git stash
Saved working directory and index state WIP on dev: f52c633 add merge
```

现在，用git status查看工作区，就是干净的（除非有没有被Git管理的文件），因此可以放心地创建分支来修复bug。

首先确定要在哪个分支上修复bug，假定需要在master分支上修复，就从master创建临时分支：

```
$ git checkout master
Switched to branch 'master'
Your branch is ahead of 'origin/master' by 6 commits.
  (use "git push" to publish your local commits)

$ git checkout -b issue-101
Switched to a new branch 'issue-101'

```
现在修复bug，需要把“Git is free software ...”改为“Git is a free software ...”，然后提交：

```
$ git add readme.txt 
$ git commit -m "fix bug 101"
[issue-101 4c805e2] fix bug 101
 1 file changed, 1 insertion(+), 1 deletion(-)
```
修复完成后，切换到master分支，并完成合并，最后删除issue-101分支：
```
$ git checkout master
Switched to branch 'master'
Your branch is ahead of 'origin/master' by 6 commits.
  (use "git push" to publish your local commits)

$ git merge --no-ff -m "merged bug fix 101" issue-101
Merge made by the 'recursive' strategy.
 readme.txt | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
```
现在，是时候接着回到dev分支干活了。
```
$ git checkout dev
Switched to branch 'dev'

$ git status
On branch dev
nothing to commit, working tree clean
```
工作区是干净的，刚才的工作现场存到哪去了？用git stash list命令看看：


```
$ git stash list
stash@{0}: WIP on dev: f52c633 add merge
```

工作现场还在，Git把stash内容存在某个地方了，但是需要恢复一下，有两个办法：  

一是用git stash apply恢复，但是恢复后，stash内容并不删除，你需要用git stash drop来删除；

另一种方式是用git stash pop，恢复的同时把stash内容也删了：
```
$ git stash pop
On branch dev
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)

	new file:   hello.py

Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	modified:   readme.txt

Dropped refs/stash@{0} (5d677e2ee266f39ea296182fb2354265b91b3b2a)
```
再用git stash list查看，就看不到任何stash内容了：  

```
$ git stash list
```
你可以多次stash，恢复的时候，先用git stash list查看，然后恢复指定的stash，用命令：  

```
$ git stash apply stash@{0}
```
在master分支上修复了bug后，我们要想一想，dev分支是早期从master分支分出来的，所以，这个bug其实在当前dev分支上也存在。  

那怎么在dev分支上修复同样的bug？  

同样的bug，要在dev上修复，我们只需要把4c805e2 fix bug 101这个提交所做的修改“复制”到dev分支。注意：我们只想复制4c805e2 fix bug 101这个提交所做的修改，并不是把整个master分支merge过来。

为了方便操作，Git专门提供了一个cherry-pick命令，让我们能复制一个特定的提交到当前分支：
```
$ git branch
* dev
  master
$ git cherry-pick 4c805e2
[master 1d4b803] fix bug 101
 1 file changed, 1 insertion(+), 1 deletion(-)
```
Git自动给dev分支做了一次提交，注意这次提交的commit是1d4b803，它并不同于master的4c805e2，因为这两个commit只是改动相同，但确实是两个不同的commit。用git cherry-pick，我们就不需要在dev分支上手动再把修bug的过程重复一遍。

【小结】  
修复bug时，我们会通过创建新的bug分支进行修复，然后合并，最后删除；

当手头工作没有完成时，先把工作现场git stash一下，然后去修复bug，修复后，再git stash pop，回到工作现场；

在master分支上修复的bug，想要合并到当前dev分支，可以用git cherry-pick <commit>命令，把bug提交的修改“复制”到当前分支，避免重复劳动。

## Feature分支
在实际开发中，会伴随着新功能的加入，有很多是实验性的功能，未来在正式版中不一定会保留。因此，为了避免把主分支搞乱，一般情况每添加一个新功能，就新建一个分支，在改分支进行日常开发，完成，根据情况进行合并等操作，最后删除该分支即可。

示例：开发代号为Vulcan的新功能。  
新建并切换分支：  

```
$ git checkout -b feature-vulcan
Switched to a new branch 'feature-vulcan'
```
开发完成后：

```
$ git add vulcan.py

$ git status
On branch feature-vulcan
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)

	new file:   vulcan.py

$ git commit -m "add feature vulcan"
[feature-vulcan 287773e] add feature vulcan
 1 file changed, 2 insertions(+)
 create mode 100644 vulcan.py
```
切回dev，准备合并：  

```
$ git checkout dev
```
一切顺利的话，feature分支和bug分支是类似的，合并，然后删除。  
但是，此时，由于某种原因，该功能取消发布，包含源码的分支需要就地销毁。  

```
$ git branch -d feature-vulcan
error: The branch 'feature-vulcan' is not fully merged.
If you are sure you want to delete it, run 'git branch -D feature-vulcan'.
```
销毁失败。Git友情提醒，feature-vulcan分支还没有被合并，如果删除，将丢失掉修改，如果要强行删除，需要使用大写的-D参数。  

强行删除：  

```
$ git branch -D feature-vulcan
Deleted branch feature-vulcan (was 287773e).
```

## 多人协作

从远程仓库克隆时，实际上Git自动把本地的master分支和远程的master分支对应起来了，并且，远程仓库的默认名称是origin。  

查看远程库的信息，用**git remote**或者查看更详细的信息**git remote -v**

```
$ git remote
origin

$ git remote -v
origin  https://github.com/panda-Hi/learnGit.git (fetch)
origin  https://github.com/panda-Hi/learnGit.git (push)
```
### 推送分支

推送分支，就是把该分支上的所有本地提交推送到远程库。推送时，要指定本地分支，这样，Git就会把该分支推送到远程库对应的远程分支上：  

```
$ git push origin master
```
如果要推送其他分支，比如dev，就改成：  

```
$ git push origin dev
```

但是，并不是一定要把本地分支往远程推送，那么，哪些分支需要推送，哪些不需要呢？  


- master分支是主分支，因此要时刻与远程同步；

- dev分支是开发分支，团队所有成员都需要在上面工作，所以也需要与远程同步；

- bug分支只用于在本地修复bug，就没必要推到远程了，除非老板要看看你每周到底修复了几个bug；

- feature分支是否推到远程，取决于你是否和你的小伙伴合作在上面开发。

### 抓取分支

多人协作时，大家都会往master和dev分支上推送各自的修改。

模拟团队同事，可以在另一台电脑（注意要把SSH Key添加到GitHub）或者同一台电脑的另一个目录下克隆：

```
$ git clone git@github.com:panda-Hi/learnGit.git
Cloning into 'learngit'...
remote: Counting objects: 40, done.
remote: Compressing objects: 100% (21/21), done.
remote: Total 40 (delta 14), reused 40 (delta 14), pack-reused 0
Receiving objects: 100% (40/40), done.
Resolving deltas: 100% (14/14), done.
```

当这位同事从远程库clone时，默认情况下，只能看到本地的master分支。可以用git branch命令：
```
$ git branch
* master
```
现在他需要在dev分支上开发，就必须创建远程origin的分支到本地。  
创建本地dev分支：  
```
$ git checkout -b dev origin/dev
```
现在他可以在dev上继续修改，并把dev分支push到远程。

```
$ git add env.txt

$ git commit -m "add env"
[dev 7a5e5dd] add env
 1 file changed, 1 insertion(+)
 create mode 100644 env.txt

$ git push origin dev
Counting objects: 3, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (2/2), done.
Writing objects: 100% (3/3), 308 bytes | 308.00 KiB/s, done.
Total 3 (delta 0), reused 0 (delta 0)
To github.com:michaelliao/learngit.git
   f52c633..7a5e5dd  dev -> dev
```

这位同事已经向origin/dev分支推送了他的提交，而碰巧我们也对同样的文件进行了修改，共试图推送。

```
$ cat env.txt
env

$ git add env.txt

$ git commit -m "add new env"
[dev 7bd91f1] add new env
 1 file changed, 1 insertion(+)
 create mode 100644 env.txt

$ git push origin dev
To github.com:michaelliao/learngit.git
 ! [rejected]        dev -> dev (non-fast-forward)
error: failed to push some refs to 'git@github.com:panda-Hi/learnGit.git'
hint: Updates were rejected because the tip of your current branch is behind
hint: its remote counterpart. Integrate the remote changes (e.g.
hint: 'git pull ...') before pushing again.
hint: See the 'Note about fast-forwards' in 'git push --help' for details.
```
提示推送失败，因为存在冲突。  
解决办法：先用git pull。把最新的提交从origin/dev抓取下来。然后，然后，在本地合并，解决冲突，再推送：
```
$ git pull
There is no tracking information for the current branch.
Please specify which branch you want to merge with.
See git-pull(1) for details.

    git pull <remote> <branch>

If you wish to set tracking information for this branch you can do so with:

    git branch --set-upstream-to=origin/<branch> dev
```

git pull也失败了，原因是没有指定本地dev分支与远程origin/dev分支的链接，根据提示，设置dev和origin/dev的链接

```
$ git branch --set-upstream-to=origin/dev dev
Branch 'dev' set up to track remote branch 'dev' from 'origin'.
```
再次pull：

```
$ git pull
Auto-merging env.txt
CONFLICT (add/add): Merge conflict in env.txt
Automatic merge failed; fix conflicts and then commit the result.
```
这回git pull成功，但是合并有冲突，需要手动解决，解决的方法和分支管理中的解决冲突完全一样。解决后，提交， 再push

```
$ git commit -m "fix env conflict"
[dev 57c53ab] fix env conflict

$ git push origin dev
Counting objects: 6, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (4/4), done.
Writing objects: 100% (6/6), 621 bytes | 621.00 KiB/s, done.
Total 6 (delta 0), reused 0 (delta 0)
To github.com:michaelliao/learngit.git
   7a5e5dd..57c53ab  dev -> dev
```

因此多人协作的工作模式，通常是：
1. 可以试图用git push origin <branch-name>推送自己的修改；
2. 如果推送失败，则因为远程分支比你的本地更新，需要先用git pull试图合并；
3. 如果合并有冲突，则解决冲突，并在本地提交；
4. 没有冲突或者解决掉冲突后，再用git push origin <branch-name>推送就能成功！

如果git pull提示no tracking information，则说明本地分支和远程分支的链接关系没有创建，用命令**git branch --set-upstream-to <branch-name> origin/<branch-name>**

### Rebase

作用：  
**1. rebase操作可以把本地未push的分叉提交历史整理成直线；**

**2. rebase的目的是使得我们在查看历史提交的变化时更容易，因为分叉的提交需要三方对比。**

多人在同一个分支上协作时，很容易出现冲突。即使没有冲突，后push的人不得不先pull，在本地合并，然后才能push成功。

每次合并再push后，分支变成了这样：  

```
$ git log --graph --pretty=oneline --abbrev-commit
* d1be385 (HEAD -> master, origin/master) init hello
*   e5e69f1 Merge branch 'dev'
|\  
| *   57c53ab (origin/dev, dev) fix env conflict
| |\  
| | * 7a5e5dd add env
| * | 7bd91f1 add new env
| |/  
* |   12a631b merged bug fix 101
|\ \  
| * | 4c805e2 fix bug 101
|/ /  
* |   e1e9c68 merge with no-ff
|\ \  
| |/  
| * f52c633 add merge
|/  
*   cf810e4 conflict fixed
```

在和远程分支同步后，我们对hello.py这个文件做了两次提交。用git log命令看看：  

```
$ git log --graph --pretty=oneline --abbrev-commit
* 582d922 (HEAD -> master) add author
* 8875536 add comment
* d1be385 (origin/master) init hello
*   e5e69f1 Merge branch 'dev'
|\  
| *   57c53ab (origin/dev, dev) fix env conflict
| |\  
| | * 7a5e5dd add env
| * | 7bd91f1 add new env
```

注意到Git用(HEAD -> master)和(origin/master)标识出当前分支的HEAD和远程origin的位置分别是582d922 add author和d1be385 init hello，本地分支比远程分支快两个提交。

现在我们尝试推送本地分支：  
提示失败，可能是有人更早地推送到了远程分支。所以，先pull一下。

```
$ git pull
remote: Counting objects: 3, done.
remote: Compressing objects: 100% (1/1), done.
remote: Total 3 (delta 1), reused 3 (delta 1), pack-reused 0
Unpacking objects: 100% (3/3), done.
From github.com:michaelliao/learngit
   d1be385..f005ed4  master     -> origin/master
 * [new tag]         v1.0       -> v1.0
Auto-merging hello.py
Merge made by the 'recursive' strategy.
 hello.py | 1 +
 1 file changed, 1 insertion(+)
```
再用git status看看状态：

```
$ git status
On branch master
Your branch is ahead of 'origin/master' by 3 commits.
  (use "git push" to publish your local commits)

nothing to commit, working tree clean
```
加上刚才合并的提交，现在我们本地分支比远程分支超前3个提交。

用git log看看：
```
$ git log --graph --pretty=oneline --abbrev-commit
*   e0ea545 (HEAD -> master) Merge branch 'master' of github.com:michaelliao/learngit
|\  
| * f005ed4 (origin/master) set exit=1
* | 582d922 add author
* | 8875536 add comment
|/  
* d1be385 init hello
```
发现提交历史分叉了，会显得很杂乱，时间线本该是一条线的。  

使用git rebase

```
$ git rebase
First, rewinding head to replay your work on top of it...
Applying: add comment
Using index info to reconstruct a base tree...
M	hello.py
Falling back to patching base and 3-way merge...
Auto-merging hello.py
Applying: add author
Using index info to reconstruct a base tree...
M	hello.py
Falling back to patching base and 3-way merge...
Auto-merging hello.py
```
再次使用git log查看提交历史：

```
$ git log --graph --pretty=oneline --abbrev-commit
* 7e61ed4 (HEAD -> master) add author
* 3611cfe add comment
* f005ed4 (origin/master) set exit=1
* d1be385 init hello
...

```

发现时间线已经成为直线了。

原理分析：  

Git把我们本地的提交“挪动”了位置，放到了f005ed4 (origin/master) set exit=1之后，这样，整个提交历史就成了一条直线。rebase操作前后，最终的提交内容是一致的，但是，我们本地的commit修改内容已经变化了，它们的修改不再基于d1be385 init hello，而是基于f005ed4 (origin/master) set exit=1，但最后的提交7e61ed4内容是一致的。

这就是rebase操作的特点：把分叉的提交历史“整理”成一条直线，看上去更直观。缺点是本地的分叉提交已经被修改过了。

最后通过git push推送到远程。

## 实践场景
前提：开发团队有多人，但是日常开发均在dev分支进行开发和提交，master分支由组长负责，统一将dev适时合并到master分支。  
描述：项目开发过程中，某个会员星级升级的模块，虽然功能开发完成，但是发现存在效率问题，所以考虑进一步优化。当前分支本地代码已经保持与远程仓库一致，然后创建新的分支user_level_improve，在此分支进行开发和测试改进的代码。完成后，首先将该分支按正常的commit、push，保持该分支与它的远程仓库代码一致，接下来需要将代码合并至dev，并push到远程仓库。  
#### 具体操作：
1. 首先确保当前分支是user_level_improve，并且与它的远程代码一致，可通过命令git status查看状态，或者通过idea界面提示。
2. 切换到 dev 分支
```git checkout dev```
3. 查看当前分支，确保切换成功(*为当前分支,git branch -a 是查看所有分支，包括远程分支)
```git branch```

```
D:\workproject\server2>git branch
* dev
  pay_slip
  pay_slip001
  user_level_improve

D:\workproject\server2>git branch -a
* dev
  pay_slip
  pay_slip001
  user_level_improve
  remotes/origin/HEAD -> origin/dev
  remotes/origin/dev
  remotes/origin/double_eleven
  remotes/origin/haohuoyichangchuli
  remotes/origin/master
  remotes/origin/mid_antumn
  remotes/origin/pay_slip
  remotes/origin/sit
  remotes/origin/user_level_improve

```
4. 拉取dev远程仓库的代码，确保最新
```git pull origin dev ```

5. 完成后，合并分支
```git merge user_level_improve```
可以看到，已完成合并
```
D:\workproject\server2>git merge user_level_improve
Merge made by the 'recursive' strategy.
 .../mapper/user/UserLevelConfigMapper.xml          |  2 +-
 .../utry/operate/service/LevelManageService.java   | 21 +++++++
 .../service/impl/LevelManageServiceImpl.java       | 41 +++----------
 .../user/service/impl/WxUserLevelServiceImpl.java  | 70 ++++++++++++----------
 4 files changed, 67 insertions(+), 67 deletions(-)
```
6. 把合并后dev分支的代码，push到远程仓库
```git push origin dev```
```
D:\workproject\server2>git push origin dev
Enumerating objects: 61, done.
Counting objects: 100% (61/61), done.
Delta compression using up to 4 threads
Compressing objects: 100% (15/15), done.
Writing objects: 100% (21/21), 1.43 KiB | 367.00 KiB/s, done.
Total 21 (delta 10), reused 0 (delta 0)
To https:*******************
   4105a7eb..18e0ca92  dev -> dev

```

现在可以在dev正常进行开发了。