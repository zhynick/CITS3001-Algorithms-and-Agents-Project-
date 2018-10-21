package agents;

import hanabAI.Agent;
import hanabAI.Hanabi;

/**
 * Plays a set number of games and keeps track of results
 * 
 * @author Jason Cheng, 2018
 */

public class GameAvg {
	
	private static int numberOfGames = 1000;
	
	public static void main(String[] args){
		
		float acc = 0;
		int min = 25;
		int max = 0;
		int perfects = 0;
		int zeroes = 0;
		for (int i = 0; i<numberOfGames; i++) {
			Agent[] agents = {new agents.AgentOne(), new agents.AgentOne(), new agents.AgentOne()};
			Hanabi H = new Hanabi(agents);
			int result = H.play(new StringBuffer());
			if (result > max) max = result;
			if (result < min) min = result;
			if (result == 25) perfects++;
			if (result == 0) zeroes++;
			//System.out.println(result);
			acc += result;
			
		}
		System.out.printf("Simulated %s games\n\n", numberOfGames);
		System.out.printf("  Average result: %f\n", acc / numberOfGames);
		System.out.printf("  Scores ranged from %s to %s.\n\n", min, max);
		System.out.printf("  Perfect Games: %s\n", perfects);
		float ratio = zeroes;
		ratio = ratio/numberOfGames*100;
		System.out.printf("  FAILS: %s (%f%%)\n", zeroes, ratio);
	}
}