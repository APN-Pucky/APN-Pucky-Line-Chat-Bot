package com.example.bot.spring;

import com.example.bot.spring.Card.CardType;
import com.example.bot.spring.Card.CardCategory;
import com.example.bot.spring.Card.CardInstance;
import com.example.bot.spring.Card;
import com.example.bot.spring.Deck;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.HashMap;


public class Data
{
	//end crypto
	public static String line_seperator = System.getProperty("line.seperator");
	public static String file_seperator = File.separator;
	public static String xml_time = "";
	static XMLParser xml = null;
	public static Card[] distinct_cards;
	public static Card[] all_cards;
	public static Fusion[] fusions = new Fusion[] {};
	public static Mission[] missions = new Mission[]{};
	public static int[][][][] levels = new int[][][][]{};
	public static int[][][] style_borders;
	public static int[][] frame_borders;
  public static HashMap<String,int[]> icon_borders;
  public static HashMap<String,int[]> skill_borders;


	public static void init() {
		xml= new XMLParser();
		fusions = xml.loadFusions();
		missions = xml.loadMissions();
		levels = xml.loadLevels();
		style_borders = xml.loadStyle();
		frame_borders = xml.laodFrame();
		icon_borders = xml.loadIcon();
		skill_borders = xml.loadSkill();

		Pair<Card[],Card[]> p = xml.loadCards();
		distinct_cards = p.t;
		all_cards = p.u;
		xml = null;
		xml_time = Task.time();
		//for(Fusion f : fusions)all_cards[f.getID()].setMaterials(f.getMaterials());
	}


	public static int[] getIDsFromCardInstances(CardInstance[] cis)
	{
		int[] arr = new int[cis.length];
		for(int i =0; i < cis.length;i++) arr[i] = cis[i].getID();
		return arr;
	}

	public static CardInstance[] getCardInstancesFromIDs(int[] ids)
	{
		CardInstance[] cis = new CardInstance[ids.length];
		if(ids.length==0)return cis;
		cis[0] = new CardInstance(ids[0]);
		for(int i = 1 ; i < ids.length;i++)
		{
			if(ids[i] == cis[i-1].getID())
			{
				cis[i] = cis[i-1].clone(); //TODO maybe no clone needed
			}
			else
			{
				cis[i] = new CardInstance(ids[i]);
			}
		}
		return cis;
	}

	public static String getNameByID(int s_id)
	{
		return getCardByID(s_id).getName();
	}

	public static String getNameAndLevelByID(CardInstance ci)
	{
		return ci.toString();
	}

	public static String getNameAndLevelByID(int s_id)
	{
		Card c = getCardByID(s_id);
		if(c == null){System.out.println("WTFFFFFFFFFFF!!!!!!!! --1--  " + s_id); return "Dominion Shard";}
		return c.getName() + "-" +  getCardLevel(s_id);
	}

	public static CardInstance getCardInstanceByNameAndLevel(String name)
	{
		Pattern p = Pattern.compile("\\s*-\\d+");

		Matcher m = p.matcher(name);
		//m_SIM.find();m_SIM.group();m_SIM.find();m_SIM.group();m_SIM.find();
		//int sim = Integer.parseInt(m_SIM.group());

		if(!m.find())
		{
			Card c = getCardByName(name);
			if(c == null)
			{
				System.out.println("WTFFFFFFFFFFF!!!!!!!! --2--  " + name);
				return null;
			}
			return new CardInstance(c.getIDs()[c.getIDs().length-1],c);
		}
		String[] split = name.split("-");
		int level =1;
		try{level = Integer.parseInt(split[split.length-1].trim());}catch(Exception e){e.printStackTrace(); System.out.println(name);}
		String n = "";
		for(int i =0; i < split.length-1;i++)
		{
			n += split[i];
			if(i!=split.length-2)n+="-";
		}
		Card c = getCardByName(n);
		if(c == null)System.out.println("WTFFFFFFFFFFF!!!!!!!! --3--  " + name);
		return new CardInstance(c.getIDs()[level-1],c);
	}

