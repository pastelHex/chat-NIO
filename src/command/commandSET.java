package command;

import java.nio.channels.SocketChannel;

import model.client;
import model.server;

public class commandSET implements commandChain {

	private commandChain next;
	private String cmdEnum;

	public commandSET() {
		this.cmdEnum = "@" + commands.SET.toString();
	}

	@Override
	public void interpretCommand(client c, String args) {
		// TODO Auto-generated method stub
	}

	@Override
	public void interpretCommandForServer(server s, SocketChannel sc, String cmd) {
		if (cmd.startsWith(cmdEnum)) {
			String[] splitCmd = cmd.split(";");
			int number = Integer.parseInt(splitCmd[1]);
			int porte = Integer.parseInt(splitCmd[3]);
			s.clientsChannels.put(sc, number);
			s.clientsNames.put(number, splitCmd[2]);
			s.clientsPorts.put(number, porte);
			s.write(sc, "@OK;");
		} else {
			if (next != null)
				next.interpretCommandForServer(s, sc, cmd);
		}
	}

	@Override
	public void setNext(commandChain c) {
		this.next = c;
	}

}
