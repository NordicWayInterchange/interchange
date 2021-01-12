package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFilterable;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import org.apache.qpid.server.filter.*;
import org.apache.qpid.server.filter.selector.ParseException;
import org.apache.qpid.server.filter.selector.SelectorParser;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JMSSelectorFilterFactoryTest {
	private static Logger logger = LoggerFactory.getLogger(JMSSelectorFilterFactoryTest.class);

	//equals without quote seems to be matching one header against another
	@Test
	public void mathcingWithoutSingleQuotes() {
		assertThatExceptionOfType(HeaderNotFoundException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry = NO");
		});
	}

	@Test
	public void mathcingOriginatingCountry() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("originatingCountry = 'NO'")).isTrue();
	}

	@Test
	public void mathcingUnknownAttributeIsNotValid() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("illegalAttribute = 'NO'")).isFalse();
	}

	@Test
	public void mathingWildcardWithoutSingleQuotes() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("messageType like datex%");
		});
	}

	@Test
	public void expirationFilteringIsNotSupported() {
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("JMSExpiration > 3");
		});
	}

	@Test
	public void unknownHeaderAttributeNotAccepted() {
		assertThatExceptionOfType(HeaderNotFoundException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("region like 'some region%'");
		});
	}

	@Test
	public void alwaysTrueIsNotAccepted() {
		assertThatExceptionOfType(SelectorAlwaysTrueException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("messageType like 'spat%' or 1=1");
		});
	}

	@Test
	public void likeAnyStringIsAlwaysTrueHenceNotAccepted() {
		assertThatExceptionOfType(SelectorAlwaysTrueException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry like '%'");
		});
	}

	@Test
	public void invalidSyntaxIsNotAccepted() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("messageType flike 'spat%'");
		});
	}

	@Test
	public void filterWithDoubleQuotedStringValueIsNotAllowed() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry = \"NO\"");
		});
	}

	@Test
	public void testMinusOneFiter() {
		assertThatExceptionOfType(SelectorAlwaysTrueException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry like '-1%'");
		});
	}

	@Test
	public void testSelectorWithBackTick() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry like `NO`");
		});
	}

	@Test
	public void latitudeIsNotPossibleToFilterOn() {
		assertThatExceptionOfType(HeaderNotFilterable.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("latitude > 1");
		});
	}

	@Test
	public void longitudeIsNotPossibleToFilterOn() {
		assertThatExceptionOfType(HeaderNotFilterable.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("longitude > 1");
		});
	}

	@Test
	public void timestampIsNotPossibleToFilterOn() {
		assertThatExceptionOfType(HeaderNotFilterable.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("timestamp > 1");
		});
	}

	@Test
	public void emptySelectorWillMatchEverythingAndThereforeNotAllowed() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("")).isFalse();
	}

	@Test
	public void parseJmsSelectorAndUseQpidExpressionsToEvaluateCapabilityMatch() throws ParseException{
		String selector = "messageType = 'DATEX2' AND quadTree LIKE '%,123456%' OR quadTree LIKE '%,1256%'";
		JMSSelectorFilter jmsSelectorFilter = JMSSelectorFilterFactory.get(selector);
		System.out.println(jmsSelectorFilter.toString());

		SelectorParser<FilterableMessage> selectorParser = new SelectorParser<>();
		selectorParser.setPropertyExpressionFactory(JMSMessagePropertyExpression.FACTORY);
		Expression<FilterableMessage> parse = selectorParser.parse(selector);
		debugExpression(parse);
	}


	private void debugExpression(Expression<FilterableMessage> expression) {
		if (expression instanceof ComparisonExpression) {
			System.out.println("Comparison: " + expression.toString());
		} else if (expression instanceof UnaryExpression) {
			UnaryExpression<FilterableMessage> unaryExpression = (UnaryExpression<FilterableMessage>) expression;
			String expressionClassName = unaryExpression.getClass().getSimpleName();
			if (expressionClassName.startsWith("LikeExpression")) {
				Pattern pattern = getLikePattern(expression);
				System.out.printf("LikeExpression: pattern %s, symbol:%s toString:%s %n", pattern, unaryExpression.getExpressionSymbol(), expression.toString());
			}
			System.out.println("Unary: " + expression.toString());
		} else if (expression instanceof BinaryExpression) {
			BinaryExpression<FilterableMessage> binaryExpression = (BinaryExpression<FilterableMessage>) expression;
			debugExpression(binaryExpression.getLeft());
			debugExpression(binaryExpression.getRight());
		}
	}

	private Pattern getLikePattern(Expression<FilterableMessage> expression)  {
		Pattern pattern = null;
		try {
			Class<?> likeExpressionClass = Class.forName("org.apache.qpid.server.filter.ComparisonExpression$LikeExpression");
			Field likePattern = likeExpressionClass.getField("likePattern");
			likePattern.setAccessible(true);
			pattern = (Pattern) likePattern.get(expression);
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return pattern;
	}
}

