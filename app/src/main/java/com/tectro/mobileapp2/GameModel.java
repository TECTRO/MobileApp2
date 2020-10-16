package com.tectro.mobileapp2;

import android.app.job.JobInfo;

import com.tectro.mobileapp2.Interfaces.IActivityUpdatable;
import com.tectro.mobileapp2.Interfaces.ICombinationCheckable;
import com.tectro.mobileapp2.data_models.GameCard;
import com.tectro.mobileapp2.data_models.GameCombinations;
import com.tectro.mobileapp2.data_models.GameRate;
import com.tectro.mobileapp2.data_models.Player;
import com.tectro.mobileapp2.enums.CardColors;
import com.tectro.mobileapp2.enums.CardCombinations;
import com.tectro.mobileapp2.enums.CardNames;
import com.tectro.mobileapp2.enums.CardSuit;
import com.tectro.mobileapp2.enums.GameStates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private GameStates GameState;
    private Random rand;

    private GameCombinations AvailableCombinations;
    private List<GameCard> CardDeck;

    private Player Gamer;
    private Player Enemy;

    private GameRate Rate;

    public void ShareCards() {
        ArrayList<GameCard> Cards = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            Cards.add(CardDeck.remove(rand.nextInt(CardDeck.size())));
        Gamer.setTakenCards(Cards);

        Cards.clear();

        for (int i = 0; i < 5; i++)
            Cards.add(CardDeck.remove(rand.nextInt(CardDeck.size())));
        Enemy.setTakenCards(Cards);

        //region Notifier
        GameState = GameStates.SharedCards;
        if (Activity != null) {
            Activity.UpdateCards();
            Activity.UpdateGameState(GameState);
        }

        //endregion
    }

    public void UpdateRate(Player player, int rate) {
        player.setReadyRate(rate);

        //region Notifier
        if (isRateTaken())
            GameState = GameStates.RatesTaken;
        if (Activity != null) {
            Activity.UpdateRates();
            Activity.UpdateGameState(GameState);
        }
        //endregion
    }

    public boolean ReplaceCard(int index, Player player) {

        GameCard newCard = CardDeck.remove(rand.nextInt(CardDeck.size()));
        CardDeck.add(player.getTakenCards().get(index));
        boolean result = player.TryReplaceCard(index, newCard);


        //region Notifier
        if (Activity != null && result)
            Activity.UpdateCards();
        //endregion

        return result;
    }

    public void EndGameLap() {
        boolean canContinue = false;
        if (Gamer.getReadyRate() > 0 && Gamer.getTakenCards().size() > 0)
            if (Enemy.getReadyRate() > 0 && Enemy.getTakenCards().size() > 0)
                canContinue = true;


        if (canContinue) {
            CardCombinations GamerCombination = AvailableCombinations.GetCombination(Gamer.getTakenCards());
            CardCombinations EnemyCombination = AvailableCombinations.GetCombination(Enemy.getTakenCards());

            int GamerCombinationLevel = GamerCombination.ordinal();
            int EnemyCombinationLevel = EnemyCombination.ordinal();

            Player winner = null;

            if (GamerCombinationLevel > EnemyCombinationLevel) {
                Rate.CollectResults(Gamer);
                winner = Gamer;
            } else if (GamerCombinationLevel < EnemyCombinationLevel) {
                Rate.CollectResults(Enemy);
                winner = Enemy;
            } else
                Rate.CollectResults(null);

            GameState = GameStates.RoundFinished;

            //region Notifier
            if (Activity != null) {
                if (Rate.getGamesLeft() == 0 || (Enemy.getScore() == 0 || Gamer.getScore() == 0)) {
                    GameState = GameStates.GameFinished;

                    if (Gamer.getScore() > Enemy.getScore())
                        winner = Gamer;
                    if (Gamer.getScore() < Enemy.getScore())
                        winner = Enemy;
                    if (Gamer.getScore() == Enemy.getScore())
                        winner = Enemy;

                    Activity.FinishGame(winner);
                } else
                    Activity.FinishRound(winner);

                Activity.UpdateCombinations(Gamer, GamerCombination);
                Activity.UpdateCombinations(Enemy, EnemyCombination);

                Activity.UpdateGameState(GameState);
            }
            //endregion

        }
    }

    public void StartNewGameLap() {
        CardDeck.addAll(Gamer.getTakenCards());
        CardDeck.addAll(Enemy.getTakenCards());

        Gamer.setTakenCards(null);
        Enemy.setTakenCards(null);

        Gamer.setReadyRate(0);
        Enemy.setReadyRate(0);

        GameState = GameStates.RoundInitial;

        if (Activity != null) {
            Activity.UpdateCards();
            Activity.UpdateGameState(GameState);
        }
    }

    //region InteractionHelpers

    public Random getRand() {
        return rand;
    }

    public Player getGamer() {
        return Gamer;
    }

    public Player getEnemy() {
        return Enemy;
    }

    public void setActivity(IActivityUpdatable activity) {
        Activity = activity;
    }

    public GameStates getGameState() {
        return GameState;
    }

    public int GetCurrentRound() {
        return Rate.getTotalGames() - Rate.getGamesLeft() + 1;
    }

    public boolean isCardsShared() {
        return Gamer.getTakenCards().size() > 0 && Enemy.getTakenCards().size() > 0;
    }

    public boolean isRateTaken() {
        return (Gamer.getReadyRate() > 0 && Enemy.getReadyRate() > 0);
    }
    //endregion

    //region Constructor

    private GameModel() {
        rand = new Random();

        CardDeck = new ArrayList<>();

        for (CardNames cardName : CardNames.values()) {
            for (CardSuit suit : CardSuit.values()) {
                if (suit != CardSuit.NONE && cardName != CardNames.Joker) {
                    CardDeck.add(new GameCard(cardName, suit));
                }
            }
        }


        AvailableCombinations = new GameCombinations();

        Gamer = new Player(1000);
        Enemy = new Player(1000);

        Rate = new GameRate(Arrays.asList(Gamer, Enemy), 10);
        GameState = GameStates.RoundInitial;

    }

    //endregion

}
