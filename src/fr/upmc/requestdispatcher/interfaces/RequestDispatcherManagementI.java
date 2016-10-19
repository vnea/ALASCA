package fr.upmc.requestdispatcher.interfaces;

import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;

public interface RequestDispatcherManagementI {

		public void addRequestSubmissioner(String requestSubmissionInboundPortURI, RequestNotificationOutboundPort rnop) throws Exception;
}