	@Deprecated
	public static int getIDByNameAndLevel(String name)
	{
		Pattern p = Pattern.compile("\\s*-\\d+");

		Matcher m = p.matcher(name);
		//m_SIM.find();m_SIM.group();m_SIM.find();m_SIM.group();m_SIM.find();
		//int sim = Integer.parseInt(m_SIM.group());

		if(!m.find())
		{
			Card c = getCardByName(name);
			if(c == null)System.out.println("WTFFFFFFFFFFF!!!!!!!! --2--  " + name);
			return c.getIDs()[c.getIDs().length-1];
		}
		String[] split = name.split("-");
		int level =1;
		try{level = Integer.parseInt(split[split.length-1].trim());}catch(Exception e){e.printStackTrace(); System.out.println(name);}
		String n = "";
		for(int i =0; i < split.length-1;i++)
		{
			n += split[i];
			if(i!=split.length-2)n+="-";
		}
		if(getCardByName(n) == null)System.out.println("WTFFFFFFFFFFF!!!!!!!! --3--  " + name);
		return getCardByName(n).getIDs()[level-1];
	}

	@Deprecated
	public static int getIDByName(String s_name)
	{
		return getCardByName(s_name).getIDs()[0];
	}


	public static Card getCardByName(String s_name)
	{
		for(Card c : distinct_cards)
		{
			if(StringUtil.equalsIgnoreSpecial(c.getName(),s_name.trim()))return c;
		}
		return Card.NULL;
	}
	@Deprecated
	public static Card getCardByNameAndRarity(String s_name, int rarity)
	{
		for(Card c : distinct_cards)
		{
			if(StringUtil.equalsIgnoreSpecial(c.getName(),s_name.trim()))return c;
		}
		return null;
	}

		public static String factionToString(int faction)
		{
			if(faction ==1) return "imperial";
			else if(faction==2) return "raider";
			else if(faction ==3) return "bloodthirsty";
			else if(faction ==4) return "xeno";
			else if(faction ==5) return "righteous";
			else if(faction ==6) return "progenitor";
			return "allfaction"; //error
		}
		public static String fusionToString(int fusion)
		{
			if(fusion ==0) return "Single";
			else if(fusion ==1) return "Dual";
			else if(fusion ==2) return "Quad";
			return "Unknkown Fusion";
		}
		public static String rarityToString(int rarity)
		{
			if(rarity ==1) return "Common";
			else if(rarity ==2) return "Rare";
			else if(rarity ==3) return "Epic";
			else if(rarity ==4) return "Legendary";
			else if(rarity ==5) return "Vindicator";
			else if(rarity ==6) return "Mythic";
			return "Unknkown Rarity";
		}
	public static Card getCardByID(int s_id)
	{
		return all_cards[s_id];
		/*for(Card c : cards)
		{
			for(int id : c.getIDs())
			{
				if(id == s_id)return c;
			}
		}
		return null;*/
	}

	public static int getCardLevel(CardInstance ci)
	{
		return ci.getLevel();
	}

	@Deprecated
	public static int getCardLevel(int id)
	{
		int[] ids  = getCardByID(id).getIDs();
		for(int i = 0; i <ids.length;i++)
		{
			if(ids[i]==id)return i+1;
		}
		return 0;
	}

	public static Mission getMissionByID(int id)
	{
		for(Mission c : missions)
		{
			if(c != null && id == c.getID())return c;

		}
		return null;
	}

	public static Mission getMissionByName(String name)
	{
		for(Mission c : missions)
		{
			if(c != null && StringUtil.equalsIgnoreSpecial(c.getName(),name))return c;
		}
		return null;
	}

	public static Fusion getFusionByID(int id)
	{
		for(Fusion c : fusions)
		{
			if(c != null && id == c.getID())return c;

		}
		return Fusion.NULL;
	}

	public static Card[] getCardArrayFromIDArray(int[] ids)
	{
		Card[] ret = new Card[ids.length];
		for(int i = 0; i <ids.length;i++)
		{
			ret[i] = getCardByID(ids[i]);
		}
		return ret;
	}

	public static String getDeckString(CardInstance[] deck)
	{
		if(deck == null)return null;
		String decks = "";
		for(CardInstance i : deck)
		{
			if(i!=CardInstance.NULL)decks += i + ", ";
		}
		return decks;
	}

