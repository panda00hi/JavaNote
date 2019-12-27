[原文]](https://blog.csdn.net/SimpleAdamEva/article/details/86622076)

> 其实这里还有一个问题，就是有多处return时，最终返回的值可能不同。原因应该是与jvm运行时，主内存与拷贝内层的存值（或者说指向的堆栈地址不同。此处先大致留个印象，后期结合jvm再来补充。）

### finaly的用处

finally 一般是在try里捕获异常用的，为了确保某些操作一定可以执行。

``` java
public class Demo01 {

public static void main(String[] args) {
query();
}

public static void query(){
    int i=0;
        try {
        i ++;
            i = i/0;//这时会抛出异常
            System.out.println("某些操作...");
        } catch (Exception e) {
            i +=20;
        }finally{
            System.out.println(i);
            System.out.println("一些必须要执行的操作....");
        }
    }
}
```

执行结果：

``` JAVA
21
一些必须要执行的操作....
```

抛出异常以后不会再继续往下执行try包含的语句了，首先会进入catch中，然后finally语句一定会执行。

### 两种情况下finally语句不会执行

1. 某些情况下，try语句根本没执行到;
2. 在try语句中，有System.exit(0); 这样的语句，终止了java虚拟机JVM的运行。

### finally和return的执行顺序

``` JAVA
public class Demo02 {

    public static void main(String[] args) {
    int j=query();
    System.out.println(j);
    }
    public static int query(){
        int i=0;
        try {
            System.out.println("try\n");
            return i +=10;
        } catch (Exception e) {
            System.out.println("catch\n");
            i +=20;
        }finally {
            System.out.println("finally i:"+i+"\n");
            i +=10;
            System.out.println("finally\n");
            	
            }
        return 200;
    
    }
}
```

运行结果：

``` code
try
finally i:10
finally
10
```

可以看出来，只要try语句执行了以后，就算try语句中有return语句，fianlly也会执行，所以能看出来fianlly在return执行之后执行，代码中可以看出，return i +=10; 这个时候i已经是10了，打印语句打印的i的结果就算10，说明了return语句中i +=10是执行了的.

但是看如下代码

``` JAVA
public class Demo03{

    public static void main(String[] args) {
        int j=query();
        System.out.println(j);
    }
    public static int query(){
        int i=0;
        try {
            System.out.println("try\n");
            return i +=10;
        } catch (Exception e) {
            System.out.println("catch\n");
            i +=20;
        }finally {
            System.out.println("finally i:"+i+"\n");
            i +=10;
            System.out.println("finally\n");
            
            return i;
        }
    
        
    }
}
```

执行结果：

``` code
try
finally i:10
finally
20
```

可以看到最后返回结果成为了20.
说明fianlly语句是在return语句执行后，return语句返回之前执行的，原因就算在JVM虚拟机中.

### JVM的虚拟机栈

在JVM虚拟机中，有虚拟机栈，上面的代码中每一个方法都对应了一个栈帧，方法的执行对应栈帧的入栈，方法的执行
完毕对应着栈帧的出栈。

栈帧可以理解为一个方法的运行空间。主要是由两部分组成，一部分是局部变量表，方法中定义的局部变量以及方法的参数存放在局部变量表中。另一部分是操作数栈，用来存放操作数

Demo02的finally代码中，虽然执行了 i+=10; 但是没有return，所以局部变量表中没有变化，所以i还是10。
Demo03的代码块中，最后reutrn i 的执行，更新了局部变量中i的值，所以最后返回的结果 i 为20 了。

return 返回后，就代表这方法的结束，相应的方法的栈帧就出栈了，这个时候也意味着，return返回时最后执行的，所以
fianlly语句是在return语句执行后，返回之前执行的！

对于引用类型，操作是在原地址上进行的，所以就算引用类型没有return也会影响到最终返回的结果！！！！

## 总结

1.fianlly是在return语句执行后，return返回之前执行的，当然也就是说fianlly必执行(建立在try的基础上)
2.finally中修改的基本类型没有return是不影响返回值结果的，有了return才会影响
3.fianlly中修改list、map、set引用类型，就算没有return，也会影响返回值结果

