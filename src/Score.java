import java.io.Serializable;

public class Score implements Serializable {

    private int p1Score;
    private int p2Score;


    public Score(int p1Score, int p2Score){
        this.p1Score=p1Score;
        this.p2Score=p2Score;
    }

    public int getP1Score() {
        return p1Score;
    }

    public int getP2Score() {
        return p2Score;
    }
}
