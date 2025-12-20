
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
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GUI gui;

    private int p1Score;
    private int p2Score;

    private int totalTiles = 0;
    private boolean firstGame = true;

    public Client() {
        try {
            Socket adressSocket = new Socket(hostname, port);

            this.out = new ObjectOutputStream(adressSocket.getOutputStream());
            this.in = new ObjectInputStream(adressSocket.getInputStream());

            new Thread(() -> {
                try {
                    listenLoop(this.out, this.in);
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

                if (flip.isCorrect()) {
                    gui.disable(index, flip.getCurrentIndex());

                } else {

                    Timer t = new Timer(850, e -> {

                        gui.flipTile(gui.buttonList.get(index), index, false);
                        gui.flipTile(gui.buttonList.get(currentIndex), currentIndex, false);

                    });
                    t.setRepeats(false);
                    t.start();


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

    public void sendOb(Object o) throws IOException {
        out.writeObject(o);
    }

    public void setGUI(GUI gui) {
        this.gui = gui;
    }


    public static void main(String[] args) {
        Client client = new Client();
    }
}
