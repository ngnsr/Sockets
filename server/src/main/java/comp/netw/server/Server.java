package comp.netw.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Random;

public class Server {

    final static String WHO_MSG = "Author: Risenhin Vladyslav\nVariant: 28\nDescription: Prime numbers filter";

    private static final int PORT = 4444;

    private static final String LOG_PATH = System.getProperty("user.dir") + "/logs";
    private static final String SERVER_LOG_PATH = LOG_PATH + "/server.log";
    private static final Random random = new Random();

    private static ServerSocket serverSocket;
    private static DateTimeFormatter date;
    private static PrintWriter logger;

    final static String NUMBER_IS_PRIME_MSG = "is a prime";
    final static String NUMBER_IN_NOT_PRIME_MSG = "is not a prime";

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
        initLogger();

        initServerSocket();

        while (true) {
            try {
                new ClientHandler(serverSocket.accept()).start();
            } catch (final IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
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

    private static void initLogger() {
        date = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        try {
            final File file = new File(SERVER_LOG_PATH);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            logger = new PrintWriter(new FileWriter(file, true), true);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static void initServerSocket() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (final IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static class ClientHandler extends Thread {

        private final Socket clientSocket;
        private InetAddress LOCAL_ADDRESS;
        private String HOST_ADDRESS;

        // stat
        private int count = 0;
        private int primeCount = 0;
        private long min = Integer.MAX_VALUE;
        private long max = Integer.MIN_VALUE;

        public ClientHandler(final Socket clientSocket) {
            this.clientSocket = clientSocket;
            HOST_ADDRESS = clientSocket.getInetAddress().getHostAddress();
            LOCAL_ADDRESS = clientSocket.getLocalAddress();
        }

        @Override
        public void run() {
            String request = "";
            try (
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {
                do {
                    try {
                        request = in.readLine();
                    } catch (NoSuchElementException e) {
                        return;
                    }
                    executeRuquest(request.trim(), out, in);
                } while (request != null);

            } catch (final Exception e) { }
        }

        private void executeRuquest(final String request, final PrintWriter out, final BufferedReader in) {
            int requestLength = -1;
            String command = "";
            long number = -1;

            if (request.endsWith("exit")) {
                out.printf("Total numbers: %s\nPrime numbers: %s\nMax number: %s\nMin number: %s\n", count, primeCount,
                        count > 0 ? max : "-", count > 0 ? min : "-");
            } else if (request.matches("\\d+\\s+[a-zA-Z]+(\\s+[+-]?\\d+)?")) {
                final String[] args = request.split("\\s+");

                try {
                    requestLength = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    printWrongRequestMsg(out);
                    return;
                }

                command = args[1];

                if (args.length == 2) {
                    if (command.equalsIgnoreCase(Commands.Who.name())) {
                        executeCommandWho(out);
                    } else if (command.equalsIgnoreCase(Commands.GenerateAndCheck.name())) {
                        executeCommandGenerateAndCheck(out);
                    }
                } else if (args.length == 3 && command.equalsIgnoreCase(Commands.Check.name())) {
                    try {
                        number = Long.parseLong(args[2]);
                    } catch (NumberFormatException e) {
                        printWrongRequestMsg(out);
                        return;
                    }
                    executeCommandCheck(out, number);
                }else{
                    printWrongRequestMsg(out);
                }
            } else {
                printWrongRequestMsg(out);
            }
        }

        private void printWrongRequestMsg(final PrintWriter out){
            out.println("Wrong request!");
        }

        private void executeCommandWho(final PrintWriter out) {
            out.println(WHO_MSG);
            log(Commands.Who.name());
        }

        private void executeCommandGenerateAndCheck(final PrintWriter out) {
            StringBuilder sb = new StringBuilder(1300);
            for (int i = 0; i < 50; i++) {
                long n = Math.abs(Server.random.nextInt());
                boolean isPrime = isPrime(n);
                updateStat(n, isPrime);
                sb.append(String.format("%s %s\n", n, isPrime ? NUMBER_IS_PRIME_MSG : NUMBER_IN_NOT_PRIME_MSG));
            }
            out.printf(sb.toString());
            log(Commands.GenerateAndCheck.name());
        }

        private void executeCommandCheck(final PrintWriter out, final long number) {
            final boolean isPrime = isPrime(number);

            out.printf("%s %s\n", number, isPrime ? NUMBER_IS_PRIME_MSG : NUMBER_IN_NOT_PRIME_MSG);

            updateStat(number, isPrime);
            log(Commands.Check.name());
        }

        private void updateStat(long number, boolean isPrime) {
            count++;
            if (number > max) {
                max = number;
            }
            if (number < min) {
                min = number;
            }
            if (isPrime) {
                primeCount++;
            }
        }

        private boolean isPrime(final long n) {
            if (n <= 1) {
                return false;
            }

            if (n % 2 == 0) {
                return (n == 2);
            }

            long upperBound = (long)Math.sqrt(n) + 1;
            for (long i = 3; i < upperBound; i+=2) {
                if (n % i == 0)
                    return false;
            }
            return true;
        }

        private void log(String command) {
            String logMessage = String.format("[ %s | %s | %s ] : %s\n", date.format(LocalDateTime.now()), LOCAL_ADDRESS, HOST_ADDRESS, command);
            logger.printf(logMessage);
            System.out.print(logMessage);
        }
    }
}
