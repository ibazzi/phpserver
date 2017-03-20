package org.eclipse.php.builtin.server.core.internal.debugger;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

public class HttpReverseProxyServer {

	private static final String HTTP_CONN_KEEPALIVE = "http.proxy.conn-keepalive";
	private static final String HTTP_IN_CONN = "HTTP_IN_CONN";

	private RequestListenerThread fThread;
	private IHttpRequestHandler fHandler;

	public HttpReverseProxyServer(IHttpRequestHandler handler) {
		fHandler = handler;
	}

	public void start(int port) throws Exception {
		fThread = new RequestListenerThread(port);
		fThread.setHttpRequestHandler(fHandler);
		fThread.setDaemon(false);
		fThread.start();
	}

	public void stop() {
		try {
			fThread.stopServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public interface ConnectionClosedHandler {

		public void notifyConnectionClosed(HttpServerConnection connection);

	}

	public interface IHttpRequestHandler {

		public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context)
				throws HttpException, IOException;

		public void close(HttpServerConnection connection) throws IOException;

	}

	static class ProxyHandler implements HttpRequestHandler, ConnectionClosedHandler {

		private IHttpRequestHandler fHttpRequestHandler;

		public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context)
				throws HttpException, IOException {
			if (fHttpRequestHandler != null) {
				fHttpRequestHandler.handle(request, response, context);
			}
		}

		public void setHttpRequestHandler(IHttpRequestHandler requestHandler) {
			fHttpRequestHandler = requestHandler;
		}

		@Override
		public void notifyConnectionClosed(HttpServerConnection connection) {
			try {
				if (fHttpRequestHandler != null) {
					fHttpRequestHandler.close(connection);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static class RequestListenerThread extends Thread {

		private ServerSocket serversocket;
		private HttpService httpService;
		private ProxyHandler handler;
		private boolean isRunning = false;

		public RequestListenerThread(final int port) throws IOException {
			this.serversocket = new ServerSocket(port);

			final HttpProcessor inhttpproc = new ImmutableHttpProcessor(new HttpRequestInterceptor[] {
					new RequestContent(), new RequestTargetHost(), new RequestConnControl(),
					new RequestUserAgent("Test/1.1"), new RequestExpectContinue(true) });
			handler = new ProxyHandler();
			final UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
			reqistry.register("*", handler);

			this.httpService = new HttpService(inhttpproc, reqistry);
		}

		public void setHttpRequestHandler(IHttpRequestHandler requestHandler) {
			handler.setHttpRequestHandler(requestHandler);
		}

		@Override
		public void run() {
			isRunning = true;
			System.out.println("Listening on port " + this.serversocket.getLocalPort());
			while (isRunning) {
				try {

					final int bufsize = 512 * 1024;
					// Set up incoming HTTP connection
					final Socket insocket = this.serversocket.accept();
					final DefaultBHttpServerConnection inconn = new DefaultBHttpServerConnection(bufsize);
					System.out.println("Incoming connection from " + insocket.getInetAddress());
					inconn.bind(insocket);

					// Start worker thread
					final Thread t = new ProxyThread(this.httpService, inconn, handler);
					t.setDaemon(true);
					t.start();
				} catch (final InterruptedIOException ex) {
					break;
				} catch (final IOException e) {
					System.err.println("I/O error initialising connection thread: " + e.getMessage());
					break;
				}
			}
		}

		public void stopServer() throws IOException {
			if (serversocket != null) {
				isRunning = false;
				serversocket.close();
				serversocket = null;
			}
		}
	}

	static class ProxyThread extends Thread {

		private final HttpService httpservice;
		private final HttpServerConnection inconn;
		private final ProxyHandler handler;

		public ProxyThread(final HttpService httpservice, final HttpServerConnection inconn,
				final ProxyHandler handler) {
			super();
			this.httpservice = httpservice;
			this.inconn = inconn;
			this.handler = handler;
		}

		@Override
		public void run() {
			System.out.println("New connection thread");
			final HttpContext context = new BasicHttpContext(null);

			try {
				while (!Thread.interrupted()) {
					if (!this.inconn.isOpen()) {
						handler.notifyConnectionClosed(inconn);
						break;
					}
					context.setAttribute(HTTP_IN_CONN, this.inconn);
					this.httpservice.handleRequest(this.inconn, context);

					final Boolean keepalive = (Boolean) context.getAttribute(HTTP_CONN_KEEPALIVE);
					if (!Boolean.TRUE.equals(keepalive)) {
						handler.notifyConnectionClosed(inconn);
						this.inconn.close();
						break;
					}
				}
			} catch (final ConnectionClosedException ex) {
				System.err.println("Client closed connection");
			} catch (final IOException ex) {
				System.err.println("I/O error: " + ex.getMessage());
			} catch (final HttpException ex) {
				System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
			} finally {
				handler.notifyConnectionClosed(inconn);
				try {
					this.inconn.shutdown();
				} catch (final IOException ignore) {
				}
			}
		}
	}

}
