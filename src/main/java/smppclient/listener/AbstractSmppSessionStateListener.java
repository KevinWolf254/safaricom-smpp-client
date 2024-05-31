package smppclient.listener;

import org.jsmpp.extra.SessionState;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSmppSessionStateListener implements SessionStateListener {
	private static final Logger log = LoggerFactory.getLogger(AbstractSmppSessionStateListener.class);
	private SMPPSession session;
//    private final long reconnectInterval;
    private boolean isApplicationShuttingDown = false;
    
	public AbstractSmppSessionStateListener() {
//		Assert.isTrue(reconnectInterval > bindTimeout, "Reconnect interval should be greater then the bind timeout!");
//		this.reconnectInterval = reconnectInterval;
	}
	
	@Override
	public void onStateChange(SessionState newState, SessionState oldState, Session session) {
        log.info("Session with id {} changed state from {} to {}", session.getSessionId(), oldState, newState);
        this.setSession((SMPPSession) session);
        
        if (!isApplicationShuttingDown && newState.equals(SessionState.CLOSED)) {
            log.info("Session {} closed, reconnect after {} ms", session.getSessionId());
            reconnect();
        }
	}

	protected SMPPSession getSession() {
		return session;
	}

	protected void setSession(SMPPSession session) {
		this.session = session;
	}
	
	public abstract void reconnect();

	public void setApplicationSuttingDown(boolean isApplicationSuttingDown) {
		this.isApplicationShuttingDown = isApplicationSuttingDown;
	}

}
