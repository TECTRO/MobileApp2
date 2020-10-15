package com.tectro.mobileapp2.Interfaces;

import com.tectro.mobileapp2.data_models.Player;
import com.tectro.mobileapp2.enums.GameStates;

public interface IActivityUpdatable {
    void FinishGame(Player winner);
    void FinishRound(Player winner);
    void UpdateCards();
    void UpdateScore();
    void UpdateRates();
    void UpdateGameState(GameStates gameState);
}
