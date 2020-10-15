package com.tectro.mobileapp2.data_models;

import android.security.identity.EphemeralPublicKeyNotFoundException;

import com.tectro.mobileapp2.Interfaces.IPlayersHoldable;
import com.tectro.mobileapp2.Interfaces.IRateCollectable;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Player implements IRateCollectable {
    private ArrayList<ChangeableCard> TakenCards;
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

    public boolean TryReplaceCard(int index,GameCard newCard)
    {
        if(index<TakenCards.size())
            return TakenCards.get(index).TryChangeCard(newCard);

        return false;
    }

    public ArrayList<GameCard> getTakenCards() {
        return (ArrayList<GameCard>) TakenCards.stream().map(ChangeableCard::getHoldenCard).collect(Collectors.toList());
    }

    public void setTakenCards(ArrayList<GameCard> takenCards) {
        TakenCards = takenCards != null ? (ArrayList<ChangeableCard>) takenCards.stream().map(ChangeableCard::new).collect(Collectors.toList()) : new ArrayList<>();
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
        this.readyRate = Math.min(readyRate, Score);
    }

    public Player(int initialScore)
    {
        Score = initialScore;
        TakenCards = new ArrayList<>();
    }
    //endregion
}

class ChangeableCard
{

    private GameCard HoldenCard;
    private boolean CanChange;

    //region Access modifiers
    public boolean TryChangeCard(GameCard card)
    {
        if(CanChange){
            HoldenCard = card;
            CanChange = false;
            return true;
        }

        return false;
    }

    public GameCard getHoldenCard() {
        return HoldenCard;
    }

    ChangeableCard(GameCard holdenCard) {
        HoldenCard = holdenCard;
        CanChange = true;
    }

    ChangeableCard() {
        CanChange = true;
    }
    //endregion
}
