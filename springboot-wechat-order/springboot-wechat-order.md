##### 一、日志记录：
- 1、日志框架的能力：
	- 定制输出目标
	- 定制输出格式
	- 携带上下文信息
	- 运行时选择性输出
	- 灵活的配置
	- 优异的性能
- 2、选择：日志门面：SL4J;日志实现：logback
- 3、日志级别：可以自定义输出什么级别的(可以定义小于的不输出，等于的不输出，或者等于的输出)
	- 日志级别源码分类：</br>
	```java
    WARN(30, "WARN"),
    INFO(20, "INFO"),
    DEBUG(10, "DEBUG"),
    TRACE(0, "TRACE");
    ```
- 4、@SL4J：就可以不用写当前的类名
	- @Data 可以不写get、set方法 
	- 安装lombok plugin
	- 引入这个依赖：</br>
```java
	 <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
```
 - 5、可以区分info和error文件存放日志       
##### 二、微信：
- 区分公众平台和开放平台
##### 三、大型项目：
 - 分布式：不同的业务由不同的机器去执行
 - 集群：相同的业务由不同的机器去执行
 - redis：安装rdm(redis desktop manager)
##### 四、高并发
- apache ab来压测自己的网站：
	- ab -n 100(100个请求) -c 100(1秒内100个并发) http://www.baidu.com
	- ab -t 60(连续60秒) -c 100 http://www.baidu.com
- 秒杀多线程存在问题解决
	- 1、synchronize，不够好
		- 粒度太大，性能不够好
		- 只适合单点
	- 2、redis分布式锁
		- setnx命令（set if not exist）
		- getset命令：先把之前的值取出来，在设置之前的值为当前值
		- 优点：支持分布式、可以更细粒度地控制、多台机器上多个进程读同一个数据进行操作
		- 缓存实现序列化：用serialize插件自动生成序列号
##### 五、部署
- Tomcat：
- java -jar