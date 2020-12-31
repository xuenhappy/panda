package org.bamboo.nlp.panda;

import org.bamboo.nlp.panda.core.Atom;

/**
 * token for last
 * @author xuen
 *
 */
public class Token {
	
	
	
	private Atom atom;
	private String type;
	
	
	
	public Token(Atom atom, String type) {
		super();
		this.atom = atom;
		this.type = type;
	}



	public CharSequence getText() {
		return atom.image;
	}
	



	@Override
	public String toString() {
		return "Token [atom=" + atom + ", type=" + type + "]";
	}



	public int getLength() {
		return atom.image.length();
	}



	public int getBeginPosition() {
		return atom.begin;
	}



	public int getEndPosition() {
		return atom.end;
	}



	public String getType() {
		return type;
	}

}
