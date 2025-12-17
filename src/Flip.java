import java.io.Serializable;

public class Flip implements Serializable {

    private int index;
    private int currentIndex;
    private boolean correct;
    boolean first;



    public Flip(int index, int currentIndex, boolean correct, boolean first){
        this.index=index;
        this.currentIndex=currentIndex;
        this.correct=correct;
        this.first=first;
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

    public boolean first() {
        return first;
    }
}
