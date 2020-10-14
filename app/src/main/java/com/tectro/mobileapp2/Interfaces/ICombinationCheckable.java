package com.tectro.mobileapp2.Interfaces;

import com.tectro.mobileapp2.data_models.GameCard;

import java.util.List;

public interface ICombinationCheckable {
    boolean isSuitable(List<GameCard> combination);
}
