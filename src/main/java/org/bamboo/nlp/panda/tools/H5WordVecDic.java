package org.bamboo.nlp.panda.tools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import ch.systemsx.cisd.hdf5.HDF5DataClass;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5ObjectReadOnlyInfoProviderHandler;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * 采用H5文件支持的wordvec数据
 * 
 * @author xuen
 *
 */
public class H5WordVecDic implements WordVecDic {

	private final IHDF5Reader reader;
	private final Map<CharSequence, Integer> word_idx;
	private final int dim_size;
	private final float[][] cache;

	public H5WordVecDic(String h5file) throws IOException {
		IHDF5Reader reader = HDF5Factory.openForReading(h5file);
		try {
			// check key
			if (!reader.exists("wds"))
				throw new IOException("bad hdf5 file input " + h5file + " no data set wds");
			if (!reader.exists("ary"))
				throw new IOException("bad hdf5 file input " + h5file + " no data set ary");
			IHDF5ObjectReadOnlyInfoProviderHandler robj = reader.object();
			// check is dataset
			if (!robj.isDataSet("wds"))
				throw new IOException("bad hdf5 file input " + h5file + "  wds is not dataset");
			if (!robj.isDataSet("ary"))
				throw new IOException("bad hdf5 file input " + h5file + "  ary is not dataset");
			HDF5DataSetInformation ary_df = robj.getDataSetInformation("ary");
			HDF5DataSetInformation wds_df = robj.getDataSetInformation("wds");
			// check size
			long[] ary_sizes = ary_df.getDimensions();
			long[] wds_sizes = wds_df.getDimensions();
			if (wds_sizes.length != 1)
				throw new IOException("bad hdf5 file input " + h5file + " no data set wds dim rank size not 1");
			if (ary_sizes.length != 2)
				throw new IOException("bad hdf5 file input " + h5file + " no data set ary dim rank size not 2");
			if (ary_sizes[0] != wds_sizes[0])
				throw new IOException("bad hdf5 file input " + h5file + " no data set word not eq str");

			// check type
			if (ary_df.getTypeInformation().getElementSize() != 4
					|| ary_df.getTypeInformation().getDataClass() != HDF5DataClass.FLOAT)
				throw new IOException("bad hdf5 file input " + h5file + "  ary is not float32");
			if (wds_df.getTypeInformation().getDataClass() != HDF5DataClass.STRING)
				throw new IOException("bad hdf5 file input " + h5file + "  wds is not string");
			this.reader = reader;
			this.dim_size = (int) ary_sizes[1];
			this.word_idx = new TreeMap<CharSequence, Integer>();
			String[] words = this.reader.readStringArray("wds");
			for (int i = 0; i < words.length; i++)
				this.word_idx.put(words[i], i);
			if (this.word_idx.size()<20001) {
				this.cache = this.reader.readFloatMatrix("ary");
			} else {
				this.cache = null;
			}
		} catch (IOException e) {
			reader.close();
			throw e;
		}

	}

	@Override
	public void close() throws IOException {
		if (this.reader != null)
			this.reader.close();
	}

	@Override
	public int dimSize() {
		return dim_size;
	}

	@Override
	public long wordNum() {

		return word_idx.size();
	}

	@Override
	public boolean hasWord(CharSequence str) {
		return this.word_idx.containsKey(str);
	}

	@Override
	public float[] embeding(CharSequence seq) {
		if (this.word_idx.containsKey(seq)) {
			int idx = word_idx.get(seq);
			if (this.cache != null)// 输出缓存数据
				return Arrays.copyOf(this.cache[idx], this.cache[idx].length);
			// 输出硬盘数据
			float[][] v = reader.float32().readMatrixBlock("ary", 1, (int) this.dim_size, idx, 0);
			if (v != null && v.length > 0)
				return v[0];
		}
		return null;
	}

}
