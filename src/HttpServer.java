import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private static final int DEFAULT_PORT = 9998;

    public static void main(String[] args) {

        int port = DEFAULT_PORT;

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port: "
                    + serverSocket.getLocalPort() + "\n");
        } catch (IOException e) {
            System.out.println("Port " + port + " is blocked.");
            System.exit(-1);
        }

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Session session = new Session(clientSocket);
                new Thread(session).start();
            } catch (IOException e) {
                System.out.println("Failed to establish connection.");
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        }
    }

}
