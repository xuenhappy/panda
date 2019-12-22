package org.bamboo.nlp.panda.tools;

import java.io.IOException;

/**
 * 采用H5文件支持的wordvec数据
 * @author xuen
 *
 */
public class H5WordVecDic  implements WordVecDic{

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int dimSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long wordNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasWord(CharSequence str) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float[] embeding(CharSequence seq) {
		// TODO Auto-generated method stub
		return null;
	}

}
