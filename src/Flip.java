import java.io.Serializable;

public class Flip implements Serializable {

    private int index;
    private int currentIndex;
    private boolean correct;
    boolean flipBack;



    public Flip(int index, int currentIndex, boolean correct, boolean flipBack){
        this.index=index;
        this.currentIndex=currentIndex;
        this.correct=correct;
        this.flipBack =flipBack;
    }

    public int getIndex() {
        return index;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isCorrect() {
        return correct;
    }

    public boolean flipBack() {
        return flipBack;
    }
}
