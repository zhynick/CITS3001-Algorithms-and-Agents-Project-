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
	HashMap<Integer, int[][]> hinted_cards;
	Stack<Card> discard_pile;
	ArrayList<Card> safe_to_discard;
	ArrayList<Card> current_playable_cards; 
	boolean firstAction = true; 
	double playability_certainity; //The overall certainity of playing a card
	double discard_certainity; // The overall certainity of discarding a card
	
	State current_state; //current_state of the game 
	
	public double average(double[] input)
	{
		double sum = 0.0;
		
		for(double a : input)
		{
			sum+=a;
		}
		
		sum = sum/input.length;
		
		return sum; 
	}

	
	public int playable(Colour c){ //repurposing Tim French's code 
	    java.util.Stack<Card> fw = played_pile.get(c);
	    if (fw.size()==5) return -1;
	    else return fw.size()+1;
	  }
	
	public void get_safe_playables() //This is much easier. A card is safe to play if playable says so. 
	{
		current_playable_cards.clear(); 
		for(Colour c : colours_value)
		{
			int playable_rank = playable(c);
			if(playable_rank == -1)
			{
				continue;
			}
			Card playable_card = new Card(c, playable_rank);
			current_playable_cards.add(playable_card);
		}
		
		
	}
	
	 
	public void init(State s)
	{
	
		played_pile = new HashMap<Colour, Stack<Card>>();
		discard_pile = new Stack<Card>();
		safe_to_discard = new ArrayList<Card>();
		current_cards = new HashMap<Integer, Card[]>();
	//  current_playable_cards = new ArrayList<Card>();
	// current_cards_safe_to_discard = new ArrayList<Card>();
		hinted_cards = new HashMap<Integer, int[][]>();
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
	

	   public void getAll_Hints(State s) throws IllegalActionException //testing what other opponents know about their cards 
	  {
		  int[][] current_hint;
		  Stack<Action> action_holder = new Stack<Action>();
		  State t = (State) s.clone();
		  for(int b = 0; b<Math.min(numPlayers,s.getOrder());b++) //get actions and push them to the stack.
		  {
		      Action a = t.getPreviousAction();
		      action_holder.push(a);
		      t = t.getPreviousState();
		  }
		  
		  for(int i = 0 ; i < numPlayers ; i++)
		  {
			 if(hinted_cards.get(i) == null && i !=index)
			 {
				int[][] new_array = new int[colours.length][2];
				hinted_cards.put(i, new_array ); 
			 }
		  }
		  while(!action_holder.isEmpty())
		  {
			  Action a = action_holder.pop();
			 // System.out.println("WHAT:" + a.toString());
			  if((a.getType()==ActionType.HINT_COLOUR || a.getType() == ActionType.HINT_VALUE))
		        {
				  int[][] holder = hinted_cards.get(a.getHintReceiver());
		          boolean[] hints = a.getHintedCards();
		          
		          if(a.getHintReceiver() == index)
		        	{
		        	  for(int j = 0; j<hints.length; j++)
		        	  {
		        		if(hints[j])
			            {
			              if(a.getType()==ActionType.HINT_COLOUR) 
			              {
			            	  colours[j] = a.getColour();
			              }
			              else
			            	  values[j] = a.getValue();
			             
			            }
		        		
		        	  }
		        	}
		        	
		        	else
		        	{
		        	for(int j = 0; j<hints.length; j++)
			        {
		        		if(hints[j])
		        		{
		        			if(a.getType()==ActionType.HINT_COLOUR) 
		        			{
		        				holder[j][0] = 1;
		        			}
		        			else
		        				holder[j][1] = 1;
		             
		        		}
		        	}
		          }
		          hinted_cards.replace(a.getHintReceiver(), holder); 
		        }
		         
			  else if((a.getType() == ActionType.DISCARD || a.getType() == ActionType.PLAY))
		         {
		        	 int[][] holder1 = hinted_cards.get(a.getPlayer());
		        	 int position = a.getCard();
		        	 if(a.getPlayer() == index)
		        	 {
		        		 colours[position] = null;
		        		 values[position] = 0;
		        	 }
		        	 else
		        	 {
		        		 holder1[position][0] = 0;
		        		 holder1[position][1] = 0;
		        		 hinted_cards.replace(a.getPlayer(), holder1); 
		        	 }
		         }
		        
		  }
		  	
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
					played_pile.put(colours_value[i], current_state.getFirework(colours_value[i]));
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
	}
	
	public double[] get_percentages_playable(double[] playable_chance, int player_id, Colour[] input_colour, int[] input_value) //Given what we know so far, give each card in the hand a chance of being safe to be played
 	{
 		float zero = 0;
 		Arrays.fill(playable_chance, zero);
 		Colour[] known_colours = new Colour[colours.length];
 		int[] known_values = new int[colours.length];
 		if(player_id == index)
 		{
 			known_colours = colours;
 			known_values = values;
 		}
 		else
 		{
 			known_colours = input_colour;
 			known_values = input_value;
 		}
 		
 		for(int d = 0 ; d < colours.length; d++)
 		{
 			double probability = 0;
 			Colour current_colour = known_colours[d];
 			int current_value = known_values[d];
 			if(current_colour != null & current_value != 0)
 			{
 				for(Card item : current_playable_cards)
 	 			{
 	 				if(current_colour == item.getColour() && current_value == item.getValue())
 	 				{
 	 					probability += 1.0;
 	 					break;
 	 				}
 	 			}
 	 		}
 			
 			else if(current_colour == null && current_value != 0) //if you know the value
 			{
 				int seen = 0;
 				int[] current_playables = new int[colours_value.length];
 				int[] copies_seen = new int[colours_value.length];
 				int count = 0;
 				for(Colour item : colours_value)
 				{
 					int next_playable = playable(item);
 					current_playables[count] = next_playable;
 					for(Card a : seen_cards)
 	 				{
 	 					if(a.getValue() == next_playable && a.getColour() == item)
 	 					{
 	 						copies_seen[count]+=1;
 	 					}
 	 				}
 					count+=1;
 				}
 				for(Card a : seen_cards)
 				{
 					if(a.getValue() == current_value)
 					{
 						seen+=1;
 					}
 				}
 					
 				for(int i = 0 ; i < colours_value.length; i++)
 				{
 					if(current_value == current_playables[i])
 					{
 						if(current_value == 1)
 						{
 						probability +=((double) (3-copies_seen[i])/((double)15 - seen));
 						}
 					
 				
 						else if(current_value >= 2 & current_value <=4)
 						{
 						probability +=((double) (2-copies_seen[i])/((double)10 - seen));
 					
 						}
 				
 						else if(current_value == 5)
 						{
 						probability +=((double) (1)/((double)5 - seen));
 					
 						}
 					}
 				}
 			}
 			
 			else if(current_colour != null && current_value==0)
 			{
 				int seen = 0;
 				int copies_seen = 0;
 				int playable_number = playable(current_colour); // get the playable rank
 				for(Card a : seen_cards) //for each card that's been seen, made up of played_pile + discard_pile + opponent's hands. 
 				{ 
 					
 					if(a.getColour() == current_colour)
 					{
 						seen+=1;
 						if(a.getValue() == playable_number)
 						{
 							copies_seen+=1;
 						}
 					}
 				}
 				switch(playable_number)
 				{
 				case 1:
 					probability = (double)(3-copies_seen)/((double)10-seen);
 					break;
 				case 5:
 					probability = (double)(1)/((double)10-seen);
 					break;
 				default:
 					probability = (double)(2-copies_seen)/((double)10-seen);
 					break;
 				}
 			}
 			
 			else
 			{
 				 Card playable_cards[] = new Card[5];
 				 int copies_seen[] = new int[5];
 				 int count = 0;
 				 int unseen = 50 - seen_cards.size();
 				 for(Colour item : colours_value)
 				 {
 					 if(playable(item) == -1)
 					 {
 						 playable_cards[count] = null;
 						 count+=1;
 						 continue; 
 					 }
 					 
 					 Card playable_card = new Card(item, playable(item));
 					 playable_cards[count] = playable_card;
 					
 				 }
 				 
 				 for(int m = 0 ; m < playable_cards.length; m++)
 				 {
 					 if(playable_cards[m] == null)
 					 {
 						 continue;
 					 }
 					 
 					 
 					 for(Card cards : seen_cards)
 					 {
 						 if(playable_cards[m].equals(cards))
 						 {
 							 copies_seen[m]+=1;
 						 }
 					 }
 				 }
 				 
 				 for(int a = 0; a < colours.length; a++)
 				 {
 					if(playable_cards[a] == null)
					 {
						 continue;
					 }
					 
 					 
 					 switch(playable_cards[a].getValue())
 					 {
 					 case 1:
 						 probability += ((double)(3-copies_seen[a])/((double)(50-seen_cards.size())));
 						 break;
 					 case 5:
 	 					probability += ((double)(1-copies_seen[a])/((double)(50-seen_cards.size())));
 	 					break;
 	 				 default:
 	 					probability += ((double)(2-copies_seen[a])/((double)(50-seen_cards.size())));
 	 					break;
 	 				 }
 				 }
 			}
 		
			playable_chance[d] = probability;
 		}
 		return playable_chance;
 	}
	
	
	
	public void fillDiscard(double[] discard_chance, int player_id, Colour[] input_colour, int[] input_value) //in this case, input_colour and input_values are what the player knows about their hand
	{
		double[] discard_probability= new double[colours.length];
		
		for(int a = 0 ; a < colours.length; a++)
		{
			int current_value = input_value[a];
			Colour current_colour = input_colour[a];
			double current_probability = 0;
			
			
			if(current_value != 0 && current_colour != null)  //If you know what the colour and value are, then if any of the cards in the safe_to_discard pile match, then the probability equaals one
			{
				for(Card c : safe_to_discard)
				{
					if(current_value == c.getValue() && current_colour ==  c.getColour())
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
							current_probability += (double) 3- safe_colour[i]/(double) (10-same_colour_seen);
						
						case 5:
							current_probability += (double) 1-safe_colour[i]/(double) (10-same_colour_seen); 
						
						default:
							current_probability += (double) 2-safe_colour[i]/(double) (10-same_colour_seen);
						
						}
						
					}
					
				}
			}
			
			else if(current_value != 0  && current_colour == null) //you know the value
			{
				int copies_seen = 0; 
				int same_value_seen = 0; 
				int[] safe_colour = new int[5]; 
				
				ArrayList<Card> same_value = new ArrayList<Card>(); //cards that are the same value as the current card that can be safely discarded
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
				
				
				Iterator<Entry<Card, Integer>> card_counter2 =  card_counter.entrySet().iterator();
				
				while(card_counter2.hasNext())
				{
					Entry<Card, Integer> current = card_counter2.next();
					Card c = current.getKey();
					int count = current.getValue(); //how many of that card has been seen 
					
					switch(c.getValue()) //what is the chance of the card in your hand being one of the safe to discard cards(via value)
					{
					case 1:
						current_probability+= (double)3-count/(double) 15-copies_seen; 		//total 15 1s
						
					case 5:
						current_probability+= (double)1-count/(double) 5-copies_seen; 		//total 5 5s
						
					default:
						current_probability+= (double)2-count/(double)10-copies_seen;		//total 10 2s, 3s, 4s
					
					
					
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
								int count = copies_seen.get(c);
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
						current_probability += (double) 3-number_seen/(double)52-seen_cards.size();
					
					case 5:
						current_probability += (double) 1-number_seen/(double)52 - seen_cards.size();
						
					default:
						current_probability += (double) 2-number_seen/(double)52- seen_cards.size();
					}
				}
					
			}
					
				discard_probability[a] = current_probability;
		}
		discard_chance = discard_probability;
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
		
		Discard discard_class = new Discard(discard_pile, fuse_tokens, seen_cards, current_cards , played_pile);
		safe_to_discard = discard_class.get_safe_discards();
		
		return null;
	}


	
	
	
	
	public String toString()
	{
		return "Unknown"; 
		
		
		
	}
	
	
	
	
	private static final Colour[] colours_value = new Colour[] {Colour.BLUE,Colour.RED,Colour.GREEN,Colour.WHITE,Colour.YELLOW};
}
