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
package org.jbasics.types.strategy;

import org.jbasics.checker.ContractCheck;
import org.jbasics.exception.DelegatedException;
import org.jbasics.pattern.delegation.Delegate;
import org.jbasics.pattern.delegation.ReleasableDelegate;
import org.jbasics.pattern.resolver.Resolver;
import org.jbasics.pattern.strategy.ContextualSubstitutionStrategy;
import org.jbasics.pattern.strategy.SubstitutionStrategy;
import org.jbasics.types.delegates.UnmodifiableDelegate;
import org.jbasics.types.resolver.SystemPropertyResolver;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertySubstitutionStrategy implements SubstitutionStrategy<CharSequence, CharSequence>,
		ContextualSubstitutionStrategy<CharSequence, CharSequence, Properties> {
	public static final Pattern STANDARD_SUBSTITUTION_PATTERN;
	public static final PropertySubstitutionStrategy SHARED_SYSTEM_PROPERTY;

	public static final int STANDARD_KEY_GROUP_NUMBER = 1;
	public static final int STANDARD_DEFAULT_GROUP_NUMBER = 3;

	private final Delegate<? extends Resolver<String, String>> valueResolver;
	private final Pattern pattern;
	private final int keyGroupNumber;
	private final int defaultGroupNumber;

	static {
		// It is critical that the pattern is initialized first because it is already used in the constructor!
		// Using the initializer allows us to make sure we are always initialized in the right order.
		// Since it can differ on JVM if already initialized on attribute definition
		STANDARD_SUBSTITUTION_PATTERN = Pattern.compile("\\$\\{(.*?)(:(.*?))?\\}"); //$NON-NLS-1$
		SHARED_SYSTEM_PROPERTY = new PropertySubstitutionStrategy();
	}

	public PropertySubstitutionStrategy() {
		this(SystemPropertyResolver.SHARED_INSTANCE_DELEGATE);
	}

	public PropertySubstitutionStrategy(final Delegate<? extends Resolver<String, String>> valueResolver) {
		this(valueResolver, PropertySubstitutionStrategy.STANDARD_SUBSTITUTION_PATTERN, PropertySubstitutionStrategy.STANDARD_KEY_GROUP_NUMBER,
				PropertySubstitutionStrategy.STANDARD_DEFAULT_GROUP_NUMBER);
	}

	public PropertySubstitutionStrategy(final Delegate<? extends Resolver<String, String>> valueResolver, final Pattern pattern,
										final int keyGroupNumber,
										final int defaultGroupNumber) {
		this.valueResolver = ContractCheck.mustNotBeNull(valueResolver, "valueResolver"); //$NON-NLS-1$
		this.pattern = ContractCheck.mustNotBeNull(pattern, "pattern"); //$NON-NLS-1$
		this.keyGroupNumber = ContractCheck.mustBeInRange(keyGroupNumber, 1, 10, "keyGroupNumber"); //$NON-NLS-1$
		this.defaultGroupNumber = ContractCheck.mustBeInRange(defaultGroupNumber, 0, 10, "defaultGroupNumber"); //$NON-NLS-1$
	}

	public PropertySubstitutionStrategy(final Resolver<String, String> valueResolver) {
		this(new UnmodifiableDelegate<Resolver<String, String>>(valueResolver));
	}

	public CharSequence substitute(final CharSequence input) {
		try {
			return appendSubstituted(input, null, new StringBuilder());
		} catch (final IOException e) {
			throw DelegatedException.delegate(e);
		}
	}

	public CharSequence substitute(final CharSequence input, final Properties defaultValues) {
		try {
			return appendSubstituted(input, defaultValues, new StringBuilder());
		} catch (final IOException e) {
			throw DelegatedException.delegate(e);
		}
	}

	public <T extends Appendable> T appendSubstituted(final CharSequence request, final Properties defaultValues, final T appendable)
			throws IOException {
		try {
			ContractCheck.mustNotBeNull(appendable, "appendable"); //$NON-NLS-1$
			if (request == null || request.length() == 0) {
				return appendable;
			}
			final Resolver<String, String> resolver = this.valueResolver.delegate();
			if (resolver == null) {
				throw new IllegalStateException("Resolver delegate returns null as delegated instance but it is required to be not null"); //$NON-NLS-1$
			}
			final Matcher m = this.pattern.matcher(request);
			int i = 0;
			while (m.find(i)) {
				if (i < m.start()) {
					appendable.append(request, i, m.start());
				}
				final String key = m.group(this.keyGroupNumber);
				String defaultValue = this.defaultGroupNumber > 0 ? m.group(this.defaultGroupNumber) : null;
				if (defaultValues != null) {
					defaultValue = defaultValues.getProperty(key, defaultValue);
				}
				String value = resolver.resolve(key, defaultValue);
				if (value == null) {
					value = defaultValue;
				}
				if (value != null) {
					appendSubstituted(value, defaultValues, appendable);
				}
				i = m.end();
			}
			appendable.append(request, i, request.length());
			return appendable;
		} finally {
			if (this.valueResolver instanceof ReleasableDelegate<?>) {
				((ReleasableDelegate<?>) this.valueResolver).release();
			}
		}
	}
}
