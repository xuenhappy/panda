package org.bamboo.nlp.panda.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bamboo.mkl4j.FloatMatrix;
import org.bamboo.mkl4j.TDense;
import org.bamboo.nlp.panda.tools.IOSerializable;

/**
 * a neural network measurement
 * 
 * @author xuen
 *
 */
public class NeuralMeasurement implements Measurement, IOSerializable {
	private TDense<FloatMatrix> map;
	private TDense<FloatMatrix> join;
	private TDense<FloatMatrix> out;

	public NeuralMeasurement(TDense<FloatMatrix> map, TDense<FloatMatrix> join, TDense<FloatMatrix> out) {
		super();
		this.map = map;
		this.join = join;
		this.out = out;
	}

	@Override
	public double measure(float[] f1, float[] f2) {
		FloatMatrix i1 = new FloatMatrix(1, f1.length, f1);
		FloatMatrix i2 = new FloatMatrix(1, f2.length, f2);
		FloatMatrix m = map.forward(i1).concatColumn(map.forward(i2));
		FloatMatrix res = out.forward(join.forward(m));
		return res.get(0, 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(DataInputStream in) throws IOException {
		this.map = TDense.load(in);
		this.join = TDense.load(in);
		this.out = TDense.load(in);
	}

	@Override
	public void save(DataOutputStream out) throws IOException {
		this.map.save(out);
		this.join.save(out);
		this.out.save(out);

	}

}
