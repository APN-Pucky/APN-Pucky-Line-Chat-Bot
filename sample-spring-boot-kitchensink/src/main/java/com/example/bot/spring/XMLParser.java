package com.example.bot.spring;

import com.example.bot.spring.Fusion;
import com.example.bot.spring.Card;
import com.example.bot.spring.Card.CardInstance;
import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


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
        			ids[0] = id;
                	for (int j = 0; j < upList.getLength(); j++) {
                		Node upNode = upList.item(j);
                		if (upNode.getNodeType() == Node.ELEMENT_NODE) {
                			Element uElement = (Element) upNode;
                			ids[j+1] = Integer.parseInt(uElement.getElementsByTagName("card_id").item(0).getTextContent());
                			if(ids[j+1]>max_id)max_id = ids[j+1];
                		}
                	}
                	
                	distinct_cards[cur] = new Card(ids,name,rarity,fusion_level, Data.getFusionByID(ids[0]).getMaterials(),fort_type,set);
        			cur++;
        		}
        	}
        }}catch(Exception e){e.printStackTrace();}
    	Card[] all_cards = new Card[max_id+1];
    	for(Card c : distinct_cards)
    	{
    		for(int it_id : c.getIDs())
    		{
    			all_cards[it_id] = c;
    		}
    	}
        return new Pair<Card[],Card[]>(distinct_cards,all_cards);
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
	
}
