package org.bamboo.nlp.panda.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * a pos tagger that do nothing
 * 
 * @author xuen
 *
 */
public class NothingDoPosTagger implements PosTagger {
	private final String[] types;

	public NothingDoPosTagger() {
		CellType[] ds = CellType.values();
		this.types = new String[ds.length];
		for (int i = 0; i < types.length; i++) {
			this.types[i] = ds[i].toString();
		}
		Arrays.sort(types);
	}

	@Override
	public void tag(List<WordCell> tokens) {
		Set<CellType> tmp=new  HashSet<CellType>();
		for (WordCell c : tokens) {
			tmp.clear();
			tmp.addAll(c.getTypes());
			assert tmp.size() > 0;
			// filter low information tag
			if(tmp.size()>1&&tmp.contains(CellType.UNK))
				tmp.remove(CellType.UNK);
			if(tmp.size()>1&&tmp.contains(CellType.CHW))
				tmp.remove(CellType.CHW);
			if(tmp.size()>1&&tmp.contains(CellType.ENG))
				tmp.remove(CellType.ENG);
			for (CellType m : tmp) {
				c.setFeature(Arrays.binarySearch(this.types, m.toString()));
				break;
			}

		}
	}

	@Override
	public String explain(int feature) {
		return this.types[feature];
	}

}
