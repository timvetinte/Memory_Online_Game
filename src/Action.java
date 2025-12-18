import java.io.Serializable;

public class Action implements Serializable {

    public static enum sendAction{
        LOCK,
        UNLOCK,
        WIN,
        RESET,
    }

}
