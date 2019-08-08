import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
public class HeartsDriver{
	private static Scanner scan = new Scanner(System.in);
	private static ArrayList<String> settings;

	public static void main(String args[]){
		Hearts3 game = gameSettings();

	}
	private static Game3 gameSettings(){
		displaySettings();
		print("Change Settings? y/n");
		if(scanWord().equals("y"))
			changeSetting();
		Hearts3 newGame = new Hearts3(numHumans, numBots, passMethod, changeSeats, cheating, humanNames, botNames);

		return newGame;
	}
	private static void displaySettings{
		printFile("default.txt");
	}
	private static void print(String word){
		System.out.println(word);
	}
	private static String scanWord(){
		String word=scan.next();
		scan.nextLine();
		return word;
	}
	private static void printFile(String name){
		try{
			File default = new File(name);
			reader =new Scanner(default);
			while(reader.hasNext()){
				print(reader.next());
				reader.nextLine();
			}
			reader.close();
		}
		catch(FileNotFoundException e){
			print("No "+name+" file found");
		}
	}
	private static void replaceFile(String name){
		settings = storeFile(name);
		try{
			FileWriter inserts = new FileWriter(name);
			for(int i; i<settings.size(); i++){
				print("Replace: "+settings.get(i)+"with: ");
				inserts.write(scan.next());
				scan.nextLine();
			}
		}
		catch(FileNotFoundException e){
		}
		finally{
			inserts.close();
		}
	}
	private static ArrayList<String> storeFile(String name){
		ArrayList<String> container = new ArrayList<String>();
		try{
			File default = new File(name);
			reader =new Scanner(default);
			while(reader.hasNext()){
				settings.add((reader.next());
				reader.nextLine();
			}
		}
		catch(FileNotFoundException e){
		}
		finally{
			reader.close();
			return container;
		}
	}
}
