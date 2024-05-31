package smppclient.listener;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class OutBoundSmppSessionStateListener extends AbstractSmppSessionStateListener {
	private static final Logger log = LoggerFactory.getLogger(AbstractSmppSessionStateListener.class);
	private final String host;
	private final int port;
	private final long bindTimeout;
	private final long reconnectInterval;
	private final BindParameter bindParameter;
	private final ExecutorService executor = Executors.newSingleThreadExecutor(); // Create a single-thread executor

	public OutBoundSmppSessionStateListener(String host, int port, BindParameter bindParameter, long bindTimeout,
			long reconnectInterval) {
		super();
		Assert.isTrue(reconnectInterval > bindTimeout, "Reconnect interval should be greater then the bind timeout!");
		this.host = host;
		this.port = port;
		this.bindTimeout = bindTimeout;
		this.reconnectInterval = reconnectInterval;
		this.bindParameter = bindParameter;
	}

	@Override
	public void reconnect() {
		executor.execute(() -> {
			SMPPSession session = getSession();
			while (getSession() == null || getSession().getSessionState().equals(SessionState.CLOSED)) {
				try {
					log.info("Rescheduling OutBoundSmppSession reconnect after {} millis", reconnectInterval);
					Thread.sleep(reconnectInterval);
					log.info("Reconnecting OutBoundSmppSession...");
					String systemId = session.connectAndBind(host, port, bindParameter, bindTimeout);
					log.info("Reconnected OutBoundSmppSession with systemid {}", systemId);
				} catch (IOException e) {
					log.error("Unable to connect outBoundSmppSession", e);
				} catch (InterruptedException e) {
					return; // If interrupted, exit thread
				}

			}
		});
	}

}
