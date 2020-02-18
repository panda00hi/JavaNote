> 源码文件夹：luckymoney

## 1 springboot 配置文件application.yml

### 1.1 单个或多个对象的配置

单个配置用@Value注解，多个的话，用application.yml对象的方式，结合@ConfigurationProperties。

application.yml配置

``` 
# Dev开发环境配置
server:
  port: 8082
  servlet:
    context-path: /luckymoney

# limit视为一个对象进行配置
limit:
  minMoney: 0.01
  maxMoney: 9999
  description: 最少要发${limit.minMoney}元，最多发${limit.maxMoney}元
```

创建配置对象类

``` JAVA
package com.panda00hi.luckymoney;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author panda00hi
 * 2020/2/17
 */
@Component
@ConfigurationProperties(prefix = "limit")
public class LimitConfig {
    private BigDecimal minMoney;
    private BigDecimal maxMoney;
    private String description;

    // 省略get、set方法

```

Controller中载入配置中的值

``` JAVA
package com.panda00hi.luckymoney;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @author panda00hi
 * 2020/2/17
 */
@RestController
@RequestMapping("project")
// @Controller
public class HelloController {
    // // @Value注解，载入配置中的值
    // @Value("${minMoney}")
    // private BigDecimal minMoney;
    //
    // @Value("${description}")
    // private String description;

    @Autowired
    private LimitConfig limitConfig;

    @RequestMapping({"/test1", "/demo1"})
    public String say() {

        return "说明" + limitConfig.getDescription();
        // return "index";
    }

    @GetMapping("/say")
    public String say2(@RequestParam(value = "id", required = false,defaultValue = "默认0") String id1) {
        return "id:" + id1;
    }
}

```

### 1.2 多环境配置

application.yml中，指定加载不同的环境配置

``` 
spring:
  profiles:
    # active: product
    active: dev
```

## 2 Controller的使用

| 名称            | 作用                                                        |
|-----------------|-------------------------------------------------------------|
| @Controller     | 处理http请求                                                 |
| @RestController | Spring4之后的注解，原来返回json需要@seBody配合@Controller        |
| @RequestMapping | 配置url映射，post、get方式都支持（新版使用@GetMapping、@PostMapping） |

@RequestMapping或@GetMapping中的参数可以为多个，用数组表示，如@RequestMapping({"/hello1", "/hello2"})表示请求hello1, hello2都会接受到该Controller。

### 2.1 thymeleaf模版渲染

（目前主要采用前后端分离，所以此种情况已经不常用）

#### 2.1.1 引入jar包依赖

``` xml
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-thymeleaf</artifactId>
		</dependency>
```

#### 2.1.2 新建html模板页面

在resources目录下的，template文件夹下，新建index.html

``` html
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>index</title>
</head>

<body>
    <h1>hello，welcome ！！！</h1>
</body>

</html>
```

#### 2.1.3 使用模板

修改Controller中的返回值为指定页面

``` java
@RequestMapping("project")
@Controller
public class HelloController {
    // // @Value注解，载入配置中的值
    // @Value("${minMoney}")
    // private BigDecimal minMoney;
    //
    // @Value("${description}")
    // private String description;

    @Autowired
    private LimitConfig limitConfig;

    @RequestMapping("/test1")
    public String say() {

        // return "说明" + limitConfig.getDescription();
        // 跳转到index.html模板
        return "index";
    }
}
```

### 2.2 获取请求中的参数

#### 2.2.1 如果是从URL，用@PathVariable，否则用@RequestParam

``` java
@GetMapping("/say")
    public String say2(@RequestParam("id") Integer id1) {
        return "id:" + id1;
    }
```

浏览器请求：http://localhost:8082/luckymoney/project/say?id=100

**改为非必传，并设置没有传参时的默认赋值**
`注意：` defaultValue的值类型要与Integer id1的类型匹配，否则会报错格式转换异常。

``` java
@GetMapping("/say")
    public String say2(value = "id", required = false,defaultValue = "0", Integer id1) {
        return "id:" + id1;
    }
```

``` java
@GetMapping("/say/{id}")
    public String say2(@PathVariable("id") Integer id1) {
        return "id:" + id1;
    }
```

浏览器请求：http://localhost:8082/luckymoney/project/say/100

## 3 Spring-Data-Jpa操作数据库

### 3.1 JPA
JPA(Java Persistence API)定义了一系列对象持久化的标准，实现这一规范的产品如Hibernate、Toplink等。类似于接口，由使用者具体实现。

### 3.2 RESTful API设计

| 请求类型 | 请求路径         | 功能         |
|--------|-----------------|--------------|
| GET    | /luckymoneys    | 获取红包列表   |
| POST   | /luckymoneys    | 创建一个红包   |
| GET    | /luckymoneys/id | 通过id查询红包 |
| PUT    | /luckymoneys/id | 通过id更新红包 |

get请求：一般用于查询数据，获取一些非重要性的信息。
post请求：一般用于插入数据。
put请求：一般用于数据更新。
delete请求：一般用于数据删除

### 3.3 基于RESTful+jpa+mysql的示例

