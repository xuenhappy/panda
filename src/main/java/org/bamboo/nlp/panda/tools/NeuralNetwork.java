package org.bamboo.nlp.panda.tools;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

/**
 * 一些常见的神经网络结构
 * 
 * @author xuen
 *
 */
public final class NeuralNetwork {

	private interface AFC {
		/**
		 * 执行激活函数
		 * 
		 * @param val
		 * @return
		 */
		public FloatMatrix dothis(FloatMatrix val);
	}

	/**
	 * 常用的激活函数
	 * 
	 * @author xuen
	 *
	 */
	public static enum Activation {
		/**
		 * tanh激活
		 */
		Tanh(new AFC() {
			@Override
			public FloatMatrix dothis(FloatMatrix val) {
				return MatrixFunctions.tanhi(val);
			}
		}), 
		
		/**
		 * sigmoid激活
		 */
		Sigmoid(new AFC() {
			@Override
			public FloatMatrix dothis(FloatMatrix val) {
				return MatrixFunctions.tanhi(val.divi(2)).addi(1).divi(2);
			}
		}),
		
		/**
		 * exp激活
		 */
		Exp(new AFC() {
			public FloatMatrix dothis(FloatMatrix val) {
				return MatrixFunctions.expi(val);
			};
		});

		private final AFC act;

		private Activation(AFC act) {
			this.act = act;
		}
	}

	/**
	 * dense层
	 * 
	 * @author xuen
	 *
	 */
	public static final class Dense {
		private final Activation activation;
		private final FloatMatrix weight;
		private final FloatMatrix blas;

		public Dense(FloatMatrix weight, FloatMatrix blas, Activation activation) {
			this.activation = activation;
			this.weight = weight;
			this.blas = blas;
		}

		public FloatMatrix call(FloatMatrix input) {
			FloatMatrix val = input.mul(weight);
			if (blas != null)
				val = val.addiRowVector(blas);
			if (activation != null)
				val = activation.act.dothis(val);
			return val;
		}

	}

	/**
	 * 基础GRU层
	 * 
	 * @author xuen
	 *
	 */
	public static final class GRU {

		public FloatMatrix zeroState() {
			return null;
		}

		public FloatMatrix call(FloatMatrix state, FloatMatrix input) {
			return null;
		}

	}

	public static void main(String[] args) {
		FloatMatrix f1 = new FloatMatrix(new float[] { 1, 0, 3, 4 });
		FloatMatrix f2 = new FloatMatrix(new float[][] { { 8, 0, 3, 4 }, { 2, 3, 4, 5 } });
		f2.addiRowVector(f1);
		System.out.println(f2);
	}

}
