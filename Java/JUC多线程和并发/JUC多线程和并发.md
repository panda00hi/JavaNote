### 学习方法

**理论-->demo-->总结**

## 一、volatile的理解

### 1.volatile是Java虚拟机提供的轻量级的同步机制

**特点**：遵守JMM规范，能够保证可见性、有序性(禁止指令重排)，**不保证原子性**。

#### 1.1 保证可见性

当多个线程访问同一个变量是，一个线程改变了这个变量的值，其他线程能够立即看到修改的值。

``` java
package com.pandahi.juc;

import java.util.concurrent.TimeUnit;

/**
 * 1. 验证volatile 可见性
 * 1.1 假如int num = 0，num之前没有volatile关键字修饰
 * 1.2 添加volatile关键字修饰，及时通知其他线程，主物理内存中的值已经修改
 */
public class VolatileDemo {
    public static void main(String[] args) {
        MyData myData = new MyData();
        // 第一个线程
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "   come in ……");

            try {
                // 暂停3秒
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            myData.addTo60();
            System.out.println(Thread.currentThread().getName() + "   update number value:" + myData.num);
        }, "AAA").start();

        // 第二个线程：main
        while (myData.num == 0) {
            // 只要读取到的num值为0，这里将一直循环，不进行下一步
        }
        System.out.println(Thread.currentThread().getName() + "mission is over, num value:  " + myData.num);

    }
}

class MyData {
    // int num = 0;
    volatile int num = 0;

    public void addTo60() {
        this.num = 60;
    }
}
```

运行结果：

不加volatile关键字

``` java
AAA   come in …… AAA   update number value:60 // main进入死循环
```

加上volatile关键字后，结果为

``` java
AAA   come in …… AAA   update number value:60 mainmission is over, num value:  60
```

#### 1.2 不保证原子性

**原子性：**不可分割、完整的，即某个线程正在做某个具体业务时，中间不可以被加塞或者被分割，需要整体完整，要么同时成功，要么同时失败。

**num++操作实际的底层操作：**

![img](.\images\clipboard.png)

代码：

``` java
package com.pandahi.juc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1. 验证volatile 可见性
 * 1.1 假如int num = 0，num之前没有volatile关键字修饰
 * 1.2 添加volatile关键字修饰
 * <p>
 * 2. 验证volatile不保证原子性
 * 2.1 原子性概念：不可分割、完整性，即某个线程正在做某个具体业务时，中间不可以被加塞或者被分割，需要整体完整，要么同时成功，要么同时失败
 * 2.2 使用synchronized可以，但是太重了，可以使用Atomic类实现
 */
public class VolatileDemo {
    public static void main(String[] args) {
        MyData myData = new MyData();
        // 验证可见性
        // visibleVolatile(myData);
        // 验证不保证原子性，以及使用Atomic可以实现原子性
        atomicVolatile(myData);
    }

    public static void atomicVolatile(MyData myData) {
        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    myData.addPlusPlus();
                    myData.syncAdd();
                    myData.addAtomic();
                }
            }, "Thread " + i).start();
        }
        // 等待以上20个线程都全部完成，再用main线程取得最终的结果值，看是多少
        // Thread.activeCount()活跃的线程数量，大于2(一个是main一个是gc线程)
        while (Thread.activeCount() > 2) {
            Thread.yield();
        }
        System.out.println(Thread.currentThread().getName() + "    int type, finally num value:" + myData.num);
        System.out.println(Thread.currentThread().getName() + "    use synchronized, finally num value:" + myData.number);
        System.out.println(Thread.currentThread().getName() + "    AtomicInteger type, finally num value:" + myData.atomicInteger);
    }

    /**
     * 验证可见性
     *
     * @param myData
     */
    public static void visibleVolatile(MyData myData) {
        // 第一个线程
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "   come in ……");

            try {
                // 暂停3秒
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            myData.addTo60();
            System.out.println(Thread.currentThread().getName() + "   update number value:" + myData.num);
        }, "AAA").start();

        // 第二个线程：main
        while (myData.num == 0) {
            // 只要读取到的num值为0，这里将一直循环，不进行下一步
        }
        System.out.println(Thread.currentThread().getName() + "mission is over, num value:  " + myData.num);
    }
}

class MyData {
    // int num = 0;
    volatile int num = 0;
    volatile int number = 0;

    public void addTo60() {
        this.num = 60;
    }

    public void addPlusPlus() {
        // num++是非原子操作，底层实际进行了三个操作
        // 首先拷贝初始值，第二步在各自工作内存加1，然后将加完的值再写回主内存
        num++;
    }

    /**
     * 解决原子性:
     * 1. 加sync
     * 2. 使用juc的AtomicInteger
     */
    public synchronized void syncAdd() {
        number++;
    }

    AtomicInteger atomicInteger = new AtomicInteger();

    public void addAtomic() {
        atomicInteger.getAndIncrement();
    }
}
```

多次执行结果：

``` 
main    int type, finally num value:19956
main    use synchronized, finally num value:20000
main    AtomicInteger type, finally num value:20000

main    int type, finally num value:19961
main    use synchronized, finally num value:20000
main    AtomicInteger type, finally num value:20000

main    int type, finally num value:19970
main    use synchronized, finally num value:20000
main    AtomicInteger type, finally num value:20000
```

#### 1.3 禁止指令重排

有序性：计算机在执行程序时，为了提高性能，编译器和处理器常常会对**指令做重排**，一般分以下三种：

![image-20191223174044877](.\images\image-20191223174044877.png)

源代码 -》编译器优化的重排 - 》指令并行的重排 -》 内存系统的重排 -》 最终执行的指令。

单线程环境里程序的实际执行和代码的顺序一致。

处理器在进行重排序时必须要考虑指令之间的**数据依赖性**

多线程环境中，线程交替执行，由于编译器优化重排，两个线程中使用的变量能否保证一致性时无法确定的，结果无法预测。

**重排案例1：**

``` 
public void mySort(){
int x = 11;  // 语句1
int y = 12;  // 语句2
x = x + 5;   // 语句3
y = x + x;   // 语句4
}
```

结果分析：多线程环境下由于存在指令重排，可能的执行顺序有

1234、2134、1324

问题：语句4可以重排后变成第一条吗？答：不会。变量值需要先声明后使用。语句四依赖x的值，没办法排到第一个。

**重排案例2：**

int a, b, x, y = 0; 

| 线程1          | 线程2          |
| -------------- | -------------- |
| x = a; | y = b; |
| b = 1; | a = 2; |
| 结          果 | x = 0      y=0 |

如果编译器对这段程序代码执行重排优化后，先给b、a赋值，可能出现如下情况：

| 线程1          | 线程2          |
| -------------- | -------------- |
| b = 1; | a = 2; |
| x= a; | y = b; |
| 结          果 | x = 2      y=1 |

这个结果说明在多线程环境下，由于编译器优化重排的存在，两个线程中使用的变量能否保证一致性是无法确定的。

#### **底层实现**--内存屏障（Memory Barrier）

volatile实现禁止指令重排优化，从而避免多线程环境下程序出现乱序执行的现象。

内存屏障，又称内存栅栏，是一个CPU指令，作用有两个：

一、保证特定操作的执行顺序。

二、保证某些变量的内存可见性（利用该特性实现volatile的内存可见性）。

由于编译器和处理器都能执行指令重排优化。如果在指令间插入一条MemoryBarrier则会告诉编译器和CPU，不管什么指令都不能和这条MemoryBarrier指令重排序，也就是说***通过插入内存屏障禁止在内存屏障前后的指令执行重排序优化***。内存屏障的另一个作用时强制刷出各种CPU的缓存数据，因此任何CPU上的线程都能读取到这些数据的最新版本。

![image-20191224104348627](.\images\image-20191224104348627.png)

> **小结：线程安全性获得保证.**

1. 工作内存与主内存同步延迟现象导致的**可见性问题**

   可以使用synchronized或volatile关键字解决，他们都可以使一个线程修改后的变量立即对其他线程可见。

2. 对于指令重排导致的**可见性问题和有序性问题**

   可以利用volatile关键字解决，因为volatile的另一个作用就是禁止重排序优化。

### 2. JMM(java内存模型)

JMM（Java Memory Model）本身是一种抽象的概念，并不真实存在，他描述的时一组规则或规范，通过这组规范定义了程序中各个变量（包括实例字段，静态字段和构成数组对象的元素）的访问方式。

**特点(线程安全)：**可见性、**原子性**、有序性

**JMM关于同步的规定：**

1. 线程解锁前，必须把共享变量的值刷回主内存

2. 线程加锁前，必须读取主内存的最新值到自己的工作内存

3. 加锁解锁时同一把锁

JVM运行程序的实体时线程，而每个线程创建时JVM都会为其创建一个工作内存（有些地方称之为栈空间），工作内存时每个线程的私有数据区域，而Java内存模型中规定，所有变量都存储在主内存，主内存是共享内存区域，所有线程都可以访问，但线程对比哪里的操作（读取和赋值）必须在各自工作内存中进行，首先将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量协会主内存，不能直接操作内存中的变量，各个线程中的工作内存中存储着主内存中的变量副本拷贝，因此，不同的线程间无法访问彼此的工作内存，线程间的通信（传值）必须通过主内存来完成。

![img](.\images\jmm.png)

### 3. 在哪些地方用过volatile

#### 3.1 单线程模式下没有问题，但是多线程并发时，要考虑安全问题。

``` java
package com.pandahi.juc;

/**
 * 多线程并发情况下，单例模式存在安全问题，结果会生成多个instance，无法控制
 * 
 */
public class SingletonDemo {

    private static SingletonDemo instance = null;

    private SingletonDemo() {
        System.out.println(Thread.currentThread().getName() + "  我是构造方法SingletonDemo()");
    }

    // 加synchronized可以解决，但是整个方法被锁住，安全性得到保证，但并发性大大降低
    // private static synchronized SingletonDemo getInstance() {
    //     if (instance == null) {
    //         instance = new SingletonDemo();
    //     }
    //     return instance;
    // }
    private static  SingletonDemo getInstance() {
        if (instance == null) {
            instance = new SingletonDemo();
        }
        return instance;
    }

    public static void main(String[] args) {

        // 单线程情况下，没有问题，只会被执行一次
        // System.out.println(SingletonDemo.getInstance() == SingletonDemo.getInstance());  // 结果为true
        // System.out.println(SingletonDemo.getInstance() == SingletonDemo.getInstance());  // 结果为true
        // System.out.println(SingletonDemo.getInstance() == SingletonDemo.getInstance());  // 结果为true

        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                SingletonDemo.getInstance();
            }, "Thread  " + i).start();
        }
    }
}
```

运行结果：

``` 
Thread  0  我是构造方法SingletonDemo()
Thread  1  我是构造方法SingletonDemo()
```

#### 3.2 解决单例模式多线程不安全的问题

**（1）DCL（double check lock）双端检测机制**

``` java
// 使用DCL机制，在加锁前后都进行一次判断
    private static SingletonDemo getInstance() {
        if (instance == null) {
            synchronized (SingletonDemo.class) {
                if (instance == null) {
                    instance = new SingletonDemo();
                }
            }
        }
        return instance;
    }
```

