package model;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class server {

	private static server singleton = null;
	public HashMap<Integer, String> clientsNames = new HashMap<>();// NO & name?
	public HashMap<Integer, Integer> clientsPorts = new HashMap<>();// NO & port?
	public HashMap<SocketChannel, Integer> clientsChannels = new HashMap<>();
	private static Selector selector;
	private static InetSocketAddress address;
	private static HashMap<SocketChannel, String> commands = new HashMap<>();

	public static InetSocketAddress getAddress() {
		return address;
	}

	public static void setAddress(InetSocketAddress address) {
		server.address = address;
	}

	/*
	 * public static server getServer() { if (singleton == null) singleton = new
	 * server(); server.address = new InetSocketAddress("localhost", 20123); return
	 * singleton; }
	 */

	public static server getServer(int port) {
		if (singleton == null)
			singleton = new server();
		server.address = new InetSocketAddress("localhost", port);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					singleton.startServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		return singleton;
	}

	public void startServer() throws IOException {
		server.selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(address);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Server started...");

		while (true) { 
			server.selector.select();
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = keys.next();
				keys.remove();
				if (!key.isValid()) {
					continue;
				}
				if (key.isAcceptable()) {
					System.out.println("Accepting connection");
					accept(key);
				}
				if (key.isReadable()) {
					System.out.println("Reading from client");
					read(key);
				}

				checkRcvMessages();
			}
		}

	}

	public void checkRcvMessages() {
		while (!commands.isEmpty()) {
			Iterator<Entry<SocketChannel, String>> it = commands.entrySet().iterator();
			while (it.hasNext()) {
				Entry<SocketChannel, String> entry = it.next();
				SocketChannel channel = entry.getKey();
				String cmd = entry.getValue();
				it.remove();
				if (cmd.startsWith("@")) {
					String[] splitCmd = cmd.split(";");
					switch (splitCmd[0]) {
					case "@Set":// register client - @Set;id;name;port
						System.out.println("set"+splitCmd[3]+splitCmd[2]);
						int number = Integer.parseInt(splitCmd[1]);
						int porte = Integer.parseInt(splitCmd[3]);
						clientsChannels.put(channel, number);
						clientsNames.put(number, splitCmd[2]);
						clientsPorts.put(number, porte);
						write(channel, "@OK;");
						break;
					case "@CONTACTS":
						System.out.println("send clietn");
						String clientList = "@clients;" + getClients();
						write(channel, clientList);
						break;
					case "@Get":// @Get;id;nazwa;->@Get;id;nazwa;port;
						System.out.println("get");
						int id = Integer.parseInt(splitCmd[1]);
						String nazwa = splitCmd[2];
						int port = clientsPorts.get(id);
						write(channel, "@Get;" + id + ";" + nazwa + ";" + port + ";");
						break;
					}

				} else {
					System.out.println("Bad command structure.");
				}
			}
		}
	}

	public void write(SocketChannel channel, String msg) {
		ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
		try {
			channel.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
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
			System.out.println("Server:Read command: " + message);
			commands.put(channel, message);
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

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel channel = serverChannel.accept();
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
		clientsChannels.put(channel, -1);
	}

	public String getClients() {
		StringBuilder b = new StringBuilder();
		/*
		 * singleton.clientsNames.entrySet().stream().forEach(e -> {
		 * System.out.println(e); b.append(e.getKey() + ":" + e.getValue() + ";"); });
		 */
		String s = "";
		Iterator<Entry<Integer, String>> it = clientsNames.entrySet().iterator();
		do {
			Entry<Integer, String> entry = it.next();
			//System.out.println(entry);
			//b.append(entry.getKey() + ":" + entry.getValue());
			s = s + entry.getKey() + ":" + entry.getValue() + ";";
		} while ((it.hasNext()));
		//String ugh="";
	/*	for (Integer key : clientsNames.keySet()) {
			System.out.println(clientsNames.get(key));
			ugh = ugh +key + ":" + clientsNames.get(key) + ";";
		}*/
		//System.out.println(ugh);
		System.out.println(s);
		//System.out.println(b.toString());
		return s;
	}

}
