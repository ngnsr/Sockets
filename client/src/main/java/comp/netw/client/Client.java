package comp.netw.client;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

//TODO логер клієнт + сервер
//TODO запис простих чисел у файл.

public class Client {
    private static final Scanner scanner = new Scanner(System.in);
    private static Socket client;
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(final String[] args) {
        String host = readConsoleHost();
        int port = readConsoleNumber("Enter port number.\n > ");

        startConnection(host, port);

        int mode = chooseMode();

        executeMode(mode);

        closeConnection();
    }

    private static void executeMode(int mode){
        switch(mode){
            case 1: requestCheckIfPrime();
            break;
            case 2: requestGenerateAndCheck();
            break;
            case 3: requestWho();
        }
    }

    private static void requestWho() {
        String command = "Who";
        int commandLength = command.length();
        out.print(commandLength);
        out.println(command);

        printResponse();
        // logger
    }

    private static void requestGenerateAndCheck() {
        String command = "GenerateAndCheck";
        int commandLength = command.length();
        out.print(commandLength);
        out.println(command);
        printResponse();
        // write prime to file.
    }

    private static void requestCheckIfPrime() {
        String command = "Check";
        int commandLength = command.length();

        final String message = "Enter number: \n > ";

        String line;
        boolean open = true;
        while (open) {
            System.out.println(message);
            line = scanner.nextLine();
            if (line.equals("exit")) {
                break;
            }
            if(line.matches("[+]?\\d+")){
                out.print(commandLength);
                out.print(command);
                out.println(Integer.parseInt(line));
                // write to file.
                open = printResponse();
            }
        }
    }

    private static boolean printResponse(){
        StringBuilder sb = new StringBuilder(260);
        String line = "";
        int testConnection = 0;
        
        try {
            while((line = in.readLine()) != null){
                sb.append(line);
                sb.append("\n");
            }
            System.out.println(sb);
            testConnection = in.read();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return testConnection >= 0 ? true : false;
    }

    private static int chooseMode() {
        String message = "Choose mode number:\n1 - request to check whether the number is prime\n2 - generate and check 50 random numbers\n3 - Who\n > ";
        int mode;

        do {
            mode = readConsoleNumber(message);
        } while (mode >=1 && mode <= 3);
        return mode;
    }

    private static void startConnection(final String host, final int port) {
        try {
            client = new Socket(host, port);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void closeConnection() {
        try {
            scanner.close();
            out.close();
            client.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static int readConsoleNumber(String message) {
        int n = -1;
        do {
            System.out.print(message);
            try {
                n = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Try again!");
            }
            System.out.println();
        } while (n < 0);

        return n;
    }

    private static String readConsoleHost() {
        String host = "";
        String message = "Enter IPV4 host: ";

        do {
            System.out.print(message);
            try {
                host = scanner.nextLine().trim();
            } catch (Exception e) {
                System.out.println("Try again!");
            }
            System.out.println();
        } while (!host.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") || !host.equals("localhost"));

        return host;
    }
}
