import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class NioSocketDemo {
    private Selector selector;//通道选择器（管理器
    public static void main(String[] args) throws IOException {
        NioSocketDemo nio = new NioSocketDemo();
        nio.initServer(8888);
        nio.listenSelector();
    }
    public void initServer(int port) throws IOException{
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);//非阻塞
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server start...");
    }
    public void listenSelector() throws IOException {
        //轮询监听selector
        while(true){
            //等待用链接，相当于传统的accept
            //select 模型，多路复用
            selector.select();
//            selector.selectNow();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                //处理请求
                iterator.remove();;
                handler(key);
            }
        }
    }

    private void handler(SelectionKey key) throws IOException {
        if(key.isAcceptable()){
            System.out.println("new client add");
            //处理客户端链接请求事件
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();
            //别忘记了设置非阻塞
            socketChannel.configureBlocking(false);
            //接收客户端发送的信息是后，需要给通道设置读的权限
            socketChannel.register(selector,SelectionKey.OP_READ);
        }else if(key.isReadable()){
            //处理读的事件
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //nio的关键点在这个地方，没有阻塞
            int readData = socketChannel.read(buffer);
            if(readData>0){
                String data = new String(buffer.array(), "GBK").trim();
                System.out.println("get message:"+data);
            }else{
                System.out.println("client close");
                key.cancel();
            }
        }
    }
}
