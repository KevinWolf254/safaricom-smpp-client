package smppclient.listener;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.bean.MessageType;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smppclient.service.IncomingSMSService;
import smppclient.service.ProcessDeliveryService;

public class InBoundMessageReceiverListener implements MessageReceiverListener {
	private static final Logger log = LoggerFactory.getLogger(InBoundMessageReceiverListener.class);
	private final ProcessDeliveryService processDeliveryService;
	private final IncomingSMSService incomingSMSService;
    private Charset charset;

	public InBoundMessageReceiverListener(ProcessDeliveryService processDeliveryService,
			IncomingSMSService incomingSMSService) {
		this.processDeliveryService = processDeliveryService;
		this.incomingSMSService = incomingSMSService;
		this.charset = StandardCharsets.ISO_8859_1;
	}

	@Override
	public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
		return null;
	}

	@Override
	public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {

		if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
			try {
				final DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
				// lets cover the id to hex string format
				long id = Long.parseLong(delReceipt.getId()) & 0xffffffff;
				final String messageId = Long.toString(id, 16).toUpperCase();
				log.info("Receiving delivery receipt for message '{}' from {} to {} : {}", messageId,
						deliverSm.getSourceAddr(), deliverSm.getDestAddress(), delReceipt);

				processDeliveryService.process(deliverSm, id);

			} catch (InvalidDeliveryReceiptException e) {
				log.error("Failed getting delivery receipt", e);
			}
		} else {
			log.info("Receiving message : {}", new String(deliverSm.getShortMessage(), charset));
			incomingSMSService.saveRequestSMPP(deliverSm);
		}

	}

	@Override
	public void onAcceptAlertNotification(AlertNotification alertNotification) {
	}

}
