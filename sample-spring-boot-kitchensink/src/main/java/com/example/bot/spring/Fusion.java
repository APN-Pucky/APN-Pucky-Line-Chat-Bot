package com.example.bot.spring;

public class Fusion 
{
	public static final Fusion NULL = new Fusion(0,new int[]{});
	private int id;
	private int[] materials;
	
	public Fusion(int id, int[] mats)
	{
		this.id = id;
		this.materials = mats;
	}

	public int getID() {return id;}
	public int[] getMaterials() {return materials;}
	
	public String toString()
	{
		String ret = "";
		for(int i = 0; i < materials.length;i++)
		{
			ret += "[" + materials[i]+ "] + ";
		}
		ret += "=> [" + id + "]";
		return ret;
	}
	
	public boolean equals(Fusion f) {return f.getID()==id;}
	
}
