package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Agent;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.Hanabi;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class tester {
	
	public static void main(String[] args) throws IllegalActionException
	{
		final Colour[] colours_value = new Colour[] {Colour.BLUE,Colour.RED,Colour.GREEN,Colour.WHITE,Colour.YELLOW};
		
		/*Stack<Card> deck = Card.shuffledDeck();
		Agent[] agents = {new agents.BasicAgent(),new agents.BasicAgent(), new agents.BasicAgent()};
		String[] s = new String[agents.length];
		for(int i=0; i<s.length; i++)s[i] = agents[i].toString();
		Action a = new Action(0, "Sadness", ActionType.DISCARD, 2);
		State state = new State(s, deck);
		AgentOne tester = new AgentOne();
		tester.init(state);
	//	tester.record_hands(state);
		state.nextState(a, deck);
		Stack<Card> testing_discard = state.getDiscards(); 
		Iterator m = state.getDiscards().iterator();
		while(m.hasNext())
		{
			System.out.println(m.next().toString());
		}
		tester.record_hands(state); */
		Agent[] tester_agents = new Agent[] {new agents.BasicAgent(),new agents.BasicAgent(), new agents.BasicAgent()}; 
		Hanabi test = new Hanabi(tester_agents);
		test.play();
		State s = test.getState();
		AgentOne new_agent = new AgentOne();
		new_agent.init(s);
		new_agent.record_hands(s);
		Iterator i = s.getDiscards().iterator();
		/*for(Colour c : colours_value)
		{
			Iterator i = s.getFirework(c).iterator();
			while(i.hasNext())
			{
				Card a = (Card) i.next();
				System.out.println("Playable Card:" + " " + "Colour:" + a.getColour() + "Value:" + a.getValue()); 
			}
			
		}*/
		while(i.hasNext())
		{
			Card a = (Card) i.next();
			System.out.println("Playable Card:" + " " + "Colour:" + a.getColour() + "Value:" + a.getValue()); 
		}
		
		
		
		s.getDiscards();
		
		
		
		ArrayList<Card> test_arraylist = new ArrayList<Card>();
		new_agent.get_safe_discards(test_arraylist);
		for(Card c: test_arraylist)
		{
			System.out.println("Colour:" + c.getColour().toString() + "Value:" + c.getValue());
		}
		
	}
	
}
	

