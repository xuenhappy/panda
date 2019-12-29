package org.bamboo.nlp.panda.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bamboo.nlp.panda.source.Resource;
import org.bamboo.nlp.panda.tools.DoubleArrayTrie;
import org.bamboo.nlp.panda.tools.StrList;

/**
 * 基于trie词典的方式识别基本单元
 * 
 * @author xuen
 *
 */
public class DictCellRecongnizer implements CellRecognizer {
	private static final Pattern DICT_SPLIT_REGEX = Pattern.compile("\\s*\\|\\|\\s*");
	private static final Pattern TAGS_SPLIT_REGEX = Pattern.compile("\\s*,\\s*");

	private static final class StrArray implements StrList {
		private final ArrayList<String> datas;

		public StrArray(ArrayList<String> datas) {
			this.datas = datas;
		}

		@Override
		public Iterator<CharSequence> iterator() {
			final Iterator<String> c = datas.iterator();
			return new Iterator<CharSequence>() {

				@Override
				public boolean hasNext() {
					return c.hasNext();
				}

				@Override
				public CharSequence next() {
					return c.next();
				}
			};
		}

		@Override
		public int size() {
			return datas.size();
		}

		public void clear() {
			datas.clear();
		}

	}

	/**
	 * tire dict
	 */
	private final DoubleArrayTrie<CellType[]> dicts;

	public DictCellRecongnizer(String usrDict) throws IOException {
		this.dicts = new DoubleArrayTrie<CellType[]>();
		loadDict(usrDict);
	}

	/**
	 * load allDict
	 * 
	 * @param usrDict
	 * @throws IOException
	 */
	private void loadDict(String usrDict) throws IOException {
		LinkedList<CellType[]> vals = new LinkedList<CellType[]>();
		LinkedList<StrArray> keys = new LinkedList<StrArray>();
		Set<String> dup = new HashSet<String>();
		loadDict(keys, vals, Resource.getResource(Resource.INNER_WORD_DICT),dup);
		if (usrDict != null && !usrDict.isEmpty()) {
			File file = new File(URI.create(usrDict));
			loadDict(keys, vals, new FileInputStream(file),dup);
		} // load usr dict
		this.dicts.build(keys, vals);
		// clear data
		for (StrArray s : keys)
			s.clear();
		keys.clear();
		vals.clear();
		dup.clear();
	}

	/**
	 * load data from inputstream
	 * 
	 * @param data
	 * @param resource
	 * @throws IOException
	 */
	private void loadDict(List<StrArray> keys, List<CellType[]> vals, InputStream resource,Set<String> dup) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource, "utf-8"), 1024 * 5);
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				String[] ls = DICT_SPLIT_REGEX.split(line);
				if (ls.length != 2)
					continue;
				String key = ls[0].toLowerCase();
				if (dup.contains(key))
					continue;
				dup.add(key);
				ArrayList<String> keyary = BaseLex.splitStr2(key);
				if (keyary.size() < 2)
					continue;
				String[] tags = TAGS_SPLIT_REGEX.split(ls[1]);
				CellType[] ts = new CellType[tags.length];
				for (int i = 0; i < tags.length; i++)
					ts[i] = CellType.valueOf(tags[i]);
				keys.add(new StrArray(keyary));
				vals.add(ts);
			}
		} finally {
			reader.close();
		}
	}

	@Override
	public void read(AtomList baseStr, CellMap map) {
		dicts.parseText(baseStr, new DoubleArrayTrie.IHit<CellType[]>() {
			CellMap.Cursor cursor = map.head();

			@Override
			public boolean hit(int begin, int end, CellType[] value) {
				Atom newAtom = baseStr.sub(begin, end);
				newAtom.addType(value);
				cursor = map.addCell(cursor, new WordCell(newAtom, begin, end));
				return true;
			}
		});
	}

	@Override
	public void close() throws IOException {
	}

}
