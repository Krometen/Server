import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

class ExchangeWithClient{

    //файловая система сервера
    //  private static String path = "C:\\YourPath";
    private static String path = "C:\\Users\\pacst\\Desktop\\all\\proj\\Server_folder";

    static void allFiles(Socket socket) throws IOException {
        System.out.println("allFiles");
        System.out.println("===================Start===================");
        File folder = new File(path);

        final String[] extension = {""/*No filter by extension*/};
        String[] files = folder.list((folder1, name) -> {
                    for(String ignored : extension)
                        return true;
                    return false;
                }
        );
        String all = Arrays.toString(files);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(all);
        System.out.println(all);

        System.out.println("===================End===================");
    }

    static void upload(Socket socket) throws IOException {
        System.out.println("User uploads file ");
        System.out.println("===================Start===================");

        //если клиент в стоке пути решил выйти из аплода - сервер тоже выходит
        try {
            DataInputStream exit = new DataInputStream(socket.getInputStream());
            if (exit.readBoolean()) {
                System.out.println("The client returned to menu ");
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //если клиент в стоке имени решил выйти из аплода - сервер тоже выходит
        DataInputStream exit1 = new DataInputStream(socket.getInputStream());
        try {
            if (exit1.readBoolean()) {
                System.out.println("The client returned to menu ");
                return;
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        //апрув от клиента
        String approve;
        try {
            DataInputStream inpA = new DataInputStream(socket.getInputStream());
            approve = inpA.readUTF();
        } catch (Exception e) {
            System.out.println("Something is wrong with the approve: " + e);
            return;
        }

        //метод приема файла от клиента
        if (approve.equals("yes")) {
            System.out.println("User approved data transfer ");

            OutputStream outF;
            InputStream inpF = socket.getInputStream();

            String name;
            File filePath;
            try {
                DataInputStream inpN = new DataInputStream(socket.getInputStream());
                // Ожидание сообщения от клиента
                name = inpN.readUTF();
                System.out.println("File name \"" + name + "\"");

                //путь куда попадет файл клиента
                filePath = new File(path);
                outF = new FileOutputStream(filePath + "\\" + name);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            //принимаем от клиента размер файла
            DataInputStream inpL = new DataInputStream(socket.getInputStream());
            long lenghtF = inpL.readLong();

            //принятие и запись файла в фс сервера
            byte[] bytes = new byte[(int) lenghtF];
            int count;
            assert inpF != null;
            ArrayList<Integer> arr = new ArrayList<>();
            while((count = inpF.read(bytes)) > 0) {
                outF.write(bytes, 0, count);
                arr.add(count);
                int sum = 0;
                for(int d : arr) sum += d;
                if(sum >= lenghtF){
                    break;
                }
            }
            outF.close();

            System.out.println("File in the system. Path: " + filePath + "\\" + name);
            //отклик клиенту
            String mess = "Server: File in the system \"" + name + "\"\n";
            DataOutputStream outMess = new DataOutputStream(socket.getOutputStream());
            outMess.writeUTF(mess);

            System.out.println("====================End====================");
        }else{
            System.out.println("Not approved");
            ExchangeWithClient.upload(socket);
        }
    }


    static void setFile(Socket socket) throws IOException {
        System.out.println("Set File");
        System.out.println("===================Start===================");

        //если клиент в стоке имени решил выйти из аплода - сервер тоже выходит
        try {
            DataInputStream exit = new DataInputStream(socket.getInputStream());
            if (exit.readBoolean()) {
                System.out.println("The client returned to menu ");
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        DataInputStream inpN = new DataInputStream(socket.getInputStream());
        String name = inpN.readUTF();

        //объявляем путь
        File file = new File(path + "\\" + name);
        System.out.println("File sent to client: "+file);

        InputStream inpF;

        DataOutputStream error = new DataOutputStream(socket.getOutputStream());
        OutputStream outF;
        outF = socket.getOutputStream();

        try{
            inpF = new FileInputStream(file);
        } catch (Exception e) {
            System.out.println("\nInvalid file path: " + e + "\n");
            //если путь неверный - запускаем выгрузку заново
            error.writeBoolean(true);
            if (socket.isConnected()) {
                ExchangeWithClient.setFile(socket);
                return;
            }
            return;
        }
        //если нет исключений - даем клинту знать, что путь настоящий и сервер может продолжить эту сессию выгрузки
        error.writeBoolean(false);

        //если клиент в стоке пути решил выйти из аплода - сервер тоже выходит
        try {
            DataInputStream exit1 = new DataInputStream(socket.getInputStream());
            if (exit1.readBoolean()) {
                System.out.println("The client returned to menu ");
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //отправляем размер файла серваку
        long lenghtF = file.length();
        DataOutputStream outL = new DataOutputStream(socket.getOutputStream());
        outL.writeLong(lenghtF);

        //передаем байты клиенту
        byte[] bytes = new byte[(int) lenghtF];
        while (inpF.read(bytes)>0) {
            outF.write(bytes);
        }
        System.out.println("Success");
        System.out.println("====================End====================");
    }
}