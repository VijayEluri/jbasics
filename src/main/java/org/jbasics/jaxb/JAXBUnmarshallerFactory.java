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
package org.jbasics.jaxb;

import org.jbasics.exception.DelegatedException;
import org.jbasics.pattern.delegation.Delegate;
import org.jbasics.pattern.factory.Factory;
import org.jbasics.types.delegates.LazySoftReferenceDelegate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class JAXBUnmarshallerFactory implements Factory<Unmarshaller> {
	private final Delegate<JAXBContext> jaxbContextDelegate;

	public JAXBUnmarshallerFactory(final Delegate<JAXBContext> contextDelegate) {
		if (contextDelegate == null) {
			throw new IllegalArgumentException("Null parameter: contextDelegate");
		}
		this.jaxbContextDelegate = contextDelegate;
	}

	public JAXBUnmarshallerFactory(final Factory<JAXBContext> contextFactory) {
		if (contextFactory == null) {
			throw new IllegalArgumentException("Null parameter: contextFactory");
		}
		this.jaxbContextDelegate = new LazySoftReferenceDelegate<JAXBContext>(contextFactory);
	}

	public Unmarshaller newInstance() {
		JAXBContext ctx = this.jaxbContextDelegate.delegate();
		if (ctx == null) {
			throw new IllegalStateException("Cannot create unmarshaller since jaxb context delegate returns null");
		}
		try {
			return ctx.createUnmarshaller();
		} catch (JAXBException e) {
			throw DelegatedException.delegate(e);
		}
	}
}
