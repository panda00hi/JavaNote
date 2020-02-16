## 1 三种配置方式

### 1、设置url-pattern为*.do

在web.xml文件中配置DispatcherServlet的时候，将url-pattern配置为*.do的方式，其实除了这种方式之外你还可以配置为其他任意方式：

``` 
*.action
*.abc
*.123
```

只要你的请求url中包含配置的url-pattern，该url就可以到达DispatcherServlet。当然这里业内通常都将url-pattern配置为*.do的方式

### 2、设置url-pattern为/*

如果将url-pattern设置为/*之后，会报出404的错误。
原因：因为/*表示匹配所有，返回jsp视图时first.jsp会再次进入spring的DispatcherServlet类，然后此时是没有对应的Controller来处理的。

其实说的简单一点就是/*这种配置会匹配把本不该匹配到的，如返回到前端的内容，再次捕获，导致缺失正常逻辑下的Controller处理而报错。
在实际开发中最好不要这样配置url-pattern。

### 3、设置url-pattern为/

如果将url-pattern设置为/之后，只要是在web.xml文件中找不到匹配的URL，它们的访问请求都将交给DispatcherServlet处理，静态资源：css文件, js文件, 图片也会被拦截并交给DispatcherServlet处理。
该配置方式不会拦截.jsp文件和.jspx文件，因为这个在tomcat中的conf目录里面的web.xml文件中已经添加的相应的处理方式了，他会交给org.apache.jasper.servlet. JspServlet来处理。即我们可以正常访问系统中的jsp文件。

``` xml
<!-- The mapping for the default servlet -->
    <servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

    <!-- The mappings for the JSP servlet -->
    <servlet-mapping>
        <servlet-name>jsp</servlet-name>
        <url-pattern>*.jsp</url-pattern>
        <url-pattern>*.jspx</url-pattern>
    </servlet-mapping>
```

现在restful风格的URL越来越流行，推荐使用。

## 2 解决静态资源不能访问：

### 1、使用defaultServlet  

在tomcat安装目录中conf/web.xml, 在这个文件中有一个叫做DefaultServlet的配置, 当系统找不到处理某次url请求该交由谁处理的时候，就会交给这个servlet处理。我们可以通过使用这个DefaultServlet来处理静态资源，在项目的web.xml文件中添加下面配置，要添加在DispatcherServlet的前面，这样会将带有下面后缀名的请求交给defaultservlet来处理：

``` xml
<servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>*.jpg</url-pattern>
</servlet-mapping>
servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>*.js</url-pattern>
</servlet-mapping>
<servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>*.css</url-pattern>
</servlet-mapping>
```

### 2、使用mvc:default-servlet-handler    

在springmvc.xml文件中添加下面配置即可，该方式会对所有的请求进行处理，然后交由相应的servlet，这种方式其实最终也是由DefaultServlet来处理：

``` xml
<mvc:default-servlet-handler/> 
```

### 3、使用mvc:resources  

在springmvc中提供了mvc:resources标签用来解决静态资源无法访问的问题，只需要在springmvc.xml的配置文件中添加下面内容即可，这样会交给spring mvc的ResourceHttpRequestHandler类来处理：

``` 
<mvc:resources mapping="/images/**" location="/images/" />
```

**注意：**

* mapping 表示对该资源的请求。注意，后面是两个星号**。
* location 表示静态资源所在目录，在我的项目中就在webapp下创建一个images文件夹，我会将所有的图片放到这个文件夹下。

