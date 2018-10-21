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
import javafx.scene.paint.Color;

public class AgentOne implements Agent {
	Colour[] colours; //Known colours of cards in hand
	int[] values; //Known values of cards in hand
	boolean firstAction = true; //Is it the first turn of the game?
    int numPlayers;// Number of players in the game
	ArrayList<Card> current_playable_cards; //Cards that are currently playable given a board state s
	ArrayList<Card> current_cards_safe_to_discard; //Cards that are currently safe to discard given a board state s(will take into account deck limit)
	ArrayList<Card> seen_cards;
	HashMap<Integer, Card[]> current_cards;  //Current hand of each player, mapped to an Int value(aside from your own)
	HashMap<Colour, Stack<Card>> played_pile; //What cards have been played, sorted by colour
	HashMap<Integer, int[][]> hinted_cards; //Stores the hints that other players have received. 
	HashMap<Integer, List<int[]>> actual_hinted_cards; //Stores what those hints actually are.(Hinted Cards just stores whether they've received a hint). 
	HashMap<Integer, float[]> playability_of_opponent_cards; //Stores the chances of opponents card to be playable given what hints they know. 
	HashMap<Integer, HashMap<String, double[]>> opponent_hint_probabilities; //Stores the changed played_probablities
	Stack<Card> discard_pile;  //Self explanatory
	int index; //Int value of the agent
	double[] self_playable_chance;	//chance of card in hand being playable
	double[] discard_chance;  	// chance of card in hand being able to be discarded 
	int hint_tokens; //number of hint tokens 
	int fuse_tokens; //number of fuse tokens 
	int[] age; //age of each card in the hand(used in discardOldest)
	int turn_number; //current turn number, used to denote early/mid/late game
	State current_state; //current state
	
	
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
 	
