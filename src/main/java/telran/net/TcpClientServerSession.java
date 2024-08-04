package telran.net;

import java.net.*;
import java.io.*;
import static telran.net.TcpConfigurationProperties.*;

public class TcpClientServerSession extends Thread {
	Socket socket;
	Protocol protocol;
	TcpServer tcpServer;

	public TcpClientServerSession(Socket socket, Protocol protocol, TcpServer tcpServer) {
		this.socket = socket;
		this.tcpServer = tcpServer;
		// using the method setSoTimeout and some solution for getting session to know
		// about shutdown
		// you should stop the thread after shutdown command
		this.protocol = protocol;
	}

	public void run() {
		try (BufferedReader receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintStream sender = new PrintStream(socket.getOutputStream())) {
			String line = null;
			boolean sessionRunning = true;
			socket.setSoTimeout(SOCKET_TIMEOUT);
			long idleTime = 0;
			while (tcpServer.running && sessionRunning) {
				try {
					line = receiver.readLine();
					if (line == null) {
						break;
					}
					String responseStr = protocol.getResponseWithJSON(line);
					sender.println(responseStr);
					idleTime = 0;
				} catch (SocketTimeoutException e) {
					idleTime += SOCKET_TIMEOUT;
					if (idleTime > SESSION_IDLE_TIMEOUT) {
						sessionRunning = false;
					}
				}
			}
			socket.close();
			System.out.printf("Session %s was gracefull shut down.\n", 
					Thread.currentThread().getName());

		} catch (Exception e) {
			System.out.printf("Session %s was shut down with error: %s.\n", 
					Thread.currentThread().getName(),
					e.getMessage());
		}
	}

}
