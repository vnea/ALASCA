package fr.upmc.requestdispatcher.interfaces;

import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;

public interface RequestDispatcherManagementI {

		public void addRequestReceiver(final String requestSubmissionInboundPortURI, final RequestNotificationOutboundPort rnop) throws Exception;
}
