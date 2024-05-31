package smppclient.service;

import org.jsmpp.bean.DeliverSm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessDeliveryServiceImpl implements ProcessDeliveryService {
	private static final Logger log = LoggerFactory.getLogger(ProcessDeliveryServiceImpl.class);

	@Override
	public void process(DeliverSm deliverSm, long id) {
		log.info("Processing DeliverSm {} with id {}", deliverSm, id);
	}

}
