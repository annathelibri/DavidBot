/*
 * This class was created by <AdrianTodt>. It's distributed as
 * part of the DavidBot. Get the Source Code in github:
 * https://github.com/adriantodt/David
 *
 * DavidBot is Open Source and distributed under the
 * GNU Lesser General Public License v2.1:
 * https://github.com/adriantodt/David/blob/master/LICENSE
 *
 * File Created @ [05/11/16 22:51]
 */

package cf.adriantodt.David.modules.init;

import cf.adriantodt.David.commands.base.Holder;
import cf.adriantodt.David.modules.cmds.Feeds;
import cf.adriantodt.David.modules.cmds.Pushes;
import cf.adriantodt.David.modules.gui.impl.QueueLogAppender;
import cf.adriantodt.David.modules.rest.RESTInterface;
import cf.adriantodt.utils.TaskManager;
import cf.adriantodt.utils.ThreadBuilder;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

import static cf.adriantodt.David.modules.cmds.MakeCommandManagerAModule.TooFast.userTimeout;
import static cf.adriantodt.utils.TaskManager.startAsyncTask;

public class MergeTasksWithInitModule {



	public static void startAsyncTasks() {


		startAsyncTask("User Timeout", () -> {
			synchronized (userTimeout) {
				userTimeout.replaceAll((user, integer) -> Math.max(0, integer - 1));
			}
		}, 5);
	}

	public static void startJDAAsyncTasks() {
		TaskManager.startAsyncTask("Feed Main Task", Feeds::loop, 5);

		new ThreadBuilder().setDaemon(true).setName("Web-Interface").build(() -> new Thread(RESTInterface::startWebServer)).start();

		new ThreadBuilder().setDaemon(true).setName("Log4j2Discord").build(() -> new Thread(() -> {
			System.out.println("Log4j2Discord Enabled!");
			Holder<String> s = new Holder<>();
			while ((s.var = QueueLogAppender.getNextLogEvent("DiscordLogListeners")) != null) {
				Pushes.pushSimple("get", channel -> "[LOG] " + s.var);
			}
			System.out.println("Log4j2Discord Disabled...");
		})).start();
	}
}