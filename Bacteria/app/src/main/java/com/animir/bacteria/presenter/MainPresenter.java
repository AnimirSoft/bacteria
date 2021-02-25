package com.animir.bacteria.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import com.animir.bacteria.R;
import com.animir.bacteria.model.MainModel;
import java.util.ArrayList;
import java.util.HashMap;

public class MainPresenter implements MainConstance.Presenter{

    private boolean gamePlayFlag = true;
    private boolean startGame = false;
    private boolean aiPlayFlag = false;

    private boolean firstPlayer = false;
    private static MainPresenter mPresenter = null;
    private static MainModel mModel = null;

    private int[][] gameBoard;

    private LinearLayout mGameMainLayout = null;
    private View.OnClickListener mClickListener = null;
    private MainConstance.Views mInterfaceViews = null;
    private Context mContext = null;
    private int mBoardX, mBoardY;

    boolean move1CheckFlag = false, move2CheckFlag = false;

    private ArrayList<HashMap<String, Integer>> area1List = null;
    private ArrayList<HashMap<String, Integer>> area2List = null;

    private int firstX = -1, firstY = -1;

    private int aiPlayerCheck = 2;

    private int aiMoveX, aiMoveY;
    private final int AIHANDLER_START = 100;
    private final int AIHANDLER_POINTCHECK1 = 200;
    private final int AIHANDLER_POINTCHECK2 = 300;
    private final int AIHANDLER_END = 400;

    private final int AIHANDLER_TIME = 500;

    private MainPresenter() {}
    public static MainPresenter getInstance(){
        if(mPresenter == null) {
            mPresenter = new MainPresenter();
            mModel = MainModel.getInstance();
        }
        return mPresenter;
    }

    public void setGameMainLayout(LinearLayout gameMainLayout, View.OnClickListener clickListener){
        mGameMainLayout = gameMainLayout;
        mClickListener = clickListener;
    }

    public void init(Context context, MainConstance.Views interfaceViews, int boardX, int boardY){
        firstPlayer = false;
        startGame = false;
        mContext = context;
        mInterfaceViews = interfaceViews;
        mBoardX = boardX;
        mBoardY = boardY;
        gameBoard = new int[mBoardX][mBoardY];

        int[] gamePointsTmp = new int[2];
        gamePointsTmp[0] = 0;
        gamePointsTmp[1] = 0;

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int screenX = display.getWidth();
        int screenY = display.getHeight();

        int size = ((((screenX > screenY ? screenY : screenX) / 100) * 90) / 7);


        for(int i = 0; i < mBoardX; i++){
            LinearLayout childLayout = new LinearLayout(mContext);
            for(int i2 = 0; i2 < mBoardY; i2++){
                Button btn = new Button(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size,size);
                params.setMargins(1,1,1,1);
                btn.setLayoutParams(params);
                btn.setTag(i + "," + i2);
                btn.setOnClickListener(mClickListener);

                int color = 0;
                gameBoard[i][i2] = 0;
                btn.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary2));

                // P1 세팅
                if(((i == 0 && i2 == 0) || (i == 0 && i2 == 1) || (i == 1 && i2 == 0) || (i == 1 && i2 == 1))
                || ((i == mBoardX-1 && i2 == mBoardY-1) || (i == mBoardX-1 && i2 == mBoardY-2)
                        || (i == mBoardX-2 && i2 == mBoardY-1) || (i == mBoardX-2 && i2 == mBoardY-2))){
                    btn.setBackgroundColor(mContext.getResources().getColor(R.color.colorPlayer));
                    gameBoard[i][i2] = 1;
                }

                // P2 세팅
                if(((i == 0 && i2 == mBoardY-1) || (i == 0 && i2 == mBoardY-2) || (i == 1 && i2 == mBoardY-1) || (i == 1 && i2 == mBoardY-2))
                ||((i == mBoardX-1 && i2 == 0) || (i == mBoardX-1 && i2 == 1) || (i == mBoardX-2 && i2 == 0) || (i == mBoardX-2 && i2 == 1))){
                    btn.setBackgroundColor(mContext.getResources().getColor(R.color.colorAi));
                    gameBoard[i][i2] = 2;
                }

                // 게임 초기 점수
                if(gameBoard[i][i2] == 1){
                    gamePointsTmp[0] += 1;
                }else if(gameBoard[i][i2] == 2){
                    gamePointsTmp[1] += 1;
                }

                childLayout.addView(btn);

            }
            mGameMainLayout.addView(childLayout);

