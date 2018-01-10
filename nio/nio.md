##### 一、阻塞与非阻塞：
- 阻塞：做某件事情，知道完成，除非超时，如果没有完成，继续等待
- 非阻塞：做一件事情，尝试着去做，如果不能做完，就不做了，意思是直接返回，如果能够做完，就做完
##### 二、传统IO模型，存在的阻塞点：
 -  ![avatar](https://raw.githubusercontent.com/szfst/learnNote/master/nio/img/io.png)
 - 传统IO，socket编程（windows telnet ctrl+]可发送多个字符，cmd的字符集为GBK）
 - [阻塞点]https://github.com/szfst/learnNote/blob/master/nio/code/TraditionalSocketDemo.java：
	 - <code>Socket socket = serverSocket.accept();</code>
	 -  <code>int data = is.read(b);</code>
     - 启用多个线程
     - 缺点：
	     - 占用资源，系统资源：IO、存储、内存、CPU
	 - 优点：
		 - 一对一服务，服务质量比较优秀
	- 提高点：多路复用
##### 三、NIO模型
- ![avatar](https://raw.githubusercontent.com/szfst/learnNote/master/nio/img/nio.png)
- 相比传统IO改进：
  - 增加了一个重要角色（Selector），主要负责调度和监控客户端和服务器（调度器），可以选择为哪个客户端服务(其实它内部也是有一个线程池)
  - 由阻塞方式改为非阻塞（non-blocking）
  - 阻塞点：
	  - <code>selector.select();</code>
	  - 真正关心的阻塞点事：读取数据（没有阻塞）
##### 四、对比总结
- 传统IO：
  - 为每一个线程创建一个连接
- NIO
  - 使用多路复用，实际上内部也是有一个线程池，线程是有限的，线程数要小于连接数
