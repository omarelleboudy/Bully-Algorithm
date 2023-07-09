import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

    private ServerSocket serverSocket;

    public SocketServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void StartServer()
    {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New Process has been created.");
                BullyProcessHandler processHandler = new BullyProcessHandler(socket);

                Thread thread = new Thread(processHandler);
                thread.start();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public void CloseServerSocket()
    {
        try
        {
            if(serverSocket != null)
            {
                serverSocket.close();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1543);
        SocketServer server = new SocketServer(serverSocket);
        server.StartServer();
    }
}
