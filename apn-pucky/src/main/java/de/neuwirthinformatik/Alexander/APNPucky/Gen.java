package de.neuwirthinformatik.Alexander.APNPucky;


import java.util.ArrayList;
import java.util.Random;

import de.neuwirthinformatik.Alexander.APNPucky.Card.CardInstance;


public class Gen {
	// gen
	private static final int pool_size = 50;
	private static final int generations = 10;
	private static final double mutate_percentage = 0.3;
	private static final double crossover_percentage = 0.3;
	private static final double struct_probabilty = 0.25;
	// card
	private static final double mutate_attack_percent = 0.05;
	private static final double mutate_health_percent = 0.05;
	private static final double mutate_cost_probability = 0.01;

	// skill
	private static final double mutate_x_percent = 0.05;
	private static final double mutate_n_probability = 0.01;
	private static final double mutate_c_probability = 0.01;
	private static final double mutate_all_probability = 0.01;
	private static final double mutate_y_probability = 0.01;
	private static final double mutate_trigger_probability = 0.01;

	private static Random r = new Random();

	static public void main(String[] s) throws Exception {
		GlobalData.init();
		// String web = Wget.wGet("https://www.amazon.co.uk/dp/B018GRDG5O");
		// String web = Wget.wGet("https://www.amazon.de/dp/B018GWRCV8");
		// String web =
		// Wget.wGet("https://www.amazon.com/Amazon-50-000-Coins/dp/B018HB6E80");
		/*
		 * CardInstance.Info[] is = new CardInstance.Info[5]; is[0] =
		 * GlobalData.getCardInstanceByNameAndLevel("Obsidian Overlord").getInfo();
		 * is[1] = GlobalData.getCardInstanceByNameAndLevel("Miasma Master").getInfo();
		 * is[2] =
		 * GlobalData.getCardInstanceByNameAndLevel("HyperSec Hunter").getInfo(); is[3]
		 * = GlobalData.getCardInstanceByNameAndLevel("Warp Fabricator").getInfo();
		 * is[4] = GlobalData.getCardInstanceByNameAndLevel("Dreamhaunter").getInfo();
		 * 
		 * System.out.println(is[0]); System.out.println(mutate(is[0]));
		 * System.out.println(gen(is));
		 */
		/*
			System.out.println(varby1(0,mutate_cost_probability));
		}
		System.out.println(" \n");*/
		for(int i = 0; i < 10;i++)
		{
		System.out.println(gen(i*i));}
		System.exit(0);
		// System.out.println(GlobalData.getCardInstanceByNameAndLevel("Obsidian
		// Overlord").description());
	}
	public static String gen(int seedr) {
		ArrayList<Card> printed = new ArrayList<Card>();
		int number = pool_size;
		CardInstance.Info[] is = new CardInstance.Info[number];
		int offset = GlobalData.all_cards.length;
		for (int i = 1; i < GlobalData.all_cards.length && number > 0; i++) {
			Card c = GlobalData.all_cards[offset - i];
			if (c != null && c.fusion_level == 2 && !printed.contains(c)
					&& !c.getName().toLowerCase().startsWith("test")
					&& !c.getName().toLowerCase().startsWith("revolt ranger")
					&& !c.getName().toLowerCase().startsWith("cephalodjinn")) {
				printed.add(c);
				number--;
				is[number] = GlobalData.getCardInstanceById(c.getHighestID()).getInfo();
			}
		}
		r.setSeed(System.currentTimeMillis()+seedr);
		int i = 1;
		String msg = "";
		String faction = "allfaction";
		while (faction.equals("allfaction"))
			faction = GlobalData.factionToString(r.nextInt(7));
		boolean summon;
		gen(is);
		do {
			summon = false;
			CardInstance.Info t = is[i-1];
			String desc = t.description();
			String type;
			if (couldBeStruct(t) && r.nextDouble() < struct_probabilty) {
				t = new CardInstance.Info(0, t.getHealth(), t.getCost(), t.getLevel(), t.getSkills());
				desc = t.description();
				type = "Structure";
			} else {
				type= "Assault";
			}
			for (SkillSpec ss : t.getSkills()) {
				if (!ss.getY().equals("allfactions")) {
					faction = ss.getY();
				}
				if (ss.getId().equals("summon")) {
					summon = true;
					desc =desc.replace(GlobalData.getNameAndLevelByID(ss.getCard_id()), "Gen #" + (i + 1));
				}
			}
			msg += "Gen #" + i + "\n";
			msg += StringUtil.capitalizeOnlyFirstLetters(faction) + " ";
			
			msg += type;
			msg += "\n";
			msg += desc;
			msg += "\n\n";
			i++;
		} while (summon && i < is.length);
		return StringUtil.removeLastCharacter(msg, 2);
	}

	public static String gen() {
		return gen(0);
	}

