package command;

import java.nio.channels.SocketChannel;

import model.client;
import model.server;

public interface commandChain {
	// not make sense in this structure, im not doing this
	// public String generateCommand(String... args);

	public void interpretCommand(client c, String args);

	public void interpretCommandForServer(server s, SocketChannel sc, String cmd);

	public void setNext(commandChain c);
}
