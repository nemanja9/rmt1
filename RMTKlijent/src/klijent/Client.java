package klijent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

	static Socket socket = null;
	static BufferedReader serverInput = null;
	static PrintStream serverOutput = null;
	static BufferedReader konzola = null;
	
	public static void istorija(String istorija) {
		try (FileWriter fw = new FileWriter("klijentLOG.txt");
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
				out.print(istorija);

		} catch (IOException e1) {
			e1.getMessage();
		}

		
		
	}

	public static void main(String[] args) {
		try {
			socket = new Socket("localhost", 9000);

			serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			serverOutput = new PrintStream(socket.getOutputStream());

			konzola = new BufferedReader(new InputStreamReader(System.in));

			new Thread(new Client()).start();

			String input;
			boolean end = true;
			while (end) {
				input = serverInput.readLine();
				if (input != null)  {
					System.out.println(input);
				}
				
				if (input==null || input.startsWith("Izvrsili ste 3")) {
					end=false;
				}
//				if (input.startsWith("ISTORIJA:")) {
//					try (FileWriter fw = new FileWriter("klijentLOG.txt",false);
//							BufferedWriter bw = new BufferedWriter(fw);
//							PrintWriter out = new PrintWriter(bw)) {
//							out.print(input);
//
//					} catch (IOException e1) {
//						e1.getMessage();
//					}
					
//				}
				

			}
			socket.close();
		} catch (

		UnknownHostException e) {

			System.out.println("UNKNOWN HOST");

		} catch (IOException e) {
			System.out.println("SERVER IS DOWN");
		}
	}

	@Override
	public void run() {
		try {
			String msg;

			while (true) {
				msg = konzola.readLine();
				serverOutput.println(msg);

				if (msg.startsWith("***quit")) {
					break;
				}
		} }catch (Exception e) {

		}

	}
}

