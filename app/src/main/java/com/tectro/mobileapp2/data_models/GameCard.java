package com.tectro.mobileapp2.data_models;

import com.tectro.mobileapp2.enums.CardColors;
import com.tectro.mobileapp2.enums.CardNames;
import com.tectro.mobileapp2.enums.CardSuit;

public class GameCard
{
    public final CardNames Name;
    public final CardSuit Suit;
    public final CardColors Color;

    public GameCard(CardNames name, CardSuit suit) {
        Name = name;
        Suit = suit;
        if(suit== CardSuit.Diamonds || suit == CardSuit.Hearts)
            Color = CardColors.Red;
        else
            Color = CardColors.Black;
    }

    public GameCard(CardNames name, CardColors color) {
        Name = name;
        Suit = CardSuit.NONE;
        Color = color;
    }
}
