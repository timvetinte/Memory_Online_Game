
import javax.swing.*;
import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
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

    private boolean firstGame = true;

    public Client() {

    }

    public void disconnect(){

        try {
            if (gameOut != null) gameOut.close();
            if (gameIn != null) gameIn.close();
            if (chatOut != null) chatOut.close();
            if (chatIn != null) chatIn.close();
        } catch (IOException e) {

        }
        gameOut = null;
        gameIn = null;
        chatOut = null;
        chatIn = null;


    }

    public void connect() {

        disconnect();

        try {
            Socket gameSocket = new Socket(hostname, port);
            Socket chatSocket = new Socket(hostname, chatPort);

            this.gameOut = new ObjectOutputStream(gameSocket.getOutputStream());
            this.chatOut = new ObjectOutputStream(chatSocket.getOutputStream());


            this.gameIn = new ObjectInputStream(gameSocket.getInputStream());
            this.chatIn = new ObjectInputStream(chatSocket.getInputStream());

            new Thread(() -> {
                try {
                    listenLoop(gameIn);
                } catch (EOFException f) {
                    SwingUtilities.invokeLater(() -> gui.otherPlayerDisconnected());
                } catch (SocketException s) {
                    disconnect();
                    System.out.println("SOCKET EXCEPTION: SOCKET CLOSED");
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            new Thread(() -> {
                try {
                    chatLoop(chatIn);
                } catch (SocketException f){
                    disconnect();
                    System.out.println("Socket Exception in chat");
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).start();


        } catch (IOException e) {
            System.out.println("GAME CLOSING...");
        }
    }

    public void listenLoop(ObjectInputStream in) throws IOException, ClassNotFoundException {
        while (true) {
            Object msg = in.readObject();

            if (msg instanceof Integer tiles) {
                gui.setTileAmount(tiles);

                if (firstGame) {
                    gui.initGame(tiles);
                    firstGame = false;
                }

            }
            if (msg instanceof String) {
                gui.p2Userid = (String) msg;
            }
            if (msg instanceof Flip flip) {
                int index = flip.getIndex();
                int currentIndex = flip.getCurrentIndex();
                gui.flipTile(gui.buttonList.get(index), index, true);
                gui.flipTile(gui.buttonList.get(currentIndex), currentIndex, true);


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
                    case RESET -> gui.resetGame();
                    case DISABLE -> gui.disableButtons(true);
                    case OTHERDISCONNECT -> {
                        gui.otherPlayerDisconnected();
                        disconnect();
                    }
                }
            }
            if (msg instanceof ArrayList list) {
                gui.cards.removeAll();
                gui.setCardList(list);
                gui.addButtons();
                gui.cards.revalidate();
                gui.cards.repaint();
            }
            if (msg instanceof Score score) {
                gui.setScoreText(score.getP1Score(), score.getP2Score());
                p1Score = score.getP1Score();
                p2Score = score.getP2Score();
            }
        }
    }

    public void chatLoop(ObjectInputStream in) throws IOException, ClassNotFoundException {
        while (true) {
            Object msg = in.readObject();

            if (msg instanceof Message) {
                String chatMessage = ((Message) msg).getChatMessage();
                gui.chat.append(chatMessage);
            }
        }
    }

    public void sendOb(Object o) throws IOException {
        if(gameOut!=null) {
            gameOut.writeObject(o);
        } else {
            System.out.println("GAMEOUT WAS NULL");
        }
    }

    public void resetFirstGame(){
        firstGame=true;
    }

    public void sendChatMessage(Message msg) throws IOException {
        chatOut.writeObject(msg);
        chatOut.flush();
    }

    public void setGUI(GUI gui) {
        this.gui = gui;
    }


    static void main() {
        new Client();
    }
}
