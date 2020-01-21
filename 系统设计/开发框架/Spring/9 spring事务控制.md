## 1 概念

* 第一：JavaEE 体系进行分层开发，事务处理位于业务层，Spring 提供了分层设计业务层的事务处理解决方案。
* 第二：spring 框架为我们提供了一组事务控制的接口。这组接口是在spring-tx-5.0.2. RELEASE.jar 中。
* 第三：spring 的事务控制都是基于 AOP 的，它既可以使用编程的方式实现，也可以使用配置的方式实现。

## 2 spring 中事务控制的API

### 2.1 PlatformTransactionManager 事务管理器
此接口是 spring 的事务管理器，它里面提供了我们常用的操作事务的方法

– 获取事务状态信息：
TransactionStatue getTransaction(TransactionDefinition define)

– 提交事务：
void commit(TransactionStatus status)

– 回滚事务：
void rollback(TransactionStatus status)

具体对象（实现类）：

* org.springframework.jdbc.datasource. DataSourceTransactionManager 使用Spring JDBC或iBatis进行持久化数据时使用
* org.springframework.orm... hibernate.. 使用hibernate版本使用

### 2.2 TransactionDefinition 事务的定义信息对象

* 获取事务对象名称：String getName()
* 获取事务隔离级：int getIsolationLevel()
* 获取事务传播行为：int getPropagationBehavior()

①REQUIRED：如果当前没有事务，就新建一个事务，如果已经存在一个事务中，加入到这个事务中。一般的选 择（默认值）
②SUPPORTS：支持当前事务，如果当前没有事务，就以非事务方式执行（没有事务）
③MANDATORY：使用当前的事务，如果当前没有事务，就抛出异常
④REQUERS_NEW：新建事务，如果当前在事务中，把当前事务挂起。
⑤NOT_SUPPORTED：以非事务方式执行操作，如果当前存在事务，就把当前事务挂起
⑥NEVER：以非事务方式运行，如果当前存在事务，抛出异常
⑦NESTED：如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则执行REQUIRED类似的操作。

获取事务超时时间：int getTimeOut() 默认值是-1，没有超时限制。如果有，以秒为单位进行设置。

​ //读写型事务，增删改，也会开启事务

获取事务是否只读：boolean isReadOnly() //只读型事务，执行查询时也会开启事务

* TransactionStatus 此接口提供的是事务具体的运行状态

​ 刷新事务：void flush()

​ 获取是否存在存储点：boolean hasSavepoint()

​ 获取事务是否完成：boolean isComplecated()

​ 获取事务是否为新事物：boolean isNewTransaction()

​ 获取事务是否回滚：boolean isRollBackOnly()

​ 设置事务回滚：void setRollBackOnly()

### 2.3 事务隔离级别

​- 未提交读取（Read Uncommitted） Spring标识：ISOLATION_READ_UNCOMMITTED

​- 已提交读取（Read Committed）Spring标识：ISOLATION_READ_COMMITTED。

​- 可重复读取（Repeatable Read）Spring标识：ISOLATION_REPEATABLE_READ。

​- 序列化（Serializable）Spring标识：ISOLATION_SERIALIZABLE。

| 读数据一致性 | 脏读                             | 不可读 | 幻读 |     |
|------------|----------------------------------|-------|-----|-----|
| 已提交读取  | 最低阶别，只能保证不读取物理上损坏的数据 | 1     | 1   | 1   |
| conte      | 语句级 Oracle默认级                | x     | 1   | 1   |
| 可重复读取  | 事务级 Mysql默认级别               | x     | x   | 1   |
| 序列化      | 最高级别，事务级                    | x     | x   | x   |

## 3 基于xml的声明式事务控制

### 3.1 spring pom文件导入依赖，配置文件中增加tx的命名空间

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>springdemo</artifactId>
        <groupId>com.panda00hi</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>tx02_xml</artifactId>
    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.48</version>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.8.14</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>

    </dependencies>

</project>
```

``` xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">
</beans>
```

### 3.2 业务层接口和实现类

``` JAVA
package com.panda00hi.service;

import com.panda00hi.domain.Account;

/**
 * 账户的业务层接口
 * @author panda00hi
 * 2020/1/16
 */
public interface IAccountService {

    /**
     * 查询一个
     * @return
     */
    Account findAccountById(Integer accountId);

