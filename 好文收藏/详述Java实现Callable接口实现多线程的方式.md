
另外可以参考（https://www.jianshu.com/p/32dee2e483b8）文章


首先看一下Callable接口的源码
``` JAVA
@FunctionalInterface
public interface Callable<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    V call() throws Exception;
}
```
首先明确一点这是一个函数式接口，其中的@FunctionalInterface定义了这个接口为函数式接口（具体函数式接口和普通接口有何区别可以自行查阅相关资料） ，Callable接口接受一个泛型作为接口中call方法的返回值类型，因此我们在使用时需要传入一个返回值类型。

然后我去实现这个接口来定义我自己的线程类，这里我传入了一个String类型作为接口call方法的返回值类型，然后实现了call方法，将result作为返回结果返回.

```java
public class MyCallable<String> implements Callable<String> {
 
	private int tickt=10;
	
	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		String result;
		while(tickt>0) {
			System.out.println("票还剩余："+tickt);
			tickt--;
		}
		result=(String) "票已卖光";
		return result;
	}
 
}
```

采用实现Callable接口实现多线程启动方式和以往两种的方式不太一样，下面就看一下怎样启动采用实现Callable接口的线程，首先我 new 一个我的实现实例，然后将我生成的实例对象注册进入到

FutureTask类中，然后将FutureTask类的实例注册进入Thread中运行。最后可以采用FutureTask<V>中的get方法获取自定义线程的返回值.
``` JAVA
public static void main(String[] args) throws InterruptedException, ExecutionException {		
    MyCallable<String> mc=new MyCallable<String>();
    FutureTask<String> ft=new FutureTask<String>(mc);
    new Thread(ft).start();
    String result=ft.get();
    System.out.println(result);
	}
```



最后我解释一下为什么实现Callable接口实现的多线程的形式需要采用这样的方式去启动线程。上面我们已经看了Callable接口的源码，其中只有一个call方法的定义，下面我们看一下FutureTask<V>类的定义，其中我们可以看到FutureTask<V>类是实现了RunnableFuture<V>接口的，我们再看FutureTask<V>类其中 的一个构造方法如下，其中需要传入一个Callable<V>类型的参数，更多内容可以自行查看FutureTask<V>类的具体实现，这里我们只需要知道此构造方法传入一个Callable<V>类型的参数，然后赋值给FutureTask<V>中的私有属性

``` JAVA
public class FutureTask<V> implements RunnableFuture<V>{ 
 
 
 public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;       // ensure visibility of callable
    }
}
```
然后我们再看 RunnableFuture<V>  接口，这个接口我们可以看到继承了Runnable, Future<V> 两个接口

``` JAVA
public interface RunnableFuture<V> extends Runnable, Future<V> {
    /**
     * Sets this Future to the result of its computation
     * unless it has been cancelled.
     */
    void run();
}
```

最后就是我们Thread类，无论我们以怎样的形式实现多线程，都需要调用Thread类中的start方法去向操作系统请求io，cup等资源，在Tread中有一个构造方法是传入一个Runnable接口类型的参数，而我们上面的FutureTask<V>实例实现了 RunnableFuture<V> 接口，而 RunnableFuture<V> 接口又继承了Runnable接口和 Funture<V>接口，因此我们可以将FutureTask<V> 的一个实例当做是一个Runnable接口的实例传入Thread来启动我们新建的线程.

示意图：
![img](https://img-blog.csdn.net/20180730145740512?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MjYwNjEzNQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

（转载自：https://blog.csdn.net/weixin_42606135/article/details/81282736）