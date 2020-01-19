spring ioc容器将bean对象创建好并传递给使用者的过程叫做bean的装配。

# bean的三种实例化方式

spring ioc容器会创建我们配置好的bean的对象，创建对象的方式有三种，分别是默认方式、实例工厂、静态工厂。一般使用默认的即可。

* 默认方式（调用无参构造器）：我们之前写的例子就是使用的默认方式，这种方式spring ioc容器会调用bean的无参构造方法来创建对象，所以此时务必要保证这些bean有无参构造方法。
* 静态工厂：这里需要自定一个工厂类，里面的方法是static修饰的

``` java
/**
 * 静态工厂，方法都是static修饰
 */
public class MyBeanFactory {

    public static StudentService createStudentService() {
        return new StudentServiceImpl();
    }
}

```

将applicationContext配置文件中修改如下，这里需要告诉spring要使用的工厂类和方法：

``` 
<!--静态工厂-->
    <bean id="studentService" class="com.pandahi.factory.MyBeanStaticFactory" factory-method="createStudentService"/>
```

- 实例工厂
自己定义一个工厂累，在该类中方法都是非静态的。
调用实例工厂方法创建Bean实例工厂方法与静态工厂方法只有一点不同：调用静态工厂方法只需要使用工厂类即可；调用实例工厂需要工厂实例。所以配置时，静态工厂方法使用class指定静态工厂类，**实例工厂方法使用factory-bean指定工厂实例**。
`````` JAVA
/**
 * 实例工厂，方法是非静态的
 */
public class MyBeanFactory {

    public StudentService createStudentService() {
        return new StudentServiceImpl();
    }
}
```
将applicationContext配置文件中修改如下，这里需要告诉spring要使用的工厂类和方法：
```
<!--实例工厂-->
    <!--1、把自己写的类注册到spring-->
    <!--2、实例工厂，需要factory-bean指定工厂实例，指定当前的bean是由工厂的createStudentService方法创建的-->
    <bean id="myFactory" class="com.pandahi.factory.MyBeanFactory"/>
    <bean id="studentService" factory-bean="myFactory" factory-method="createStudentService"/>
```
参考：https://blog.csdn.net/chainiao_zhang/article/details/77334479
