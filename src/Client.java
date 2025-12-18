
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class Client {

    String hostname = "127.0.0.1";
    int port = 5432;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GUI gui;
    int currentIndex;
    static Flip flip1;


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

    public void listenLoop(ObjectOutputStream out,ObjectInputStream in) throws IOException, ClassNotFoundException {
        while (true) {
            Object msg = in.readObject();
            if (msg instanceof String) {
                System.out.println(msg);
                out.flush();
            }
            if (msg instanceof Flip flip) {
                int index = flip.getIndex();
                int currentIndex = flip.getCurrentIndex();
                gui.flipTile(gui.buttonList.get(index), index, true);
                gui.flipTile(gui.buttonList.get(currentIndex), currentIndex, true);

                if(flip.isCorrect()){
                    gui.disable(index, flip.getCurrentIndex());

                    gui.flipTile(gui.buttonList.get(index), index, false);
                    gui.flipTile(gui.buttonList.get(currentIndex), currentIndex, false);
                    System.out.println(flip.getIndex());
                    System.out.println(flip.getCurrentIndex());
                } else {

                }
            }
            if (msg instanceof Select select){
                System.out.println(select.getIndex());
                int index = select.getIndex();
                System.out.println("FLIPPED TILE");
                gui.flipTile(gui.buttonList.get(index), index, true);
            }
            if (msg instanceof Lock){
                if(GUI.buttonLock){
                    GUI.buttonLock=false;
                } else {
                    GUI.buttonLock=true;
                }
            }
            if (msg instanceof ArrayList<?> list){
                GUI.cardList = (ArrayList<tiles>) list;
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
