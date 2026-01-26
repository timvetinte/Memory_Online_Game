
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
            {gameSocket.close();} else {
                System.out.println("Gamesocket was not closed");}
            if (chatSocket != null) {chatSocket.close();} else {
            System.out.println("chatSocket was not closed");}
            if (gameOut != null) {gameOut.close();} else {
            System.out.println("game out was not closed");}
            if (gameIn != null) {gameIn.close();} else {
            System.out.println("game in was not closed");}
            if (chatOut != null) {chatOut.close();} else {
            System.out.println("chat out was not closed");}
            if (chatIn != null) {chatIn.close();} else {
            System.out.println("chat in was not closed");}
        } catch (IOException e) {

        }

        if (listenLoop != null) {
            listenLoop.interrupt();
        } else {
            System.out.println("listenloop was not interrupted");}
        if (chatLoop != null) {
            chatLoop.interrupt();
        } else {
            System.out.println("chatloop was not interrupted");
        }

    }

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
                            System.out.println("EOFEXCEPTION");
                        });
                    } catch (Exception ignored) {

                    }
                } catch (SocketException s) {
                    disconnect();
                    System.out.println("SOCKET EXCEPTION: SOCKET CLOSED");
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
            listenLoop.setDaemon(true);

            chatLoop = new Thread(() -> {
                try {
                    chatLoop(chatIn);
                } catch (SocketException f) {
                    System.out.println("Socket Exception in chat");
                } catch (IOException | ClassNotFoundException e) {
                    if(!running) {
                        throw new RuntimeException(e);
                    }
                }
            });

            chatLoop.setDaemon(true);

            listenLoop.start();
            chatLoop.start();


        } catch (IOException e) {
            System.out.println("GAME CLOSING...");
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
                System.out.println(action);
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
                        System.out.println("DISABLED CORRECTLY");
                    }
                    case OTHERDISCONNECT -> {
                        gui.otherPlayerDisconnected();
                        System.out.println("Server sent: OTHERDISCONNECT");
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
        System.out.println("LISTENLOOP EXITED CORRECTLY");
    }

    public void chatLoop(ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            while (running) {
                Object msg = in.readObject();

                if (msg instanceof Message) {
                    String chatMessage = ((Message) msg).getChatMessage();
                    System.out.println(chatMessage);
                    gui.chat.append(chatMessage);
                }
            }
        }catch (SocketException | EOFException f){
            return;
        }
        System.out.println("CHATLOOP EXITED CORRECTLY");
    }

    public void sendOb(Object o) throws IOException {
        if (gameOut != null) {
            gameOut.writeObject(o);
        } else {
            System.out.println("GAMEOUT WAS NULL");
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