	public static String getDeckString(int[] deck)
	{
		if(deck == null)return null;
		String decks = "";
		for(int i : deck)
		{
			if(i!=0)decks += Data.getNameAndLevelByID(i) + ", ";
		}
		return decks;
	}

	public static String getInvString(int[] ids)
	{
		java.util.Arrays.sort(ids); //TODO pre-check is sorted
		int count = 0;
		String inv = "";
		for(int j=0;j< ids.length;)
		{
			count = Data.getCount(ids,ids[j]);
			if(ids[j]!=0 )inv += Data.getNameAndLevelByID(ids[j]) + "#" + count + "\n";
			j+=count;
		}
		return inv;
	}

	public static String removeHash(String s)
	{
		String[] cards = s.split(",");
		ArrayList<String> al = new ArrayList<String>();
		for(String c : cards){
			//System.out.println(c);
			int number = 1;
			if(c.contains("#"))
			{
				Pattern p = Pattern.compile("#\\d+");
				Matcher m = p.matcher(c);
				m.find();
				String tmp = m.group();
				tmp = tmp.substring(1);
				number = Integer.parseInt(tmp);
				c = c.split("#")[0];
			}
			for(int i =0;i<number;i++)
			{
				al.add(c);
			}
		}
		String deck = "";
		for(String c : al)
		{
			deck += c + ", ";
		}
		return deck;
	}

	public static Deck constructDeck(int[] deck)
	{
		int com = deck[0];
		int dom = deck[1];

		int[] ids = new int[deck.length-2];
		for(int i =2; i < deck.length;i++)
		{
			ids[i-2] =  deck[i];
		};
		return new Deck(com,dom,ids);
	}

	public static int[] constructDeckArray(String deck)
	{
		return constructDeck(deck).toIDArray();
	}

	public static Deck constructDeck(String deck)
	{
		deck = Data.removeHash(deck);
		String[] ss = deck.split(", *");
		int com = Data.getIDByNameAndLevel(ss[0]);
		int dom = Data.getIDByNameAndLevel(ss[1]);
		int[] ids = new int[ss.length-2];
		for(int i =2; i < ss.length;i++)
		{
			ids[i-2] =  Data.getIDByNameAndLevel(ss[i]);
		};
		return new Deck(com,dom,ids);
	}

	public static Card[] constructCardArray(String deck)
	{
		deck = Data.removeHash(deck);
		String[] ss = deck.split(", *");
		Card[] ids = new Card[ss.length];
		for(int i =0; i < ss.length;i++)
		{
			ids[i] =  Data.getCardByName(ss[i]);
		};
		return ids;
	}

	public static boolean isANC(int dominion_id ,int commander_id)
	{
		return isDominion(dominion_id) || dominion_id == commander_id;
	}
	@Deprecated
	public static boolean isANOld(int dominion_id)
	{
		Card d = Data.getCardByID(dominion_id);
		if(d == null)return false;
		return d.getName().contains("Alpha") || d.getName().contains("Nexus");
	}

	public static boolean isDominion(int dom_id)
	{
		Card d = Data.getCardByID(dom_id);
		if(d==null){System.out.println("NULL == DOM ||| UPDATE XMLS?!?! ||| "+ dom_id+ "DATA");return false;}
		boolean ret = d.category == CardCategory.DOMINION;
		//if(TUM.settings.ASSERT && ret != isANOld(dom_id))throw new NullPointerException("Unsure if dominion or not " + dom_id);
		return ret;
	}

	public static boolean isCommander(int cmd)
	{
		Card d = Data.getCardByID(cmd);
		boolean ret = d.type == CardType.COMMANDER;
		return ret;
	}

	public static boolean isFortress(int fort)
	{
		Card d = Data.getCardByID(fort);
		if(d==null){System.out.println("NULL == DOM ||| UPDATE XMLS?!?! ||| "+ fort+ "DATA");return false;}
		boolean ret = d.category == CardCategory.FORTRESS_DEFENSE || d.category == CardCategory.FORTRESS_SIEGE|| d.category == CardCategory.FORTRESS_CONQUEST;
		return ret;
	}

