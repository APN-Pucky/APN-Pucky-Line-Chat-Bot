package com.example.bot.spring;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import com.example.bot.spring.Card.CardInstance;


public class Render {
	Font optimus;
	Font arial;
	public Render() {
		try{
			optimus = Font.createFont(Font.TRUETYPE_FONT, KitchenSinkApplication.resourceLoader.getResource("classpath:" + "static/Optimus.otf").getInputStream());
			arial = Font.createFont(Font.TRUETYPE_FONT, KitchenSinkApplication.resourceLoader.getResource("classpath:" + "static/arialbold.ttf").getInputStream());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public BufferedImage render(CardInstance c)
	{
		BufferedImage img =
				  new BufferedImage(160, 220,
				                    BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.createGraphics();
		int[] style = Data.style_borders[c.getFaction()][c.getRarity()];
		int[] frame = Data.frame_borders[c.getFusionLevel()];
		int[] icon = Data.icon_borders.get("icon_" + c.getUnitType().toLowerCase()+ "_common");
		int[] costs = Data.icon_borders.get("cost_container");
		draw(g,"cogs",0, 0, 150, 125, 5, 20, 150, 125);
		draw(g,"cardResources",style,new int[] {0,0,160,220});
		draw(g,"cardResources",frame,new int[] {0,0,160,220});
		draw(g,"cardResources",icon,new int[] {2,2,24,24});
		drawLevel(g,c);
		g.setColor(Color.WHITE);
		if(c.getCost()>0)
		{
			draw(g,"cardResources",costs,new int[] {120,26,32,32});
			drawCenteredString(g,""+c.getCost(), 136, 49,optimus.deriveFont(Font.PLAIN,20));
		}
		if(c.getUnitType().equals("Assault"))
		{
			g.setFont(optimus.deriveFont(Font.PLAIN,16));
			g.drawString(""+c.getAttack(),24,215);
		}
		drawRightAlignedString(g,""+c.getHealth(),136,215,optimus.deriveFont(Font.PLAIN,16));
		drawArialText(g,c.getCard().getName(),35,18,120,arial.deriveFont(Font.BOLD,12));
		drawArialText(g, StringUtil.capitalizeOnlyFirstLetters(Data.factionToString(c.getFaction())), 10, 140, 140,arial.deriveFont(Font.BOLD,12));
		drawSkill(g,c,arial.deriveFont(Font.BOLD,12));
		return img;
	}

	public static void drawRightAlignedString(Graphics g, String text, int x, int y, Font font) {
	    FontMetrics metrics = g.getFontMetrics(font);
	    x -= (metrics.stringWidth(text));
	    g.setFont(font);
	    g.drawString(text, x, y);
	}
	public static void drawCenteredString(Graphics g, String text, int x, int y, Font font) {
	    FontMetrics metrics = g.getFontMetrics(font);
	    x -= (metrics.stringWidth(text)) / 2;
	    g.setFont(font);
	    g.drawString(text, x, y);
	}

	public static void drawSkill(Graphics g, CardInstance c, Font font)
	{
		SkillSpec[] ss = c.getSkills();
		for(int i =0; i < ss.length;i++)
		{
			int[] skill = Data.skill_borders.get(ss[i].id);
			draw(g,"skills0",skill,new int[] {14, 148 + 16 * i, 16, 16});

			drawArialText(g,ss[i].text(),32, 160 + 16 * i, 115,font );
		}
	}

	public static void drawArialText(Graphics g,String str,int dx,int dy,int maxWidth, Font font) {
	    FontMetrics metrics = g.getFontMetrics(font);
		int wsIdX=0, postDy;
		int x = 11;
		postDy = dy;
		boolean isLong = false;
		do {
			g.setFont(font.deriveFont(Font.PLAIN,x));
			x--;
		} while (g.getFontMetrics(g.getFont()).stringWidth(str) > maxWidth && x > 7);
		postDy = dy;
		if (x == 7) {
			//whitespace index.
			wsIdX = 0;
			isLong = true;
			int sl = (int)Math.floor(str.length() / 2);
			for (int i = 0;i < sl;i++) {
				//start searching from middle.
				if (str.charAt(sl - i) == ' ') {
					wsIdX = sl - i;
					break;
				} else if (str.charAt(sl + i + 1) == ' ') {
					wsIdX = sl + i + 1;
					break;
				}
			}
		} else {
			postDy += (x - 8) / 2;
		}
		if (isLong) {
			g.setFont(font.deriveFont(Font.BOLD, 9));
			g.drawString(str.substring(0, wsIdX), dx, postDy - 4);
			g.drawString(str.substring(wsIdX+1, str.length()), dx, postDy + 4);
		} else {
			g.drawString(str, dx, postDy);
		}
	}


	public static void draw(Graphics g, String img, int[] s, int[] d) {draw(g,img,s[0],s[1],s[2],s[3],d[0],d[1],d[2],d[3]);}
	public static void draw(Graphics g, String img, int sx,int  sy,int sw,int sh,int dx,int dy,int dw,int dh)
	{
		try {
			g.drawImage(ImageIO.read(KitchenSinkApplication.resourceLoader.getResource("classpath:static/"+img+".png").getInputStream()),  dx,dy,dx+dw,dy+dh,sx,sy,sx+sw,sy+sh,null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void drawLevel(Graphics g, CardInstance ci)
	{
		int fused = (ci.getFusionLevel()> 0) ? 1 : 0;
		//max level
		int ml = ci.getCard().getIDs().length;
		//half level
		double hl = (ml > 6) ? Math.ceil(ml / 2) : ml;
		double x = Math.floor((160 - 11 * hl) / 2);
		int y = 205;
		int dxy = 11;

		int i = 0;
		for (i = 0; i < ml;i++) {
			//sort of linebreak at level hl + 1.
			if (i == hl) {
				x = Math.floor((160 - 11 * (ml - hl)) / 2);
				y -= dxy;
			}
			int filled = (i<ci.getLevel()) ? 1 : 0;
			//var path = "/root/icon[fused=" + fused + " and filled=" + filled + "]/source[1]"
			int[] icon = Data.icon_borders.get("icon_" + (fused>0?"fused":"unfused") + "_" + (filled>0?"full":"empty"));
			draw(g,"cardResources",icon, new int[] {(int)x,y,dxy,dxy});
			x += dxy;
		}
	}
}
