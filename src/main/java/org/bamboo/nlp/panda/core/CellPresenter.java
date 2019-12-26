package org.bamboo.nlp.panda.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * the  presenter of cell
 * @author xuen
 *
 */
public interface CellPresenter extends Closeable{
	
	/**
	 * embed the word cell
	 * @param cell
	 * @param context the context of this word in
	 * @throws IOException
	 */
	public void embed(CellMap cells,AtomList context)throws IOException;

}