在某一个线程执行到第一次检测，读取到instance不为null时，instance的引用对象可能没有完成初始化。**instance=new SingleDemo(); **  可以被分为一下三步（伪代码）：

``` 
memory = allocate();//1.分配对象内存空间
instance(memory);	//2.初始化对象
instance = memory;	//3.设置instance执行刚分配的内存地址，此时instance!=null
```

步骤2和步骤3不存在数据依赖关系，而且无论重排前还是重排后程序的执行结果在单线程中并没有改变，因此这种重排优化时允许的，**如果3步骤提前于步骤2，但是instance还没有初始化完成**

``` 
memory = allocate();//1. 分配对象内存空间
instance = memory;	//2. 设置instance执行刚分配的内存地址，此时instance!=null，但是对象还没有初始化完成！！
instance(memory);	//3. 初始化对象
```

但是指令重排只会保证串行语义的执行的一致性（单线程），但并不关心多线程间的语义一致性。

所以当一条线程访问instance不为null时，由于instance示例未必已初始化完成，也就造成了线程安全问题。

**小结**：大部分时候，构造方法只执行一次，但是执行重排机制会让程序小概率出现构造方法被执行多次的情况，即**DCL机制不保证线程安全，原因是存在指令重排，所以需要结合volatile禁止指令重排**

**（2）单例模式volatile**

为解决以上问题，需要将SingletonDemo实例上加volatile，等对象初始化等过程都完成再取，防止出现线程不安全问题。

``` java
private static volatile SingletonDemo instance = null;
```

## 二、CAS（是对乐观锁思想的一种实现）

### 1.compareAndSet——比较并交换

AtomicInteger.compareAndSet(int expect, indt update)源码。

多个线程都拿到主内存中的变量值的副本5。第一个线程跟期望值均为5，则进行更新，将值改为2019，并写回主内存。此时主内存中的值为2019，另一个线程拿到的真实值跟期望值不同，所以修改失败。

![image-20191224140805925](.\images\image-20191224140805925.png)

第一个参数为拿到的期望值，如果期望值一致，进行update赋值，如果期望值不一致，证明数据被修改过，返回fasle，取消赋值

``` java
 /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }

```

demo：

``` java
package com.pandahi.juc;

import java.util.concurrent.atomic.AtomicInteger;
/**

* CAS是什么？比较并交换

*/
public class CASDemo {
    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger(5);
        boolean result1 = atomicInteger.compareAndSet(5, 2019);
        boolean result2 = atomicInteger.compareAndSet(5, 2019);
        System.out.println("第一次修改期望值是5：" + result1 + "现在值为：" + atomicInteger);
        System.out.println("第二次修改期望值是5：" + result2 + "现在值为：" + atomicInteger);

    }
}
```

运行结果：

``` java
第一次修改期望值是5：true现在值为：2019
第二次修改期望值是5：false现在值为：2019
```

### 2. CAS底层实现原理？对Unsafe的理解

1. atomicInteger.getAndIncrement()

比较当前工作内存中的值和主内存中的值，如果相同，则执行操作，斗则继续你叫主内存和工作内存的值

``` java
atomicInteger.getAndIncrement()方法的源代码:
/**
 * Atomically increments by one the current value.
 *
 * @return the previous value
 */
public final int getAndIncrement() {
    return unsafe.getAndAddInt(this, valueOffset, 1);
}
```

![image-20191224141417761](.\images\image-20191224141417761.png)

2. UnSafe(原子整型做num++操作，不需要用synchronized也能保证多线程安全的原因)

   - 是CAS的核心类 由于Java 方法无法直接访问底层 , 需要通过本地(native)方法来访问, Unsafe相当于一个后面, 基于该类可以直接操作特额定的内存数据. UnSafe类在于sun.misc包中, 其内部方法操作可以向C的指针一样直接操作内存, 因为Java中CAS操作的执行依赖于Unsafe类的方法.

     **UnSafe类中所有的方法都是native修饰的,也就是说UnSafe类中的方法都是直接调用操作底层资源执行响应的任务**

   - 变量ValueOffset，便是该变量在内存中的偏移地址, 因为UnSafe就是根据内存偏移地址获取数据的

![image-20191224141558560](.\images\image-20191224141558560.png)

   - 变量value和volatile修饰，保证了多线程之间的可见性.

3. CAS是什么

   CAS的全称为Compare-And-Swap , 它是一条CPU并发原语.
   它的功能是判断内存某个位置的值是否为预期值, 如果是则更新为新的值, 这个过程是原子的.

   CAS**并发原语**提现在Java语言中就是sun.miscUnSaffe类中的各个方法. 调用UnSafe类中的CAS方法，JVM会帮我实现CAS汇编指令。这是一种完全依赖于硬件功能, 通过它实现了原子操作, 再次强调, **由于CAS是一种系统原语, 原语属于操作系统用于范畴, **是由若干条指令组成, 用于完成某个功能的一个过程, **并且原语的执行必须是连续的, 在执行过程中不允许中断, 也即是说CAS是一条原子指令, 不会造成所谓的数据不一致的问题，线程安全**

   

![image-20191224141802800](.\images\image-20191224141802800.png)

   

![image-20191224141809447](.\images\image-20191224141809447.png)

   

![image-20191224141818558](.\images\image-20191224141818558.png)

   

``` java
   //unsafe.getAndAddInt
       public final int getAndAddInt(Object var1, long var2, int var4) {
           int var5;
           do {
               var5 = this.getIntVolatile(var1, var2);
           } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));
           return var5;
       }
   ```

   var1 AtomicInteger对象本身

   var2 该对象的引用地址

   var4 需要变动的数据

   var5 通过var1 var2找出的主内存中真实的值

   用该对象前的值与var5比较；

   如果相同，更新var5+var4并且返回true，

   如果不同，继续去之然后再比较，直到更新完成

### 3. CAS的缺点

1. 循环时间长, 开销大

   例如getAndAddInt方法执行，有个do while循环，如果CAS失败，一直会进行尝试，如果CAS长时间不成功，可能会给CPU带来很大的开销

   

![image-20191224145033613](.\images\image-20191224145033613.png)

2. 只能保证一个共享变量的原子操作

   对多个共享变量操作时，循环CAS就无法保证操作的原子性，这个时候就可以用锁来保证原子性

3. ABA问题

   

## 三、原子类AtomicInteger的ABA问题? 原子引用?

### 1. ABA问题的产生(狸猫换太子)

![image-20191224145310925](.\images\image-20191224145310925.png)

CAS算法实现一个重要前提就是需要取出内存中的数据并在当下时刻比较并替换, 那么在这个时间差里, 会导致数据的变化.(虽然结果一致, 但是中间经历的过程不确定)

比如**线程1**从内存位置V取出A，**线程2**同时也从内存取出A，并且线程2进行一些操作将值改为B，然后线程2又将V位置数据改成A，这时候线程1进行CAS操作发现内存中的值依然时A，然后线程1操作成功。

==尽管线程1的CAS操作成功，但是不代表这个过程没有问题==

### 2. 如何解决? 原子引用

原子引用: jdk自带atomicInteger等, 也可以自行进行包装, 利用AtomicReference, 将所需要的类型包装成原子类型.

``` java
package com.pandahi.juc;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 原子引用，解决ABA问题
 */
public class AtomicRefrenceDemo {
    public static void main(String[] args) {
        User z3 = new User("z3", 22);
        User li4 = new User("li4", 25);

        AtomicReference<User> atomicReference = new AtomicReference<>();
        atomicReference.set(z3);
        // 如果主物理内存中是z3，那么，修改为li4
        System.out.println(atomicReference.compareAndSet(z3, li4) + "  " + atomicReference.get().toString());
        System.out.println(atomicReference.compareAndSet(z3, li4) + "  " + atomicReference.get().toString());
    }

}

class User {
    String username;
    int age;

    public User(String username, int age) {
        this.username = username;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", age=" + age +
                '}';
    }
}
```

运行结果:

``` 
true  User{username='li4', age=25}
false  User{username='li4', age=25}
```

### 3. 时间戳原子引用

``` java
package com.pandahi.juc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 时间戳的原子引用，利用版本号解决ABA问题
 */
public class ABADemo {
    static AtomicReference<Integer> atomicReference = new AtomicReference<>(100);
    static AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(100, 1);

    public static void main(String[] args) {

        System.out.println("**********ABA问题的产生************");

        new Thread(() -> {
            // t1 线程对数据进行了多次操作
            atomicReference.compareAndSet(100, 101);
            atomicReference.compareAndSet(101, 100);
        }, "t1").start();

        new Thread(() -> {

            // 暂停1秒t2线程，保证t1完成了一次ABA操作
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 这里只对值头尾进行比较，符合期望，仍能够修改成功
            System.out.println(atomicReference.compareAndSet(100, 2019) + "  " + atomicReference.get());

        }, "t2").start();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // ABA问题解决
        System.out.println("**********ABA问题的解决************");

        new Thread(() -> {
            int stamp = atomicStampedReference.getStamp();
            System.out.println(Thread.currentThread().getName() + "  第1次版本号：" + stamp);
            // 暂停1秒钟t3线程
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 进行CAS操作，版本号已经变为3，实际值100->101->100，头尾值相同，但是版本号不同
            atomicStampedReference.compareAndSet(100, 101, atomicStampedReference.getStamp(), atomicStampedReference.getStamp() + 1);
            System.out.println(Thread.currentThread().getName() + "  第2次版本号：" + atomicStampedReference.getStamp());
            atomicStampedReference.compareAndSet(101, 100, atomicStampedReference.getStamp(), atomicStampedReference.getStamp() + 1);
            System.out.println(Thread.currentThread().getName() + "  第3次版本号：" + atomicStampedReference.getStamp());

        }, "t3").start();

        new Thread(() -> {
            // 与t3取到的版本号相同
            int stamp = atomicStampedReference.getStamp();
            System.out.println(Thread.currentThread().getName() + "  第1次版本号：" + stamp);
            // 暂停3秒钟t4线程，保证t3也完成了一次ABA操作
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean result = atomicStampedReference.compareAndSet(100, 2019, stamp, stamp + 1);

            System.out.println(Thread.currentThread().getName() + "  修改成功否：" + result + "   当前最新实际版本号为：" + atomicStampedReference.getStamp());
            System.out.println(Thread.currentThread().getName() + "  当前实际最新值：" + atomicStampedReference.getReference());

        }, "t4").start();

    }
}
```

运行结果:

``` java
**********ABA问题的产生************
true  2019
**********ABA问题的解决************
t3  第1次版本号：1
t4  第1次版本号：1
t3  第2次版本号：2
t3  第3次版本号：3
t4  修改成功否：false   当前最新实际版本号为：3
t4  当前实际最新值：100
```

## 四、集合的多线程并发问题

### 注意：HashSet与ArrayList底层都是 HashMap

HashSet底层是一个HashMap，存储的值放在HashMap的key里，value存储了一个PRESENT的静态Object对象

``` java
    /**
     * Adds the specified element to this set if it is not already present.
     * More formally, adds the specified element <tt>e</tt> to this set if
     * this set contains no element <tt>e2</tt> such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns <tt>false</tt>.
     *
     * @param e element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     * element
     */
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }
```

