package comp.netw.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client{
    public static void main(String[] args){
        Socket client;
        BufferedOutputStream os;
        BufferedInputStream is;

        Scanner scanner = new Scanner(System.in);

        try {
            client = new Socket("localhost", 4444);
            os = new BufferedOutputStream(client.getOutputStream());
            is = new BufferedInputStream(client.getInputStream());
            String line;
            byte[] data;
            while(true){
                line = scanner.nextLine();
                if(line.equals("exit")){
                    break;
                }
                data = line.getBytes(StandardCharsets.UTF_8);
                os.write(data);
                os.flush();

                line = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println(line);
            }

            scanner.close();
            os.close();
            client.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
