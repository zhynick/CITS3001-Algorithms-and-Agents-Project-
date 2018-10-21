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
	double[] self_playable_chance; //chance of each card being safe to play
	double[] self_discard_chance; //chance of each card being safe to discard
	ArrayList<Card> seen_cards; //Similiar to Agent One, stores all cards that've been seen
	HashMap<Colour, Stack<Card>> played_pile; //what cards have been played
	HashMap<Integer, Card[]> current_cards;  //Current hand of each player, mapped to an Int value(aside from your own)
	HashMap<Integer, int[][]> hinted_cards; //what do opponents know about their cards
	HashMap<Integer, HashMap<String, double[]>>opponent_hint_probabilities; 
	HashMap<Integer, List<double[]>> opponent_current_played_discard_proabilities; 
	Stack<Card> discard_pile; //cards that have been discarded
	ArrayList<Card> safe_to_discard; //cards that are safe to discard
	ArrayList<Card> current_playable_cards;  //cards that are safe to play
	boolean firstAction = true; //is it the first action
	boolean lastTurn false;
	int deckSize;
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
	
	//given two double arrays, and what has been changed in each, get the higher average value of the two 
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

 		if(first_sum >= second_sum) //If the average of the first is higher, return the first array, else return the second. 
 		{
 			return 1;
 		}
 		
 		else
 		{
 			return 2; 
 		}
 	}
 	
 	
 	//Given what you know your opponents know, get how much each possible hint increases their chance of playing a card in their hand 
 	public void update_ally_play_percentages()
 	{	for(int i = 0 ; i < numPlayers ; i++)
 		{
 			if(i == index)
 			{
 				continue;
 			}
 			
 			
 			//Hinted Cards is a HashMap that links the player's id to a 2D array, each 2D array reflects what they know about their hand.
 			//If Player 1 knows they the first card in their hand is red, hinted cards.get(1) would return a 2D array with array[0][0] being 1, 
 			//if they knew the value array[0][1] would be 1, and if they knew both both would be 1
 			Colour[] opponent_colours = new Colour[colours.length];
 			int[] opponent_values = new int[colours.length];
 			int count = 0;
 			int[][] opponent_hints = hinted_cards.get(i);
 			HashMap<String, double[]> hint_probailities = new HashMap<String, double[]>();
 			for(int card[] : opponent_hints) //This translates the 2D array into hinted cards into the actual colour/values the opponent knows
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
 			
 			
 			for(int b = 0 ; b < current_state.getHand(i).length; b++) //iterate over each card in their hand 
 			{
 				if(current_state.getHand(i)[b] == null)//if they don't have a card at the index, skip
				{
						continue;
				}
 				
 				else if(opponent_colours[b] != null && opponent_values[b] != 0) //all information is known , no more hints are needed
 				{
 					continue;
 				}
 				
 				else if(opponent_colours[b] == null && opponent_values[b] != 0) //only the value is known 
 				{
 					Colour unknown = current_state.getHand(i)[b].getColour(); //get the actual colour of that card
 					opponent_colours[b] = unknown;
 					int[] changed = new int[colours.length];
 					for(int index = 0 ; index < current_state.getHand(i).length; index++)
 					{
 						if(current_state.getHand(i)[index] == null)
 						{
 							continue;
 						}
 						if(opponent_colours[index] == null && current_state.getHand(i)[index].getColour().equals(unknown))
 						{
 							opponent_colours[index]  = unknown; //simulating giving them the hint for that colour, change each unknown card in the hand to that colour
 						}
 					}
 					double[] input = new double[colours.length];
 					double[] playable_chance = get_percentages_playable(input, i , opponent_colours, opponent_values); //calculate the new playable value based on the hint
 					hint_probailities.put(unknown.toString(),playable_chance); //add the changed playable value to a HashMap that links what the hint was to the new playable value
 				}
 				
 				else if(opponent_colours[b] != null && opponent_values[b] == 0 )//if only the colour is known
 				{
 					int unknown = current_state.getHand(i)[b].getValue();
 					opponent_values[b] = unknown;
 					int[] changed = new int[colours.length];
 					for(int index = 0 ; index < current_state.getHand(i).length; index++) //do the same, but try giving them the value instead
 					{
 						if(current_state.getHand(i)[index] == null)
 						{
 							continue;
 						}
 						
 						if(opponent_values[index] == 0 && current_state.getHand(i)[index].getValue() == unknown)
 						{
 							opponent_values[index]  = unknown; //simulating giving them the hint for that value, change each unknown card in the hand to that value
 						}
 					}
 					double[] input = new double[colours.length];
 					double[] playable_chance = get_percentages_playable(input, i , opponent_colours, opponent_values);
 					hint_probailities.put(Integer.toString(unknown),playable_chance); //add that hinted value and the change playability value to a hashmap 
 				}
 				
 				//If they don't know the hint or the colour, only place the hint that gave a higher playable percentage to the hashmap 
 				else //nothing is known 
 				{
 					int unknown_value = current_state.getHand(i)[b].getValue();
 					Colour unknown_colour = current_state.getHand(i)[b].getColour();  
 					opponent_values[b] = unknown_value;
 					boolean[] changed_one = new boolean[colours.length]; //as we are using comparison here, we need to note which cards were changed by the new knowledge
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
 					double[] playable_chance_value = get_percentages_playable(input, i , opponent_colours, opponent_values); //test value 
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
 					double[] playable_chance_colour = get_percentages_playable(input2, i , opponent_colours, opponent_values); //test colour
 					int result = comparison(playable_chance_value, playable_chance_colour, changed_one, changed_two); //compare the two
 					if(result == 1) //if value gives a higher chance
 					{
 						hint_probailities.put(Integer.toString(unknown_value), playable_chance_value);
 						
 					}
 					
 					else if(result == 2) //if colour gives a higher chance
 					{
 						hint_probailities.put(unknown_colour.toString(), playable_chance_colour);
 						
 						
 					}
 				}
 				opponent_colours = clone_opponent_colours.clone(); //reset colours and values
 				opponent_values = clone_opponent_values.clone();
 		}
 			
 			if(	opponent_hint_probabilities.get(i) == null) //add all the possible hints for a player into a hashmap that links their id to the hint hashmap 
 			{
 				opponent_hint_probabilities.put(i, hint_probailities);
 			}
 			else
 			{
 				opponent_hint_probabilities.remove(i);
 				opponent_hint_probabilities.put(i, hint_probailities);
 			}
 		}
 	}
 	
 	//Using the New Hashmap, get the hint that gives the highest chance of playing something. 
 	public int[] get_highest_hint_probabiltiies()
 	{
 		Iterator<Entry<Integer, HashMap<String, double[]>>> current_iterator = opponent_hint_probabilities.entrySet().iterator();
 		double[] highest_hint = new double[colours.length];
 		String hint_type = "";
 		int return_id = -1;	
 		boolean[] nothing = new boolean[colours.length];

 		while(current_iterator.hasNext()) //Iterate through each player
 		{
 			Entry<Integer, HashMap<String, double[]>> current = current_iterator.next();
 			int id = current.getKey(); //get their id
 			HashMap<String, double[]> current_hint = current.getValue();
 			Iterator<Entry<String, double[]>> hint_iterator = current_hint.entrySet().iterator(); //get their hint hashmap 
 			
 			while(hint_iterator.hasNext()) //iterate through each possible hint for that player
 			{
 				int count = 0;
 				Entry<String, double[]> first_test_hint  = hint_iterator.next();
 				String hint = first_test_hint.getKey(); //this is the hint being tested
 				double[] first = first_test_hint.getValue(); //this is how the hint changes the probability chance 
 				boolean[] changed = new boolean[colours.length];
 				for(Card c : current_state.getHand(id)) //This gives the array to see which cards were changed by that hint
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
 				
 				int result = comparison(first, highest_hint, changed , nothing ); //compare the next hint to the current hint
 				if(result == 1) //if the next hint is better
 				{
 					nothing = changed; //set it to the next hint's boolean array
 					highest_hint = first.clone(); //set the probabilities to the next hint's changed probabilities
 					hint_type = hint; //set the hint to the next hint
 					return_id = id; //set the id of the player receiving the hint to the next id
 				}
 				
 				else if (result == 2) //keep the current hint if its better
 				{
 					continue;
 				}
 				
 				count = 0;	
 			}
 		
 			
 		}
 		
 			
 		if(hint_type == "") //If there is no good hint
 		{
 			return null;
 		}
 		int[] answer = new int[3]; //answer[0] gives the id of the player receiving the hint, answer[1] gives the hint type , answer[2] gives what the hint is (red/blue..)
 		int colour_number = -1;
 		if(hint_type.matches("Blue|Red|Green|White|Yellow")) //if the hint is a colour, set the array
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
 		
 	return answer; //return that answer
 	}
	
	public double[] fillDiscard(double[] discard_chance, int player_id, Colour[] input_colour, int[] input_value) //in this case, input_colour and input_values are what the player knows about their hand
	{
		double[] discard_probability= new double[colours.length];
		tester b = new tester();
		b.print_int(input_value);
		b.print_colour(input_colour);
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
							current_probability += (double) (3- safe_colour[i])/(double) (10-same_colour_seen);
							break;
						
						case 5:
							current_probability += (double) (1-safe_colour[i])/(double) (10-same_colour_seen); 
							break;
						
						default:
							current_probability += (double) (2-safe_colour[i])/(double) (10-same_colour_seen);
							break;
						
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
						current_probability+= (double)(3-count)/(double) (15-copies_seen); 		//total 15 1s
						break;
						
					case 5:
						current_probability+= (double)(1-count)/(double) (5-copies_seen); 		//total 5 5s
						break;
						
					default:
						current_probability+= (double)(2-count)/(double)(10-copies_seen);		//total 10 2s, 3s, 4s
						break;
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
						current_probability += (double) (3-number_seen)/(double)(52-seen_cards.size());
						break;
					
					case 5:
						current_probability += (double) (1-number_seen)/(double)(52 - seen_cards.size());
						break;
						
					default:
						current_probability += (double) (2-number_seen)/(double)(52- seen_cards.size());
						break;
					}
				}
					
			}
					
				discard_probability[a] = current_probability;
		}
	
		b.print_double(discard_probability);
		
		discard_chance = discard_probability;
		return discard_probability;
	}
	 
	public void init(State s)
	{
	
		played_pile = new HashMap<Colour, Stack<Card>>();
		discard_pile = new Stack<Card>();
		safe_to_discard = new ArrayList<Card>();
		current_cards = new HashMap<Integer, Card[]>();
		current_playable_cards = new ArrayList<Card>();
		hinted_cards = new HashMap<Integer, int[][]>();
		opponent_hint_probabilities = new HashMap<Integer, HashMap<String, double[]>>();
		opponent_current_played_discard_proabilities = new HashMap<Integer, List<double[]>>();
		numPlayers = s.getPlayers().length;
		seen_cards = new ArrayList<Card>();
		hint_tokens = s.getHintTokens();
		fuse_tokens = s.getFuseTokens();
		
		
		if(numPlayers==5){
		  colours = new Colour[4];	
	      values = new int[4];
	    }
	    else{
	      colours = new Colour[5];
	      values = new int[5];
	    }
	    index = s.getNextPlayer();
	    firstAction = false;
	
	}
	
	public void setOpponentProbabilities()
	{
		Iterator<Entry<Integer, int[][]>> current_id = hinted_cards.entrySet().iterator();
		while(current_id.hasNext())
		{
			Entry<Integer, int[][]> current = current_id.next();
			int id = current.getKey();
			int[][] hints = current.getValue();
			int count = 0;
			int[] opponent_values = new int[colours.length];
			Colour[] opponent_colours = new Colour[colours.length];
			
			for(int card[] : hints) //This translates the 2D array into hinted cards into the actual colour/values the opponent knows
 			{
 				if(card[0] == 1 && card[1] == 1)
 				{
 					opponent_colours[count] = current_state.getHand(id)[count].getColour();
 					opponent_values[count] = current_state.getHand(id)[count].getValue();
 				}
 				
 				
 				else if(card[0] == 1 && card[1] == 0)
 				{
 					opponent_colours[count] = current_state.getHand(id)[count].getColour(); 
 				}
 				
 				else if(card[1] == 1 && card[0] == 0)
 				{
 					opponent_values[count] = current_state.getHand(id)[count].getValue();
 				}
 				
 				else
 				{
 					// no info noted
 				}
 				count++;
 			}
			count = 0;
			double[] opponent_discard_chance = new double[colours.length];
			double[] opponent_playable_chance = new double[colours.length];
			opponent_discard_chance = fillDiscard(opponent_discard_chance, id, opponent_colours, opponent_values);
			opponent_playable_chance = get_percentages_playable(opponent_playable_chance, id, opponent_colours, opponent_values);
			
			List<double[]> current_list = new ArrayList<double[]>();
			current_list.add(0, opponent_playable_chance);
			current_list.add(1, opponent_discard_chance);
			if(opponent_current_played_discard_proabilities.get(id) == null)
			{
				opponent_current_played_discard_proabilities.put(id, current_list);
			}
			
			else
			{
				opponent_current_played_discard_proabilities.replace(id, current_list);
			}
		}
	}
	
	public Action best_Action()
	{
		double hint_token_probability = 0.1*(hint_tokens);
		double fuse_token_probability = 0.15*(fuse_tokens);
		
		
		double average_playable_chance = average(self_playable_chance);
		double average_discard_chance = average(self_discard_chance);
		
		double[][] opponent_chances = new double[numPlayers][2]; //0 is average playable chance, 1 is average discard chance
		
		for(int i = 0 ; i < opponent_chances.length; i++)
		{
			
			if(i == index) //dont need to do this for yourself
			{
				continue;
			}
			
			opponent_chances[i][0] = average(opponent_current_played_discard_proabilities.get(i).get(0));
			opponent_chances[i][1] = average(opponent_current_played_discard_proabilities.get(i).get(1));
		}
		
		double deckSize_left = 0.01 * (double) deckSize;
		
		
		
		return null;
		
	}
	
	public Action playProbablySafe() //play an option if it has a percetange above 0(does not do this if there are 2 fuse tokens as one more mistake would cause death) 
	{
		double value = 0;
		int self_index = 0;
		for(int i = 0 ;  i < self_playable_chance.length ; i++)//get the card which has the highest play probability 
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
	
	public Action playSafe() //only plays a card if its safe to play, with probability being 100%
	{
		double value = 0;
		int self_index = 0;
		for(int i = 0 ;  i < self_playable_chance.length ; i++)//get the card which has the highest play probability 
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
		for(int i = 0 ;  i < self_playable_chance.length ; i++)//inverse of play safe, discard the card that has the least chance of being played
		{
			if(self_playable_chance[i] < value)
			{
				value = (float) self_playable_chance[i];
				self_index = i; 
			}
		}
		
		if(value < 0.1 && fuse_tokens > 1)//if that card has less then 0.1 chance of being played, discard it, else return null
		{
			  try {
				values[self_index] = 0;
				colours[self_index] = null; 
				return new Action(index, toString(), ActionType.DISCARD, self_index);
			} catch (IllegalActionException e) {
				System.out.println("Something has gone wrong");
				e.printStackTrace();
			}
		}
		return null;
		
	}
	
	
	public Action bestHint() //only hint that will be implemented for now
	{
		int[] hope = get_highest_hint_probabiltiies(); //get the int array from highest_hint_probabilities
		boolean[] cards = new boolean[colours.length];	
		
		if(hope == null)
		{
			return null;
		}
		
		if(hope[0] == index )
		{
			return null;
		}
		
		if(hope[1] == 0) //if hint is a colour
		{
			Card[] current_cards_of_opponent = 	current_cards.get(hope[0]);  //set the boolean array
				
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
		
		else if(hope[1] == 1) //if hint is a number
		{
			Card[] current_cards_of_opponent = 	current_cards.get(hope[0]);  //set the boolean array
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
		
		
		 try { //give the hint
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
	
	public Action doAction(State s) 
	{
		if(firstAction)
		{
			init(s);
			firstAction = true;
		}
		if(current_state.getOrder() >= current_state.getFinalActionIndex())
		{
			lastTurn = true;
		}
		
		current_state = s;
		hint_tokens = s.getHintTokens();
		fuse_tokens = s.getFuseTokens();
		
		try {
			getAll_Hints(current_state);
		} catch (IllegalActionException e) {
			
			e.printStackTrace();
		}
		setCards();
		deckSize = 50 - seen_cards.size();
		
		Discard discard_class = new Discard(discard_pile, fuse_tokens, seen_cards, current_cards , played_pile);
		safe_to_discard = discard_class.get_safe_discards();
		self_playable_chance = get_percentages_playable(self_playable_chance, index, colours, values);
		self_discard_chance = fillDiscard(self_discard_chance, index, colours, values);
		
		
		
		return null;
	}


	
	
	
	
	public String toString()
	{
		return "Unknown"; 
		
		
		
	}
	
	
	
	
	private static final Colour[] colours_value = new Colour[] {Colour.BLUE,Colour.RED,Colour.GREEN,Colour.WHITE,Colour.YELLOW};
}
