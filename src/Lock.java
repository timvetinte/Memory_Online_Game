import java.io.Serializable;

public class Lock implements Serializable {

    private boolean lock;

public Lock (boolean lock){
    this.lock=lock;
}

    public boolean isLocked() {
        return lock;
    }
}
