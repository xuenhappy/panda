package org.bamboo.nlp.panda.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
		for (WordCell c : tokens) {
			Collection<CellType> types = c.getTypes();
			int fnum = 0;
			int sz = tokens.size();
			assert sz>0;
			for (CellType m : types) {
				if ((sz - fnum) > 1 && (m == CellType.UNK || m == CellType.CHW || m == CellType.CHW)) {//filter low infomation tag
					fnum++;
					continue;
				}
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
