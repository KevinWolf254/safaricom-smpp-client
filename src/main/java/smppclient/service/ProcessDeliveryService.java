package smppclient.service;

import org.jsmpp.bean.DeliverSm;

public interface ProcessDeliveryService {
	void process(DeliverSm deliverSm, long id);
}
