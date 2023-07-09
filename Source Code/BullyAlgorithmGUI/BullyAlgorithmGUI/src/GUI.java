import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GUI implements ActionListener {
    private class GuiOutputStream extends OutputStream {
        JTextArea textArea;

        public GuiOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int data) throws IOException {
            textArea.append(new String(new byte[] { (byte) data }));
        }
    }

    JButton addProcessButton;
    JButton killCoordinatorButton;
    public static ArrayList<Process> RunningProcesses = new ArrayList<Process>();
    public static Process ServerProcess = null;
    public GUI() {
        var frame = new JFrame();
        var panel = new JPanel();


        JTextArea textArea = new JTextArea(); // Output text area

        GUI.GuiOutputStream rawout = new GUI.GuiOutputStream(textArea);
        var textAreaPanel = new JScrollPane(textArea);

        frame.setSize(700, 700);
        frame.setVisible(true);

        // Set new stream for System.out
        System.setOut(new PrintStream(rawout, true));


        addProcessButton = new JButton("Create Process");
        killCoordinatorButton = new JButton("Kill Coordinator");

        addProcessButton.setBounds(0,0,1,1);
        killCoordinatorButton.setBounds(0,0,1,1);

        addProcessButton.addActionListener(this);
        killCoordinatorButton.addActionListener(this);

        panel.setLayout(new GridLayout(1,2));
        var buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2,1));

        buttonPanel.add(addProcessButton);
        buttonPanel.add(killCoordinatorButton);
        panel.add(textAreaPanel);
        panel.add(buttonPanel);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                TerminateAllProcesses();
            }
        });

        frame.setTitle("Bully Algorithm");
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {

       new GUI();
        RunSocketServer();
    }
    public static void RunSocketServer()
    {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "./OtherJars/SocketServer.jar");
        try {
            Process server = pb.start();
            ServerProcess = server;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void CreateProcess()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {

                ProcessBuilder pb = new ProcessBuilder("java", "-jar", "./OtherJars/BullyProcess.jar");
                try {
                    Process p = pb.start();
                    RunningProcesses.add(p);

                    BufferedReader is =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                    String line;
                    while ((line = is.readLine()) != null)
                        System.out.println(line);

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }).start();
    }

    public void TerminateAllProcesses()
    {
        for(int i = 0; i < RunningProcesses.size(); i++)
        {
            RunningProcesses.get(i).destroy();
        }
        RunningProcesses.clear();
        ServerProcess.destroy();
        System.exit(0);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == addProcessButton)
        {
            CreateProcess();
        }
        if(e.getSource() == killCoordinatorButton) {
            if (RunningProcesses.size() > 0) {
                Process currentCoordinator = Collections.max(RunningProcesses, Comparator.comparingLong(Process::pid));
                RunningProcesses.remove(currentCoordinator);

                var nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                System.out.println(nowDate + " Process with Id _ " + String.valueOf(currentCoordinator.pid()) + " _ TERMINATED _ 4 ");

                currentCoordinator.destroy();


            }
        }
    }
}