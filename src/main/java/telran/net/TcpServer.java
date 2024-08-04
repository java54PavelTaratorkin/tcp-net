package telran.net;

import java.net.*;
import java.util.concurrent.*;

import static telran.net.TcpConfigurationProperties.*;

public class TcpServer implements Runnable {
	Protocol protocol;
	int port;
	boolean running = true;
	ExecutorService executor;

	public TcpServer(Protocol protocol, int port) {
		this.protocol = protocol;
		this.port = port;
		this.executor = Executors.newFixedThreadPool(getNumberOfThreads());
	}

	private int getNumberOfThreads() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.availableProcessors();
	}

	public void shutdown() {
		executor.shutdown();
		running = false;
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {

        }
	}

	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			// using ServerSocket method setSoTimeout
			System.out.println("Server is listening on port " + port);
			serverSocket.setSoTimeout(SOCKET_TIMEOUT);
			while (running) {
				try {
                    Socket socket = serverSocket.accept();
                    if (running) {
                        TcpClientServerSession session = new TcpClientServerSession(socket, protocol, this);
                        executor.execute(session);
                    } else {
                        socket.close();
                    }
				} catch (SocketTimeoutException e) {

				}

			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
