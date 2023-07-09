import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class BullyProcessHandler implements Runnable{
    public static ArrayList<BullyProcessHandler> BullyProcessHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private long bullyProcessId;

    public BullyProcessHandler(Socket socket)
    {
        try
        {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bullyProcessId = Long.parseLong(bufferedReader.readLine());
            BullyProcessHandlers.add(this);

            var nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            BroadcastMessage(nowDate + " New Process with Id _ "+ this.bullyProcessId+ " _ CREATED _ 0");
        }
        catch(IOException ex)
        {
            TerminateEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    @Override
    public void run() {
        String messageFromProcess;
        while(!socket.isClosed())
        {
            try
            {
                messageFromProcess = bufferedReader.readLine();
                BroadcastMessage(messageFromProcess);
            }
            catch(IOException ex)
            {
                TerminateEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void BroadcastMessage(String message)
    {
        for (BullyProcessHandler bullyProcessHandler : BullyProcessHandlers) {
            try
            {
                if(bullyProcessHandler.bullyProcessId != bullyProcessId)
                {
                    bullyProcessHandler.bufferedWriter.write(message);
                    bullyProcessHandler.bufferedWriter.newLine();
                    bullyProcessHandler.bufferedWriter.flush();
                }
            }
            catch(IOException ex)
            {
                TerminateEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void RemoveProcessHandler()
    {
        BullyProcessHandlers.remove(this);
        var nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(nowDate + " Process with Id _ "+ this.bullyProcessId+ " _ TERMINATED _ 4");
        BroadcastMessage(nowDate + " Process with Id _ "+ this.bullyProcessId+ " _ TERMINATED _ 4");
    }

    public void TerminateEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter)
    {
        RemoveProcessHandler();
        try {
            if(bufferedReader != null)  bufferedReader.close();
            if(bufferedWriter != null)  bufferedWriter.close();
            if(socket != null)  socket.close();
        }
        catch( IOException ex)
        {
            ex.printStackTrace();
        }
    }


}
