package smppclient.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

public class ApplicationShutdownListener implements ApplicationListener<ContextClosedEvent> {
	private static final Logger log = LoggerFactory.getLogger(AbstractSmppSessionStateListener.class);
	private final AbstractSmppSessionStateListener smppSessionStateListener;
	
	public ApplicationShutdownListener(AbstractSmppSessionStateListener smppSessionStateListener) {
		this.smppSessionStateListener = smppSessionStateListener;
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
        smppSessionStateListener.setApplicationSuttingDown(true);
        log.info("Set application shutting down to {}", true);
	}

}
