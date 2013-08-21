/*
 * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
 *
 * Stephan Schloepke: http://www.schloepke.de/
 * innoQ Deutschland GmbH: http://www.innoq.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jbasics.math.approximation;

import java.math.BigDecimal;
import java.math.MathContext;

import org.jbasics.checker.ContractCheck;
import org.jbasics.math.MathFunction;
import org.jbasics.math.NumberConverter;
import org.jbasics.utilities.DataUtilities;

public class RegulaFalsi {
	private final MathFunction zeroFunction;
	private final MathContext mc;
	private final BigDecimal epsilon;

	public RegulaFalsi(final MathFunction zeroFunction, final MathContext mc) {
		this.zeroFunction = ContractCheck.mustNotBeNull(zeroFunction, "zeroFunction");
		this.mc = DataUtilities.coalesce(mc, MathContext.DECIMAL64);
		this.epsilon = BigDecimal.ONE.scaleByPowerOfTen(-this.mc.getPrecision());
	}

	//	public BigDecimal findZero(final BigDecimal fx, final BigDecimal x1, final BigDecimal x2) {
	//		return findZero(fx, x1, x2, NumberConverter.toBigDecimal(this.zeroFunction.calculate(this.mc, x1)).subtract(fx, this.mc), NumberConverter
	//				.toBigDecimal(this.zeroFunction.calculate(this.mc, x2)).subtract(fx, this.mc));
	//	}
	//
	//	private BigDecimal findZero(final BigDecimal fx, final BigDecimal x1, final BigDecimal x2, final BigDecimal f1, final BigDecimal f2) {
	//		final BigDecimal z = x1.subtract(x2.subtract(x1).divide(f2.subtract(f1), this.mc).multiply(f1, this.mc));
	//		final BigDecimal fz = NumberConverter.toBigDecimal(this.zeroFunction.calculate(this.mc, z)).subtract(fx, this.mc);
	//		if (x2.subtract(x1).abs().compareTo(this.epsilon) > 0 || f2.abs().compareTo(this.epsilon) > 0) {
	//			return z;
	//		}
	//		if (fz.multiply(f2).signum() < 0) {
	//			return findZero(fx, x2, z, f2, fz);
	//		} else {
	//			return findZero(fx, x1, z, f1.multiply(f2.divide(f2.add(fz), this.mc)), fz);
	//		}
	//	}

	public BigDecimal findZero(final BigDecimal fx, BigDecimal x1, BigDecimal x2) {
		BigDecimal f1 = NumberConverter.toBigDecimal(this.zeroFunction.calculate(this.mc, x1)).subtract(fx, this.mc);
		BigDecimal f2 = NumberConverter.toBigDecimal(this.zeroFunction.calculate(this.mc, x2)).subtract(fx, this.mc);
		BigDecimal z, fz;
		int i = 0;
		do {
			if (i++ > 1000) {
				throw new RuntimeException("Iteration to look takes to long");
			}
			final BigDecimal temp = f2.subtract(f1);
			z = x1.subtract(x2.subtract(x1).divide(temp, this.mc).multiply(f1, this.mc)).max(BigDecimal.ZERO);
			fz = NumberConverter.toBigDecimal(this.zeroFunction.calculate(this.mc, z)).subtract(fx, this.mc);
			if (f1.signum() == fz.signum()) {
				f1 = f1.multiply(f2.divide(f2.add(fz), this.mc));
				x2 = z;
				f2 = fz;
			} else {
				x1 = x2;
				f1 = f2;
				x2 = z;
				f2 = fz;
			}
		} while (x2.subtract(x1).abs().compareTo(this.epsilon) > 0 && fz.abs().compareTo(this.epsilon) > 0);
		return z;
	}
}