package jp.techacademy.takuya.okitsu.jumpactiongame;

/**
 * Created by takuy on 2016/11/19.
 */
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScreen extends ScreenAdapter{
    static final float CAMERA_WIDTH = 10;
    static final float CAMERA_HEIGHT = 15;
    static final float WORLD_WIDTH = 10;
    static final float WORLD_HEIGHT = 15 * 20; //20画面上れば終了
    static final float GUI_WIDTH = 320;
    static final float GUI_HEIGHT = 480;

    static final int GAME_STATE_READY = 0;
    static final int GAME_STATE_PLAYING = 1;
    static final int GAME_STATE_GAMEOVER = 2;

    //重力
    static final float GRAVITY = -12;

    private JumpActionGame mGame;

    Sprite mBg;
    OrthographicCamera mCamera;
    FitViewport mViewPort;

    Random mRandom;
    List<Step> mSteps;
    List<Star> mStars;
    Ufo mUfo;
    Player mPlayer;

    float mHeightSoFar;
    int mGameState;
    Vector3 mTouchPoint;

    public GameScreen(JumpActionGame game) {
        mGame = game;

        //背景の準備
        Texture bgTexture = new Texture("back.png");
        //TextureRegionで切り出すときの原点は左上
        mBg = new Sprite(new TextureRegion(bgTexture,0,0,540,810));
        mBg.setSize(CAMERA_WIDTH,CAMERA_HEIGHT);
        mBg.setPosition(0,0);

        //カメラ、ViewPointを生成、設定する
        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false,CAMERA_WIDTH,CAMERA_WIDTH);
        mViewPort = new FitViewport(CAMERA_WIDTH,CAMERA_HEIGHT,mCamera);

        //メンバ変数の初期化
        mRandom = new Random();
        mSteps = new ArrayList<Step>();
        mStars = new ArrayList<Star>();
        mGameState = GAME_STATE_READY;
        mTouchPoint = new Vector3();
        createStage();
    }

    @Override
    public void render (float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //それぞれの状態をアップデートする
        update(delta);

        //カメラの座標をアップデートし、スプライトの表示に反映させる
        mCamera.update();
        mGame.batch.setProjectionMatrix(mCamera.combined);

        mGame.batch.begin();

        //原点は左下
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2,mCamera.position.y - CAMERA_HEIGHT / 2);
        mBg.draw(mGame.batch);

        // Step
        for (int i = 0; i < mSteps.size(); i++) {
            mSteps.get(i).draw(mGame.batch);
        }

        // Star
        for (int i = 0; i < mStars.size(); i++) {
            mStars.get(i).draw(mGame.batch);
        }

        // UFO
        mUfo.draw(mGame.batch);

        //Player
        mPlayer.draw(mGame.batch);

        mGame.batch.end();
    }

    @Override
    public void resize(int width,int height) {
        mViewPort.update(width,height);
    }

    //ステージを作成する
    private void createStage() {

        //テクスチャの準備
        Texture stepTexture = new Texture("step.png");
        Texture starTexture = new Texture("star.png");
        Texture playerTexture = new Texture("uma.png");
        Texture ufoTexture = new Texture("ufo.png");

        //StepとStarをゴールの高さまで配置している
        float y = 0;

        float maxJumpHeight = Player.PLAYER_JUMP_VELOCITY * Player.PLAYER_JUMP_VELOCITY / (2 * -GRAVITY);
        while (y < WORLD_HEIGHT -5) {
            int type = mRandom.nextFloat() > 0.8f ? Step.STEP_TYPE_MOVING : Step.STEP_TYPE_STATIC;
            float x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH);

            Step step = new Step(type,stepTexture,0,0,144,36);
            step.setPosition(x,y);
            mSteps.add(step);

            if (mRandom.nextFloat() > 0.6f) {
                Star star = new Star(starTexture,0,0,72,72);
                star.setPosition(step.getX() + mRandom.nextFloat(),step.getY() + Star.STAR_HEIGHT +
                mRandom.nextFloat() * 3);
                mStars.add(star);
                }

            y += (maxJumpHeight - 0.5f);
            y -= mRandom.nextFloat() * (maxJumpHeight / 3);
        }

        //Playerを配置
        mPlayer = new Player(playerTexture,0,0,72,72);
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.getWidth() /2,Step.STEP_HEIGHT);

        //ゴールのUFOを配置
        mUfo = new Ufo(ufoTexture, 0, 0, 120, 74);
        mUfo.setPosition(WORLD_WIDTH /2 - Ufo.UFO_WIDTH /2,y);
        }
    //それぞれのオブジェクトをアップデートする
    private void update(float delta) {
        switch (mGameState) {
            case GAME_STATE_READY:
                updateReady();
                break;
            case GAME_STATE_PLAYING:
                updatePlaying(delta);
                break;
            case GAME_STATE_GAMEOVER:
                updateGameOver();
                break;
        }

    }
    private void updateReady() {
        if (Gdx.input.justTouched()) {
            mGameState = GAME_STATE_PLAYING;
        }
    }

    private void updatePlaying(float delta) {
        float accel = 0;
        if (Gdx.input.isTouched()) {
            mViewPort.unproject(mTouchPoint.set(Gdx.input.getX(),Gdx.input.getY(),0));
            Rectangle left = new Rectangle(0,0,CAMERA_WIDTH / 2 ,CAMERA_HEIGHT);
            Rectangle right = new Rectangle(CAMERA_WIDTH / 2,0,CAMERA_WIDTH / 2,CAMERA_HEIGHT);
            if (left.contains(mTouchPoint.x,mTouchPoint.y)){
                accel = 5.0f;
            }
            if (right.contains(mTouchPoint.x,mTouchPoint.y)){
                accel = -5.0f;
            }
        }
        //Step
        for (int i = 0;i < mSteps.size();i++) {
            mSteps.get(i).update(delta);
        }

        //Player
        if (mPlayer.getY() <= 0.5f) {
            mPlayer.hitStep();
        }
        mPlayer.update(delta,accel);
        mHeightSoFar = Math.max(mPlayer.getV(),mHeightSoFar);
    }
    private void updateGameOver(){

    }

}
