永远不要相信用户的输入，我们开发的系统凡是涉及到用户输入的地方，都要进行校验，这里的校验分为前台校验和后台校验，前台校验通常由javascript来完成，后台校验主要由java来负责，这里我们可以通过spring mvc+hibernate validator。

## spring mvc+hibernate validator
在java中有一个bean validation的数据验证规范，该规范的实现者有很多，其中hibernate validator使用的较多一些，这里的hibernate validator是hibernate框架下的一款用于数据校验的框架，以前我们统称的hibernate一般特指的是hibernate orm。
### 1.1 导入jar包依赖
这里我们来用下6.0.9的版本，该版本需要jdk8+。先来使用maven导入相关jar包：

```xml
<dependency>
   <groupId>org.hibernate</groupId>
   <artifactId>hibernate-validator</artifactId>
   <version>6.0.9.Final</version>
</dependency>
```
### 1.2 编写javabean

编写javabean，在需要校验的属性上面添加相应的注解.

```java
package com.panda00hi.bean;

import javax.validation.constraints.*;

/**
 * 用户
 */
public class User {

    @NotEmpty(message = "姓名不能为空")
    @Size(min = 4, max = 20,message = "姓名长度必须在{min}-{max}之间")
    private String name;

    @Min(value = 0, message = "年龄不能小于{value}")
    @Max(value = 120, message = "年龄不能大于{value}")
    private int age;

    @Pattern(regexp = "^1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}$", message = "手机号码不正确")
    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                '}';
    }
}

```
### 1.3 修改springmvc.xml配置文件

修改springmvc.xml配置文件，注册一个验证器，这里使用的是HibernateValidator实现的验证器：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">


    <!--注解驱动，添加验证器-->
    <mvc:annotation-driven validator="panda00hiValidation"/>

    <!--组件扫描器-->
    <context:component-scan base-package="com.panda00hi.*"/>

    <!--注册hibernate validation验证器-->
    <bean id="panda00hiValidation" class="org.springframework.validation.beanvalidation.LocalValidatorFactoryBean">
        <property name="providerClass" value="org.hibernate.validator.HibernateValidator"/>
    </bean>

    <!--请求仍然会跳转到内部jsp，需要新配置一个-->
    <!--外部视图解析器，放在内部解析器的之前-->
    <bean class="org.springframework.web.servlet.view.BeanNameViewResolver"/>

    <!--定义外部资源view对象，重定向-->
    <bean id="baidu" class="org.springframework.web.servlet.view.RedirectView">
        <!--注意这里的value值要把https://协议部分加上，否则访问的是localhost:8080/www.baidu.com-->
        <property name="url" value="https://www.baidu.com/"/>
    </bean>

    <!--内部视图解析器-->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/jsp/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
</beans>
```
### 1.4 创建Controller

创建controller，在方法的参数中写上User和BindingResult，在User前面添加@Validated注解，这里需要注意:  
1. 不能将@Validated 注解在String类型和基本类型的形参前；
2. BindingResult参数可以获取到所有验证异常的信息，当校验不通过的时候将提示信息放到ModelAndView中传递到jsp里面。

```java
package com.panda00hi.controller;

import com.panda00hi.bean.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    //@Validated 不能加在String或基本数据类型前面
    //BindingResult可以获得验证异常的信息

    @RequestMapping("/register.do")
    public ModelAndView register(@Validated User user, BindingResult br)  {

        ModelAndView mv = new ModelAndView();

        //获得验证不通过的异常
        List<ObjectError> allErrors = br.getAllErrors();

        //校验不通过的情况
        if (allErrors != null && allErrors.size() > 0) {

            //后边判断是哪个属性的error
            FieldError nameError = br.getFieldError("name");
            FieldError ageError = br.getFieldError("age");
            FieldError phoneError = br.getFieldError("phone");

            if (nameError != null) {
                //若异常对象非空，把异常信息添加到modelAndView中
                mv.addObject("nameError", nameError.getDefaultMessage());
            }
            if (ageError != null) {
                mv.addObject("ageError", ageError.getDefaultMessage());
            }
            if (phoneError != null) {
                mv.addObject("phoneError", phoneError.getDefaultMessage());
            }

            mv.setViewName("/register");
            return mv;
        }

        mv.addObject("name", user.getName());
        mv.addObject("msg", "注册成功");
        mv.setViewName("/user");
        return mv;
    }
}

```

### 1.5 创建register.jsp，提交表单信息。
springMVC用JavaBean中的user来接受，通过验证其校验。

```html
<%--
  Created by IntelliJ IDEA.
  User: panda
  Date: 2019/4/10
  Time: 12:08
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>注册</title>
</head>
<body>
<h1>欢迎注册！请要求填写信息</h1>
<form action="/user/register.do" method="post">
    姓名:<input type="text" name="name">${nameError}<br>
    年龄:<input type="text" name="age">${ageError}<br>
    手机号:<input type="text" name="phone">${phoneError}<br>
    <input type="submit" value="提交">

</form>

</body>
</html>

```

### 1.6 创建user.jsp，显式注册成功。

```html
<%--
  Created by IntelliJ IDEA.
  User: panda
  Date: 2019/4/10
  Time: 12:12
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>注册成功</title>
</head>
<body>
${name}<br>
${msg}

</body>
</html>

```
## 2 效果

若不符合校验要求，提交后返回提示信息：

若符合要求，则跳转到注册成功页面：

## 3 Hibernate Validator 中常用的验证注解

- @AssertFalse 验证注解的元素值是 false
- @AssertTrue 验证注解的元素值是 true
- @DecimalMax（value=x） 验证注解的元素值小于等于指定的十进制value 值
- @DecimalMin（value=x） 验证注解的元素值大于等于指定的十进制value 值
- @Digits(integer=整数位数, fraction=小数位数)验证注解的元素值的整数位数和小数位数上限
- @Future 验证注解的元素值（日期类型）比当前时间晚
- @Max（value=x） 验证注解的元素值小于等于指定的 value值
- @Min（value=x） 验证注解的元素值大于等于指定的 value值
- @NotNull 验证注解的元素值不是 null
- @Null 验证注解的元素值是 null
- @Past 验证注解的元素值（日期类型）比当前时间早
- @Pattern(regex=正则表达式) 验证注解的元素值不指定的正则表达式匹配
- @Size(min=最小值, max=最大值) 验证注解的元素值的在 min 和 max （包含）指定区间之内，如字符长度、集合大小
- @Valid 该注解主要用于字段为一个包含其他对象的集合或map或数组的字段，或该字段直接为一个其他对象的引用，这样在检查当前对象的同时也会检查该字段所引用的对象。
- @NotEmpty 验证注解的元素值不为 null 且不为空（字符串长度不为 0、集合大小不为 0）
- @Range(min=最小值, max=最大值)验证注解的元素值在最小值和最大值之间
- @NotBlank 验证注解的元素值不为空（不为 null、去除首位空格后长度为0），不同于@NotEmpty， @NotBlank只应用于字符串且在比较时会去除字符串的空格
- @Length(min=下限, max=上限) 验证注解的元素值长度在 min 和 max 区间内
- @Email 验证注解的元素值是Email，也可以通过正则表达式和 flag 指定自定义的 email 格式
