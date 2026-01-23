import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public void ServerGUI() {

    JFrame serverWindow = new JFrame();
    serverWindow.setResizable(false);

    JButton easy = new JButton("EASY 16");
    JButton medium = new JButton("MEDIUM 36 ");
    JButton hard = new JButton("HARD 64");
    JPanel buttonPanel = new JPanel(new FlowLayout());
    serverWindow.add(buttonPanel);

    AtomicReference<Server> server = new AtomicReference<>();

    buttonPanel.add(easy);
    buttonPanel.add(medium);
    buttonPanel.add(hard);

    JPanel labelPanel = new JPanel(new BorderLayout());
    JLabel chooseDifficulty = new JLabel("Choose a difficulty!", SwingConstants.CENTER);
    labelPanel.add(chooseDifficulty);
    labelPanel.setBorder(new EmptyBorder(15, 15, 10, 15));

    JPanel startButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton startServer = new JButton("Start Server");
    JButton closeServer = new JButton("Close Server");
    startButtonPanel.add(startServer);
    startButtonPanel.setBorder(new EmptyBorder(0, 15, 15, 15));

    serverWindow.add(labelPanel, BorderLayout.NORTH);
    serverWindow.add(buttonPanel, BorderLayout.CENTER);
    serverWindow.add(startButtonPanel, BorderLayout.SOUTH);

    serverWindow.setSize(400, 200);
    serverWindow.setVisible(true);
    serverWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    easy.setEnabled(false);


    easy.addActionListener(e -> {
        easy.setEnabled(false);
        medium.setEnabled(true);
        hard.setEnabled(true);
        Server.totalTiles=16;

    });

    medium.addActionListener(e -> {
        easy.setEnabled(true);
        medium.setEnabled(false);
        hard.setEnabled(true);
        Server.totalTiles=36;

    });

    hard.addActionListener(e -> {
        easy.setEnabled(true);
        medium.setEnabled(true);
        hard.setEnabled(false);
        Server.totalTiles=64;

    });

    startServer.addActionListener(e -> {
        chooseDifficulty.setText("Server running...");
        startServer.setFocusable(false);
        startButtonPanel.remove(startServer);
        startButtonPanel.add(closeServer);
        startButtonPanel.repaint();
        startButtonPanel.revalidate();

        new Thread(() -> server.set(new Server())).start();

    });

    closeServer.addActionListener(e -> {
        Server s = server.get();
        if(s!=null) {
            try {
                server.get().shutdownSever();
            } catch (Exception ignored) {
                System.out.println("shutdown error");
            }
        }
        chooseDifficulty.setText("Choose a difficulty!");
        startButtonPanel.remove(closeServer);
        startButtonPanel.add(startServer);
    });
}

void main() {
    ServerGUI();
}