``` java
 	// PRESENT是一个Object对象常量
	// Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();
```

### 1. ArrayList是线程不安全, 请编写一个不安全的案例并给出解决方案

> 注: 通过查看源码, 可知new ArrayList<Integer>(); 默认创建大小为10, 如果超过则进行扩容等操作.

``` java
	 /**
     * Constructs an empty list with an initial capacity of ten.
     */
    	public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }
```

**原来如此:** 为什么ArrayList是线程不安全而Vector是线程安全??  为什么Vector线程安全而不优先使用呢?

**答:** 通过查看add()方法的源码, 可以看到ArrayList的add()没有synchronized关键字修饰, 而Vector有加锁. 但是, Vector虽然保证了安全性, 但是并发性大大降低. 因此不优先采用.

部分源码:

```java 

    /**
     * ArrayList的add()方法
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        ensureCapacityInternal(size + 1); // Increments modCount!!
        elementData[size++] = e; 
        return true; 
    }

``` 

```java
    /**
     * Vector的add()方法
     * Appends the specified element to the end of this Vector.
     *
     * @param e element to be appended to this Vector
     * @return {@code true} (as specified by {@link Collection#add})
     * @since 1.2
     */
    public synchronized boolean add(E e) {
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = e;
        return true;
    }
```

**集合类多线程不安全代码示例**

``` java
package com.pandahi.juc;

import java.util.*;

/**
 * 集合类不安全问题
 */
public class ContainerNotSafeDemo {
    public static void main(String[] args) {

        /**
         * 导致故障！！！！！！！！
         * 直接使用new ArrayList，多线程情况下不安全。
         * 故障现象：java.util.ConcurrentModificationException并发修改异常
         */
        List<String> list = new ArrayList<>();
        // 使用Vector类可以保证安全，但是并发性能大幅降低
        // List<String> list = new Vector<>();
        // 使用JDK封装好的Collections功能包中的方法，对集合类进行包装增强
        // List<String> list = Collections.synchronizedList(new ArrayList<>());

        // 开启多个线程，进行多线程操作
        for (int i = 0; i < 30; i++) {
            new Thread(() -> {
                list.add(UUID.randomUUID().toString().substring(0, 8));
                System.out.println(list);
            }, "Thread " + i).start();
        }
    }

}
```

运行报错：

``` 
Exception in thread "Thread 10" java.util.ConcurrentModificationException
```

**导致原因**：一个正在写入，另一个来抢夺，导致数据不一致，引发并发修改异常。

**解决办法:** 

``` java
List<String> list = new Vector<>();// 法1：Vector线程安全
List<String> list = Collections.synchronizedList(new ArrayList<>());// 法2：使用辅助类
List<String> list = new CopyOnWriteArrayList<>();// 法3：写时复制，读写分离

Map<String, String> map = new ConcurrentHashMap<>();
Map<String, String> map = Collections.synchronizedMap(new HashMap<>());
```

方法1：使用线程安全的集合类Vector。但是会导致并发性能大幅降低。不推荐。

方法2：Collections工具类，提供的多线程方法，对非线程安全的集合进行保证增强。( `注意:Collection是接口提供了对集合对象进行基本操作的通用接口方法，意义是为各种具体的集合提供最大化统一的操作方式；而Collections是一个包装类，包含各种有关集合操作的静态多态方法，如排序、线程并发安全等，相当于一个工具类，服务于Collection框架` )

方法3：使用**CopyOnWriteArrayList**

### 2. CopyOnWrite实现读写分离

类比案例：**写时复制**，读写分离的思想。

CopyOnWrite容器，即写时复制的容器。往一个容器添加元素的时候，不直接往当前容器Object[]添加，而是先将当前容器Object[]进行copy，复制出一个新的容器Object[] newElements ，新的容器里添加元素，添加完元素之后，再将原来容器的引用指向新的容器setArray(newElements); 。这样的好处是可以对CopyOnWrite容器进行并发的读，而不需要加锁。因为当前容器不会添加任何元素，所以CopyOnWrite容器时一种读写分离的思想，读和写不同的容器。

> 相当于签到表，后到的部分，要将原有名单复制一份，原有作废，在新名单签至末位，以此类推。

CopyOnWriteArrayList源码中的add方法实现：

``` java
    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            newElements[len] = e;
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

```

## 五、Java锁-公平锁/非公平锁/可重入锁/递归锁/自旋锁谈谈你的理解? 请手写一个自旋锁? 死锁编码和定位分析

### 1. 公平和非公平锁

1. 是什么

   并发包java.util.concurrent.locks包下，reentrantLock的创建可以指定构造函数的boolean类型来得到公平锁或者非公平锁 默认是非公平锁。

   公平锁就是先来后到，非公平锁允许加塞，优先级不确定。 `Lock lock = new ReentrantLock();` 默认非公平。

``` java
    /**
     * Creates an instance of {@code ReentrantLock}.
     * This is equivalent to using {@code ReentrantLock(false)}.
     */
    public ReentrantLock() {
        sync = new NonfairSync();
    }
```

* 公平锁

  是指多个线程按照申请锁的顺序来获取锁类似排队打饭 先来后到。

* 非公平锁

  是指在多线程获取锁的顺序并不是按照申请锁的顺序, 有可能后申请的线程比先申请的线程优先获取到锁, 在高并发的情况下, 有可能造成优先级反转或者饥饿现象

2. 两者区别

* 公平锁：Thread acquire a fair lock in the order in which they requested id.

  在并发环境下，每个线程在获取锁时，会先查看此锁维护的等待队列，如果为空，或当前线程为第一个，就占有锁，否则加入到等待队列，按照FIFO的规则排队。

* 非公平锁：a nonfair lock permits barging: threads requesting a lock can jump  ahead of the queue of waiting threads if the lock happens to be  available when it is requested.

  非公平锁比较粗鲁，上来就直接尝试占有额，如果尝试失败，就再采用类似公平锁那种方式.

3. Other

   Java ReentrantLock而言, 
   通过构造哈数指定该锁是否是公平锁 默认是非公平锁 非公平锁的优点在于吞吐量必公平锁大.

   对于synchronized而言 也是一种非公平锁.

### 2. 可重入锁（即递归锁）

``` java
// 两个方法是同一锁，能够进入
public sync void method01()
{
    method02();
}
public sync void method02()
{
    ……
}
```

1. 是什么

   同一线程外层函数获得锁之后，内层递归函数仍能获取该锁的代码，在同一个线程在外层方法获取锁的时候，在进入内层方法会自动获取锁，即**线程可以进入任何一个它已经拥有的锁所同步的代码块。**

2. **ReentrantLock/Synchronized 就是一个典型的可重入锁**

3. **可重入锁最大的作用是避免死锁**

4. 代码示例

（1）synchronized是典型的可重入锁

``` java
package com.pandahi.juc;

/**
 * synchronized是典型的可重入锁
 * 同一线程外层函数获得锁之后，内层递归函数仍然能获取该锁的代码，
 * 在同一个线程在外层方法获得锁的时候，进入内层方法将自动获得锁
 * <p>
 * t1  invoked sendSMS()        t1 线程外层函数获得锁之后
 * t1  ####invoked sendEmail()  t1 进入内层方法将自动获得锁
 * t2  invoked sendSMS()
 * t2  ####invoked sendEmail()
 */
public class ReenterLockDemo {

    public static void main(String[] args) {
        Phone phone = new Phone();
        new Thread(() -> {
            try {
                phone.sendSMS();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "t1").start();

        new Thread(() -> {
            try {
                phone.sendSMS();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, "t2").start();
    }
}
/**
 * 资源类
 */
class Phone {
    public synchronized void sendSMS() {
        System.out.println(Thread.currentThread().getName() + "  invoked sendSMS()");
        sendEmail();
    }

    public synchronized void sendEmail() {
        System.out.println(Thread.currentThread().getName() + "  ####invoked sendEmail()");
    }
}
```

运行结果：

``` 
t1  invoked sendSMS()
t1  ####invoked sendEmail()
t2  invoked sendSMS()
t2  ####invoked sendEmail()
```

（2）ReentrantLock也是典型的可重入锁（但是要注意，lock与unlock要配对，加锁几次，就要解锁几次。否则将阻塞）

``` java
		GetSetTest getSetTest = new GetSetTest();

        Thread t3 = new Thread(getSetTest, "t3");
        Thread t4 = new Thread(getSetTest, "t4");

        t3.start();
        t4.start();
……

class GetSetTest implements Runnable {
    Lock lock = new ReentrantLock();

    @Override
    public void run() {
        get();
    }

    public void get() {
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + "  invoked get()");
            set();
        } finally {
            lock.unlock();
        }
    }

    public void set() {
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + "  ####invoked set()");
        } finally {
            lock.unlock();
        }
    }

}
```

运行结果：

``` 
t3  invoked get()
t3  ####invoked set()
t4  invoked get()
t4  ####invoked set()
```

### 3. 自旋锁

1. 是什么

   ​    尝试获取锁的线程不会立即阻塞，而是采用循环的方式取尝试获得锁，好处是减少线程上下文切换的消耗，没有类似wait的阻塞，缺点时循环会消耗CPU。

   

``` java
       public final int getAndAddInt(Object var1, long var2, int var4) {
           int var5;
           do {
               var5 = this.getIntVolatile(var1, var2);
           } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));
           return var5;
       }
   ```

2. 手写自旋锁

   通过CAS操作完成自旋锁，A线程先进来调用mylock方法自己持有锁5秒钟，B随后进来发现当前有线程持有锁，不是null，所以只能通过自旋等待，知道A释放锁后B随后抢到。

   

``` java
   package com.pandahi.juc;
   
   import java.util.concurrent.TimeUnit;
   import java.util.concurrent.atomic.AtomicReference;
   
   /**

    - 好处是不会阻塞，但是，其中一个线程长时间持有时，其他线程将会自旋，等待当前占用的线程释放锁，造成长时间的CPU消耗。
    - @Author: pandaHi
    - @Date: 2019/12/25

    */
   public class SpinLockDemo {
   
       // 原子引用线程，初始值为null
       AtomicReference<Thread> atomicReference = new AtomicReference<>();
   
       public void myLock() {
           Thread thread = Thread.currentThread();
           System.out.println(Thread.currentThread().getName() + "  come in");
   
           // 如果为null，则将当前线程放入，返回true，取反，所以while不进入
           while (!atomicReference.compareAndSet(null, thread)) {
               // AA线程进入myLock方法、BB线程也进入。但是由于AA线程进入后，率先将atomicReference改为了
               // 自身线程对象，所以BB等其他线程将进入while循环自旋，直到AA线程通过myUnlock方法将atomicReference
               // 又置为null，BB线程才会跳出while自旋。
           }
       }
   
       public void myUnlock() {
           Thread thread = Thread.currentThread();
           atomicReference.compareAndSet(thread, null);
           System.out.println(Thread.currentThread().getName() + "  invoked myUnlock()");
       }
   
       public static void main(String[] args) {
           SpinLockDemo spinLockDemo = new SpinLockDemo();
           new Thread(() -> {
               spinLockDemo.myLock();
               // 暂停一会线程
               try {
                   TimeUnit.SECONDS.sleep(3);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               spinLockDemo.myUnlock();
           }, "AA").start();
   
   
           new Thread(() -> {
               spinLockDemo.myLock();
               // 暂停一会线程
               try {
                   TimeUnit.SECONDS.sleep(1);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               spinLockDemo.myUnlock();
           }, "BB").start();
   
           new Thread(() -> {
               spinLockDemo.myLock();
               // 暂停一会线程
               try {
                   TimeUnit.SECONDS.sleep(1);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               spinLockDemo.myUnlock();
           }, "CC").start();
       }
   }
   
   ```

   运行结果：

   

