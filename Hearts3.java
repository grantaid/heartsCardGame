import java.util.ArrayList;
public class Hearts3{
	int numHumans;
	int numBots;
	String passMethod;
	boolean changeSeats;
	boolean cheating;
	ArrayList<String> humanNames;
	ArrayList<String> botNames;
	public Hearts3(int numHumans, int numBots, String passMethod, boolean changeSeats,
	boolean cheating, ArrayList<String> humanNames, ArrayList<String> botNames){
			this.numHumans=numHumans;
			this.numBots=numBots;
			this.passMethod=passMethod;
			this.changeSeats=changeSeats;
			this.cheating=cheating;
			this.humanNames=humanNames;
			this.botNames=botNames;
	}
}
