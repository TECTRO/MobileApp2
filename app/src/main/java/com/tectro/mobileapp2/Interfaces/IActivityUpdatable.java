package com.tectro.mobileapp2.Interfaces;

import com.tectro.mobileapp2.data_models.Player;

public interface IActivityUpdatable {
    void FinishGame(Player winner);
    void UpdateCards();
    void UpdateScore();
    void UpdateRates();
}
