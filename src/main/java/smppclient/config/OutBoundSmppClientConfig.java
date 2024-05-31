package smppclient.config;

import java.io.IOException;

import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PreDestroy;
import smppclient.listener.ApplicationShutdownListener;
import smppclient.listener.OutBoundSmppSessionStateListener;

@Configuration
public class OutBoundSmppClientConfig {
	private static final Logger log = LoggerFactory.getLogger(OutBoundSmppClientConfig.class);

    private final String host;
    private final int port;
    private final String integrationType;
    private final int enquireLinkTimer;
    private final long transactionTimer;

    private final int pduProcessorDegree;
    private final long bindTimeout;
    private final long reconnectInterval;

	private final SMPPSession outBoundSmppSession;
	private final BindParameter bindParameter;
	
	public OutBoundSmppClientConfig(@Value("${smpp.host}")String host, @Value("${smpp.port}")int port,
			@Value("${smpp.send.systemid}")String systemId, @Value("${smpp.send.password}")String password, 
			@Value("${smpp.system-type}")String systemType,
			@Value("${smpp.enquire_timeout}")int enquireLinkTimer, @Value("${smpp.timeout:5000}")long transactionTimer, 
			@Value("${sms.integration_type}")String integrationType) {
		this.host = host;
		this.port = port;
		this.integrationType = integrationType;
		this.enquireLinkTimer = enquireLinkTimer;
		this.transactionTimer = transactionTimer;
		outBoundSmppSession = new SMPPSession();
		bindTimeout = 5000L;
		reconnectInterval = 5500L;// TODO - RETURN TO 1000L;
		pduProcessorDegree = 10;

		bindParameter = 
				new BindParameter(BindType.BIND_TX, systemId, password, systemType, TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null);
        
	}
	
	@Bean
	SMPPSession outBoundSmppSession() {
	    if (StringUtils.hasText(integrationType)) {
			outBoundSmppSession.setEnquireLinkTimer(enquireLinkTimer);
			outBoundSmppSession.setTransactionTimer(transactionTimer);
			outBoundSmppSession.setPduProcessorDegree(pduProcessorDegree);
			outBoundSmppSession.addSessionStateListener(outBoundSmppSessionStateListener());
			
			try {
				log.info("Connecting outBoundSmppSession...");
				String systemId = outBoundSmppSession.connectAndBind(host, port, bindParameter, bindTimeout);
				log.info("Connected outBoundSmppSession with systemid {}", systemId);
			} catch (IOException e) {
				log.error("Unable to connect outBoundSmppSession", e);
			}
	    }
	    return outBoundSmppSession;
	}
	
	@Bean
	OutBoundSmppSessionStateListener outBoundSmppSessionStateListener() {
        return new OutBoundSmppSessionStateListener(host, port, bindParameter, bindTimeout, reconnectInterval);
    }
	
	@Bean
	ApplicationShutdownListener outBoundSmppSessionStateListenerApplicationShutdownListener() {
		return new ApplicationShutdownListener(outBoundSmppSessionStateListener());
	}
	
	@PreDestroy
    public void destroy() {
		log.debug("Is outBoundSmppSession open {} ", !outBoundSmppSession.getSessionState().equals(SessionState.CLOSED));
		log.debug("Is outBoundSmppSession bound {} ", outBoundSmppSession.getSessionState().isBound());
        if (outBoundSmppSession != null && !outBoundSmppSession.getSessionState().equals(SessionState.CLOSED) && !outBoundSmppSession.getSessionState().isBound()) {
    		log.info("Closing outBoundSmppSession...");
    		outBoundSmppSession.close();
    		log.info("Closed outBoundSmppSession.");
        }else if (outBoundSmppSession != null && !outBoundSmppSession.getSessionState().equals(SessionState.CLOSED) && outBoundSmppSession.getSessionState().isBound()) {
    		log.info("Closing and unbinding outBoundSmppSession...");
    		outBoundSmppSession.unbindAndClose();
    		log.info("Closed and unbinded outBoundSmppSession.");        	
        }
    }
}
