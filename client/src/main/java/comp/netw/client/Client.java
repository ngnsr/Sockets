package comp.netw.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Client {
    private static final Scanner consoleScanner = new Scanner(System.in);

    private static Socket client;
    private static PrintWriter out;
    private static BufferedReader in;

    private static PrintWriter logger;
    private static PrintWriter primesLogger;
    private static DateTimeFormatter date;

    private static InetAddress LOCAL_ADDRESS;
    private static String HOST_ADDRESS;

    private static final int CONNECT_TIMEOUT = 3000;

    // if not exist create
    private static final String LOG_PATH = System.getProperty("user.dir") + "/logs";
    private static final String CLIENT_LOG_PATH = LOG_PATH + "/client.log";
    private static final String PRIMES_LOG_PATH = LOG_PATH + "/primes.log";

    final static String NUMBER_IS_PRIME_MSG = "is a prime";
    final static String MODE_MSG = "Choose mode :\n1 - request to check whether the number is prime\n2 - generate and check 50 random numbers\n3 - Who\n- exit\n > ";

    // command(command length)
    enum Commands {
        Check(5),
        GenerateAndCheck(16),
        Who(3);

        private int length;

        private Commands(int length) {
            this.length = length;
        }

        public int getLength() {
            return length;
        }
    }

    public static void main(final String[] args) {
        initLoggers();

        initAndStartSocketConnection();

        while (true) {
            final int mode = (int) chooseMode();
            if (mode == -1 ) {
                // request for stat
                out.println("6 exit");
                printResponse();
                break;
            }
            makeRequest(mode);
        }

        closeConnection();
    }

    public static void initLoggers() {
        ensureLogsDirExists();

        logger = initLogger(PRIMES_LOG_PATH);

        primesLogger = initLogger(CLIENT_LOG_PATH);
        // need for client logger
        date = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    }

    public static void ensureLogsDirExists(){
        final File logFolder = new File(LOG_PATH);
        if(!logFolder.exists()){
            try {
                logFolder.mkdir();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private static PrintWriter initLogger(final String path) {
        try {
            final File file = new File(path);
            ensureFileExists(file);
            return new PrintWriter(new FileWriter(file, true), true);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void ensureFileExists(final File file){
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void makeRequest(final int mode) {
        if (mode == 1) {
            requestCheckIfPrime();
        } else if (mode == 2) {
            requestGenerateAndCheck();
        } else if (mode == 3) {
            requestWho();
        }
    }

    private static void requestWho() {
        System.out.println("Who:");
        out.println(Commands.Who.getLength() + 1 + " " + Commands.Who.name());

        log(Commands.Who.name());
        printResponse();
    }

    private static void requestGenerateAndCheck() {
        System.out.println("Generate 50 numbers and check if they are prime:");
        out.println(Commands.GenerateAndCheck.getLength() + 1 + " " + Commands.GenerateAndCheck.name());
        log(Commands.GenerateAndCheck.name());
        String inpString;

        try {
            do {
                inpString = in.readLine();
                sb.append(inpString + "\n");
                logPrimeNumber(inpString);
            } while (in.ready());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(sb.toString());
        sb.setLength(0);
   }

    private static void requestCheckIfPrime() {
        System.out.println("Check: ");
        System.out.println("type \'exit\' to exit");
        final String message = "Enter number: \n > ";

        String stringNumber;
        while (true) {
            System.out.print(message);
            stringNumber = consoleScanner.nextLine();
            System.out.println();

            if (stringNumber.equals("exit")) {
                break;
            }

            if (isNumberValid(stringNumber)) {
                // out.printf("%s %s %s\n", Commands.Check.getLength(), Commands.Check.name(), stringNumber);
                out.println(Commands.Check.getLength() + 2 + stringNumber.length() + " " + Commands.Check.name() + " " + stringNumber);
                log(Commands.Check.name());
                try {
                    String response = in.readLine();
                    System.out.println(response);
                    logPrimeNumber(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println();
            }
        }
    }

    private static void logPrimeNumber(String in) {
        if (in.matches("^\\d+ .*") && in.endsWith(NUMBER_IS_PRIME_MSG)) {
            primesLogger.println(in.split(" ")[0]);
        }
    }

    private static boolean isNumberValid(final String stringNumber) {
        boolean valid = true;
        try {
            Long.parseLong(stringNumber);
        } catch (final NumberFormatException e) {
            valid = false;
        }
        return valid;
    }

    static StringBuilder sb = new StringBuilder(1500);
    private static void printResponse() {
        try {
            do {
                sb.append(in.readLine() + "\n");
            } while (in.ready());
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(sb.toString());
        sb.setLength(0);
    }

    private static int chooseMode() {
        String in = "";
        int n = -1;
        while(n < 1 || n > 3){
            System.out.print(MODE_MSG);
            in = consoleScanner.nextLine();
            System.out.println();
            if(in.equals("exit")){
                break;
            }
            try {
                n = Integer.parseInt(in);
            } catch (final Exception e) {
                System.out.println("Try again!");
            }
        } 
        return n;
    }

    private static void initAndStartSocketConnection() {
        boolean conected = false;
        client = new Socket();
        while (!conected) {
            try {
                final String host = readConsoleHost();
                final int port = (int) readConsoleNumber("Enter port number.\n > ", 1025, 65_535);
                final SocketAddress socketAddress = new InetSocketAddress(host, port);
                client.connect(socketAddress, CONNECT_TIMEOUT);
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                HOST_ADDRESS = client.getInetAddress().getHostAddress();
                LOCAL_ADDRESS = client.getLocalAddress();
                conected = true;
            } catch (final IOException e) {
                conected = false;
                System.err.println(e.getMessage());
            }
        }
    }

    private static void closeConnection() {
        try {
            consoleScanner.close();
            out.close();
            client.close();
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static long readConsoleNumber(final String message, final long MIN_VALUE, final long MAX_VALUE) {
        long n = -1;
        do {
            System.out.print(message);
            try {
                n = Long.parseLong(consoleScanner.nextLine());
            } catch (final Exception e) {
                System.out.println("Try again!");
            }
            System.out.println();
        } while (n < MIN_VALUE || n > MAX_VALUE);

        return n;
    }

    private static String readConsoleHost() {
        final String message = "Enter IPV4 host: ";
        String host = "";

        do {
            System.out.print(message);
            try {
                host = consoleScanner.nextLine().trim();
            } catch (final Exception e) {
                System.out.println("Try again!");
            }
            System.out.println();
        } while (!host.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") && !host.equals("localhost"));

        return host;
    }

    private static void log(String command) {
        logger.printf("[ %s | %s | %s ] : %s\n", date.format(LocalDateTime.now()), LOCAL_ADDRESS, HOST_ADDRESS,
                command);
    }
}