``` 
   AA  come in
   CC  come in
   BB  come in
   AA  invoked myUnlock()
   BB  invoked myUnlock()
   CC  invoked myUnlock()
   ```

### 4. 独占锁(写锁)/共享锁(读锁)/互斥锁

 1. 概念

    传统的synchronized和lock虽然保证了“读”和“写”的安全，但是也导致了“读”的并发性大大降低。因此需要更进一步控制。

     - 独占锁：指该锁一次只能被一个线程锁持有，对ReentrantLock和Synchronized而言都是独占锁。

     - 共享锁：该锁可被多个线程锁持有

       ReentrantReadWriteLock其读锁是共享锁，写锁是独占锁。

    - 互斥锁：读锁的共享锁可以保证高并发读是高效的，读写、写读、写写的过程是互斥的。（有写的操作，就是互斥的）

    2. 代码示例	

    

``` java
    package com.pandahi.juc;
    
    import java.util.HashMap;
    import java.util.Map;
    import java.util.concurrent.TimeUnit;
    import java.util.concurrent.locks.Lock;
    import java.util.concurrent.locks.ReentrantLock;
    
    /**
     * 多个线程同时读一个资源类没有任何问题，所以为了满足并发量，读取共享资源应该允许同时进行
     * 但是
     * 如果有一个线程想要写共享资源，就不应该再有其他线程对资源进行读或写
     * 总结
     * 读读可共存
     * 读写不可共存
     * 写写不可共存
     * <p>
     * 写操作：原子+独占，整个过程必须是一个完整的过程，不能被加塞、中断
     *
     * @Author: pandaHi
     * @Date: 2019/12/25
     */
    public class ReadWriteLockDemo {
        public static void main(String[] args) {
            MyCache myCache = new MyCache();
    
            // 5个线程读，5个线程写
            for (int i = 0; i < 5; i++) {
                // lambda表达式要求是final类型的，所以提前赋值final型的临时变量
                final int tempInt = i;
                new Thread(() -> {
                    myCache.put(tempInt + "", tempInt);
                }, "Thread " + i).start();
    
            }
            
            
            for (int i = 0; i < 5; i++) {
                // lambda表达式要求是final类型的，所以提前赋值final型的临时变量
                final int tempInt = i;
                new Thread(() -> {
                    myCache.get(tempInt + "");
                }, "Thread " + i).start();
    
            }
        }
    }
    
    
    /**
     * 缓存资源类
     * 同一时刻，可供多人读，但是仅供一人写
     */
    class MyCache {
        private volatile Map<String, Object> map = new HashMap<>();
    
        // 可以保证一致性安全性，但是失去了多个线程读的并发性能
        // private Lock lock = new ReentrantLock();
    
        /**
         * 写操作必须保证原子性，独占不可分割。即，同一线程的"正在写入"、"写入完成"是连续的
         *
         * @param key
         * @param value
         */
        public void put(String key, Object value) {
            System.out.println(Thread.currentThread().getName() + "  正在写入：" + key);
            // 模拟网络时延500毫秒
            try {
                TimeUnit.MICROSECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "  写入完成：");
        }
    
        /**
         * 读是可以共享的，各个线程不一样
         *
         * @param key
         */
        public void get(String key) {
            System.out.println(Thread.currentThread().getName() + "  正在读取：");
            // 模拟网络时延500毫秒
            try {
                TimeUnit.MICROSECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Object result = map.get(key);
            System.out.println(Thread.currentThread().getName() + "  正在读取：" + result);
    
        }
    
    }
    ```

    运行结果:

    

