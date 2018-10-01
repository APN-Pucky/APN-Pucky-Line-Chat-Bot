package de.neuwirthinformatik.Alexander.APNPucky;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.message.TextMessageContent;

import lombok.Getter;
import lombok.NonNull;


public class APNMessageHandler 
{
	private final LineMessagingClient lmc;
	@Getter private final String replyToken;
	@Getter private final Event event;
	@Getter private final TextMessageContent content;
	@Getter @NonNull private final String message;
	@Getter @NonNull private final String[] args;
	@Getter @NonNull private final String[] cargs;

	private static final int HM_USER_NUMBER_SAVE = 6;
	@Getter @NonNull private final static HashMap<String,String[]> hm_userid = new HashMap<String, String[]>();
	
	public static final String[][] alias = new String[][] { { "materials", "mats", "build", "-m", "-b" }, {"insult", "destroy", "shut", "dumb", "dump", "kill"},
		{ "today", "current" }, { "change", "release" }, { "next", "upcoming" }, { "update", "-u" }, { "list", "search" },
		{ "card", "-c", "show", "display" }, {"icard","-ic"}, {"imaterials","-im"}, {"inext","-in"}, { "battlegroundeffect", "bge" },{"generate","gen","design","make","create","mk","rich"},{"coins","amazon","buy"}, { "random", "crazy", "fun","drunk", "lol"},
		{ "joke", "geek" }, { "nude", "nudes","xxx","porn" },{"donkey","mule","ass"},{"chicken","rooster", "cock"},{"rip", "die", "stop"},{"poop","shit","shite","poopy","pop","dyn"},{"dad","daddy","dev", "share","forward","bug"}, { "version", "-v" }, { "help", "\\?", "-h" },
		{ "options", "-o", "opts" }, };
	protected APNMessageHandler(LineMessagingClient lmc,String msg) {
		this.lmc = lmc;
		this.replyToken = "";
		this.event = null;
		this.content = null;
		this.message = msg;
		this.args = msg.toLowerCase().trim().replaceAll("\\s+", " ").split(" ");
		this.cargs = msg.trim().replaceAll("\\s+", " ").split(" ");
		applyAlias();
	}
	public APNMessageHandler(LineMessagingClient lmc,String replyToken, Event event, TextMessageContent content) {
		this.lmc = lmc;
		this.replyToken = replyToken;
		this.event = event;
		this.content = content;
		this.message = getContent().getText();
		this.args = getContent().getText().toLowerCase().trim().replaceAll("\\s+", " ").split(" ");
		this.cargs = getContent().getText().trim().replaceAll("\\s+", " ").split(" ");
		applyAlias();
		applyHashMap();
	}
	
	public String[] getRecentNames() {
		String[] ids = getRecentGroupIds();
		String[] ret = new String[ids.length];
		for(int i = 0; i < ids.length;i++)
		{
			try {
				ret[i] = lmc.getGroupMemberProfile(getSenderId(),ids[i]).get().getDisplayName();
			} catch (InterruptedException | ExecutionException e) {
				ret[i] = "APN-Pucky";
			}
		}
		return ret;
	}
	public String[] getRecentGroupIds() {return getRecentGroupIds(getSenderId());}
	public String[] getRecentGroupIds(String gid)
	{
		return hm_userid.get(gid);
	}
	
	private void applyHashMap()
	{
		if(hm_userid.containsKey(getSenderId()))
		{
			String[] cur = hm_userid.get(getSenderId());
			String[] n;
			if(cur.length>=HM_USER_NUMBER_SAVE)
			{
				n = new String[HM_USER_NUMBER_SAVE];
				System.arraycopy(cur, 1, n, 0, HM_USER_NUMBER_SAVE-1);
				n[HM_USER_NUMBER_SAVE-1] = event.getSource().getUserId(); 
			}
			else
			{
				n = new String[cur.length+1];
				System.arraycopy(cur, 0, n, 0, cur.length);
				n[cur.length] = getUserId();
			}
			hm_userid.put(getSenderId(), n);
		}
		else
		{
			hm_userid.put(getSenderId(), new String[] {getUserId()});
		}
	}
	
	private void applyAlias()
	{
		for (String[] sa : alias) {
			for (int i =1;i < sa.length;i++) {
				if(sa[i].equals(getArg(1)))
				{
					setArg(1,sa[0]);
					return; //Speed
				}
			}
		}
	}
	
	public String getUserId() {
		return getEvent().getSource().getUserId();
	}
	public String getSenderId() {
		return getEvent().getSource().getSenderId();
	}
	
	private void setArg(int index, String value)
	{
		if(index<args.length)
		{
			args[index]=value;
			cargs[index] = value;
		}
	}

	public String getArg(int index) {return getArg(index,false);};
	public String getArg(int index,boolean c)
	{
		return index<args.length? c?cargs[index]:args[index]:"";
	}
	
	public int getNumber(int index) 
	{
		return Integer.parseInt(getArg(index));
	}
	
	public boolean equals(int index, String value) {
		return getArg(index).equals(value);
	}
	
	public boolean isNumber(int index) {
		return getArg(index).matches("\\d+");
	}

	public String getFrom(int index) {return getFrom(index,false);}
	public String getFrom(int index,boolean c) {
		String ret = "";
		for(int i = index;i<args.length;i++)
		{
			ret += getArg(i,c) +" ";
		}
		return ret.trim();
	}
}
