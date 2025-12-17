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
    boolean player1Chooses = true;
    int roundPLayer1 = 0;
    int roundPLayer2 = 0;
    int player1Score = 0;
    int player2Score = 0;
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


            while (true) {


                while (correctSelections < GUI.totalTiles) {
                    roundPLayer1 = 0;
                    roundPLayer2 = 0;
                    ObjectOutputStream currentplayerOut;
                    ObjectInputStream currentplayerIn;

                    if (player1Chooses) {
                        currentplayerOut = out2;
                        currentplayerIn = in1;

                    } else {
                        currentplayerOut = out2;
                        currentplayerIn = in1;
                    }
                    while (true) {
                        Object flip1 = in1.readObject();
                        out2.writeObject(flip1);
                        Flip flip2 = (Flip) in1.readObject();
                        if (flip2.isCorrect()) {
                            out2.writeObject(flip2);
                            System.out.println(flip2.getIndex());
                            System.out.println(flip2.getCurrentIndex());
                            correctSelections++;
                            break;
                        }
                        currentplayerOut.writeObject(flip2);
                    }
                    System.out.println("DETFUNKADE");


                    //HÄR BYTER SPELAREN

                    if (player1Chooses) {
                        player1Chooses = false;
                    } else player1Chooses = true;

                    if (player1Chooses) {
                        currentplayerOut = out1;
                        currentplayerIn = in2;

                    } else {
                        currentplayerOut = out2;
                        currentplayerIn = in1;
                    }


                }
                while (true) {

                    out1.writeObject("GAME OVER! Total Score: " + getPlayer1Score() + " opponent score: " + getPlayer2Score() + " points");
                    out2.writeObject("GAME OVER! Total Score: " + getPlayer2Score() + " opponent score: " + getPlayer1Score() + " points");

                    out1.writeObject(new String("Play again? 1 for yes /2 for no"));
                    out1.flush();
                    out2.writeObject(new String("Play again? 1 for yes /2 for no"));
                    out2.flush();


                    player1Score = 0;
                    player2Score = 0;

                    roundPLayer1 = 0;
                    roundPLayer2 = 0;


                    int Player1Respond = (Integer) in1.readObject();
                    int Player2Respond = (Integer) in2.readObject();

                    if (Player1Respond == 1 && Player2Respond == 1) {
                        break;
                    } else {
                        out1.writeObject("GAME OVER");
                        out2.writeObject("GAME OVER");
                        out1.flush();
                        out2.flush();


                        out1.close();
                        in1.close();
                        player1.close();

                        out2.close();
                        in2.close();
                        player2.close();
                        return;

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