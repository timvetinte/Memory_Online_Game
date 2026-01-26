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
        Server.totalTiles = 16;

    });

    medium.addActionListener(e -> {
        easy.setEnabled(true);
        medium.setEnabled(false);
        hard.setEnabled(true);
        Server.totalTiles = 36;

    });

    hard.addActionListener(e -> {
        easy.setEnabled(true);
        medium.setEnabled(true);
        hard.setEnabled(false);
        Server.totalTiles = 64;

    });

    startServer.addActionListener(e -> {

        switch (Server.totalTiles) {
            case 16 -> chooseDifficulty.setText("Server running... Easy difficulty");
            case 36 -> chooseDifficulty.setText("Server running... Medium difficulty");
            case 64 -> chooseDifficulty.setText("Server running... Hard difficulty");
        }

        startServer.setFocusable(false);
        easy.setEnabled(false);
        medium.setEnabled(false);
        hard.setEnabled(false);
        startButtonPanel.remove(startServer);
        startButtonPanel.add(closeServer);
        startButtonPanel.repaint();
        startButtonPanel.revalidate();

        new Thread(() -> server.set(new Server())).start();

    });

    closeServer.addActionListener(e -> {

        try {
            Server s = server.get();
            if (s != null) {
                s.shutdownSever();
                System.out.println("Shutdown");
            } else {
                System.out.println("Server is null!");
            }
        } catch (Exception ex) {
            System.out.println("shutdown error: " + ex);
            ex.printStackTrace();
        }

        chooseDifficulty.setText("Choose a difficulty!");
        startButtonPanel.remove(closeServer);
        startButtonPanel.add(startServer);

        easy.setEnabled(true);
        medium.setEnabled(true);
        hard.setEnabled(true);

        switch (Server.totalTiles) {
            case 16 -> easy.setEnabled(false);
            case 36 -> medium.setEnabled(false);
            case 64 -> hard.setEnabled(false);
        }
        startButtonPanel.repaint();
        startButtonPanel.revalidate();
    });
}

void main() {
    ServerGUI();
}
