import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;


public class GUI extends JFrame implements ActionListener {

    JPanel gamePanel;
    JPanel sideBar;

    private String userId = null;
    String p2Userid = null;

    int totalTiles = 0;
    int correctSelections = 0;
    boolean first = true;
    int currentIndex = -1;
    static boolean buttonLock = false;
    private Client client;
    JLabel scoreText = new JLabel("<html> P1: 0 points<br> P2: 0 points<html> ");
    JTextArea chat = new JTextArea();




    ArrayList<tiles> cardList = new ArrayList<>();
    ArrayList<JButton> buttonList = new ArrayList<>();


    JPanel cards = new JPanel();

    public GUI(Client client) {

        this.client = client;

    }

    void initGame(int totalTiles) {
        int side = (int) Math.sqrt(totalTiles);
        gamePanel = new JPanel(new BorderLayout());
        sideBar = new JPanel(new BorderLayout());

        chat.setEditable(false);
        JTextField enterMessage = new JTextField();

        this.setResizable(false);
        cards.setLayout(new GridLayout(side, side));


        cards.setPreferredSize(new Dimension(450, 450)); // Square game board

        gamePanel.add(cards, BorderLayout.CENTER);
        gamePanel.add(sideBar, BorderLayout.EAST);


        sideBar.setPreferredSize(new Dimension(300, 0));


        sideBar.add(scoreText, BorderLayout.NORTH);
        sideBar.add(chat, BorderLayout.CENTER);
        sideBar.add(enterMessage, BorderLayout.SOUTH);


        scoreText.setFont(new Font("Arial", Font.PLAIN, 20));

        JButton settingsButton = new JButton("Disconnect");
        gamePanel.add(settingsButton, BorderLayout.SOUTH);
        enterMessage.addActionListener(e -> {
            String message = enterMessage.getText();
            try {
                sendMessage(userId, message);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            enterMessage.setText(null);
        });

        setContentPane(gamePanel);
        setTitle("Memory");

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void setTileAmount(int tiles){
        totalTiles = tiles;
    }

    public void setCardList(ArrayList <tiles> list){
        cardList = list;
    }

    public void addButtons() {
        for (int i = 0; i < totalTiles; i++) {
            JButton jb = new JButton();
            buttonList.add(jb);
            cards.add(jb);
            int index = i;
            jb.setFont(new Font("arial", Font.PLAIN, 35));
            jb.addActionListener(e -> {
                try {
                    clickButton(jb, index);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    public void clickButton(JButton button, int index) throws InterruptedException, IOException {
        if (!buttonLock) {
            if (first) {
                //"Reveals" the text of the first button
                setButtonSymbolText(button, index);

                //Updates the last button clicked to this button
                currentIndex = index;

                //Sends an object for it to be flipped for the other player
                client.sendOb(new Select(index));

                //Sets so next click is not the first
                first = false;

                //Checks that player doesn't click on the same button twice
            } else if (currentIndex != index) {

                //Checks that player makes a correct selection
                if (cardList.get(currentIndex).getSymbol().equals(cardList.get(index).getSymbol())) {

                    //"Reveals" the text or symbol of the second button
                    setButtonSymbolText(button, index);

                    //Disables both buttons since they are correct
                    buttonList.get(index).setEnabled(false);
                    buttonList.get(currentIndex).setEnabled(false);

                    //Sets so it's time for a new first click
                    first = true;

                    //Updates the score
                    //NEEDS TO BE UPDATED SO IT IS AN OBJECT BEING SENT TO THE SERVER
                    correctSelections++;

                    //Sends the click or flip, doesn't update the other players to be disabled
                    //NEEDS TO BE FIXED
                    client.sendOb(new Flip(index, currentIndex, true, false));


                    //Player doesnt make correct selection
                } else {

                    //Reveals the text or symbol of click
                    setButtonSymbolText(button, index);
                    //Locks player out of making any further moves for 850 ms
                    buttonLock = true;
                    try {

                        //Sends the click
                        client.sendOb(new Flip(index, currentIndex, false, true));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    Timer t = new Timer(1000, e -> {

                        //Sets both to blank again
                        buttonList.get(index).setText(null);
                        buttonList.get(currentIndex).setText(null);


                        //Sets it to first click again
                        first = true;

                        //Removes the lock
                        buttonLock = false;
                    });
                    t.setRepeats(false);
                    t.start();
                }
            }
        }
    }

    public void flipTile(JButton button, int index, boolean reveal) {
        if (reveal) {
            setButtonSymbolText(button, index);
        } else {
            button.setText(null);
        }
    }

    public void setButtonSymbolText(JButton button, int index) {
        button.setText(cardList.get(index).getSymbol());
    }


    public void disable(int index, int currentIndex) {
        buttonList.get(index).setEnabled(false);
        buttonList.get(currentIndex).setEnabled(false);
    }

    public void enterUser() {
        JFrame userWindow = new JFrame();
        userWindow.setResizable(false);

        JPanel labelPanel = new JPanel(new BorderLayout());
        JLabel enterName = new JLabel("Enter your username", SwingConstants.CENTER);
        labelPanel.add(enterName);
        labelPanel.setBorder(new EmptyBorder(15, 15, 10, 15));

        JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JTextField userDialogue = new JTextField(20);
        textFieldPanel.add(userDialogue);
        textFieldPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton connect = new JButton("CONNECT");
        connect.setEnabled(false);
        buttonPanel.add(connect);
        buttonPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        userWindow.add(labelPanel, BorderLayout.NORTH);
        userWindow.add(textFieldPanel, BorderLayout.CENTER);
        userWindow.add(buttonPanel, BorderLayout.SOUTH);

        userWindow.setSize(400, 200);
        userWindow.setVisible(true);

        userDialogue.addActionListener(e -> {
            String userName = userDialogue.getText();
            if (userName.length() > 15) {
                enterName.setText("Username too long. Maximum 15 characters.");
                connect.setEnabled(false);
            } else if (userName.length() < 3) {
                enterName.setText("Username too short. Minimum 3 characters.");
                connect.setEnabled(false);
            } else {
                enterName.setText("Username set to: " + userName);
                userId = userName;
                connect.setEnabled(true);
            }
        });
        connect.addActionListener(e -> {
            connect.setEnabled(false);
            enterName.setText("Looking for other player.");
            enterName.setFocusable(false);
            connect.setFocusable(false);
            revalidate();
            repaint();

            SwingUtilities.invokeLater(() -> {
                client.connect();

                try {
                    client.sendOb(userId);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                userWindow.dispose();
            });
        });
    }



        public void showWinWindow(int scenario) {

        JFrame winWindow = new JFrame();
        JPanel buttons = new JPanel();
        JPanel text = new JPanel(new BorderLayout());
        winWindow.setResizable(false);
        JLabel startNew = new JLabel("", SwingConstants.CENTER);

        switch (scenario) {
            case 1 -> startNew.setText("YOU WON! Play again?");

            case 2 -> startNew.setText("You lost. Play again?");
            case 3 -> startNew.setText("Draw... Play again?");
        }

        JButton positive = new JButton("Yes");
        JButton negative = new JButton("No");
        winWindow.add(text, BorderLayout.NORTH);
        winWindow.add(buttons);
        text.add(startNew);
        buttons.add(positive);
        buttons.add(negative);
        buttons.setBorder(new EmptyBorder(15, 15, 15, 15));
        text.setBorder(new EmptyBorder(15, 5, 0, 5));
        winWindow.pack();
        winWindow.setVisible(true);
        winWindow.setLocationRelativeTo(this);

        positive.addActionListener(e -> {
            try {
                client.sendOb(1);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            winWindow.dispose();
        });
        negative.addActionListener(e ->
        {
            try {
                client.sendOb(0);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            winWindow.dispose();

        });
    }

    public void resetGame() {
        cards.removeAll();
        buttonList.clear();
        correctSelections = 0;
        first = true;
        currentIndex = -1;
        buttonLock = false;


        int side = (int) Math.sqrt(totalTiles);
        cards.setLayout(new GridLayout(side, side));


        cards.repaint();
        cards.revalidate();
    }

    public void setScoreText(int a, int b) {
        scoreText.setText("<html>" + userId + ": " + a + " points<br>" + p2Userid + ": " + b + " points<html> ");
    }

    public void sendMessage(String userName, String chatMessage) throws IOException {
        String sendsThis = userName + ": " + chatMessage + "\n";
        chat.append(sendsThis);
        client.sendChatMessage(new Message(sendsThis));
    }


    @Override
    public void actionPerformed(ActionEvent e) {
    }

    static void main() {
        Client client2 = new Client();
        GUI gui = new GUI(null);
        client2.setGUI(gui);
        gui.setClient(client2);
        gui.enterUser();
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