#### 3.3.1 增加jar包依赖

``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

#### 3.3.2 application.yml添加数据库配置

``` 
# 数据库连接
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/luckymoney?characterEncoding=utf-8
    username: root
    password: 123456
  jpa:
    hibernate:
    # 用create，表示建表
      ddl-auto: update
    # 是展示sql执行细节，方便开发测试
    show-sql: true

```

#### 3.3.3 创建实体类@Entity、@Id、@GeneratedValue

Luckymoney.java

``` JAVA
package com.panda00hi.luckymoney;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * @author panda00hi
 * 2020/2/18
 */
@Entity
public class Luckymoney {

    @Id
    @GeneratedValue
    private Integer id;

    private BigDecimal money;

    /**
     * 发送方
     */
    private String producer;

    /**
     * 接收方
     */
    private String consumer;

    // 省略无参构造、get、set方法
}

```

#### 3.3.4 创建持久层接口，继承jpa提供的接口，从而使用其封装的各种数据库操作相关的方法

``` JAVA
package com.panda00hi.luckymoney;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 持久层接口
 * 继承JpaRepository，两个参数，一个实体类，一个id类型
 * @author panda00hi
 * 2020/2/18
 */
public interface LuckymoneyRepository extends JpaRepository<Luckymoney, Integer> {
}
```

#### 3.3.5 创建Controller，实现各项操作

``` JAVA
package com.panda00hi.luckymoney;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author panda00hi
 * 2020/2/18
 */
@RestController
@RequestMapping("/demo01")
public class LuckymoneyController {

    @Autowired
    private LuckymoneyRepository repository;

    /**
     * 获取红包列表
     */
    @GetMapping("/luckymoneys")
    public List<Luckymoney> list() {
        List<Luckymoney> all = repository.findAll();
        return CollectionUtils.isEmpty(all) ? new ArrayList<>() : all;
    }

    /**
     * 创建红包
     */
    @PostMapping("/luckymoneys")
    public Luckymoney create(@RequestParam("money") BigDecimal mon, 
                             @RequestParam("producer") String pro) {
        Luckymoney luckymoney = new Luckymoney();
        luckymoney.setProducer(pro);
        luckymoney.setMoney(mon);
        return repository.save(luckymoney);

    }

    /**
     * 通过id查询红包
     */
    @GetMapping("/luckymoneys/{id}")
    public Luckymoney findById(@PathVariable("id") Integer id) {

        return repository.findById(id).orElse(null);
    }

    /**
     * 通过id更新红包
     */
    @PutMapping("/luckymoneys/{id}")
    public Luckymoney update(@PathVariable("id") Integer id,
                             @RequestParam("consumer") String consumer) {

        Optional<Luckymoney> optional = repository.findById(id);
        if (optional.isPresent()) {
            Luckymoney luckymoney = optional.get();
            luckymoney.setConsumer(consumer);
            return repository.save(luckymoney);
        }
        return null;
    }
}

```

#### 3.3.6 使用postman进行测试

> 扩展：

Spring Data Jpa中一共提供多种持久化接口

* Repository：

    1 提供了findBy + 属性方法 
    2 @Query 
    　　HQL： nativeQuery 默认false
    　　SQL: nativeQuery 默认true
        更新的时候，需要配合@Modifying使用

* CurdRepository:

    继承了Repository 主要提供了对数据的增删改查

* PagingAndSortRepository:

    继承了CrudRepository 提供了对数据的分页和排序，缺点是只能对所有的数据进行分页或者排序，不能做条件判断

* JpaRepository： 继承了PagingAndSortRepository

    开发中经常使用的接口，主要继承了PagingAndSortRepository，对返回值类型做了适配

* JpaSpecificationExecutor

    提供多条件查询

## 4 关于并发中的事务示例

数据库事务，是指作为单个逻辑工作单元执行的一系列操作，要么完全地执行，要么完全地不执行。
使用到spring提供的@Transactional注解

### 4.1 创建含有修改数据库操作的service

添加注解@Transactional

``` JAVA
package com.panda00hi.luckymoney;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author panda00hi
 * 2020/2/18
 */
@Service
public class LuckymoneyService {

    @Autowired
    private LuckymoneyRepository repository;

    /**
     * 事务 指数据库事务
     * 确保方法中涉及修改数据库的操作要么都成功，要么都失败。
     */
    @Transactional
    public void createTwo() {
        Luckymoney luckymoney1 = new Luckymoney();
        luckymoney1.setProducer("刘1");
        luckymoney1.setMoney(new BigDecimal("520.00"));
        repository.save(luckymoney1);

        Luckymoney luckymoney2 = new Luckymoney();
        luckymoney2.setProducer("刘1");
        luckymoney2.setMoney(new BigDecimal("1314.00"));
        repository.save(luckymoney2);

    }

}

```

### 4.2 Controller中调用该service的createTwo()方法。

``` JAVA
 @PostMapping("/luckymoneys/two")
    public void createTwo() {
        service.createTwo();
    }
```

### 4.3 postman测试，并查看数据库中数据结果

