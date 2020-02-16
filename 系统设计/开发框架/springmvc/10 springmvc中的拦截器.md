## 1 拦截器的作用以及和过滤器的区别
Spring MVC 的处理器拦截器类似于 Servlet 开发中的过滤器 Filter，用于对处理器进行预处理和后处理。
用户可以自己定义一些拦截器来实现特定的功能。
谈到拦截器，还要向大家提一个词——拦截器链（Interceptor Chain）。拦截器链就是将拦截器按一定的顺
序联结成一条链。在访问被拦截的方法或字段时，拦截器链中的拦截器就会按其之前定义的顺序被调用。

**过滤器**是 servlet 规范中的一部分，任何 java web 工程都可以使用。
**拦截器**是 SpringMVC 框架自己的，只有使用了 SpringMVC 框架的工程才能用。
**过滤器**在 url-pattern 中配置了/*之后，可以对所有要访问的资源拦截。
**拦截器**它是只会拦截访问的控制器Controller的方法请求，如果访问的是 jsp，html,css,image 或者 js 是不会进行拦
截的。
它也是 AOP 思想的具体应用。
我们要想自定义拦截器， 要求必须实现：HandlerInterceptor 接口。

## 2 实现自定义的拦截器

定义一个类实现HandlerInterceptor接口，这样就创建了一个拦截器，该接口中有三个方法：
- preHandle(request, response, Object handler)：  
该方法在controller中的方法执行之前执行。其返回值为 boolean，若为 true，则紧接着会执行controller方法，且会将afterCompletion()方法压栈进入入到一个专门的方法栈中等待执行。
- postHandle(request, response, Object handler, modelAndView)：  
该方法在controller方法执行之后执行。controller方法若最终未被执行，则该方法不会执行。由于该方法是在controller方法执行完后执行，且该方法参数中包含ModelAndView，所以该方法可以修改controller方法的处理结果数据，且可以修改跳转方向。
- afterCompletion(request, response, Object handler, Exception ex)：  
当 preHandle()方法返回 true 时，会将该方法放到专门的方法栈中，等到对请求进行响应的所有
工作完成之后才执行该方法。即该方法是在中央调度器渲染（数据填充）了响应页面之后执行的，此时对 ModelAndView 再操作也对响应无济于事。

### 2.1 一个拦截器的情况

#### 2.1.1 在interceptor包下自定义一个拦截器MyInterceptor.java：

```
package com.panda00hi.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器
 * 只拦截对controller的请求，jsp等不拦截
 */
public class MyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println("拦截器中的preHandle方法");

        //如果返回true，将继续向下执行。
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("拦截器中的postHandle方法");

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("拦截器中的afterCompletion方法");
    }
}


```
#### 2.1.2 在springmvc.xml中注册拦截器

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">

    <!--注解驱动-->
    <mvc:annotation-driven/>

    <!--注册组件扫描器-->
    <context:component-scan base-package="com.panda00hi.*"/>

    <!--注册拦截器，/**表示拦截所有请求-->
    <mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/**"/>
            <bean class="com.panda00hi.interceptor.MyInterceptor"/>
        </mvc:interceptor>
    </mvc:interceptors>

    <!--静态资源-->
    <mvc:resources mapping="/imags/**" location="/images/"/>
    <mvc:resources mapping="/css/**" location="/css/"/>


    <!--内部视图解析器-->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/jsp/"/>
        <property name="suffix" value=".jsp"/>
    </bean>

</beans>
```
#### 2.1.3 创建TestController和返回结果的result.jsp

TestController.java

```
package com.panda00hi.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestController {

    @RequestMapping("/test.do")
    public ModelAndView test() throws Exception{
        ModelAndView mv = new ModelAndView();

        System.out.println("test方法");
        mv.setViewName("result");

        return mv;
    }
}

```

result.jsp
```
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>result</title>
</head>
<body>
<h1>this is the result.jsp</h1>
</body>
</html>

```

#### 2.1.4 创建Filter过滤器


```
package com.panda00hi.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/*")
public class MyFilter implements Filter {

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        System.out.println("过滤器中的doFilter方法");
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}

```


#### 2.1.5 部署到tomcat，查看效果
浏览器访问   
http://localhost:8080/test.do  

***控制台依次打印**：*  
```
过滤器中的doFilter方法  
拦截器中的preHandle方法  
test方法   
拦截器中的postHandle方法  
拦截器中的afterCompletion方法  
```

浏览器访问   
http://localhost:8080/jsp/result.jsp

***控制台只打印**：*  
```
过滤器中的doFilter方法
```

### 2.2 多个拦截器

#### 2.2.1 再定义一个拦截器MyInterceptor2.java

``` JAVA

package com.panda00hi.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 拦截器2
 */
public class MyInterceptor2 implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println("拦截器2中的preHandle方法");

        //如果返回true，将继续向下执行。
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("拦截器2中的postHandle方法");

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("拦截器2中的afterCompletion方法");
    }
}

```
#### 2.2.2 在springmvc.xml配置文件中注册第二个拦截器

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">

    <!--注解驱动-->
    <mvc:annotation-driven/>

    <!--注册组件扫描器-->
    <context:component-scan base-package="com.panda00hi.*"/>

    <!--注册拦截器，可多个，/**表示拦截所有请求-->
    <mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/**"/>
            <bean class="com.panda00hi.interceptor.MyInterceptor"/>

        </mvc:interceptor><mvc:interceptor>
            <mvc:mapping path="/**"/>
            <bean class="com.panda00hi.interceptor.MyInterceptor2"/>
        </mvc:interceptor>
    </mvc:interceptors>

    <!--静态资源-->
    <mvc:resources mapping="/imags/**" location="/images/"/>
    <mvc:resources mapping="/css/**" location="/css/"/>


    <!--内部视图解析器-->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/jsp/"/>
        <property name="suffix" value=".jsp"/>
    </bean>

</beans>
```
#### 2.2.3 当有请求访问controller时，在控制台会打印出下面内容：


```
拦截器中的preHandle方法
拦截器2中的preHandle方法
test方法
拦截器2中的postHandle方法
拦截器中的postHandle方法
拦截器2中的afterCompletion方法
拦截器中的afterCompletion方法

```

当有多个拦截器时，会形成拦截器链。拦截器链的执行顺序，与其注册顺序一致。

#### 2.2.4 流程图示：

![拦截器方法执行流程图](https://upload-images.jianshu.io/upload_images/5353735-d4a42147ce660295.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





