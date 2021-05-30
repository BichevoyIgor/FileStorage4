import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.printf("Client connected ip: %S %n", socket.getLocalSocketAddress());
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            while (true) {
                String command = in.readUTF();
                if ("COMMAND_EXIT".equals(command)) {
                    System.out.printf("Client disconnected ip: %S %n", socket.getLocalSocketAddress());
                    break;
                } else if (command.startsWith("COMMAND_UPLOAD ")) {
                    String[] pathCommand = command.split(" ", 2);
                    System.out.println("Получил команду качать с клиента и путь: " + pathCommand[1]);
                    uploading(out, in);
                } else if (command.startsWith("COMMAND_DOWNLOAD ")) {
                    String[] pathCommand = command.split(" ", 2);
                    System.out.println("Получил команду отправить на клиента и путь: " + pathCommand[1]);
                    sendMessage("лови файл с сервера", out, in);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploading(DataOutputStream out, DataInputStream in) throws IOException {
        try {
            File file = new File("server/" + in.readUTF()); // read file name
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file);
            long size = in.readLong();
            byte[] buffer = new byte[8 * 1024];
            for (int i = 0; i < (size + (8 * 1024 - 1)) / (8 * 1024); i++) {
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();
            out.writeUTF("OK");
        } catch (Exception e) {
            out.writeUTF("WRONG");
        }
    }



    public void sendMessage(String str, DataOutputStream out, DataInputStream in) {
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