	private static CardInstance.Info gen(CardInstance.Info[] is) {
		for (int i = 0; i < generations * is.length; i++) {
			int i1 = r.nextInt(is.length);
			int i2 = r.nextInt(is.length);
			int i3 = r.nextInt(is.length);
			is[i1] = mutate(is[i1]);
			CardInstance.Info tmp = crossover(is[i2], is[i3]);
			is[i2] = crossover(is[i2], is[i3]);
			is[i3] = tmp;
		}
		return is[r.nextInt(is.length)];
	}

	private static boolean check(CardInstance.Info i) {
		int errr = checkI(i);
		//System.out.println(errr);
		if (errr < 0)
			return false;
		return true;
	}

	private static int checkI(CardInstance.Info i) {
		boolean wall = false;
		boolean flurry = false;
		for (int j = 0; j < i.getSkills().length; j++) {
			if (i.getSkills()[j].getX() == 0 && !i.getSkills()[j].getId().equals("wall")
					&& !i.getSkills()[j].getId().equals("jam") && !i.getSkills()[j].getId().equals("summon")
					&& !i.getSkills()[j].getId().equals("flurry")&& !i.getSkills()[j].getId().equals("overload"))
				return -1;
			if (i.getSkills()[j].isAll()
					&& (!couldBeTrigger(i.getSkills()[j]) || i.getSkills()[j].getId().equals("mimic")))
				return -2;
			if (!i.getSkills()[j].getTrigger().equals("activate") && (!couldBeTrigger(i.getSkills()[j]) || i.getSkills()[j].getC()>0))
				return -3;

			if (i.getSkills()[j].getId().equals("wall"))
				wall = true;
			if (i.getSkills()[j].getId().equals("flurry"))
				flurry = true;
			for (int k = 0; k < i.getSkills().length; k++) {
				if (k != j && i.getSkills()[j].getId().equals(i.getSkills()[k].getId()))
					return -4;
			}
		}
		if (flurry) {
			
			for (SkillSpec s : i.getSkills()) {
				if (s.getId().equals("jam"))
					return -6;
			}
		}
		if (wall) {
			if (i.getAttack() != 0)
				return -8;
			if (!couldBeStruct(i))
				return -7;
		}
		return 0;
	}

	private static boolean couldBeTrigger(SkillSpec s) {
		return (s.getId().equals("enfeeble") || s.getId().equals("strike") || s.getId().equals("mortar")
				|| s.getId().equals("siege") || s.getId().equals("sunder") || s.getId().equals("weaken")
				|| s.getId().equals("overload") || s.getId().equals("protect") || s.getId().equals("rally")
				|| s.getId().equals("entrap") || s.getId().equals("jam") || s.getId().equals("mimic")
				|| s.getId().equals("enrage") || s.getId().equals("rush") || s.getId().equals("heal")
				|| s.getId().equals("mend") || s.getId().equals("fortify") || s.getId().equals("summon")
				|| s.getId().equals("enhance") || s.getId().equals("evolve"));
	}

	private static boolean couldBeStruct(CardInstance.Info i) {
		for (SkillSpec s : i.getSkills()) {
			if (s.getId().equals("armor"))
				return false;
			if (s.getId().equals("fortify"))
				return false;
			if (s.getId().equals("mend"))
				return false;
			if (s.getId().equals("avenge"))
				return false;
			if (s.getId().equals("scavenge"))
				return false;
			if (s.getId().equals("payback"))
				return false;
			if (s.getId().equals("revenge"))
				return false;
			if (s.getId().equals("tribute"))
				return false;
			if (s.getId().equals("coalition"))
				return false;
			if (s.getId().equals("legion"))
				return false;
			if (s.getId().equals("pierce"))
				return false;
			if (s.getId().equals("rupture"))
				return false;
			if (s.getId().equals("swipe"))
				return false;
			if (s.getId().equals("drain"))
				return false;
			if (s.getId().equals("venom"))
				return false;
			if (s.getId().equals("hunt"))
				return false;
			if (s.getId().equals("mark"))
				return false;
			if (s.getId().equals("berserk"))
				return false;
			if (s.getId().equals("inhibit"))
				return false;
			if (s.getId().equals("sabotage"))
				return false;
			if (s.getId().equals("leech"))
				return false;
			if (s.getId().equals("poison"))
				return false;
			if (s.getId().equals("allegiance"))
				return false;
			if (s.getId().equals("valor"))
				return false;
			if (s.getId().equals("bravery"))
				return false;
		}
		return true;
	}

