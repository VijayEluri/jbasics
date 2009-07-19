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
package org.jbasics.math.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import org.jbasics.math.IrationalNumber;

public class ExponentialIrationalNumber extends BigDecimalIrationalNumber {
	public static final IrationalNumber<BigDecimal> E = new ExponentialIrationalNumber(BigDecimal.ONE);

	public static IrationalNumber<BigDecimal> valueOf(BigDecimal x) {
		if (BigDecimal.ONE.compareTo(x) == 0) {
			return E;
		}
		return new ExponentialIrationalNumber(x);
	}

	private ExponentialIrationalNumber(BigDecimal x) {
		super(x);
	}

	@Override
	protected BigDecimal calculate(BigDecimal x, MathContext mc) {
		int scale = (int) Math.ceil(Math.sqrt((mc.getPrecision() + 10) * Math.log(10) / Math.log(2)));
		if (x.abs().compareTo(BigDecimal.ONE) > 0) {
			BigInteger temp = x.unscaledValue();
			int scale2 = (int) (x.scale() * (Math.log(10) / Math.log(2))) + 1;
			int precis2 = temp.bitLength();
			scale = scale + precis2 - scale2;
		} else {
			scale = scale + ((int) (x.scale() * (Math.log(10) / Math.log(2))));
		}
		MathContext roundContext = mc == null ? MathContext.DECIMAL128 : mc;
		BigDecimal xScaled = x.divide(new BigDecimal(BigInteger.ONE.shiftLeft(scale)));
		MathContext calcContext = new MathContext(roundContext.getPrecision() + (int) Math.ceil(scale * Math.log10(2))
				+ 1, RoundingMode.HALF_EVEN);
		BigInteger k = BigInteger.ONE;
		int n = 1;
		BigDecimal xPower = BigDecimal.ONE;
		BigDecimal result = BigDecimal.ONE;
		do {
			xPower = xPower.multiply(xScaled, calcContext);
			result = result.multiply(BigDecimal.valueOf(n)).add(xPower);
			k = k.multiply(BigInteger.valueOf(n++));
		} while (calcContext.getPrecision() - xPower.scale() + xPower.precision() > 0);
		result = result.round(calcContext).divide(new BigDecimal(k), calcContext);
		for (int i = scale; i > 0; i--) {
			result = result.multiply(result, calcContext);
		}
		return result.round(roundContext);
	}

}
