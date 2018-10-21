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
	HashMap<Integer, Card[]> current_cards;  //Current hand of each player, mapped to an Int value(aside from your own)
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
		current_cards = new HashMap<Integer, Card[]>();
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
				
				if(current_cards.get(i) == null)
				{
					current_cards.put(i, current_state.getHand(i));
					for(Card c: current_state.getHand(i))
					{

						if(c == null)
						{
							continue;
						}
						seen_cards.add(c);
					}
				}
				
				else
				{
				
					current_cards.remove(i);
					current_cards.put(i, current_state.getHand(i));
					for(Card c: s.getHand(i))
					{
						if(c == null)
						{
							continue;
						}
						seen_cards.add(c);
					}
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
			double current_probability = 0;
			
			
			if(current_value != 0 && current_colour != null)  //If you know what the colour and value are, then if any of the cards in the safe_to_discard pile match, then the probability equaals one
			{
				for(Card c : safe_to_discard)
				{
					if(current_value == c.getValue() && current_colour c.getColour())
					{
						current_probability = 1;
						break;
					}
				}
			}
			
			else if(current_value == 0 && current_colour != null) //If you know what the colour is, but not the value 
			{
				int copies_seen = 0; 
				int same_colour_seen = 0; 
				int[] safe_colour = new int[5]; 
				
				for(Card c: safe_to_discard)//this is not a good way to do it, but switch safe_colour[c.getValue()-1] to 1 if there is an instance of that card in safe to discard
				{
					if(c.getColour() == current_colour)
					{
						safe_colour[c.getValue()-1] = 1;
					}
				}
				
				for(Card c: seen_cards)
				{
					if(c.getColour() == current_colour)
					{
						same_colour_seen++;
						if(safe_colour[c.getValue()-1] != 0)
						{
							safe_colour[c.getValue()-1] +=1; //to avoid confusion, copies of the same card are x -1 (as 1 was already set to determine if it existed in the discard pile)
						}
						
					}
					
				}
				
				for(int i : safe_colour)
				{
					i = i -1;
				}
				
				
				
				for(int i = 0 ; i < safe_colour.length; i++)
				{
					if(safe_colour[i] != 0)
					{
						switch(i+1) //note that 1 can never be a possibility here
						{
						case 1:
							probability += (double) 3- safe_colour[i]/(double) (10-same_colour_seen);
						
						case 5:
							probability += (double) 1-safe_colour[i]/(double) (10-same_colour_seen); 
						
						default:
							probability += (double) 2-safe_colour[i]/(double) (10-same_colour_seen);
						
						}
						
					}
					
				}
			}
			
			else if(current_value != 0  && current_colour == null) //you know the value
			{
				int copies_seen = 0; 
				int same_value_seen = 0; 
				int[] safe_colour = new int[5]; 
				
				ArrayList<Colour> same_value = new ArrayList<Colour>(); //cards that are the same value as the current card that can be safely discarded
				HashMap<Card, Integer> card_counter = new HashMap<Card, Integer>(); 
				
				for(Card c : safe_to_discard)
				{
					if(c.getValue() == current_value)
					{
						same_value.add(c);
					}
				
				}
				
				for(Card c : same_value) //getting how many of those cards have been seen.
				{
					for(Card items : seen_cards)
					{
						if(c.getValue() == current_value)
						{
							same_value_seen+=1;
						}
						
						
						if(c.getValue() == items.getValue() && c.getColour() == items.getColour()) //placing them in the hashmap
						{
							if(card_counter.get(c) == null)
							{
								card_counter.put(c, 1);
							}
							
							else
							{
								int count = card_counter.get(c);
								card_counter.replace(c, count+1);
							}
							
						}
					
					}
				}
				
				
				Iterator<Card, Integer> card_counter2 =  card_counter.entrySet().iterator();
				
				while(card_counter2.hasNext())
				{
					Entry<Card, Integer> current = card_counter2.next();
					Card c = current.getKey();
					int count = current.getValue(); //how many of that card has been seen 
					
					switch(c.getValue()) //what is the chance of the card in your hand being one of the safe to discard cards(via value)
					{
					case 1:
						probability+= (double)3-count/(double) 15-copies_seen; 		//total 15 1s
						
					case 5:
						probability+= (double)1-count/(double) 5-copies_seen; 		//total 5 5s
						
					default:
						proabability+= (double)2-count/(double)10-copies_seen;		//total 10 2s, 3s, 4s
					
					
					
					}
				}
			}
			
			else	//if you know nothing, how safe it is to discard
			{
				HashMap<Card, Integer> copies_seen = new HashMap<Card, Integer>(); //get how many of each card has been seen
				
				for(Card c : safe_to_discard) 
				{
					
					for(Card item : seen_cards)
					{
						if(item.getColour() == c.getColour() && c.getValue() == item.getValue())
						{
							if(copies_seen.get(c) == null)
							{
								copies_seen.put(c, 1);
							}
							
							else
							{
								int count = card_counter.get(c);
								copies_seen.replace(c, count+1);
							}
							
						}
					
					}
				}
				
				for(int i = 0 ; i < safe_to_discard.size(); i++) //for each card that's safe to discard
				{
					Card current = safe_to_discard.get(i);
					int number_seen = copies_seen.get(current); //how many of those cards have been seen
					
					switch(current.getValue())
					{
					case 1:
						probability += (double) 3-number_seen/(double)52-seen_cards.size();
					
					case 5:
						probability += (double) 1-number_seen/(double)52 - seen_cards.size();
						
					default:
						probability += (double) 2-number_seen/(double)52- seen_cards.size();
					}
				}
					
			}
					
				discard_probability[a] = probability;
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
		safe_to_discard = discard_class.get_safe_discards();
		
		return null;
	}


	
	
	
	
	public String toString()
	{
		return "Unknown"; 
		
		
		
	}
	
	
	
	
	private static final Colour[] colours_value = new Colour[] {Colour.BLUE,Colour.RED,Colour.GREEN,Colour.WHITE,Colour.YELLOW};
}
