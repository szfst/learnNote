##### 一、高并发思路：
- 队列削峰（限流）：广播机制、分组消费
- 并发分布式锁，单点处理共享
- 数据实时性的取舍
- 缓存
- 服务降级
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
- redis主从配置，配置slave of，主从同步，冲不能读只能写，主写从就有，但是从不能读
##### 三、nginx：
1、upstream：
![avatar](https://github.com/szfst/learnNote/blob/master/jgyj/nginx/nginx-1.jpg?raw=true)
2、nginx静态文件需要重新配置，否则有可能出现访问异常
3、可以通过nginx开放80端口，其他的都可以通过nginx去做跳转，不需要开放了。
##### 四、restful：
restful是根据资源来定位的
1、哪些接口不适合用restful？传递一个对象的情况，用restful就很难受
2、遇到这些难处怎么办？自定义资源占位（也就是在url多加一些自定义的资源）
