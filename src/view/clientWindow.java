package view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.ListSelectionModel;
import javax.swing.JSeparator;

public class clientWindow extends JFrame {

	private JPanel contentPane;
	public JButton connectClientBtn;
	public JButton loadClientsBtn;
	public JButton sendBtn;
	public JTable clientsList;
	public JTextPane readTextPane;
	public JTextArea writeTextArea;
	public JLabel lblNewLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					clientWindow frame = new clientWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public clientWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);

		lblNewLabel = new JLabel("New label");
		panel.add(lblNewLabel);

		JPanel writeChatPanel = new JPanel();
		readTextPane = new JTextPane();
		writeTextArea = new JTextArea();
		JScrollPane readChat = new JScrollPane(readTextPane);
		writeChatPanel.setLayout(new BorderLayout(0, 0));
		JScrollPane writeChat = new JScrollPane(writeTextArea);
		writeChatPanel.add(writeChat);
		JSplitPane chatPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, readChat, writeChatPanel);

		sendBtn = new JButton("Send");

		writeChatPanel.add(sendBtn, BorderLayout.SOUTH);
		chatPane.setResizeWeight(0.7);

		JPanel clientsPanel = new JPanel();
		JSplitPane widokSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, clientsPanel, chatPane);
		clientsPanel.setLayout(new BorderLayout(0, 0));

		clientsList = new JTable(new DefaultTableModel(new Object[] { "number", "name" }, 0));
		clientsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// clientsList.setVisibleRowCount(10);
		// clientsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		clientsPanel.add(clientsList);

		JPanel clientsBtnPanel = new JPanel();
		clientsPanel.add(clientsBtnPanel, BorderLayout.SOUTH);
		clientsBtnPanel.setLayout(new BoxLayout(clientsBtnPanel, BoxLayout.PAGE_AXIS));

		JSeparator separator = new JSeparator();
		clientsBtnPanel.add(separator);

		connectClientBtn = new JButton("Connect");

		clientsBtnPanel.add(connectClientBtn);

		loadClientsBtn = new JButton("Load clients");

		clientsBtnPanel.add(loadClientsBtn);

		JSeparator separator_1 = new JSeparator();
		clientsBtnPanel.add(separator_1);
		widokSplitPane.setResizeWeight(0.2);
		contentPane.add(widokSplitPane, BorderLayout.CENTER);

		clientWindow thisObject = this;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					thisObject.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
