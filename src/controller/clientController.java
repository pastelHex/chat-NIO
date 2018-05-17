package controller;

import view.clientWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.table.DefaultTableModel;

import command.commands;
import model.*;

public class clientController {

	private clientWindow window;
	private client client;

	public clientController(client c, clientWindow cw) {
		window = cw;
		client = c;
		c.controll = this;
		window.lblNewLabel.setText(c.myName);
		window.lblNewLabel.repaint();
		addListeners();
		client.generateCommand(commands.SET);
		client.checkCmdToSend();
	}

	private void addListeners() {
		window.connectClientBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("clieck3");
				int selRow = window.clientsList.getSelectedRow();
				String NO = window.clientsList.getValueAt(selRow, 0).toString();
				String name = window.clientsList.getValueAt(selRow, 1).toString();
				String[] data = { NO, name };
				client.generateCommand(commands.GET, data);
				client.checkCmdToSend();
			}
		});
		window.loadClientsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("clieck2");
				client.generateCommand(commands.CONTACTS);
				client.checkCmdToSend();
			}
		});
		window.sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("clieck1");
				String text = window.writeTextArea.getText();
				String[] arg = { text };
				client.generateCommand(commands.MSG, arg);
				client.checkCmdToSend();
				window.writeTextArea.setText("");
				window.readTextPane.setText(client.getMessagess());
				window.readTextPane.repaint();
			}
		});
	}

	public void handlerGotMsg() {
		window.readTextPane.setText(client.getMessagess());
		window.readTextPane.repaint();
	}

	public void handlerGotClientList() {
		System.out.println("handler list");
		DefaultTableModel model = (DefaultTableModel) window.clientsList.getModel();
		String[][] data = new String[client.clientNames.size()][2];
		int i = 0;
		for (Integer key : client.clientNames.keySet()) {
			System.out.println(client.clientNames.get(key));
			data[i][0] = key.intValue() + "";
			data[i][1] = client.clientNames.get(key);
			i++;
			// ugh = ugh +key + ":" + client.clientNames.get(key) + ";";
		}
		model = new DefaultTableModel(data, new Object[] { "number", "name" });
		window.clientsList.setModel(model);
		window.clientsList.repaint();
	}

}
