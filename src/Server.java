import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {

    private String p1name;
    private String p2name;

    public static String ip = null;

    int gamePort = 5432;
    int chatPort = 5433;

    int player1Score = 0;
    int player2Score = 0;

    ServerSocket serverSocketGame;
    ServerSocket serverSocketChat;

    private Socket player1;
    private Socket player2;
    private Socket player1Chat;
    private Socket player2Chat;

    private Thread gameThread;
    private Thread chatThread;
    private Thread acceptingThread;

    int playAgain = 0;

    ArrayList<Thread> threadList = new ArrayList();
    ArrayList<ServerSocket> socketList = new ArrayList();


    private boolean firstGame = true;
    private volatile boolean running = true;

    int nextScore = 5;

    int correctSelections = 0;
    static int totalTiles = 16;

    ArrayList<tiles> cardList = new ArrayList<>();

    public Server() {


        try {
            serverSocketGame = new ServerSocket();
            serverSocketGame.setReuseAddress(true);
            serverSocketGame.bind(new InetSocketAddress(gamePort));


            serverSocketChat = new ServerSocket();
            serverSocketChat.setReuseAddress(true);
            serverSocketChat.bind(new InetSocketAddress(chatPort));

            socketList.add(serverSocketChat);
            socketList.add(serverSocketGame);


            firstGame = true;
            acceptingThread = new Thread(() -> {
                try {
                    while (running) {
                        System.out.println("Waiting for player 1");
                        player1 = serverSocketGame.accept();
                        System.out.println("Player one connected");

                        System.out.println("Waiting for player 2");
                        player2 = serverSocketGame.accept();
                        System.out.println("Player two connected");
                        player1Chat = serverSocketChat.accept();
                        player2Chat = serverSocketChat.accept();

                        ObjectOutputStream chatOut1 = new ObjectOutputStream(player1Chat.getOutputStream());
                        ObjectOutputStream chatOut2 = new ObjectOutputStream(player2Chat.getOutputStream());
                        ObjectInputStream chatIn1 = new ObjectInputStream(player1Chat.getInputStream());
                        ObjectInputStream chatIn2 = new ObjectInputStream(player2Chat.getInputStream());

                        System.out.println("Starting Game...");
                        gameThread = new Thread(() -> startGameServer(player1, player2, chatOut1, chatOut2));
                        gameThread.start();
                        chatThread = new Thread(() -> {
                            try {
                                handleChat(chatOut1, chatOut2, chatIn1, chatIn2);
                            } catch (InterruptedException e) {
                                System.out.println("GAME CLOSED " + e);
                            }
                        });
                        chatThread.start();
                        threadList.add(gameThread);
                        threadList.add(chatThread);
                        System.out.println("2 threads added");
                    }
                } catch (IOException e) {
                    running = false;
                }
            });
            acceptingThread.start();
        } catch (IOException e) {
            running = false;
        }
    }


            public void shutdownSever() throws IOException {
                int index = 1;
                running = false;

                if (player1 != null) player1.close();
                if (player2 != null) player2.close();
                if (player1Chat != null) player1Chat.close();
                if (player2Chat != null) player2Chat.close();

                for (ServerSocket s : socketList) {
                    s.close();
                    System.out.println("shutdown " + index + " sockets");
                    index++;
                }

                index = 1;
                for (Thread t : threadList) {
                    t.interrupt();
                    System.out.println("shutdown " + index + " threads");
                    index++;
                }
                if (acceptingThread != null) {
                    acceptingThread.interrupt();
                }
            }

    private void handleChat(ObjectOutputStream out1, ObjectOutputStream out2, ObjectInputStream in1, ObjectInputStream in2) throws InterruptedException {

        new Thread(() -> messageReceiver(in1, out2)).start();
        new Thread(() -> messageReceiver(in2, out1)).start();
        Thread.sleep(Long.MAX_VALUE);

    }


    private void messageReceiver(ObjectInputStream in, ObjectOutputStream out) {
        try {
            while (running) {
                Object msg = in.readObject();
                if (msg instanceof Message) {
                    out.writeObject(msg);
                    out.flush();
                    System.out.println(((Message) msg).getChatMessage().trim());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            running = false;
        }
    }


    private void startGameServer(Socket player1, Socket player2, ObjectOutputStream chatOut1, ObjectOutputStream chatOut2) {

        try (ObjectOutputStream out1 = new ObjectOutputStream(player1.getOutputStream());
             ObjectOutputStream out2 = new ObjectOutputStream(player2.getOutputStream());
             ObjectInputStream in1 = new ObjectInputStream(player1.getInputStream());
             ObjectInputStream in2 = new ObjectInputStream(player2.getInputStream())) {


            while (running) {
                correctSelections = 0;
                player1Score = 0;
                player2Score = 0;


                populateField();

                out1.reset();
                out1.writeObject(totalTiles);
                out1.writeObject(cardList);
                out1.flush();

                out2.reset();
                out2.writeObject(totalTiles);
                out2.writeObject(cardList);
                out2.flush();


                out1.writeObject(Action.sendAction.LOCK);
                out2.writeObject(Action.sendAction.LOCK);

                if (firstGame) {

                    p1name = (String) in1.readObject();
                    p2name = (String) in2.readObject();

                    out1.writeObject(p2name);
                    out2.writeObject(p1name);

                    out1.writeObject(new Score(0, 0));
                    out2.writeObject(new Score(0, 0));

                    firstGame = false;
                }
                out1.writeObject(Action.sendAction.UNLOCK);
                out2.writeObject(Action.sendAction.LOCK);


                while (correctSelections < totalTiles / 2) {

//Spelare 1
                    while (running) {
                        Object flip1 = in1.readObject();
                        //Första klicket
                        out2.writeObject(flip1);
                        //Första klicket skickas till andra spelaren

                        Flip flip2 = (Flip) in1.readObject();
                        //Andra klicket

                        if (flip2.isCorrect()) {
                            //Om andra klicket korrekt
                            out2.writeObject(flip2);
                            //Skicka till andra spelaren


                            player1Score = player1Score + nextScore;
                            //lägger på korrekta summan till spelaren

                            correctSelections++;
                            //totala poäng, jämförs med totala mängden som finns att vinna
                            System.out.println(correctSelections + " " + "correct selection");

                            out1.writeObject(new Score(player1Score, player2Score));
                            out2.writeObject(new Score(player2Score, player1Score));
                            nextScore = 5;
                            break;
                        } else {
                            if (nextScore > 1) {
                                nextScore--;
                            }
                        }
                        out2.writeObject(flip2);

                    }


                    if (correctSelections == totalTiles / 2) {
                        out1.writeObject(Action.sendAction.LOCK);
                        out2.writeObject(Action.sendAction.LOCK);
                        break;
                    }

                    out1.writeObject(Action.sendAction.LOCK);
                    out2.writeObject(Action.sendAction.UNLOCK);


                    //HÄR BYTER SPELAREN

                    while (running) {
                        Object flip1 = in2.readObject();
                        out1.writeObject(flip1);

                        Flip flip2 = (Flip) in2.readObject();
                        if (flip2.isCorrect()) {
                            out1.writeObject(flip2);

                            player2Score = player2Score + nextScore;

                            correctSelections++;
                            System.out.println(correctSelections + " " + "correct selection");
                            out1.writeObject(new Score(player1Score, player2Score));
                            out2.writeObject(new Score(player2Score, player1Score));
                            nextScore = 5;
                            break;
                        } else {
                            if (nextScore > 1) {
                                nextScore--;
                            }
                        }
                        out1.writeObject(flip2);

                    }


                    if (correctSelections == totalTiles / 2) {
                        out1.writeObject(Action.sendAction.LOCK);
                        out2.writeObject(Action.sendAction.LOCK);
                        break;
                    }

                    out2.writeObject(Action.sendAction.LOCK);
                    out1.writeObject(Action.sendAction.UNLOCK);

                }
                while (running) {

                    if (player1Score > player2Score) {
                        chatOut1.writeObject(new Message(p1name + " won with: " + player1Score + " points!\n"));
                        chatOut2.writeObject(new Message(p1name + " won with: " + player1Score + " points!\n"));
                        out1.writeObject(Action.sendAction.WIN);
                        out2.writeObject(Action.sendAction.LOSE);
                    } else if (player2Score > player1Score) {
                        chatOut1.writeObject(new Message(p2name + " won with: " + player2Score + " points!\n"));
                        chatOut2.writeObject(new Message(p2name + " won with: " + player2Score + " points!\n"));
                        out2.writeObject(Action.sendAction.WIN);
                        out1.writeObject(Action.sendAction.LOSE);
                    } else {
                        chatOut1.writeObject(new Message("A draw occurred with: " + player1Score + " points.\n"));
                        chatOut2.writeObject(new Message("A draw occurred with: " + player1Score + " points.\n"));
                        out1.writeObject(Action.sendAction.DRAW);
                        out2.writeObject(Action.sendAction.DRAW);
                    }

                    int p1 = (int) in1.readObject();
                    int p2 = (int) in2.readObject();
                    playAgain = p1 + p2;
                    if (playAgain == 2) {
                        playAgain = 0;

                        out1.writeObject(Action.sendAction.RESET);
                        out2.writeObject(Action.sendAction.RESET);
                        out1.writeObject(new Score(0, 0));
                        out2.writeObject(new Score(0, 0));
                        break;
                    } else {
                        chatOut1.writeObject(new Message("No new game starting, one or more declined. \n"));
                        chatOut2.writeObject(new Message("No new game starting, one or more declined. \n"));
                        out1.writeObject(Action.sendAction.DISABLE);
                        out2.writeObject(Action.sendAction.DISABLE);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            running=false;
            try {
                player1.close();
                player2.close();
            } catch (IOException ex) {
            }
            System.out.println("IOException");

        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException");
        } finally {
            firstGame = true;
        }
    }


    public void populateField() {
        cardList.clear();
        for (int i = 0; i < totalTiles / 2; i++) cardList.add(new tiles(i));
        cardList.addAll(cardList);
        Collections.shuffle(cardList);
    }


    void main() {
        new Server();

    }


}