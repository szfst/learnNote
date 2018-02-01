##### 二、架构演进：
- 高并发思路：
	- 队列削峰（限流）：广播机制、分组消费
	- 并发分布式锁，单点处理共享
	- 数据实时性的取舍
	- 缓存
	- 服务降级
##### 三、redis
- 基本补充内容
	- 1、默认的数据库16个，默认取第0个，用select 1切换数据库
	- 2、ttl：查看剩余时间，以秒为单位，-1表示不会过期，-2表示没有这个值或者过期了。
	- 3、type：查看key值类型，type a
	- 4、nx结尾命令
	- 5、jedis和redis的版本最好保持一致
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