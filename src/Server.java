import java.io.*;
import java.net.*;

public class Server extends Thread
{
    // открываемый порт сервера
    private static final int port = 1111;

    private  Socket socket;

    public Server() {}
    public void setSocket(int num, Socket socket)
    {
        this.socket = socket;
        setDaemon(true);
        setPriority(NORM_PRIORITY);
        start();
    }
    public void run() {
        try{
            while (true) {
                System.out.println("Waiting for a menu item ");
                InputStream imMen = socket.getInputStream();
                DataInputStream impMen = new DataInputStream(imMen);
                String sm = impMen.readUTF();
                while ((socket.isConnected()) && (sm.equals("1") || sm.equals("2") || sm.equals("3"))) {
                    if (sm.equals("1")) {
                        ExchangeWithClient.upload(socket);
                        break;
                    }
                    if (sm.equals("2")) {
                        ExchangeWithClient.setFile(socket);
                        break;
                    }
                    else {
                        ExchangeWithClient.allFiles(socket);
                        break;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] ar)
    {
        ServerSocket srvSocket = null;
        try {
            try {
                int num = 0; // Счётчик подключений
                // Подключение сокета к localhost
                InetAddress ia = InetAddress.getByName("localhost");
                srvSocket = new ServerSocket(port, 0, ia);

                System.out.println("Server started\n\n");
                System.out.println(srvSocket.getInetAddress()+"\n\n");
                try {
                    while(true) {
                        // ожидание подключения
                        Socket socket = srvSocket.accept();
                        System.err.println("Client accepted! His number " + num);
                        // Стартуем обработку клиента
                        // в отдельном потоке
                        new Server().setSocket(num++, socket);
                    }

                } catch (IOException ex) {
                    System.out.println("Can't accept client connection. "+ex);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (srvSocket != null) {
                    srvSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}