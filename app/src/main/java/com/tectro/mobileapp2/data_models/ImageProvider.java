package com.tectro.mobileapp2.data_models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.appcompat.app.AppCompatActivity;

import com.tectro.mobileapp2.R;
import com.tectro.mobileapp2.enums.CardColors;
import com.tectro.mobileapp2.enums.CardNames;

public class ImageProvider {

    public ImageProvider(AppCompatActivity activity) {
        OriginalImage = BitmapFactory.decodeResource(activity.getResources(),
                R.drawable.game_cards_source);

        cardWidth = OriginalImage.getWidth() / 13.0;
        cardHeight = OriginalImage.getHeight() / 5.0;
    }

    private Bitmap OriginalImage;
    private final double cardWidth;
    private final double cardHeight;

    public Bitmap GetCardImage(GameCard card) {


        if (card.Name != CardNames.Joker)
            return Bitmap.createBitmap(OriginalImage, ((int) cardWidth * card.Name.ordinal()), ((int) cardHeight * card.Suit.ordinal()), (int) cardWidth, (int) cardHeight);

        if (card.Color == CardColors.Black)
            return Bitmap.createBitmap(OriginalImage, 0, ((int) cardHeight * 4), (int) cardWidth, (int) cardHeight);
        else
            return Bitmap.createBitmap(OriginalImage, ((int) cardWidth), ((int) cardHeight * 4), (int) cardWidth, (int) cardHeight);
    }

    Paint bluePint = new Paint();

    public Bitmap GetCardImage(GameCard card, boolean isSelected) {

        Bitmap CardImage;

        if (card.Name != CardNames.Joker)
            CardImage = Bitmap.createBitmap(OriginalImage, ((int) cardWidth * card.Name.ordinal()), ((int) cardHeight * card.Suit.ordinal()), (int) cardWidth, (int) cardHeight);
        else
        if (card.Color == CardColors.Black)
            CardImage = Bitmap.createBitmap(OriginalImage, 0, ((int) cardHeight * 4), (int) cardWidth, (int) cardHeight);
        else
            CardImage = Bitmap.createBitmap(OriginalImage, ((int) cardWidth), ((int) cardHeight * 4), (int) cardWidth, (int) cardHeight);

        if (isSelected) {
            double BorderWidth = (cardWidth * 1.03);
            double BorderHeight = (cardHeight * 1.03);

            Bitmap result = Bitmap.createBitmap((int) BorderWidth, (int) BorderHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            bluePint.setColor(Color.rgb(128,0,0));
            canvas.drawRoundRect(0, 0, canvas.getWidth(), canvas.getHeight(), 40,40,bluePint);
            canvas.drawBitmap(CardImage, (int) ((BorderWidth - cardWidth) / 2.0), (int) ((BorderHeight- cardHeight) / 2.0), bluePint);
            return result;
        }

        return CardImage;
    }

    public Bitmap GetCardBack() {
        return Bitmap.createBitmap(OriginalImage, ((int) cardWidth * 2), ((int) cardHeight * 4), (int) cardWidth, (int) cardHeight);
    }

}
