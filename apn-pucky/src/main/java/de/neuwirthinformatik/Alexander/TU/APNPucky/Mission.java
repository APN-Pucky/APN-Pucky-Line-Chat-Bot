package de.neuwirthinformatik.Alexander.TU.APNPucky;

public class Mission 
{
	private int id,costs;
	private String name;
	
	public Mission(int id, int costs, String name)
	{
		this.id = id;
		this.costs = costs;
		this.name = name;
	}

	public int getID() {return id;}
	public int getCosts() {return costs;}
	public String getName() {return name;}
	
	public String toString()
	{
		return name + " [" + id +"]:$" + costs;
	}
	
}
