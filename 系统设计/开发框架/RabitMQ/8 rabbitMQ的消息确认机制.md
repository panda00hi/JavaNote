## 1 事务机制

### 1.1 生产者

``` JAVA
package com.mmr.rabbitmq.tx;

import com.mmr.rabbitmq.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 事务机制-生产者
 *
 * @author panda00hi
 * 2020/3/31
 */
public class TxSend {
    private static final String QUEUE_NAME = "test_queue_tx";

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String msg = "hello tx message!";
        try {
            channel.txSelect();
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
            // 模拟出现异常
            int i = 1 / 0;
            System.out.println("send " + msg);
            channel.txCommit();
        } catch (Exception e) {
            channel.txRollback();
            System.out.println("send message txRollback");
        } finally {
            channel.close();
            connection.close();
        }
    }
}
```

### 1.2 消费者

``` JAVA
package com.mmr.rabbitmq.tx;

import com.mmr.rabbitmq.util.ConnectionUtils;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 消费者
 *
 * @author panda00hi
 * 2020/3/31
 */
public class TxRecv {
    private static final String QUEUE_NAME = "test_queue_tx";

    public static void main(String[] args) throws IOException, TimeoutException {

        Connection connection = ConnectionUtils.getConnection();
        final Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        channel.basicConsume(QUEUE_NAME, true, new DefaultConsumer(channel) {
            // 消息到达，将触发此方法
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("Recv[tx] msg: " + msg);
            }
        });
    }
}
```

### 1.3 缺点

会降低吞吐量

## 2 confirm模式

### 2.1 生产者端confirm模式的实现原理

> 生产者将信道设置成confirm模式，一旦信道进入confirm模式，所有在该信道上面发布的消息都会被指派一个唯一的ID(从1开始)，一旦消息被投递到所有匹配的队列之后，broker就会发送一个确认给生产者(包含消息的唯一ID)，这就使得生产者知道消息已经正确到达目的队列了，如果消息和队列是可持久化的，那么确认消息会将消息写入磁盘之后发出，broker回传给生产者的确认消息中deliver-tag域包含了确认消息的序列号。此外broker也可以设置basicAck的multiple域，表示到这个序列号之前的所有消息都已经得到了处理。

**confirm模式最大的好处是异步处理，减少了等待**

### 2.2 confi模式的使用

开启confirm模式：
channel.confirmSelect()

发送模式：
1 单条/多条：调用waitForConfirms()确认。
2 异步：异步模式，rabbitmq提供一个回调方法。

#### 2.2.1 单条或多条批量消息

**生产者1**：单条

``` JAVA
package com.mmr.rabbitmq.confirm;

import com.mmr.rabbitmq.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 普通模式:发送单条消息
 *
 * @author panda00hi
 * 2020/3/31
 */
public class Send1 {
    private static final String QUEUE_NAME = "test_queue_confirm1";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = ConnectionUtils.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 生产者调用confirmSelect()方法，将channel设置为confirm模式
        channel.confirmSelect();
        String msg = "hello confirm message";
        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());

        if (!channel.waitForConfirms()) {
            System.out.println("message send failed!");
        } else {
            System.out.println("message send ok!");
        }

        channel.close();
        connection.close();
    }
}
```
**生产者2**：多条批量
``` JAVA
package com.mmr.rabbitmq.confirm;

import com.mmr.rabbitmq.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 批量模式:发送多条消息
 *
 * @author panda00hi
 * 2020/3/31
 */
public class Send2 {
    private static final String QUEUE_NAME = "test_queue_confirm1";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = ConnectionUtils.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 生产者调用confirmSelect()方法，将channel设置为confirm模式
        channel.confirmSelect();
        String msg = "hello confirm message batch";
        // 批量发送
        for (int i = 0; i < 10; i++) {
            channel.basicPublish("", QUEUE_NAME, null, (msg + " " + i).getBytes());
        }
        // 确认
        if (!channel.waitForConfirms()) {
            System.out.println("message send failed!");
        } else {
            System.out.println("message send ok!");
        }

        channel.close();
        connection.close();
    }
}
```
**消费者**：
``` JAVA
package com.mmr.rabbitmq.confirm;

import com.mmr.rabbitmq.util.ConnectionUtils;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 消费者
 *
 * @author panda00hi
 * 2020/3/31
 */
public class Recv {
    private static final String QUEUE_NAME = "test_queue_confirm1";

    public static void main(String[] args) throws IOException, TimeoutException {

        Connection connection = ConnectionUtils.getConnection();
        final Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicConsume(QUEUE_NAME, true, new DefaultConsumer(channel) {
            // 消息到达，将触发此方法
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("Recv[confirm] msg: " + msg);
            }
        });
    }
}
```
#### 2.3 异步模式

Channel对象提供的ConfirmListener()回调方法包含deliverTag(当前Channel发出的消息序号)，我们需要自己为每一个Channel维护一个unconfirm的消息序号集合，每publish一条数据，集合中元素加1，每回调一次handleAck方法，unconfirm集合删掉相应的一条(multiple=false)或多条(multiple=true)记录。从程序运行效率上看，这个unconfirm集合最好采用有序集合SortedSet存储结构。

生产者：
``` JAVA
package com.mmr.rabbitmq.confirm;

import com.mmr.rabbitmq.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

/**
 * 异步模式
 *
 * @author panda00hi
 * 2020/3/31
 */
public class Send3 {
    private static final String QUEUE_NAME = "test_queue_confirm3";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = ConnectionUtils.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 生产者调用confirmSelect()方法，将channel设置为confirm模式
        channel.confirmSelect();
        // 未确认的消息标识
        final SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<Long>());

        // 通道添加监听
        channel.addConfirmListener(new ConfirmListener() {
            // 没有问题的handleAck
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                if (multiple) {
                    System.out.println("----handleAck---multiple");
                    // headSet方法返回的是指定元素之前的，所以需要加1，才能包含当前返回的元素
                    confirmSet.headSet(deliveryTag + 1).clear();
                } else {
                    System.out.println("---handleAck---multiple false");
                    confirmSet.remove(deliveryTag);
                }
            }

            // 有问题的
            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                if (multiple) {
                    System.out.println("---handleNack---multiple");
                    confirmSet.headSet(deliveryTag + 1).clear();
                } else {
                    System.out.println("---handleNack---multiple false");
                    confirmSet.remove(deliveryTag);
                }
            }
        });

        String msg = "hello confirm message batch";
        while (true) {
            long seqNo = channel.getNextPublishSeqNo();
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
            confirmSet.add(seqNo);
        }
    }
}
```




