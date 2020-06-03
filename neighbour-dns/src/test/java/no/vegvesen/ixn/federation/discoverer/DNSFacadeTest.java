package no.vegvesen.ixn.federation.discoverer;

/*-
 * #%L
 * neighbour-dns
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

import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {DNSFacade.class, DNSProperties.class})
@EnableConfigurationProperties
public class DNSFacadeTest {

	@Autowired
	private DNSFacade dnsFacade;

	@Test
	public void testNumberOfNeighbours(){
		List<Neighbour> neighbours = dnsFacade.getNeighbours();
		assertThat(neighbours)
				.withFailMessage("Number of known Neighbours in the actual dns is less than two")
				.hasSizeGreaterThan(2);
	}

	@Test
	public void testThatDiscoveredNeighboursAreNotNull(){
		List<Neighbour> neighbours = dnsFacade.getNeighbours();

		for(Neighbour i : neighbours){
			assertThat(i).isNotNull();
		}
	}

	@Test
	public void bouvetNodePresent() {
		Neighbour bouvet = null;
		for (Neighbour neighbour : dnsFacade.getNeighbours()) {
			if (neighbour.getName().equals("bouveta-fed.itsinterchange.eu")){
				bouvet = neighbour;
			}
		}
		assertThat(bouvet).isNotNull();
		assertThat(bouvet.getControlChannelUrl("/")).isNotNull();
		assertThat(bouvet.getMessageChannelPort()).isNotNull().isEqualTo("5671");
	}

}
