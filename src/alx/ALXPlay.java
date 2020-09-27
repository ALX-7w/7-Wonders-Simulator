package alx;

import model.Cards;
import model.Player;
import model.PlayerAction;

public class ALXPlay {
    private PlayerAction act;

    public PlayerAction getAct() {
        return act;
    }

    public Cards getCard() {
        return card;
    }

    public int getLeftCost() {
        return leftCost;
    }

    public int getRightCost() {
        return rightCost;
    }

    private Cards card;
    private int leftCost, rightCost;

    public ALXPlay(PlayerAction act, Cards card, int leftCost, int rightCost) {
        this.act = act;
        this.card = card;
        this.leftCost = leftCost;
        this.rightCost = rightCost;
    }
}
