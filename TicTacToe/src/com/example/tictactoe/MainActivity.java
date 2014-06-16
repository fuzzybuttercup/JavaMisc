package com.example.tictactoe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.*;
import android.view.View;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity {

	ImageButton[][] Tiles = new ImageButton[3][3];
	Vibrator vibrator;
	State playerState = State.X;
	SocketClientAsync socket;
	
	enum State {
		Empty,
		X,
		O,
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        
        
        Tiles[0][0] = (ImageButton)findViewById(R.id.Tile_00);
        Tiles[0][1] = (ImageButton)findViewById(R.id.Tile_01);
        Tiles[0][2] = (ImageButton)findViewById(R.id.Tile_02);
        Tiles[1][0] = (ImageButton)findViewById(R.id.Tile_10);
        Tiles[1][1] = (ImageButton)findViewById(R.id.Tile_11);
        Tiles[1][2] = (ImageButton)findViewById(R.id.Tile_12);
        Tiles[2][0] = (ImageButton)findViewById(R.id.Tile_20);
        Tiles[2][1] = (ImageButton)findViewById(R.id.Tile_21);
        Tiles[2][2] = (ImageButton)findViewById(R.id.Tile_22);
        
        
        for(ImageButton[] tileRow :Tiles){
        	for(ImageButton tile :tileRow){
        		tile.setImageResource(R.drawable.blank);
        	}
        }
    }
    
    public void Connect()
    {
    	socket = new SocketClientAsync();
    	try {
			socket.Connect("192.168.0.110");
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    }
    
    public void tileClicked(View v)
    {
    	int id = v.getId();
    	switch(id)
    	{
    	case R.id.Tile_00:
    		clicked(0,0);
    		break;
    	case R.id.Tile_01:
    		clicked(0,1);
    		break;
    	case R.id.Tile_02:
    		clicked(0,2);
    		break;
    	case R.id.Tile_10:
    		clicked(1,0);
    		break;
    	case R.id.Tile_11:
    		clicked(1,1);
    		break;
    	case R.id.Tile_12:
    		clicked(1,2);
    		break;
    	case R.id.Tile_20:
    		clicked(2,0);
    		break;
    	case R.id.Tile_21:
    		clicked(2,1);
    		break;
    	case R.id.Tile_22:
    		clicked(2,2);
    		break;
    	}
    }
    
    private void clicked(int x, int y)
    {
    	switch(getState(Tiles[x][y]))
    	{
		case Empty:
			sendMove(x, y);
			break;
		case O:
		case X:
			cantMove(x, y);
		default:
			
			break;
    	}
    }
    
    private State getState(View v)
    {
    	String tag = v.getTag().toString().toUpperCase();
    	
    	if(tag == "X")
    		return State.X;
    	else if(tag == "O")
    		return State.O;
    	else if(tag == "" || tag == " ")
    		return State.Empty;
    	else
    		return State.Empty;
    	
    }
    private void setState(ImageButton v, State state)
    {
    	switch(state)
    	{
		case Empty:
			v.setTag(" ");
			v.setImageResource(R.drawable.blank);
			break;
		case O:
			v.setTag("O");
			v.setImageResource(R.drawable.tile_o);
			break;
		case X:
			v.setTag("X");
			v.setImageResource(R.drawable.tile_x);
			break;
    	}
    }
    private void cantMove(int x, int y)
    {
    	vibrator.vibrate(100);
    }
    private void sendMove(int x, int y)
    {
    	setState(Tiles[x][y], playerState);
    	try {
    		socket.WriteCommand("Move", Integer.toString(x), Integer.toString(y));
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		System.out.println(e);
    	}
    	
    }
    private void recieveMove()
    {
    	String[] command = new String[0];
    	try {
			command = socket.ReadCommand();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try{
	    	if(command.length > 0)
	    	{
	    		
	    		if(command[0] == "MOVE" && command.length >= 3)
	    		{
	    			int x, y;
	    			x = Integer.parseInt(command[1]);
	    			y = Integer.parseInt(command[1]);
	    			
	    			setState(Tiles[x][y], playerState == (State.O)? State.X : State.O);
	    		}
	    		else if(command[0] == "REQUESTBOARD")
	    			sendBoard();
	    		
	    		else if(command[0] == "RECEIVEBOARD")
	    			recieveBoard();
	    		else
	    			showMessage("Recieved Unknown Command.");
	    		
	    	}
    	}
    	catch(Exception e){
    		showMessage(e.getMessage());
    		e.printStackTrace();
    	}
    }
    private void recieveBoard()
    {
    	
    }
    private void sendBoard()
    {
    	
    }
    private void showMessage(String text)
    {
    	Context context = getApplicationContext();	
    	Toast toast = Toast.makeText(context, text, 1000);
    	toast.show();
    }
}
