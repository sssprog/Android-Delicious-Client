package com.sssprog.delicious.dbmodels;

import java.io.Serializable;

import com.sssprog.activerecord.ActiveRecord;

public class PostModel extends ActiveRecord/* implements Serializable*/ {
	
	/**
	 * 
	 */
//	private static final long serialVersionUID = 5686876307878710841L;
	public String description;
	public String extended;
	public String href;
	public boolean privatePost;
	public long creationDate;
	public String tags;
	
	/*
	 * <post 
	 * 	description="Overview (Java Platform SE 7 )" 
	 * extended="" 
	 * hash="e6295f8d9d0666bd81e7f514c323c02c" 
	 * href="http://docs.oracle.com/javase/7/docs/api/" 
	 * private="no" 
	 * shared="yes" 
	 * tag="reference java api se7 docs java7 javadoc documentation api programming Java API java" 
	 * time="2012-11-24T12:06:37Z"/>
	 */

}
