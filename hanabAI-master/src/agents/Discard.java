package agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.Map.Entry;

import hanabAI.Card;
import hanabAI.Colour;

public class Discard {
	Stack<Card> discard_pile;
	int index;
	ArrayList<Card> current_cards_safe_to_discard; //Cards that are currently safe to discard given a board state s(will take into account deck limit)
	HashMap<Integer, Card[]> current_cards;  //Current hand of each player, mapped to an Int value(aside from your own)
	HashMap<Colour, Stack<Card>> played_pile; //What cards have been played, sorted by colour
	
	public Discard(Stack<Card> a, int b, ArrayList<Card> c, HashMap<Integer, Card[]> d, HashMap<Colour, Stack<Card>> e)
	{
		discard_pile = a;
		index = b;
		current_cards_safe_to_discard = c;
		current_cards = d;
		played_pile = e;
	}
	
	private static final Colour[] colours_value = new Colour[] {Colour.BLUE,Colour.RED,Colour.GREEN,Colour.WHITE,Colour.YELLOW};
	
	public void get_count(HashMap<Card, Integer> output , Stack<Card> discard_pile) //get how many of each card is in the disacrd pile 
	{
		Card c = null; 
		boolean contains = false; 
		for(Card item : discard_pile)
		{	
			Iterator<Entry<Card, Integer>> i = output.entrySet().iterator();
			while(i.hasNext())
			{
				Card a = i.next().getKey();
				if(item.equals(a))
				{
					c = a;
					contains= true;
					break;
				}
			}
			if(!contains)
			{
				output.put(item,  1);
			}
			
			else
			{
				contains = false;
				int a = output.get(c);
				output.replace(c,  a + 1);
			}	
		}
		
	}
	
	public int playable(Colour c){ //repurposing Tim French's code 
	    java.util.Stack<Card> fw = played_pile.get(c);
	    if (fw.size()==5) return -1;
	    else return fw.size()+1;
	  }
	
	
	
	public void step1_discard(ArrayList<Card> discard_safe)
	{
		for(Colour c : colours_value) //Step 1, this checks for cards that have already been played
 		{
 			int playable_rank = playable(c);
 			for(int i = playable_rank-1 ; i >= 1 ; i--)
 			{
 				Card discard_card = new Card(c, i);
 				discard_safe.add(discard_card);
 			}
 		}
	
	}
	
	public void step2_discard(ArrayList<Card> discard_safe) //Step 2, this checks counts of each discarded card value of each colour and places them into the HashMap 
	{
		HashMap<Card, Integer> number_of_cards_discarded = new HashMap<Card, Integer>();
 		get_count(number_of_cards_discarded, discard_pile);
 		Iterator<Entry<Card, Integer>> iterate = number_of_cards_discarded.entrySet().iterator(); 
 		
 		while(iterate.hasNext()) 
 		{
 			Entry<Card, Integer> colour_value = iterate.next();
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
 	 			
 	 				else if(value >= 2 && value < 5 && (int) colour_value.getValue() == 2)
 	 				{
 	 					for(int i = value ; i < 6; i++)
 	 					{
 	 						discard_safe.add(new Card(colours_value[a], i)); 
 	 					}
 	 				
 	 				}
 				}
 			}
 		}
	}
	
	
	public void step3_discard(ArrayList<Card> discard_safe)
	{
		Iterator<Entry<Integer, Card[]>> iterator_step_3 = current_cards.entrySet().iterator();
 		while(iterator_step_3.hasNext())  //Step 3, this checks each other player's hand(you can't see your own) and adds them to the safe discard list should there not already be a copy there(2 for one)
 		{
 			Entry<Integer, Card[]> each_player_hand = iterator_step_3.next();
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
 					if(player_hand[i] == null)
 					{
 						continue;
 					}
 					int card_type_already_present = Collections.frequency(discard_safe , player_hand[i]) + Collections.frequency(played_pile.get(player_hand[i].getColour()), player_hand[i]);
 					
 					if(player_hand[i].getValue() != 1 && card_type_already_present > 1)
 					{
 						continue;
 					}
 					
 					else if(player_hand[i].getValue() == 1)
 					{
 						
 						discard_safe.add(new Card(player_hand[i].getColour(), player_hand[i].getValue()));
 					}
 					
 					else if(player_hand[i].getValue() == 5)
 					{
 						continue;
 					}
 					
 					else if(card_type_already_present == 0)
 					{
 						discard_safe.add(new Card(player_hand[i].getColour(), player_hand[i].getValue()));
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
 	public ArrayList<Card> get_safe_discards() //This method gets cards that are safe to discard from the current board state
 	{
 		current_cards_safe_to_discard.clear();
 		//Colour[] discard_safe_colours = new Colour[deck.length];
 		//int[] discard_safe_values = new int[deck.length];
 		step1_discard(current_cards_safe_to_discard); //Step 1, this checks for cards that have already been played
 		step2_discard(current_cards_safe_to_discard); //Step 2, this checks counts of each discarded card value of each colour and places them into the HashMap 
 		step3_discard(current_cards_safe_to_discard);
 		
 		return current_cards_safe_to_discard;
 	
 	}

}
