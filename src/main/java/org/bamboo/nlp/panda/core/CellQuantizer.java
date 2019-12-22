package org.bamboo.nlp.panda.core;

import java.io.Closeable;

/**
 * embeding the cell
 * @author xuen
 *
 */
public interface CellQuantizer extends Closeable{
	
	/**
	 * embeding the cell
	 * @param str
	 * @return
	 */
	public void embededing(WordCell cell);
	
	
	
	/**
	 * the distance of from pre to next
	 * @param pre
	 * @param next
	 * @return
	 */
	public double distance(WordCell pre,WordCell next);
	
	
	

}