            mInterfaceViews.showView(firstPlayer, gamePointsTmp, gamePlayFlag, false);
        }

    }


    @Override
    public void gameReSet() {
        startGame = false;
        gamePlayFlag = true;
        firstPlayer = false;
        move1CheckFlag = false;
        move2CheckFlag = false;
        firstX = -1;
        firstY = -1;

        mGameMainLayout.removeAllViews();

        init(mContext, mInterfaceViews, mBoardX, mBoardY);
    }

    @Override
    public void OnClicker(View view) {
        if(gamePlayFlag){
            String tag = view.getTag().toString();
            String[] tmp = tag.split(",");
            int x = Integer.parseInt(tmp[0]);
            int y = Integer.parseInt(tmp[1]);

            int player = gameBoard[x][y];

            if(firstPlayer == false){
                // P1
                nonSelectColorReSet("Player1");

                if(player == 1){
                    move1CheckFlag = selectMove1Area(x, y, R.color.colorWhite);
                    move2CheckFlag = selectMove2Area(x, y, R.color.colorWhite2);
                    firstX = x;
                    firstY = y;
                }

                // 이동 가능한 경우
                if(player == 0 && (move1CheckFlag || move2CheckFlag)){

                    boolean move = false;

                    // 1칸 증식
                    for(int i1 = 0; i1 < area1List.size(); i1++){
                        HashMap<String, Integer> map = area1List.get(i1);
                        int mapX = map.get("X");
                        int mapY = map.get("Y");

                        if(x == mapX && y == mapY){
                            move = true;
                            gameBoard[mapX][mapY] = 1;
                            selectMoveColor(mapX, mapY, R.color.colorPlayer);
                            break;
                        }
                    }

                    if(move == false){
                        // 2 칸 이동
                        for(int i2 = 0; i2 < area2List.size(); i2++){
                            HashMap<String, Integer> map = area2List.get(i2);
                            int mapX = map.get("X");
                            int mapY = map.get("Y");

                            if(x == mapX && y == mapY){
                                move = true;
                                gameBoard[mapX][mapY] = 1;
                                selectMoveColor(mapX, mapY, R.color.colorPlayer);
                                gameBoard[firstX][firstY] = 0;
                                Log.d("#@#", "OnClicker 1p 2 firstX : " + firstX + " | firstY : " + firstX);
                                selectMoveColor(firstX, firstY, R.color.colorPrimary2);
                                break;
                            }
                        }
                    }

                    if(move){
                        firstX = -1;
                        firstY = -1;

                        move1CheckFlag = false;
                        move2CheckFlag = false;

                        gamePlayCheck(x, y);
                    }
                }
            }else {
                // P2
                if(mModel.getStringPref(mContext, mModel.getKeyPlayer()).equals(mModel.getValuePlayer())){
                    nonSelectColorReSet("Player2");

                    if(player == 2){
                        move1CheckFlag = selectMove1Area(x, y, R.color.colorWhite);
                        move2CheckFlag = selectMove2Area(x, y, R.color.colorWhite2);
                        firstX = x;
                        firstY = y;
                    }

                    if(player == 0 && (move1CheckFlag || move2CheckFlag)){

                        boolean move = false;

                        // 1칸 증식
                        for(int i1 = 0; i1 < area1List.size(); i1++){
                            HashMap<String, Integer> map = area1List.get(i1);
                            int mapX = map.get("X");
                            int mapY = map.get("Y");

                            if(x == mapX && y == mapY){
                                move = true;
                                gameBoard[mapX][mapY] = 2;
                                selectMoveColor(mapX, mapY, R.color.colorAi);
                                break;
                            }
                        }

                        if(move == false){
                            // 2 칸 이동
                            for(int i2 = 0; i2 < area2List.size(); i2++){
                                HashMap<String, Integer> map = area2List.get(i2);
                                int mapX = map.get("X");
                                int mapY = map.get("Y");

                                if(x == mapX && y == mapY){
                                    move = true;
                                    gameBoard[mapX][mapY] = 2;
                                    selectMoveColor(mapX, mapY, R.color.colorAi);
                                    gameBoard[firstX][firstY] = 0;
                                    Log.d("#@#", "OnClicker 2p 2 firstX : " + firstX + " | firstY : " + firstX);
                                    selectMoveColor(firstX, firstY, R.color.colorPrimary2);
                                    break;
                                }
                            }
                        }

                        if(move){
                            firstX = -1;
                            firstY = -1;

                            move1CheckFlag = false;
                            move2CheckFlag = false;

                            gamePlayCheck(x, y);
                        }
                    }
                }
            }
        }
        //Toast.makeText(mContext, tag, Toast.LENGTH_SHORT).show();
    }

    private void gamePlayCheck(int x, int y){

        // 컬러, 플레이어 순서 체크
        firstPlayer = !firstPlayer;

        //TODO: x,y 좌표가 변경되는 이슈있음
        //게임 판정
        int[] gamePoints = gameRuleCheck(x, y);

        Log.d("#@#", "X : " + x + " | Y : " + y);

        boolean area1Check = false, area2Check = false;
        gamePlayFlag = false;
        boolean notMove = false;

        // 이동 불가능한 지역이 있는지 체크
        for(int i = 0; i < mBoardX; i++) {
            for (int i2 = 0; i2 < mBoardY; i2++) {
                if(gameBoard[i][i2] == 0){

                    area1Check = selectMove1AreaCheck(i, i2, (!firstPlayer ? 1 : 2));
                    area2Check = selectMove2AreaCheck(i, i2, (!firstPlayer ? 1 : 2));

                    if(area1Check || area2Check){
                        notMove = true;
                        gamePlayFlag = true;
                        break;
                    }
                }
            }
        }

        Log.d("#@#", "게임가능여부 : " + String.valueOf(gamePlayFlag));

        mInterfaceViews.showView(firstPlayer, gamePoints, gamePlayFlag, notMove);

        if(gamePlayFlag && getPlayerSwitch().equalsIgnoreCase(getValueAI()) && firstPlayer) {
            aiMoveX = -1;
            aiMoveY = -1;
            aiPlayerCheck = 2;
            aiHandler.sendEmptyMessageDelayed(AIHANDLER_START, 0);
        }
    }

    private boolean selectMove1Area(int x, int y, int color){
        boolean moveCheckFlag = false;
        area1List = new ArrayList<>();
        if(y+1 < mBoardY){
            if(gameBoard[x][y+1] == 0){
                selectMoveColor(x, y+1, color);
                area1List.add(getMoveAreaPosition(x,y+1));
                moveCheckFlag = true;
            }
        }
        if(y-1 != -1){
            if(gameBoard[x][y-1] == 0) {
                selectMoveColor(x, y - 1, color);
                area1List.add(getMoveAreaPosition(x,y-1));
                moveCheckFlag = true;
            }
        }
        if(x-1 != -1){
            if(gameBoard[x-1][y] == 0) {
                selectMoveColor(x - 1, y, color);
                area1List.add(getMoveAreaPosition(x-1,y));
                moveCheckFlag = true;
            }
        }
        if(x+1 < mBoardX){
            if(gameBoard[x+1][y] == 0) {
                selectMoveColor(x + 1, y, color);
                area1List.add(getMoveAreaPosition(x+1,y));
                moveCheckFlag = true;
            }
        }
        if(x+1 < mBoardX && y+1 < mBoardY){
            if(gameBoard[x+1][y+1] == 0) {
                selectMoveColor(x + 1, y + 1, color);
                area1List.add(getMoveAreaPosition(x+1,y+1));
                moveCheckFlag = true;
            }
        }
        if(x-1 != -1 && y-1 != -1){
            if(gameBoard[x-1][y-1] == 0) {
                selectMoveColor(x - 1, y - 1, color);
                area1List.add(getMoveAreaPosition(x-1,y-1));
                moveCheckFlag = true;
            }
        }

        if(x-1 != -1 && y+1 < mBoardY){
            if(gameBoard[x-1][y+1] == 0) {
                selectMoveColor(x - 1, y + 1, R.color.colorWhite);
                area1List.add(getMoveAreaPosition(x-1,y+1));
                moveCheckFlag = true;
            }
        }
        if(x+1 < mBoardX && y-1 != -1){
            if(gameBoard[x+1][y-1] == 0) {
                selectMoveColor(x + 1, y - 1, R.color.colorWhite);
                area1List.add(getMoveAreaPosition(x+1,y-1));
                moveCheckFlag = true;
            }
        }
        return moveCheckFlag;
    }

    private boolean selectMove2Area(int x, int y, int color){
        boolean moveCheckFlag = false;
        area2List = new ArrayList<>();

        if(y+2 < mBoardY){ //오른쪽
            if(gameBoard[x][y+2] == 0){
                selectMoveColor(x, y+2, color);
                area2List.add(getMoveAreaPosition(x,y+2));
                moveCheckFlag = true;
            }
        }
        if(x-1 > -1 && y+2 < mBoardY){ // 오른쪽 위 1
            if(gameBoard[x-1][y+2] == 0){
                selectMoveColor(x-1, y+2, color);
                area2List.add(getMoveAreaPosition(x-1,y+2));
                moveCheckFlag = true;
            }
        }
        if(x-2 > -1 && y+2 < mBoardY){ // 오른쪽 위 2
            if(gameBoard[x-2][y+2] == 0){
                selectMoveColor(x-2, y+2, color);
                area2List.add(getMoveAreaPosition(x-2,y+2));
                moveCheckFlag = true;
            }
        }
        if(x+1 < mBoardX && y+2 < mBoardY){ // 오른쪽 아래 1
            if(gameBoard[x+1][y+2] == 0){
                selectMoveColor(x+1, y+2, color);
                area2List.add(getMoveAreaPosition(x+1,y+2));
                moveCheckFlag = true;
            }
        }
        if(x+2 < mBoardX && y+2 < mBoardY){ // 오른쪽 아래 2
            if(gameBoard[x+2][y+2] == 0){
                selectMoveColor(x+2, y+2, color);
                area2List.add(getMoveAreaPosition(x+2,y+2));
                moveCheckFlag = true;
            }
        }

        if(y-2 > -1){ //왼쪽
            if(gameBoard[x][y-2] == 0){
                selectMoveColor(x, y-2, color);
                area2List.add(getMoveAreaPosition(x,y-2));
                moveCheckFlag = true;
            }
        }

        if(x-1 > -1 && y-2 > -1){ //왼쪽 위 1
            if(gameBoard[x-1][y-2] == 0){
                selectMoveColor(x-1, y-2, color);
                area2List.add(getMoveAreaPosition(x-1,y-2));
                moveCheckFlag = true;
            }
        }

        if(x-2 > -1 && y-2 > -1){ //왼쪽 위 2
            if(gameBoard[x-2][y-2] == 0){
                selectMoveColor(x-2, y-2, color);
                area2List.add(getMoveAreaPosition(x-2,y-2));
                moveCheckFlag = true;
            }
        }

        if(x+1 < mBoardX && y-2 > -1){ //왼쪽 아래 1
            if(gameBoard[x+1][y-2] == 0){
                selectMoveColor(x+1, y-2, color);
                area2List.add(getMoveAreaPosition(x+1,y-2));
                moveCheckFlag = true;
            }
        }
        if(x+2 < mBoardX && y-2 > -1){ //왼쪽 아래 2
            if(gameBoard[x+2][y-2] == 0){
                selectMoveColor(x+2, y-2, color);
                area2List.add(getMoveAreaPosition(x+2,y-2));
                moveCheckFlag = true;
            }
        }


        if(x-2 > -1){ // 위
            if(gameBoard[x-2][y] == 0){
                selectMoveColor(x-2, y, color);
                area2List.add(getMoveAreaPosition(x-2,y));
                moveCheckFlag = true;
            }
        }

        if(x-2 > -1 && y-1 > -1){ // 위 왼쪽
            if(gameBoard[x-2][y-1] == 0){
                selectMoveColor(x-2, y-1, color);
                area2List.add(getMoveAreaPosition(x-2,y-1));
                moveCheckFlag = true;
            }
        }

        if(x-2 > -1 && y+1 < mBoardY){ // 위 왼쪽
            if(gameBoard[x-2][y+1] == 0){
                selectMoveColor(x-2, y+1, color);
                area2List.add(getMoveAreaPosition(x-2,y+1));
                moveCheckFlag = true;
            }
        }

        if(x+2 < mBoardX){ // 아래
            if(gameBoard[x+2][y] == 0){
                selectMoveColor(x+2, y, color);
                area2List.add(getMoveAreaPosition(x+2,y));
                moveCheckFlag = true;
            }
        }

        if(x+2 < mBoardX && y-1 > -1){ // 아래 왼쪽
            if(gameBoard[x+2][y-1] == 0){
                selectMoveColor(x+2, y-1, color);
                area2List.add(getMoveAreaPosition(x+2,y-1));
                moveCheckFlag = true;
            }
        }

        if(x+2 < mBoardX && y+1 < mBoardY){ // 아래 오른쪽
            if(gameBoard[x+2][y+1] == 0){
                selectMoveColor(x+2, y+1, color);
                area2List.add(getMoveAreaPosition(x+2,y+1));
                moveCheckFlag = true;
            }
        }

        return moveCheckFlag;
    }

    /**
     * 1칸 이동 가능한지 체크
     * @param x
     * @param y
     * @return
     */
    private boolean selectMove1AreaCheck(int x, int y, int player){
        boolean moveCheckFlag = false;
        if(y+1 < mBoardY){
            if(gameBoard[x][y+1] == player)
                moveCheckFlag = true;
        }
        if(y-1 != -1){
            if(gameBoard[x][y-1] == player)
                moveCheckFlag = true;
        }

        if(x-1 != -1){
            if(gameBoard[x-1][y] == player)
                moveCheckFlag = true;
        }
        if(x+1 < mBoardX){
            if(gameBoard[x+1][y] == player)
                moveCheckFlag = true;
        }

        if(x+1 < mBoardX && y+1 < mBoardY){
            if(gameBoard[x+1][y+1] == player)
                moveCheckFlag = true;
        }
        if(x-1 != -1 && y-1 != -1){
            if(gameBoard[x-1][y-1] == player)
                moveCheckFlag = true;
        }

        if(x-1 != -1 && y+1 < mBoardY){
            if(gameBoard[x-1][y+1] == player)
                moveCheckFlag = true;
        }
        if(x+1 < mBoardX && y-1 != -1){
            if(gameBoard[x+1][y-1] == player)
                moveCheckFlag = true;
        }
        return moveCheckFlag;
    }

    /**
     * 2칸 이동 가능한지 체크
     * @param x
     * @param y
     * @return
     */
    private boolean selectMove2AreaCheck(int x, int y, int player){
        boolean moveCheckFlag = false;
        if(y+2 < mBoardY){ //오른쪽
            if(gameBoard[x][y+2] == player)
                moveCheckFlag = true;
        }
        if(x-1 > -1 && y+2 < mBoardY){ // 오른쪽 위 1
            if(gameBoard[x-1][y+2] == player)
                moveCheckFlag = true;
        }
        if(x-2 > -1 && y+2 < mBoardY){ // 오른쪽 위 2
            if(gameBoard[x-2][y+2] == player)
                moveCheckFlag = true;
        }
        if(x+1 < mBoardX && y+2 < mBoardY){ // 오른쪽 아래 1
            if(gameBoard[x+1][y+2] == player)
                moveCheckFlag = true;
        }
        if(x+2 < mBoardX && y+2 < mBoardY){ // 오른쪽 아래 2
            if(gameBoard[x+2][y+2] == player)
                moveCheckFlag = true;
        }

        if(y-2 > -1){ //왼쪽
            if(gameBoard[x][y-2] == player)
                moveCheckFlag = true;
        }

        if(x-1 > -1 && y-2 > -1){ //왼쪽 위 1
            if(gameBoard[x-1][y-2] == player)
                moveCheckFlag = true;
        }

        if(x-2 > -1 && y-2 > -1){ //왼쪽 위 2
            if(gameBoard[x-2][y-2] == player)
                moveCheckFlag = true;
        }

        if(x+1 < mBoardX && y-2 > -1){ //왼쪽 아래 1
            if(gameBoard[x+1][y-2] == player)
                moveCheckFlag = true;
        }
        if(x+2 < mBoardX && y-2 > -1){ //왼쪽 아래 2
            if(gameBoard[x+2][y-2] == player)
                moveCheckFlag = true;
        }

        if(x-2 > -1){ // 위
            if(gameBoard[x-2][y] == player)
                moveCheckFlag = true;
        }

        if(x-2 > -1 && y-1 > -1){ // 위 왼쪽
            if(gameBoard[x-2][y-1] == player)
                moveCheckFlag = true;
        }

        if(x-2 > -1 && y+1 < mBoardY){ // 위 왼쪽
            if(gameBoard[x-2][y+1] == player)
                moveCheckFlag = true;
        }

        if(x+2 < mBoardX){ // 아래
            if(gameBoard[x+2][y] == player)
                moveCheckFlag = true;
        }

        if(x+2 < mBoardX && y-1 > -1){ // 아래 왼쪽
            if(gameBoard[x+2][y-1] == player)
                moveCheckFlag = true;
        }

        if(x+2 < mBoardX && y+1 < mBoardY){ // 아래 오른쪽
            if(gameBoard[x+2][y+1] == player)
                moveCheckFlag = true;
        }

        return moveCheckFlag;
    }

    /**
     * AI가 1칸 이동 가능한지 체크
     * @param x
     * @param y
     * @return
     */
    private ArrayList<HashMap<String, Integer>> selectAIMove1AreaCheck(int x, int y, int player){
        ArrayList<HashMap<String, Integer>> moveList = new ArrayList<>();
        if(y+1 < mBoardY){
            if(gameBoard[x][y+1] == player)
                moveList.add(getMoveAreaPosition(x, y+1));
        }
        if(y-1 != -1){
            if(gameBoard[x][y-1] == player)
                moveList.add(getMoveAreaPosition(x, y-1));
        }

        if(x-1 != -1){
            if(gameBoard[x-1][y] == player)
                moveList.add(getMoveAreaPosition(x-1, y));
        }
        if(x+1 < mBoardX){
            if(gameBoard[x+1][y] == player)
                moveList.add(getMoveAreaPosition(x+1, y));
        }

        if(x+1 < mBoardX && y+1 < mBoardY){
            if(gameBoard[x+1][y+1] == player)
                moveList.add(getMoveAreaPosition(x+1, y+1));
        }
        if(x-1 != -1 && y-1 != -1){
            if(gameBoard[x-1][y-1] == player)
                moveList.add(getMoveAreaPosition(x-1, y-1));
        }

        if(x-1 != -1 && y+1 < mBoardY){
            if(gameBoard[x-1][y+1] == player)
                moveList.add(getMoveAreaPosition(x-1, y+1));
        }
        if(x+1 < mBoardX && y-1 != -1){
            if(gameBoard[x+1][y-1] == player)
                moveList.add(getMoveAreaPosition(x+1, y-1));;
        }
        return moveList;
    }

    /**
     * 2칸 이동 가능한지 체크
     * @param x
     * @param y
     * @return
     */
    private ArrayList<HashMap<String, Integer>> selectAIMove2AreaCheck(int x, int y, int player){
        ArrayList<HashMap<String, Integer>> moveList = new ArrayList<>();
        if(y+2 < mBoardY){ //오른쪽
            if(gameBoard[x][y+2] == player)
                moveList.add(getMoveAreaPosition(x, y+2));
        }
        if(x-1 > -1 && y+2 < mBoardY){ // 오른쪽 위 1
            if(gameBoard[x-1][y+2] == player)
                moveList.add(getMoveAreaPosition(x-1, y+2));
        }
        if(x-2 > -1 && y+2 < mBoardY){ // 오른쪽 위 2
            if(gameBoard[x-2][y+2] == player)
                moveList.add(getMoveAreaPosition(x-2, y+2));
        }
        if(x+1 < mBoardX && y+2 < mBoardY){ // 오른쪽 아래 1
            if(gameBoard[x+1][y+2] == player)
                moveList.add(getMoveAreaPosition(x+1, y+2));
        }
        if(x+2 < mBoardX && y+2 < mBoardY){ // 오른쪽 아래 2
            if(gameBoard[x+2][y+2] == player)
                moveList.add(getMoveAreaPosition(x+2, y+2));
        }

        if(y-2 > -1){ //왼쪽
            if(gameBoard[x][y-2] == player)
                moveList.add(getMoveAreaPosition(x, y-2));
        }

        if(x-1 > -1 && y-2 > -1){ //왼쪽 위 1
            if(gameBoard[x-1][y-2] == player)
                moveList.add(getMoveAreaPosition(x-1, y-2));
        }

        if(x-2 > -1 && y-2 > -1){ //왼쪽 위 2
            if(gameBoard[x-2][y-2] == player)
                moveList.add(getMoveAreaPosition(x-2, y-2));
        }

        if(x+1 < mBoardX && y-2 > -1){ //왼쪽 아래 1
            if(gameBoard[x+1][y-2] == player)
                moveList.add(getMoveAreaPosition(x+1, y-2));
        }
        if(x+2 < mBoardX && y-2 > -1){ //왼쪽 아래 2
            if(gameBoard[x+2][y-2] == player)
                moveList.add(getMoveAreaPosition(x+2, y-2));
        }

        if(x-2 > -1){ // 위
            if(gameBoard[x-2][y] == player)
                moveList.add(getMoveAreaPosition(x-2, y));
        }

        if(x-2 > -1 && y-1 > -1){ // 위 왼쪽
            if(gameBoard[x-2][y-1] == player)
                moveList.add(getMoveAreaPosition(x-2, y-1));
        }

        if(x-2 > -1 && y+1 < mBoardY){ // 위 왼쪽
            if(gameBoard[x-2][y+1] == player)
                moveList.add(getMoveAreaPosition(x-2, y+1));
        }

        if(x+2 < mBoardX){ // 아래
            if(gameBoard[x+2][y] == player)
                moveList.add(getMoveAreaPosition(x+2, y));
        }

        if(x+2 < mBoardX && y-1 > -1){ // 아래 왼쪽
            if(gameBoard[x+2][y-1] == player)
                moveList.add(getMoveAreaPosition(x+2, y-1));
        }

        if(x+2 < mBoardX && y+1 < mBoardY){ // 아래 오른쪽
            if(gameBoard[x+2][y+1] == player)
                moveList.add(getMoveAreaPosition(x+2, y+1));
        }

        return moveList;
    }

    private HashMap<String, Integer> getMoveAreaPosition(int x, int y){
        HashMap<String, Integer> move = new HashMap<>();
        move.put("X", x);
        move.put("Y", y);
        return move;
    }
    private void selectMoveColor(int x, int y, int color){
        LinearLayout child = (LinearLayout) mGameMainLayout.getChildAt(x);
        Button btn = (Button) child.getChildAt(y);
        btn.setBackgroundColor(mContext.getResources().getColor(color));
    }

    private void nonSelectColorReSet(String type ){
        String data = "";
        Log.d("#@#", "gameBoard ["+type+"]========= ");
        for(int i = 0; i < mBoardX; i++) {
            LinearLayout child = (LinearLayout) mGameMainLayout.getChildAt(i);
            for (int i2 = 0; i2 < mBoardY; i2++) {
                Button btn = (Button) child.getChildAt(i2);
                data += gameBoard[i][i2] + " ";
                if(gameBoard[i][i2] == 0){
                    if((i+","+i2).equals(btn.getTag().toString()))
                        btn.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary2));
                }
            }

            Log.d("#@#", "gameBoard : " + data);
            data = "";
        }
        Log.d("#@#", "gameBoard ========= ");
    }


    private int[] gameRuleCheck(int x, int y) {
        int[] gamePointsTmp = new int[2];
        gamePointsTmp[0] = 0;
        gamePointsTmp[1] = 0;

        int player = gameBoard[x][y];
        Log.d("#@#", "gameBoard Player : " + player);

        // ===============================
        // 게임 판정 세팅
        // ===============================
        if(y+1 < mBoardY){
            if(gameBoard[x][y+1] != player && gameBoard[x][y+1] != 0)
                gameBoard[x][y+1] = player;
        }
        if(y-1 != -1){
            if(gameBoard[x][y-1] != player && gameBoard[x][y-1] != 0)
                gameBoard[x][y-1] = player;
        }

        if(x-1 != -1){
            if(gameBoard[x-1][y] != player && gameBoard[x-1][y] != 0)
                gameBoard[x-1][y] = player;
        }
        if(x+1 < mBoardX){
            if(gameBoard[x+1][y] != player && gameBoard[x+1][y] != 0)
                gameBoard[x+1][y] = player;
        }

        if(x+1 < mBoardX && y+1 < mBoardY){
            if(gameBoard[x+1][y+1] != player && gameBoard[x+1][y+1] != 0)
                gameBoard[x+1][y+1] = player;
        }
        if(x-1 != -1 && y-1 != -1){
            if(gameBoard[x-1][y-1] != player && gameBoard[x-1][y-1] != 0)
                gameBoard[x-1][y-1] = player;
        }

        if(x-1 != -1 && y+1 < mBoardY){
            if(gameBoard[x-1][y+1] != player && gameBoard[x-1][y+1] != 0)
                gameBoard[x-1][y+1] = player;
        }
        if(x+1 < mBoardX && y-1 != -1){
            if(gameBoard[x+1][y-1] != player && gameBoard[x+1][y-1] != 0)
                gameBoard[x+1][y-1] = player;
        }

        boolean gamePlayCheck = false;
        // 점수 및 컬러 세팅
        for(int i = 0; i < mBoardX; i++) {
            LinearLayout child = (LinearLayout) mGameMainLayout.getChildAt(i);
            for (int i2 = 0; i2 < mBoardY; i2++) {
                Button btn = (Button) child.getChildAt(i2);
                if(gameBoard[i][i2] == 1){
                    if((i+","+i2).equals(btn.getTag().toString()))
                        btn.setBackgroundColor(mContext.getResources().getColor(R.color.colorPlayer));
                    gamePointsTmp[0] += 1;
                }else if(gameBoard[i][i2] == 2){
                    if((i+","+i2).equals(btn.getTag().toString()))
                        btn.setBackgroundColor(mContext.getResources().getColor(R.color.colorAi));
                    gamePointsTmp[1] += 1;
                }else{
                    btn.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary2));
                    gamePlayCheck = true;
                }
            }
        }

        gamePlayFlag = gamePlayCheck;


        return gamePointsTmp;
    }


    public void setPlayerSwitch(String player){
        mModel.setStringPref(mContext, mModel.getKeyPlayer(), player);
    }

    public String getPlayerSwitch(){
        return mModel.getStringPref(mContext, mModel.getKeyPlayer());
    }

    public String getKeyPlayer(){
        return mModel.getKeyPlayer();
    }
    public String getValuePlayer(){
        return mModel.getValuePlayer();
    }
    public String getValueAI(){
        return mModel.getValueAI();
    }

    public boolean getStartGame(){
        return startGame;
    }
    public void setStartGame(boolean startGame){
        this.startGame = startGame;
    }

    public boolean getFirstPlayer(){
        return firstPlayer;
    }
    @SuppressLint("HandlerLeak")
    private Handler aiHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case AIHANDLER_START:

                    // 이동 하고자 하는 포인트 찾기
                    searchAiMovePoint();

                    aiHandler.sendEmptyMessageDelayed(AIHANDLER_POINTCHECK1, AIHANDLER_TIME);
                    break;
                case AIHANDLER_POINTCHECK1:

                    // 터치한 곳에 이동 가능한 포인트 찾기
                    Log.d("#@#", "[AI] : AIHANDLER_POINTCHECK1 " + firstX + ", " + firstY);
                    nonSelectColorReSet("AI1");
                    aiTurn1(firstX, firstY);

