package command;

import java.nio.channels.SocketChannel;

import model.client;
import model.server;
import model.structures;

public class commandGET implements commandChain {

	/*
	 * @Override public String generateCommand(String... args) { String cmd = return
	 * null; }
	 */

	private commandChain next;
	private String cmdEnum;

	public commandGET() {
		this.cmdEnum = "@" + commands.GET.toString();
	}

	@Override
	public void interpretCommand(client c, String cmd) {
		if (cmd.startsWith(cmdEnum)) {
			String[] splitCmd = cmd.split(";");
			int id = Integer.parseInt(splitCmd[1]);
			String nazwa = splitCmd[2];
			int port = Integer.parseInt(splitCmd[3]);
			System.out.println("client:connecting to " + port);
			c.registerContact(port, structures.CLIENT);
			c.currentFriendoID = id;
		} else {
			if (next != null)
				next.interpretCommand(c, cmd);
		}
	}

	@Override
	public void setNext(commandChain c) {
		this.next = c;
	}

	@Override
	public void interpretCommandForServer(server s, SocketChannel sc, String cmd) {
		if (cmd.startsWith(cmdEnum)) {
			String[] splitCmd = cmd.split(";");
			int id = Integer.parseInt(splitCmd[1]);
			String nazwa = splitCmd[2];
			int port = s.clientsPorts.get(id);
			s.write(sc, cmdEnum + ";" + id + ";" + nazwa + ";" + port + ";");
		} else {
			if (next != null)
				next.interpretCommandForServer(s, sc, cmd);
		}
	}

}
