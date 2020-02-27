ThreadLocal 是线程本地存储，在每个线程中都创建了一个 ThreadLocalMap 对象，每个线程可以访问自己内部 ThreadLocalMap 对象内的 value。通过这种方式，避免资源在多线程间共享。

经典的使用场景是为每个线程分配一个 JDBC 连接 Connection。这样就可以保证每个线程的都在各自的 Connection 上进行数据库的操作，不会出现 A 线程关了 B线程正在使用的 Connection； 还有 Session 管理 等问题。

一下是几篇详细分析ThreadLocal的技术博客可以参考。

原文地址：https://www.cnblogs.com/ldq2016/p/9041856.html
该文章主要思想是，对于多线程资源共享的问题，同步机制采用了“以时间换空间”的方式，而ThreadLocal采用了“以空间换时间”的方式。**前者仅提供一份变量，让不同的线程排队访问，而后者为每一个线程都提供了一份变量，因此可以同时访问而互不影响。**  

这边博客阅读量比较大，可能很多人搜索 `ThreadLocal` 相关知识都会看到这篇文章。但是，文中观点比较有争议，另外可以参考这几篇不错的文章，更加准确理解ThreadLocal，避免跑偏。

主要观点：通俗点就是**让别的线程不要干扰自己的对象**==ThreadLocal 并不是为了解决线程安全问题，而是提供了一种将实例绑定到当前线程的机制，类似于隔离的效果，实际上自己在方法中 new 出来变量也能达到类似的效果。ThreadLocal 跟线程安全基本不搭边，绑定上去的实例也不是多线程公用的，而是每个线程 new 一份，这个实例肯定不是共用的，如果共用了，那就会引发线程安全问题。ThreadLocal 最大的用处就是用来把实例变量共享成全局变量，在程序的任何方法中都可以访问到该实例变量而已。网上很多人说 ThreadLocal 是解决了线程安全问题，其实是望文生义，两者不是同类问题。==

* 【Java ThreadLocal你之前了解的可能有误】https://www.jianshu.com/p/b74de925cd7a

* 【正确理解ThreadLocal】**来自2007年的远古好贴**https://www.iteye.com/topic/103804

* 【这样使用ThreadLocal对吗】https://www.xttblog.com/?p=3087

