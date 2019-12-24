package org.bamboo.nlp.panda.core;

import org.bamboo.nlp.panda.tools.DoubleArrayTrie;

/**
 * 基于trie词典的方式识别基本单元
 * @author xuen
 *
 */
public class DictCellRecongnizer implements CellRecognizer{
	
	private DoubleArrayTrie<CellType[]> dicts;


	@Override
	public void read(AtomList baseStr, CellMap map) {
	
		
	}

}
