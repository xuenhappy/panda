package org.bamboo.nlp.panda.core;

/**
 * 基本单元识别器
 * @author xuen
 *
 */
public interface CellRecognizer {
	
	/**
	 * recognize the word cell from base string and put it into the map
	 * @param baseStr
	 * @param map
	 */
	public void read(AtomList baseStr,CellMap map);

}
