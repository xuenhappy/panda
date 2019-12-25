package org.bamboo.nlp.panda.core;

import java.io.Closeable;
import java.io.IOException;

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
	public void embededing(WordCell cell)throws IOException;
	
	
	
	/**
	 * the distance of from pre to next
	 * @param pre
	 * @param next
	 * @return
	 */
	public double distance(WordCell pre,WordCell next)throws IOException;
	
	
	

}
