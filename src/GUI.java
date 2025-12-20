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

    static int totalTiles = 0;
    int correctSelections = 0;
    boolean first = true;
    int currentIndex = -1;
    static boolean buttonLock = false;
    private Client client;
    JLabel scoreText = new JLabel("<html>Player 1: 0 <br>Player 2: 0<html>");



    static ArrayList<tiles> cardList = new ArrayList<>();
    ArrayList<JButton> buttonList = new ArrayList<>();


    JPanel cards = new JPanel();

    public GUI(Client client) {

        this.client = client;

    }

    void initGame(int totalTiles) {
        int side = (int) Math.sqrt(totalTiles);
        gamePanel = new JPanel(new BorderLayout());
        sideBar = new JPanel(new BorderLayout());

        this.setResizable(false);
        cards.setLayout(new GridLayout(side, side));


        cards.setPreferredSize(new Dimension(450, 450)); // Square game board

        gamePanel.add(cards, BorderLayout.CENTER);
        gamePanel.add(sideBar, BorderLayout.EAST);


        sideBar.setPreferredSize(new Dimension(200, 0));


        sideBar.add(scoreText, BorderLayout.NORTH);
        scoreText.setFont(new Font("Arial", Font.PLAIN, 20));

        JButton settingsButton = new JButton("Settings");
        gamePanel.add(settingsButton, BorderLayout.SOUTH);
        settingsButton.addActionListener(e -> showSettingWindow());

        setContentPane(gamePanel);
        setTitle("Memory");

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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
                    Timer t = new Timer(1000, e -> {

                        //Sets both to blank again
                        buttonList.get(index).setText(null);
                        buttonList.get(currentIndex).setText(null);


                        //Sets it to first click again
                        first = true;
                        try {

                            //Sends the click
                            client.sendOb(new Flip(index, currentIndex, false, true));
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
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

    public static void setButtonSymbolText(JButton button, int index) {
        button.setText(cardList.get(index).getSymbol());
    }


    public void disable(int index, int currentIndex) {
        buttonList.get(index).setEnabled(false);
        buttonList.get(currentIndex).setEnabled(false);
    }

    public void showWinWindow(int scenario) {

        JFrame winWindow = new JFrame();
        JPanel buttons = new JPanel();
        JPanel text = new JPanel(new BorderLayout());
        winWindow.setResizable(false);
        JLabel startNew = new JLabel("", SwingConstants.CENTER);

        switch (scenario) {
            case 1 ->
                startNew.setText("YOU WON! Play again?");

        case 2 ->
                startNew.setText("You lost. Play again?");
         case 3 ->
                startNew.setText("Draw... Play again?");
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
        winWindow.setLocationRelativeTo(null);
        winWindow.setVisible(true);

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

    public void showSettingWindow() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel resetRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton reset = new JButton("Reset Game");
        reset.setPreferredSize(new Dimension(200, 40));
        resetRow.add(reset);

        JPanel difficultyRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton easy = new JButton("Easy");
        JButton medium = new JButton("Medium");
        JButton hard = new JButton("Hard");
        difficultyRow.add(easy);
        difficultyRow.add(medium);
        difficultyRow.add(hard);

        settingsPanel.add(resetRow);
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        settingsPanel.add(difficultyRow);

        setContentPane(settingsPanel);
        revalidate();
        repaint();

        Runnable applyDifficulty = () -> {
            cards.removeAll();
            buttonList.clear();
            correctSelections = 0;
            first = true;
            buttonLock = false;

            addButtons();
            setContentPane(gamePanel);
            revalidate();
            repaint();
        };

        switch (totalTiles) {
            case 16:
                easy.setEnabled(false);
                medium.setEnabled(true);
                hard.setEnabled(true);
                break;
            case 30:
                easy.setEnabled(true);
                medium.setEnabled(false);
                hard.setEnabled(true);
                break;
            case 64:
                easy.setEnabled(true);
                medium.setEnabled(true);
                hard.setEnabled(false);
                break;
        }

        reset.addActionListener(e -> applyDifficulty.run());

        easy.addActionListener(e -> {
            totalTiles = 16;
            applyDifficulty.run();
        });

        medium.addActionListener(e -> {
            totalTiles = 30;
            applyDifficulty.run();
        });

        hard.addActionListener(e -> {
            totalTiles = 64;
            applyDifficulty.run();
        });
    }

    public void setScoreText(int a, int b) {
        scoreText.setText("<html>Player 1: " + a + "<br>Player 2:" + b +"<html> ");
    }


    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public static void main() {
        GUI gui = new GUI(null);

        Client client2 = new Client();
        gui.setClient(client2);
        client2.setGUI(gui);
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
