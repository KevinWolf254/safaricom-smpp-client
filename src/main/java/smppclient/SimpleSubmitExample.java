package smppclient;

import java.io.IOException;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

//@Component
public class SimpleSubmitExample {
	private static final Logger log = LoggerFactory.getLogger(SimpleSubmitExample.class);
    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();
        
//    @PostConstruct
    public static void connect() {
        SMPPSession session = new SMPPSession();
        try {
            log.info("Connecting...");
            String systemId = session.connectAndBind("smscsim.smpp.org", 2775, 
            	new BindParameter(BindType.BIND_TX, 
            		"JWXYSqy2I6InSfG", "V0XZvLP3", "", 
            		TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null));
            
            log.info("SMSC system ID is {}", systemId);
            
            try {
                String messageId = session.submitShortMessage("",
                    TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, "MelroseLabs",
                    TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN, "447712345678",
                    new ESMClass(), (byte)0, (byte)1,  null, null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), 
                    (byte)0, 
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false), (byte)0,
                    "Hello World €$£".getBytes());
                    
                log.info("Message successfully submitted (message_id={})", messageId);
                
            } catch (PDUException e) {
                // Invalid PDU parameter
                log.error("Invalid PDU parameter", e);
            } catch (ResponseTimeoutException e) {
                // Response timeout
                log.error("Response timeout", e);
            } catch (InvalidResponseException e) {
                // Invalid response
                log.error("Receive invalid response", e);
            } catch (NegativeResponseException e) {
                // Receiving negative response (non-zero command_status)
                log.error("Receive negative response", e);
            } catch (IOException e) {
                log.error("IO error occured", e);
            }

            session.unbindAndClose();
            log.info("Session closed");

		} catch (IOException e) {
            log.error("Failed connect and bind to SMSC", e);
        }
    }
}