    /**
     * 转账
     * @param sourceName
     * @param targetName
     * @param money
     */
    void transfer(String sourceName, String targetName, Float money);

}

```

``` JAVA
package com.panda00hi.service.impl;

import com.panda00hi.dao.IAccountDao;
import com.panda00hi.dao.impl.AccountDaoImpl;
import com.panda00hi.domain.Account;
import com.panda00hi.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账户的业务层实现类
 */
public class AccountServiceImpl implements IAccountService {

    private IAccountDao accountDao;

    public void setAccountDao(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Account findAccountById(Integer accountId) {
        return accountDao.findAccountById(accountId);

    }

    @Override
    public void transfer(String sourceName, String targetName, Float money) {
        System.out.println("transfer开始执行……");
        // 2.1 根据名称查询转出账户
        Account source = accountDao.findAccountByName(sourceName);
        // 2.2 根据名称查询转入账户
        Account target = accountDao.findAccountByName(targetName);
        // 2.3 转出账户减钱
        source.setMoney(source.getMoney() - money);
        // 2.4 转入账户加钱
        target.setMoney(target.getMoney() + money);
        // 2.5 更新转出账户
        accountDao.updateAccount(source);
        // 测试异常情况下事务执行情况
        // int i = 1 / 0;
        // 2.6 更新转入账户
        accountDao.updateAccount(target);

    }

}

```

### 3.3 dao层接口和实现类

``` java
package com.panda00hi.dao;

import com.panda00hi.domain.Account;

/**
 * 账户的持久层接口
 * @author panda00hi
 * 2020/1/15
 */
public interface IAccountDao {
    Account findAccountById(Integer accountId);

    Account findAccountByName(String accountName);

    void updateAccount(Account account);
}

```

``` JAVA
package com.panda00hi.dao.impl;

import com.panda00hi.dao.IAccountDao;
import com.panda00hi.domain.Account;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author panda00hi
 * 2020/1/15
 */
public class AccountDaoImpl extends JdbcDaoSupport implements IAccountDao {

    @Override
    public Account findAccountById(Integer accountId) {
        List<Account> accounts = super.getJdbcTemplate().query("select * from account where id = ?", new BeanPropertyRowMapper<>(Account.class), accountId);
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    @Override
    public Account findAccountByName(String accountName) {
        List<Account> accounts = super.getJdbcTemplate().query("select * from account where name = ?", new BeanPropertyRowMapper<>(Account.class), accountName);
        if (CollectionUtils.isEmpty(accounts)) {
            return null;
        }
        if (accounts.size() > 1) {
            throw new RuntimeException("结果集不唯一，数据有问题");
        }
        return accounts.get(0);
    }

    @Override
    public void updateAccount(Account account) {
        super.getJdbcTemplate().update("update account set name = ?,money=? where id=?", account.getName(), account.getMoney(), account.getId());
    }

}

```

### 3.4 在配置文件中配置业务层和持久层

``` xml
    <!--配置业务层-->
    <bean id="accountService" class="com.panda00hi.service.impl.AccountServiceImpl">
        <property name="accountDao" ref="accountDao"></property>
    </bean>

    <!--配置账户的持久层-->
    <bean id="accountDao" class="com.panda00hi.dao.impl.AccountDaoImpl">
        <property name="dataSource" ref="dataSource"></property>
    </bean>

    <!--配置数据源-->
    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"></property>
        <property name="url" value="jdbc:mysql://172.17.0.1:33060/eesy"></property>
        <property name="username" value="root"></property>
        <property name="password" value="123456"></property>
    </bean>
```

### 3.4 配置事务管理器

> 1、配置事务管理器

    2、配置事务的通知
            此时我们需要导入事务的约束   tx名称空间和约束，同时也需要aop的（可在官方文档Data access查找）
            使用tx:advice标签配置事务通知
                属性：id：给事务通知起一个唯一标识
                transaction-manager：给事务通知提供一个事务管理器引用
    3、配置aop中的通用切入点表达式
    4、建立事务通知和切入点表达式的对应关系
    5、配置事务的属性

``` xml

<!--配置事务管理器-->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"></property>
    </bean>

