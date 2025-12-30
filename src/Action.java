import java.io.Serializable;

public class Action implements Serializable {

    public enum sendAction {
        LOCK,
        UNLOCK,
        WIN,
        LOSE,
        DRAW,
        DISABLE,
        RESET;

        int scoreP1;
        int scoreP2;

    }
}