package org.runaway.exception;

import org.runaway.exception.RunawayException;

//import java.sql.SQLException;

public class B {
	
	public String calc(String paramA) // throws SQLException
	{
		
		int x = 35;
		C c;
		
		try {
			c = new C();
			c.getData(54110, paramA);
			x = 73;
			
			return "abcd";

		} catch(/*SQLException |*/ RuntimeException e) {
			RunawayException be = RunawayException.of(e);
			be.snap("paramA", paramA);
			be.snap("x", x);
			throw be;
		}
		
	}

}
