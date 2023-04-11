package comp.netw.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server {

    public static void main(String[] args) {
        ServerSocket serverSocket;
        Socket socket;
        BufferedInputStream is;
        BufferedOutputStream os;

        try {
            serverSocket = new ServerSocket(4444);
            socket = serverSocket.accept();
            String line;
            while (true) {
                is = new BufferedInputStream(socket.getInputStream());
                os = new BufferedOutputStream(socket.getOutputStream());
                line = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if(line.equals("exit") || line.isBlank()){
                    break;
                }
                os.write(line.getBytes(StandardCharsets.UTF_8));
            }
            is.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
