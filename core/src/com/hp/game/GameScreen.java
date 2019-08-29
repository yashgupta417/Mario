package com.hp.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Random;

public class GameScreen implements Screen,InputProcessor {
	SpriteBatch batch;
	Texture background;
	Texture[] man;
	Texture dizzyMan;
	Rectangle manRectangle;
	int manState=0;
	int pause=0,goUp=20;
	float gravity=-9.8f;
	float velocity=0;
	float manY;
	float time;
	float upacc=0;
	ArrayList<Integer> CoinXs=new ArrayList<Integer>();
	ArrayList<Integer> CoinYs=new ArrayList<Integer>();
	ArrayList<Circle> CoinRectangles=new ArrayList<Circle>();
	Texture coin;
	int coinCount=0;
	ArrayList<Integer> BombXs=new ArrayList<Integer>();
	ArrayList<Integer> BombYs=new ArrayList<Integer>();
	ArrayList<Circle> BombRectangles=new ArrayList<Circle>();
	Texture bomb;
	int bombCount=0;
	Random random;
	int score=0;
	BitmapFont font;
	int gameState=0;
	Sound coinSound,gameOverSound;
	Mario game;


	public GameScreen (final Mario game) {
		this.game=game;
		batch = new SpriteBatch();
		Gdx.input.setInputProcessor((InputProcessor) this);
		Gdx.input.setCatchBackKey(true);
		background = new Texture("bg.png");
		man = new Texture[4];
		man[0] = new Texture("frame-1.png");
		man[1] = new Texture("frame-2.png");
		man[2] = new Texture("frame-3.png");
		man[3] = new Texture("frame-4.png");
		dizzyMan=new Texture("dizzy-1.png");
		manY = Gdx.graphics.getHeight() / 2;
		coin=new Texture("coin.png");
		bomb=new Texture("bomb.png");
		random=new Random();
		font=new BitmapFont();
		coinSound=Gdx.audio.newSound(Gdx.files.internal("coin-sound.wav"));
		gameOverSound=Gdx.audio.newSound(Gdx.files.internal("game-over-sound.wav"));

	}
	public void setManPosition(){
		if(Gdx.input.justTouched()){
			goUp=0;
		}
		time=Gdx.graphics.getDeltaTime();
		if(goUp<20){
			goUp++;
			upacc=20;
		}else{
			upacc=0;
		}
		velocity+=(gravity+upacc)*(time);
		manY+=velocity+((gravity+upacc)/2)*(time)*(time);
		if(manY<0) {
			manY = 0;
			velocity=0;
		}else if(manY>Gdx.graphics.getHeight()-man[manState].getHeight()){
			manY=Gdx.graphics.getHeight()-man[manState].getHeight();
			velocity=0;
		}
		if(pause<8)
			pause++;
		else {
			pause = 0;
			if (manState < 3)
				manState++;
			else
				manState = 0;
		}
		DisplayMan();
	}
	public void DisplayMan(){
		if(gameState==2){
			batch.draw(dizzyMan,Gdx.graphics.getWidth()/2-dizzyMan.getWidth(),manY);
		}else{
			batch.draw(man[manState],Gdx.graphics.getWidth()/2-man[manState].getWidth(),manY);
			manRectangle =new Rectangle(Gdx.graphics.getWidth()/2-man[manState].getWidth()
					,manY,man[manState].getWidth(),man[manState].getHeight());
		}

	}
	public void ActivateCoinFactory(){
		if(coinCount<100)
			coinCount++;
		else {
			coinCount=0;
			makeCoin();
		}
		CoinRectangles.clear();
		for(int i=0;i<CoinYs.size();i++){
			batch.draw(coin,CoinXs.get(i),CoinYs.get(i));
			CoinXs.set(i,CoinXs.get(i)-4);
			CoinRectangles.add(new Circle(CoinXs.get(i)+coin.getWidth()/2 , CoinYs.get(i)+coin.getHeight()/2 ,coin.getWidth()/2));
		}
	}
	public void ActivateBombFactory(){
		if(bombCount<150)
			bombCount++;
		else {
			bombCount=0;
			makeBomb();
		}
		BombRectangles.clear();
		for(int i=0;i<BombXs.size();i++){
			batch.draw(bomb,BombXs.get(i),BombYs.get(i));
			BombXs.set(i,BombXs.get(i)-8);
			BombRectangles.add(new Circle(BombXs.get(i)+bomb.getWidth()/2 , BombYs.get(i)+bomb.getHeight()/2 ,bomb.getWidth()/2));
		}
	}
	public void makeCoin(){
		float height=random.nextFloat()*Gdx.graphics.getHeight();// it will give 0 to 1
		CoinYs.add((int)height);
		CoinXs.add(Gdx.graphics.getWidth());
	}
	public void makeBomb(){
		float height=random.nextFloat()*Gdx.graphics.getHeight();// it will give 0 to 1
		BombYs.add((int)height);
		BombXs.add(Gdx.graphics.getWidth());
	}
	public void checkOverlapping(){
		for(int i=0;i<CoinRectangles.size();i++){
			if(Intersector.overlaps(CoinRectangles.get(i),manRectangle)){
				Gdx.app.log("Message","coin collision");
				score++;
				coinSound.play();
				CoinRectangles.remove(i);
				CoinXs.remove(i);
				CoinYs.remove(i);
				break;
			}
		}
		for(int i=0;i<BombRectangles.size();i++){
			if(Intersector.overlaps(BombRectangles.get(i),manRectangle)){
				Gdx.app.log("Message","bomb collision");
				gameOverSound.play();
				gameState=2;
			}
		}
	}
	public void DisplayScore(){
		font.setColor(Color.WHITE);
		font.getData().setScale(10);//size
		font.draw(batch,String.valueOf(score),100,200);
	}

	@Override
	public void render (float delta) {
		batch.begin();
		batch.draw(background,0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)){
			// Do something
			game.setScreen(new MenuScreen(game));
		}

		if(gameState==1){
			//Live
			ActivateCoinFactory();
			ActivateBombFactory();
			setManPosition();
			checkOverlapping();
			DisplayScore();
		}else if(gameState==0){
			if(Gdx.input.justTouched()){
				gameState=1;
			}
			DisplayMan();
			DisplayScore();
		}else if(gameState==2){

			velocity=0;
			CoinXs.clear();
			CoinYs.clear();
			BombXs.clear();
			BombYs.clear();
			CoinRectangles.clear();
			BombRectangles.clear();
			coinCount=0;
			bombCount=0;
			DisplayMan();
			DisplayScore();
			if(Gdx.input.justTouched()){
				gameState=1;
				score=0;
				manY=Gdx.graphics.getHeight()/2;
			}

		}

		batch.end();

	}

	@Override
	public void show() {

	}



	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose () {
		batch.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode== Input.Keys.BACK){
			Gdx.input.setCatchBackKey(false);
			game.setScreen(new MenuScreen(game));
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
