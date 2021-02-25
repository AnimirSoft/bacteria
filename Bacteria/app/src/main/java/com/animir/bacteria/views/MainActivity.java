package com.animir.bacteria.views;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.animir.bacteria.R;
import com.animir.bacteria.presenter.MainConstance;
import com.animir.bacteria.presenter.MainPresenter;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements MainConstance.Views, View.OnClickListener{
    private MainPresenter mPresenter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        findViewById(R.id.winnerLayout).setVisibility(View.INVISIBLE);

        mPresenter = MainPresenter.getInstance();
        mPresenter.setGameMainLayout(findViewById(R.id.gameMainLayout), this::onClick);
        mPresenter.init(this, this, 7, 7);

        findViewById(R.id.orderP1).setVisibility(View.VISIBLE);
        findViewById(R.id.orderP2).setVisibility(View.INVISIBLE);


        String playerSwitch = mPresenter.getPlayerSwitch();
        if(playerSwitch.equals(mPresenter.getValueAI())){
            ((TextView)findViewById(R.id.textP2Name)).setText("AI : ");
            ((Switch)findViewById(R.id.playerAiSwitch)).setChecked(true);
        }else{
            ((TextView)findViewById(R.id.textP2Name)).setText("P2 : ");
            ((Switch)findViewById(R.id.playerAiSwitch)).setChecked(false);
        }

    }


    @Override
    public void showView(boolean firstPlayer, int[] gamePoints, boolean gamePlayFlag, boolean notMove) {


        // 게임 점수
        ((TextView)findViewById(R.id.textP1)).setText(String.valueOf(gamePoints[0]));
        ((TextView)findViewById(R.id.textP2)).setText(String.valueOf(gamePoints[1]));

        if(gamePlayFlag == false){
            // 게임 끝
            findViewById(R.id.winnerLayout).setVisibility(View.VISIBLE);

            if(notMove){
                ((TextView)findViewById(R.id.winnerText)).setText("Winner " + (mPresenter.getFirstPlayer() ? "P1" : "P2" + "\n" + "Not moveable") + " !!");
                Toast.makeText(this, "Not moveable", Toast.LENGTH_SHORT).show();
            }else{
                ((TextView)findViewById(R.id.winnerText)).setText("Winner " + (gamePoints[0] > gamePoints[1] ? "P1" : "P2") + " !!");
            }
            return;
        }

        // 게임 순서
        if(firstPlayer == false){
            findViewById(R.id.orderP1).setVisibility(View.VISIBLE);
            findViewById(R.id.orderP2).setVisibility(View.INVISIBLE);
        }else{
            findViewById(R.id.orderP1).setVisibility(View.INVISIBLE);
            findViewById(R.id.orderP2).setVisibility(View.VISIBLE);
        }
    }



    @OnClick({R.id.btnReSet, R.id.btnGameStart})
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.btnReSet:
                findViewById(R.id.winnerLayout).setVisibility(View.INVISIBLE);
                mPresenter.gameReSet();
                btnClickables(true);
                break;
            case R.id.btnGameStart:
                if(!mPresenter.getStartGame()){
                    Toast.makeText(this, "Start Game!!",Toast.LENGTH_SHORT).show();
                    mPresenter.setStartGame(true);
                    btnClickables(false);
                }
                break;
            default:
                if(mPresenter.getStartGame())
                    mPresenter.OnClicker(view);
                break;
        }
    }

    @OnCheckedChanged(R.id.playerAiSwitch)
    void onChecked(boolean checked) {
        if(checked){
            ((TextView)findViewById(R.id.textP2Name)).setText("AI : ");
            mPresenter.setPlayerSwitch(mPresenter.getValueAI());
        }else{
            ((TextView)findViewById(R.id.textP2Name)).setText("P2 : ");
            mPresenter.setPlayerSwitch(mPresenter.getValuePlayer());
        }
    }

    private void btnClickables(boolean clickable){
        findViewById(R.id.btnGameStart).setEnabled(clickable);
        findViewById(R.id.playerAiSwitch).setClickable(clickable);
    }
}
