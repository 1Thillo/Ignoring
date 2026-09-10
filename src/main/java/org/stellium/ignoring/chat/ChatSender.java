package org.stellium.ignoring.chat;

/**
 * The player a chat message is a conversation with: the writer of a public message or of an
 * incoming whisper, and the recipient of a whisper sent by this client.
 */
public record ChatSender(String name, boolean privateMessage) {
}
