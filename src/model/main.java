package model;

import controller.clientController;
import view.clientWindow;

public class main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		server s = server.getServer(20000);
		client c = client.getClient(20001, "eagle1", 20000);
		client c2 = client.getClient(20002, "eagle2", 20000);
		client c3 = client.getClient(20003, "eagle3", 20000);
		clientController cc3 = new clientController(c3, new clientWindow());
		clientController cc = new clientController(c, new clientWindow());
		clientController cc2 = new clientController(c2, new clientWindow());
		
	}

}
