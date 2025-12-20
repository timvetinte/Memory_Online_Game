
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class Client {

    String hostname = "127.0.0.1";
    int port = 5432;
    int chatPort = 5433;

    private GUI gui;

    private int p1Score;
    private int p2Score;

    private ObjectOutputStream gameOut;
    private ObjectOutputStream chatOut;

    private ObjectInputStream gameIn;
    private ObjectInputStream chatIn;


    private int totalTiles = 0;
    private boolean firstGame = true;

    public Client() {
        try {
            Socket gameSocket = new Socket(hostname, port);
            Socket chatSocket = new Socket(hostname, chatPort);

            this.gameOut = new ObjectOutputStream(gameSocket.getOutputStream());
            this.chatOut = new ObjectOutputStream(chatSocket.getOutputStream());


            this.gameIn = new ObjectInputStream(gameSocket.getInputStream());
            this.chatIn = new ObjectInputStream(chatSocket.getInputStream());

            new Thread(() -> {
                try {
                    listenLoop(gameOut, gameIn);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            new Thread(() -> {
                try {
                    chatLoop(chatOut, chatIn);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).start();


        } catch (IOException e) {
            System.out.println("GAME CLOSING...");
        }
    }

    public void listenLoop(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        while (true) {
            Object msg = in.readObject();

            if (msg instanceof Integer tiles) {
                this.totalTiles = tiles;
                GUI.totalTiles = tiles;

                if (firstGame) {

                    gui.initGame(tiles);
                    firstGame = false;
                }
            }
            if (msg instanceof String) {
                System.out.println(msg);
                out.flush();
            }
            if (msg instanceof Flip flip) {
                int index = flip.getIndex();
                int currentIndex = flip.getCurrentIndex();
                gui.flipTile(gui.buttonList.get(index), index, true);
                gui.flipTile(gui.buttonList.get(currentIndex), currentIndex, true);

                /*

                Timer t1 = new Timer(300, e -> {

                });
                t1.setRepeats(false);
                t1.start();
                */

                if (flip.isCorrect()) {
                    gui.disable(index, flip.getCurrentIndex());

                } else {

                    Timer t2 = new Timer(850, e -> {

                        gui.flipTile(gui.buttonList.get(index), index, false);
                        gui.flipTile(gui.buttonList.get(currentIndex), currentIndex, false);

                    });
                    t2.setRepeats(false);
                    t2.start();


                }
            }
            if (msg instanceof Select select) {

                int index = select.getIndex();

                gui.flipTile(gui.buttonList.get(index), index, true);
            }
            if (msg instanceof Action.sendAction action) {
                switch (action) {
                    case LOCK -> {
                        GUI.buttonLock = true;
                        gui.cards.setBackground(null);
                    }
                    case UNLOCK -> {

                        GUI.buttonLock = false;
                        gui.cards.setBackground(Color.ORANGE);
                    }
                    case WIN -> gui.showWinWindow(1);
                    case LOSE -> gui.showWinWindow(2);
                    case DRAW -> gui.showWinWindow(3);
                    case RESET ->
                        gui.resetGame();
                }
            }
            if (msg instanceof ArrayList<?> list) {
                gui.cards.removeAll();
                gui.cardList = (ArrayList<tiles>) list;
                gui.addButtons();
                gui.cards.revalidate();
                gui.cards.repaint();
            }
            if(msg instanceof Score score){
                gui.setScoreText(score.getP1Score(), score.getP2Score());
                p1Score = score.getP1Score();
                p2Score = score.getP2Score();
            }
        }
    }

    public void chatLoop(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        while (true) {
            Object msg = in.readObject();

            if(msg instanceof Message){
                String chatMessage = ((Message) msg).getChatMessage();
                gui.chat.append(chatMessage);
            }
        }
    }

    public void sendOb(Object o) throws IOException {
        gameOut.writeObject(o);
    }

    public void sendChatMessage(Message msg) throws IOException {
        chatOut.writeObject(msg);
        chatOut.flush();
    }

    public void setGUI(GUI gui) {
        this.gui = gui;
    }


    public static void main(String[] args) {
        Client client = new Client();
    }
}
