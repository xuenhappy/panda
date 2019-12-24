package org.bamboo.nlp.panda.source;

import java.io.InputStream;

public final class Resource {
	
	public static final String HTML_FORMAT_CSS="htmlformat.css";
	public static final String INNER_WORD_DICT="panda.bd";
	
	
	
	/**
	 * get give name data
	 * @param name
	 * @return
	 */
	public static final InputStream getResource(String name) {
		return Resource.class.getResourceAsStream(name);
	}

}
