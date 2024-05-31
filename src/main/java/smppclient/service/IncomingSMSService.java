package smppclient.service;

import org.jsmpp.bean.DeliverSm;

public interface IncomingSMSService {
	void saveRequestSMPP(DeliverSm deliverSm);
}
