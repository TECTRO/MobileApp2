package com.tectro.mobileapp2.data_models;

import com.tectro.mobileapp2.Interfaces.ICombinationCheckable;
import com.tectro.mobileapp2.enums.CardCombinations;
import com.tectro.mobileapp2.enums.CardNames;
import com.tectro.mobileapp2.enums.CardSuit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GameCombinations
{
    private final List<ICombinationCheckable> AvailableCombinations;

    public CardCombinations GetCombination(List<GameCard> combination)
    {
        int result = 0;
        for (int i = 0; i < AvailableCombinations.size(); i++) {
            if (AvailableCombinations.get(i).isSuitable(combination))
                result = i;
        }

        return CardCombinations.values()[result];
    }

    //region constructor
    private ArrayList<Integer> GetRepetitions(List<GameCard> combination) {
        ArrayList<Integer> result = new ArrayList<>();

        for (GameCard card1 : combination) {
            int repetitions = 1;
            for (GameCard card2 : combination) {
                if (card1 != card2)
                    if (card1.Name == card2.Name)
                        repetitions++;
            }
            result.add(repetitions);
        }

        return result;
    }

    private int GetSuits(List<GameCard> combination) {
        ArrayList<CardSuit> result = new ArrayList<>();

        for (GameCard card : combination) {
            if (card.Name != CardNames.Joker)
                if (!result.contains(card.Suit))
                    result.add(card.Suit);
        }
        return result.size();
    }



    public GameCombinations() {
        AvailableCombinations = Arrays.asList(
                //старшая карта
                (combination) -> {
                    ArrayList<Integer> rep = GetRepetitions(combination);
                    int result = 0;
                    for (int i : rep)
                        result += i;

                    return result == combination.size();
                },
                //пара
                (combination) -> GetRepetitions(combination).contains(2),

                //две пары
                (combination) -> {
                    ArrayList<Integer> TotalRepeatCount = new ArrayList<>();

                    for (int i = 0; i < combination.size(); i++) {

                        for (int j = i + 1; j < combination.size(); j++) {
                            GameCard card1 = combination.get(i);
                            GameCard card2 = combination.get(j);
                            if (card1 != card2) {
                                if (card1.Name == card2.Name && !TotalRepeatCount.contains(i) && !TotalRepeatCount.contains(j)) {
                                    TotalRepeatCount.add(i);
                                    TotalRepeatCount.add(j);
                                }
                            }
                        }
                    }

                    return TotalRepeatCount.size() == 4;
                },
                //ceт
                (combination) -> GetRepetitions(combination).contains(3),
                //стрит
                (combination) -> {

                    List<Integer> t = combination.stream().map(c -> c.Name.ordinal()).collect(Collectors.toList());
                    Collections.sort(t);

                    boolean isStreet = true;
                    for (int i = 0; i < t.size() - 1; i++) {
                        if (Math.abs(t.get(i) - t.get(i + 1)) != 1) {
                            isStreet = false;
                            break;
                        }
                    }

                    return isStreet;
                },
                //флеш
                (combination) -> GetSuits(combination) == 1,
                //Фулл хаус
                (combination) -> {
                    ArrayList<Integer> t = GetRepetitions(combination);
                    return t.contains(3) && t.contains(2);
                },
                //каре
                (combination) -> GetRepetitions(combination).contains(4),
                //стрит флеш
                (combination) -> {

                    List<Integer> t = combination.stream().map(c -> c.Name.ordinal()).collect(Collectors.toList());
                    Collections.sort(t);

                    boolean isStreet = true;
                    for (int i = 0; i < t.size() - 1; i++) {
                        if (Math.abs(t.get(i) - t.get(i + 1)) != 1) {
                            isStreet = false;
                            break;
                        }
                    }

                    return isStreet && GetSuits(combination) == 1;
                },
                //стрит роял
                (combination) -> {

                    int street = 0;
                    for (GameCard card : combination) {
                        if (card.Name == CardNames.Ten) street++;
                        if (card.Name == CardNames.Ace) street++;
                        if (card.Name == CardNames.Queen) street++;
                        if (card.Name == CardNames.King) street++;
                        if (card.Name == CardNames.Jack) street++;
                    }

                    return street == 5 && GetSuits(combination) == 1;
                }

        );
    }
    //endregion

}
