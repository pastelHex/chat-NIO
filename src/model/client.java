package model;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import command.commands;
import controller.*;

public class client {

	private static int id = 1;
	private int ID;
	private int serverPort;
	private Selector selector;
	private SocketChannel myChannel;
	private ServerSocketChannel myServerChannel;
	private InetSocketAddress mySocketAddress;
	public HashMap<Integer, String> clientNames = new HashMap<>();
	private HashMap<SocketChannel, String> commandsRcv = new HashMap<>();
	private HashMap<structures, String> commandsToSend = new HashMap<>();
	private HashMap<structures, SocketChannel> whoICanTalkTo = new HashMap<>();
	public String myName;
	public int myPort;
	public int currentFriendoID = -1;
	private StringBuilder myMessagesHistory = new StringBuilder();
	public clientController controll;

	public synchronized String getMessagess() {
		return myMessagesHistory.toString();
	}

	public synchronized void addMessage(boolean isItMe, String msg) {
		if (isItMe)
			myMessagesHistory.append("Me: \n" + msg + "\n");
		else
			myMessagesHistory.append(clientNames.get(currentFriendoID) + ": \n" + msg + "\n");
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	synchronized static private void setID(client c) {
		c.ID = id;
		id++;
	}

	public synchronized void whoICanTalkPut(structures s, SocketChannel c) {
		whoICanTalkTo.put(s, c);
	}

	public synchronized void registerContact(int port, structures struct) {
		InetSocketAddress sockA = new InetSocketAddress("localhost", port);
		try {
			SocketChannel sc = SocketChannel.open();
			sc.configureBlocking(false);
			if (struct == structures.SERVER) {
				sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT);
				sc.connect(sockA);
				whoICanTalkPut(struct, sc);
			} else if (struct == structures.CLIENT) {
				sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT);
				sc.connect(sockA);
				whoICanTalkTo.remove(struct);
				whoICanTalkPut(struct, sc);
			}
			selector.wakeup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private client(int port, String name, int serverPort) {
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setServerPort(serverPort);
		this.mySocketAddress = new InetSocketAddress("localhost", port);
		this.myName = name;
		myPort = port;
		setID(this);
		client thisObject = this;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				thisObject.startClient();
			}
		});
		t.start();
	}

	public void startClient() {
		try {
			ServerSocketChannel myServerChannel = ServerSocketChannel.open();
			myServerChannel.configureBlocking(false);
			myServerChannel.socket().bind(new InetSocketAddress("localhost", myPort));
			myServerChannel.register(selector, SelectionKey.OP_ACCEPT);

			/*
			 * myChannel = SocketChannel.open(); myChannel.configureBlocking(false);
			 * myChannel.register(selector, SelectionKey.OP_READ);
			 * myChannel.connect(mySocketAddress);
			 */

			registerContact(serverPort, structures.SERVER);

			while (true) {
				// send all ready commends
				checkRcvMessages();
				checkCmdToSend();

				selector.select();
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					keys.remove();
					System.out.println("key!!!");
					if (!key.isValid()) {
						continue;
					}
					if (key.isConnectable()) {// client
						System.out.println("I am connected to the server");
						connect(key);
					}
					if (key.isReadable()) {
						System.out.println("Client read");
						read(key);
					}
					if (key.isAcceptable()) {// serv
						System.out.println("client:I am accepting somebody " + myName);
						accept(key);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void accept(SelectionKey key) throws IOException {
		System.out.println("client: " + myName + " accept conn");
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel channel = serverChannel.accept();
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
		whoICanTalkPut(structures.CLIENT, channel);
	}

	private void connect(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		if (channel.isConnectionPending()) {
			channel.finishConnect();
		}
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
	}

	public void checkCmdToSend() {
		while (!commandsToSend.isEmpty()) {
			Iterator<Entry<structures, String>> it = commandsToSend.entrySet().iterator();
			while (it.hasNext()) {
				Entry<structures, String> entry = it.next();
				String cmd = entry.getValue();
				structures struc = entry.getKey();
				it.remove();
				System.out.println("client:" + myName + " to" + struc + " " + cmd);
				write(whoICanTalkTo.get(struc), cmd);
			}
		}
	}

	private void write(SocketChannel channel, String msg) {
		ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
		try {
			channel.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void checkRcvMessages() {
		while (!commandsRcv.isEmpty()) {
			Iterator<Entry<SocketChannel, String>> it = commandsRcv.entrySet().iterator();
			while (it.hasNext()) {
				Entry<SocketChannel, String> e = it.next();
				SocketChannel channel = e.getKey();
				String cmd = e.getValue();
				cmd = cmd.substring(0, cmd.length() - 1);
				it.remove();
				if (cmd.startsWith("@")) {
					String[] splitCmd = cmd.split(";");
					switch (splitCmd[0]) {
					case "@OK":// register client - @Set;id;name

						break;
					case "@clients":
						System.out.println(cmd + "ooo");
						for (int i = 1; i < splitCmd.length; i++) {
							String[] splitClient = splitCmd[i].split(":");
							try {
								clientNames.put(Integer.parseInt(splitClient[0]), splitClient[1]);
							} catch (Exception ex) {
								System.out.println("bleh");
							}
							controll.handlerGotClientList();
						}
						break;
					case "@Get":// @Get;id;nazwa;->@Get;id;nazwa;port;
						int id = Integer.parseInt(splitCmd[1]);
						String nazwa = splitCmd[2];
						int port = Integer.parseInt(splitCmd[3]);
						// friendSocketAddress = new InetSocketAddress("localhost", port);
						System.out.println("client:connecting to " + port);
						registerContact(port, structures.CLIENT);
						currentFriendoID = id;
						break;
					case "@Msg":
						// TODO HANDLE MESSAGE;
						this.addMessage(false, splitCmd[1]);
						controll.handlerGotMsg();
						break;
					}
				} else {
					System.out.println("Bad command structure.");
				}
			}
		}
	}

	private void read(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.clear();
		int readBytes = -1;
		try {
			readBytes = channel.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Reading problem, closing connection");
			key.cancel();
			try {
				channel.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
		if (readBytes > 0) {
			buffer.flip();
			byte[] data = new byte[1000];
			buffer.get(data, 0, readBytes);
			String message = new String(data);
			System.out.println(myName + "Client:Read command: " + message);
			commandsRcv.put(channel, message);
		} else {
			System.out.println("Nothing was there to be read, closing connection");
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			key.cancel();
			return;
		}
	}

	public int getID() {
		return this.ID;
	}

	public static client getClient(int port, String name, int serverPort) {
		client c = new client(port, name, serverPort);
		return c;
	}

	public void write(String message) {
		ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
		try {
			myChannel.write(buffer);
			buffer.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void generateCommand(commands command, String... data) {
		String cmd = "";
		switch (command) {
		case CONTACTS:
			cmd = "@CONTACTS;";
			break;
		case GET:
			cmd = "@GET;" + data[0] + ";" + data[1] + ";";
			break;
		case MSG:
			cmd = "@MSG;" + data[0] + ";";
			this.addMessage(true, data[0]);
			break;
		case SET:
			cmd = "@SET;" + this.getID() + ";" + this.myName + ";" + this.myPort + ";";
			break;
		case BYE:
			cmd = "@BYE;";
		default:
			break;
		}
		structures str = (command == commands.MSG || command == commands.BYE) ? structures.CLIENT
				: structures.SERVER;
		commandsToSend.put(str, cmd);
	}
}
