package com.example.bot.spring;

import com.example.bot.spring.Fusion;
import com.example.bot.spring.Card;
import com.example.bot.spring.Card.CardInstance;
import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;

import java.net.URL;
import com.example.bot.spring.Card.CardInstance;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.HashMap;


public class XMLParser
{
	public static final int CARD_SECTIONS_COUNT = 16;//TODO: load from data
	private int card_count = 1;
	private int fusion_count = 0;
	private int mission_count = 0;
	private Document[] card_documents = new Document[CARD_SECTIONS_COUNT+1];
	private Document fusion_document;
	private Document mission_document;
	private Document level_document;
	private Document document;


	public XMLParser()
	{

		try{
			System.out.println("XMLParser DOWNLOAD");
			System.out.println("XMLParser Start");
			for(int i =1;i<=CARD_SECTIONS_COUNT;i++)
			{
				//File inputFile = new File("data/cards_section_"+i+".xml");
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				card_documents[i] = dBuilder.parse(new BufferedInputStream(new URL("http://mobile-dev.tyrantonline.com/assets/cards_section_"+i+".xml").openStream()));
				card_documents[i].getDocumentElement().normalize();
				NodeList nList = card_documents[i].getElementsByTagName("unit");
				card_count+=nList.getLength();
			}

			//File inputFile = new File("data/fusion_recipes_cj2.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			fusion_document = dBuilder.parse(new BufferedInputStream(new URL("http://mobile-dev.tyrantonline.com/assets/fusion_recipes_cj2.xml").openStream()));
			fusion_document.getDocumentElement().normalize();
			NodeList nList = fusion_document.getElementsByTagName("fusion_recipe");
			fusion_count+=nList.getLength();

			//inputFile = new File("data/missions.xml");
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			mission_document = dBuilder.parse(new BufferedInputStream(new URL("http://mobile-dev.tyrantonline.com/assets/missions.xml").openStream()));
			mission_document.getDocumentElement().normalize();
			nList = mission_document.getElementsByTagName("mission");
			mission_count+=nList.getLength();

			//inputFile = new File("data/levels.xml");
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			level_document = dBuilder.parse(new BufferedInputStream(new URL("http://mobile-dev.tyrantonline.com/assets/levels.xml").openStream()));
			level_document.getDocumentElement().normalize();

			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			ResourceLoader r = KitchenSinkApplication.resourceLoader;
			Resource re = r.getResource("classpath:" + "static/cardSources.xml");
			InputStream is = re.getInputStream();
			//File f= re.getFile();
			document = dBuilder.parse(is);
			document.getDocumentElement().normalize();

		}catch(Exception e){e.printStackTrace();}
		System.out.println("XMLParser Done");

	}

