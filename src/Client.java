
import org.w3c.dom.ls.LSOutput;

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

    static String hostname = null;
    int port = 5432;
    int chatPort = 5433;

    private GUI gui;

    private int p1Score;
    private int p2Score;

    Thread listenLoop = null;
    Thread chatLoop = null;

    Socket gameSocket = null;
    Socket chatSocket = null;

    private ObjectOutputStream gameOut;
    private ObjectOutputStream chatOut;

    private ObjectInputStream gameIn;
    private ObjectInputStream chatIn;

    public boolean firstGame = true;
    private volatile boolean running = true;

    public Client() {

    }

    public void disconnect() {
        running = false;
        try {
            if (gameSocket != null)
            {gameSocket.close();}
            if (chatSocket != null) {chatSocket.close();}
            if (gameOut != null) {gameOut.close();}
            if (gameIn != null) {gameIn.close();}
            if (chatOut != null) {chatOut.close();}
            if (chatIn != null) {chatIn.close();}
        } catch (IOException ignored) {

        }

        if (listenLoop != null) {
            listenLoop.interrupt();
            listenLoop=null;

        if (chatLoop != null) {
            chatLoop.interrupt();
            chatLoop = null;
        }
        }

    }

    /*public void connectThread(){
        new Thread (() -> {
                try {
            connect(hostname);
        } catch (Exception e) {e.printStackTrace();}
        }).start();
    }
    */

    public void connect() {
        running = true;

        try {
            gameSocket = new Socket(hostname, port);

            chatSocket = new Socket(hostname, chatPort);


            this.gameOut = new ObjectOutputStream(gameSocket.getOutputStream());
            this.chatOut = new ObjectOutputStream(chatSocket.getOutputStream());


            this.gameIn = new ObjectInputStream(gameSocket.getInputStream());
            this.chatIn = new ObjectInputStream(chatSocket.getInputStream());

            listenLoop = new Thread(() -> {
                try {
                    listenLoop(gameIn);
                } catch (EOFException f) {
                    try {
                        SwingUtilities.invokeLater(() -> {
                            gui.otherPlayerDisconnected();

                        });
                    } catch (Exception ignored) {

                    }
                } catch (SocketException ignored) {

                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });


            chatLoop = new Thread(() -> {
                try {
                    chatLoop(chatIn);
                } catch (SocketException ignored) {

                } catch (IOException | ClassNotFoundException e) {
                    if(!running) {
                        throw new RuntimeException(e);
                    }
                }
            });



            listenLoop.start();
            chatLoop.start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenLoop(ObjectInputStream in) throws IOException, ClassNotFoundException {


        while (running) {
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
                        gui.sideBar.setBackground(null);
                        gui.scoreText.setForeground(Color.BLACK);
                    }
                    case UNLOCK -> {

                        GUI.buttonLock = false;
                        gui.sideBar.setBackground(Color.ORANGE);
                        gui.scoreText.setForeground(Color.WHITE);
                    }
                    case WIN -> gui.showWinWindow(1);
                    case LOSE -> gui.showWinWindow(2);
                    case DRAW -> gui.showWinWindow(3);
                    case RESET -> gui.resetGame();
                    case DISABLE -> {
                        gui.disableButtons(true);
                    }
                    case OTHERDISCONNECT -> {
                        gui.otherPlayerDisconnected();

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
        try {
            while (running) {
                Object msg = in.readObject();

                if (msg instanceof Message) {
                    String chatMessage = ((Message) msg).getChatMessage();
                    gui.chat.append(chatMessage);
                }
            }
        }catch (SocketException | EOFException ignored){

        }
    }

    public void sendOb(Object o) throws IOException {
        if (gameOut != null) {
            gameOut.writeObject(o);
        } else {
            JOptionPane.showMessageDialog(null, "Server not found, IP may be incorrect");
            gui.enterUser();
        }
    }

    public void resetFirstGame() {
        firstGame = true;
    }

    public void sendChatMessage(Message msg) throws IOException {
        if (chatOut != null) {
            chatOut.writeObject(msg);
            chatOut.flush();
        }
    }

    public void setGUI(GUI gui) {
        this.gui = gui;
    }


    static void main() {
        new Client();
    }
}
