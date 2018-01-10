import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TraditionalSocketDemo {
    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(7777);
        System.out.println("server start...");
        while(true){
            Socket socket = serverSocket.accept();
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("new client connect...");
                    InputStream is = null;
                    try {
                        is = socket.getInputStream();
                        byte[] b =new byte[1024];
                        while(true){
                            int data = is.read(b);
                            if(data!=-1){
                                String info = new String(b,0,data,"GBK");
                                System.out.println(info);
                            }else{
                                break;
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }
}
