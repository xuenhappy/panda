package org.bamboo.nlp.panda.lucene;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.apache.lucene.util.QueryBuilder;

/**
 * parse a string to the lucene query like {@link QueryBuilder}
 * 
 * @author xuen
 *
 */
public class PandaQueryParser {

	/**
	 * the entity of parser parse
	 * 
	 * @author xuen
	 *
	 */
	public static enum Entity {

	}

	private final Map<Entity, String> typeMap;

	public PandaQueryParser() {
		this.typeMap = new HashMap<PandaQueryParser.Entity, String>();
	}

	public void setTypeMap(Entity entity, String type) {
		this.typeMap.put(entity, type);
	}

	/**
	 * parse the query to use
	 * 
	 * @param query
	 * @return
	 */
	public Map<Query, Float> parse(String query) {
		return null;

	}

}
