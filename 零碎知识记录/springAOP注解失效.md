**场景描述**：
在业务中，service的实现类中，某一个类下，有方法A、B。其中，A方法主要是查询相关，并未涉及到数据变更，因此不需要添加事务注解 `@Transactional(rollbackFor = Exception.class)` 。但是，A方法的执行中，需要调用B方法来获取进行某些变更后的值。（方法B涉及到了数据变更，因此有事务注解。A方法需要保证数据的实时性或其他原因，不适合使用事务注解）

**问题：**
如何使A中调用的B方法事务注解生效？？？？

**分析：**
针对所有的Spring AOP注解，Spring在扫描bean的时候如果发现有此类注解，那么会动态构造一个代理对象。

如果你想要通过类X的对象直接调用其中带注解的A方法，此注解是有效的。因为此时，Spring会判断你将要调用的方法上存在AOP注解，那么会使用类X的代理对象调用A方法。

但是假设类X中的A方法会调用带注解的B方法，而你依然想要通过类X对象调用A方法，那么B方法上的注解是无效的。因为此时Spring判断你调用的A并无注解，所以使用的还是原对象而非代理对象。接下来A再调用B时，在原对象内B方法的注解当然无效了。

**解决方法：**
最简单的方式当然是可以让方法A和B没有依赖，能够直接通过类X的对象调用B方法。

但是很多时候可能我们的逻辑拆成这样写并不好，那么就还有一种方法：想办法手动拿到代理对象。

AopContext类有一个currentProxy()方法，能够直接拿到当前类的代理对象。那么以上的例子，就可以这样解决：

``` JAVA
// 在A方法内部调用B方法
// 1.直接调用B，注解失效。
B()
// 2.拿到代理类对象，再调用B。
((X)AopContext.currentProxy()).B()
```

``` java
Map<String, Object> stringIntegerMap = ((WorkQualityTempServiceImpl) AopContext.currentProxy()).getStringIntegerMap(checkBatchId);
```

