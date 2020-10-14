package com.tectro.mobileapp2;

import com.tectro.mobileapp2.Interfaces.IActivityUpdatable;
import com.tectro.mobileapp2.Interfaces.ICombinationCheckable;
import com.tectro.mobileapp2.data_models.GameCard;
import com.tectro.mobileapp2.data_models.GameRate;
import com.tectro.mobileapp2.data_models.Player;
import com.tectro.mobileapp2.enums.CardColors;
import com.tectro.mobileapp2.enums.CardNames;
import com.tectro.mobileapp2.enums.CardSuit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameModel {

    //region Singleton
    private static GameModel currentModel;

    public static GameModel CreateInstance() {
        if (currentModel == null) currentModel = new GameModel();
        return currentModel;
    }

    public static GameModel RebuildInstance() {
        currentModel = new GameModel();
        return currentModel;
    }
    //endregion

    private IActivityUpdatable Activity;
    private Random rand;

    private List<ICombinationCheckable> Combinations;
    private List<GameCard> CardDeck;

    private Player Gamer;
    private Player Enemy;

    private GameRate Rate;

    public void ShareCards() {
        for (int i = 0; i < 5; i++)
            Gamer.getTakenCards().add(CardDeck.remove(rand.nextInt(CardDeck.size())));

        for (int i = 0; i < 5; i++)
            Enemy.getTakenCards().add(CardDeck.remove(rand.nextInt(CardDeck.size())));

        //region Notifier
        if (Activity != null)
            Activity.UpdateCards();
        //endregion
    }

    public void UpdateRate(Player player, int rate) {
        player.setReadyRate(rate);

        //region Notifier
        if (Activity != null)
            Activity.UpdateRates();
        //endregion
    }

    public void ReplaceCard(int index, Player player) {
        GameCard newCard = CardDeck.remove(rand.nextInt(CardDeck.size()));
        CardDeck.add(player.getTakenCards().get(index));
        player.getTakenCards().set(index, newCard);

        //region Notifier
        if (Activity != null)
            Activity.UpdateCards();
        //endregion

    }

    public void EndGameLap() {
        boolean canContinue = false;
        if (Gamer.getReadyRate() > 0 && Gamer.getTakenCards().size() > 0)
            if (Enemy.getReadyRate() > 0 && Enemy.getTakenCards().size() > 0)
                canContinue = true;


        if (canContinue) {
            int GamerCombinationLevel = GetCombinationLevel(Gamer);
            int EnemyCombinationLevel = GetCombinationLevel(Enemy);

            Player winner = null;

            if (GamerCombinationLevel > EnemyCombinationLevel) {
                Rate.CollectResults(Gamer);
                winner = Gamer;
            } else if (GamerCombinationLevel < EnemyCombinationLevel) {
                Rate.CollectResults(Enemy);
                winner = Enemy;
            } else
                Rate.CollectResults(null);


            //region Notifier
            if (Activity != null)
                if (Rate.getGamesLeft() == 0)
                    Activity.FinishGame(winner);
                else
                {
                    Activity.UpdateRates();
                    Activity.UpdateCards();
                    Activity.UpdateScore();
                }
            //endregion

        }

    }


    //region InteractionHelpers

    public Player getGamer() {
        return Gamer;
    }

    public Player getEnemy() {
        return Enemy;
    }


    public boolean isCardsShared() {
        return Gamer.getTakenCards().size() > 0 && Enemy.getTakenCards().size() > 0;
    }
    //endregion

    //region Constructor
    private int GetCombinationLevel(Player player) {
        return Combinations.stream().map(c -> c.isSuitable(player.getTakenCards())).collect(Collectors.toList()).lastIndexOf(false);
    }

    private GameModel() {
        rand = new Random();

        CardDeck = new ArrayList<GameCard>(Arrays.asList(
                new GameCard(CardNames.Joker, CardColors.Black),
                new GameCard(CardNames.Joker, CardColors.Red)
        ));


        for (CardNames cardName : CardNames.values()) {
            for (CardSuit suit : CardSuit.values()) {
                if (suit != CardSuit.NONE && cardName != CardNames.Joker) {
                    CardDeck.add(new GameCard(cardName, suit));
                }
            }
        }


        Gamer = new Player(1000);
        Enemy = new Player(1000);
        Rate = new GameRate(Arrays.asList(Gamer, Enemy), 10);


        //todo добавить комбинации
        Combinations = Arrays.asList(
                (combination) -> {

                    return false;
                },
                (combination) -> {

                    return false;
                },
                (combination) -> {

                    return false;
                },
                (combination) -> {

                    return false;
                },
                (combination) -> {

                    return false;
                },
                (combination) -> {

                    return false;
                }
        );

    }

    public void setActivity(IActivityUpdatable activity) {
        Activity = activity;
    }
    //endregion

}
