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
	Colour[] colours;
	private int[] values;
	Card[] hand; 
	private boolean firstAction = true;
	private int numPlayers;
	ArrayList<Card> seen_cards;
	HashMap<Integer, Card[]> current_cards; 
	HashMap<Colour, Stack<Card>> played_pile;
	Stack<Card> discard_pile; 
	int index; 
	int turns_bought; 
	
	
	public void record_hands(State s)
	{		
			discard_pile = s.getDiscards();
			/*Iterator m = discard_pile.iterator();
			while(m.hasNext())
			{
				System.out.println(m.next().toString());
			}*/
			
			Colour[] colours_value = new Colour[] {Colour.BLUE,Colour.RED,Colour.GREEN,Colour.WHITE,Colour.YELLOW};
 
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
				current_cards.put(i, s.getHand(i));
				for(Card hand : s.getHand(i))
				{
					if(!seen_cards.contains(hand))
					{
						seen_cards.add(hand);
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
		    hand = s.getHand(index); 
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
			hand = s.getHand(index); 
			
			
		}
		
		return null;
	}
	
	
	public String toString()
	{
		return "Sadness";
	}

}