	/*public static int getNullCount(Bot[] bs)
	{
		int c = 0;
		for(Bot i : bs)if(i == null)c++;
		return c;
	}

	/*public static <T> int getCount(T[] ids, T id)
	{
		int c = 0;
		for(T i : ids)if(i.equals(id))c++;
		return c;
	}*/

	public static int getCount(int[] ids, int id)
	{
		int c = 0;
		for(int i : ids)if(i == id)c++;
		return c;
	}

	public static int getCount(CardInstance[] ids, CardInstance id)
	{
		int c = 0;
		for(CardInstance i : ids)if(i.equals(id))c++;
		return c;
	}

	public static int getCount(Object[] ids, Object id) //equals T method
	{
		int c = 0;
		for(Object i : ids)if(i.equals(id))c++;
		return c;
	}

	public static boolean contains(CardInstance[] ids, CardInstance id)
	{
		for(CardInstance i : ids)if(i.equals(id))return true;
		return false;
	}

	public static boolean contains(int[] ids, int id)
	{
		for(int i : ids)if(i == id)return true;
		return false;
	}

	public static CardInstance[] concat(CardInstance[] a, CardInstance[] b)
	{
		CardInstance[] ret = new CardInstance[a.length+b.length];
		System.arraycopy(a, 0, ret, 0, a.length);
		System.arraycopy(b, 0,ret, a.length, b.length);
		return ret;
	}

	public static int[] concat(int[] a, int[] b)
	{
		int[] ret = new int[a.length+b.length];
		System.arraycopy(a, 0, ret, 0, a.length);
		System.arraycopy(b, 0,ret, a.length, b.length);
		return ret;
	}

	public static boolean hasDuplicates(int [] x ) {
	    Set<Integer> set = new HashSet<Integer>();
	    for ( int i = 0; i < x.length; ++i ) {
	        if ( set.contains( x[i])) {
	            return true;
	        }
	        else {
	            set.add(x[i]);
	        }
	    }
	    return false;
	}

	public static String getCookie(String name)
	{
		return getCookie(name,"cookies"+"/");
		/*//System.out.println("Loading Cookie: " + name);
		String text = "";
		try
		{
			BufferedReader brTest = new BufferedReader(new FileReader(TUM.settings.cookies_folder()+"/cookie_" +name));
			text = brTest .readLine();
		    brTest.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Loaded Cookie: " + name);

		text = text.replaceAll("&", ";");
	    return text;*/
	}

