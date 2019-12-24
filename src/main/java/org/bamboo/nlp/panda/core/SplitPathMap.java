package org.bamboo.nlp.panda.core;

import java.util.Iterator;
import java.util.List;

/**
 * split path map
 * 
 * @author xuen
 *
 */
public class SplitPathMap implements HtmlVisually {

	public SplitPathMap(CellMap cmap,CellQuantizer quantizer) {
		Iterator<WordCell> it = cmap.iterator();
		while (it.hasNext())
			quantizer.embededing(it.next());
	}

	@Override
	public String toHtml() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<WordCell> bestPath() {
		return null;
	}

	/**
	 * Calculate shortest cut path
	 * 
	 * @param quantizer
	 */
	public void optim() {
		
		
	}

}
