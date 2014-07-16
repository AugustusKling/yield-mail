yield-mail
==========

Extension function for yield to forward events via email.

Example usage:
```
function mail yield.mail.MailFunction "file:/tmp/yield-mail.jar"

# Watch a file for changes.
watch "/var/log/sample.log"
# Merge indented lines.
combine
# Read lines as JSON object or convert them if the former fails.
toJSON
# Apply a regular expression to split up the log event.
grok message ^(?<time>[^ ]+) (?<level>\w+)\s+\[(?<module>[^\]]+)\] (?<message>.+)$
# Discard everything but errors.
where level="ERROR"

# Convent JSON to text.
toText Error encountered in ${module}: ${message}
# Send remaining events.
mail to="dev@host.sample" mail.smtp.host="localhost"
```
