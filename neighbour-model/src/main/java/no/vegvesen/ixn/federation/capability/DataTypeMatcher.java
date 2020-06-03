package no.vegvesen.ixn.federation.capability;

/*-
 * #%L
 * neighbour-model
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
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
 * #L%
 */

import no.vegvesen.ixn.federation.model.DataType;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
@Component
public class DataTypeMatcher {
	public static Set<DataType> calculateCommonInterest(Set<DataType> s1, Set<DataType> s2) {
		return s1.stream()
				.filter(dt1 -> s2.stream().anyMatch(dt1::matches))
				.collect(Collectors.toSet());
	}
}