	public Pair<Card[],Card[]> loadCards()
	{
		System.out.println("Loading Cards");
		int max_id = 0;
		Card[] distinct_cards = new Card[card_count];
		int id,rarity,fusion_level,fort_type,set;
		String name;
		int f;

		int attack, health, cost, level;
		ArrayList<SkillSpec> skills = new ArrayList<SkillSpec>();

		int cur = 1;
		distinct_cards[0] = Card.NULL;
		try{
			for(int i =1;i<=CARD_SECTIONS_COUNT;i++) //sections
			{
				NodeList nList = card_documents[i].getElementsByTagName("unit");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						name = eElement.getElementsByTagName("name").item(0).getTextContent();
						f = Integer.parseInt(eElement.getElementsByTagName("type").item(0).getTextContent());
						id = Integer.parseInt(eElement.getElementsByTagName("id").item(0).getTextContent());
						rarity = Integer.parseInt(eElement.getElementsByTagName("rarity").item(0).getTextContent());
						if(eElement.getElementsByTagName("set").getLength() > 0)set = Integer.parseInt(eElement.getElementsByTagName("set").item(0).getTextContent());
						else
						set = 0;
						if(eElement.getElementsByTagName("fortress_type").getLength() > 0)
						fort_type = Integer.parseInt(eElement.getElementsByTagName("fortress_type").item(0).getTextContent());
						else
						fort_type = 0;
						if(eElement.getElementsByTagName("fusion_level").getLength()==0)fusion_level=0;
						else
						fusion_level = Integer.parseInt(eElement.getElementsByTagName("fusion_level").item(0).getTextContent());

						NodeList upList = eElement.getElementsByTagName("upgrade");

						int[] ids = new int[upList.getLength()+1];
						CardInstance.Info[] info = new CardInstance.Info[upList.getLength()+1];
						ids[0] = id;
						attack = 0; health = 0; level =0;cost=0;
						skills.clear();
						//System.out.println(name + " " + i);
						if(eElement.getElementsByTagName("attack").getLength()!=0)attack = eElement.getElementsByTagName("attack").item(0).getTextContent().length()>0?Integer.parseInt(eElement.getElementsByTagName("attack").item(0).getTextContent()):0;
						if(eElement.getElementsByTagName("health").getLength()!=0)health = eElement.getElementsByTagName("health").item(0).getTextContent().length()>0?Integer.parseInt(eElement.getElementsByTagName("health").item(0).getTextContent()):0;
						if(eElement.getElementsByTagName("cost").getLength()!=0)cost = eElement.getElementsByTagName("cost").item(0).getTextContent().length()>0?Integer.parseInt(eElement.getElementsByTagName("cost").item(0).getTextContent()):0;
						if(eElement.getElementsByTagName("level").getLength()!=0)level = eElement.getElementsByTagName("level").item(0).getTextContent().length()>0?Integer.parseInt(eElement.getElementsByTagName("level").item(0).getTextContent()):0;

						//System.out.println(ids[0] + " " + attack + " " + health + " " + cost + " " + level);
						NodeList skillList = eElement.getElementsByTagName("skill");
						for(int j =0;j < skillList.getLength();j++)
						{
							String skill_id = fixSkillName(((Element)skillList.item(j)).hasAttribute("id")?((Element)skillList.item(j)).getAttribute("id"):"no_skill");
							String trigger = ((Element)skillList.item(j)).hasAttribute("trigger")?((Element)skillList.item(j)).getAttribute("trigger"):"activate";
							int x = ((Element)skillList.item(j)).hasAttribute("x")?Integer.parseInt(((Element)skillList.item(j)).getAttribute("x")):0;
							int card_id = ((Element)skillList.item(j)).hasAttribute("card_id")?Integer.parseInt(((Element)skillList.item(j)).getAttribute("card_id")):0;
							String faction = ((Element)skillList.item(j)).hasAttribute("y")?Data.factionToString(Integer.parseInt(((Element)skillList.item(j)).getAttribute("y"))):"allfactions";
							int n = ((Element)skillList.item(j)).hasAttribute("n")?Integer.parseInt(((Element)skillList.item(j)).getAttribute("n")):0;
							int c = ((Element)skillList.item(j)).hasAttribute("c")?Integer.parseInt(((Element)skillList.item(j)).getAttribute("c")):0;
							String s = fixSkillName(((Element)skillList.item(j)).hasAttribute("s")?((Element)skillList.item(j)).getAttribute("s"):"no_skill");
							String s2 = fixSkillName(((Element)skillList.item(j)).hasAttribute("s2")?((Element)skillList.item(j)).getAttribute("s2"):"no_skill");
							int all = ((Element)skillList.item(j)).hasAttribute("all")?Integer.parseInt(((Element)skillList.item(j)).getAttribute("all")):0;
							for(int k = 0;k < skills.size();k++)if(skills.get(k).id.equals(skill_id))skills.remove(k); //update old skill
							skills.add(new SkillSpec(skill_id, x,faction,n,c,s,s2,all==1,card_id,trigger));
						}
						info[0] = new CardInstance.Info(attack,health,cost,level,skills.toArray(new SkillSpec[]{}));
						for (int j = 0; j < upList.getLength(); j++) {
							Node upNode = upList.item(j);
							if (upNode.getNodeType() == Node.ELEMENT_NODE) {
								Element uElement = (Element) upNode;
								ids[j+1] = Integer.parseInt(uElement.getElementsByTagName("card_id").item(0).getTextContent());
								if(ids[j+1]>max_id&& ids[j+1]<100000)max_id = ids[j+1];

								if(uElement.getElementsByTagName("attack").getLength()!=0)attack = uElement.getElementsByTagName("attack").item(0).getTextContent().length()>0?Integer.parseInt(uElement.getElementsByTagName("attack").item(0).getTextContent()):0;
								if(uElement.getElementsByTagName("health").getLength()!=0)health = uElement.getElementsByTagName("health").item(0).getTextContent().length()>0?Integer.parseInt(uElement.getElementsByTagName("health").item(0).getTextContent()):0;
								if(uElement.getElementsByTagName("cost").getLength()!=0)cost = uElement.getElementsByTagName("cost").item(0).getTextContent().length()>0?Integer.parseInt(uElement.getElementsByTagName("cost").item(0).getTextContent()):0;
								if(uElement.getElementsByTagName("level").getLength()!=0)level = uElement.getElementsByTagName("level").item(0).getTextContent().length()>0?Integer.parseInt(uElement.getElementsByTagName("level").item(0).getTextContent()):0;
								skillList = uElement.getElementsByTagName("skill");
								for(int h =0;h < skillList.getLength();h++)
								{
									//System.out.println(ids[0] + " " + attack + " " + health + " " + cost + " " + level);
									String skill_id = fixSkillName(((Element)skillList.item(h)).hasAttribute("id")?((Element)skillList.item(h)).getAttribute("id"):"no_skill");
									String trigger = ((Element)skillList.item(h)).hasAttribute("trigger")?((Element)skillList.item(h)).getAttribute("trigger"):"activate";
									int x = ((Element)skillList.item(h)).hasAttribute("x")?Integer.parseInt(((Element)skillList.item(h)).getAttribute("x")):0;
									int card_id = ((Element)skillList.item(h)).hasAttribute("card_id")?Integer.parseInt(((Element)skillList.item(h)).getAttribute("card_id")):0;
									String faction = ((Element)skillList.item(h)).hasAttribute("y")?Data.factionToString(Integer.parseInt(((Element)skillList.item(h)).getAttribute("y"))):"allfactions";
									int n = ((Element)skillList.item(h)).hasAttribute("n")?Integer.parseInt(((Element)skillList.item(h)).getAttribute("n")):0;
									int c = ((Element)skillList.item(h)).hasAttribute("c")?Integer.parseInt(((Element)skillList.item(h)).getAttribute("c")):0;
									String s = fixSkillName(((Element)skillList.item(h)).hasAttribute("s")?((Element)skillList.item(h)).getAttribute("s"):"no_skill");
									String s2 = fixSkillName(((Element)skillList.item(h)).hasAttribute("s2")?((Element)skillList.item(h)).getAttribute("s2"):"no_skill");
									int all = ((Element)skillList.item(h)).hasAttribute("all")?Integer.parseInt(((Element)skillList.item(h)).getAttribute("all")):0;
									for(int k = 0;k < skills.size();k++)if(skills.get(k).id.equals(skill_id))skills.remove(k); //update old skill
									SkillSpec ss = new SkillSpec(skill_id, x,faction,n,c,s,s2,all==1,card_id,trigger);
									//System.out.println(ss);
									//Fix wrongnames skills; armore besiege

									skills.add(ss);
								}
								info[j+1] = new CardInstance.Info(attack,health,cost,level,skills.toArray(new SkillSpec[]{}));
							}
						}

						distinct_cards[cur] = new Card(ids,name,rarity,fusion_level, Data.getFusionByID(ids[0]).getMaterials(),fort_type,set,f,info);
						cur++;
					}
				}
			}}catch(Exception e){e.printStackTrace();}
			Card[] all_cards = new Card[max_id+1];
			System.out.println("Number of cards: " + max_id);
			for(Card c : distinct_cards)
			{
				for(int it_id : c.getIDs())
				{
					if(it_id>100000)
					{
						if(StringUtil.containsIgnoreSpecial(c.name, "test") || StringUtil.containsIgnoreSpecial(c.name, "twin"))break;
						System.out.println("Fixed id for " + c.name + " [" + it_id + "]");
						it_id /=10;
					}
					all_cards[it_id] = c;
				}
			}
			return new Pair<Card[],Card[]>(distinct_cards,all_cards);
		}

		private String fixSkillName(String s)
		{
			if(s.equals("armored"))return "armor";
			if(s.equals("besiege"))return "mortar";
			return s;
		}
		public Fusion[] loadFusions()
		{
			System.out.println("Loading Fusions");
			Fusion[] fusions = new Fusion[fusion_count];
			int id;
			int[] mats;
			int cur = 0;
			NodeList nList = fusion_document.getElementsByTagName("fusion_recipe");
			try{
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						id = Integer.parseInt(eElement.getElementsByTagName("card_id").item(0).getTextContent());
						NodeList resList = eElement.getElementsByTagName("resource");
						int mats_size = 0;
						for(int j =0;j < resList.getLength();j++)
						{
							mats_size+=Integer.parseInt(((Element)resList.item(j)).getAttribute("number"));
						}
						mats = new int[mats_size];
						int mats_index=0;
						for(int j =0;j < resList.getLength();j++)
						{
							int number = Integer.parseInt(((Element)resList.item(j)).getAttribute("number"));
							for(int i = 0; i < number ; i++)
							{
								mats[mats_index] = Integer.parseInt(((Element)resList.item(j)).getAttribute("card_id"));
								mats_index++;
							}
						}
						fusions[cur] = new Fusion(id,mats);
						cur++;
					}
				}}catch(Exception e){e.printStackTrace();}
				return fusions;
			}

			public Mission[] loadMissions()
			{
				System.out.println("Loading Missions");
				Mission[] missions = new Mission[mission_count];
				int id;
				int costs;
				String name;
				int cur = 0;
				NodeList nList = mission_document.getElementsByTagName("mission");
				try{
					for (int temp = 0; temp < nList.getLength(); temp++) {
						Node nNode = nList.item(temp);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) nNode;
							id = Integer.parseInt(eElement.getElementsByTagName("id").item(0).getTextContent());
							costs = Integer.parseInt(eElement.getElementsByTagName("energy").item(0).getTextContent());
							name = eElement.getElementsByTagName("name").item(0).getTextContent();

							missions[cur] = new Mission(id,costs,name);
							cur++;
						}
					}}catch(Exception e){e.printStackTrace();}
					return missions;
				}

				public int[][][][] loadLevels()
				{
					System.out.println("Loading Levels");
					//rarity, level, fusion_level, data {salvage, sp_cost(next,level), buyback_cost}
					int[][][][] levels = new int[7][11][3][3]; // look levels.xml for max numbers
					int rarity,level,fusion_level,salvage,sp_cost, buyback_cost;
					NodeList nList = level_document.getElementsByTagName("card_level");
					try{
						for (int temp = 0; temp < nList.getLength(); temp++) {
							Node nNode = nList.item(temp);
							if (nNode.getNodeType() == Node.ELEMENT_NODE) {
								Element eElement = (Element) nNode;
								rarity = Integer.parseInt(eElement.getElementsByTagName("rarity").item(0).getTextContent());
								level = Integer.parseInt(eElement.getElementsByTagName("level").item(0).getTextContent());
								sp_cost = 0;
								if(eElement.getElementsByTagName("sp_cost").getLength() > 0)sp_cost = Integer.parseInt(eElement.getElementsByTagName("sp_cost").item(0).getTextContent());
								salvage = Integer.parseInt(eElement.getElementsByTagName("salvage").item(0).getTextContent());

								NodeList resList = eElement.getElementsByTagName("fusion_salvage");

								fusion_level =0;
								levels[rarity][level][fusion_level][0] = salvage;
								levels[rarity][level][fusion_level][1] = sp_cost;

								for(int j =0;j < resList.getLength();j++)
								{
									fusion_level = Integer.parseInt(((Element)resList.item(j)).getAttribute("level"));
									salvage = Integer.parseInt(((Element)resList.item(j)).getAttribute("salvage"));
									levels[rarity][level][fusion_level][0] = salvage;
									levels[rarity][level][fusion_level][1] = sp_cost;
								}

								resList = eElement.getElementsByTagName("buyback_cost");

								for(int j =0;j < resList.getLength();j++)
								{
									fusion_level = Integer.parseInt(((Element)resList.item(j)).getAttribute("level"));
									buyback_cost = Integer.parseInt(((Element)resList.item(j)).getAttribute("salvage"));

									levels[rarity][level][fusion_level][2] = buyback_cost;
								}

							}
						}}catch(Exception e){e.printStackTrace();}
						return levels;

					}

					public int[][][] loadStyle()
					{
						NodeList nList = document.getElementsByTagName("style");
						int[][][] style_borders = new int[7][7][4];
						for (int temp = 0; temp < nList.getLength(); temp++) {
							Node nNode = nList.item(temp);
							if (nNode.getNodeType() == Node.ELEMENT_NODE) {
								Element eElement = (Element) nNode;
								String name = eElement.getElementsByTagName("name").item(0).getTextContent();
								int faction = Integer.parseInt(eElement.getElementsByTagName("type").item(0).getTextContent());
								int rarity = Integer.parseInt(eElement.getElementsByTagName("rarity").item(0).getTextContent());
								Element n = (Element) eElement.getElementsByTagName("source").item(0);
								int x = Integer.parseInt(n.getAttribute("x"));
								int y = Integer.parseInt(n.getAttribute("y"));
								int width = Integer.parseInt(n.getAttribute("width"));
								int height = Integer.parseInt(n.getAttribute("height"));
								style_borders[faction][rarity] = new int[]{x,y,width,height};
							}
						}
						return style_borders;
					}

					public int[][] laodFrame()
					{
						NodeList nList = document.getElementsByTagName("frame");
						int[][] frame_borders = new int[4][4];
						for (int temp = 0; temp < nList.getLength(); temp++) {
							Node nNode = nList.item(temp);
							if (nNode.getNodeType() == Node.ELEMENT_NODE) {
								Element eElement = (Element) nNode;
								String name = eElement.getElementsByTagName("name").item(0).getTextContent();
								int fusion = Integer.parseInt(eElement.getElementsByTagName("fusion_level").item(0).getTextContent());
								Element n = (Element) eElement.getElementsByTagName("source").item(0);
								int x = Integer.parseInt(n.getAttribute("x"));
								int y = Integer.parseInt(n.getAttribute("y"));
								int width = Integer.parseInt(n.getAttribute("width"));
								int height = Integer.parseInt(n.getAttribute("height"));
								frame_borders[fusion] = new int[]{x,y,width,height};
							}
						}
						return frame_borders;
					}

					public HashMap<String,int[]> loadIcon()
					{
						NodeList nList = document.getElementsByTagName("icon");
						HashMap<String,int[]> icon_borders = new HashMap<String,int[]>();
						for (int temp = 0; temp < nList.getLength(); temp++) {
							Node nNode = nList.item(temp);
							if (nNode.getNodeType() == Node.ELEMENT_NODE) {
								Element eElement = (Element) nNode;
								String name = eElement.getElementsByTagName("name").item(0).getTextContent();
								//int fusion = Integer.parseInt(eElement.getElementsByTagName("fusion_level").item(0).getTextContent());
								Element n = (Element) eElement.getElementsByTagName("source").item(0);
								int x = Integer.parseInt(n.getAttribute("x"));
								int y = Integer.parseInt(n.getAttribute("y"));
								int width = Integer.parseInt(n.getAttribute("width"));
								int height = Integer.parseInt(n.getAttribute("height"));
								icon_borders.put(name,new int[]{x,y,width,height});
							}
						}
						return icon_borders;
					}
					public HashMap<String,int[]> loadSkill()
					{
						NodeList nList = document.getElementsByTagName("skillType");
						HashMap<String,int[]> skill_borders = new HashMap<String,int[]>();
						for (int temp = 0; temp < nList.getLength(); temp++) {
							Node nNode = nList.item(temp);
							if (nNode.getNodeType() == Node.ELEMENT_NODE) {
								Element eElement = (Element) nNode;
								String name = eElement.getElementsByTagName("name").item(0).getTextContent().toLowerCase();
								//int fusion = Integer.parseInt(eElement.getElementsByTagName("fusion_level").item(0).getTextContent());
								Element n = (Element) eElement.getElementsByTagName("source").item(0);
								int x = Integer.parseInt(n.getAttribute("x"));
								int y = Integer.parseInt(n.getAttribute("y"));
								int width = Integer.parseInt(n.getAttribute("width"));
								int height = Integer.parseInt(n.getAttribute("height"));
								skill_borders.put(name,new int[]{x,y,width,height});
							}
						}
						return skill_borders;
					}

				}