	public static String getCookie(String name,String folder)
	{
		//System.out.println("Loading Cookie: " + name);
		String text = "";
		try
		{
			BufferedReader brTest = new BufferedReader(new FileReader(folder+"cookie_" +name));
			text = brTest .readLine();
		    brTest.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Loaded Cookie: " + name);
		text = text.replaceAll("&", ";");
	    return text;
	}

	public static String[] getBotList()
	{
		File folder = new File("cookies");

		File[] listOfFiles = folder.listFiles();
		String[] ret = new String[listOfFiles.length];

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				ret[i] = listOfFiles[i].getName().split("cookie_")[1];
			}
		}
		return ret;
	}

	/*public static Bot getBotByName(String n)
	{
		for(Bot b : Data.bots)
		{
			if(b !=null && b.getName().equals(n))return b;
		}
		return null;
	}*/

	public static String[] getBotList(String sfolder)
	{
		File folder = new File(sfolder);

		File[] listOfFiles = folder.listFiles();
		String[] ret = new String[listOfFiles.length];

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				ret[i] = listOfFiles[i].getName().split("cookie_")[1];
			}
		}
		return ret;
	}

	public static void deleteFile(String file)
	{
		File f = new File(file);
		if(f.exists())f.delete();
	}

	public static void createFile(String file)
	{
		File f = new File(file);
		if(!f.exists())
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public static String readFile(String path)
	{
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void copyFile(String src, String dst)
	{
		try {
			Files.copy(new File(src).toPath(), new File(dst).toPath(),StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void appendLine(String file, String line)
	{
		File f = new File(file);
		if(!f.exists()){
			if(f.getParentFile()!= null)f.getParentFile().mkdirs();
			try {
			f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		BufferedWriter output;
		try {
			output = new BufferedWriter(new FileWriter(file, true));
			output.append(line + System.getProperty("line.seperator"));
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void appendLines(String file, String[] lines)
	{
		File f = new File(file);
		if(!f.exists()){
			if(f.getParentFile()!= null)f.getParentFile().mkdirs();
			try {
			f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		BufferedWriter output;
		try {
			output = new BufferedWriter(new FileWriter(file, true));
			for(String line : lines)
				output.append(line + "\n");
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int[] removeDuplicates(int[] arr)
	{
		return Arrays.stream(arr).distinct().toArray();
	}

	public static int[] removeDuplicates(int t,int[] arr)
	{
		int num  = Data.getCount(arr,t);
		if(num==0)return arr;
		int[] n = new int[arr.length+1-num];
		boolean added = false;
		int j = 0;
		for(int i =0;i < arr.length;i++)
		{
			if(arr[i]==t)
			{
				if(!added)
				{
					added = true;
					n[j] = arr[i];
					j++;
				}
			}
			else
			{
				n[j] = arr[i];
				j++;
			}

		}
		return n;
	}


	/*public static boolean isRareCommander(Card c)
	{
		return c.getName().equals("Ascaris") ||c.getName().equals("Malika") ||c.getName().equals("Terrogor") ||c.getName().equals("Maion") ||c.getName().equals("Cyrus");
	}*/


	public static int getSalvageValue(CardInstance ci)
	{
		return levels[ci.getRarity()][ci.getLevel()][ci.getFusionLevel()][0];
	}

	public static int getRestoreCosts(CardInstance ci)
	{
		return levels[ci.getRarity()][1][ci.getFusionLevel()][2];
	}

	public static int getUpgradeCosts(CardInstance ci)
	{
		return levels[ci.getRarity()][ci.getLevel()][ci.getFusionLevel()][1];
	}

	public static int getSPNeededToLevelTo(CardInstance low, CardInstance high)
	{
		int sp_cost = 0;
		for(int i=low.getLevel(); i < high.getLevel();i++)
		{
			sp_cost += levels[low.getRarity()][i][low.getFusionLevel()][1];
		}
		return sp_cost;
	}

	public static int getSPNeededToMax(CardInstance ci)
	{
		int sp_cost = 0;
		for(int i=ci.getLevel(); i < ci.getIDs().length;i++)
		{
			sp_cost += levels[ci.getRarity()][i][ci.getFusionLevel()][1];
		}
		return sp_cost;
	}


	@Deprecated
	public static int getSalvageValue(int id)
	{
		return getSalvageValue(Data.getCardByID(id));
	}

	//LEVELSXML
	@Deprecated
	public static int getSalvageValue(Card c)
	{
		switch(c.getRarity()){
		case(1):return 1;
		case(2):return 5;
		case(3):return 20;
		case(4):return 40;
		case(5):return 80;
		case(6):return 160;
		default:return 360;
		}
	}

	//LEVELSXML
	@Deprecated
	public static int getSPNeededToMax(int id)
	{
		Card c = getCardByID(id);
		int level = Data.getCardLevel(id);
		switch( level)
		{
			case(1):return 275;
			case(2):return 270;
			case(3):return 255;
			case(4):return 225;
			case(5):return 150;
			default:return 0;
		}
	}
	//LEVELSXML
	@Deprecated
	public static int getSPNeededToMax(Card c) //TODO return by int id based on missinglevels
	{
		switch(c.getRarity()){
		case(1):return 15;
		case(2):return 50;
		case(3):return 275;
		case(4):return 275;
		case(5):return 275;
		case(6):return 275;
		default:return 275;
		}
	}

	/*public static Bot[] getGuildLeaders()
	{
		ArrayList<Bot> l = new ArrayList<Bot>();
		for(Bot b : bots)
		{
			if(b.isLeader())l.add(b);
		}
		return l.toArray(new Bot[]{});
	}*/

	/*public static String[] getLeadGuildNames()
	{
		Bot[] ls  = getGuildLeaders();
		String[] r = new String[ls.length];
		for(int i =0;i < ls.length;i++)
		{
			r[i] = ls[i].getGuild();
		}
		return r;
	}

	public static Bot[] getGuildOfficers()
	{
		ArrayList<Bot> l = new ArrayList<Bot>();
		for(Bot b : bots)
		{
			if(b.isLeader() || b.isOfficer())l.add(b);
		}
		return l.toArray(new Bot[]{});
	}

	public static String[] getInviteGuildNames()
	{
		Bot[] ls  = getGuildOfficers();
		//String[] r = new String[ls.length];
		List<String> al = new ArrayList<>();
		for(int i =0;i < ls.length;i++)
		{
			al.add(ls[i].getGuild());
			//r[i] = ls[i].getGuild();
		}
		Set<String> hs = new HashSet<>();
		hs.addAll(al);
		al.clear();
		al.addAll(hs);
		return hs.toArray(new String[]{});
	}

	private static File c = new File("data/customdecks.txt");
	private static File c_tmp = new File("data/customdecks_tmp.txt");

	public static void saveGauntletRaw(String gaunt_name, String deck_line)
	{
		saveGauntletRaw(gaunt_name,new String[]{deck_line});
	}

	public static void saveGauntletRaw(String gaunt_name, String[] deck_lines) //TODO better syncs
	{
		System.out.println("Queeing Gauntlet"+ gaunt_name);
		Task.start(() -> {
			synchronized(c)
			{
				System.out.println("EOQ Gauntlet"+ gaunt_name);
				try
				{
					if(!c.exists())
					{

						if(c_tmp.exists())
						{
							System.out.println("Interruption in saving deck, tring to restore it", gaunt_name+ "saveGauntletRaw");
							c_tmp.renameTo(c);
						}
						else
						{
							System.out.println("Interruption in saving deck, manual restore needed", gaunt_name+ "saveGauntletRaw");
							createFile("data/customdecks.txt"); //Does it work?
						}
					}
					BufferedReader reader = new BufferedReader(new FileReader(c));
					BufferedWriter writer = new BufferedWriter(new FileWriter(c_tmp));

					String lineToRemove = gaunt_name;
					String currentLine;

					while((currentLine = reader.readLine()) != null) {
					    // trim newline when comparing with lineToRemove
					    String trimmedLine = currentLine.trim();
					    if(trimmedLine.startsWith(lineToRemove))
					    {
					    	continue;
					    }
					    writer.write(currentLine + System.getProperty("line.separator"));
					}
					for(String w : deck_lines)
					{
						writer.write(w + System.getProperty("line.separator"));
					}
					writer.close();
					reader.close();
					c.delete();
					c_tmp.renameTo(c);
					//Thread.sleep(1000);
					System.out.println("Saved Gauntlet"+ gaunt_name);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	public static void saveGauntlet(String gaunt_name, String deck)
	{
		saveGauntlet(gaunt_name,deck,Data.constructDeckArray(deck));
	}
	public static void saveGauntlet(String gaunt_name,String deck, int[] ids)
	{
		/*if(ids[1]==ids[2] && isAN(ids[1]))
		{
			int[] tmp = ids.clone();
			tmp[2]=0;
			deck = Data.getDeckString(tmp);
		}
		if(ids[0]==ids[1])deck = deck.substring(deck.indexOf(",")+1);
		final String deck_line = gaunt_name + ": " + deck;
		saveGauntletRaw(gaunt_name,deck_line);

	}
	public static void saveGauntlet(String gaunt_name, int[] ids)
	{
		saveGauntlet(gaunt_name,Data.getDeckString(ids),ids);
	}
	public static void saveGauntlet(Bot b, int[] ids)
	{
		String gaunt_name = "gauntlet_" + b.getGuild() + "_" + b.getName();
		saveGauntlet(gaunt_name,ids);
	}

	public static void saveOwnedCards(Bot b, int[] ids)
	{
		synchronized(b.inv)
		{
			try
			{
				File o = new File("data/ownedcards_" + b.getName() + ".txt");
				File o_tmp = new File("data/ownedcards_" + b.getName() + "_tmp.txt");

				String inv = Data.getInvString(ids);

				o.delete();

				BufferedWriter writer = new BufferedWriter(new FileWriter(o_tmp));

				writer.write(inv.replace("\n", System.getProperty("line.separator")));

				writer.close();

				o_tmp.renameTo(o);
				System.out.println("Saved Ownedcards"+ b.getName());
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}*/
}
