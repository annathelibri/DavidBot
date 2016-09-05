/*
 * This class was created by <AdrianTodt>. It's distributed as
 * part of the DavidBot. Get the Source Code in github:
 * https://github.com/adriantodt/David
 *
 * DavidBot is Open Source and distributed under the
 * GNU Lesser General Public License v2.1:
 * https://github.com/adriantodt/David/blob/master/LICENSE
 *
 * File Created @ [02/09/16 08:18]
 */

package cf.adriantodt.bot;

import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Date;

import static cf.adriantodt.bot.Answers.announce;
import static cf.adriantodt.bot.Answers.sendCased;
import static cf.adriantodt.bot.Utils.cpuUsage;

public class Statistics {
	public static Date startDate = null;
	public static int loads = 0, saves = 0, crashes = 0, noperm = 0, invalidargs = 0, msgs = 0, cmds = 0, wgets = 0, toofasts = 0, musics = 0;

	public static String calculate(Date startDate, Date endDate) {

		//milliseconds
		long different = endDate.getTime() - startDate.getTime();

		if (different <= 0) {
			return "Já saiu! *Vai jogar!* **Agora!**";
		}

		different = different / 1000;
		long minutesInMilli = 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different;

		return String.format(
			"%d dias, %d horas, %d minutos, %d segundos",
			elapsedDays,
			elapsedHours, elapsedMinutes, elapsedSeconds);

	}

	public static int parseInt(String s, int onCatch) {
		try {
			return Integer.parseInt(s);
		} catch (Exception ignored) {
		}
		return onCatch;
	}

	public static void printStats(MessageReceivedEvent event) {
		int mb = 1024 * 1024;
		Runtime instance = Runtime.getRuntime();
		announce(event, "Estatísticas da Última sessão");
		sendCased(event,
			"- Ligado à " + Statistics.calculate(Statistics.startDate, new Date())
				+ "\n - " + Statistics.msgs + " mensagens enviadas"
				+ "\n - " + Statistics.cmds + " comandos executados"
				+ "\n - " + Statistics.crashes + " crashes ocorreram"
				+ "\n - " + Statistics.toofasts + " comandos bloqueados por SpamDetection"
				+ "\n - " + Statistics.wgets + " solicitações Web"
				+ "\n - " + Statistics.musics + " músicas tocadas"
				+ "\n - " + Thread.activeCount() + " threads ativas"
				+ "\n - Sem Permissão: " + Statistics.noperm + " / Argumentos Invalidos: " + Statistics.invalidargs
				+ "\n - Saves: " + Statistics.saves + " / Loads: " + Statistics.loads
				+ "\n - Guilds conhecidas: " + Bot.API.getGuilds().size()
				+ "\n - Canais conhecidos: " + Bot.API.getTextChannels().size()
				+ "\n - Uso de RAM(Usando/Total/Máximo): " + ((instance.totalMemory() - instance.freeMemory()) / mb) + " MB/" + (instance.totalMemory() / mb) + " MB/" + (instance.maxMemory() / mb) + " MB"
				+ "\n - Uso de CPU: " + (Math.floor(cpuUsage * 10000) / 100) + "%"
			, "");
	}
}
