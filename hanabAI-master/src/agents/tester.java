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
	
	
	public void print(Colour[] s)
	{
		String output = "";
		for(int i = 0 ; i < s.length; i++)
		{
			if(s[i] == null)
			{
				continue;
			}
			output = output + " " +  s[i].toString();
		}
		System.out.println(output);
	}
	
	
	public void print_int(int[] s)
	{
		String output = "";
		for(int i = 0 ; i < s.length; i++)
		{
			output = output + " " + Integer.toString(s[i]); 
		}
		System.out.println(output);
	}
	
	
	
	
	
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
		new_agent.getHints(s);
		tester t = new tester();
		t.print(new_agent.colours);
		t.print_int(new_agent.values);;
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
	
		ArrayList<Card> test_arraylist = new ArrayList<Card>();
		new_agent.step2_discard(test_arraylist);
		/*for(Card c: test_arraylist)
		{
			System.out.println("Colour:" + c.getColour().toString() + "Value:" + c.getValue());
		} */ 
		
	}
	
}
	

