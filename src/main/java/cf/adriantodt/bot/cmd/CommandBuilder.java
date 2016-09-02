/*
 * This class was created by <AdrianTodt>. It's distributed as
 * part of the DavidBot. Get the Source Code in github:
 * https://github.com/adriantodt/David
 *
 * DavidBot is Open Source and distributed under the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3:
 * https://github.com/adriantodt/David/blob/master/LICENSE
 *
 * File Created @ [02/09/16 07:55]
 */

package cf.adriantodt.bot.cmd;

import cf.adriantodt.bot.guild.DiscordGuild;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandBuilder {
	private TriConsumer<DiscordGuild, String, MessageReceivedEvent> action = null;
	private Supplier<Long> permProvider = () -> 0L;
	private Supplier<String> usageProvider = () -> null;

	public CommandBuilder setAction(TriConsumer<DiscordGuild, String, MessageReceivedEvent> consumer) {
		action = consumer;
		return this;
	}

	public CommandBuilder setAction(BiConsumer<String, MessageReceivedEvent> consumer) {
		action = (guild, s, event) -> consumer.accept(s, event);
		return this;
	}

	public CommandBuilder setAction(Consumer<MessageReceivedEvent> consumer) {
		action = (guild, s, event) -> consumer.accept(event);
		return this;
	}

	public CommandBuilder setAction(Runnable runnable) {
		action = (guild, s, event) -> runnable.run();
		return this;
	}

	public CommandBuilder setPermRequired(long value) {
		permProvider = () -> value;
		return this;
	}

	public CommandBuilder setUsage(String usage) {
		usageProvider = () -> usage;
		return this;
	}

	public CommandBuilder setPermRequired(Supplier<Long> provider) {
		permProvider = provider;
		return this;
	}

	public CommandBuilder setUsage(Supplier<String> provider) {
		usageProvider = provider;
		return this;
	}

	public ICommand build() {
		return new ICommand() {
			@Override
			public void run(DiscordGuild guild, String arguments, MessageReceivedEvent event) {
				action.accept(guild, arguments, event);
			}

			@Override
			public long retrievePerm() {
				return permProvider.get();
			}

			@Override
			public String retrieveUsage() {
				return usageProvider.get();
			}
		};
	}
}