``` 
    Thread 0  正在写入：0
    Thread 2  正在写入：2
    Thread 1  正在写入：1
    Thread 3  正在写入：3
    Thread 2  写入完成：
    Thread 0  正在读取：
    Thread 0  写入完成：
    Thread 0  正在读取：null
    Thread 1  写入完成：
    Thread 4  正在写入：4
    Thread 3  写入完成：
    Thread 1  正在读取：
    Thread 2  正在读取：
    Thread 4  写入完成：
    Thread 1  正在读取：null
    Thread 2  正在读取：null
    Thread 3  正在读取：
    Thread 3  正在读取：null
    Thread 4  正在读取：
    Thread 4  正在读取：null
    ```

    发现某个线程进行写操作时，会被打断。显然不符合实际。

    java.util.concurrent.locks包下有三个接口，使用Lock的话，会牺牲读的性能.

    因此，使用读写锁，**ReadWriteLock接口**的其中一个实现类**ReentrantReadWriteLock**

    > [java.util.concurrent.locks](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/package-summary.html)
    >
    > ## Interfaces
    >
    > 
    >
    > - [*Condition*](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/Condition.html)
    > - [*Lock*](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/Lock.html)
    > - [*ReadWriteLock*](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/ReadWriteLock.html)
    >
    > 
    >
    > ## Classes
    >
    > 
    >
    > - [AbstractOwnableSynchronizer](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/AbstractOwnableSynchronizer.html)
    > - [AbstractQueuedLongSynchronizer](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/AbstractQueuedLongSynchronizer.html)
    > - [AbstractQueuedSynchronizer](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/AbstractQueuedSynchronizer.html)
    > - [LockSupport](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/LockSupport.html)
    > - [ReentrantLock](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/ReentrantLock.html)
    > - [ReentrantReadWriteLock](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html)
    > - [ReentrantReadWriteLock.ReadLock](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.ReadLock.html)
    > - [ReentrantReadWriteLock.WriteLock](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.WriteLock.html)
    > - [StampedLock](file:///D:/documents/电子书/docs/api/java/util/concurrent/locks/StampedLock.html)

3. 使用读写锁解决问题

   

``` java
   package com.pandahi.juc;
   
   import java.util.HashMap;
   import java.util.Map;
   import java.util.concurrent.TimeUnit;
   import java.util.concurrent.locks.Lock;
   import java.util.concurrent.locks.ReentrantLock;
   import java.util.concurrent.locks.ReentrantReadWriteLock;
   
   /**

    - 多个线程同时读一个资源类没有任何问题，所以为了满足并发量，读取共享资源应该允许同时进行
    - 但是
    - 如果有一个线程想要写共享资源，就不应该再有其他线程对资源进行读或写
    - 总结
    - 读读可共存
    - 读写不可共存
    - 写写不可共存
    - <p>
    - 写操作：原子+独占，整个过程必须是一个完整的过程，不能被加塞、中断

    *

    - @Author: pandaHi
    - @Date: 2019/12/25

    */
   public class ReadWriteLockDemo {
       public static void main(String[] args) {
           MyCache myCache = new MyCache();
   
           // 5个线程读，5个线程写
           for (int i = 0; i < 5; i++) {
               // lambda表达式要求是final类型的，所以提前赋值final型的临时变量
               final int tempInt = i;
               new Thread(() -> {
                   myCache.put(tempInt + "", tempInt);
               }, "Thread " + i).start();
   
           }
   
           for (int i = 0; i < 5; i++) {
               // lambda表达式要求是final类型的，所以提前赋值final型的临时变量
               final int tempInt = i;
               new Thread(() -> {
                   myCache.get(tempInt + "");
               }, "Thread " + i).start();
   
           }
       }
   }
   
   
   /**

    - 缓存资源类
    - 同一时刻，可供多人读，但是仅供一人写

    */
   class MyCache {
   
       // volatile 保证可见性
       private volatile Map<String, Object> map = new HashMap<>();
   
       // 可以保证一致性安全性，但是失去了多个线程读的并发性能
       // private Lock lock = new ReentrantLock();
   
       private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
   
   
       /**

        * 写操作必须保证原子性，独占不可分割。即，同一线程的"正在写入"、"写入完成"是连续的

        */
       public void put(String key, Object value) {
           rwLock.writeLock().lock();
           try {
               System.out.println(Thread.currentThread().getName() + "  正在写入：" + key);
               // 模拟网络时延500毫秒
               try {
                   TimeUnit.MICROSECONDS.sleep(500);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               map.put(key, value);
               System.out.println(Thread.currentThread().getName() + "  写入完成：" + value);
           } catch (Exception e) {
               e.printStackTrace();
           } finally {
               rwLock.writeLock().unlock();
           }
   
       }
   
       /**

        * 读是可以共享的，各个线程不一样

        *

        * @param key

        */
       public void get(String key) {
           rwLock.readLock().lock();
   
           try {
               System.out.println(Thread.currentThread().getName() + "  正在读取……");
               // 模拟网络时延500毫秒
               try {
                   TimeUnit.MICROSECONDS.sleep(500);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               Object result = map.get(key);
               System.out.println(Thread.currentThread().getName() + "  读取完成：" + result);
           } catch (Exception e) {
               e.printStackTrace();
           } finally {
               rwLock.readLock().unlock();
           }
       }
   
       /**

        * 模拟清空缓存

     * 
        */
    public void clear() {
           map.clear();
       }
   }
   ```

   
   
   
   运行结果：
   
   

``` 
   Thread 0  正在写入：0
   Thread 0  写入完成：0
   Thread 1  正在写入：1
   Thread 1  写入完成：1
   Thread 2  正在写入：2
   Thread 2  写入完成：2
   Thread 3  正在写入：3
   Thread 3  写入完成：3
   Thread 4  正在写入：4
   Thread 4  写入完成：4
   Thread 0  正在读取……
   Thread 1  正在读取……
   Thread 2  正在读取……
   Thread 4  正在读取……
   Thread 3  正在读取……
   Thread 1  读取完成：1
   Thread 4  读取完成：4
   Thread 2  读取完成：2
   Thread 0  读取完成：0
   Thread 3  读取完成：3
   ```

### 5. 死锁编码及定位分析

1. 是什么

   ​    死锁是指两个或两个以上的线程在执行过程中，因争夺资源而造成的一种互相等待的现象，若无外力干涉那它们都将无法继续推进下去。

2. 代码实现

   

``` java
   package com.interview.thread;
   
   
   import java.util.concurrent.TimeUnit;
   
   class HoldLockThread implements Runnable {
   
       private String lockA;
       private String lockB;
   
       public HoldLockThread(String lockA, String lockB) {
           this.lockA = lockA;
           this.lockB = lockB;
       }
   
       @Override
       public void run() {
   
           synchronized (lockA) {
               System.out.println(Thread.currentThread().getName() + "  自己持有：" + lockA + "   尝试获得：" + lockB);
               try {
                   TimeUnit.SECONDS.sleep(2);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               synchronized (lockB) {
                   System.out.println(Thread.currentThread().getName() + "  自己持有：" + lockB + "   尝试获得：" + lockA);
   
               }
           }
       }
   }
   
   
   /**

    - 死锁是指两个或两个以上的进程在执行过程中，
    - 因争夺资源而造成的一种互相等待的现象，
    - 若无外力干涉那它们都将无法继续推进下去

    *

    - @Author: pandaHi
    - @Date: 2019/12/20

    */
   public class DeadLockDemo {
       public static void main(String[] args) {
           String lockA = "lockA";
           String lockB = "lockB";
   
           new Thread(new HoldLockThread(lockA, lockB), "thread AAA").start();
           new Thread(new HoldLockThread(lockB, lockA), "thread BBB").start();
   
           /**

            * 死锁检测
            * Linux下   ps -ef|grep xxx         ls -l

            *

            * windows   jps(即Java ps)      jps -l    jstack  编号（查看具体的错误信息）

            */
       }
   
   }
   ```

   运行结果：

   

``` 
   thread AAA  自己持有：lockA   尝试获得：lockB
   thread BBB  自己持有：lockB   尝试获得：lockA
   ```

3. 定位和解决

   - jps -l 命令定位进程号

     

``` 
     D:\ideaCode\javaSE>jps -l
     1524
     15416 org.jetbrains.jps.cmdline.Launcher
     16648 com.interview.thread.DeadLockDemo
     5528 org.jetbrains.kotlin.daemon.KotlinCompileDaemon
     7320 sun.tools.jps.Jps
     ```

   - jstack 16648 查看具体情况

     

``` 
     D:\ideaCode\javaSE>jstack 16648
     2019-12-27 09:48:53
     Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.191-b12 mixed mode):
     
     "DestroyJavaVM" #13 prio=5 os_prio=0 tid=0x0000000002bd2800 nid=0x47b0 waiting on condition [0x0000000000000000]
        java.lang.Thread.State: RUNNABLE
     
     "thread BBB" #12 prio=5 os_prio=0 tid=0x000000001b1d1800 nid=0x36c0 waiting for monitor entry [0x000000001becf000]
        java.lang.Thread.State: BLOCKED (on object monitor)
             at com.interview.thread.HoldLockThread.run(DeadLockDemo.java:27)
             - waiting to lock <0x0000000780945f88> (a java.lang.String)
             - locked <0x0000000780945fc0> (a java.lang.String)
             at java.lang.Thread.run(Thread.java:748)
     
     "thread AAA" #11 prio=5 os_prio=0 tid=0x000000001b1cf000 nid=0x41c4 waiting for monitor entry [0x000000001bdce000]
        java.lang.Thread.State: BLOCKED (on object monitor)
             at com.interview.thread.HoldLockThread.run(DeadLockDemo.java:27)
             - waiting to lock <0x0000000780945fc0> (a java.lang.String)
             - locked <0x0000000780945f88> (a java.lang.String)
             at java.lang.Thread.run(Thread.java:748)
     
     "Service Thread" #10 daemon prio=9 os_prio=0 tid=0x000000001b1c4800 nid=0x3378 runnable [0x0000000000000000]
        java.lang.Thread.State: RUNNABLE
     
     "C1 CompilerThread2" #9 daemon prio=9 os_prio=2 tid=0x000000001b136800 nid=0x3300 waiting on condition [0x0000000000
     000000]
        java.lang.Thread.State: RUNNABLE
     
     "C2 CompilerThread1" #8 daemon prio=9 os_prio=2 tid=0x000000001b12c800 nid=0xb04 waiting on condition [0x00000000000
     00000]
        java.lang.Thread.State: RUNNABLE
     
     "C2 CompilerThread0" #7 daemon prio=9 os_prio=2 tid=0x000000001b129800 nid=0x8fc waiting on condition [0x00000000000
     00000]
        java.lang.Thread.State: RUNNABLE
     
     "Monitor Ctrl-Break" #6 daemon prio=5 os_prio=0 tid=0x000000001b126800 nid=0xdec runnable [0x000000001b7ce000]
        java.lang.Thread.State: RUNNABLE
             at java.net.SocketInputStream.socketRead0(Native Method)
             at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
             at java.net.SocketInputStream.read(SocketInputStream.java:171)
             at java.net.SocketInputStream.read(SocketInputStream.java:141)
             at sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:284)
             at sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:326)
             at sun.nio.cs.StreamDecoder.read(StreamDecoder.java:178)
             - locked <0x0000000780a2bc40> (a java.io.InputStreamReader)
             at java.io.InputStreamReader.read(InputStreamReader.java:184)
             at java.io.BufferedReader.fill(BufferedReader.java:161)
             at java.io.BufferedReader.readLine(BufferedReader.java:324)
             - locked <0x0000000780a2bc40> (a java.io.InputStreamReader)
             at java.io.BufferedReader.readLine(BufferedReader.java:389)
             at com.intellij.rt.execution.application.AppMainV2$1.run(AppMainV2.java:64)
     
     "Attach Listener" #5 daemon prio=5 os_prio=2 tid=0x0000000019dc7800 nid=0x3148 waiting on condition [0x0000000000000
     000]
        java.lang.Thread.State: RUNNABLE
     
     "Signal Dispatcher" #4 daemon prio=9 os_prio=2 tid=0x000000001b0e0800 nid=0x3f20 runnable [0x0000000000000000]
        java.lang.Thread.State: RUNNABLE
     
     "Finalizer" #3 daemon prio=8 os_prio=1 tid=0x0000000002cc5800 nid=0x20e4 in Object.wait() [0x000000001b0cf000]
        java.lang.Thread.State: WAITING (on object monitor)
             at java.lang.Object.wait(Native Method)
             - waiting on <0x0000000780808ed0> (a java.lang.ref.ReferenceQueue$Lock)
             at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:144)
             - locked <0x0000000780808ed0> (a java.lang.ref.ReferenceQueue$Lock)
             at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:165)
             at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:216)
     
     "Reference Handler" #2 daemon prio=10 os_prio=2 tid=0x0000000002cc2000 nid=0x32bc in Object.wait() [0x000000001afce0
     00]
        java.lang.Thread.State: WAITING (on object monitor)
             at java.lang.Object.wait(Native Method)
             - waiting on <0x0000000780806bf8> (a java.lang.ref.Reference$Lock)
             at java.lang.Object.wait(Object.java:502)
             at java.lang.ref.Reference.tryHandlePending(Reference.java:191)
             - locked <0x0000000780806bf8> (a java.lang.ref.Reference$Lock)
             at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)
     
     "VM Thread" os_prio=2 tid=0x0000000019d38000 nid=0x4168 runnable
     
     "GC task thread#0 (ParallelGC)" os_prio=0 tid=0x0000000002be8000 nid=0x217c runnable
     
     "GC task thread#1 (ParallelGC)" os_prio=0 tid=0x0000000002bea000 nid=0x3dd4 runnable
     
     "GC task thread#2 (ParallelGC)" os_prio=0 tid=0x0000000002beb800 nid=0x14ec runnable
     
     "GC task thread#3 (ParallelGC)" os_prio=0 tid=0x0000000002bed000 nid=0x2a70 runnable
     
     "VM Periodic Task Thread" os_prio=2 tid=0x000000001b1c8800 nid=0x3f04 waiting on condition
     
     JNI global references: 12
     
     
     Found one Java-level deadlock:
     =============================
     "thread BBB":
       waiting to lock monitor 0x0000000019d5dce8 (object 0x0000000780945f88, a java.lang.String),
       which is held by "thread AAA"
     "thread AAA":
       waiting to lock monitor 0x0000000019d60628 (object 0x0000000780945fc0, a java.lang.String),
       which is held by "thread BBB"
     
     Java stack information for the threads listed above:
     ===================================================
     "thread BBB":
             at com.interview.thread.HoldLockThread.run(DeadLockDemo.java:27)
             - waiting to lock <0x0000000780945f88> (a java.lang.String)
             - locked <0x0000000780945fc0> (a java.lang.String)
             at java.lang.Thread.run(Thread.java:748)
     "thread AAA":
             at com.interview.thread.HoldLockThread.run(DeadLockDemo.java:27)
             - waiting to lock <0x0000000780945fc0> (a java.lang.String)
             - locked <0x0000000780945f88> (a java.lang.String)
             at java.lang.Thread.run(Thread.java:748)
     
     Found 1 deadlock.
     ```

     可以看到死锁具体情况。

## 六、CountDownLatch/CyclicBarrier/Semaphore

### 1. CountDownLatch

> A synchronization aid that allows one or more threads to wait until a set of operations being performed in other threads completes.
>
> A `CountDownLatch` is initialized with a given *count*. The [ `await` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/CountDownLatch.html#await--) methods block until the current count reaches **zero** due to invocations of the [ `countDown()` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/CountDownLatch.html#countDown--) method, after which all waiting threads are released and any subsequent invocations of [ `await` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/CountDownLatch.html#await--) return immediately. This is a one-shot phenomenon -- the count cannot be reset. If you need a version that resets the count, consider using a [ `CyclicBarrier` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/CyclicBarrier.html).

1. 它允许一个或多个线程一直等待，直到其他线程的操作执行完后再执行。例如，应用程序的主线程希望在负责启动框架服务的线程已经启动所有的框架服务之后再执行。

2. CountDownLatch主要有两个方法，当一个或多个线程调用await()方法时，调用线程会被阻塞。其他线程调用countDown()方法会将计数器减1，当计数器的值变为0时，因调用await()方法被阻塞的线程才会被唤醒，继续执行。

3. 代码示例

   

``` java
   package com.pandahi.juc;
   
   import java.util.concurrent.CountDownLatch;
   import java.util.concurrent.TimeUnit;
   
   /**

    - @Author: pandaHi
    - @Date: 2019/12/25

    */
   public class CountDownLatchDemo {
       public static void main(String[] args) throws InterruptedException {
   
           // 正常实现
           // countDownLatchTest();
           // 正常实现
           // general();
           // 错误的，不能保证main线程最后执行
           error();
   
   
       }
   
       public static void error() {
           for (int i = 0; i < 6; i++) {
               new Thread(() -> {
                   System.out.println(Thread.currentThread().getName() + "  上完自习，离开教室");
               }, "Thread " + i).start();
           }
           System.out.println(Thread.currentThread().getName() + "  班长最后关门走人");
       }
   
       public static void general() {
           for (int i = 0; i < 6; i++) {
               new Thread(() -> {
                   System.out.println(Thread.currentThread().getName() + "  上完自习，离开教室");
               }, "Thread " + i).start();
           }
           // 活跃线程>2，main线程将一直在while循环中，直到小于等于2，才跳出while，继续执行
           while (Thread.activeCount() > 2) {
               try {
                   TimeUnit.SECONDS.sleep(1);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
           System.out.println(Thread.currentThread().getName() + "  班长最后关门走人");
       }
   
   
       public static void countDownLatchTest() throws InterruptedException {
           CountDownLatch countDownLatch = new CountDownLatch(6);
   
           for (int i = 0; i < 6; i++) {
               new Thread(() -> {
                   System.out.println(Thread.currentThread().getName() + "  上完自习，离开教室");
                   countDownLatch.countDown();
               }, "Thread " + i).start();
           }
   
           // main线程代表班长，在计数减到0时，才唤醒
           countDownLatch.await();
           System.out.println(Thread.currentThread().getName() + "  *****班长最后关门走人");
       }
   }
   ```

### 2. CyclicBarrier（循环屏障--集齐七颗龙珠召唤神龙）

1. CycliBarrier(效果类比：人到齐了才开会/集齐七颗龙珠召唤神龙)

   可循环（Cyclic）使用的屏障。让一组线程到达一个屏障（也可叫同步点）时被阻塞，知道最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续干活，线程进入屏障通过CycliBarrier的await()方法

2. 代码示例

   

``` java
   package com.pandahi.juc;
   
   import java.util.concurrent.BrokenBarrierException;
   import java.util.concurrent.CyclicBarrier;
   
   /**

    - @Author: pandaHi
    - @Date: 2019/12/25

    */
   public class CyclicBarrierDemo {
       public static void main(String[] args) {
           // CyclicBarrier(int parties, Runnable barrierAction)构造器
           CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
               System.out.println("********召唤神龙");
           });
           for (int i = 1; i <= 7; i++) {
               final int tempInt = i;
               new Thread(() -> {
                   System.out.println(Thread.currentThread().getName() + "  收集到第：" + tempInt + "龙珠");
                   // 先收集到的等待
                   try {
                       cyclicBarrier.await();
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   } catch (BrokenBarrierException e) {
                       e.printStackTrace();
                   }
               }, "Thread " + i).start();
           }
       }
   }
   
   ```

   运行结果：

   

``` 
   Thread 1  收集到第：1龙珠
   Thread 3  收集到第：3龙珠
   Thread 2  收集到第：2龙珠
   Thread 4  收集到第：4龙珠
   Thread 5  收集到第：5龙珠
   Thread 6  收集到第：6龙珠
   Thread 7  收集到第：7龙珠
   ********召唤神龙
   ```

### 3. Semaphore（信号量）

可以代替Synchronize和Lock

1. 是什么？

   信号量的主要用户两个目的, 一个是用于**多个共享资源的互斥使用**，另一个用于**并发线程数的控制**

2. 构造器：

   默认是非公平锁，如果加塞加不到，则变为公平锁。

| Constructor and Description                                                                                                        |
|------------------------------------------------------------------------------------------------------------------------------------|
| `Semaphore(int permits)` <br>Creates a `Semaphore` with the given number of permits and nonfair fairness setting.|
| `Semaphore(int permits,boolean fair)` <br>Creates a `Semaphore` with the given number of permits and the given fairness setting.|

3. 代码案例：抢车位--6车3车位

   

``` java
   package com.pandahi.juc;
   
   import java.util.concurrent.Semaphore;
   import java.util.concurrent.TimeUnit;
   
   /**

    - 抢车位案例

    *

    - @Author: pandaHi
    - @Date: 2019/12/25

    */
   public class SemaphoreDemo {
       public static void main(String[] args) {
           // Semaphore semaphore = new Semaphore();
           Semaphore semaphore = new Semaphore(3);//模拟三个停车位
           for (int i = 1; i <= 6; i++) {//模拟6部汽车
               new Thread(() -> {
                   try {
                       semaphore.acquire();
                       System.out.println(Thread.currentThread().getName() + "  抢到车位");
                       try {
                           TimeUnit.SECONDS.sleep(3);//停车3s
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                       System.out.println(Thread.currentThread().getName() + "  停车3s后离开车位");
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   } finally {
                       // 释放车位
                       semaphore.release();
                   }
               }, "Car " + i).start();
           }
   
       }
   }
   ```

   运行结果：

   

``` 
   Car 1  抢到车位
   Car 2  抢到车位
   Car 3  抢到车位
   Car 2  停车3s后离开车位
   Car 4  抢到车位
   Car 3  停车3s后离开车位
   Car 5  抢到车位
   Car 1  停车3s后离开车位
   Car 6  抢到车位
   Car 6  停车3s后离开车位
   Car 5  停车3s后离开车位
   Car 4  停车3s后离开车位
   ```

## 七、阻塞队列

### 1. 特点

阻塞队列在数据结构钟所起的作用大致如下：

![image-20191225163833098](.\images\image-20191225163833098.png)

* 当阻塞队列是空时, 从队列中获取元素的操作将会被阻塞.

  当阻塞队列是满时, 往队列中添加元素的操作将会被阻塞.

* 试图从空的阻塞队列钟获取元素的线程也将会被阻塞，直到其他的线程往空的队列插入新的元素。

  同样
  试图往已满的阻塞队列中添加新元素的线程同样也会被阻塞，直到其他线程从队列中移除一个或者多个元素或者全清空队列后使队列重新变得空闲起来并后续新增。

### 2. 为什么用，有什么好处？？

1. 在多线程领域，所谓阻塞，在某些情况下会挂起线程，一旦满足条件，被挂起的线程又会自动被唤醒。

2. 为什么需要BlockingQueue？

   好处时我们不需要关心什么时候需要阻塞线程，什么时候需要唤醒线程，因为这一切BlockingQueue都给你一手包办了。

   在concurrent包发布以前，在多线程环境下，程序员必须自己控制这些细节，尤其还要兼顾效率和线程安全，复杂易出错。

### 3. BlockingQueue的核心方法

|             | *Throws exception*                                           | *Special value*                                              | *Blocks*                                                     | *Times out*                                                  |
| ----------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **Insert**  | [ `add(e)` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/BlockingQueue.html#add-E-) | [ `offer(e)` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/BlockingQueue.html#offer-E-) | [ `put(e)` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/BlockingQueue.html#put-E-) | [ `offer(e, time, unit)` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/BlockingQueue.html#offer-E-long-java.util.concurrent. TimeUnit-) |
| **Remove**  | [ `remove()` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/BlockingQueue.html#remove-java.lang. Object-) | [ `poll()` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/BlockingQueue.html#poll-long-java.util.concurrent. TimeUnit-) | [ `take()` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/BlockingQueue.html#take--) | [ `poll(time, unit)` ](file:///D:/documents/电子书/docs/api/java/util/concurrent/BlockingQueue.html#poll-long-java.util.concurrent. TimeUnit-) |
| **Examine** | [ `element()` ](file:///D:/documents/电子书/docs/api/java/util/Queue.html#element--) | [ `peek()` ](file:///D:/documents/电子书/docs/api/java/util/Queue.html#peek--) | *not applicable*                                             | *not applicable*                                             |

| 抛出异常 | 当阻塞队列满时, 再往队列里面add插入元素会抛IllegalStateException: Queue full<br/>当阻塞队列空时, 再往队列Remove元素时候回抛出NoSuchElementException |
| -------- | ------------------------------------------------------------ |
| 特殊值   | 插入方法, 成功返回true 失败返回false<br/>移除方法, 成功返回元素, 队列里面没有就返回null |
| 一直阻塞 | 当阻塞队列满时, 生产者继续往队列里面put元素, 队列会一直阻塞直到put数据or响应中断退出<br/>当阻塞队列空时, 消费者试图从队列take元素, 队列会一直阻塞消费者线程直到队列可用.|
| 超时退出 | 当阻塞队列满时, 队列会阻塞生产者线程一定时间, 超过后限时后生产者线程就会退出 |

### 4. 架构梳理

1. 架构

![image-20191225165213580](.\images\image-20191225165213580.png)

2. 种类分析

   - **ArrayBlockingQueue:** 由数组结构组成的有界阻塞队列.

   - **LinkedBlockingDeque**: 由链表结构组成的有界(但大小默认值Integer>MAX_VALUE)阻塞队列.

   - PriorityBlockingQueue: 支持优先级排序的无界阻塞队列.

   - DelayQueue: 使用优先级队列实现的延迟无界阻塞队列.

   - **SynchronousQueue**: 不存储元素的阻塞队列, 也即是单个元素的队列.（有且只有一个，生产一个消费一个。）

`SynchronousQueue没有容量与其他BlcokingQueue不同，SynchronousQueue是一个不存储元素的BlcokingQueue。每个put操作必须要等待一个take操作，否则阻塞，不能继续添加元素，反之亦然` 

   - LinkedTransferQueue: 由链表结构组成的无界阻塞队列.

    - LinkedBlockingDeque:由链表结构组成的双向阻塞队列.

### 5. 应用场景

* 生产者消费者模式
* 线程池
* 消息中间件

#### 1. 生产者消费者模式——传统版

![image-20191225181116243](.\images\image-20191225181116243.png)

   

``` java
package com.pandahi.juc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 描述：一个初始值为0的变量，两个线程对其进行交替操作，一个加1，一个减1，来 5 轮
 * <p>
 * 关键：
 * 1  线程    操作(方法)    资源类
 * 2  判断    干活    通知
 * 3  防止虚假唤醒机制
 *
 * @Author: pandaHi
 * @Date: 2019/12/25
 */
public class ProdConsumerTraditionDemo {
    public static void main(String[] args) {

        ShareData shareData = new ShareData();

        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                shareData.increment();
            }
        }, "AAA 生产").start();

        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                shareData.decrement();
            }
        }, "BBB 消费").start();
    }
}

/**
 * 资源类
 */
class ShareData {
    private int num = 0;
    private Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();

    /**
     * 加操作，模拟生产过程
     */
    public void increment() {

        // 相当于同步代码块加锁
        lock.lock();

        try {
            // 1 判断
            while (num != 0) {
                // 等待，不能生产
                condition.await();
            }
            // 2 干活。当前num为0了，需要进行生产，即加操作
            num++;
            System.out.println(Thread.currentThread().getName() + "  " + num);
            // 3 通知唤醒
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    /**
     * 减操作，模拟生产过程
     */
    public void decrement() {

        // 相当于同步代码块加锁
        lock.lock();

        try {
            // 1 判断
            while (num == 0) {
                // 等待，不能消费
                condition.await();
            }
            // 2 干活。消费完成，num减为0
            num--;
            System.out.println(Thread.currentThread().getName() + "  " + num);
            // 3 通知唤醒
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

}
```

运行结果：(生产一个消费一个)

``` 
AAA 生产  1
BBB 消费  0
AAA 生产  1
BBB 消费  0
AAA 生产  1
BBB 消费  0
AAA 生产  1
BBB 消费  0
AAA 生产  1
BBB 消费  0
```

#### 2. 生产者消费者模式——阻塞队列版

知识点：volatile、CAS、atomicInteger、BlockQueue、线程交互、原子引用

``` java
package com.pandahi.juc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * volatile/CAS/atomicInteger/BlockQueue/线程交互/原子引用
 *
 * @Author: pandaHi
 * @Date: 2019/12/26
 */
public class ProdConsumerBlockQueueDemo {
    public static void main(String[] args) throws Exception {
        MyResource myResource = new MyResource(new ArrayBlockingQueue<>(10));

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "  生产者线程启动");
            try {
                myResource.myProd();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Prod").start();

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "  消费者线程启动");
            System.out.println();
            System.out.println();
            try {
                myResource.myConsumer();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, "Consumer").start();

        // 暂停一会线程
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();
        System.out.println();
        System.out.println("5秒时间到，main线程调用停止方法，结束生产消费活动");
        myResource.stop();

    }
}

class MyResource {
    // 默认开启，进行生产+消费过程
    private volatile boolean FLAG = true;
    private AtomicInteger atomicInteger = new AtomicInteger();

    /**
     * 接口的思想：通过接口，适配所有实现BlockingQueue接口的类。
     * <p>
     * 原则：写，足够的抽象往高处写；查，足够的落地往细节落地。
     * <p>
     * 声明接口为null，将适配范围放宽；通过构造器注入实际的实现类。
     * 传入array、link等等，均能满足，提高程序的高适配特性。
     */
    BlockingQueue<String> blockingQueue = null;

    public MyResource(BlockingQueue<String> blockingQueue) {
        this.blockingQueue = blockingQueue;
        // 通过反射，方便后台排错时，查询传入的实际类的名字
        System.out.println(blockingQueue.getClass().getName());
    }

    /**
     * 生产动作
     *
     * @throws Exception
     */
    public void myProd() throws Exception {
        // while外声明，实现复用
        String data = null;
        boolean retValue;
        while (FLAG) {
            data = atomicInteger.incrementAndGet() + "";
            retValue = blockingQueue.offer(data, 2L, TimeUnit.SECONDS);
            if (retValue) {
                System.out.println(Thread.currentThread().getName() + "\t 插入队列 " + data + " 成功");
            } else {
                System.out.println(Thread.currentThread().getName() + "\t 插入队列 " + data + " 失败");
            }
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println(Thread.currentThread().getName() + "\t 大Boss叫停，表示FLAG = false，生产动作结束");
    }

    /**
     * 消费动作
     *
     * @throws Exception
     */
    public void myConsumer() throws Exception {
        // 生产和消费受同一个FLAG控制，要么都进行，要么都结束
        String result = null;
        while (FLAG) {

            result = blockingQueue.poll(2L, TimeUnit.SECONDS);
            if (null == result || result.equalsIgnoreCase("")) {
                FLAG = false;
                System.out.println(Thread.currentThread().getName() + "\t 超过2秒钟没有取到蛋糕，消费退出");
                return;
            }
            System.out.println((Thread.currentThread().getName() + "\t 消费队列蛋糕 " + result + " 成功"));
        }
    }

    /**
     * 终止生产和消费
     */
    public void stop() {
        FLAG = false;
        System.out.println("FLAG的值被修改为false");
    }
}
```

运行结果：（注意由于是打印，所以打印的顺序可能不是最理想的顺序）

``` 
Prod  生产者线程启动
Consumer  消费者线程启动

Prod	 插入队列 1 成功
Consumer	 消费队列蛋糕 1 成功
Prod	 插入队列 2 成功
Consumer	 消费队列蛋糕 2 成功
Prod	 插入队列 3 成功
Consumer	 消费队列蛋糕 3 成功
Prod	 插入队列 4 成功
Consumer	 消费队列蛋糕 4 成功

Prod	 插入队列 5 成功
5秒时间到，main线程调用停止方法，结束生产消费活动
FLAG的值被修改为false
Consumer	 消费队列蛋糕 5 成功
Prod	 大Boss叫停，表示FLAG = false，生产动作结束
```

### 6.lock和synchronized有什么区别？？用新的lock有什么好处？?

#### 1. 区别主要有以下几个方面：

1. 原始构成

   synchronized是关键字，属于jvm层面。

   ​	monitorenter（底层通过monitor对象来完成，其实wait/notify等方法也依赖于monitor对象只有在同步块或方法中，才能调用wait/notify等，可以查看javap指令查看底层执行过程）

   Lock是具体类（java.util.concurrent.locks.lock），是api层面的锁

2. 使用方法

   synchronized不需要用户手动释放锁，当synchronized代码指定完后，系统会自动让线程释放对锁的占用；

   Reentrantlock需要手动加锁和释放锁，lock和unlock，配合try/finally语句块

3. 等待是否可中断

   synchronized不可中断，除非抛出异常或者正常运行结束；

   Reentrantlock可中断，可以

   (1) 设置超时方法tryLock(Long timeout, TimeUnit unit) 

   (2) lockInterruptibly()放代码中，调用interrupt()方法可中断

4. 加锁是否公平

   synchronized非公平锁

   Reentrantlock默认非公平锁。查看源码构造方法可知，可以通过传参true 公平锁，false为非公平锁(默认)

5. 锁绑定多个条件Condition

   synchronized没有，只能随机唤醒一个或唤醒所有；

   Reentrantlock可以借助Condition类，设置条件，用来实现分组唤醒需要唤醒的线程们，可以更加精确地控制唤醒。

#### 2. 代码案例

要求：多个线程之间按顺序调用，实现A -> B -> C三个线程启动，

A打印5次，B打印10次，C打印15次……进行10轮。

``` java
package com.pandahi.juc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 要求：多线程之间调用，实现A->B->C三个线程启动，要求如下：
 * A 打印5次，B 打印10次，C 打印15次
 * 紧接着
 * A 打印5次，B 打印10次，C 打印15次
 * ……
 * 循环10轮
 *
 * @Author: pandaHi
 * @Date: 2019/12/26
 */
public class SyncAndReentrantLockDemo {
    public static void main(String[] args) {
        ShareRes shareRes = new ShareRes();

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                shareRes.print10();
            }
        }, "B").start();

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                shareRes.print5();
            }
        }, "A").start();

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                shareRes.print15();
            }
        }, "c").start();

    }

}

class ShareRes {
    private int num = 1;
    private Lock lock = new ReentrantLock();

    /**
     * 相当于三把备用钥匙
     */
    Condition c1 = lock.newCondition();
    Condition c2 = lock.newCondition();
    Condition c3 = lock.newCondition();

    public void print5() {
        lock.lock();

        try {
            // 1 判断
            while (num != 1) {
                // 说明进入的不是线程A，所以在循环中等待
                c1.await();
            }
            // 2 干活 打印5次
            for (int i = 1; i <= 5; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            // 3 通知唤醒。修改标志变量，并通知下一个线程
            num = 2;
            c2.signal();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void print10() {
        lock.lock();
        try {
            // 1 判断
            while (num != 2) {
                // 说明进入的不是线程A，所以在循环中等待
                c2.await();
            }
            // 2 干活 打印5次
            for (int i = 1; i <= 10; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            // 3 通知唤醒。修改标志变量，并通知下一个线程
            num = 3;
            c3.signal();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void print15() {
        lock.lock();

        try {
            // 1 判断
            while (num != 3) {
                // 说明进入的不是线程A，所以在循环中等待
                c3.await();
            }
            // 2 干活 打印5次
            for (int i = 1; i <= 15; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            // 3 通知唤醒。修改标志变量，并通知下一个线程
            num = 1;
            c1.signal();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
```

运行结果：

``` 
A	1
A	2
A	3
A	4
A	5
B	1
B	2
B	3
B	4
B	5
B	6
B	7
B	8
B	9
B	10
c	1
c	2
c	3
c	4
c	5
c	6
c	7
c	8
c	9
c	10
c	11
c	12
c	13
c	14
c	15
…………// 总计15轮
```

## 八、线程池

#### 回顾：创建线程的几种方式

1. 继承Thread类

2. 实现Runnable接口

3. 实现Callable（有返回值）

   

``` java
   public class CallableDemo {
       public static void main(String[] args) throws ExecutionException, InterruptedException {
           // 在 FutureTask 中传入 Callable 的实现类
           FutureTask<Integer> futureTask = new FutureTask<>(new Callable<Integer>() {
               @Override
               public Integer call() throws Exception {
                   return 666;
               }
           });
           // 把 futureTask 放入线程中
           new Thread(futureTask).start();
           // 获取结果
           Integer res = futureTask.get();
           System.out.println(res);
       }
   }
   ```

4. 线程池

### 1. Callable接口的使用

``` java
package com.interview.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @Author: pandaHi
 * @Date: 2019/12/19
 */

class MyThread2 implements Runnable {
    @Override
    public void run() {
    }
}

class MyThread implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println(Thread.currentThread().getName() + "******come in Callable");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return 1024;
    }
}

public class CallableDemo {
    public static void main(String[] args) throws Exception {
        // 两个线程，一个main，一个AA(futureTask)
        // FutureTask实现了RunnableFuture，RunnableFuture继承了runnable接口
        FutureTask<Integer> futureTask = new FutureTask<>(new MyThread());
        // 使用同一个futureTask那么就只进入一次
        new Thread(futureTask, "AA").start();
        new Thread(futureTask, "BB").start();

        System.out.println(Thread.currentThread().getName() + "**********************");
        int result01 = 100;

        // 类似自旋锁，判断futureTask是否执行完成
        // while (!futureTask.isDone()) {
        //
        // }

        // 要求获得callable线程的计算结果，如果没有计算完成就要求，会导致阻塞，直到所调用线程执行完成
        int result02 = futureTask.get();
        System.out.println("**result:**" + (result01 + result02));

    }
}
```

运行结果：

``` 
main**********************
AA******come in Callable
**result:**1124
```

### 2. 为什么使用线程池

1. 线程池用于多线程处理中，它可以根据系统的情况，可以有效控制线程执行的数量，优化运行效果。线程池做的工作主要是控制运行的线程的数量，**处理过程中将任务放入队列**，然后在线程创建后启动这些任务，**如果线程数量超过了最大数量，那么超出数量的线程排队等候**，等其它线程执行完毕，再从队列中取出任务来执行。

2. 主要特点：

   线程复用、控制最大并发数、管理线程

   - 降低资源消耗，通过重复利用已创建的线程来降低线程创建和销毁造成的消耗

      - 提高响应速度，当任务到达时，任务可以不需要等到线程创建就能立即执行
      - 提高线程的可管理性，线程是稀缺资源，如果无限制地创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以实现统一的分配、调优和监控  

### 3. 如何使用线程池

#### 1. 架构实现

​    Java中的线程池是通过Executor框架实现的, 该框架中用到了Executor、**Executors**、ExecutorService、**ThreadPoolExecutor**这几个类.

![image-20191226152101256](.\images\image-20191226152101256.png)

可以类比集合框架的顶级接口Collection接口，**ThreadPoolExecutor**作为java.util.concurrent包对外提供基础实现，以内部线程池的形式对外提供管理任务执行，线程调度，线程池管理等等服务。

#### 2. 重点三种实现方式

实现有五种，Executors.newScheduledThreadPool()是带时间调度的，java8新推出Executors.newWorkStealingPool(int), 使用目前机器上可用的处理器作为他的并行级别。

重点有三种：

* Executors.newFixedThreadPool(int)	一池固定数线程

  **执行长期的任务，性能好很多**

  （1）创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。

  （2）newFixedThreadPool创建的线程池corePoolSize和maximumPoolSize值是相等的，使用的是LinkedBlockingQueue。

  

![image-20191226160026340](.\images\image-20191226160026340.png)

* Executors.newSingleThreadExecutor()    一池一线程

  **一个任务一个任务地执行**

  （1）创建一个单线程化的线程池，只会用唯一的工作线程来执行任务，保证所有任务都是按照执行顺序执行。

  （2） newSingleThreadExecutor将corePoolSize和MaxmumPoolSize都设置为1，它使用的的LinkedBlockingQueue。

  

![image-20191226155823860](.\images\image-20191226155823860.png)

* Executors.newCachedThreadPool()    一池N线程

  **执行很多短期异步的小程序或负载较轻的服务器**

  （1）创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则创建新线程。

  （2）newCachedThreadPool将corePoolSize设置为0，MaxmumPoolSize设置为Integer. MAX_VALUE。它使用的是SynchronousQueue, 也就是说来了任务就创建线程运行，如果线程空闲超过60秒，就销毁线程。

  

![image-20191226155914244](.\images\image-20191226155914244.png)

  

#### 3. 代码示例

``` java
package com.pandahi.juc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: pandaHi
 * @Date: 2019/12/26
 */
public class MyThreadPoolDemo {
    public static void main(String[] args) {
        // ExecutorService threadPool = Executors.newSingleThreadExecutor();  // 一池一线程

        // ExecutorService threadPool = Executors.newFixedThreadPool(5);  // 一池固定数线程

        ExecutorService threadPool = Executors.newCachedThreadPool();  // 一池N线程
        // 标配 使用->关闭   要成对使用
        // 模拟10个用户来办理业务，每个用户就是一个来自外部的请求线程
        try {
            for (int i = 0; i < 100; i++) {
                threadPool.execute(() -> System.out.println(Thread.currentThread().getName() + "\t 办理业务"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

}

```

### 4. 线程池的重要参数

底层的构造器7大参数：

``` java
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) 

```

类比银行办理业务：处理窗口和访问顾客。

1. **corePoolSize**：线程池中常驻核心线程数

   - 在创建了线程池后，当有请求任务来之后，就会安排池中的线程去执行请求任务
   - 当线程池的线程数达到corePoolSize后，就会把到达的任务放到缓存队列当中

2. **maximumPoolSize**：线程池能够容纳同时执行的最大线程数，必须大于等于1
3. **keepAliveTime**：多余的空闲线程的存活时间

   - 当前线程池数量超过corePoolSize时，档口空闲时间达到keepAliveTime值时，多余空闲线程会被销毁直到只剩下corePoolSize个线程为止
   - 默认情况下：只有当线程池中的线程数大于corePoolSize时keepAliveTime才会起作用, 知道线程中的线程数不大于corepoolSIze, 

4. **unit**：keepAliveTime的单位
5. **workQueue**：任务队列，被提交但尚未被执行的任务
6. threadFactory：表示生成线程池中工作线程的线程工厂，用于创建线程。一般用默认的即可
7. handler：拒绝策略，表示当队列满了并且工作线程大于等于线程池的最大线程数（maximumPoolSize）时如何来拒绝请求执行的runable的策略

|                          | 作用                                                         |
| :----------------------- | :----------------------------------------------------------- |
| corePoolSize             | 核心线程池大小                                               |
| maximumPoolSize          | 最大线程池大小                                               |
| keepAliveTime            | 线程池中超过 corePoolSize 数目的空闲线程最大存活时间；可以allowCoreThreadTimeOut(true) 使得核心线程有效时间 |
| TimeUnit                 | keepAliveTime 时间单位                                       |
| workQueue                | 阻塞任务队列                                                 |
| threadFactory            | 新建线程工厂                                                 |
| RejectedExecutionHandler | 当提交任务数超过 maxmumPoolSize+workQueue 之和时，任务会交给RejectedExecutionHandler 来处理 |

### 5. 线程池的底层工作原理（重要）

其中比较容易让人误解的是：corePoolSize，maximumPoolSize，workQueue之间关系。

1. 在创建了线程池之后，等待提交过来的任务请求。

2. 当调用execute()方法添加一个请求任务时，线程池会做出如下判断

   2.1 如果正在运行的线程数量小于corePoolSize，那么马上创建新线程运行这个任务；

   2.2 如果正在运行的线程数量大于或等于corePoolSize，那么将这个任务放入workQueue阻塞队列；

   2.3 如果此时队列满了且运行的线程数小于maximumPoolSize，那么还是要创建非核心线程立刻处理此任务

   2.4 如果队列满了且正在运行的线程数量大于或等于maxmumPoolSize，那么启动拒绝策略来执行

3. 当一个线程完成任务时，它会从队列中取下一个任务来执行

4. 当一个线程无事可做超过一定的时间（keepAliveTime）时，线程池会判断：

   如果当前运行的线程数大于corePoolSize，那么这个线程会被停掉；所以线程池的所有任务完成后它会收缩到corePoolSize的大小。

![img](http://blog.cuzz.site/2019/04/16/Java并发编程/92ad4409-2ab4-388b-9fb1-9fc4e0d832cd.jpg)

![image-20191226163240322](.\images\image-20191226163240322.png)

## 九、线程池——生产上是如何设置合理参数

### 1. 线程池的拒绝策略

#### 1. 是什么

**等待队列已经满了**，再也塞不下新的任务，同时**线程池中的线程数达到了最大线程数**，无法继续为新任务服务。这时，我们需要拒绝策略机制合理的进行处理

#### 2. 拒绝策略

JDK内置的拒绝策略，RejectedExecutionHandler接口的四种实现：

* AbortPolicy（默认）：直接抛出RejectedExecutionException异常，阻止系统正常运行。 
* CallerRunsPolicy：线程调用运行该任务的 execute 本身。此策略既不会抛弃任务，也不会抛出异常，而是提供简单的反馈控制机制，能够减缓新任务的提交速度。
* DiscardPolicy：抛弃队列中等待最久的任务，然后把当前任务加入队列中，尝试再次提交当前任务。 
* DiscardOldestPolicy：直接丢弃任务，不予任何处理也不抛异常。如果允许任务丢失，这是最好的一种方案。

### 2. 工作或实际生产环境中 单一的/固定数的/可变的 三种创建线程池的方式 哪一种用的多？？

**注意：事实上一个都不用，在生产中只能使用自定义的！！！！！！！！！**

**联想到阻塞队列**

Java中的BlockingQueue主要有两种实现，分别是ArrayBlockingQueue 和 LinkedBlockingQueue。

ArrayBlockingQueue是一个用数组实现的有界阻塞队列，必须设置容量。

LinkedBlockingQueue是一个用链表实现的有界阻塞队列，容量可以选择进行设置，不设置的话，将是一个无边界的阻塞队列，最大长度为Integer. MAX_VALUE。

所以，问题的关键就是，如果不设置的话，将是一个无边界的阻塞队列，最大长度为Integer. MAX_VALUE（大概是21亿）。也就是说，如果我们不设置LinkedBlockingQueue的容量的话，其默认容量将会是Integer. MAX_VALUE。

而newFixedThreadPool中创建LinkedBlockingQueue时，并未指定容量。此时，LinkedBlockingQueue就是一个无边界队列，对于一个无边界队列来说，是可以不断的向队列中加入任务的，这种情况下就有可能因为任务过多而导致内存溢出问题。

上面提到的问题主要体现在newFixedThreadPool和newSingleThreadExecutor两个工厂方法上，并不是说newCachedThreadPool和newScheduledThreadPool这两个方法就安全了，这两种方式创建的最大线程数可能是Integer. MAX_VALUE，而创建这么多线程，必然就有可能导致OOM。

**原因：(可以参考阿里巴巴开发规范 )**

> 3.【强制】线程资源必须通过线程池提供，不允许在应用中自行显式创建线程。 说明：线程池的好处是减少在创建和销毁线程上所消耗的时间以及系统资源的开销，解决资源不足的问 题。如果不使用线程池，有可能造成系统创建大量同类线程而导致消耗完内存或者“过度切换”的问题。

> 4.【强制】线程池不允许使用 Executors 去创建，而是通过 ThreadPoolExecutor 的方式，这 样的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险。 说明：Executors 返回的线程池对象的弊端如下： 1） FixedThreadPool 和 SingleThreadPool： 允许的请求队列长度为 Integer. MAX_VALUE，可能会堆积大量的请求，从而导致OOM。
> 2） CachedThreadPool： 允许的创建线程数量为 Integer. MAX_VALUE，可能会创建大量的线程，从而导致OOM。

FixedThreadPool和SingleThreadPool允许请求队列长度为Integer. MAX_VALUE，可能会堆积大量请求；；CachedThreadPool和ScheduledThreadPool允许的创建线程数量为Integer. MAX_VALUE，可能会创建大量线程，导致OOM

### 3. 自定义线程池

``` java
package com.pandahi.juc;

import java.util.concurrent.*;

/**
 * 实际生产生活中的自定义线程池
 *
 * @Author: pandaHi
 * @Date: 2019/12/26
 */
public class MyThreadPoolDemo02 {
    public static void main(String[] args) {

        ExecutorService threadPool = new ThreadPoolExecutor(
                2, 5, 1L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(3), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        // 四种内置的拒绝策略
        // new ThreadPoolExecutor().AbortPolicy();
        // new ThreadPoolExecutor().CallerRunsPolicy();
        // new ThreadPoolExecutor().DiscardOldestPolicy();
        // new ThreadPoolExecutor().DiscardPolicy();

        try {
            // 当同时请求的线程超过设置的最大值和等待队列数之和时，会执行饱和拒绝策略。
            // 即maximumPoolSize=5，new LinkedBlockingDeque<>(3)
            // AbortPolicy();方式是抛出java.util.concurrent.RejectedExecutionException异常
            // 阻止系统正常运行。
            // 8以内不会触发拒绝策略，超过8则会可能会触发
            for (int i = 0; i < 8; i++) {
                // 开启
                threadPool.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "\t 办理业务");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭
            threadPool.shutdown();
        }

    }

}
```

运行结果：

``` 
pool-1-thread-1	 办理业务
pool-1-thread-2	 办理业务
pool-1-thread-2	 办理业务
pool-1-thread-2	 办理业务
pool-1-thread-2	 办理业务
pool-1-thread-3	 办理业务
pool-1-thread-5	 办理业务
pool-1-thread-4	 办理业务
```

### 4. 如何合理配置线程池？从哪些方面考虑。

1. **CPU密集型**

   > 一般公式：线程池线程数 = CPU核数+1    

   该任务需要大量运算，而没有阻塞，CPU一直全速运行；

   CPU密集任务只有在真正多核CPU上才可能得到加速（通过多线程）；

   在单核CPU，无论开多少模拟的多线程，该任务都不可能得到加速，因为CPU总的运算能力有限，属于物理限制；

   CPU密集型任务配置尽可能少的线程数量；

2. **IO密集型**

   - 由于IO密集型任务线程并不是一直在执行任务，则应配置经可能多的线程，如CPU核数 * 2
   - IO密集型，即该任务需要大量的IO，即大量的阻塞。

   在单线程上运行IO密集型的任务会导致浪费大量的 CPU运算能力浪费在等待。

   所以在IO密集型任务中使用多线程可以大大的加速程序运行，即使在单核CPU上，这种加速主要就是利用了被浪费掉的阻塞时间。

   IO密集型时，大部分线程都阻塞，故需要多配置线程数：

   > 参考公式：==CPU核数/（1-阻塞系数） 阻塞系数在0.8~0.9之间  
   >
   > 八核CPU：8/（1-0，9）=80

   

   

   

