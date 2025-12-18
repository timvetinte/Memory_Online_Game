import java.io.Serializable;

public class Select implements Serializable {

    private int index;

    public Select(int index){
        this.index=index;

    }

    public int getIndex() {
        return index;
    }
}
