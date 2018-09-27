//this change was made in the lab at the same time a change was made at home
//this change was made from the lab and uploaded to github
import java.util.Scanner;
import java.io.File;
//import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;
public class Hearts{
	private Scanner scan = new Scanner(System.in);
	File savedGames = new File("savedGames.txt");
	private Hearts contGame=null;
	//FileWriter fw = null;
	private String[] names = {"","Basil", "Tundi", "Junior", "Madison","Zen","Yunquan","Max","Nathanial","Tony","Jon","Jane","Myron","Spencer",
	"Garfield","Ahmed","Mike","Tatiana","Darnel","Bing","Mashood","Makseen","Karen","Arlette","Lyle"};
	private String[] cardNames = {"Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten","Jack","Queen","King","Ace","The Fool","The Magician",
	"High Priestess","The Empress","The Emperor","The Hierophant","The Lovers","The Chariot","Strength","The Hermit","Wheel of Fortune","Justice",
	"The Hanged Man","Death","Temperance","The Devil","The Tower","The Star","The Moon","The Sun","Judgement","The World"};
	private int numPlayers;
	private int initNumCardsInHand;
	private int[][] memory;
	private int[][] hand;
	private int[][] currNumCardsInSuit;
	private int[][][] currHigh;
	//temporary variables
	private int t, t2, t3, length, add,	suit, t4, t5, t6, value, weight, s;
	private int[] numValues;
	private String tempLine;
	private String tempLine2;
	private int numRound;
	private boolean winner, exit, found;
	private int playersRemaining;
	private int[] playersRemainingArray;
	int numCardsInDeck;
	int numDecks;
	//pos in hand, ordered in reverse
	int[][] isolatedLowAndCount;
	int[] goMoon;
	//1st dimension 0= position in hand, 1= positions in memory
	int[][] passPos;
	//maybe make one dimensional (temporary use only)
	int[][] weights;
	int[][][] weightTable;
	int topWeightPos;
	//number of queens that player pos has. number of 2's player pos has.
	//could have rule that if have 2 clubs, must play it (every time they start a round!)
	int[] hasQueen;
	int startingClub;
	int scenario;
	int direction, directionPossibilities, across, offset, circleOffset;
	int[] suitPlayed;
	int[] human;
	//weak if few cards in suit and value is low. Pos in isolatedLowAndCount.
	int weakestIsoLow;
	int[][] weakIsoLowAndCount;
	//stores hand pos of first card in each suit, -1 if no card
	int[][] suitPos;
	//counts number of isolated low cards in each suit
	int[][] isoLowSuit;
	int[][] brokenAndCount;

