## spring集成rabbitMQ

依赖：
```xml
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit</artifactId>
    <version>2.2.5.RELEASE</version>
</dependency>
```

### 生产者
``` JAVA
package com.mmr.rabbitmq.spring;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author panda00hi
 * 2020/3/31
 */
public class SpringMain {

    public static void main(final String... args) throws Exception {
        AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:rabbitmq-context.xml");
        //RabbitMQ模板
        RabbitTemplate template = ctx.getBean(RabbitTemplate.class);
        //发送消息
        template.convertAndSend("Hello, world!!");
        Thread.sleep(1000);// 休眠1秒
        //容器销毁
        // destroy已过时
        // ctx.destroy();
        ctx.close();
    }

}
```
### 消费者
``` JAVA
package com.mmr.rabbitmq.spring;

/**
 * @author panda00hi
 * 2020/3/31
 */
public class MyConsumer {
    //具体执行业务的方法
    public void listen(String foo) {
        System.out.println("\n消费者： " + foo + "\n");
    }
}

```
### xml配置
resources目录下，新建rabbitmq-context.xml
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:rabbit="http://www.springframework.org/schema/rabbit"
       xsi:schemaLocation="http://www.springframework.org/schema/rabbit
   http://www.springframework.org/schema/rabbit/spring-rabbit-1.4.xsd
   http://www.springframework.org/schema/beans
   http://www.springframework.org/schema/beans/spring-beans-4.1.xsd">

  <!-- 定义RabbitMQ的连接工厂 -->
  <rabbit:connection-factory id="connectionFactory"
                             host="127.0.0.1" port="5672" username="user_mmr" password="123456"
                             virtual-host="/vhost_mmr"/>

  <!-- 定义Rabbit模板，指定连接工厂以及定义exchange -->
  <rabbit:template id="amqpTemplate" connection-factory="connectionFactory" exchange="fanoutExchange"/>

  <!-- MQ的管理，包括队列、交换器等 -->
  <rabbit:admin connection-factory="connectionFactory"/>

  <!-- 定义队列，自动声明 -->
  <rabbit:queue name="myQueue" auto-declare="true"/>

  <!-- 定义交换器，把Q绑定到交换机，自动声明 -->
  <rabbit:fanout-exchange name="fanoutExchange" auto-declare="true">
    <rabbit:bindings>
      <rabbit:binding queue="myQueue"/>
    </rabbit:bindings>
  </rabbit:fanout-exchange>

  <!-- 队列监听 -->
  <rabbit:listener-container connection-factory="connectionFactory">
    <rabbit:listener ref="myConsumer" method="listen" queue-names="myQueue"/>
  </rabbit:listener-container>

  <bean id="myConsumer" class="com.mmr.rabbitmq.spring.MyConsumer"/>

</beans>

```