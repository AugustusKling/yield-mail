package yield.mail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.mail.internet.AddressException;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.Yielder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MailFunction extends FunctionConfig {

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		ObjectNode arguments = parseArguments(args);

		Properties mailSettings = new Properties();
		String prefix = null;
		List<String> to = null;
		List<String> cc = null;
		String password = null;
		String username = null;

		Iterator<Entry<String, JsonNode>> fields = arguments.fields();
		while (fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();
			String key = field.getKey();
			String value = field.getValue().textValue();

			if (key.startsWith("mail.")) {
				mailSettings.setProperty(key, value);
			} else if ("to".equals(key)) {
				to = Arrays.asList(value.split("\\s*,\\s*"));
			} else if ("cc".equals(key)) {
				cc = Arrays.asList(value.split("\\s*,\\s*"));
			} else if ("prefix".equals(key)) {
				prefix = value;
			} else if ("username".equals(key)) {
				username = value;
			} else if ("password".equals(key)) {
				password = value;
			} else {
				throw new IllegalArgumentException("Configuration option "
						+ key + " not recognized.");
			}
		}

		if (to == null) {
			throw new IllegalArgumentException("Missing email recipient.");
		}
		if (cc == null) {
			cc = Collections.emptyList();
		}

		Yielder<String> input = getYielderTypesafe(String.class,
				ConfigReader.LAST_SOURCE, context);
		try {
			Mailer mailer = new Mailer(mailSettings, to, cc, prefix, username,
					password);
			input.bind(mailer);
		} catch (AddressException e) {
			throw new IllegalArgumentException("Recipient address invalid.");
		}
		return wrapResultingYielder(input);
	}

}
