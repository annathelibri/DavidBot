/*
 * This class was created by <AdrianTodt>. It's distributed as
 * part of the DavidBot. Get the Source Code in github:
 * https://github.com/adriantodt/David
 *
 * DavidBot is Open Source and distributed under the
 * GNU Lesser General Public License v2.1:
 * https://github.com/adriantodt/David/blob/master/LICENSE
 *
 * File Created @ [08/10/16 11:26]
 */

package cf.adriantodt.bot.data;

import cf.adriantodt.bot.Bot;
import cf.adriantodt.bot.utils.Tasks;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rethinkdb.model.MapObject;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.time.format.DateTimeFormatter;
import java.util.*;

import static cf.adriantodt.bot.data.DataManager.conn;
import static cf.adriantodt.bot.data.DataManager.r;
import static cf.adriantodt.bot.data.I18n.getLocalized;
import static cf.adriantodt.bot.data.ReturnHandler.h;
import static cf.adriantodt.bot.utils.Utils.nnOrD;

public class Users {
	private static List<Data> all = new ArrayList<>();
	private static Map<User, Data> userMap = new HashMap<>();
	private static Map<Data, Integer> timeoutUntilDbRemoval = new HashMap<>();

	static {

		Tasks.startAsyncTask(() -> {
			timeoutUntilDbRemoval.replaceAll((guild, integer) -> Math.min(integer - 1, 0));
			timeoutUntilDbRemoval.entrySet().stream().filter(entry -> entry.getValue() == 0).map(Map.Entry::getKey).forEach(data -> {
				//TODO IMPL DB REMOVAL
				timeoutUntilDbRemoval.remove(data);
			});
		}, 60);
	}

	public static List<Data> all() {
		return Collections.unmodifiableList(all);
	}

	public static void loadAll() {
		//TODO IMPLEMENT
		h.query(r.table("guilds").getAll().run(conn)).list().getAsJsonArray().forEach(Users::unpack);
	}

	private static Data unpack(JsonElement element) {
		JsonObject object = element.getAsJsonObject();
		Data data = all.stream().filter(dataPredicate -> object.get("id").getAsString().equals(dataPredicate.id)).findFirst().orElseGet(Data::new);
		data.id = object.get("id").getAsString();
		data.lang = object.get("lang").getAsString();
		userMap.put(data.getUser(), data);
		if (data.getUser() == null) {
			timeoutUntilDbRemoval.put(data, 5);
		}
		return data;
	}

	@SubscribeEvent
	public static void newUser(GuildMemberJoinEvent e) {
		Data data = fromDiscord(e.getMember().getUser());
		if (timeoutUntilDbRemoval.containsKey(data)) timeoutUntilDbRemoval.remove(data);
	}

	@SubscribeEvent
	public static void byeUser(GuildMemberLeaveEvent e) {
		if (e.getJDA().getGuilds().stream().anyMatch(guild -> guild != e.getGuild() && guild.isMember(e.getMember().getUser())))
			return;
		timeoutUntilDbRemoval.put(fromDiscord(e.getMember().getUser()), 5);
	}

	public static Data fromDiscord(User user) {
		if (userMap.containsKey(user)) {
			return userMap.get(user);
		} else {
			Data data = new Data();
			userMap.put(user, data);
			data.id = user.getId();

			MapObject m =
				r.hashMap("id", data.id)
					.with("lang", data.lang);

			r.table("users").insert(m).runNoReply(conn);

			return data;
		}
	}

	public static Data fromId(String id) {
		for (Data g : all) {
			if (g.id.equals(id)) return g;
		}

		return null;
	}

	public static Data fromDiscord(GuildMessageReceivedEvent event) {
		return fromDiscord(event.getAuthor());
	}

	public static String toString(Data data, JDA jda, String language, Guild guildAt) {
		User user = data.getUser(jda);
		Member member = data.getMember(guildAt);
		if (member == null) throw new RuntimeException("User doesn't belong to the Guild.");
		return getLocalized("user.name", language) + ": " + user.getName() + "\n" +
			getLocalized("user.nick", language) + ": " + (member.getNickname() == null ? "(" + getLocalized("user.none", language) + ")" : member.getNickname()) + "\n" +
			getLocalized("user.roles", language) + ": " + nnOrD(String.join(", ", member.getRoles().stream().map(Role::getName).toArray(String[]::new)), "(" + getLocalized("user.none", language) + ")") + "\n" +
			getLocalized("user.memberSince", language) + ": " + member.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\n" +
			getLocalized("user.commonGuilds", language) + ": " + nnOrD(String.join(", ", jda.getGuilds().stream().filter(guild -> guild.isMember(user)).map(Guild::getName).toArray(String[]::new)), "(" + getLocalized("user.none", language) + ")") + "\n" +
			"ID: " + user.getId() + "\n" +
			getLocalized("user.status", language) + ": " + member.getOnlineStatus() + "\n" +
			getLocalized("user.playing", language) + ": " + (member.getGame() == null ? "(" + getLocalized("user.none", language) + ")" : member.getGame().toString());
	}

	public static class Data {
		private String id = "-1", lang = "en_US";

		private static void pushUpdate(Users.Data data, MapObject changes) {
			r.table("users").get(data.id).update(arg -> changes).runNoReply(conn);
		}

		public String getId() {
			return id;
		}

		public String getLang() {
			return lang;
		}

		public void setLang(String lang) {
			this.lang = lang;
			pushUpdate(this, r.hashMap("lang", lang));
		}

		public long getUserPerms(Guilds.Data data) {
			return data.getUserPerms(id);
		}

		public long getUserPerms(Guilds.Data data, long orDefault) {
			return data.getUserPerms(id, orDefault);
		}

		public void setUserPerms(Guilds.Data data, long userPerms) {
			data.setUserPerms(id, userPerms);
		}

		public long getUserPerms(Guild guild) {
			return getUserPerms(Guilds.fromDiscord(guild));
		}

		public long getUserPerms(Guild guild, long orDefault) {
			return getUserPerms(Guilds.fromDiscord(guild), orDefault);
		}

		public void setUserPerms(Guild guild, long userPerms) {
			setUserPerms(Guilds.fromDiscord(guild), userPerms);
		}

		public User getUser(JDA jda) {
			return jda.getUserById(id);
		}

		public User getUser() {
			return getUser(Bot.API);
		}

		public Member getMember(Guilds.Data data, JDA jda) {
			return getMember(data.getGuild(jda));
		}

		public Member getMember(Guilds.Data data) {
			return getMember(data.getGuild(Bot.API));
		}

		public Member getMember(Guild guild) {
			return guild == null ? null : guild.getMember(getUser(guild.getJDA()));
		}
	}
}