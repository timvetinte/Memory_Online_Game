import java.io.Serializable;

public class Message implements Serializable {

    private String chatMessage;

    public Message( String chatMessage){
        this.chatMessage=chatMessage;
    }

    public String getChatMessage() {
        return chatMessage;
    }
}
