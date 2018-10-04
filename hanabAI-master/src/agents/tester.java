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
import hanabAI.IllegalActionException;
import hanabAI.State;

public class tester {
	
	public static void main(String[] args) throws IllegalActionException
	{
		Stack<Card> deck = Card.shuffledDeck();
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
		tester.record_hands(state);
		
	}
	
	
	
}
