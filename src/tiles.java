import java.io.Serializable;

public class tiles implements Serializable {

    private int symbol;

    public tiles(int symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        switch (symbol) {
            case 0: return "֍";
            case 1: return "❀";
            case 2: return "☘";
            case 3: return "⬟";
            case 4: return "⍜";
            case 5: return "⍟";
            case 6: return "⌘";
            case 7: return "⌺";
            case 8: return "♦";
            case 9: return "⎈";
            case 10: return "♠";
            case 11: return "⏾";
            case 12: return "℮";
            case 13: return "℧";
            case 14: return "♥";
            case 15: return "⌼";
            case 16: return "⍬";
            case 17: return "▣";
            case 18: return "☃";
            case 19: return "☏";
            case 20: return "☠";
            case 21: return "☢";
            case 22: return "☣";
            case 23: return "☺";
            case 24: return "♚";
            case 25: return "♣";
            case 26: return "◈";
            case 27: return "◒";
            case 28: return "☁";
            case 29: return "★";
            case 30: return "☂";
            case 31: return "◎";
            case 32: return "☼";
            default: return "?";
        }
    }
}
