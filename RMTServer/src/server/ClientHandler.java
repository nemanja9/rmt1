
package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

import klijent.Client;



public class ClientHandler extends Thread {

	BufferedReader clientInput = null;
	PrintStream clientOutput = null;
	Socket socket = null;
	String username;
	String trenutni = null;

	public static LinkedList<Korisnik> korisnici = new LinkedList<Korisnik>();

	public void unosUListu() {
		try (BufferedReader br = new BufferedReader(new FileReader("data/log.txt"))) {
			String line;
			int i = 1;
			String user = "";
			String pass = "";

			while ((line = br.readLine()) != null) {
				if (i % 2 != 0)
					user = line;
				else {
					pass = line;
					Korisnik e = new Korisnik(user, pass);
					korisnici.add(e);
					// clientOutput.println(e.getUsername());
					// clientOutput.println(e.getPassword());

				}

				i++;
			}
			// clientOutput.println("ima ih " + korisnici.size());
		} catch (FileNotFoundException e1) {

			e1.printStackTrace();
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	}

	private static boolean checkString(String str) {

		char ch;
		boolean velikoSlovo = false;
		boolean maloSlovo = false;
		boolean broj = false;
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
			if (Character.isDigit(ch)) {
				broj = true;
			} else if (Character.isUpperCase(ch)) {
				velikoSlovo = true;
			} else if (Character.isLowerCase(ch)) {
				maloSlovo = true;
			}
			if (broj && velikoSlovo && maloSlovo)
				return true;
		}
		return false;
	}

	private String unos() {
		String ulaz = "";
		try {
			ulaz = clientInput.readLine();
			if (ulaz.startsWith("***quit")) {
				Server.onlineUsers.remove(this);
				socket.close();
			}
			return ulaz;
		} catch (Exception e) {
			clientOutput.println("Bacen izuzetak.");
		}
		return null;
	}

	public ClientHandler(Socket socketForCom) {
		socket = socketForCom;

	}

