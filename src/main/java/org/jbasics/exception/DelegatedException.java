/*
 * Copyright (c) 2009-2015
 * 	IT-Consulting Stephan Schloepke (http://www.schloepke.de/)
 * 	klemm software consulting Mirko Klemm (http://www.klemm-scs.com/)
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
package org.jbasics.exception;

import org.jbasics.checker.ContractCheck;
import org.jbasics.pattern.delegation.Delegate;

import java.util.concurrent.Callable;

/**
 * An exception container to hold an exception which is only delegated. The real exception stack trace is taken and used
 * rather than creating the one for the delegated. <p> A {@link DelegatedException} is always a {@link RuntimeException}
 * since the main case is to rethrow an exception as {@link RuntimeException} which was a checked exception before.
 * </p>
 *
 * @author Stephan Schloepke
 * @since 1.0
 */
public final class DelegatedException extends RuntimeException implements Delegate<Throwable> {
	private static final long serialVersionUID = 8127831096870654601L;

	private DelegatedException(final Throwable t) {
		super(t.getMessage(), t); //$NON-NLS-1$
		setStackTrace(t.getStackTrace());
	}

	public static DelegatedException delegate(final Throwable t) {
		if (ContractCheck.mustNotBeNull(t, "t") instanceof DelegatedException) {
			return (DelegatedException) t;
		}
		return new DelegatedException(t);
	}

	public Throwable delegate() {
		return getCause();
	}

	@Override
	public String toString() {
		StringBuilder temp = new StringBuilder("[Delegated] ");
		temp.append(getCause().getClass().getName());
		String message = getLocalizedMessage();
		if (message != null) {
			temp.append(": ").append(message); //$NON-NLS-1$
		}
		return temp.toString();
	}

	public static <T> T callDelegated(Callable<T> callable) {
		if (callable == null) {
			throw new IllegalArgumentException("Null parameter: callable");
		}
		try {
			return callable.call();
		} catch(Exception e) {
			throw DelegatedException.delegate(e);
		}
	}

}
