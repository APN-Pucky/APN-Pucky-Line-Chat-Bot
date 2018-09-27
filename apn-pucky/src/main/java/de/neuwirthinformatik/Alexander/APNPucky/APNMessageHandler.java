package de.neuwirthinformatik.Alexander.APNPucky;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.message.TextMessageContent;

import lombok.Getter;
import lombok.NonNull;


public class APNMessageHandler 
{
	@Getter private final String replyToken;
	@Getter private final Event event;
	@Getter private final TextMessageContent content;
	@Getter @NonNull private final String message;
	@Getter @NonNull private final String[] args;
	
	public static final String[][] alias = new String[][] { { "materials", "mats", "build", "-m", "-b" }, {"insult", "destroy", "shut", "dumb", "dump", "kill"},
		{ "today", "current" }, { "change", "release" }, { "next", "upcoming" }, { "update", "-u" }, { "list", "search" },
		{ "card", "-c", "show", "display" }, {"icard","-ic"}, {"imaterials","-im"}, {"inext","-in"}, { "battlegroundeffect", "bge" },{"generate","gen","design","make","create","mk","rich"},{"coins","amazon","buy"}, { "random", "crazy", "fun","drunk", "lol"},
		{ "joke", "geek" }, { "nude", "nudes" },{"poop","shit","shite","poopy","pop"},{"dad","daddy","dev", "share","forward","bug"}, { "version", "-v" }, { "help", "\\?", "-h" },
		{ "options", "-o", "opts" }, };
	protected APNMessageHandler(String msg) {
		this.replyToken = "";
		this.event = null;
		this.content = null;
		this.message = msg;
		this.args = msg.toLowerCase().trim().replaceAll("\\s+", " ").split(" ");
		applyAlias();
	}
	public APNMessageHandler(String replyToken, Event event, TextMessageContent content) {
		this.replyToken = replyToken;
		this.event = event;
		this.content = content;
		this.message = getContent().getText();
		this.args = getContent().getText().toLowerCase().trim().replaceAll("\\s+", " ").split(" ");
		applyAlias();
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
	
	public String getUserID() {
		return getEvent().getSource().getUserId();
	}
	public String getSenderID() {
		return getEvent().getSource().getSenderId();
	}
	
	private void setArg(int index, String value)
	{
		if(index<args.length)
		{
			args[index]=value;
		}
	}
	
	public String getArg(int index)
	{
		return index<args.length?args[index]:"";
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
	
	public String getFrom(int index) {
		String ret = "";
		for(int i = index;i<args.length;i++)
		{
			ret += getArg(i) +" ";
		}
		return ret.trim();
	}
}
