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
            populateField();
            out1.writeObject(cardList);
            out2.writeObject(cardList);

            out2.writeObject(Action.sendAction.LOCK);

            while (true) {


                while (correctSelections < GUI.totalTiles / 2) {


                    while (true) {
                        Object flip1 = in1.readObject();
                        out2.writeObject(flip1);
                        System.out.println(flip1.getClass());
                        Flip flip2 = (Flip) in1.readObject();
                        if (flip2.isCorrect()) {
                            out2.writeObject(flip2);
                            System.out.println(flip2.getIndex());
                            System.out.println(flip2.getCurrentIndex());
                            player1Score++;
                            correctSelections++;
                            System.out.println(correctSelections);
                            break;
                        }
                        out2.writeObject(flip2);

                    }
                    System.out.println("DET FUNKADE");


                    out1.writeObject(Action.sendAction.LOCK);
                    out2.writeObject(Action.sendAction.UNLOCK);
                    System.out.println("SHOULD UNLOCK HERE");



                    //HÄR BYTER SPELAREN

                    while (true) {
                        Object flip1 = in2.readObject();
                        out1.writeObject(flip1);
                        System.out.println(flip1.getClass());
                        Flip flip2 = (Flip) in2.readObject();
                        if (flip2.isCorrect()) {
                            out1.writeObject(flip2);
                            System.out.println(flip2.getIndex());
                            System.out.println(flip2.getCurrentIndex());
                            player2Score++;
                            correctSelections++;
                            System.out.println(correctSelections + " " + "correct selection");
                            break;
                        }
                        out1.writeObject(flip2);

                    }
                    System.out.println("DETFUNKADE");
                    out2.writeObject(Action.sendAction.LOCK);
                    out1.writeObject(Action.sendAction.UNLOCK);
                    System.out.println("SHOULD UNLOCK HERE");



                }
                while (true) {
                    correctSelections = 0;
                    out1.writeObject(Action.sendAction.WIN);
                    out2.writeObject(Action.sendAction.WIN);
                    playAgain = playAgain + (int) in1.readObject();
                    playAgain = playAgain + (int) in2.readObject();
                    if (playAgain == 2) {
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

    public int getPlayer2Score() {
        return player2Score;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void populateField() {
        cardList.clear();
        for (int i = 0; i < totalTiles / 2; i++) cardList.add(new tiles(i));
        cardList.addAll(cardList);
        Collections.shuffle(cardList);
    }


    public void main(String[] args) {
        Server server = new Server();

    }


}