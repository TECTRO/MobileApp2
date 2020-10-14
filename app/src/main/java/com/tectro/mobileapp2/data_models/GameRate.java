package com.tectro.mobileapp2.data_models;

import java.util.List;

public class GameRate
{
    final List<Player> Players;
    private int GamesLeft;


    public void CollectResults(final Player winner)
    {
        int result = 0;
        for (Player plr : Players)
            if (plr != winner)
                result += plr.getReadyRate();

        final int finalResult = result;

        for (Player pl : Players)
            pl.CollectRate(winner, ()-> finalResult);

    }

    //region accessors
    public GameRate(List<Player> players, int totalGames) {
        Players = players;
        GamesLeft = totalGames;
    }

    public int getGamesLeft() {
        return GamesLeft;
    }
    //endregion
}
