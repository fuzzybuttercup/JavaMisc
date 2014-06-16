package com.example.tictactoe;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.PriorityQueue;


public final class SocketClientAsync implements Runnable {
	
	private final int timeout = 250;

	private ObjectInputStream input;
	private ObjectOutputStream output;
	private Socket socket;
	private Thread thread;
	private String serverAddr;
	private volatile PriorityQueue<String> ToSend;
	private volatile PriorityQueue<String> Recieved;
	private volatile Exception error = null;
	
	@Override
	public void run() {

		try {
			socket = new Socket(serverAddr, 4000);
			socket.setSoTimeout(timeout);
			input = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
			
			ToSend = new PriorityQueue<String>(10);
			Recieved = new PriorityQueue<String>(10);
			
			while (true) {
				update();
			}
		} catch (IOException e) {
			error = e;
			System.out.println("Failed to connect");
		}
	}

	private void update() {

		read();
		write();
		
	}
	private void read() {
		String Message = null;
		
		try {
			Message = (String) input.readObject();
			if(Message != null)
				Recieved.add(Message);
			} catch (SocketTimeoutException e) {
				
			} catch (Exception ex) {
				error = ex;
		}
	}
	private void write() {
		while(!ToSend.isEmpty())
		{
			String message = ToSend.poll();
			if (message != null) {
				try {
					output.writeObject((Object) message);
				} catch (IOException e) {
					System.out.println("Couldn't Write");
				}
			}
		}
	}
	public void ConnectAsync(String ServerAddr){
		serverAddr = ServerAddr;
		
		thread = new Thread(this);
		thread.start();
	}
	public void Connect(String ServerAddr) throws Exception {
		
		ConnectAsync(ServerAddr);
			
		while(true)
		{
			if(Connected())
				break;
			CheckForError();
		}
	}

	public int closeCrap() {
		try {
			input.close();
			output.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void Write(String message) throws Exception {
		CheckForError();
		ToSend.add(message);
	}
	public void WriteCommand(String command, String[] args) throws Exception
	{
		String message;
		message = "<" + command + ">";
		if(args.length > 0)
		{
			message += "[";
			for(String arg :args)
			{
				message += arg;
			}
			message += "]";
		}
		
		Write(message);
	}
	public void WriteCommand(String command, String argA, String argB, String argC) throws Exception{
		WriteCommand(command, new String[] {argA, argB, argC});
	}
	public void WriteCommand(String command, String argA, String argB) throws Exception{
		WriteCommand(command, new String[] {argA, argB});
	}
	public void WriteCommand(String command, String argA) throws Exception{
		WriteCommand(command, new String[] {argA});
	}
	public String Read() throws Exception {
		CheckForError();
		return Recieved.poll();
	}
	public String[] ReadCommand() throws Exception
	{
		String fromClient = Read();
		String command, args;
		int start, stop;
		ArrayList<String> array = new ArrayList<String>();
		
		fromClient = fromClient.trim();
		fromClient = fromClient.toUpperCase();
		
		start = fromClient.indexOf('<') + 1;
		stop = fromClient.indexOf('>');
		if(start == 0 || stop == -1)
			command = "";
		else
			command = fromClient.substring(start, stop);
		
		start = fromClient.indexOf('[') + 1;
		stop = fromClient.indexOf(']');
		if(start == 0 || stop == -1)
			args = "";
		else
			args = fromClient.substring(start, stop);
		
		array.add(command);
		start = 0;
		
		while(true)
		{
			stop = args.indexOf(',', start + 1);
			
			if(stop == -1)
			{
				stop = args.length();
				array.add(args.substring(start, stop).trim());
				break;
			}

			array.add(args.substring(start, stop).trim());
			start = stop;
			start++;
		}
		return array.toArray(new String[array.size()]);
	}

	public Boolean CanRead(){
		try	{
			return !Recieved.isEmpty();
		}catch(Exception e){
			return false;
		}
				
	}
	public Boolean Connected(){
		return socket.isConnected();
	}
	public void CheckForError() throws Exception{
		if(error != null)
			throw error;
	}
	

}