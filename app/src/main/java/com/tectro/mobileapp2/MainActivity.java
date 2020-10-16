package com.tectro.mobileapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tectro.mobileapp2.Interfaces.IActivityUpdatable;
import com.tectro.mobileapp2.data_models.GameCard;
import com.tectro.mobileapp2.data_models.ImageProvider;
import com.tectro.mobileapp2.data_models.Player;
import com.tectro.mobileapp2.enums.CardCombinations;
import com.tectro.mobileapp2.enums.GameStates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements IActivityUpdatable {

    ArrayList<View> GamerCardsPreview;
    ArrayList<View> EnemyCardsPreview;
    GameModel Model;
    ImageProvider Provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Provider = new ImageProvider(this);
        Model = GameModel.CreateInstance();
        Model.setActivity(this);

        EnemyCardsPreview = new ArrayList<>(Arrays.asList(
                (View) findViewById(R.id.secondLeftCard1),
                (View) findViewById(R.id.firstLeftCard1),
                (View) findViewById(R.id.centerCard1),
                (View) findViewById(R.id.firstRightCard1),
                (View) findViewById(R.id.secondRightCard1)
        ));

        GamerCardsPreview = new ArrayList<>(Arrays.asList(
                (View) findViewById(R.id.secondLeftCard),
                (View) findViewById(R.id.firstLeftCard),
                (View) findViewById(R.id.centerCard),
                (View) findViewById(R.id.firstRightCard),
                (View) findViewById(R.id.secondRightCard)
        ));

        EndOfReplacementToast = Toast.makeText(this, "Вы исчерпали количество замен", Toast.LENGTH_SHORT);

        UpdateGameState(Model.getGameState());
        UpdateCards();
    }


    //region Обработчики комманд
    GameCard selectedCard = null;
    int selectedCardIndex = 0;
    private Toast EndOfReplacementToast;

    public void SelectCard(View v) {
        int selectedPreview = GamerCardsPreview.indexOf(v);

        boolean isUpdated = false;
        if (Model.getGamer().getTakenCards().size() > selectedPreview) {
            if (selectedCard != Model.getGamer().getTakenCards().get(selectedPreview)) {

                selectedCardIndex = selectedPreview;
                selectedCard = Model.getGamer().getTakenCards().get(selectedPreview);
                isUpdated = true;
            }
        }
        if (!isUpdated)
            selectedCard = null;

        UpdateCards();
    }

    public void ReplaceCard(View v) {
        if (selectedCard != null)
            if (!Model.ReplaceCard(selectedCardIndex, Model.getGamer()))
                EndOfReplacementToast.show();

        selectedCard = null;
        UpdateCards();
    }

    public void ShareCards(View v) {
        Model.ShareCards();
    }

    public void SharePlayerRate(View v) {
        try {
            EditText textHolder = (EditText) findViewById(R.id.PlayerRateHolder);
            Model.UpdateRate(Model.getGamer(), Integer.parseInt(textHolder.getText().toString()));
            Model.UpdateRate(Model.getEnemy(), Model.getRand().nextInt(500));
            textHolder.setText("");

            //region скрыть клавиатуру
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            //endregion

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void EndGameRound(View v) {
        Model.EndGameLap();
    }

    public void NextGameRound(View v) {
        Model.StartNewGameLap();
    }

    //endregion

    //region Обработчики событий
    @Override
    public void FinishGame(Player winner) {
        Toast.makeText(this, (Model.getGamer() == winner ? "Вы победили" : (winner == null ? "У вас ничья" : "Вы проиграли")), Toast.LENGTH_LONG).show();
    }

    @Override
    public void FinishRound(Player winner) {
        ((TextView) findViewById(R.id.WinnerInfo)).setText(
                winner == null ?
                        "У вас ничья, может повезет в следующем раунде" :
                        (winner == Model.getGamer() ?
                                "Поздравляю, раунд за вами" :
                                "Как вы смогли проиграть компьютеру? Ну вы даете."
                        )
        );
    }

    @Override
    public void UpdateCards() {
        List<GameCard> GamerCards = Model.getGamer().getTakenCards();
        List<GameCard> EnemyCards = Model.getEnemy().getTakenCards();

        for (int i = 0; i < 5; i++) {
            GameCard currentG = null;
            if (i < GamerCards.size())
                currentG = GamerCards.get(i);

            if (currentG != null) {
                if (currentG == selectedCard)
                    ((ImageView) GamerCardsPreview.get(i)).setImageBitmap(Provider.GetCardImage(currentG, true));
                else
                    ((ImageView) GamerCardsPreview.get(i)).setImageBitmap(Provider.GetCardImage(currentG));

            } else
                ((ImageView) GamerCardsPreview.get(i)).setImageBitmap(Provider.GetCardBack());

            GameCard currentE = null;
            if (i < EnemyCards.size())
                currentE = EnemyCards.get(i);

            if (currentE != null)
                ((ImageView) EnemyCardsPreview.get(i)).setImageBitmap(Provider.GetCardImage(currentE));
            else
                ((ImageView) EnemyCardsPreview.get(i)).setImageBitmap(Provider.GetCardBack());

        }
    }

    @Override
    public void UpdateScore() {
        ((TextView) findViewById(R.id.PlayerScoreInfo)).setText(String.valueOf(Model.getGamer().getScore()));
        ((TextView) findViewById(R.id.EnemyScoreInfo)).setText(String.valueOf(Model.getEnemy().getScore()));
    }

    @Override
    public void UpdateRates() {
        ((TextView) findViewById(R.id.PlayerRateInfo)).setText(String.valueOf(Model.getGamer().getReadyRate()));
        ((TextView) findViewById(R.id.EnemyRateInfo)).setText(String.valueOf(Model.getEnemy().getReadyRate()));
        ((TextView) findViewById(R.id.GameRoundInfo)).setText(String.valueOf(Model.GetCurrentRound()));
    }

    @Override
    public void UpdateCombinations(Player player, CardCombinations combination) {
        if(player == Model.getEnemy())
            ((TextView)findViewById(R.id.EnemyCombinationInfo)).setText(combination.toString().replace('_',' '));

        if(player == Model.getGamer())
            ((TextView)findViewById(R.id.PlayerCombinationInfo)).setText(combination.toString().replace('_',' '));
    }

    @Override
    public void UpdateGameState(GameStates gameState) {
        UpdateRates();
        UpdateScore();
        UpdateCards();

        if (gameState != null)
            switch (gameState) {
                case RoundInitial: {
                    findViewById(R.id.EnemyCardsLayout).setVisibility(View.GONE);

                    findViewById(R.id.RateInfoLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.WinnerInfoLayout).setVisibility(View.GONE);

                    findViewById(R.id.ReplaceCardBtn).setVisibility(View.GONE);
                    findViewById(R.id.EndGameStepBtn).setVisibility(View.GONE);
                    findViewById(R.id.ShareCardsBtn).setVisibility(View.VISIBLE);

                    findViewById(R.id.PlayerRateLayout).setVisibility(View.GONE);
                    findViewById(R.id.PlayerNavigationLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.PlayerRoundFinishedLayout).setVisibility(View.GONE);

                }
                break;
                case SharedCards: {
                    findViewById(R.id.EnemyCardsLayout).setVisibility(View.GONE);

                    findViewById(R.id.RateInfoLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.WinnerInfoLayout).setVisibility(View.GONE);

                    findViewById(R.id.ReplaceCardBtn).setVisibility(View.GONE);
                    findViewById(R.id.EndGameStepBtn).setVisibility(View.GONE);
                    findViewById(R.id.ShareCardsBtn).setVisibility(View.GONE);

                    findViewById(R.id.PlayerRateLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.PlayerNavigationLayout).setVisibility(View.GONE);
                    findViewById(R.id.PlayerRoundFinishedLayout).setVisibility(View.GONE);
                }
                break;
                case RatesTaken: {
                    findViewById(R.id.EnemyCardsLayout).setVisibility(View.GONE);

                    findViewById(R.id.RateInfoLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.WinnerInfoLayout).setVisibility(View.GONE);

                    findViewById(R.id.ReplaceCardBtn).setVisibility(View.VISIBLE);
                    findViewById(R.id.EndGameStepBtn).setVisibility(View.VISIBLE);
                    findViewById(R.id.ShareCardsBtn).setVisibility(View.GONE);

                    findViewById(R.id.PlayerRateLayout).setVisibility(View.GONE);
                    findViewById(R.id.PlayerNavigationLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.PlayerRoundFinishedLayout).setVisibility(View.GONE);
                }
                break;
                case RoundFinished: {
                    findViewById(R.id.EnemyCardsLayout).setVisibility(View.VISIBLE);

                    findViewById(R.id.RateInfoLayout).setVisibility(View.GONE);
                    findViewById(R.id.WinnerInfoLayout).setVisibility(View.VISIBLE);

                    findViewById(R.id.ReplaceCardBtn).setVisibility(View.GONE);
                    findViewById(R.id.EndGameStepBtn).setVisibility(View.GONE);
                    findViewById(R.id.ShareCardsBtn).setVisibility(View.GONE);

                    findViewById(R.id.PlayerRateLayout).setVisibility(View.GONE);
                    findViewById(R.id.PlayerNavigationLayout).setVisibility(View.GONE);
                    findViewById(R.id.PlayerRoundFinishedLayout).setVisibility(View.VISIBLE);
                }
                break;
                case GameFinished: {
                    findViewById(R.id.EnemyCardsLayout).setVisibility(View.VISIBLE);

                    findViewById(R.id.RateInfoLayout).setVisibility(View.GONE);
                    findViewById(R.id.WinnerInfoLayout).setVisibility(View.VISIBLE);

                    findViewById(R.id.ReplaceCardBtn).setVisibility(View.GONE);
                    findViewById(R.id.EndGameStepBtn).setVisibility(View.GONE);
                    findViewById(R.id.ShareCardsBtn).setVisibility(View.GONE);

                    findViewById(R.id.PlayerRateLayout).setVisibility(View.GONE);
                    findViewById(R.id.PlayerNavigationLayout).setVisibility(View.GONE);
                    findViewById(R.id.PlayerRoundFinishedLayout).setVisibility(View.GONE);
                }
                break;
            }
    }

    //endregion
}