    <!--配置事物的通知-->
    <tx:advice id="txAdvice" transaction-manager="transactionManager">
        <!--配置事务的属性
        isolation：用于指定事务的隔离级别，默认值是default，表示使用数据库的默认隔离级别
        no-rollback-for：
        propagation：用于指定事务的传播行为，默认值是REQUIRED，表示一定会有事务，增伤爱的选择；SUPPORS，可以是查询方法。
        read-only：用于指定事务是否只读，只有查询方法才能设置为true，表示读写。
        rollback-for：用于指定一个异常，当产生该异常时，事务回滚，产生其他异常时，事务不回滚。没有默认值。表示任何异常都回滚。
        timeout：用于指定事务的超时时间，默认值是-1，表示不超时。如果指定了数值，以秒为单位
        -->
        <tx:attributes>
            <tx:method name="*" propagation="REQUIRED" read-only="false"/>
            <tx:method name="find*" propagation="SUPPORTS" read-only="true"/>
        </tx:attributes>
    </tx:advice>

    <!--配置aop-->
    <aop:config>
        <!--配置通用切入点表达式-->
        <aop:pointcut id="pt1" expression="execution(* com.panda00hi.service.impl.*.*(..))"/>
        <!--建立切入点表达式和事务通知的对应关系-->
        <aop:advisor advice-ref="txAdvice" pointcut-ref="pt1"/>
    </aop:config>
```

## 4 基于注解的事务控制

### 4.1 spring配置文件
spring配置文件引入aop、tx、context的命名空间，并配置要扫描的包以及数据源等

``` xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">

    <!--使注解可用：配置spring创建容器时要扫描的包-->
    <context:component-scan base-package="com.panda00hi"/>
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">

    <!--使注解可用：配置spring创建容器时要扫描的包-->
    <context:component-scan base-package="com.panda00hi"/>

    <!--配置JdbcTemplate-->
    <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
        <property name="dataSource" ref="dataSource"></property>
    </bean>
    <!--配置数据源-->
    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"></property>
        <property name="url" value="jdbc:mysql://172.17.0.1:33060/eesy"></property>
        <property name="username" value="root"></property>
        <property name="password" value="123456"></property>
    </bean>

    <!--配置事务管理器-->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"></property>
    </bean>
    
    <!--开启spring注解对事务的支持-->
    <tx:annotation-driven transaction-manager="transactionManager"/>
</beans>

```

### 4.2 创建业务层和dao层的接口和实现类并分别使用@Service("accountService")和@Repository("accountDao")注解，让spring管理对象

### 4.3 在业务层使用@transactional注解

该注解的属性和 xml 中的属性含义一致。该注解可以出现在接口上，类上和方法上。
出现接口上，表示该接口的所有实现类都有事务支持。
出现在类上，表示类中所有方法有事务支持
出现在方法上，表示方法有事务支持。
以上三个位置的优先级：方法>类>接口

``` JAVA
package com.panda00hi.service.impl;

import com.panda00hi.dao.IAccountDao;
import com.panda00hi.dao.impl.AccountDaoImpl;
import com.panda00hi.domain.Account;
import com.panda00hi.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 账户的业务层实现类
 */
@Service("accountService")
@Transactional
public class AccountServiceImpl implements IAccountService {

    @Autowired
    private IAccountDao accountDao;

    @Override
    public Account findAccountById(Integer accountId) {
        return accountDao.findAccountById(accountId);
    }

    @Override
    public void transfer(String sourceName, String targetName, Float money) {
        System.out.println("transfer开始执行……");
        // 2.1 根据名称查询转出账户
        Account source = accountDao.findAccountByName(sourceName);
        // 2.2 根据名称查询转入账户
        Account target = accountDao.findAccountByName(targetName);
        // 2.3 转出账户减钱
        source.setMoney(source.getMoney() - money);
        // 2.4 转入账户加钱
        target.setMoney(target.getMoney() + money);
        // 2.5 更新转出账户
        accountDao.updateAccount(source);
        // 测试异常情况下事务执行情况
        // int i = 1 / 0;
        // 2.6 更新转入账户
        accountDao.updateAccount(target);

    }

}

```

## 5 不使用xml，纯注解

创建单独的config包，建一些配置的类，使用configuration注解等从这些类引入配置，而不需要使用bean.xml。

``` JAVA
@Configuration
@EnableTransactionManagement
public class SpringTxConfiguration {
～～～～
}
```

