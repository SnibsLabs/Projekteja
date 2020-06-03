import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class TicClient<E>{
	
    private static DataOutputStream out;				//liikenne
    private static DataInputStream in;				
    private Socket sock = null;						//user-soketti
    private static ServerSocket ss = null;			//Server-soketti
    
    private static String IP=null;			//tiedot
    private static String port=null;		

    private Thread ticSai = null; 			//saie


    
    
public static void main(String [ ] args){
	
	Scanner intern = new Scanner(System.in);
	
	
	System.out.println("Syötä IP yhdistääksesi peliin");
	System.out.println("tai syötä 0 pelataksesi paikallisesti");
	

	while (true) {				//pelaajalta IP
		
		IP = intern.nextLine();
	
		if (IP.equals("0")) {
			IP="localhost";
			break;
		}
	
		if (IP.isEmpty()) { 	//jos pelaaja syöttää tyhjän
			System.out.println("Syötä IP tai 0");
		}
		else {
			continue;
		} 
	}

	while (true) {				//pelaajalta portti
	System.out.println("Syötä portti (oletus 1234)");
	
	port = intern.nextLine();
	
	if (port.isEmpty()) {
		System.out.println("Syötä portti");
	}
	else {
		break;	}	
	}
	
//	intern.close();
    TicClient<Object> X = new TicClient<Object>();
    int portti = Integer.valueOf(port);
    boolean yhteys = false;
    
    
    yhteys = X.connect(IP, portti); //koetetaan yhdistaa
    
    
    if (yhteys!=false) {

    	X.TicTac(1,in,out);
    
    }
    
    if (yhteys!=true){  	//perustetaan palvelin
    	try {
    		ss = new ServerSocket(1234);
    		System.out.println("Peliä ei löytynyt, luodaan oma");
    		System.out.println("Kuunnellaan porttia 1234");
    		X.ServerCon();
    	} catch (Exception e) {
    		System.out.println("Eeee" + e);
    		ss = null;
    	}
    
    
    	
    }
    //System.exit(0);
}//main




public boolean connect(String IP, int portti) { //itse asiakkaana
	int yhteys = 0;
	try {
        sock = new Socket(IP, portti);     // yhteydenotto
    } catch (Exception e) {
    	yhteys = 1;
    	System.out.println("Yhteys epäonnistui " + e);            
    }

	if (yhteys ==0) {
	 try {
         in = new DataInputStream(sock.getInputStream());
         out = new DataOutputStream(sock.getOutputStream());        
	 } catch (Exception e) {
         System.out.println("" + e);
         if (sock != null) {
             try {
                 sock.close();  //suljetaan varalta ongelman ilmetessä
             } catch (Exception e2) {
            	 System.out.println(e2);
             }
         }
     }
	 
	 return true;
	 }
	return false;
}


private void ServerCon() { //itse serverinä

    if (ss == null)
        return;
    
    try {
    	Socket cs = ss.accept();
    		
    	TicClient<E>.Pelaaja janne = new Pelaaja(cs);
    	//uusi client -> uusien kuuntelun lopetus
    	
    	ticSai =new Thread (janne);
    	ticSai.start();
    	
    	}catch (Exception e) {
            System.err.println("" + e);
            ss = null;
}
}

public class Pelaaja implements Runnable { //yhdistyjältä datavirrat

    Socket asiakas;
    DataOutputStream out = null;
    DataInputStream in = null;
//    String nimi = null;

    Pelaaja(Socket cs) {
        super();
        asiakas = cs;
    }

    public void run() {

        try {
        	System.out.println("Vastustaja yhdisti: " + asiakas.getInetAddress() +
                    ":" + asiakas.getPort());
    	
            in = new DataInputStream(asiakas.getInputStream());
            out = new DataOutputStream(asiakas.getOutputStream());
            
            TicTac(2,in,out);
            }catch (Exception e) {
                System.err.println("Chattaaja.run: " + e);
            } 
        }
}

public void TicTac(int vuoro, DataInputStream sis, DataOutputStream out){ //itse peli
	
	
	Scanner pelaa = new Scanner(System.in);
	int move = -1;				//siirto
	int gameState = 1;			//1 pelaa, 2 voitit, 3 hävisit
	HashMap<Integer, String> pelitaulu = new HashMap<Integer, String>();		//piirretään X O
	int turn = vuoro;			//kumman vuoro
	
	puhdas(pelitaulu);			//populoidaan hashmap
	
	System.out.println("");
	System.out.println("Syöttämällä alla olevan kuvan mukaan numeroita (1-9)");
	System.out.println("voit asettaa merkkisi pelilaudalle.");
	System.out.println("Lopeta peli syöttämällä 0");
	System.out.println("||1||2||3||"); 
	System.out.println("||4||5||6||"); 
	System.out.println("||7||8||9||");
	System.out.println("");
	
	gameState = boardDraw(pelitaulu); //piirretään ja tarkastetaan
	

	while (true) {
	while(gameState==1) {
		
		System.out.println("pelitilanne numerona " + gameState);
		System.out.println("vuorosi alkaa");
		if (turn==1) {				//pelaajalta siirto
					
				System.out.println("Syötä siirtosi");
		
			
				while(pelaa.hasNext()){
				String liike = pelaa.next();
				move = Integer.parseInt(liike);
				if (move>9 || move<0){
					System.out.println("Syötä numero 0-9"); //tarkistus
				}
				if (move == 0) {
					System.out.println("olet lopettanut pelin");
					try {
						out.writeInt(move);
						out.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.exit(0);
				}
				if (move<10 && move>-1) {
					System.out.println("Hyvä");
					break;}}	//jos syötetty int on hyväksyttävä
			
			
			

			
			if (pelitaulu.get(move).contains(" ")) { //jos ei vielä täytetty
				pelitaulu.put(move, "O");
				
				try {
					out.writeInt(move);
					out.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("Lähetetty siirto");
				gameState = boardDraw(pelitaulu);
				turn=2;	
				break;
				
			}
			if (pelitaulu.get(move).contains("X")||pelitaulu.get(move).contains("O")) {
				System.out.println("Ruutu on jo täytetty");
				continue;
			}
			
				
		}
		
	if (turn ==2) {	//otetaan vastustajan liike
		
		System.out.println("Vastustajan vuoro alkaa");
		
		
		try {
			int vihLiike = sis.readInt();
			
			if (vihLiike == 0) {
				System.out.println("Vastustaja on lopettanut, lopetetaan");
				System.exit(0);
			}
			
			System.out.println("Vastustaja lähetti siirtonsa");
			pelitaulu.put(vihLiike, "X");
			gameState = boardDraw(pelitaulu);
		    turn=1;		//oma vuoro
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			break;
		}

		

	}
	}
	
	if (gameState!=1) {	//voittotilanteet / tasapeli
		if (gameState==2){
			System.out.println("VOITIT");
			turn = 2;
		}
		if (gameState==3){
			System.out.println("HÄVISIT");
			turn =1;
		}
		if (gameState==4){
			System.out.println("tasapeli...");
		}
		puhdas(pelitaulu);
		gameState=1;
	}
	}
}//TIKTAK
	
	
	




public static int boardDraw(HashMap<Integer, String> pelitaulu) { // tulostaa laudan ja tarkastaa tuloksen
	System.out.println("||" +pelitaulu.get(1)+ "||" +pelitaulu.get(2)+ "||" +pelitaulu.get(3) + "||");
	
	System.out.println("||" +pelitaulu.get(4)+ "||" +pelitaulu.get(5)+ "||" +pelitaulu.get(6) + "||");
	
	System.out.println("||" +pelitaulu.get(7)+ "||" +pelitaulu.get(8)+ "||" +pelitaulu.get(9) + "||");
	

	for (int a = 1; a < 9; a++) { //switch case toimii paremmin
		
		String line = " ";
		int b = 1;
		switch (a) { //kaikki voittolinjat
		case 1:
			line = pelitaulu.get(1)+pelitaulu.get(2)+pelitaulu.get(3);
			b = winnings(line);
			if (b>1){
				return b;
			}
		case 2:
			line = pelitaulu.get(4)+pelitaulu.get(5)+pelitaulu.get(6);
//			line = pelitaulu.get(4&5&6);
			b = winnings(line);
			if (b>1){
				return b;
			}
		case 3:
			line = pelitaulu.get(7)+pelitaulu.get(8)+pelitaulu.get(9);
//			line = pelitaulu.get(7&8&9);
			b = winnings(line);
			if (b>1){
				return b;
			}
		case 4:
			line = pelitaulu.get(1)+pelitaulu.get(4)+pelitaulu.get(7);
			//line = pelitaulu.get(1&4&7);
			b = winnings(line);
			if (b>1){
				return b;
			}
		case 5:
			line = pelitaulu.get(2)+pelitaulu.get(5)+pelitaulu.get(8);
		//	line = pelitaulu.get(2&5&8);
			b = winnings(line);
			if (b>1){
				return b;
			}
		case 6:
			line = pelitaulu.get(3)+pelitaulu.get(6)+pelitaulu.get(9);
			//line = pelitaulu.get(3&6&9);
			b = winnings(line);
			if (b>1){
				return b;
			}
		case 7:
			line = pelitaulu.get(1)+pelitaulu.get(5)+pelitaulu.get(9);
		//	line = pelitaulu.get(1&5&9);
			b = winnings(line);
			if (b>1){
				return b;
			}
		case 8:
			line = pelitaulu.get(3)+pelitaulu.get(5)+pelitaulu.get(7);
			//line = pelitaulu.get(3&5&7);
			b = winnings(line);
			if (b>1){
				return b;
			}
			
			

		
		
		}//switch
		for (int c = 1; c < pelitaulu.size(); c++) { //tasapelitarkkailu
			if (pelitaulu.get(c) == " ") {
		        return 1;
		      }
		} 
}
	return 4;
}

public static int winnings(String line) {	//onko voittolinja

	if (line.equals("XXX")) {
		return 3;
	} 
	else if (line.equals("OOO")) {
		return 2;
	}
	return 1;
}

public static HashMap<Integer, String> puhdas(HashMap<Integer, String> taulu) {
	
	for (int a = 0; a < 10; a++) { //alustus taululle (erillinen metodi?)
		taulu.put(a," ");
	}
	return taulu;
	
}

}

