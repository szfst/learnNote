##### 一、高并发思路：
- 队列削峰：广播机制、分组消费
- 并发分布式锁，单点处理共享
- 数据实时性的取舍
- 缓存
- 服务降级
- 限流
##### 二、redis
- 基本补充内容
	- 1、默认的数据库16个，默认取第0个，用select 1切换数据库
	- 2、ttl：查看剩余时间，以秒为单位，-1表示不会过期，-2表示没有这个值或者过期了。
	- 3、type：查看key值类型，type a
	- 4、nx结尾命令
	- 5、jedis和redis的版本最好保持一致
	- 6、redis命名空间管理，用":",
	- 7、redis-cli监听日志命令：monitor
- redis分布式锁
	- 1、setnx和getset方法都有原子性
	- 2、redis分布式命令：setnx、getset、expire、del方法
	- 3、分布式流程图：
		- 1、基本思路：
		
![avatar](https://github.com/szfst/learnNote/blob/master/jgyj/redis/redis-1.jpg?raw=true)

	private void closeOrder(String lockName){
        RedisShardedPoolUtil.expire(lockName,5);//有效期50秒，防止死锁
        log.info("获取{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
        iOrderService.closeOrder(hour);
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        log.info("===============================");
    }
    //
	log.info("关闭订单定时任务启动");
	long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));
	Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
        if(setnxResult != null && setnxResult.intValue() == 1){
            //如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else{
            log.info("没有获得分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
        
- 2、优化思路：
    - 为什么要用此方法：双重防止死锁，防止在未设置expire的时候关闭程序，锁无法释放

![avatar](https://github.com/szfst/learnNote/blob/master/jgyj/redis/redis-2.jpg?raw=true)

	    log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
        if(setnxResult != null && setnxResult.intValue() == 1){
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else{
            //未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if(lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)){
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
                //再次用当前时间戳getset。
                //返回给定的key的旧值，->旧值判断，是否可以获取锁
                //当key没有旧值时，即key不存在时，返回nil ->获取锁
                //这里我们set了一个新的value值，获取旧的值。
                if(getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr,getSetResult))){
                    //真正获取到锁
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }else{
                    log.info("没有获取到分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else{
                log.info("没有获取到分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("关闭订单定时任务结束");
- 3、java直接用redission</br>
	redission wait_time，获取锁的等待时间，最好统一设置为0，防止有坑	
```java	
	RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            if(getLock = lock.tryLock(0,50, TimeUnit.SECONDS)){
                log.info("Redisson获取到分布式锁:{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
//                iOrderService.closeOrder(hour);
            }else{
                log.info("Redisson没有获取到分布式锁:{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redisson分布式锁获取异常",e);
        } finally {
            if(!getLock){
                return;
            }
            lock.unlock();
            log.info("Redisson分布式锁释放锁");
        }
```
- redis配置
最大连接数
redis.max.total=20
最大空闲数
redis.max.idle=10
最小空闲数
redis.min.idle=2
从jedis连接池获取连接时，校验并返回可用的连接
redis.test.borrow=true
把连接放回jedis连接池时，校验并返回可用的连接
redis.test.return=false
//连接耗尽是否阻塞
config.setBlockWhenExhausted(true);//连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true。
- pool.close()
	- 弃用RedisPool.returnBrokenResource(jedis)，           RedisPool.returnResource(jedis)，改用close方法。
- 单点登录：
	- cookie.setHttpOnly(true);不能通过脚本获取cookie，防止攻击（防止别人吧cookies发送到自己的网站做非法用途）
- redis分布式：
	- 分布式算法：consistent hashing（一致性hash算法）
	- 原理：环形hash，虚拟节点
		- 命中率计算公式：（1-n/(n+m))*100%，n为服务器数量
	- java代码：shardedJedis
- redis主从配置，配置slave of，主从同步，从不能写只能读，主写了数据之后从就有主写的数据，但是从不能写
##### 三、nginx：
- 1、upstream：</br>
![avatar](https://github.com/szfst/learnNote/blob/master/jgyj/nginx/nginx-1.jpg?raw=true)
- 2、nginx静态文件需要重新配置，否则有可能出现访问异常
- 3、可以通过nginx开放80端口，其他的都可以通过nginx去做跳转，不需要开放了。
##### 四、restful：
</br>restful是根据资源来定位的
- 1、哪些接口不适合用restful？传递一个对象的情况，用restful就很难受
- 2、遇到这些难处怎么办？自定义资源占位（也就是在url多加一些自定义的资源）
##### 五、其他
- 重写HttpServletResponse：
```java
response.reset();//这里要添加reset，否则报异常 getWriter() has already been called for this response.
response.setCharacterEncoding("UTF-8");//这里要设置编码，否则会乱码
response.setContentType("application/json;charset=UTF-8");//这里要设置返回值的类型，因为全部是json接口。
PrintWriter out = response.getWriter();
out.print(JsonUtil.obj2String(resultMap));
out.flush();
out.close();// 这里要关闭       
```
##### 六、MYSQL悲观锁乐观锁
- 乐观锁：乐观锁的特点先进行业务操作，不到万不得已不去拿锁。即“乐观”的认为拿锁多半是会成功的，因此在进行完业务操作需要实际更新数据的最后一步再去拿一下锁就好。
乐观锁在数据库上的实现完全是逻辑的，不需要数据库提供特殊的支持。一般的做法是在需要锁的数据上**增加一个版本号，或者时间戳**，然后按照如下方式实现：</br>
```sql
	1. SELECT data AS old_data, version AS old_version FROM …;
	2. 根据获取的数据进行业务操作，得到new_data和new_version
	3. UPDATE SET data = new_data, version = new_version WHERE version = old_version
	if (updated row > 0) {
	    // 乐观锁获取成功，操作完成
	} else {
	    // 乐观锁获取失败，回滚并重试
	}
	
```
乐观锁是否在事务中其实都是无所谓的，其底层机制是这样：在数据库内部update同一行的时候是不允许并发的，即数据库每次执行一条update语句时会获取被update行的写锁，直到这一行被成功更新后才释放。因此在业务操作进行前获取需要锁的数据的当前版本号，然后实际更新数据时再次对比版本号确认与之前获取的相同，并更新版本号，即可确认这之间没有发生并发的修改。如果更新失败即可认为老版本的数据已经被并发修改掉而不存在了，此时认为获取锁失败，需要回滚整个业务操作并可根据需要重试整个过程。</br>
- 悲观锁：悲观锁的特点是先获取锁，再进行业务操作，即“悲观”的认为获取锁是非常有可能失败的，因此要先确保获取锁成功再进行业务操作。通常所说的“一锁二查三更新”即指的是使用悲观锁。通常来讲在数据库上的悲观锁需要数据库本身提供支持，即通过常用的**select … for update**操作来实现悲观锁。当数据库执行select for update时会获取被select中的数据行的行锁，因此其他并发执行的select for update如果试图选中同一行则会发生排斥（需要等待行锁被释放），因此达到锁的效果。select for update获取的行锁会在当前事务结束时自动释放，因此必须在事务中使用。
这里需要注意的一点是不同的数据库对select for update的实现和支持都是有所区别的，例如oracle支持select for update no wait，表示如果拿不到锁立刻报错，而不是等待，mysql就没有no wait这个选项。另外mysql还有个问题是select for update语句执行中所有扫描过的行都会被锁上，这一点很容易造成问题。因此如果在mysql中用悲观锁务必要确定走了索引，而不是全表扫描。
- 行锁，表锁（针对对悲观锁）：
	- 1、明确指定主键，并且有结果集，row-level Lock（id是主键，也即是索引）</br>
			<code>select * from table_a where id = "66" for update</code>
	- 2、明确指定主键，并且无结果集，无Lock（id是主键，也即是索引）</br>
			<code>select * from table_a where id = "-100" for update</code>
	- 3、无主键，Table-Level Lock（name不是主键，也不是索引）</br>
			<code>select * from table_a where name = "iphone" for update</code>	
	- 4、主键不明确，Table-Level Lock</br>
			<code>select * from table_a where id <>  "66"  for update</code>
			<code>select * from table_a where id like "66"  for update</code>	
- 总结：
	- 乐观锁在不发生取锁失败的情况下开销比悲观锁小，但是一旦发生失败回滚开销则比较大，因此适合用在取锁失败概率比较小的场景，可以提升系统并发性能
	- 乐观锁还适用于一些比较特殊的场景，例如在业务操作过程中无法和数据库保持连接等悲观锁无法适用的地方			
