package org.bamboo.nlp.panda.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bamboo.nlp.panda.PandaConf;
import org.bamboo.nlp.panda.source.Resource;
import org.bamboo.nlp.panda.tools.DoubleArrayTrie;
import org.bamboo.nlp.panda.tools.SimpleStrList;

/**
 * 基于trie词典的方式识别基本单元
 * 
 * @author xuen
 *
 */
public class DictCellRecongnizer implements CellRecognizer {

	private final DoubleArrayTrie<CellType[]> dicts;

	public DictCellRecongnizer(PandaConf conf) throws IOException {
		this.dicts = new DoubleArrayTrie<CellType[]>();
		loadDict(conf.getUsrDict());
	}

	/**
	 * load allDict
	 * 
	 * @param usrDict
	 * @throws IOException
	 */
	private void loadDict(String usrDict) throws IOException {
		Map<SimpleStrList, CellType[]> data = new HashMap<SimpleStrList, CellType[]>();
		loadDict(data, Resource.getResource(Resource.INNER_WORD_DICT));
		if (usrDict != null && !usrDict.isEmpty()) {
			File file = new File(URI.create(usrDict));
			loadDict(data, new FileInputStream(file));
		} // load usr dict
		LinkedList<CellType[]> vals = new LinkedList<CellType[]>();
		LinkedList<SimpleStrList> keys = new LinkedList<SimpleStrList>();
		for (Map.Entry<SimpleStrList, CellType[]> ent : data.entrySet()) {
			vals.add(ent.getValue());
			keys.add(ent.getKey());
		}
		this.dicts.build(keys, vals);
		// clear data
		data.clear();
		for (SimpleStrList s : keys)
			s.clear();
		keys.clear();
		vals.clear();
	}

	/**
	 * load data from inputstream
	 * 
	 * @param data
	 * @param resource
	 * @throws IOException
	 */
	private void loadDict(Map<SimpleStrList, CellType[]> data, InputStream resource) throws IOException {
		resource.close();
	}

	@Override
	public void read(AtomList baseStr, CellMap map) {
		dicts.parseText(baseStr, new DoubleArrayTrie.IHit<CellType[]>() {
			CellMap.Node head = map.head();

			@Override
			public boolean hit(int begin, int end, CellType[] value) {
				Atom newAtom = baseStr.sub(begin, end);
				for (CellType t : value)
					newAtom.addType(t);
				head = map.addNext(head, new WordCell(newAtom, begin, end));
				return true;
			}
		});
	}

}
