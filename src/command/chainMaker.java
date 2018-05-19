package command;

public class chainMaker {

	public static commandChain makeClientChainCommand() {
		commandChain get = new commandGET();
		commandChain msg = new commandMSG();
		commandChain clt = new commandCLIENTS();

		clt.setNext(get);
		msg.setNext(clt);
		return msg;
	}

	public static commandChain makeServerChainCommand() {
		commandChain set = new commandSET();
		commandChain get = new commandGET();
		commandChain msg = new commandMSG();
		commandChain clt = new commandCLIENTS();

		clt.setNext(get);
		get.setNext(set);
		return clt;
	}
}
