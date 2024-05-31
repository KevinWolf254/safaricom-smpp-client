package smppclient.service;

import org.jsmpp.bean.DeliverSm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IncomingSMSServiceImpl implements IncomingSMSService {
	private static final Logger log = LoggerFactory.getLogger(IncomingSMSServiceImpl.class);

	@Override
	public void saveRequestSMPP(DeliverSm deliverSm) {
		log.info("Saving DeliverSm {}", deliverSm);
	}

}
