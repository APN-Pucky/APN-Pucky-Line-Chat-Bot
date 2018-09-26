package de.neuwirthinformatik.Alexander.APNPucky;


public class Deck 
{
	public enum Type{BOTH,OFF,DEF; public boolean off(){return this == OFF || this == BOTH;}boolean def(){return this == DEF || this == BOTH;}}
	
	private int commander_id;
	private int dominion_id;
	
	private int[] cards;
	
	public Deck(int com,int dom,int[]cards)
	{
		commander_id = com;
		dominion_id = dom;
		this.cards = cards;		
	}
	
	public int[] toIDArray()
	{
		int[] tmp = new int[cards.length+2];
		tmp[0] = commander_id;
		tmp[1] = dominion_id;
		for(int i = 0; i < cards.length;i++)
		{
			tmp[i+2] = cards[i];
		}
		return tmp;
	}
	
	public int[] getOffenseDeck()
	{
		return fixDominionDeck();
		
	}
	
	
	
	public int[] fixDominionDeck()
	{
		int[] ret = toIDArray();
		if(!isANC())
		{
			/*int[] n_deck = new int[ret.length+1];
			n_deck[0] = ret[0];//com
			n_deck[1] = ret[0];//dom
			for(int i = 1;i<ret.length;i++)//cards
			{
				n_deck[i+1] = ret[i];
			}
			ret = n_deck;*/
			ret[1] = ret[0];
		}
		return ret;
	}
	
	public int[] getDefenseDeck()
	{	
		return fixDominionDeck();
	}
	/**
	 * Dominion Alpha, Commander or nexus
	 * @return
	 */
	private boolean isANC()
	{
		return GlobalData.isANC(dominion_id,commander_id);
	}
	
	public int getCommander(){return commander_id;};
	public int getDominion(){return dominion_id;};
	
	public String toString()
	{
		return GlobalData.getDeckString(toIDArray());
	}
}
