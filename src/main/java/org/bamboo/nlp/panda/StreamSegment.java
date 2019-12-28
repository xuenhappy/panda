package org.bamboo.nlp.panda;

import java.io.Reader;

public class StreamSegment {
	/**
	 * data
	 */
	protected final Reader input;
	
	
	public StreamSegment(Reader input) {
		super();
		this.input = input;
	}



	/**
	 * split data
	 * @return
	 */
	public synchronized Token next() {
		return null;
	}

	
	
	public synchronized void reset(Reader input) {
		// TODO Auto-generated method stub
		
	}
	

}
