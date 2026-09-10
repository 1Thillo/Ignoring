package org.stellium.ignoring.chat;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Works out who wrote a chat message, and whether the message was private.
 *
 * <p>Chat plugins format messages server side and send the result as plain system chat, so
 * the chat type that separates a public message from a whisper never reaches the client.
 * Reading the sender out of the text would mean guessing where the name ends and the rank,
 * the nickname or the message itself begins.
 *
 * <p>There is a better anchor. Plugins make the name clickable so that clicking it starts a
 * whisper, and that click carries the command with the name already filled in. The name in
 * {@code /msg SomePlayer} is the plugin's own answer to "who is this", exact and unambiguous.
 *
 * <p>Public and private messages differ in how much text that clickable element covers.
 * On a public message only the bare name is clickable:
 *
 * <pre>[Rank] SomePlayer » hello
 *        ^^^^^^^^^^ carries /msg SomePlayer</pre>
 *
 * <p>On a private message the whole header is clickable, both partners and the arrow between
 * them:
 *
 * <pre>[You » SomePlayer] hello
 * ^^^^^^^^^^^^^^^^^^ carries /msg SomePlayer</pre>
 *
 * <p>So if the clickable text is more than just the name, the message is private. That test
 * needs no knowledge of the server's arrow glyph, bracket style or language, which matters
 * because the word for "you" in a whisper header is translated per server.
 *
 * <p>It does assume the shown name and the account name differ at most in capitalisation. A
 * server that displays a free-form nickname while whispering to the account name would make a
 * public line look like a header, and it would be treated as private.
 */
public final class ChatSenderResolver {

    /** Command names that start a private conversation, as plugins tend to spell them. */
    private static final List<String> WHISPER_COMMANDS =
        List.of("msg", "tell", "w", "whisper", "pm", "m", "message", "dm", "t", "r", "reply");

    private ChatSenderResolver() {
    }

    /**
     * @return the other party of the message and whether it was private, or empty when the
     *         message carries no clickable name at all, as with server broadcasts
     */
    public static Optional<ChatSender> resolve(Component message) {
        return message == null ? Optional.empty() : find(message);
    }

    private static Optional<ChatSender> find(Component node) {
        String command = commandOf(node.getStyle().getClickEvent());
        if (command != null) {
            String name = whisperTarget(command);
            if (name != null) {
                // Only the name itself means a public message; a whole header means a whisper.
                // Compared without case, because a server may show GFiti for the account Gfiti.
                boolean privateMessage = !node.getString().trim().equalsIgnoreCase(name);
                return Optional.of(new ChatSender(name, privateMessage));
            }
        }

        for (Component sibling : node.getSiblings()) {
            Optional<ChatSender> found = find(sibling);
            if (found.isPresent()) {
                return found;
            }
        }

        return Optional.empty();
    }

    private static String commandOf(ClickEvent click) {
        return switch (click) {
            case ClickEvent.SuggestCommand suggest -> suggest.command();
            case ClickEvent.RunCommand run -> run.command();
            case null, default -> null;
        };
    }

    /** Pulls the player name out of a command like {@code /msg SomePlayer } . */
    private static String whisperTarget(String command) {
        String[] parts = command.strip().split("\s+");
        if (parts.length < 2) {
            return null;
        }

        String verb = parts[0];
        if (verb.startsWith("/")) {
            verb = verb.substring(1);
        }

        // Plugins sometimes qualify their commands, as in /essentials:msg.
        int namespace = verb.indexOf(':');
        if (namespace >= 0) {
            verb = verb.substring(namespace + 1);
        }

        if (!WHISPER_COMMANDS.contains(verb.toLowerCase(Locale.ROOT))) {
            return null;
        }

        String name = parts[1];
        return name.isBlank() ? null : name;
    }
}
