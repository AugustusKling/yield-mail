package yield.mail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import yield.config.ConfigReader;
import yield.config.TypedYielder;
import yield.core.EventQueue;
import yield.core.EventType;

public class ParameterParsingTest {
	@Test(expected = IllegalArgumentException.class)
	public void missingRecipientFails() {
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, new TypedYielder(new EventType(
				String.class), new EventQueue<>(String.class)));
		MailFunction mailFunction = new MailFunction();

		mailFunction.getSource("", context);
	}

	@Test
	public void simpleParamsAccepted() {
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, new TypedYielder(new EventType(
				String.class), new EventQueue<>(String.class)));
		MailFunction mailFunction = new MailFunction();

		mailFunction.getSource("to=\"test@dummy.example\"", context);
		mailFunction
				.getSource(
						"cc=\"test2@dummy.example\" to=\"test@dummy.example\"",
						context);
	}

	@Test
	public void quotedParamsAccepted() {
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, new TypedYielder(new EventType(
				String.class), new EventQueue<>(String.class)));
		MailFunction mailFunction = new MailFunction();

		mailFunction.getSource("\"to\"=\"test@dummy.example\"", context);
		mailFunction.getSource(
				"cc=\"test2@dummy.example\" \"to\"=\"test@dummy.example\"",
				context);
		mailFunction.getSource(
				"to=\"test@dummy.example\" \"mail.smtp.host\"=\"localhost\"",
				context);
	}

	@Test
	public void withDotsAccepted() {
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, new TypedYielder(new EventType(
				String.class), new EventQueue<>(String.class)));
		MailFunction mailFunction = new MailFunction();

		mailFunction.getSource(
				"to=\"test@dummy.example\" mail.smtp.host=\"localhost\"",
				context);
	}
}
