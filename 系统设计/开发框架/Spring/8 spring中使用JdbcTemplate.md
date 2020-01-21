## 1 JdbcTemplate概述
是 spring 框架中提供的一个对象，是对原始 Jdbc API 对象的简单封装。
spring 框架为我们提供了很多
的操作模板类。

* 操作关系型数据的：

JdbcTemplate
HibernateTemplate

* 操作 nosql 数据库的：

RedisTemplate

* 操作消息队列的：

JmsTemplate

位于spring-jdbc-5.0.2. RELEASE.jar，同时需要导入用于事务管理的包：spring-tx-5.0.2. RELEASE.jar。

## 2 JdbcTemplate对象的创建

除了默认构造函数之外，都需要提供一个数据源。通过属性的set方法，我们可以在配置文件中配置这些对象，进行依赖注入。
可以参考源码：

``` JAVA
	/**
	 * Construct a new JdbcTemplate for bean usage.
	 * <p>Note: The DataSource has to be set before using the instance.
	 * @see #setDataSource
	 */
	public JdbcTemplate() {
	}

	/**
	 * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
	 * <p>Note: This will not trigger initialization of the exception translator.
	 * @param dataSource the JDBC DataSource to obtain connections from
	 */
	public JdbcTemplate(DataSource dataSource) {
		setDataSource(dataSource);
		afterPropertiesSet();
	}

	/**
	 * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
	 * <p>Note: Depending on the "lazyInit" flag, initialization of the exception translator
	 * will be triggered.
	 * @param dataSource the JDBC DataSource to obtain connections from
	 * @param lazyInit whether to lazily initialize the SQLExceptionTranslator
	 */
	public JdbcTemplate(DataSource dataSource, boolean lazyInit) {
		setDataSource(dataSource);
		setLazyInit(lazyInit);
		afterPropertiesSet();
	}
```

## 3 配置数据源

### 3.1 配置bean.xml（spring内置数据源DriverManagerDataSource）

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!--配置账户的持久层-->
    <bean id="accountDao" class="com.panda00hi.dao.impl.AccountDaoImpl">
        <property name="dataSource" ref="dataSource"></property>
    </bean>

    <!--配置jdbcTemplate-->
    <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
        <!--为jdbcTemplate设置数据源-->
        <property name="dataSource" ref="dataSource"></property>
    </bean>

    <!--配置数据源（spring内置的）-->
    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"></property>
        <property name="url" value="jdbc:mysql://xx.xx.xx.xx:3306/dbName"></property>
        <property name="username" value="root"></property>
        <property name="password" value="123456"></property>
    </bean>

</beans>
```

### 3.2 其他数据源

除了内置的DriverManagerDataSource数据源，也可配置其他的。

1. 配置C3P0数据源

第一步，导入c3p0的依赖
第二步，到spring的配置文件中配置相关的属性

``` xml
<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
<property name="driverClass" value="com.mysql.jdbc.Driver"></property>
<property name="jdbcUrl" value="jdbc:mysql://xx.xx.xx.xx:3306/dbName"></property>
<property name="user" value="root"></property>
<property name="password" value="1234"></property>
</bean>
```

2. 同理，配置DBCP数据源

导入依赖->配置xml

``` xml
<!-- 配置数据源 -->
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
<property name="driverClassName" value="com.mysql.jdbc.Driver"></property>
<property name="url" value="jdbc:mysql://xx.xx.xx.xx:3306/dbName"></property>
<property name="user" value="root"></property>
<property name="password" value="1234"></property>
</bean>
```

## 4 使用JdbcTemplate实现增删改查

### 4.1 数据库表数据

* **创建数据库，并使用**

``` 
create database dbTest;
use dbTest;
```

* **创建表，包含id name money三个属性**

``` 
create table account(
id int primary key auto_increment,
name varchar(40),
money float
)character set utf8 collate utf8_general_ci;
```

### 4.2 具体使用

``` JAVA
package com.panda00hi.jdbcTemplate;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * JdbcTemplate的最基本用法
 *
 * @author panda00hi
 * 2020/1/15
 */
public class JdbcTemplateDemo02 {
    public static void main(String[] args) {
        // 1、获取容器
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
        // 2、获取容器对象
        JdbcTemplate jt = (JdbcTemplate) ac.getBean("jdbcTemplate");
        // 获取容器对象时，如果不进行强转也可以手动指定类型
        // JdbcTemplate jt = ac.getBean("jdbcTemplate", JdbcTemplate.class);
        // 3、执行操作
        jt.execute("insert into account(name,money) values('eee',3000)");
        // 保存
        // jt.update("insert into account(name,money) values (?,?)", "fff", 4567);
        // 更新
        // jt.update("update account set name =?, money=? where id = ?", "test", 1234, 6);
        // 删除
        // jt.update("delete from account where id = ?", 7);
        // 查询所有
        List<Account> accounts = jt.query("select * from account where money > ?", new BeanPropertyRowMapper<>(Account.class), 1000f);
        for (Account account : accounts) {
            System.out.println(account);
        }

        // 查询一个
        // List<Account> account = jt.query("select * from account where id = ?", new BeanPropertyRowMapper<>(Account.class), 1);
        // System.out.println("id为1的账户记录：" + account.get(0));

        // 查询一个返回一行一列
        // Long count = jt.queryForObject("select count(*) from account where money > ?", long.class, 1000f);
        // System.out.println("大于1000的账户记录条数：" + count);

        
        /*// 准备数据源，spring内置数据源
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://xx.xx.xx.xx:3306/dbName");
        ds.setUsername("root");
        ds.setPassword("123456");

        // 1、创建jdbcTemplate对象
        JdbcTemplate jt = new JdbcTemplate();
        // 给jt设置数据源
        jt.setDataSource(ds);
        // 2、执行操作
        jt.execute("insert into account(name,money) values('ccc',1000)");
        */

    }

}

