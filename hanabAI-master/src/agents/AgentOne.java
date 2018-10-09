package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
	float[] playable_chance;	//chance of card in hand being playable
	float[] discard_chance;  	// chance of card in hand being able to be discarded 
	
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
	
	public void get_count(HashMap<Card, Integer> output , Stack<Card> discard_pile) //get how many of each card is in the disacrd pile 
	{
		for(Card item : discard_pile)
		{
			for(int i = 0 ; i < deck.length; i++)
			{
				if(item.equals(deck[i]))
				{
					if(output.get(item) == null)
					{
						output.put(item, 1);
					}
					
					else
					{
						output.put(item, output.get(item) + 1);
					}
				}
				
			}
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
 		
 		for(Colour c : colours_value) //Step 1, this checks for cards that have already been played
 		{
 			int playable_rank = playable(c);
 			for(int i = playable_rank ; i >= 1 ; i--)
 			{
 				Card discard_card = new Card(c, i);
 				discard_safe.add(discard_card);
 			}
 		}
 		HashMap<Card, Integer> number_of_cards_discarded = new HashMap<Card, Integer>();
 		get_count(number_of_cards_discarded, discard_pile); 
 		Iterator<Entry<Card, Integer>> iterate = number_of_cards_discarded.entrySet().iterator(); 
 		
 		while(iterate.hasNext()) //Step 2, this checks counts of each discarded card value of each colour and places them into the HashMap 
 		{
 			Map.Entry colour_value = iterate.next();
 			Card current_card = (Card) colour_value.getKey(); 
 			Colour colour_type = current_card.getColour();
 			int value = current_card.getValue();
 			for(int a = 0 ; a < colours_value.length; a++)
 			{
 				if(colour_type == colours_value[a])
 				{
 					if(value == 1 && (int) colour_value.getValue() == 3)
 	 				{
 	 					for(int i = value ; i < 6; i++)
 	 					{
 	 						discard_safe.add(new Card(colours_value[a], i)); 
 	 					}
 	 				}
 	 			
 	 				else if(value >= 2 && value <= 5 && (int) colour_value.getValue() == 2)
 	 				{
 	 					for(int i = value ; i < 6; i++)
 	 					{
 	 						discard_safe.add(new Card(colours_value[a], i)); 
 	 					}
 	 				
 	 				}
 	 			
 	 				else if( value == 5 && (int) colour_value.getValue() == 1)
 	 				{
 	 					discard_safe.add(new Card(colours_value[a], 5)); 				
 	 				}
 				}
 			}
 		}
 		
 		Iterator<Entry<Integer, Card[]>> iterator_step_3 = current_cards.entrySet().iterator();
 		
 		while(iterator_step_3.hasNext())  //Step 3, this checks each other player's hand(you can't see your own) and adds them to the safe discard list should there not already be a copy there(2 for one)
 		{
 			Map.Entry each_player_hand = iterator_step_3.next();
 			int current_player = (int) each_player_hand.getKey();
 			Card[] player_hand = (Card[]) each_player_hand.getValue(); 
 			
 			if(current_player == index) //Don't consider what your current cards are, can be reworked later
 			{
 				continue;
 			}
 			
 			else
 			{
 				for(int i = 0; i < player_hand.length; i++)
 				{
 					int card_type_already_present = Collections.frequency(discard_safe , player_hand[i]);
 					
 					if(player_hand[i].getValue() == 1 && card_type_already_present > 1)
 					{
 						continue;
 					}
 					
 					else if(player_hand[i].getValue() == 1)
 					{
 						
 						discard_safe.add(new Card(player_hand[i].getColour(), player_hand[i].getValue()));
 					}
 					
 					else if(card_type_already_present == 0)
 					{
 						discard_safe.add(new Card(player_hand[i].getColour(), player_hand[i].getValue()));
 					}
 				}
 			}
 		}
 	}
 		
 	
 	public void get_percentages_playable() //Given what we know so far, give each card in the hand a chance of being safe to be played
 	{
 		Card[] current_known_cards = new Card[colours.length];
 		for(int i = 0 ; i < current_known_cards.length ; i++)
 		{
 			for(int a = 0 ; a < colours.length ; a++)
 			{
 				current_known_cards[i] = new Card(colours[a], values[a]);				
 			}
 		}
 		
 		for(int d = 0 ; d < current_known_cards.length; d++)
 		{
 			Colour c = current_known_cards[d].getColour();
 			int i = current_known_cards[d].getValue();
 			
 			for(int b = 0 ; b < current_playable_cards.size(); b++)
 			{
 				if(current_known_cards[d].equals(current_playable_cards.get(b)))
 				{
 					playable_chance[d] += 1.0;
 					break;
 				}
 				
 				if(c == current_playable_cards.get(b).getColour() && c!= null)
 				{
 					playable_chance[d] += 0.5;
 					continue;
 				}
 				
 				if(i == current_playable_cards.get(b).getValue() && i != 0)
 				{
 				   playable_chance[d] += 0.5;
 				}
 			}
 		}
 	}
 	
 	
 	public void get_percentages_discards() //Given what we know far, give each card in hand a chance of being safe to play 
 	{
 		Card[] current_known_cards = new Card[colours.length];
 		for(int i = 0 ; i < current_known_cards.length ; i++)
 		{
 			for(int a = 0 ; a < colours.length ; a++)
 			{
 				current_known_cards[i] = new Card(colours[a], values[a]);				
 			}
 		}
 		
 		for(int d = 0 ; d < current_known_cards.length; d++) //currently just a copy of the get_percentages_playable, logic can be improved as we go further 
 		{
 			Colour c = current_known_cards[d].getColour();
 			int i = current_known_cards[d].getValue();
 			
 			for(int b = 0 ; b < current_playable_cards.size(); b++)
 			{
 				if(current_known_cards[d].equals(current_cards_safe_to_discard.get(b))) 
 				{
 					playable_chance[d] += 1.0;
 					break;
 				}
 				
 				if(c == current_cards_safe_to_discard.get(b).getColour() && c!= null)
 				{
 					playable_chance[d] += 0.5;
 					continue;
 				}
 				
 				if(i == current_cards_safe_to_discard.get(b).getValue() && i != 0)
 				{
 				   playable_chance[d] += 0.5;
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
		 turns_bought = 0; 
		 current_playable_cards = new ArrayList<Card>();
		current_cards_safe_to_discard = new ArrayList<Card>();
		 if(numPlayers==5){
			  colours = new Colour[4];
		      values = new int[4];
		      playable_chance = new float[4];
		      discard_chance = new float[4];
		    }
		    else{
		      colours = new Colour[5];
		      values = new int[5];
		      playable_chance = new float[5];
		      discard_chance = new float[5];
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
