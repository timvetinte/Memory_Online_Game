import java.io.*;
import java.util.Collections;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {

    private String p1name;
    private String p2name;

    int gamePort = 5432;
    int chatPort = 5433;

    int player1Score = 0;
    int player2Score = 0;

    int playAgain = 0;

    private boolean firstGame = true;

    int nextScore = 5;

    int correctSelections = 0;
    int totalTiles = 16;


    private ObjectOutputStream out1;
    private ObjectOutputStream out2;

    ArrayList<tiles> cardList = new ArrayList<>();

    public Server() {


        try (ServerSocket serverSocketGame = new ServerSocket(gamePort);
             ServerSocket serverSocketChat = new ServerSocket(chatPort)) {

            firstGame = true;

            while (true) {
                System.out.println("Waiting for player 1");
                Socket player1 = serverSocketGame.accept();
                System.out.println("Player one connected");

                System.out.println("Waiting for player 2");
                Socket player2 = serverSocketGame.accept();
                System.out.println("PLayer two connected");
                Socket player1Chat = serverSocketChat.accept();
                Socket player2Chat = serverSocketChat.accept();

                ObjectOutputStream chatOut1 = new ObjectOutputStream(player1Chat.getOutputStream());
                ObjectOutputStream chatOut2 = new ObjectOutputStream(player2Chat.getOutputStream());
                ObjectInputStream chatIn1 = new ObjectInputStream(player1Chat.getInputStream());
                ObjectInputStream chatIn2 = new ObjectInputStream(player2Chat.getInputStream());

                System.out.println("Starting Game...");
                new Thread(() -> startGameServer(player1, player2, chatOut1, chatOut2)).start();
                new Thread(() -> {
                    try {
                        handleChat(chatOut1, chatOut2, chatIn1, chatIn2);
                    } catch (InterruptedException e) {
                        System.out.println("GAME CLOSED " + e);
                    }
                }).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleChat(ObjectOutputStream out1, ObjectOutputStream out2, ObjectInputStream in1, ObjectInputStream in2) throws InterruptedException {

        new Thread(() -> messageReceiver(in1, out2)).start();
        new Thread(() -> messageReceiver(in2, out1)).start();
        Thread.sleep(Long.MAX_VALUE);

    }



    private void messageReceiver(ObjectInputStream in, ObjectOutputStream out) {
        try {
            while (true) {
                Object msg = in.readObject();
                if (msg instanceof Message) {
                    out.writeObject(msg);
                    out.flush();
                    System.out.println(((Message) msg).getChatMessage().trim());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
        }
    }


private void startGameServer(Socket player1, Socket player2, ObjectOutputStream chatOut1, ObjectOutputStream chatOut2) {

    try (ObjectOutputStream out1 = new ObjectOutputStream(player1.getOutputStream());
         ObjectOutputStream out2 = new ObjectOutputStream(player2.getOutputStream());
         ObjectInputStream in1 = new ObjectInputStream(player1.getInputStream());
         ObjectInputStream in2 = new ObjectInputStream(player2.getInputStream())) {



        while (true) {
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

            if(firstGame){

                p1name = (String) in1.readObject();
                p2name = (String) in2.readObject();

                out1.writeObject(p2name);
                out2.writeObject(p1name);

                out1.writeObject(new Score(0, 0));
                out2.writeObject(new Score(0, 0));

                firstGame=false;
            }
            out1.writeObject(Action.sendAction.UNLOCK);
            out2.writeObject(Action.sendAction.LOCK);


            while (correctSelections < totalTiles / 2) {


                while (true) {
                    Object flip1 = in1.readObject();
                    out2.writeObject(flip1);

                    Flip flip2 = (Flip) in1.readObject();
                    if (flip2.isCorrect()) {
                        out2.writeObject(flip2);


                        player1Score = player1Score + nextScore;
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

                while (true) {
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
            while (true) {

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
                }
                else {
                    chatOut1.writeObject(new Message("No new game starting, one or more declined. \n"));
                    chatOut2.writeObject(new Message("No new game starting, one or more declined. \n"));
                    out1.writeObject(Action.sendAction.DISABLE);
                    out2.writeObject(Action.sendAction.DISABLE);
                    return;
                }
            }
        }
    } catch (IOException e) {
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