 	public int comparison(double[] input_one, double[] input_two, boolean[] changed_one, boolean[] changed_two)
 	{
 		double first_sum = 0;
 		double second_sum = 0;
 		
 		
 		
 		for(int i = 0 ; i < input_one.length; i++)
 		{
 			if(changed_one[i])
 			{
 				first_sum += input_one[i];
 			}
 			
 			if(changed_two[i])
 			{
 				second_sum += input_two[i];
 			}
 			
 			
 			if(i == input_one.length-1)
 			{
 				first_sum = first_sum / (double) (input_one.length);
 				second_sum = second_sum / (double) (input_one.length);
 				break;
 			}
 		}
 	//	System.out.println("first_sum:" + first_sum);
 	//	System.out.println("second_sum:" + second_sum);
 		if(first_sum >= second_sum) //If the average of the first is higher, return the first array, else return the second. 
 		{
 			return 1;
 		}
 		
 		else
 		{
 			return 2; 
 		}
 	}
 	
 	
 	public void update_ally_play_percentages()
 	{	ArrayList<String> added_hints = new ArrayList<String>();
 		tester ld = new tester();
 		for(int i = 0 ; i < numPlayers ; i++)
 		{
 			if(i == index)
 			{
 				continue;
 			}
 			tester c = new tester();
 			
 			Colour[] opponent_colours = new Colour[colours.length];
 			int[] opponent_values = new int[colours.length];
 			int count = 0;
 			int[][] opponent_hints = hinted_cards.get(i);
 			HashMap<String, double[]> hint_probailities = new HashMap<String, double[]>();
 			HashMap<String, int[]> changed_probabilities = new HashMap<String, int[]>();
 			for(int card[] : opponent_hints)
 			{
 				if(card[0] == 1 && card[1] == 1)
 				{
 					opponent_colours[count] = current_state.getHand(i)[count].getColour();
 					opponent_values[count] = current_state.getHand(i)[count].getValue();
 				}
 				
 				
 				else if(card[0] == 1 && card[1] == 0)
 				{
 					opponent_colours[count] = current_state.getHand(i)[count].getColour(); 
 				}
 				
 				else if(card[1] == 1 && card[0] == 0)
 				{
 					opponent_values[count] = current_state.getHand(i)[count].getValue();
 				}
 				
 				else
 				{
 					// no info noted
 				}
 				count++;
 			}

 			Colour[] clone_opponent_colours = opponent_colours.clone();
 			int[] clone_opponent_values = opponent_values.clone();
 			
 			
 			for(int b = 0 ; b < current_state.getHand(i).length; b++)
 			{
 				if(current_state.getHand(i)[b] == null)
				{
						continue;
				}
 				
 				else if(opponent_colours[b] != null && opponent_values[b] != 0) //all information is known , no more hints are needed
 				{
 					continue;
 				}
 				
 				else if(opponent_colours[b] == null && opponent_values[b] != 0) //only the value is known 
 				{
 					Colour unknown = current_state.getHand(i)[b].getColour();
 					opponent_colours[b] = unknown;
 					int[] changed = new int[colours.length];
 					changed[b] = 1;
 					for(int index = 0 ; index < current_state.getHand(i).length; index++)
 					{
 						if(current_state.getHand(i)[index] == null)
 						{
 							continue;
 						}
 						if(opponent_colours[index] == null && current_state.getHand(i)[index].getColour().equals(unknown))
 						{
 							opponent_colours[index]  = unknown; //simulating giving them the hint for that colour, change each unknown card in the hand to that colour
 							changed[index] = 1;
 						}
 					}
 					double[] input = new double[colours.length];
 				//	System.out.println("one" + "colour:" + unknown);
 					double[] playable_chance = get_percentages_playable(input, i , opponent_colours, opponent_values);
 					hint_probailities.put(unknown.toString(),playable_chance);
 					changed_probabilities.put(unknown.toString(), changed);
 					
 				}
 				
 				else if(opponent_colours[b] != null && opponent_values[b] == 0 )//if only the colour is known
 				{
 					int unknown = current_state.getHand(i)[b].getValue();
 					opponent_values[b] = unknown;
 					int[] changed = new int[colours.length];
 					changed[b] = 1;
 					for(int index = 0 ; index < current_state.getHand(i).length; index++)
 					{
 						if(current_state.getHand(i)[index] == null)
 						{
 							continue;
 						}
 						
 						if(opponent_values[index] == 0 && current_state.getHand(i)[index].getValue() == unknown)
 						{
 							opponent_values[index]  = unknown; //simulating giving them the hint for that colour, change each unknown card in the hand to that colour
 							changed[index] = 1;
 						}
 					}
 					double[] input = new double[colours.length];
 				//	System.out.println("two" + "value:" + unknown);
 					double[] playable_chance = get_percentages_playable(input, i , opponent_colours, opponent_values);
 					hint_probailities.put(Integer.toString(unknown),playable_chance);
 					changed_probabilities.put(Integer.toString(unknown), changed);
 				}
 				
 				else //nothing is known 
 				{
 					int unknown_value = current_state.getHand(i)[b].getValue();
 					Colour unknown_colour = current_state.getHand(i)[b].getColour();  
 					
 					
 					opponent_values[b] = unknown_value;
 					
 					boolean[] changed_one = new boolean[colours.length];
 					boolean[] changed_two = new boolean[colours.length];
 					
 					
 					changed_one[b] = true;
 					changed_two[b] = true;
 					
 					for(int index = 0 ; index < current_state.getHand(i).length; index++)
 					{
 						if(current_state.getHand(i)[index] == null)
 						{
 							
 							continue;
 						}
 						
 						if(opponent_values[index] == 0 && current_state.getHand(i)[index].getValue() == unknown_value)
 						{
 							opponent_values[index]  = unknown_value; //simulating giving them the hint for that value, change each unknown card in the hand to that value
 							changed_one[b] = true;
 						}
 					}
 					double[] input = new double[colours.length];
 					double[] playable_chance_value = get_percentages_playable(input, i , opponent_colours, opponent_values);
 					
 				//	System.out.println("three" + "value:" + unknown_value);
 				//	ld.print_double(playable_chance_value);
 					
 					opponent_values = clone_opponent_values.clone(); //reset the values, now test colour
 					opponent_colours[b] = unknown_colour;
 					for(int index = 0 ; index < current_state.getHand(i).length; index++)
 					{
 						if(current_state.getHand(i)[index] == null)
 						{
 							continue;
 						}
 						
 						if(opponent_colours[index] == null && current_state.getHand(i)[index].getColour() == unknown_colour)
 						{
 							opponent_colours[index]  = unknown_colour; //simulating giving them the hint for that colour, change each unknown card in the hand to that colour
 							changed_two[b] = true;
 						}
 					}
 					double[] input2 = new double[colours.length];
 					double[] playable_chance_colour = get_percentages_playable(input2, i , opponent_colours, opponent_values);
 					int result = comparison(playable_chance_value, playable_chance_colour, changed_one, changed_two);
 					if(result == 1)
 					{
 						//System.out.println("value:" + " " + unknown_value);
 						//ld.print_double(playable_chance_value);
 						hint_probailities.put(Integer.toString(unknown_value), playable_chance_value);
 						
 					}
 					
 					else if(result == 2)
 					{
 						hint_probailities.put(unknown_colour.toString(), playable_chance_colour);
 						
 						
 					}
 				}
 				opponent_colours = clone_opponent_colours.clone();
 				opponent_values = clone_opponent_values.clone();
 		}
 			
 			if(	opponent_hint_probabilities.get(i) == null)
 			{
 				opponent_hint_probabilities.put(i, hint_probailities);
 			}
 			else
 			{
 				opponent_hint_probabilities.remove(i);
 				opponent_hint_probabilities.put(i, hint_probailities);
 			}
 			added_hints.clear();
 		}
 	}
 	
 	
 	public int[] get_highest_hint_probabiltiies()
 	{
 		Iterator<Entry<Integer, HashMap<String, double[]>>> current_iterator = opponent_hint_probabilities.entrySet().iterator();
 		tester ld = new tester();
 		double[] highest_hint = new double[colours.length];
 		String hint_type = "";
 		int return_id = -1;
 		boolean first_hint = false;	
 		tester a = new tester();
 		boolean[] nothing = new boolean[colours.length];

 		while(current_iterator.hasNext())
 		{
 			Entry<Integer, HashMap<String, double[]>> current = current_iterator.next();
 			int id = current.getKey();
 			HashMap<String, double[]> current_hint = current.getValue();
 			Iterator<Entry<String, double[]>> hint_iterator = current_hint.entrySet().iterator();
 			
 			while(hint_iterator.hasNext())
 			{
 				int count = 0;
 				Entry<String, double[]> first_test_hint  = hint_iterator.next();
 				String hint = first_test_hint.getKey();
 				double[] first = first_test_hint.getValue();
 				boolean[] changed = new boolean[colours.length];
 				for(Card c : current_state.getHand(id))
 				{
 					if(c == null)
 					{
 						continue;
 					}
 					if(hint.matches("Blue|White|Yellow|Green|Red"))
 					{
 						if(c.getColour().toString() == hint)
 						{
 							changed[count] = true;
 						}
 					}
 					
 					else
 					{
 						if(c.getValue() == Integer.parseInt(hint))
 						{
 							changed[count] = true; 						
 						}
 						
 					}
 					count+=1;
 				}
 				
 				
 				System.out.println("HINTS" + hint);
				//a.print_Card(current_state.getHand(id));
			//	System.out.println("HINT:" + hint);
 				int result = comparison(first, highest_hint, changed , nothing );
 				if(result == 1)
 				{
 					nothing = changed;
 					highest_hint = first.clone();
 					hint_type = hint;
 					return_id = id;
 					//System.out.println("highest_hint:" + hint_type);
 					//a.print_double(highest_hint);
 				}
 				
 				else if (result == 2)
 				{
 					continue;
 				}
 				
 				count = 0;	
 			}
 		
 			
 		}
 		
 			
 		if(hint_type == "")
 		{
 			return null;
 		}
 		int[] answer = new int[3];
 		int colour_number = -1;
 		if(hint_type.matches("Blue|Red|Green|White|Yellow"))
 		{
 			switch(hint_type)
 			{
 			case "Blue":
 				colour_number = 0;
 				break;
 			case "Red":
 				colour_number = 1;
 				break;
 			case "Green":
 				colour_number = 2;
 				break;
 			case "White":
 				colour_number = 3;
 				break;
 			case "Yellow":
 				colour_number = 4;
 				break;
 			default:
 				colour_number = -1;
 				break;
 			}
 			answer[2] = colour_number;
 			answer[1] = 0;
 		}
 		
 		else 
 		{
 			answer[1] = 1;
 			answer[2] = Integer.parseInt(hint_type); 		
 		}
 		
 			answer[0] = return_id;
 		
 	return answer; 
 	}
 	 	
 								
	public void record_hands(State s)
	{
			seen_cards.clear(); 
			discard_pile = s.getDiscards();
			
			Iterator<Card> p = discard_pile.iterator();
			while(p.hasNext())
			{
				Card next = p.next();
				seen_cards.add(next);
			}
			
			
			
			for(int i = 0 ; i < 5 ; i++)
			{
				if(s.getFirework(colours_value[i]) == null)
				{
					continue;
				}
				played_pile.put(colours_value[i], s.getFirework(colours_value[i]));
				for(Card c : s.getFirework(colours_value[i]))
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
					current_cards.put(i, s.getHand(i));
					for(Card c: s.getHand(i))
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
					current_cards.put(i, s.getHand(i));
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
	  
	  public int[] get_highest_index(int[] input)
	  {
		  int[] highest_holdest = new int[2];
		  int highest_index = 0;
		  int container = 0;
		  for(int i = 0 ; i < input.length; i++)
		  {
			  if(input[i] > container)
			  {
				
				  container = input[i];
				  highest_index = i;
			  }
		  }
		  highest_holdest[0] = highest_index;
		  highest_holdest[1] = container;
		  return highest_holdest;
	  }
			  
	 
	  
	
	public void init(State s)
	{
		 current_cards = new HashMap<Integer, Card[]>();
		 played_pile = new HashMap<Colour, Stack<Card>>();
		 discard_pile = new Stack<Card>();
		 numPlayers = s.getPlayers().length;
		 current_playable_cards = new ArrayList<Card>();
		 current_cards_safe_to_discard = new ArrayList<Card>();
		 hinted_cards = new HashMap<Integer, int[][]>();
		 opponent_hint_probabilities = new HashMap<Integer, HashMap<String, double[]>>();
		 seen_cards = new ArrayList<Card>();
		 hint_tokens = s.getHintTokens();
		 fuse_tokens = s.getFuseTokens();
		 if(numPlayers > 3){
			  colours = new Colour[4];
		      values = new int[4];
		      self_playable_chance = new double[4];
		      discard_chance = new double[4];
		      age = new int[4];
		    }
		    else{
		      colours = new Colour[5];
		      values = new int[5];
		      self_playable_chance = new double[5];
		      discard_chance = new double[5];
		      age = new int[5];
		    }
		    index = s.getNextPlayer();
		    firstAction = false;
		
		
	}
	
	public Action playProbablySafe() //play an option if it has a percetange above 0(does not do this if there are 2 fuse tokens as one more mistake would cause death) 
	{
		double value = 0;
		int self_index = 0;
		for(int i = 0 ;  i < self_playable_chance.length ; i++)
		{
			if(self_playable_chance[i] > value)
			{
				value = (float) self_playable_chance[i];
				self_index = i; 
			}
		}
		
		
		if(value >= 0.8 && fuse_tokens > 1)
		{
			  try {
				values[self_index] = 0;
				colours[self_index] = null; 
				age[self_index] = 0;
				return new Action(index, toString(), ActionType.PLAY, self_index);
			} catch (IllegalActionException e) {
				System.out.println("Something has gone wrong");
				e.printStackTrace();
			}
		}
		return null;
		
		
	}
	
	public Action playSafe() //only plays a card if its safe to play 
	{
		double value = 0;
		int self_index = 0;
		for(int i = 0 ;  i < self_playable_chance.length ; i++)
		{
			if(value >= self_playable_chance[i])
			{
				value = self_playable_chance[i];
				self_index = i; 
			}
		}
		
		if(value >= (double)1.0)
		{
			  try {
				values[self_index] = 0;
				colours[self_index] = null; 
				age[self_index] = 0;
				return new Action(index, toString(), ActionType.PLAY, self_index);
			} catch (IllegalActionException e) {
				System.out.println("Something has gone wrong");
				e.printStackTrace();
			}
		}
		return null;
		
		
	}
	
	public Action discardSafe() //Discards the least playable card
	{
		float value = 0;
		int self_index = 0;
		for(int i = 0 ;  i < self_playable_chance.length ; i++)
		{
			if(self_playable_chance[i] < value)
			{
				value = (float) self_playable_chance[i];
				self_index = i; 
			}
		}
		
		if(value < 0.1 && fuse_tokens > 1)
		{
			  try {
				values[self_index] = 0;
				colours[self_index] = null; 
				age[self_index] = 0;
				return new Action(index, toString(), ActionType.DISCARD, self_index);
			} catch (IllegalActionException e) {
				System.out.println("Something has gone wrong");
				e.printStackTrace();
			}
		}
		return null;
		
	}
	
	
	public Action discardOldest() //Discards the oldest card in hand. Only used if there is no info to make a discardSafe() discard. 
	{
		java.util.Random rand = new java.util.Random();
	    int cardIndex = rand.nextInt(colours.length); //Randomly choose a card if the ages of all cards are all 0
	    int oldest_index = -1; 
	    int comparison = -1; 
	    boolean all_zero = true;
	    
	    for(int i : age)
	    {
	    	if(i != 0)
	    	{
	    		all_zero = false;
	    	}
	    
	    }
	    
	    try
	    {
	    	if(all_zero) //discard a random card since there is no info, may be improved later 
	    	{
	    		values[cardIndex] = 0;
	    		colours[cardIndex] = null;
	    		age[cardIndex] = 0;
	    		return new Action(index, toString(), ActionType.DISCARD, cardIndex);
	    	}
	    }
	    
	    catch (IllegalActionException e) {
			System.out.println("SOMETHING IS WRONG");
			e.printStackTrace();
		}
	    
	    
	    
	    for(int item = 0 ; item < age.length ; item++)
	    {
	    	if(age[item] > comparison)
	    	{
	    		comparison = age[item];
	    		oldest_index = item;
	    	}
	    	
	    }
	    
	    
	    try //discard the card with the highest age 
	    {
	    	values[oldest_index] = 0;
    		colours[oldest_index] = null;
    		age[oldest_index] = 0;
    		return new Action(index, toString(), ActionType.DISCARD, oldest_index);
	    }
	    
	    catch (IllegalActionException e) {
			System.out.println("SOMETHING IS WRONG");
			e.printStackTrace();
		}
	    
	    return null;
	}
	
	public Action bestHint() //only hint that will be implemented for now
	{
		int[] hope = get_highest_hint_probabiltiies();
		boolean[] cards = new boolean[colours.length];	
		
		if(hope == null)
		{
			return null;
		}
		
		if(hope[0] == index )
		{
			return null;
		}
		
		if(hope[1] == 0)
		{
			Card[] current_cards_of_opponent = 	current_cards.get(hope[0]); 
				
				for(int p = 0 ; p < colours.length ; p++)
				{
					if(current_cards_of_opponent[p] == null)
					{
						continue;
					}
					
					
					if(current_cards_of_opponent[p].getColour() == colours_value[hope[2]])
					{
						cards[p] = true;
					}
				}
				
				
		}
		
		else if(hope[1] == 1)
		{
			Card[] current_cards_of_opponent = 	current_cards.get(hope[0]); 
			for(int p = 0 ; p < cards.length ; p++)
				{
					if(current_cards_of_opponent[p] == null)
					{
						continue;
					}
				
					if(current_cards_of_opponent[p].getValue() == (hope[2]))
					{
						cards[p] = true;
					}
				}
		}
		
		
		 try {
			 if(hope[1] == 0 && hint_tokens > 2)
			 {
				return new Action(index, toString(), ActionType.HINT_COLOUR, hope[0], cards, colours_value[hope[2]] );
			 }
			 
			 else if(hope[1] == 1 && hint_tokens > 2 )
			 {
				return new Action(index, toString(), ActionType.HINT_VALUE, hope[0], cards, hope[2]);
			 }
		 }
			 
			 catch (IllegalActionException e) {
				System.out.println("Something has gone wrong");
				e.printStackTrace();
			}
		 
			return null;
		
	}	
	

	@Override
	public Action doAction(State s) 
	{
		if(firstAction)
		{
			init(s);
			firstAction = false;
		}
			tester ld = new tester();
			turn_number = s.getOrder();
			fuse_tokens = s.getFuseTokens();
			hint_tokens = s.getHintTokens();
			current_state = s;
			try
			{
				getAll_Hints(s);
			}
			catch(IllegalActionException e)
			{
				System.out.println("Something is Wrong");
			}
			
			System.out.println("PLAYER-ID" + index);
		
			record_hands(s);
			get_safe_playables();
			self_playable_chance = get_percentages_playable(self_playable_chance, index, colours, values);
			ld.print_double(self_playable_chance);
			ld.print_colour(colours);
			ld.print_int(values);
			
			update_ally_play_percentages(); 
			for(int card_ages = 0 ; card_ages < age.length ; card_ages++) //Increment ages of all cards, then set the age of the card that was played/discarded to zero.
			{
				age[card_ages]+=1;
				
			}		
			
			
			
			
			Action a = playSafe();
			
			if(a == null)
			{
				a = playProbablySafe();
			}
			
			if(a == null)
			{
				a = bestHint();
			}
			
			if(a == null)
			{
				a = discardSafe();
				
			}
		
			
			if(a == null)
			{
				a = discardOldest();
			}
			System.out.println(a.toString());
			return a;
	}
	
	
	
	public String toString()
	{
		return "Sadness";
	}
	
	private static final Colour[] colours_value = new Colour[] {Colour.BLUE,Colour.RED,Colour.GREEN,Colour.WHITE,Colour.YELLOW};

}