//                    //최적의 포인트 찾기
//                    boolean move = false;
//
//                    int area1AiPoints = 0, area2AiPoints = 0;
//                    int area1TmpX = -1, area1TmpY = -1, area2TmpX = -1, area2TmpY = -1;
//
//                    // 1칸 증식
//                    for(int i1 = 0; i1 < area1List.size(); i1++){
//                        HashMap<String, Integer> map = area1List.get(i1);
//                        int mapX = map.get("X");
//                        int mapY = map.get("Y");
//
//                        int tmpPoints = checkAiPoint(gameBoard, mapX, mapY);
//                        if(tmpPoints >= area1AiPoints){
//                            move = true;
//                            area1AiPoints = tmpPoints;
//                            area1TmpX = mapX;
//                            area1TmpY = mapY;
//                        }
//
//                    }
//                    if(move == false){
//                        // 2 칸 이동
//                        for(int i2 = 0; i2 < area2List.size(); i2++){
//                            HashMap<String, Integer> map = area2List.get(i2);
//                            int mapX = map.get("X");
//                            int mapY = map.get("Y");
//
//                            int tmpPoints = checkAiPoint(gameBoard, mapX, mapY);
//                            if(tmpPoints >= area2AiPoints){
//                                move = true;
//                                area2AiPoints = tmpPoints;
//                                area2TmpX = mapX;
//                                area2TmpY = mapY;
//                            }
//                        }
//                    }
//
//                    if(area1AiPoints >= area2AiPoints){
//                        aiX = area1TmpX;
//                        aiY = area1TmpY;
//                    }else{
//                        aiX = area2TmpX;
//                        aiY = area2TmpY;
//                    }

                    aiHandler.sendEmptyMessageDelayed(AIHANDLER_POINTCHECK2, AIHANDLER_TIME);

                    break;

                case AIHANDLER_POINTCHECK2:
                    //  터치한 곳으로 이동하기
                    Log.d("#@#" ,"[AI] : AIHANDLER_POINTCHECK2 " + firstX + ", " + firstY);
                    nonSelectColorReSet("AI2");
                    aiTurn2(aiMoveX, aiMoveY);

                    aiHandler.sendEmptyMessageDelayed(AIHANDLER_END, AIHANDLER_TIME);

                    break;

                case AIHANDLER_END:
                    // 턴 넘기기


                    aiHandler.removeMessages(0);
                    break;
            }
        }
    };

    private void aiTurn1(int x, int y){
        move1CheckFlag = selectMove1Area(x, y, R.color.colorWhite);
        move2CheckFlag = selectMove2Area(x, y, R.color.colorWhite2);
        aiPlayerCheck = 0;
    }


    private void aiTurn2(int x, int y){

        if(aiPlayerCheck == 0 && (move1CheckFlag || move2CheckFlag)){

            boolean move = false;

            // 1칸 증식
            for(int i1 = 0; i1 < area1List.size(); i1++){
                HashMap<String, Integer> map = area1List.get(i1);
                int mapX = map.get("X");
                int mapY = map.get("Y");

                if(x == mapX && y == mapY){
                    move = true;
                    gameBoard[mapX][mapY] = 2;
                    selectMoveColor(mapX, mapY, R.color.colorAi);
                    break;
                }
            }

            if(move == false){
                // 2 칸 이동
                for(int i2 = 0; i2 < area2List.size(); i2++){
                    HashMap<String, Integer> map = area2List.get(i2);
                    int mapX = map.get("X");
                    int mapY = map.get("Y");

                    if(x == mapX && y == mapY){
                        move = true;
                        gameBoard[mapX][mapY] = 2;
                        selectMoveColor(mapX, mapY, R.color.colorAi);
                        gameBoard[firstX][firstY] = 0;
                        Log.d("#@#", "aiTurn 2p 2 firstX : " + firstX + " | firstY : " + firstY + " & mapX : " + mapX + " | mapY : " + mapY);
                        selectMoveColor(firstX, firstY, R.color.colorPrimary2);
                        break;
                    }
                }
            }

            if(move == false){
                if(area1List.size() > 0){
                    HashMap<String, Integer> map = area1List.get((int)Math.random() * area1List.size());
                    int mapX = map.get("X");
                    int mapY = map.get("Y");


                    move = true;
                    gameBoard[mapX][mapY] = 2;
                    selectMoveColor(mapX, mapY, R.color.colorAi);

                }

                if(move == false){
                    if(area2List.size() > 0){
                        HashMap<String, Integer> map = area2List.get((int)Math.random() * area2List.size());
                        int mapX = map.get("X");
                        int mapY = map.get("Y");

                        move = true;
                        gameBoard[mapX][mapY] = 2;
                        selectMoveColor(mapX, mapY, R.color.colorAi);
                        gameBoard[firstX][firstY] = 0;
                        Log.d("#@#", "aiTurn move == false 2 firstX : " + firstX + " | firstY : " + firstX);
                        selectMoveColor(firstX, firstY, R.color.colorPrimary2);
                    }
                }
            }

            if(move){
                firstX = -1;
                firstY = -1;

                move1CheckFlag = false;
                move2CheckFlag = false;

                aiPlayerCheck = 2;

                gamePlayCheck(x, y);
            }
        }
    }

    private int checkAiPoint(int x, int y){
        int point = 0;
        if(y+1 < mBoardY){

            if(gameBoard[x][y+1] == 1)
                point++;
        }
        if(y-1 != -1){
            if(gameBoard[x][y-1] == 1)
                point++;
        }

        if(x-1 != -1){
            if(gameBoard[x-1][y] == 1)
                point++;
        }
        if(x+1 < mBoardX){
            if(gameBoard[x+1][y] == 1)
                point++;
        }

        if(x+1 < mBoardX && y+1 < mBoardY){
            if(gameBoard[x+1][y+1] == 1)
                point++;
        }
        if(x-1 != -1 && y-1 != -1){
            if(gameBoard[x-1][y-1] == 1)
                point++;
        }

        if(x-1 != -1 && y+1 < mBoardY){
            if(gameBoard[x-1][y+1] == 1)
                point++;
        }
        if(x+1 < mBoardX && y-1 != -1){
            if(gameBoard[x+1][y-1] == 1)
                point++;
        }

        return point;
    }

    private void searchAiMovePoint(){

        // AI 이동 가능한 블록 위치 찾기
        ArrayList<ArrayList<HashMap<String, Integer>>> area1List = new ArrayList<ArrayList<HashMap<String, Integer>>>();
        ArrayList<ArrayList<HashMap<String, Integer>>> area2List = new ArrayList<ArrayList<HashMap<String, Integer>>>();

        ArrayList<HashMap<String, Integer>> aiPoint = new ArrayList<>();

        for(int i = 0; i < mBoardX; i++) {
            LinearLayout childLayout = (LinearLayout) mGameMainLayout.getChildAt(i);
            for (int i2 = 0; i2 < mBoardY; i2++) {
                if(gameBoard[i][i2] == 2){
                    Button btn = (Button) childLayout.getChildAt(i2);
                    String tag = btn.getTag().toString();
                    String[] tmp = tag.split(",");
                    int x = Integer.parseInt(tmp[0]);
                    int y = Integer.parseInt(tmp[1]);
                    ArrayList<HashMap<String, Integer>> tmpMap = new ArrayList<>();
                    tmpMap = selectAIMove1AreaCheck(x, y, 0);

                    HashMap<String, Integer> map = new HashMap<>();
                    map.put("X", x);
                    map.put("Y", y);

                    if(tmpMap.size() > 0){
                        area1List.add(tmpMap);
                        map.put("Area1Position", area1List.size()-1);
                    }

                    tmpMap = selectAIMove2AreaCheck(x, y, 0);
                    if(tmpMap.size() > 0){
                        area2List.add(tmpMap);
                        map.put("Area2Position", area2List.size()-1);
                    }

                    aiPoint.add(map);
                }
            }
        }

        // 공격 가능한 이동 포인트 찾기
        int attack1Point = 0, attack2Point = 0;
        int attack1X = -1, attack1Y = -1, attack2X = -1, attack2Y = -1;
        int attack1FristX = -1, attack1FristY = -1, attack2FristX = -1, attack2FristY = -1;
        int area1Count = 0, area2Count = 0;
        int area1Index = 0, Area1Position = 0, area2Index = 0, Area2Position = 0;

        for(int i = 0; i < aiPoint.size(); i++) {
            HashMap<String, Integer> point = aiPoint.get(i);

            try{
                Area1Position = point.get("Area1Position");
                ArrayList<HashMap<String, Integer>> list1 = area1List.get(Area1Position);
                int list1Size = list1.size();
                if(list1Size > area1Count){
                    area1Count = list1Size;
                    area1Index = i;
                    for(int i2 = 0; i2 < list1Size; i2++){
                        HashMap<String, Integer> map1 = list1.get(i2);
                        int attack1PointTmp = checkAiPoint(map1.get("X"), map1.get("Y"));
                        if(attack1PointTmp > attack1Point){
                            attack1Point = attack1PointTmp;
                            attack1X = map1.get("X");
                            attack1Y = map1.get("Y");
                            attack1FristX = point.get("X");
                            attack1FristY = point.get("Y");
                        }
                    }
                }
            }catch (Exception e){}

            try{
                Area2Position = point.get("Area2Position");
                ArrayList<HashMap<String, Integer>> list2 = area2List.get(Area2Position);

                int list2Size = list2.size();
                if(list2Size > area2Count){
                    area2Count = list2Size;
                    area2Index = i;
                    for(int i3 = 0; i3 < list2Size; i3++){
                        HashMap<String, Integer> map2= list2.get(i3);
                        int attack2PointTmp = checkAiPoint(map2.get("X"), map2.get("Y"));
                        if(attack2PointTmp > attack2Point){
                            attack2Point = attack2PointTmp;
                            attack2X = map2.get("X");
                            attack2Y = map2.get("Y");
                            attack2FristX = point.get("X");
                            attack2FristY = point.get("Y");
                        }
                    }
                }

            }catch (Exception e){}
        }

        //공격 가능한 포인트가 있는지
        if(attack2Point > attack1Point) {
            // 2칸 공격이 더 효율 적일때

            /*
            ArrayList<HashMap<String, Integer>> list2Points = area2List.get(area2IndexX);
            HashMap<String, Integer> list2PointsMap = list2Points.get(area2IndexY);
            aiMoveX = list2PointsMap.get("X");
            aiMoveY = list2PointsMap.get("Y");
            HashMap<String, Integer> list2Frist = aiPoint.get(area2IndexX);
            firstX = list2Frist.get("X");
            firstY = list2Frist.get("Y");
            /*/
            aiMoveX = attack2X;
            aiMoveY = attack2Y;
            firstX = (attack2FristX != -1 ? attack2FristX : firstX);
            firstY = (attack2FristY != -1 ? attack2FristY : firstX);

            Log.d("#@#","[AI] 111 firstX : " + firstX + " | firstY : " + firstY + " !! aiMoveX : " + aiMoveX + " | aiMoveY : " + aiMoveY);
            //*/
        }else if(attack1Point > 0){
            // 1칸공격이 더 좋을때

            /*
            ArrayList<HashMap<String, Integer>> list1Points = area1List.get(area1IndexX);
            HashMap<String, Integer> list1PointsMap = list1Points.get(area1IndexY);
            aiMoveX = list1PointsMap.get("X");
            aiMoveY = list1PointsMap.get("Y");
            HashMap<String, Integer> list1Frist = aiPoint.get(area1IndexX);
            firstX = list1Frist.get("X");
            firstY = list1Frist.get("Y");
            /*/
            aiMoveX = attack1X;
            aiMoveY = attack1Y;
            firstX = (attack1FristX != -1 ? attack1FristX : firstX);
            firstY = (attack1FristY != -1 ? attack1FristY : firstX);
            Log.d("#@#","[AI] 222 firstX : " + firstX + " | firstY : " + firstY + " !! aiMoveX : " + aiMoveX + " | aiMoveY : " + aiMoveY);
            //*/
        }else{

            boolean move1Flag = false; // 1칸 증감 가능여부

            // 공격이 불가능 한 경우 1칸 증감시키기

            if(area1Count > 0){

                ArrayList<HashMap<String, Integer>> list1 = area1List.get(area1Index);
                HashMap<String, Integer> map1 = list1.get((int)Math.random() * list1.size());
                aiMoveX = map1.get("X");
                aiMoveY = map1.get("Y");
                HashMap<String, Integer> list1Frist = aiPoint.get(area1Index);
                firstX = list1Frist.get("X");
                firstY = list1Frist.get("Y");

                move1Flag = true;
                Log.d("#@#","[AI] 333 firstX : " + firstX + " | firstY : " + firstY + " !! aiMoveX : " + aiMoveX + " | aiMoveY : " + aiMoveY);
            }


            // 1증감이 않되고 2 이동만 가능할경우

            if(!move1Flag && area1Count > 0){

                ArrayList<HashMap<String, Integer>> list2 = area2List.get(area2Index);
                HashMap<String, Integer> map2 = list2.get((int)Math.random() * list2.size());
                aiMoveX = map2.get("X");
                aiMoveY = map2.get("Y");
                HashMap<String, Integer> list2Frist = aiPoint.get(area2Index);
                firstX = list2Frist.get("X");
                firstY = list2Frist.get("Y");
                Log.d("#@#","[AI] 444 firstX : " + firstX + " | firstY : " + firstY + " !! aiMoveX : " + aiMoveX + " | aiMoveY : " + aiMoveY);
            }
        }


        //Log.d("#@#","[AI] 555 firstX : " + firstX + " | firstY : " + firstY + " !! aiMoveX : " + aiMoveX + " | aiMoveY : " + aiMoveY);
    }

    private void searchAiMovePoint2(){
        // 이동 가능한 곳의 포인트를 담았다.
        // 이동 할수있는 AI에 포인트를 찾아야함
        ArrayList<ArrayList<HashMap<String, Integer>>> area1List = new ArrayList<ArrayList<HashMap<String, Integer>>>();
        ArrayList<ArrayList<HashMap<String, Integer>>> area2List = new ArrayList<ArrayList<HashMap<String, Integer>>>();
        ArrayList<HashMap<String, Integer>> areaCenterList = new ArrayList<>();


        for(int i = 0; i < mBoardX; i++) {
            for (int i2 = 0; i2 < mBoardY; i2++) {
                //Log.d("#@#", "[init] | gameBoard["+i+"]["+i2+"] : " + (gameBoard[i][i2]));
                if(gameBoard[i][i2] == 2){
                    ArrayList<HashMap<String, Integer>> tmp = new ArrayList<>();
                    boolean moveCenterCheck = false;
                    tmp = selectAIMove1AreaCheck(i, i2, 0);
                    if(tmp.size() > 0){
                        moveCenterCheck = true;
                        area1List.add(tmp);
                    }

                    tmp = selectAIMove2AreaCheck(i, i2, 0);
                    if(tmp.size() > 0){
                        moveCenterCheck = true;
                        area2List.add(tmp);
                    }

                    if(moveCenterCheck){
                        HashMap<String, Integer> moveCenterMap = new HashMap<>();
                        moveCenterMap.put("CenterX", i);
                        moveCenterMap.put("CenterY", i2);
                        areaCenterList.add(moveCenterMap);
                    }

                }
            }
        }
        //최적의 포인트 찾기
        boolean move = false;
        int area1AiPoints = 0, area2AiPoints = 0;
        int area1TmpX = 0, area1TmpY = 0, area2TmpX = 0, area2TmpY = 0;

        for(int i = 0; i < areaCenterList.size(); i++){
            HashMap<String, Integer> centerMap = areaCenterList.get(i);
            int centerX = centerMap.get("CenterX");
            int centerY = centerMap.get("CenterY");

            // 1칸 증식 이 가능한 공간
            if(area1List.size() > 0){
                try{
                    ArrayList<HashMap<String, Integer>> area1ChildList = area1List.get(i);
                    for(int i1 = 0; i1 < area1ChildList.size(); i1++){
                        HashMap<String, Integer> map = area1ChildList.get(i1);
                        int mapX = map.get("X");
                        int mapY = map.get("Y");

                        int tmpPoints = checkAiPoint(mapX, mapY);
                        if(tmpPoints > area1AiPoints){
                            move = true;
                            area1AiPoints = tmpPoints;
                            area1TmpX = mapX;
                            area1TmpY = mapY;
                        }
                    }
                }catch(Exception e){}
            }

            if(move == false){
                // 2 칸 이동
                if(area2List.size() > 0){

                    try{
                        ArrayList<HashMap<String, Integer>> area2ChildList = area2List.get(i);
                        for(int i2 = 0; i2 < area2ChildList.size(); i2++){
                            HashMap<String, Integer> map = area2ChildList.get(i2);
                            int mapX = map.get("X");
                            int mapY = map.get("Y");

                            int tmpPoints = checkAiPoint(mapX, mapY);
                            if(tmpPoints > area2AiPoints){
                                move = true;
                                area2AiPoints = tmpPoints;
                                area2TmpX = mapX;
                                area2TmpY = mapY;
                            }
                        }
                    }catch(Exception e){}
                }
            }

            if(area1AiPoints > 2 || area1AiPoints == 0){
                if(area1AiPoints == 0){

                    int count = 0;
                    int index = 0;
                    for(int i3 = 0; i3 < area1List.size(); i3++){
                        int tmpCount = area1List.get(i3).size();
                        if(tmpCount > count) {
                            count = tmpCount;
                            index = i3;
                        }
                    }

                    ArrayList<HashMap<String, Integer>> area1ChildList = area1List.get(index);
                    HashMap<String, Integer> map = area1ChildList.get((int)Math.random() * area1ChildList.size());
                    aiMoveX = map.get("X");
                    aiMoveY = map.get("Y");

                }else{
                    aiMoveX = area1TmpX;
                    aiMoveY = area1TmpY;
                }
            }else{
                aiMoveX = area2TmpX;
                aiMoveY = area2TmpY;
            }

            firstX = centerX;
            firstY = centerY;
        }
    }
}
