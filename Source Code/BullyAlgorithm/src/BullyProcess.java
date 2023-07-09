import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class BullyProcess {
    String MessageSplitter = "_";
    private Date LastMessageDate;
    private Date ElectionStartedDate;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private long BullyProcessId;
    private int State; // 1 = Alive | 2 = Election | 3 = Coordinator

    public BullyProcess(Socket socket) {

        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.BullyProcessId = ProcessHandle.current().pid();
            State = 2;
            LastMessageDate = null;
            ElectionStartedDate = null;
        } catch (IOException ex) {
            ex.printStackTrace();
            TerminateEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    public void SendMessage() {
        try {
            bufferedWriter.write(String.valueOf(BullyProcessId));
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    if (!socket.isClosed()) {
                        try {

                            if (State == 3) {
                                var nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                                System.out.println(nowDate + " Process with Id _ " + String.valueOf(BullyProcessId) + " _ Coordinator _ 3 ");
                                bufferedWriter.write(nowDate + " Process with Id _ " + String.valueOf(BullyProcessId) + " _ Coordinator _ 3 ");
                                bufferedWriter.newLine();
                                bufferedWriter.flush();

                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }, 0, 2000);
        } catch (IOException ex) {
            TerminateEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void ListenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!socket.isClosed()) {
                    try {
                        String receivedMessage;
                        receivedMessage = bufferedReader.readLine();

                        var receivedMessageSegments = receivedMessage.split(MessageSplitter);
                        if (receivedMessageSegments.length == 4 && receivedMessageSegments[3] != null) {
                            int messageType = Integer.parseInt(receivedMessageSegments[3].trim());
                            long senderProcessId = Long.parseLong(receivedMessageSegments[1].trim());

                            if (messageType == 2 && senderProcessId < BullyProcessId) {
                                StartElection();
                            } else if (messageType == 2 && senderProcessId > BullyProcessId) {
                                State = 1;
                                if(LastMessageDate != null) LastMessageDate = null;
                            } else if (messageType == 3) {
                                State = 1;
                                LastMessageDate = new Date();
                                if(ElectionStartedDate != null) ElectionStartedDate = null;
                                var nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                System.out.println(nowDate + " Process with Id _ " + String.valueOf(BullyProcessId) + " _ ACK _ 1 ");
                            }
                        }

                    } catch (IOException ex) {
                        ex.printStackTrace();
                        TerminateEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();

    }

    public void ValidateExistenceOfCoordinator() {

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    if (State == 1 && LastMessageDate != null) {
                        var DateRightNow = new Date();
                        var timeDifference = DateRightNow.getTime() - LastMessageDate.getTime();
                        var seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference);
                        if (seconds >= 3) {
                            StartElection();
                        }
                    }
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }, 0, 1000);


    }


    public void StartElection() {
        try {
            if(LastMessageDate != null) LastMessageDate = null;
            State = 2;
            ElectionStartedDate = new Date();
            var nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            var message = nowDate + " Process with Id _ " + String.valueOf(BullyProcessId) + " _ ELECTION _ 2";

            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            System.out.println(message);

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    if (State == 2 && ElectionStartedDate != null) {

                        var DateRightNow = new Date();
                        var timeDifference = DateRightNow.getTime() - ElectionStartedDate.getTime();
                        var seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference);
                        if (seconds >= 2) {
                            State = 3;
                            if(ElectionStartedDate != null) ElectionStartedDate = null;
                        }
                    }
                }
            }, 0, 1000);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void TerminateEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1543);
        BullyProcess bullyProcess = new BullyProcess(socket);
        bullyProcess.ListenForMessage();
        bullyProcess.SendMessage();
        bullyProcess.StartElection();
        bullyProcess.ValidateExistenceOfCoordinator();

    }
}
