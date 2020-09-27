package alx;

import javafx.util.Pair;
import model.Cards;
import model.Player;
import model.PlayerAction;
import model.Wonder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ALXOnePlayerController extends ALXController{
    // Use make notification between controller and player outside of the simulation
    private Object bell;
    private Action waitedAction;
    private Cards played;
    private ReentrantLock actionLock, playsLock;
    private boolean gameOver;
    private boolean side;
    private int playedTurns, totalTurn = 18;
    private ArrayList<Cards> hand;
    private PlayerAction cardUsing;
    HashMap<String, Pair<PlayerAction, Cards>> plays;
    private boolean playsUpToDate;
    HashMap<String, Integer> moneyMove;

    public void setLeftCost(int leftCost) {
        this.leftCost = leftCost;
    }

    public void setRightCost(int rightCost) {
        this.rightCost = rightCost;
    }

    private int leftCost, rightCost;


    public void setCardUsing(PlayerAction cardUsing) {
        this.cardUsing = cardUsing;
    }

    public Object getBell() {
        return bell;
    }
    public ArrayList<Cards> getHand() {
        return hand;
    }
    public Action getWaitedAction() {
        actionLock.lock();
        Action a = waitedAction;
        actionLock.unlock();
        return a;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setWaitedAction(Action a) {
        actionLock.lock();
        waitedAction=a;
        actionLock.unlock();
    }

    public Cards getPlayed() {
        return played;
    }

    public void setPlayed(Cards played) {
        this.played = played;
    }

    public void setPlayed(Cards played, int leftCost, int rightCost) {
        this.played = played;
        this.leftCost = leftCost;
        this.rightCost = rightCost;
    }

    public void setSide(boolean side) {
        this.side = side;
    }



    public ALXOnePlayerController() {
        super();
        bell = new Object();
        actionLock = new ReentrantLock();
        playsLock = new ReentrantLock();
        playsUpToDate = false;
        waitedAction = Action.NONE;
        gameOver = false;
        playedTurns = 0;
        plays = new HashMap<String, Pair<PlayerAction, Cards>>();
    }

    public void registerSimPlayer(ALXPlayer player){
        simPlayers.put(player.wonder.name, player);
    }

    public ALXPlay getSimPlayAction(String wonderName, ArrayList<Cards> hand){
        actionLock.lock();
        waitedAction = Action.PLAY;
        this.hand = hand;
        playedTurns ++;
        if(playedTurns==totalTurn){
            gameOver = true;
        }
        actionLock.unlock();
        synchronized (bell){
            bell.notify();
        }

        try {
            synchronized (bell){
                bell.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (new ALXPlay(cardUsing, played, leftCost, rightCost));
    }

    public void checkIfStarted(){
        boolean started = false;
        while (!started){
            actionLock.lock();
            if(waitedAction!=Action.NONE){
                started = true;
            }
            actionLock.unlock();
            if(!started){
                System.out.println("Not started");
                try {
                    synchronized (bell){
                        bell.wait(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void playCard(Cards c){
        setPlayed(c);
        synchronized (bell){
            bell.notify();
        }
        if(!gameOver){
            synchronized (bell){
                try {
                    bell.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void playCard(Cards c, PlayerAction using, int leftCost, int rightCost){
        setPlayed(c, leftCost, rightCost);
        setCardUsing(using);
        synchronized (bell){
            bell.notify();
        }
        if(!gameOver){
            synchronized (bell){
                try {
                    bell.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Boolean getWonderSide(Wonder w){
        actionLock.lock();
        waitedAction = Action.WONDER_SIDE;
        playedTurns ++;
        if(playedTurns==totalTurn){
            gameOver = true;
        }
        actionLock.unlock();
        synchronized (bell){
            bell.notify();
        }

        try {
            synchronized (bell){
                bell.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return side;
    }

    public Cards getCopyGuild(ArrayList<Cards> guildChoices){
        actionLock.lock();
        waitedAction = Action.COPY_GUILD;
        this.hand = hand;
        playedTurns ++;
        if(playedTurns==totalTurn){
            gameOver = true;
        }
        actionLock.unlock();
        synchronized (bell){
            bell.notify();
        }

        try {
            synchronized (bell){
                bell.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return played;
    }

    public Cards getPlayFromDiscard(ArrayList<Cards> playable){
        actionLock.lock();
        waitedAction = Action.DISCARD_PLAY;
        this.hand = hand;
        playedTurns ++;
        if(playedTurns==totalTurn){
            gameOver = true;
        }
        actionLock.unlock();
        synchronized (bell){
            bell.notify();
        }

        try {
            synchronized (bell){
                bell.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return played;
    }

    public Pair<PlayerAction,Cards> defaultAction(Player p , ArrayList<Cards> cards) {
        PlayerAction action;
        Cards lastCard;
        p.checkResources(cards);
        p.canBuildWonder=p.checkWonder();
        action = PlayerAction.CARD;
        int cardPlayed = -1;
        if (p.canBuildWonder) {
            action=PlayerAction.WONDER;
            cardPlayed=0;
            if (p.wonderOptions!=null) {
                int minLeft=50,minRight=50;
                for (int j:p.wonderOptions) {
                    if (j/100+j%100<minLeft+minRight) {
                        minLeft=j/100;
                        minRight=j%100;
                    }
                }
                p.leftCost=minLeft;
                p.rightCost=minRight;
            }
            lastCard = cards.remove(cardPlayed);
            return new Pair<PlayerAction, Cards>(action, lastCard);
        }
        for (int i=0;i<cards.size();i++) {
            if (p.hasFreeBuild>0&&p.playableCost[i]!=-2) {p.hasFreeBuild=0;cardPlayed=i;break;}
            else if (p.playableCost[i]==0) {cardPlayed=i; break;}
            else if (p.playableCost[i]>0) {
                ArrayList<Integer> a = p.resourceOptions.get(i);
                for (int j:a) {
                    if ((j/100+j%100)==p.playableCost[i]) {
                        p.leftCost=j/100;
                        p.rightCost=j%100;
                        break;
                    }
                }
                cardPlayed=i;
                break;
            }
        }
        if (cardPlayed==-1) {
            cardPlayed=0;
            if (p.canBuildWonder) {
                action=PlayerAction.WONDER;
                if (p.wonderOptions!=null) {
                    int minLeft=50,minRight=50;
                    for (int j:p.wonderOptions) {
                        if (j/100+j%100<minLeft+minRight) {
                            minLeft=j/100;
                            minRight=j%100;
                        }
                    }
                    p.leftCost=minLeft;
                    p.rightCost=minRight;
                }
            } else
                action=PlayerAction.COIN;
        }
        lastCard = cards.remove(cardPlayed);
        return new Pair<PlayerAction, Cards>(action, lastCard);
    }

    public boolean defaultWonderSide(){
        return true;
    }

    public Cards defaultCopyGuild(ArrayList<Cards> guildChoices){
        if(guildChoices.isEmpty()){
            return null;
        }
        return guildChoices.get(0);
    }

    public Cards defaultPlayFromDiscard(ArrayList<Cards> playable){
        if(playable.isEmpty()){
            return null;
        }
        return playable.get(0);
    }


    public Pair<HashMap<String, Pair<PlayerAction, Cards>>, HashMap<String, Integer>> getPlaysAndMoneyMove() {
        synchronized (plays){
            if(playsUpToDate){
                playsUpToDate = false;
                return new Pair(plays, moneyMove);
            }
            else{
                try {
                    plays.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playsUpToDate = false;
                return new Pair(plays, moneyMove);
            }
        }
    }

    public void setPlays(HashMap<String, Pair<PlayerAction, Cards>> plays, HashMap<String, Integer> moneyMove) {
        synchronized (plays){
            playsUpToDate = true;
            this.plays = plays;
            this.moneyMove = moneyMove;
        }
    }
}
