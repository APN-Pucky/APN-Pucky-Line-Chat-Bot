package com.example.bot.spring;


public class SkillSpec {
	final String id;
	final int x;
	final String y;
	final int n;
	final int c;
	final String s;
	final String s2;
	final boolean all;
	final String trigger;
	final int card_id;

	public String toString()
	{
		return (!trigger.equals("activate")?"On "+StringUtil.capitalizeOnlyFirstLetters(trigger.toString())+ ": ":"")
				+ StringUtil.capitalizeOnlyFirstLetters(id.toString())
				+ (all?" All":"") + " "
				+ (y.equals("allfactions")?"":StringUtil.capitalizeOnlyFirstLetters(y.toString())+ " ")
				+ (x>0?x+ " ":"")
				+ (n>0?n+ " ":"")
				+ (c>0?"every "+c+ " ":"")
				+ ((!s.equals("no_skill")&&!s2.equals("no_skill"))?StringUtil.capitalizeOnlyFirstLetters(s.toString())+ " to " + StringUtil.capitalizeOnlyFirstLetters(s2.toString()):"")
				+ ((card_id>0)?(" " + Data.getNameAndLevelByID(card_id)):"")
				;
	}

	public SkillSpec(String id, int x, String y, int n, int c, String s, String s2, boolean all,int card_id, String trigger) {
		this.x = x;
		this.y = y;
		this.c = c;
		this.n = n;
		this.all = all;
		this.id = id;
		this.s = s;
		this.s2 = s2;
		this.card_id = card_id;
		this.trigger = trigger;
	}
/*
	public static enum Name {
		// Placeholder for no-skill:
		no_skill,

		// Activation (harmful):
		enfeeble, jam, mortar, siege, strike, sunder, weaken,

		// Activation (helpful):
		enhance, evolve, heal, mend, overload, protect, rally, fortify, enrage, entrap, rush,

		// Activation (unclassified/polymorphic):
		mimic,

		// Defensive:
		armor, avenge, scavenge, corrosive, counter, evade, subdue, absorb, flying, payback, revenge, tribute, refresh, wall, barrier,

		// Combat-Modifier:
		coalition, legion, pierce, rupture, swipe, drain, venom, hunt,

		// Damage-Dependent:
		berserk, inhibit, sabotage, leech, poison,

		// Triggered:
		allegiance, flurry, valor, stasis, summon, bravery;

		public static
	};

	public static enum Trigger {
		activate, play, attacked, death,
	};*/
}
