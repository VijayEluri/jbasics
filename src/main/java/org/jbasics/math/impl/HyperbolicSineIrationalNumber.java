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
import java.math.MathContext;

import org.jbasics.math.IrationalNumber;

public class HyperbolicSineIrationalNumber extends BigDecimalIrationalNumber {

	private final IrationalNumber<BigDecimal> expX;

	public static IrationalNumber<BigDecimal> valueOf(BigDecimal x) {
		return new HyperbolicSineIrationalNumber(x);
	}

	private HyperbolicSineIrationalNumber(BigDecimal x) {
		super(BigDecimal.ZERO);
		this.expX = ExponentialIrationalNumber.valueOf(x);
	}

	@Override
	protected BigDecimal calculate(BigDecimal x, BigDecimal currentValue, MathContext mc) {
		// Hyperbolic Sine is sinh x = (e^x - e^(-x)) / 2
		// e^-x is the same as 1/e^x so we can actually do the math hence we only need to calculate
		// e^x once! so we calculate
		// sinh x = 1/2 * (e^x - 1/e^x)
		return this.expX.valueToPrecision(mc).subtract(BigDecimal.ONE.divide(this.expX.valueToPrecision(mc)), mc)
				.multiply(MathImplConstants.HALF);
	}

}