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

import command.chainMaker;
import command.commandChain;

public class server {

	public HashMap<Integer, String> clientsNames = new HashMap<>();// NO & name?
	public HashMap<Integer, Integer> clientsPorts = new HashMap<>();// NO & port?
	public HashMap<SocketChannel, Integer> clientsChannels = new HashMap<>();
	private Selector selector;
	private InetSocketAddress address;
	private HashMap<SocketChannel, String> commands = new HashMap<>();
	private commandChain commandChain;

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	private server() {
		commandChain = chainMaker.makeServerChainCommand();
	}

	public static server getServer(int port) {
		server s = new server();
		s.address = new InetSocketAddress("localhost", port);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					s.startServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		return s;
	}

	public void startServer() throws IOException {
		this.selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(address);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Server started...");

		while (true) {
			this.selector.select();
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

				commandChain.interpretCommandForServer(this, channel, cmd);

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
		StringBuilder a = new StringBuilder();
		this.clientsNames.entrySet().stream().forEach(e -> {
			String s = e.getKey() + ":" + e.getValue() + ";";
			a.append(s);
		});
		System.out.println(a.toString());
		return a.toString();
	}

}
