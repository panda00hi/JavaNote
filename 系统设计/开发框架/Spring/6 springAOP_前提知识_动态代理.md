动态代理的特点：

> 字节码随用随创建，随用随加载。

它与静态代理的区别也在于此。因为静态代理是字节码一上来就创建好，并完成加载。
装饰者模式就是静态代理的一种体现。

动态代理常用的有两种方式：
**基于接口的动态代理:**
提供者：JDK 官方的 Proxy 类。
要求：被代理类最少实现一个接口。

**基于子类的动态代理**
提供者：第三方的 CGLib，如果报 asmxxxx 异常，需要导入 asm.jar。
要求：被代理类不能用 final 修饰的类（最终类）。

案例：生产厂家进行生产和售后

## 1、基于接口的动态代理

IProducer.java接口

``` JAVA
package com.panda00hi.proxy;

/**
 * 对生产厂家要求的生产标准
 *
 * @Author panda00hi
 * 2020/1/14
 */
public interface IProducer {
    /**
     * 销售
     *
     * @param money
     */
    void saleProduct(float money);

    /**
     * 售后
     *
     * @param money
     */
    void afterService(float money);
}
```

实现类

``` JAVA
package com.panda00hi.proxy;

/**
 * 一个生产者
 *
 * @Author panda00hi
 * 2020/1/14
 */
public class Producer implements IProducer {
    /**
     * 销售
     *
     * @param money
     */
    @Override
    public void saleProduct(float money) {
        System.out.println("销售产品，并拿到钱：" + money);
    }

    /**
     * 售后
     *
     * @param money
     */
    @Override
    public void afterService(float money) {
        System.out.println("提供售后服务，并拿到钱：" + money);
    }

}

```

模拟一个消费者

``` JAVA
package com.panda00hi.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 模拟一个消费者(基于接口的动态代理)
 *
 * @Author panda00hi
 * 2020/1/14
 */
public class Client {
    public static void main(String[] args) {
        final Producer producer = new Producer();

        /**
         * 动态代理：
         * 特点：字节码随用随创建，随用随加载
         * 作用：不用修改源码的情况下对方法增强
         * 分类：
         * 基于接口的动态代理、基于子类的动态代理
         *
         * 基于接口的动态代理：(限制性-必须要有实现接口，才能代理)
         * 涉及的类：Proxy
         * 提供者：JDK官方
         *
         * 如何创建代理对象？
         * 使用Proxy类中的newProxyInstance方法
         * 创建代理对象的要求？
         * 该代理类最少实现一个接口，如果没有则不能使用newProxyInstance方法的参数
         *
         * java.lang.reflect.Proxy @NotNull
         * public static Object newProxyInstance(ClassLoader loader,
         *                                       @NotNull Class<?>[] interfaces,
         *                                       @NotNull reflect.InvocationHandler h)
         * throws IllegalArgumentException
         *
         * newProxyInstance的参数：
         * ClassLoader loader：类加载器，用于加载代理对象的字节码，和被代理对象使用相同的类加载器，固定写法。
         * Class[]：字节码数组，用于让代理对象和被代理对象有相同方法，固定写法。
         * InvocationHandler：用于提供增强代码，是让我们写如何代理。一般都是写一个该接口的实现类，通常情况下是匿名内部类，但不是必须的。此接口的实现累都是谁用谁写
         *
         *
         */
        IProducer proxyProducer = (IProducer) Proxy.newProxyInstance(producer.getClass().getClassLoader(),
                producer.getClass().getInterfaces(),
                new InvocationHandler() {
                    /**
                     * 作用：执行被代理对象的任何接口方法都会经过该方法
                     * 方法参数含义
                     *
                     * @param proxy  代理对象的引用
                     * @param method 当前执行的方法
                     * @param args   当前执行方法所需的参数
                     * @return 和被代理对象方法有相同的返回值
                     * @throws Throwable
                     */
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 提供增强的代码
                        Object returnValue = null;

                        // 1、获取方法执行的参数。（由被代理对象的方法于只有一个参数）
                        Float money = (Float) args[0];
                        // 2、判断当前方法是不是销售
                        if ("saleProduct".equals(method.getName())) {
                            returnValue = method.invoke(producer, money * 0.8f);
                        }
                        return returnValue;
                    }
                }
        );
        proxyProducer.saleProduct(10000f);
    }
}

```

运行结果：
`
销售产品，并拿到钱：8000.0
`

## 2、基于子类的动态代理

导入依赖

``` xml
	<dependency>
		<groupId>cglib<groupId>
		<artifactId>cglib<artifactId>
		<version>版本号<version>
	<dependency>
```

生产者

``` JAVA
package com.panda00hi.cglib;

import com.panda00hi.proxy.IProducer;

/**
 * 一个生产者
 *
 * @Author panda00hi
 * 2020/1/14
 */
public class Producer{
    /**
     * 销售
     *
     * @param money
     */
    public void saleProduct(float money) {
        System.out.println("销售产品，并拿到钱：" + money);
    }

    /**
     * 售后
     *
     * @param money
     */
    public void afterService(float money) {
        System.out.println("提供售后服务，并拿到钱：" + money);
    }

}

```

模拟消费者

``` JAVA
package com.panda00hi.cglib;

import com.panda00hi.proxy.IProducer;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 模拟一个消费者（基于子类）
 *
 * @Author panda00hi
 * 2020/1/14
 */
public class Client {
    public static void main(String[] args) {
        final Producer producer = new Producer();

        /**
         * 动态代理：
         * 特点：字节码随用随创建，随用随加载
         * 作用：不用修改源码的情况下对方法增强
         * 分类：
         * 基于接口的动态代理、基于子类的动态代理
         *
         * 基于接口的动态代理：(限制性-必须要有实现接口，才能代理)
         * 涉及的类：Proxy
         * 提供者：JDK官方
         *
         * 基于子类的动态代理：
         * 涉及的类：Enhancer
         * 提供者：第三方cglib库
         *
         * 如何创建代理对象？
         * 使用Enhancer类中的create方法
         * 创建代理对象的要求？
         * 被代理类不能是最终类
         *
         * public static Object create(Class type,
         *                             net.sf.cglib.proxy.Callback callback)
         *
         * create方法的参数：
         * class：字节码，用于指定被代理对象的字节码。
         * Callback：用于提供增强的代码，让我们写如何代理。一般都是写一个该接口的实现类，
         * 通常情况下是匿名内部类，但不是必须的。此接口的实现累都是谁用谁写，MethodInterceptor
         *
         */

        Producer cglibProducer = (Producer) Enhancer.create(producer.getClass(), new MethodInterceptor() {

            /**
             * 执行被代理对象的任何方法都会经过该方法
             *
             * @param o
             * @param method
             * @param objects     以上三个参数和基于接口的动态代理中invoke方法的参数是一样的
             * @param methodProxy 当前执行方法的代理对象
             * @return
             * @throws Throwable
             */
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                // 提供增强的代码
                Object returnValue = null;

                // 1、获取方法执行的参数
                Float money = (Float) objects[0];
                // 2、判断当前方法是不是销售
                if ("saleProduct".equals(method.getName())) {
                    returnValue = method.invoke(producer, money * 0.8f);
                }
                return returnValue;
            }
        });
        cglibProducer.saleProduct(10000f);

    }
}

```

运行结果
`销售产品，并拿到钱：8000.0` 

