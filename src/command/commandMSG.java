package command;

import java.nio.channels.SocketChannel;

import model.client;
import model.server;

public class commandMSG implements commandChain {

	private commandChain next;

	@Override
	public void interpretCommand(client c, String cmd) {
		String commandEnum = "@" + commands.MSG.toString();
		if (cmd.startsWith(commandEnum)) {
			String[] splitCmd = cmd.split(";");
			c.addMessage(false, splitCmd[1]);
			c.controll.handlerGotMsg();
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
		// TODO Auto-generated method stub
	}

}
