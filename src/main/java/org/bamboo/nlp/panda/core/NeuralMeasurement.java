package org.bamboo.nlp.panda.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bamboo.nlp.panda.tools.IOSerializable;
import org.bamboo.nlp.panda.tools.NeuralNetwork;
import org.bamboo.nlp.panda.tools.NeuralNetwork.TDense;
import org.jblas.FloatMatrix;

/**
 * a neural network measurement
 * 
 * @author xuen
 *
 */
public class NeuralMeasurement implements Measurement, IOSerializable {
	private NeuralNetwork.TDense map;
	private NeuralNetwork.TDense join;
	private NeuralNetwork.TDense out;

	public NeuralMeasurement(TDense map, TDense join, TDense out) {
		super();
		this.map = map;
		this.join = join;
		this.out = out;
	}

	public NeuralMeasurement() {
		super();
		this.map = new TDense();
		this.join = new TDense();
		this.out = new TDense();
	}

	@Override
	public double measure(float[] f1, float[] f2) {
		FloatMatrix i1 = new FloatMatrix(1, f1.length, f1);
		FloatMatrix i2 = new FloatMatrix(1, f2.length, f2);
		FloatMatrix m = FloatMatrix.concatHorizontally(map.forward(i1), map.forward(i2));
		FloatMatrix res = out.forward(join.forward(m));
		return res.get(0, 0);
	}

	@Override
	public void load(InputStream in) throws IOException {
		this.map.load(in);
		this.join.load(in);
		this.out.load(in);
	}

	@Override
	public void save(OutputStream out) throws IOException {
		this.map.save(out);
		this.join.save(out);
		this.out.save(out);
	}

}