	public Hearts(){
		System.out.println("Enter the number of players:");
		numPlayers=findNFE(scan.nextLine());
		while(numPlayers<2){
			System.out.println("Not a valid number. Enter a number of players above 1:");
			numPlayers=findNFE(scan.nextLine());
		}
		System.out.println("Enter your player name:");
		names[0]=scan.nextLine();


		if(savedGames.isFile()){
			System.out.println("Would you like to continue a saved game? Enter Y or N:");
			tempLine=loopYN();
			if(tempLine.equals("Y")){
				Scanner saveScan;
				try{
					saveScan =new Scanner(savedGames);
				}
				catch(FileNotFoundException e){
					return;
				}
				tempLine="Q";
				while(saveScan.hasNext() && !tempLine.equals("Y")){
					tempLine=saveScan.nextLine();
					System.out.println("Continue: "+tempLine+" ? Enter Y or N:");
					tempLine2=loopYN();
				}
				if(tempLine2.equals("Y")){
					//use object reader
					FileInputStream fi = null;
					try{
						File oldGame = new File(tempLine);
						fi = new FileInputStream(oldGame);
						ObjectInputStream inStream = new ObjectInputStream(fi);
						contGame=(Hearts)inStream.readObject();
					}
					catch(IOException e){
				        System.out.println(e.getMessage());
				    }
					catch(ClassNotFoundException g){
						System.out.println(g.getMessage());
						return;
					}
			        // finally{
			        //     try{
			        //         fi1.close();
			        //         fi2.close();
			        //     }
			        //     catch(IOException e){
			        //         print(e.getMessage());
			        //     }
			        // }
				}
			}
		}
		numValues = new int[4];
		human=new int[numPlayers];
		//compares alternate numbers of cards per deck and picks num closest to 52
		if(52-(52/numPlayers)*numPlayers<(52/numPlayers+1)*numPlayers-52){
			numCardsInDeck=(52/numPlayers)*numPlayers;
		}
		else{
			numCardsInDeck=(52/numPlayers+1)*numPlayers;
		}
		add=numCardsInDeck%4;
		//stores the number of cards in each suit
		for(t=3;t>-1;t--){
			initNumCardsInHand=numCardsInDeck/4;
			numValues[t]=initNumCardsInHand;
			numValues[t]+=(add-->0)?(1):(0);
		}
		//test
		for(t=0;t<4;t++){
			System.out.println("suit "+(t+1)+" numValues = "+numValues[t]);
		}
		//precedence of * and / given to the leftmost one
		if((numCardsInDeck*(numPlayers*13/numCardsInDeck+1))/numPlayers-13 >
		13-(numCardsInDeck*(numPlayers*13/numCardsInDeck))/numPlayers){
			numDecks=numPlayers*13/numCardsInDeck;
		}
		else{
			numDecks=numPlayers*13/numCardsInDeck+1;
		}
		System.out.println("Decks: "+numDecks);
		//col1=value, col2=suit, col3=numleft, col4=num played by, remaining col hold player numbers of players
		memory = new int[numCardsInDeck][4+numPlayers];
		//first value is currentNumCardsInHand
		hand = new int[numPlayers][initNumCardsInHand+1];
		playersRemaining=numPlayers;
		//this array points to the player ID in hand[]
		playersRemainingArray= new int[numPlayers];
		for(t=0;t<numPlayers;t++){
			playersRemainingArray[t]=t;
		}
		//create memory (deck) should exclude lowest cards first and count num 2 clubs and assign new starting club if none
		for(t=0;t<4;t++){ //add all suits*their length
			for(t2=0;t2<numValues[t];t2++){ //add all values
				length=lengthAcrossSuits(t);
				if(numValues[t]>12){
					memory[length+t2][0]=t2;
				}
				//excludes lower values first
				else{
					memory[length+t2][0]=t2+13-numValues[t];
				}
				memory[length+t2][1]=t;
				memory[length+t2][2]=numDecks;
				if(t==0){
					startingClub=memory[length+t2][0];
				}
			}
		}
		System.out.println(Arrays.deepToString(memory));
		System.out.println(Arrays.deepToString(hand));
		//not reused for every player
		currHigh = new int[numPlayers][4][2];
		for(t2=0;t2<numPlayers;t2++){
			for(t=0;t<4;t++){
				currHigh[t2][t][0]=numValues[t]-1;
				currHigh[t2][t][1]=numDecks;
			}
		}
		//all cards in hand may be isolated and low, required for moon playing and passing
		isolatedLowAndCount = new int[numPlayers][hand[0][0]+1];
		passPos = new int[2][numPlayers][3]];
		//pos in weights corresponds to pos in hand
		weights= new int[numPlayers][initNumCardsInHand+1]
		weightTable = new int[numScenarios][4][numValues[3]];
		isoLowSuit=new int[numPlayers][4];
		// must account for strategy when have lots of spades?
		// if have 5/3rds of the spades the average player has (ratio should not! rise as number of players rise)
		// must account for direction of passing (counterclockwise is more safe for K and A of spades)
		//sets weightTable includes suit and face value weighting
		//for every suit
		for(t=0;t<4;t++){
			if(numValues[t]>12){
				t2=1;
			}
			else{
				t2=14-numValues[t];
			}
			while(t2<=numValues[suit]){
				if(t==2){ //spades
					if(t2>11){ // king or ace
						//if don't have many spades
						weightTable[0][t][t2-1]=t2*12;
						//if have queen and few spades passing counterclockwise || if have many spades
						weightTable[1][t][t2-1]=t2*10;
					}
					else if(t2==11){ //queen
					//if don't have many spades and passing !counterclockwise (passes after K and A)
					// || if don't have many spades and passing counterclockwise
					weightTable[0][t][t2-1]=t2*12;
					//if have many spades
					weightTable[1][t][t2-1]=t2*1;
					}
					else{
						weightTable[0][t][t2-1]=t2*7;
						weightTable[1][t][t2-1]=t2*7;
					}
				}
				else if(t==3){//Hearts
					weightTable[0][t][t2-1]=t2*8;
					weightTable[1][t][t2-1]=t2*8;
				}
				else if(t==0){//clubs
					if(t2==startingClub+1){//2ofclubs
						weightTable[0][t][t2-1]=(numValues[0]+1)*10;
						weightTable[1][t][t2-1]=(numValues[0]+1)*10;
					}
					else{
						weightTable[0][t][t2-1]=t2*10;
						weightTable[1][t][t2-1]=t2*10;
					}
				}
				else{//diamonds
					weightTable[0][t][t2-1]=t2*10;
					weightTable[1][t][t2-1]=t2*10;
				}
				t2++;
			}
		}
		currNumCardsInSuit=new int[numPlayers][4];
		//if number >2: clockwise=1, counterclockwise=2, across=3, none=0, determined by incrementing then %4
		//if 3: clockwise=1 or 3, counterclockwise=2 or 4, none=0, determined by incrementing then %5
		//if 2: use 3 for now
		direction=0;
		if(numPlayers<4){
			directionPossibilities=5;
		}
		else{
			directionPossibilities=4;
		}
		across=2;
		//how many cards of each suit played. %numPlayers to see if broken
		suitPlayed = {0,0,0,0};
		suitPos = new int[numPlayers][4];
		//needs also be called after loading a saved game
		play();
	}
	//sums all cards in suits before the parameter suit
	private int lengthAcrossSuits(int suit){
		int t=0;
		while(suit>0){
			t+=numValues[--suit];
		}
		return t;
	}
	private void play(){
		//start a new hand
		while(!winner && !exit){
			direction++;
			if(direction%directionPossibilities==2 || direction%directionPossibilities==4){
				offset=-1;//counterclockwise
			}
			else if(direction%directionPossibilities==0){
				offset=0;//no passing
			}
			else if(direction%directionPossibilities==3 && directionPossibilities==4){
				offset=across++;
				if(across>=numPlayers-1){
					across=2;
				}
			}
			else{
				offset=1;//clockwise
			}
			deal();
			pass();
			//find first player
			if(numDecks>1){
				player=Math.random()*numPlayers;
			}
			else{
				player=0;
			}
			found=false;
			while(!found){
				if(memory[hand[player][1]][0]==startingClub && memory[hand[player][1]][1]==0){
					found=true;
				}
				else{
					player++;
					if(player>=numPlayers){
						player=0;
					}
				}
			}
			//num players broken a suit and pos of each player
			brokenAndCount = new int[4][numPlayers+1];
			//until hand is empty
			for(round=0;round<hand[0][0];round++){
				//everyone else play a card
				//this round only
				playedCards = new int[numPlayers];
				//this round only
				int highestCard = 0;
				int roundSuit = -1;
				//finds weakest card if gomoon
				if(goMoon[t3]==1 &&
				isoLowSuit[currPlayer][memory[hand[currPlayer][isolatedLowAndCount[currPlayer][t2]]][1]]*100/memory[hand[currPlayer][isolatedLowAndCount[currPlayer][t2]]][0]
				>=isoLowSuit[currPlayer][memory[hand[currPlayer][isolatedLowAndCount[currPlayer][t2]]][1]]*100/memory[hand[currPlayer][isolatedLowAndCount[currPlayer][weakIsoLow]]][0]){

				}
				for(int follower=0;follower<numPlayers;follower++){
					//circle round past player 0
					if(player+follower>=numPlayers){
						player=-follower;
					}
					int currPlayer=player+follower;
					//trickWinner (player) leads a card (pos in memory) (set at end of loop)
					//if human
					if(human[currPlayer]==1){
						//check if breaking rules, restate rules
					}
					//if starting club AI
					else{
						if(follower==0){
							if(round==0){
								highestCard=startingClub;
								roundSuit=0;
								memorizeAndPlay(player,1);
							}
							else if(goMoon[player]==1){

							}
						}
						if(goMoon[player+t2]==1){
							//if one of first few rounds in suit should try to get rid of isolatedlow
							//or maybe always try to get rid of isolated low?
							//only get rid of isolated low in suits with few cards!
							if(haveSuit()==1){//haveSuit
								if(){//have isolatedlow and few cards in suit -->throw highest isolated low to deceive

								}
								else{//have isolatedlow and many cards || have no isolated low --> win

								}
							}
							else{//don't have suit

							}
							for(t3=1;t3<=isolatedLowAndCount[player+t2][0];t3++){
							topWeightPos=0;
							for(t3=1;t3<=hand[player+t2][0]];t3++){
								weights[player+t2][t3]=findMoonWeights(player+t2,t3);
								if(weights[player+t2][topWeightPos]<weights[player+t2][t3]){
									topWeightPos=t3;
								}
							}

						}
					}

			}
			if(!winner){
				System.out.println("Would you like to quit?")
				templine=loopYN();
				if(templine.equals("Y")){
					System.out.println("Would you like to save your game?");
					templine=loopYN(){
						if(templine.equals("Y")){
							//write to file
						}
					}
				}
			}
		}
	}
	private void memorizeAndPlay(int player, int handPos){
		memory[hand[player][handPos]][2]--;
		memory[hand[player][handPos]][3+player]++;
		suitPlayed[memory[hand[player][handPos]][1]++;
		removeFromHand(player,1);
		//check if iso
		checkRemoveFromIso(player,isolatedLowAndCount[player][0])
		updateSuitPos(player,1);
		}
	}
	//resets number of cards in deck remaining and deal all cards
	//should never remove Q spades from deck (implement!)
	private void deal(){
		for(t=0;t<4;t++){ //each suit
			for(t2=0;t2<numValues[t];t2++){ //the values in each suit
				length=lengthAcrossSuits(t); //the values up to this suit
				memory[length+t2][2]=numDecks; //assigns numDecks to track multiple cards
				//deal that card from all decks
				for(t3=0;t3<numDecks;t3++){
					int t4=(int)Math.random()*playersRemaining; //randomly selects a remaining player
					//1st col is num cards in hand
					//increases counter and stores the memory location in hand
					hand[playersRemainingArray[t4]][++hand[playersRemainingArray[t4]][0]]=length+t2;
					//counts Queen
					if(t==3 && t2==11){
						hasQueen[playersRemainingArray[t4]]++;
					}
					//increments currNumCardsInSuit
					currNumCardsInSuit[playersRemainingArray[t4]][memory[length+t2][1]]++;
					if(hand[playersRemainingArray[t4]][0]==initNumCardsInHand){
						playersRemainingArray[t4]=playersRemainingArray[--playersRemaining];
					}
				}
			}
		}
	}
	//returns position of card to pass in memory, returns all players and 3 cards
	//when remove something from hand need to remake isolatedLowAndCount
	private void pass(){
		//for every player
		for(t3=0;t3<numPlayers;t3++){
			if(human[t3]==1){
				t3++;
			}
			//assess if they should try to shoot the moon
			//assess run of highest cards. start at end to go from highest to lowest cards in hand
			t=initNumCardsInHand-1;
			while(t>=0){
				suit=memory[hand[t3][t]][1];
				//compare currHigh at suit of the card in hand
				//if too low
				if(memory[hand[t3][t]][0]<currHigh[t3][suit][0]){
					//loop through rest of suit
					while(memory[hand[t3][t]][1]==suit){
						//only if leading
						//check for Queen (not King... too obvious if played) of spades and exclude from isolatedLowAndCount
						if(suit==2 && (memory[hand[t3][t]][0]==11 || memory[hand[t3][t][0]==12)]){
							t--;
						}
						else{
							//add low card position in hand (not memory!) and increase low card counter, decrease hand card counter
							isolatedLowAndCount[t3][++isolatedLowAndCount[t3][0]]=t--;
							isoLowSuit[player][memory[hand[isolatedLowAndCount[s]]][1]]++;
						}
					}
				}
				else{
					//isolated low not found, taking account of numDecks, decrease currHigh
					if(--currHigh[t3][suit][1]==0){
						currHigh[t3][suit][0]--;
					}
					//decrease card pointer
					t--;
				}
			}
			//1= moon strategy, 0=regular. equation evaluates left to right
			if((hand[0]+3-isolatedLowAndCount[t3][0])/hand[0]*100>80){
				goMoon[t3]=1;
			}
			//weights if go moon strategy
			if(goMoon==1){
				t6=isolatedLowAndCount[t3][0];
				//if isolated <4
				//not the ideal logic to win the game... Might want to keep a club even if isolated.
				if(t6<4){
					//pass them all
					for(t2=0;t2<t6;t2++){
						passPos[t3][t2]=hand[t3][isolatedLowAndCount[t3][t2+1]];
						removeFromHand(t3,isolatedLowAndCount[t3][t2+1]);
						isoLowSuit[t3][memory[hand[isolatedLowAndCount[t3][t2]]][1]]--;
					}
					//reset isolatedLowAndCount
					isolatedLowAndCount[t3][0]=0;
					if(t6<3){
						//for however many cards are needed to pass 3
						for(t5=0;t5<3-t6;t5++){
							//needs to be recalculated every time because numCardsInSuit is a factor
							//for all cards in hand find their weight
							topWeightPos=1;
							for(t4=1;t4<=t6;t4++){
								weights[t3][t4]=findMoonWeights(t3,isolatedLowAndCount[t3][t4]);
								if(weights[t3][t4]>weights[t3][topWeightPos]){
									topWeightPos=t4;
								}
							}
							suit=memory[hand[t3][topWeightPos]][1];
							value=memory[hand[t3][topWeightPos]][0];
							//add card to pass
							passPos[t3][isolatedLowAndCount[t3][0]+t5]=hand[t3][topWeightPos];
							//remove card from hand and decrease hand count, and suit count
							removeFromHand(t3, isolatedLowAndCount[t3][topWeightPos]);
							//remove card from isolatedLowAndCount and decrease count
							removeFromIso(t3, topWeightPos);
						}
					}
				}
				//too many isolatedLow
				else{
					//find three cards
					for(t4=0;t4<3;t4++){
						//calculate and find highest weight in isolatedLowAndCount
						topWeightPos=0;
						for(t2=1;t2<=isolatedLowAndCount[0];t2++){
							weights[t3][t2]=findMoonWeights(t3,isolatedLowAndCount[t3][t2]);
							if(weights[t3][t2]>weights[t3][topWeightPos]){
								topWeightPos=t2;
							}
						}
						passPos[t3][t4]=hand[isolatedLowAndCount[topWeightPos]];
						removeFromHand(t3, isolatedLowAndCount[t3][topWeightPos]);
						removeFromIso(t3, topWeightPos);
					}
				}

			}
			else{
				//if not goMoon and have many spades
				if(currNumCardsInSuit[t3][2]<numCardsInSuit[2]/numPlayers*5/3){
					scenario=1;
				}
				//if not goMoon and don't have many spades
				//and have queen and few spades passing counterclockwise
				else if(hasQueen[t3]>0 && offset==-1){
					scenario=1;
				}
				//if none
				else{
					scenario=0;
				}
				//pass 3 cards
				for(t=0; t<3;t++){
					topWeightPos=1;
					//assign weights of all cards, saving highest weight
					for(t2=1;t2<=hand[t3][0];t2++){
						suit=memory[hand[t2]][1];
						weights[t3][t2]=weightTable[scenario][suit][memory[hand[t2]][0]]/currNumCardsInSuit[t3][suit];
						if(weights[t3][t2]>weights[t3][topWeightPos]){
							topWeightPos=t2;
						}
					}
					passPos[t3][t]=hand[t3][topWeightPos];
					//finds if iso and reorders and recounds array
					for(t4=1;t4<=isolatedLowAndCount[t3][0];t4++){
						if(isolatedLowAndCount[t3][t4]==topWeightPos){
							while(t4<isolatedLowAndCount[t3][0]){
								isolatedLowAndCount[t3][t4]=isolatedLowAndCount[t3][++t4];
							}
							isolatedLowAndCount[t3][0]--;
						}
					}
					removeFromHand(t3,topWeightPos);
				}
			}
		}
		//makes new hands
		//for every player
		for(t=0;t<numPlayers;t++){
			//for 3 cards
			for(t2=0;t2<3;t2++){
				//inserts passed-in cards
				//subtracting because positive is the direction of play (clockwise), so they recieve from "behind" them
				//player 0 needs to receive from player 4, etc
				circleOffset=offset;
				if(t-circleOffset<0){
					circleOffset=numPlayers+t-circleOffset;
				}
				//player 4 needs to recieve from player 0
				else if(t-circleOffset>=numPlayers){
					circleOffset=circleOffset%numPlayers;
				}
				hand[t][++hand[t][0]]=passPos[t-circleOffset][t2];
				//remove and insert
				int initHandCount=hand[t][0];
				for(t3=1;t3<=hand[t][0];t3++){
					if(hand[t][t3]==passPos[t][t2]){
						for(t4=t3;t3<hand[t][0];t4++){
							hand[t][t3]=hand[t][t3+1];
						}
						hand[t][0]--;
					}
					//needs? to find equality to set new suitPos correctly
					if(hand[t][t3]>=passPos[t-circleOffset][t2]){
						for(t4=hand[t][0];t4>t3;t4--){
							hand[t][t4]=hand[t][t4-1];
						}
						hand[t][t3]=passPos[t-circleOffset][t2];
						hand[t][0]++;
					}
				}
				if(initHandCount!=hand[t][0]){
					hand[t][++hand[t][0]]=passPos[t-circleOffset][t2];
					hand[t][0]++;
				}
				suit=memory[hand[t][0]][1];
				value=memory[hand[t][0]][0];
				currNumCardsInSuit[t][suit]++;
				if(suit==2 && value==11){
					hasQueen[t]++;
				}
				//recalc isolatedLowAndCount --might bridge the gap
				if(currHigh[t][suit][0]>value){
					isolatedLowAndCount[t][++isolatedLowAndCount[t][0]]=hand[t][0];
				}
				else{
					if(--currHigh[t][suit][1]==0){
						currHigh[t][suit][0]--;
						for(t4=0; t4<isolatedLowAndCount[t][0]; t4++){
							if(memory[hand[t][isolatedLowAndCount[t][t4]]][1]==suit){
								if(memory[hand[t][isolatedLowAndCount[t][t4]]][0]>=currHigh[t][suit][0]){
									if(--currHigh[t][suit][1]==0){
										currHigh[t][suit][0]--;
									}
									removeFromIso(t,t4--);
								}
							}
						}
					}
				}
			}
			//find first card of suit if one
			t=1;
			for(t2=0; t2<4; t2++){
				if(t2==memory[hand[t3][t][1]){
					suitPos[t2]=memory[hand[t3][t][1];
				}
				else{
					suitPos[t2]=-1;
				}
				while((t<=hand[t3][0] && t2!=3) && t2==memory[hand[t3][t][1]){
					t++;
				}
			}
			//finds weakIsoLowAndCount if gomoon
			if(goMoon[t3]==1){
				//calculated before every hand played
				//checks if an iso is also weak (a low value card with few cards of that suit)--only played if gomoon--then plays suit
				//points to hand
				weakestIsoLow=isolatedLowAndCount[t3][1];//may not exist
				weakIsoLowAndCount=new int[1+isolatedLowAndCount[0]];
				for(t2=1;t2=<isolatedLowAndCount[t3][0];t2++){
					//favours playing more numerous suits of isolated low --20 arbitrary threshold
					if(100/memory[hand[t3][isolatedLowAndCount[t3][t2]]][0]/
					isoLowSuit[t3][memory[hand[t3][isolatedLowAndCount[t3][t2]]][1]]>20){
						weakIsoLowAndCount[t3][weakIsoLowAndCount[t3][0]]=t2;
						weakIsoLowAndCount[t3][0]++;
						if(100/memory[hand[t3][isolatedLowAndCount[t3][t2]]][0]/
						isoLowSuit[t3][memory[hand[t3][isolatedLowAndCount[t3][t2]]][1]]
						>100/memory[hand[t3][isolatedLowAndCount[t3][weakestIsoLow]]][0]/
						isoLowSuit[t3][memory[hand[t3][isolatedLowAndCount[t3][weakestIsoLow]]][1]]){
							weakestIsoLow=t2;
						}
					}
				}
			}
		}
	}
	//when remove from hand during play only
	private void updateSuitPos(int player, int handPos){
		//checks if the pos of first card of the suit
		if(memory[hand[player][handPos]][0]==suitPos[player][memory[hand[player][handPos]][1]]){
			if(handPos!=hand[player][0] && memory[hand[player][handPos]][1]==memory[hand[player][handPos+1]][1]){
				suitPos[player][memory[hand[player][handPos]][1]]=handPos+1;
			}
			else{
				suitPos[player][memory[hand[player][handPos]][1]]=-1;
			}
		}
	}
	//also removes from weakIsoLowAndCount
	private void checkRemoveFromIso(int player, int handPos){
		//isolatedLowAndCount stores in reverse order (high hearts to low spades)
		for(s2=isolatedLowAndCount[player][0]; s2>0;s2--){
			if(isolatedLowAndCount[player][s2]==handPos){
				removeFromIso(player,s2);
				//reorder weakIsoLowAndCount
				//check and remove if in weakIsoLowAndCount
				//ordered like isolatedLowAndCount
				for(s3=1;s3<=weakIsoLowAndCount[player][0];s3++){
					if(weakIsoLowAndCount[player][s3]==s2){
						for(s3<weakIsoLowAndCount[player][0]){
							weakIsoLowAndCount[player][s3]=weakIsoLowAndCount[player][s3+1];
						}
						weakIsoLowAndCount[player][0]--;
						//find new weakest if necessary
						if(s3==weakestIsoLow){
							weakestIsoLow=1;
							for(s3=1;s3<=weakIsoLowAndCount[player][0];s3++){

								if(100/memory[hand[t3][isolatedLowAndCount[t3][t2]]][0]/
								isoLowSuit[t3][memory[hand[t3][isolatedLowAndCount[t3][t2]]][1]]
								>100/memory[hand[t3][isolatedLowAndCount[t3][weakestIsoLow]]][0]/
								isoLowSuit[t3][memory[hand[t3][isolatedLowAndCount[t3][weakestIsoLow]]][1]]){
									weakestIsoLow=t2;
								}
							}
						}
					}
				}
				s2=0;
			}
		}
	}
	private void removeFromIso(int player, int isoPos){
		isoLowSuit[player][memory[hand[isolatedLowAndCount[player][isoPos]]][1]]--;
		for(s=isoPos; s<isolatedLowAndCount[player][0]; s++){
			isolatedLowAndCount[s]=isolatedLowAndCount[s+1];
		}
		isolatedLowAndCount[player][0]--;
	}
	private void removeFromHand(int player, int handPos){
		suit=memory[hand[player][handPos]][1];
		value=memory[hand[player][handPos]][0];
		if(handPos==suitPos[suit]){
			if(hand[player][0]!=handPos && suit==memory[hand[player][handPos+1]][1]){
				suitPos[player][suit]=handPos+1;
			}
			else{
				suitPos[player][suit]=-1;
			}
		}
		if(value==11 && suit==2){
			hasQueen[player]--;
		}
		currNumCardsInSuit[suit]--;
		for(s=handPos; s<hand[player][0]; s++){
			hand[s]=hand[s+1];
		}
		hand[player][0]--;
	}
	//in the future could try to fake out players by giving higher value cards in dominant suits
	//maybe should have special weight for 2 clubs
	//finds weight of a card for goMoon
	//ensures do not give away queen of spades --wrong place?
	private int findMoonWeights(int player, int handPos){
		value=memory[hand[player][handPos]][0];
		suit=memory[hand[player][handPos]][1];
		//favours numerous suits and low cards --therefore favours removing 2 clubs?
		weight=currNumCardsInSuit[player][suit]*(numValues[suit]-value);
		if(value==11 && suit==2){
			weight=1;
		}
		return weight;
	}
	private int findNFE(String line){
		try{
			int temp = Integer.parseInt(line);
			return temp;
		}
		catch(NumberFormatException e){
			return -1;
		}
	}
	private String loopYN(){
		String line=scan.nextLine();
		while(!line.equals("Y") && !line.equals("N")){
			System.out.println("Not a Y or N. Please enter Y or N:");
			line=scan.nextLine();
		}
		return line;
	}
}
