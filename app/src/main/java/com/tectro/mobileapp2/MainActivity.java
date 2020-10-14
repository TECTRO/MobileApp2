package com.tectro.mobileapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.tectro.mobileapp2.Interfaces.IActivityUpdatable;
import com.tectro.mobileapp2.data_models.GameCard;
import com.tectro.mobileapp2.data_models.ImageProvider;
import com.tectro.mobileapp2.data_models.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        UpdateButtons();
        UpdateCards();
    }

    public void UpdateButtons() {

        if (!Model.isCardsShared()) {
            findViewById(R.id.ReplaceCardBtn).setVisibility(View.GONE);
            findViewById(R.id.EndGameStepBtn).setVisibility(View.GONE);
            findViewById(R.id.ShareCardsBtn).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.ReplaceCardBtn).setVisibility(View.VISIBLE);
            findViewById(R.id.EndGameStepBtn).setVisibility(View.VISIBLE);
            findViewById(R.id.ShareCardsBtn).setVisibility(View.GONE);
        }

    }

    GameCard selectedCard = null;
    int selectedCardIndex = 0;
    public void SelectCard(View v) {
        int selectedPreview = GamerCardsPreview.indexOf(v);
        if (Model.getGamer().getTakenCards().size() > selectedPreview) {
            if (selectedCard == Model.getGamer().getTakenCards().get(selectedPreview))
                selectedCard = null;
            else{
                selectedCardIndex = selectedPreview;
                selectedCard = Model.getGamer().getTakenCards().get(selectedPreview);
            }
        } else
            selectedCard = null;

        UpdateCards();
    }

    public void ReplaceCard(View v) {
        if(selectedCard!=null)
        {
            Model.ReplaceCard(selectedCardIndex,Model.getGamer());
            selectedCard = null;
            UpdateCards();
        }
    }

    public void ShareCards(View v) {
        Model.ShareCards();
        UpdateButtons();
    }

    @Override
    public void FinishGame(Player winner) {

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

    }

    @Override
    public void UpdateRates() {

    }
}