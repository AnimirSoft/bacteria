package com.animir.bacteria.presenter;

import android.view.View;

public interface MainConstance {

    interface Views{
        void showView(boolean firstPlayer, int[] gamePoints, boolean gamePlayFlag, boolean notMove);
    }

    interface Presenter{
        void gameReSet();
        void OnClicker(View view);
    }
}