	public static CardInstance.Info crossover(CardInstance.Info i1, CardInstance.Info i2) {
		int attack = cross(i1.getAttack(), i2.getAttack());
		int health = cross(i1.getHealth(), i2.getHealth());
		int cost = cross(i1.getCost(), i2.getCost());
		int level = cross(i1.getLevel(), i2.getLevel());

		SkillSpec[] skills = new SkillSpec[Math.max(i1.getSkills().length, i2.getSkills().length)];
		int gid = 0;
		for (SkillSpec s1 : i1.getSkills()) {
			for (SkillSpec s2 : i2.getSkills()) {
				if (s1.getId().equals(s2.getId())) {
					skills[gid] = cross(s1, s2);
					gid++;
				}
			}
		}

		while (gid < skills.length) {
			SkillSpec test = cross(i1.getSkills()[r.nextInt(i1.getSkills().length)],
					i2.getSkills()[r.nextInt(i2.getSkills().length)]);
			boolean con = false;
			for (int i = gid - 1; i >= 0; i--) {
				if (test.getId().equals(skills[i].getId())) {
					con = true;
				}
			}
			if (!con) {
				skills[gid] = test;
				gid++;
			}
		}
		CardInstance.Info tmp = new CardInstance.Info(attack, health, cost, level, skills);
		//System.out.println("Check:" + tmp + " = " + i1 + " + " + i2);
		if (!check(tmp)) {
			//System.out.println("check");
			return crossover(i1, i2);
		}
		return tmp;
	}

	private static SkillSpec cross(SkillSpec a, SkillSpec b) {
		if (a.getId().equals(b.getId())) {
			int x = cross(a.getX(), b.getX());
			int n = cross(a.getN(), b.getN());
			int c = cross(a.getC(), b.getC());
			String y = cross(a.getY(), b.getY());
			boolean all = cross(a.isAll(), b.isAll());
			String trigger = cross(a.getTrigger(), b.getTrigger());
			return new SkillSpec(a.getId(), x, y, n, c, a.getS(), a.getS2(), all, a.getCard_id(), trigger);
		} else {
			return new SkillSpec[] { a, b }[r.nextInt(2)];
		}
	}

	private static String cross(String a, String b) {
		return new String[] { a, b }[r.nextInt(2)];
	}

	private static boolean cross(boolean a, boolean b) {
		return new boolean[] { a, b }[r.nextInt(2)];
	}

	private static int cross(int a, int b) {
		return new int[] { a, b, (a + b) / 2, (a + b+1) / 2}[r.nextInt(4)];
	}

	public static CardInstance.Info mutate(CardInstance.Info i) {
		int attack = (int) (i.getAttack() + r.nextInt(2 * (1 + (int) (i.getAttack() * mutate_attack_percent)))
				- i.getAttack() * mutate_attack_percent);
		int health = (int) (i.getHealth() + r.nextInt(2 * (1 + (int) (i.getHealth() * mutate_health_percent)))
				- i.getHealth() * mutate_health_percent);
		int cost = r.nextDouble() < mutate_cost_probability ? i.getCost() + r.nextInt(3) - 1 : i.getCost();
		int level = i.getLevel();
		if (cost < 0)
			cost = 0;
		if(cost >=5)
			cost = 4;
		if (health <= 0)
			health = 1;
		if(attack <0)
			attack = 0;
		SkillSpec[] skills = new SkillSpec[i.getSkills().length];
		for (int j = 0; j < skills.length; j++) {
			skills[j] = mutate(i.getSkills()[j]);
		}
		CardInstance.Info t= new CardInstance.Info(attack, health, cost, level, skills);
		//System.out.println("Check:" + t + " = " + i);
		if(!check(t))
			return mutate(i);
		return t;
	}

	public static SkillSpec mutate(SkillSpec s) {
		int x = varby(s.getX(), mutate_x_percent);
		int n = varby1(s.getN(), mutate_n_probability);
		int c = varby1(s.getC(), mutate_c_probability);
		boolean all = varbool(s.isAll(), mutate_all_probability);
		String y = varfaction(s.getY(), mutate_y_probability);
		String trigger = vartrigger(s.getTrigger(), mutate_trigger_probability);
		int card_id = s.getCard_id() > 0 ? 25293 : 0;


		return new SkillSpec(s.getId(), x, y, n, c, s.getS(), s.getS2(), all, card_id, trigger);
	}

	private static int varby(int value, double mutate_percent) {
		if (value == 0)
			return 0;
		return (int) (value + r.nextInt(2 * (1 + (int) (value * mutate_percent))) - value * mutate_percent);
	}

	private static int varby1(int value, double mutate_probability) {
		if (r.nextDouble() > mutate_probability)
			return value;
		if (value == 0)
			return 0;
		int posi = value - 1 + r.nextInt(3);
		if (value == 0)
			posi = 1;// don't make values Null
		return posi;
	}

	private static boolean varbool(boolean value, double mutate_probability) {
		if (r.nextDouble() > mutate_probability)
			return value;
		return !value;
	}

	private static String varfaction(String value, double mutate_probability) {
		if (value.equals("allfactions"))
			return value;
		if (r.nextDouble() > mutate_probability)
			return value;
		return GlobalData.factionToString(r.nextInt(7));
	}

	private static String vartrigger(String value, double mutate_probability) {
		if (r.nextDouble() > mutate_probability)
			return value;
		return new String[] { "activate", "play", "death", "attacked" }[r.nextInt(4)];
	}
}
