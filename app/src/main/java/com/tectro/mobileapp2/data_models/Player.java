package com.tectro.mobileapp2.data_models;

import com.tectro.mobileapp2.Interfaces.IPlayersHoldable;
import com.tectro.mobileapp2.Interfaces.IRateCollectable;

import java.util.ArrayList;

public class Player implements IRateCollectable {
    private ArrayList<GameCard> TakenCards;
    private int Score;
    private int readyRate;

    @Override
    public void CollectRate(IRateCollectable winner, IPlayersHoldable holdableFunc) {
        if (this == winner) {
            Score += holdableFunc.GetLosersRate();
        } else
            Score -= readyRate;
        readyRate = 0;
    }

    //region access modifiers

    public ArrayList<GameCard> getTakenCards() {
        return TakenCards;
    }

    public void setTakenCards(ArrayList<GameCard> takenCards) {
        TakenCards = takenCards;
    }

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        Score = score;
    }

    public int getReadyRate() {
        return readyRate;
    }

    public void setReadyRate(int readyRate) {
        this.readyRate = readyRate <= this.readyRate ? readyRate : Score;
    }

    public Player(int initialScore)
    {
        Score = initialScore;
        TakenCards = new ArrayList<>();
    }
    //endregion
}
