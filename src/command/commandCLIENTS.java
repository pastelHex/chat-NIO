package command;

import java.nio.channels.SocketChannel;

import model.client;
import model.server;
import model.structures;

public class commandCLIENTS implements commandChain {

	private commandChain next;
	private String cmdEnum;

	public commandCLIENTS() {
		cmdEnum = "@" + commands.CONTACTS.toString();
	}

	@Override
	public void interpretCommand(client c, String cmd) {
		if (cmd.startsWith(cmdEnum)) {
			String[] splitCmd = cmd.split(";");
			for (int i = 1; i < splitCmd.length; i++) {
				String[] splitClient = splitCmd[i].split(":");
				try {
					c.clientNames.put(Integer.parseInt(splitClient[0]), splitClient[1]);
				} catch (Exception ex) {
					System.out.println("bleh");
				}
				c.controll.handlerGotClientList();
			}
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
			String clientList = cmdEnum + ";" + s.getClients();
			s.write(sc, clientList);
		} else {
			if (next != null)
				next.interpretCommandForServer(s, sc, cmd);
		}
	}

}
