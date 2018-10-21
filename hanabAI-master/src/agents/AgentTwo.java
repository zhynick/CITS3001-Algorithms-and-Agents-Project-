package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Agent;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.IllegalActionException;
import hanabAI.State;

//Agent two will use probabilities to decide what action to take
//these probabilities will be based on: hint tokens remaining, amount of certainty regarding playing a card, 
//amount of certainty with regards to discarding, no. of fuse tokens remaining
//and also what moves we expect other players to take based on the knowledge we know they have
public class AgentTwo implements Agent {

	int hint_tokens; //amount of hint tokens remanining
	int fuse_tokens; //amount of fuse tokens remaining 
	int index;  //id
	int numPlayers; //number of Players
	Colour[] colours; //known colours in hand
	int[] values; //known values
	double[] discard_chance; // chance of each card in hand being safe to discard
	double[] playable_chance; //chance of each card in hand being playable
	ArrayList<Card> seen_cards; //Similiar to Agent One, stores all cards that've been seen
	HashMap<Colour, Stack<Card>> played_pile;
	Stack<Card> discard_pile;
	ArrayList<Card> safe_to_discard;
	boolean firstAction = true; 
	double playability_certainity; //The overall certainity of playing a card
	double discard_certainity; // The overall certainity of discarding a card
	
	State current_state; //current_state of the game 

	
	public int playable(Colour c){ //repurposing Tim French's code 
	    java.util.Stack<Card> fw = played_pile.get(c);
	    if (fw.size()==5) return -1;
	    else return fw.size()+1;
	  }
	
	
	 
	public void init(State s)
	{
	
		played_pile = new HashMap<Colour, Stack<Card>>();
		discard_pile = new Stack<Card>();
		safe_to_discard = new ArrayList<Card>();
	//  current_playable_cards = new ArrayList<Card>();
	// current_cards_safe_to_discard = new ArrayList<Card>();
	// hinted_cards = new HashMap<Integer, int[][]>();
	// opponent_hint_probabilities = new HashMap<Integer, HashMap<String, double[]>>();
		numPlayers = s.getPlayers().length;
		seen_cards = new ArrayList<Card>();
		hint_tokens = s.getHintTokens();
		fuse_tokens = s.getFuseTokens();
		
		
		if(numPlayers==5){
		  colours = new Colour[4];
	      values = new int[4];
	      playable_chance = new double[4];
	      discard_chance = new double[4];
	    }
	    else{
	      colours = new Colour[5];
	      values = new int[5];
	      playable_chance = new double[5];
	      discard_chance = new double[5];
	    }
	    index = s.getNextPlayer();
	    firstAction = false;
	
	}
	
	public void setCards() //Set global variables
	{
				seen_cards.clear(); 
				discard_pile = current_state.getDiscards();
				
				Iterator<Card> p = discard_pile.iterator();
				while(p.hasNext())
				{
					Card next = p.next();
					seen_cards.add(next);
				}
				
				
				
				for(int i = 0 ; i < 5 ; i++)
				{
					if(current_state.getFirework(colours_value[i]) == null)
					{
						continue;
					}
					played_pile.put(colours_value[i], s.getFirework(colours_value[i]));
					for(Card c : current_state.getFirework(colours_value[i]))
					{
						seen_cards.add(c);
					}
				}
				
				for(int i = 0 ; i < numPlayers; i++)
				{
					if(index == i)
					{
						continue; 
					}
				
					for(Card c: current_state.getHand(i))
					{
						if(c == null)
						{
							continue;
						}
							seen_cards.add(c);
					}
				}
		}
	
	
	public void fillDiscard()
	{
		double[] discard_probability= new double[colours.length]
		for(int a = 0 ; a < colours.length; a++)
		{
			int current_value = values[a];
			Colour current_colour = colours[a];
			
			for(Card c : safe_to_discard)
			{
				if()
				
				
			}
			
			
			
			
		}
		
		
		
		
	}
	 
	
	public Action doAction(State s) 
	{
		if(firstAction)
		{
			init(s);
			firstAction = true;
		}
		
		current_state = s;
		hint_tokens = s.getHintTokens();
		fuse_tokens = s.getFuseTokens();
		
		Discard discard_class = new Discard(discard_pile, fuse_tokens, seen_cards, null , played_pile);
		safe_to_discard = discard_class.get_safe_discards_version2();
		
		return null;
	}


	
	
	
	
	public String toString()
	{
		return "Unknown"; 
		
		
		
	}
	
	
	
	
	private static final Colour[] colours_value = new Colour[] {Colour.BLUE,Colour.RED,Colour.GREEN,Colour.WHITE,Colour.YELLOW};
}
