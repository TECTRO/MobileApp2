package com.tectro.mobileapp2;

import com.tectro.mobileapp2.Interfaces.IActivityUpdatable;
import com.tectro.mobileapp2.Interfaces.ICombinationCheckable;
import com.tectro.mobileapp2.data_models.GameCard;
import com.tectro.mobileapp2.data_models.GameRate;
import com.tectro.mobileapp2.data_models.Player;
import com.tectro.mobileapp2.enums.CardColors;
import com.tectro.mobileapp2.enums.CardNames;
import com.tectro.mobileapp2.enums.CardSuit;
import com.tectro.mobileapp2.enums.GameStates;

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

    private GameStates GameState;

    private IActivityUpdatable Activity;

    private Random rand;

    private List<ICombinationCheckable> Combinations;
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
    private int GetCombinationLevel(Player player) {

        int result = 0;
        ArrayList<GameCard> playerCards = player.getTakenCards();
        for (int i = 0; i < Combinations.size(); i++) {
            if(Combinations.get(i).isSuitable(playerCards))
                result = i;
        }

        return result;
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

        GameState = GameStates.RoundInitial;
        ;
        //todo добавить комбинации первая слабая последняя сильная
        Combinations = Arrays.asList(
                //старшая карта
                (combination) -> {
                    boolean HigherCard = true;
                    for (GameCard card1 : combination) {
                        for (GameCard card2 : combination) {
                            if (card1 != card2) {
                                if (card1.Name == card2.Name) {
                                    HigherCard = false;
                                    break;
                                }
                            }
                        }
                    }

                    return HigherCard;
                },
                //пара
                (combination) -> {
                    int repeatCount = 0;
                    for (int i = 0; i < combination.size(); i++) {
                        for (int j = i + 1; j < combination.size(); j++) {
                            GameCard card1 = combination.get(i);
                            GameCard card2 = combination.get(j);
                            if (card1 != card2) {
                                if (card1.Name == card2.Name) {
                                    repeatCount++;
                                }
                            }
                        }
                    }

                    return repeatCount == 1;
                },
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
                (combination) -> {

                    int maxCounter = 0;
                    for (GameCard card1 : combination) {
                        int counter = 0;
                        for (GameCard card2 : combination) {
                            if (card1 != card2) {
                                if (card1.Name == card2.Name) {
                                    counter++;
                                }
                            }
                        }
                        if (counter > maxCounter) maxCounter = counter;
                    }
                    return maxCounter == 2;
                },
                //стрит
                (combination) -> {

                    int street = 0;
                    for (GameCard card : combination) {
                        if (card.Name == CardNames.Seven) street++;
                        if (card.Name == CardNames.Eight) street++;
                        if (card.Name == CardNames.Nine) street++;
                        if (card.Name == CardNames.Ten) street++;
                        if (card.Name == CardNames.Joker) street++;
                    }
                    return street == 5;
                },
                //флеш
                (combination) -> {
                    boolean flash = true;

                    for (GameCard card1 : combination) {
                        for (GameCard card2 : combination) {
                            if (card1 != card2) {
                                if (card1.Suit != card2.Suit) {
                                    if (card1.Name == CardNames.Joker || card2.Name == CardNames.Joker) {
                                        if (card1.Color != card2.Color) {
                                            flash = false;
                                            break;
                                        }
                                    } else {
                                        flash = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    return flash;
                },
                //Фулл хаус
                (combination) -> {

                    ArrayList<GameCard> Cards = new ArrayList<>(combination);
                    ArrayList<Integer> removed = new ArrayList<>();
                    for (int i = 0; i < Cards.size(); i++) {
                        ArrayList<Integer> Indexes = new ArrayList<>();
                        Indexes.add(i);
                        for (int j = 0; j < combination.size(); j++) {
                            GameCard card1 = combination.get(i);
                            GameCard card2 = combination.get(j);
                            if (card1 != card2) {
                                if (card1.Name == card2.Name) {
                                    Indexes.add(j);
                                }
                            }
                        }
                        for (Integer val : Indexes)
                            Cards.remove(val.intValue());
                        i--;
                        removed.add(Indexes.size());
                    }


                    return removed.contains(3) && removed.contains(2);
                },
                //каре
                (combination) -> {
                    ArrayList<GameCard> Cards = new ArrayList<>(combination);
                    ArrayList<Integer> removed = new ArrayList<>();
                    for (int i = 0; i < Cards.size(); i++) {
                        ArrayList<Integer> Indexes = new ArrayList<>();
                        Indexes.add(i);
                        for (int j = 0; j < combination.size(); j++) {
                            GameCard card1 = combination.get(i);
                            GameCard card2 = combination.get(j);
                            if (card1 != card2) {
                                if (card1.Name == card2.Name) {
                                    Indexes.add(j);
                                }
                            }
                        }
                        for (Integer val : Indexes)
                            Cards.remove(val.intValue());
                        i--;
                        removed.add(Indexes.size());
                    }


                    return removed.contains(4);
                },
                //стрит флеш
                (combination) -> {

                    int street = 0;
                    for (GameCard card : combination) {
                        if (card.Name == CardNames.Seven) street++;
                        if (card.Name == CardNames.Eight) street++;
                        if (card.Name == CardNames.Nine) street++;
                        if (card.Name == CardNames.Ten) street++;
                        if (card.Name == CardNames.Joker) street++;
                    }

                    boolean flash = true;

                    for (GameCard card1 : combination) {
                        for (GameCard card2 : combination) {
                            if (card1 != card2) {
                                if (card1.Suit != card2.Suit) {
                                    if (card1.Name == CardNames.Joker || card2.Name == CardNames.Joker) {
                                        if (card1.Color != card2.Color) {
                                            flash = false;
                                            break;
                                        }
                                    } else {
                                        flash = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }


                    return street == 5 && flash;
                },
                //стрит роял
                (combination) -> {

                    int street = 0;
                    for (GameCard card : combination) {
                        if (card.Name == CardNames.Ten) street++;
                        if (card.Name == CardNames.Joker) street++;
                        if (card.Name == CardNames.Queen) street++;
                        if (card.Name == CardNames.King) street++;
                        if (card.Name == CardNames.Jack) street++;
                    }

                    boolean flash = true;

                    for (GameCard card1 : combination) {
                        for (GameCard card2 : combination) {
                            if (card1 != card2) {
                                if (card1.Suit != card2.Suit) {
                                    if (card1.Name == CardNames.Joker || card2.Name == CardNames.Joker) {
                                        if (card1.Color != card2.Color) {
                                            flash = false;
                                            break;
                                        }
                                    } else {
                                        flash = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }


                    return street == 5 && flash;
                }

        );

    }

    public void setActivity(IActivityUpdatable activity) {
        Activity = activity;
    }

    public GameStates getGameState() {
        return GameState;
    }
    //endregion

}