```

### 4.3 在 dao 中使用 JdbcTemplate

实体类

``` JAVA
package com.panda00hi.domain;

import java.io.Serializable;

/**
 * 账户的实体类
 * @author panda00hi
 * 2020/1/15
 */
public class Account implements Serializable {
    private Integer id;
    private String name;
    private Float money;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getMoney() {
        return money;
    }

    public void setMoney(Float money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", money=" + money +
                '}';
    }
}

```

#### 方式1：在 dao 中定义 JdbcTemplate

该方式的缺点（可以使用方式2解决）： dao 有很多时，每个 dao 都有一些重复性的代码。下面就是重复代码：

``` java
private JdbcTemplate jdbcTemplate;
public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
this.jdbcTemplate = jdbcTemplate;
}
```

``` JAVA
package com.panda00hi.dao.impl;

import com.panda00hi.dao.IAccountDao;
import com.panda00hi.domain.Account;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author panda00hi
 * 2020/1/15
 */
public class AccountDaoImpl2 implements IAccountDao {

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Account findAccountById(Integer accountId) {
        List<Account> accounts = jdbcTemplate.query("select * from account where id = ?", new BeanPropertyRowMapper<>(Account.class), accountId);
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    @Override
    public Account findAccountByName(String accountName) {
        List<Account> accounts = jdbcTemplate.query("select * from account where name = ?", new BeanPropertyRowMapper<>(Account.class), accountName);
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
        jdbcTemplate.update("update account set name = ?,money=? where id=?", account.getName(), account.getMoney(), account.getId());
    }

}

```

#### 方式2：让 dao 继承 JdbcDaoSupport
JdbcDaoSupport 是 spring 框架为我们提供的一个类，该类中定义了一个 JdbcTemplate 对象，我们可以
直接获取使用，但是要想创建该对象，需要为其提供一个数据源：具体源码如下

``` JAVA
public abstract class JdbcDaoSupport extends DaoSupport {

    // 定义对象
	@Nullable
	private JdbcTemplate jdbcTemplate;

	/**
     * set 方法注入数据源，判断是否注入了，注入了就创建 JdbcTemplate
	 * Set the JDBC DataSource to be used by this DAO.
	 */
	public final void setDataSource(DataSource dataSource) {
		if (this.jdbcTemplate == null || dataSource != this.jdbcTemplate.getDataSource()) {
            // 如果提供了数据源就创建 JdbcTemplate
			this.jdbcTemplate = createJdbcTemplate(dataSource);
			initTemplateConfig();
		}
	}

	/**
     * 使用数据源创建 JdcbTemplate
	 * Create a JdbcTemplate for the given DataSource.
	 * Only invoked if populating the DAO with a DataSource reference!
	 * <p>Can be overridden in subclasses to provide a JdbcTemplate instance
	 * with different configuration, or a custom JdbcTemplate subclass.
	 * @param dataSource the JDBC DataSource to create a JdbcTemplate for
	 * @return the new JdbcTemplate instance
	 * @see #setDataSource
	 */
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	/**
	 * Return the JDBC DataSource used by this DAO.
	 */
	@Nullable
	public final DataSource getDataSource() {
		return (this.jdbcTemplate != null ? this.jdbcTemplate.getDataSource() : null);
	}

	/**
     * 也可以通过注入 JdbcTemplate 对象
	 * Set the JdbcTemplate for this DAO explicitly,
	 * as an alternative to specifying a DataSource.
	 */
	public final void setJdbcTemplate(@Nullable JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		initTemplateConfig();
	}

	/**
     * 使用 getJdbcTmeplate 方法获取操作模板对象
	 * Return the JdbcTemplate for this DAO,
	 * pre-initialized with the DataSource or set explicitly.
	 */
	@Nullable
	public final JdbcTemplate getJdbcTemplate() {
	  return this.jdbcTemplate;
	}

```
持久层实现类
``` JAVA
package com.panda00hi.dao.impl;

import com.panda00hi.dao.IAccountDao;
import com.panda00hi.domain.Account;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 通过继承JdbcDaoSupport该方法只需给父类注入一个数据源即可
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
#### 两种dao实现的区别
- 第一种在 Dao 类中定义 JdbcTemplate 的方式，适用于所有配置方式（xml 和注解都可以）。
- 第二种让 Dao 继承 JdbcDaoSupport 的方式，只能用于基于 XML 的方式，注解用不了。