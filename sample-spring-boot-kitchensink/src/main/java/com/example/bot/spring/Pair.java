package com.example.bot.spring;

public class Pair<T, U> {         
    public final T t;
    public final U u;

    public Pair(T t, U u) {         
        this.t= t;
        this.u= u;
     }
    public String toString()
    {
    	return t.toString() + " - " + u.toString();
    }
 }