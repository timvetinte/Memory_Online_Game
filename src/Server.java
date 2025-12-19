import java.util.Collections;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {

    int port = 5432;

    int player1Score = 0;
    int player2Score = 0;

    int playAgain = 0;

    int correctSelections = 0;
    int totalTiles = 16;

    ArrayList<tiles> cardList = new ArrayList<>();

    public Server() {


        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                System.out.println("Waiting for player 1");
                Socket player1 = serverSocket.accept();
                System.out.println("Player one connected");

                System.out.println("Waiting for player 2");
                Socket player2 = serverSocket.accept();
                System.out.println("PLayer two connected");

                System.out.println("Starting Game...");
                new Thread(() -> clientHandler(player1, player2)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void clientHandler(Socket player1, Socket player2) {

        try (ObjectOutputStream out1 = new ObjectOutputStream(player1.getOutputStream());
             ObjectOutputStream out2 = new ObjectOutputStream(player2.getOutputStream());
             ObjectInputStream in1 = new ObjectInputStream(player1.getInputStream());
             ObjectInputStream in2 = new ObjectInputStream(player2.getInputStream())) {



            while (true) {
                correctSelections = 0;

                populateField();

                out1.reset();
                out1.writeObject(totalTiles);
                out1.writeObject(cardList);
                out1.writeObject(Action.sendAction.UNLOCK);
                out1.flush();

                out2.reset();
                out2.writeObject(totalTiles);
                out2.writeObject(cardList);
                out2.writeObject(Action.sendAction.LOCK);
                out2.flush();



                while (correctSelections < totalTiles / 2) {


                    while (true) {
                        Object flip1 = in1.readObject();
                        out2.writeObject(flip1);

                        Flip flip2 = (Flip) in1.readObject();
                        if (flip2.isCorrect()) {
                            out2.writeObject(flip2);


                            player1Score++;
                            correctSelections++;
                            System.out.println(correctSelections + " " + "correct selection");

                            out1.writeObject(new Score(player1Score, player2Score));
                            out2.writeObject(new Score(player1Score, player2Score));
                            break;
                        }
                        out2.writeObject(flip2);

                    }


                    out1.writeObject(Action.sendAction.LOCK);
                    out2.writeObject(Action.sendAction.UNLOCK);

                    if (correctSelections == totalTiles / 2) {
                        break;}



                    //HÄR BYTER SPELAREN

                    while (true) {
                        Object flip1 = in2.readObject();
                        out1.writeObject(flip1);

                        Flip flip2 = (Flip) in2.readObject();
                        if (flip2.isCorrect()) {
                            out1.writeObject(flip2);

                            player2Score++;
                            correctSelections++;
                            System.out.println(correctSelections + " " + "correct selection");
                            out1.writeObject(new Score(player1Score, player2Score));
                            out2.writeObject(new Score(player1Score, player2Score));
                            break;
                        }
                        out1.writeObject(flip2);

                    }

                    out2.writeObject(Action.sendAction.LOCK);
                    out1.writeObject(Action.sendAction.UNLOCK);


                    if (correctSelections == totalTiles / 2) {
                        break;}

                }
                while (true) {
                    out1.writeObject(new Score(0, 0));
                    out2.writeObject(new Score(0, 0));

                    out1.writeObject(Action.sendAction.WIN);
                    out2.writeObject(Action.sendAction.WIN);
                    int p1 = (int) in1.readObject();
                    int p2 = (int) in2.readObject();
                    playAgain = p1 + p2;
                    if (playAgain == 2) {
                        playAgain = 0;

                        out1.writeObject(Action.sendAction.RESET);
                        out2.writeObject(Action.sendAction.RESET);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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