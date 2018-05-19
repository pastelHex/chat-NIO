package command;

import java.nio.channels.SocketChannel;

import model.client;
import model.server;

public class commandBYE implements commandChain {

	private commandChain next;
	private String cmdEnum;

	public commandBYE() {
		this.cmdEnum = "@" + commands.BYE.toString();
	}

	@Override
	public void interpretCommand(client c, String args) {
		
	}

	@Override
	public void interpretCommandForServer(server s, SocketChannel sc, String cmd) {
		
	}

	@Override
	public void setNext(commandChain c) {
		this.next=c;
	}

}
