package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
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
	

	public void print_double(double[] s)
	{
		String output = "";
		for(int i = 0 ; i < s.length; i++)
		{
			output = output + " " + Double.toString(s[i]); 
		}
		System.out.println(output);
	}
	
	public void print_colour(Colour[] s)
	{
		String output = "";
		for(int i = 0 ; i < s.length; i++)
		{
			if(s[i] == null)
			{
				output = output + " " + "Unknown";
				continue;
			}
			output = output + " " + s[i].toString(); 
		}
		System.out.println(output);
	}
	
	public void print_Card(Card[] s)
	{
		String output = "";
		for(int i = 0 ; i < s.length; i++)
		{
			if(s[i] == null)
			{
				output = output + " " + "Unknown";
				continue;
			}
			output = output + " " + s[i].toString(); 
		}
		System.out.println(output);
	}
	
	public void print_boolean(boolean[] s)
	{
		String output = "";
		for(int i = 0 ; i < s.length; i++)
		{
			
			output = output + " " + s[i];
		}
		System.out.println(output);
	}
	
	
	public static double average(double[] input)
	{
		double item = 0;
		for(double a : input)
		{
			item+=a;
		}
		item = item/(double) input.length;
		return item;
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
		Agent[] tester_agents = new Agent[] {new agents.AgentOne(), new agents.AgentOne(), new agents.AgentOne()}; 
		Hanabi test = new Hanabi(tester_agents);
		StringBuffer log = new StringBuffer("A simple game for three basic agents:\n");
		int result = test.play(log);
		State s = test.getState();
		
		/*AgentOne one = new AgentOne();
		one.init(s);
		one.get_safe_playables();
		double[] self = new double[5];
		int player_id = 1;
		Colour[] input_colour = new Colour[] {null,null,null,null,null};
		int[] input_value = new int[] {1,1,1,1,1};
		double[] two = one.get_percentages_playable(self, player_id, input_colour, input_value); */
		
		
		
		
		/*AgentOne tester = new AgentOne();
		tester.init(s);
		tester.getHints(s);
		tester.getAll_Hints(s);
		HashMap<Integer, int[][]> new_map = tester.hinted_cards;
		Iterator<Entry<Integer, int[][]>> a = new_map.entrySet().iterator();
		tester ac = new tester();
		tester.current_state = s;
		tester.record_hands(s);
		tester.get_safe_playables();
		tester.get_percentages_discards();
		tester.update_ally_play_percentages();
		tester.get_highest_hint_probabiltiies();
		
		HashMap<Integer, HashMap<String, double[]>> as = tester.opponent_hint_probabilities;
		
		HashMap<String, double[]> current = as.get(1);
		System.out.println("adadad:" + current.size());
		Iterator<Entry<String, double[]>> i = current.entrySet().iterator();
		while(i.hasNext())
		{
			Entry<String, double[]> ad = i.next();
			double ap = average(ad.getValue());
			String p = ad.getKey();
			System.out.println("hint:" +  p + "value:" +  ap);
		}
		
		
		
		
		/*AgentOne new_agent = new AgentOne(); 
		new_agent.doAction(s);
		Discard tester = new Discard(new_agent.discard_pile, new_agent.index, new_agent.current_cards_safe_to_discard, new_agent.current_cards, new_agent.played_pile);
		ArrayList<Card> current_pile = tester.get_safe_discards();
		System.out.println(s.getPlayers().length); */
		log.append("The final score is "+result+".\n");
		System.out.print(log); 
		
		// Early Game -- Mid Game -- Late Game 
		
		// 1,2 -- 3 --- 4    Always value 5?
		
		//Make a seperate game state for each colour depending on what's been played. 
		
		
		
		
	}
	
}
	

