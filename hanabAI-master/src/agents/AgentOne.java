package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Agent;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class AgentOne implements Agent {
	Colour[] colours; //Known colours of cards in hand
	int[] values; //Known values of cards in hand
	boolean firstAction = true; //Is it the first turn of the game?
    int numPlayers;// Number of players in the game
	ArrayList<Card> seen_cards; //Cards that have been seen
	ArrayList<Card> unseen_cards;  //Cards yet to be seen(Not sure how useful this will be yet)
	ArrayList<Card> current_playable_cards; //Cards that are currently playable given a board state s
	ArrayList<Card> current_cards_safe_to_discard; //Cards that are currently safe to discard given a board state s(will take into account deck limit)
	HashMap<Integer, Card[]> current_cards;  //Current hand of each player, mapped to an Int value(aside from your own)
	HashMap<Colour, Stack<Card>> played_pile; //What cards have been played, sorted by colour 
	Stack<Card> discard_pile;  //Self explanatory
	int index; //Int value of the agent
	int turns_bought;  //How many turns can be bought by giving a hint(not yet implemented)
	
	public void update_unseen() //This methods updates cards that have already been seen, removing them from the unseen cards array
	{
	  for(Card item : seen_cards)
	  {
		  if(unseen_cards.contains(item))
		  {
			  unseen_cards.remove(item);
		  }
	  }
		
	}
	
	public int playable(Colour c){ //repurposing Tim French's code 
	    java.util.Stack<Card> fw = played_pile.get(c);
	    if (fw.size()==5) return -1;
	    else return fw.size()+1;
	  }
	
	
	public void get_safe_playables(ArrayList<Card> playable_safe) //This is much easier. A card is safe to play if playable says so. 
	{
		playable_safe.clear(); 
		for(Colour c : colours_value)
		{
			int playable_rank = playable(c);
			Card playable_card = new Card(c, playable_rank);
			playable_safe.add(playable_card);
		}
		
		
	}
	
	//You know a card is safe to discard in three scenarios: 
	//One, someone else has already thrown all the copies of a higher card of the same colour away, and you have a higher copy of that card in your hand. 
	//Say, someone threw all two red threes away, so all red 4 can now be discarded safely
	//Two, there is another copy of that card in the deck or a player's hand. 
	//This can be generalized to if current_cards has that card, or if card is not in seen_cards(meaning that its in the deck), it can be discarded
	//Three, the relevant card has already been played. So if a red 1 has been played, you know you can discard all red 1s.
 	public void get_safe_discards(ArrayList<Card> discard_safe) //This method gets cards that are safe to discard from the current board state
 	{
 		discard_safe.clear();
 		//Colour[] discard_safe_colours = new Colour[deck.length];
 		//int[] discard_safe_values = new int[deck.length];
 		
 		for(Colour c : colours_value)
 		{
 			int playable_rank = playable(c);
 			for(int i = playable_rank -1 ; i >= 0 ; i--)
 			{
 				Card discard_card = new Card(c, i);
 				discard_safe.add(discard_card);
 			}
 		}
 		
 		HashMap<Colour, Integer> highest_discarded_value = new HashMap<Colour, Integer>();
 		for(Card card : discard_pile)
 		{
 			
 			
 		}
 		
	}
	
	public void record_hands(State s)
	{		
			discard_pile = s.getDiscards();
			/*Iterator m = discard_pile.iterator();
			while(m.hasNext())
			{
				System.out.println(m.next().toString());
			}*/
		
			for(int i = 0 ; i < 5 ; i++)
			{
				if(s.getFirework(colours_value[i]) == null)
				{
					continue;
				}
				played_pile.put(colours_value[i], s.getFirework(colours_value[i]));
			}
			
			for(int i = 0 ; i < numPlayers; i++)
			{
				if(index == i)
				{
					continue; 
				}
				current_cards.put(i, s.getHand(i));
				for(Card cards : s.getHand(i))
				{
					if(!seen_cards.contains(cards))
					{
						seen_cards.add(cards);
					}
				}
		}
	}
	
	  public void getHints(State s){
		    try{
		      State t = (State) s.clone();
		      for(int i = 0; i<Math.min(numPlayers-1,s.getOrder());i++){
		        Action a = t.getPreviousAction();
		        if((a.getType()==ActionType.HINT_COLOUR || a.getType() == ActionType.HINT_VALUE) && a.getHintReceiver()==index){
		          boolean[] hints = t.getPreviousAction().getHintedCards();
		          for(int j = 0; j<hints.length; j++){
		            if(hints[j]){
		              if(a.getType()==ActionType.HINT_COLOUR) 
		                colours[j] = a.getColour();
		              else
		                values[j] = a.getValue();  
		            }
		          }
		        } 
		        t = t.getPreviousState();
		      }
		    }
		    catch(IllegalActionException e){e.printStackTrace();}
		  }
	
	
	public void init(State s)
	{
		 seen_cards = new ArrayList<Card>();
		 unseen_cards = new ArrayList<Card>(Arrays.asList(deck));
		 current_cards = new HashMap<Integer, Card[]>();
		 played_pile = new HashMap<Colour, Stack<Card>>();
		 discard_pile = new Stack<Card>();
		 numPlayers = s.getPlayers().length;
		 if(numPlayers==5){
			  colours = new Colour[4];
		      values = new int[4];
		    }
		    else{
		      colours = new Colour[5];
		      values = new int[5];
		    }
		 	record_hands(s); 
		 
		    index = s.getNextPlayer();
		    firstAction = false;
		
		
	}

	@Override
	public Action doAction(State s) 
	{
		if(firstAction)
		{
			init(s);
			firstAction = false;
		}
		
		else
		{
			getHints(s);
			record_hands(s);
			update_unseen();
			
			
		}
		
		return null;
	}
	
	
	public String toString()
	{
		return "Sadness";
	}
	
	private static final Colour[] colours_value = new Colour[] {Colour.BLUE,Colour.RED,Colour.GREEN,Colour.WHITE,Colour.YELLOW};
	
	private static final Card[] deck = {
	    new Card(Colour.BLUE,1),new Card(Colour.BLUE,1), new Card(Colour.BLUE,1),
	    new Card(Colour.BLUE,2),new Card(Colour.BLUE,2),new Card(Colour.BLUE,3),new Card(Colour.BLUE,3),
	    new Card(Colour.BLUE,4),new Card(Colour.BLUE,4),new Card(Colour.BLUE,5),
	    new Card(Colour.RED,1),new Card(Colour.RED,1), new Card(Colour.RED,1),
	    new Card(Colour.RED,2),new Card(Colour.RED,2),new Card(Colour.RED,3),new Card(Colour.RED,3),
	    new Card(Colour.RED,4),new Card(Colour.RED,4),new Card(Colour.RED,5),
	    new Card(Colour.GREEN,1),new Card(Colour.GREEN,1), new Card(Colour.GREEN,1),
	    new Card(Colour.GREEN,2),new Card(Colour.GREEN,2),new Card(Colour.GREEN,3),new Card(Colour.GREEN,3),
	    new Card(Colour.GREEN,4),new Card(Colour.GREEN,4),new Card(Colour.GREEN,5),
	    new Card(Colour.WHITE,1),new Card(Colour.WHITE,1), new Card(Colour.WHITE,1),
	    new Card(Colour.WHITE,2),new Card(Colour.WHITE,2),new Card(Colour.WHITE,3),new Card(Colour.WHITE,3),
	    new Card(Colour.WHITE,4),new Card(Colour.WHITE,4),new Card(Colour.WHITE,5),
	    new Card(Colour.YELLOW,1),new Card(Colour.YELLOW,1), new Card(Colour.YELLOW,1),
	    new Card(Colour.YELLOW,2),new Card(Colour.YELLOW,2),new Card(Colour.YELLOW,3),new Card(Colour.YELLOW,3),
	    new Card(Colour.YELLOW,4),new Card(Colour.YELLOW,4),new Card(Colour.YELLOW,5)
	  };

}
