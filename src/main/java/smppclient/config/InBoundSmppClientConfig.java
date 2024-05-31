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
import smppclient.listener.InBoundMessageReceiverListener;
import smppclient.listener.InBoundSmppSessionStateListener;
import smppclient.service.IncomingSMSService;
import smppclient.service.ProcessDeliveryService;

@Configuration
public class InBoundSmppClientConfig {
	private static final Logger log = LoggerFactory.getLogger(InBoundSmppClientConfig.class);

    private final String host;
    private final int port;
    private final String integrationType;
    private final IncomingSMSService incomingSMSService;
    private final ProcessDeliveryService processDeliveryService;

    private final long reconnectInterval;
    private final long bindTimeout;
    private final SMPPSession inBoundSmppSession;
	private final BindParameter bindParameter;

    public InBoundSmppClientConfig(@Value("${smpp.host}")String host, @Value("${smpp.port}")int port, 
			@Value("${smpp.system-type}")String systemType, @Value("${smpp.receive.systemid}")String systemUsername, 
			@Value("${smpp.receive.password}")String password, @Value("${sms.integration_type}")String integrationType,
			IncomingSMSService incomingSMSService, ProcessDeliveryService processDeliveryService) {
		this.host = host;
		this.port = port;
		this.integrationType = integrationType;
		this.incomingSMSService = incomingSMSService;
		this.processDeliveryService = processDeliveryService;
		inBoundSmppSession = new SMPPSession();
		bindTimeout = 5000L;
		reconnectInterval = 5500L;// TODO - RETURN TO 1000L;
		bindParameter = new BindParameter(BindType.BIND_RX, systemUsername, password, systemType, TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null);
	}

	@Bean
	SMPPSession inBoundSmppSession() {

      if (StringUtils.hasText(integrationType)) {
		inBoundSmppSession.setMessageReceiverListener(inBoundMessageReceiverListener());
		inBoundSmppSession.addSessionStateListener(inBoundSmppSessionStateListener());

		try {
			log.info("Connecting inBoundSmppSession...");
			String systemId = inBoundSmppSession.connectAndBind(host, port, bindParameter);
			log.info("Connected inBoundSmppSession with systemid {}", systemId);
		} catch (IOException e) {
			log.error("Unable to connect inBoundSmppSession", e);
		}
      }
	    return inBoundSmppSession;
	}
	
	@Bean
	InBoundSmppSessionStateListener inBoundSmppSessionStateListener() {
        return new InBoundSmppSessionStateListener(host, port, bindParameter, bindTimeout, reconnectInterval);
    }
	
	@Bean
	ApplicationShutdownListener inBoundSmppSessionStateListenerApplicationShutdownListener() {
		return new ApplicationShutdownListener(inBoundSmppSessionStateListener());
	}
	
	@Bean
	InBoundMessageReceiverListener inBoundMessageReceiverListener() {
		return new InBoundMessageReceiverListener(processDeliveryService, incomingSMSService);
	}

	@PreDestroy
    public void destroy() {
		log.debug("Is inBoundSmppSession open {} ", !inBoundSmppSession.getSessionState().equals(SessionState.CLOSED));
		log.debug("Is inBoundSmppSession bound {} ", inBoundSmppSession.getSessionState().isBound());
        if (inBoundSmppSession != null && !inBoundSmppSession.getSessionState().equals(SessionState.CLOSED) && !inBoundSmppSession.getSessionState().isBound()) {
    		log.info("Closing inBoundSmppSession...");
    		inBoundSmppSession.close();
    		log.info("Closed inBoundSmppSession.");
        }else if (inBoundSmppSession != null && !inBoundSmppSession.getSessionState().equals(SessionState.CLOSED) && inBoundSmppSession.getSessionState().isBound()) {
    		log.info("Closing and unbinding inBoundSmppSession...");
    		inBoundSmppSession.unbindAndClose();
    		log.info("Closed and unbinded inBoundSmppSession.");        	
        }
    }
    
}
