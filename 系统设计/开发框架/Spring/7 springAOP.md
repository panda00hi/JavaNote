## 1 什么是AOP?
AOP（Aspect Orient Programming），面向切面编程，是面向对象编程 OOP 的一种补充。在运行时，动态地将代码切入到类的指定方法、指定位置上。例如转账功能，在转账代码的前后需要一些非业务方面的处理，权限控制，记录日志，事务回滚资源释放等，这些代码就可以使用AOP将其切入到转账代码的前后，这样就可以很好地分离业务代码和非业务代码。 AOP的优点就是降低代码之间的耦合，提高代码的复用性。

简单的说它就是把我们程序重复的代码抽取出来，在需要执行的时候，使用动态代理的技术，在不修改源码的基础上，对我们的已有方法进行增强。

## 2 相关术语

* Joinpoint(连接点):

所谓连接点是指那些被拦截到的点。在 spring 中, 这些点指的是方法, 因为 spring 只支持方法类型的
连接点。

* Pointcut(切入点):

所谓切入点是指我们要对哪些 Joinpoint 进行拦截的定义。

* Advice(通知/增强):

所谓通知是指拦截到 Joinpoint 之后所要做的事情就是通知。
通知的类型：前置通知, 后置通知, 异常通知, 最终通知, 环绕通知。

* Introduction(引介):

引介是一种特殊的通知在不修改类代码的前提下, Introduction 可以在运行期为类动态地添加一些方
法或 Field。

* Target(目标对象):

代理的目标对象。

* Weaving(织入):

是指把增强应用到目标对象来创建新的代理对象的过程。
spring 采用动态代理织入，而 AspectJ 采用编译期织入和类装载期织入。

* Proxy（代理）:

一个类被 AOP 织入增强后，就产生一个结果代理类。

* Aspect(切面):

是切入点和通知（引介）的结合。

## 3 基于xml配置的aop

### 3.1 第一步 导入aop的依赖包

``` xml
       <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.8.7</version>
        </dependency>
```

### 3.2 第二步 resources目录下创建bean.xml文件，并导入约束（从官方文档可得到）

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">
        
</beans>
```

#### 3.2.1 配置步骤

spring中基于xml的aop配置步骤

    1、把通知bean也交给spring来管理
    2、使用aop:config标签表明开始aop配置
    3、使用aop:aspect标签表明配置切面
    id属性，给切面提供一个唯一标识
    ref属性：是指定通知类bean的id
    4、在aop:aspect标签的内部使用对应标签来配置通知的类型
    例如：让printLog方法在切入点方法执行之前执行，所以是前置通知
    aop:before，表示前置通知
    method属性，用于指定Logger类中哪个方法是前置通知
    pointcut属性，用于指定切入点表达式，该表达式的含义指的是对业务层中哪些方法进行增强

    切入点表达式写法：
    关键字;execution
    标准写法：访问修饰符 返回值 全包名.类名.方法名(参数列表)
    如：public void com.panda00hi.service.impl.AccountServiceImpl.saveAccount()
        访问修饰符可以省略
        void com.panda00hi.service.impl.AccountServiceImpl.saveAccount()
        返回值可以使用通配符，表示任意返回值

        * com.panda00hi.service.impl.AccountServiceImpl.saveAccount()

        包名可以使用通配符，但是通配符与包的层级要一一对应，有几级就要写几个*

        * *.*.*.*.AccountServiceImpl.saveAccount()

        包名可以使用..表示当前包及其子包

        * *..AccountServiceImpl.saveAccount()

        类名和方法名都可以使用*来实现通配

        * *..*.*()

        参数列表，可以直接写数据类型，
        基本类型直接写名称       int
        引用类型写包名.类名的方式 java.lang.String
        可以使用通配符表示任意类型，但是必须有参数
        可以使用..表示有无参数均可

    全通配写法：

    - *..*.*(..)

    [注意]实际开发中，切入点表达式的通常写法：
    切到业务层实现类下的所有方法

    - com.panda00hi.service.impl.*.*(..)