	public void ispisIstorije(String racun, String rezultat, String user) {

		String putanja = "data/istorija/" + user + ".txt";
		
		try (FileWriter fw = new FileWriter(putanja, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			out.println(racun + " = " + rezultat);

		} catch (IOException e1) {
			e1.getMessage();
		}
		
	}

	public void privatnostIstorije() {
		File dir = new File("/Users/nemanjajurisic/eclipse-workspace/rmt/RMT Klijent/istorija");
		
		if(dir.isDirectory() == false) {
			// nije folder, nista ne radi
			return;
		}
		
		System.out.println();
		File[] listFiles = dir.listFiles();
		for(File file : listFiles){
			file.delete();
			}
		
	}
	public void run() {

		boolean ulogovan = false;
		try {

			clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			clientOutput = new PrintStream(socket.getOutputStream());

			unosUListu();
			privatnostIstorije();
			

			clientOutput.println("Dobrodosli na server za racunanje.");
			clientOutput.println("Za regisraciju novog naloga izaberite opciju 1, ");
			clientOutput.println("Za logovanje na postojeci nalog izaberite opciju 2, ");
			clientOutput.println("Za nastavak bez registracije izaberite opciju 3. ");

			String izborRegStr = unos();

			int izborReg = 0;
			try {
				izborReg = Integer.parseInt(izborRegStr);
			} catch (NumberFormatException e1) {

				clientOutput.println("Niste uneli izbor lepo. ");
				izborReg = -1;

			}

			while (izborReg == -1) {
				
				clientOutput.println("Unesite ponovo: ");
				izborRegStr = unos();
				try {
					izborReg = Integer.parseInt(izborRegStr);
				} catch (NumberFormatException e1) {

					clientOutput.println("Niste uneli izbor lepo. ");
					izborReg = -1;

				}
			}
			switch (izborReg) {
			case 1:
				boolean validUser = false;
				boolean postoji = false;

				while (!validUser) {
					clientOutput.println("Unesite korisnicko ime za novog korisnika: ");
					String user = unos();
					if (user == null || user.length() <= 1) {
						clientOutput.println("Korisnicko ime mora biti duze od jednog karaktera! ");
						continue;
					}

					for (int i = 0; i < korisnici.size(); i++) {
						if (korisnici.get(i).getUsername().equals(user)) {
							clientOutput
									.println("Korisnik sa korisnickim imenom " + user + " postoji. Pokusajte ponovo.");
							postoji = true;
							break;
						}

					}
					if (postoji) {
						postoji = false;
						continue;
					}
					clientOutput.println("Unesite lozinku: ");
					String pass = unos();

					while (pass == null || !checkString(pass) || pass.length() < 8) {
						clientOutput.println("Lozinka nije dobra, unesite novu: ");
						pass = unos();
					}

					validUser = true;
					Korisnik e = new Korisnik(user, pass);
					korisnici.addLast(e);
					ulogovan = true;
					trenutni = user;
					username = user;
					try (FileWriter fw = new FileWriter("data/log.txt", true);
							BufferedWriter bw = new BufferedWriter(fw);
							PrintWriter out = new PrintWriter(bw)) {
						out.println(e.getUsername());
						out.println(e.getPassword());
					} catch (IOException e1) {

					}
				}
				break;
			case 2:
				clientOutput.println("Unesite vas username: ");
				String usernn = unos();

				boolean postojiNalog = false;
				for (int i = 0; i < korisnici.size(); i++) {
					if (korisnici.get(i).getUsername().contains(usernn)) {
						clientOutput.println("Korisnicko ime pronadjeno. Unesite sifru: ");
						postojiNalog = true;
						boolean validPass = false;
						while (!validPass) {
							String passs = unos();
							if (passs.equals(korisnici.get(i).getPassword())) {
								validPass = true;
								clientOutput.println("Lozinka upsesno unesena.");
								ulogovan = true;
								trenutni = usernn;
								username=usernn;
								break;
							}

							else
								clientOutput.println("Lozinka netacna, pokusajte ponovo.");
						}
						if (validPass)
							break;
					}
				}
				if (!postojiNalog) {
					clientOutput.println(
							"Uneti username ne postoji medju registrovanim korisnicima. Nastavljate kao gost. ");
				}

				break;
				
			default:
				
				break;
			}

			if (trenutni == null)
				clientOutput.println(">>> Dobrodosli \"GOST\" \nZa izlazak unesite ***quit");
			else
				clientOutput.println(">>> Dobrodosli " + trenutni + "\nZa izlazak unesite ***quit");

			if (!ulogovan) {
				clientOutput.println("Posto niste ulogovani, imate pravo na samo 3 racunice. Use them wisely! ");
			}
			// ------------------------------------------------------------------------------------//
			int brojUlogovan = 0;
			while (true) {

				brojUlogovan++;
				if (!ulogovan && brojUlogovan >= 4) {
					clientOutput.println("Izvrsili ste 3 racunice. Dovidjenja. ");
					break;

				}
				if(ulogovan) {
					clientOutput.println(
							"Za sabiranje unesite 1, za oduzimanje unesite 2, za mnozenje unesite 3, za deljenje unesite 4, za istoriju 5.");
				}else {
					clientOutput.println(
							"Za sabiranje unesite 1, za oduzimanje unesite 2, za mnozenje unesite 3, za deljenje unesite 4.");
				}
				
				String izborString = unos();
				if (izborString == null || izborString.startsWith("***quit")) {
					break;
				}

				int izbor = 0;
				try {
					izbor = Integer.parseInt(izborString);
				} catch (NumberFormatException e1) {

					clientOutput.println("Niste uneli izbor lepo.");
				}

				switch (izbor) {
				case 1:
					clientOutput.println("Izabrali ste zbir. Unesite izraz u obliku X+Y: ");
					String input = unos();
					String[] niz = input.split("\\+");

					clientOutput.println("Zbir je: ");

					try {
						int rezultat = Integer.parseInt(niz[0]) + Integer.parseInt(niz[1]);
						clientOutput.println(rezultat);
						if(trenutni != null)
						ispisIstorije(input, Integer.toString(rezultat), trenutni);
					} catch (Exception e) {
						clientOutput.println("Niste lepo uneli");

					}

					break;
				case 2:
					clientOutput.println("Izabrali ste razliku. Unesite izraz u obliku X-Y: ");
					String inputt = unos();
					String[] nizz = inputt.split("\\-");

					clientOutput.println("Razlika je: ");

					try {
						int rezultat = Integer.parseInt(nizz[0]) - Integer.parseInt(nizz[1]);
						clientOutput.println(rezultat);
						if(trenutni != null)
						ispisIstorije(inputt, Integer.toString(rezultat), trenutni);
					} catch (Exception e) {
						clientOutput.println("Niste lepo uneli");

					}

					break;
				case 3:
					clientOutput.println("Izabrali ste mnozenje. Unesite izraz u obliku YxZ: ");
					String inputtt = unos();
					String[] nizzz = inputtt.split("x");

					clientOutput.println("Rezultat je: ");

					try {
						int rezultat = Integer.parseInt(nizzz[0]) * Integer.parseInt(nizzz[1]);
						clientOutput.println(rezultat);
						if(trenutni != null)
						ispisIstorije(inputtt, Integer.toString(rezultat), trenutni);
					} catch (Exception e) {
						clientOutput.println("Niste lepo uneli");

					}

					break;
				case 4:
					clientOutput.println("Izabrali ste deljenje. Unesite izraz u obliku X/Y: ");
					String inputttt = unos();
					String[] nizzzz = inputttt.split("\\/");

					try {
						if (Double.parseDouble(nizzzz[1]) == 0) {
							clientOutput.println("Ne mozete deliti nulom! ");
							continue;
						} else
							clientOutput.println("Rezultat je: ");

						double rezultat = Double.parseDouble(nizzzz[0]) / Double.parseDouble(nizzzz[1]);
						clientOutput.println(rezultat);
						if(trenutni != null)
						ispisIstorije(inputttt, Double.toString(rezultat), trenutni);
					} catch (Exception e) {
						clientOutput.println("Niste lepo uneli");

					}

					break;
				case 5:
					if(ulogovan) {
						istorija(username);
					}
					break;
				default:
					clientOutput.println("Niste uneli ni jednu od opcija. ");
					break;
				}
			}

			Server.onlineUsers.remove(this);
			socket.close();

		} catch (IOException e) {
			Server.onlineUsers.remove(this);
			for (ClientHandler klijent : Server.onlineUsers) {
				if (klijent != this) {
					klijent.clientOutput.println(">>> korisnik " + username + " je napustio sobu. ");
				}
			}

			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void istorija(String fajl) {
		try (BufferedReader br = new BufferedReader(new FileReader("data/istorija/"+fajl+".txt"))) {
			String line;

			String celaIstorija = "ISTORIJA";
//			clientOutput.println("ISTORIJA:");
			
			while ((line = br.readLine()) != null) {
				
				
//				clientOutput.println(line);	
				celaIstorija = celaIstorija + "\n" +line;
			}
//			clientOutput.println("KRAJ ISTORIJE");
			celaIstorija = celaIstorija + "\n" + "KRAJ ISTORIJE";
			
			Client.istorija(celaIstorija);
			clientOutput.println(celaIstorija);
			
		} catch (FileNotFoundException e1) {

			e1.printStackTrace();
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		
	}

}
