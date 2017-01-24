package fr.upmc.requestdispatcher.interfaces;

public interface RequestDispatcherStateDataConsumerI
{
	public void			acceptRequestDispatcherDynamicData(
		String					dispatcherURI,
		RequestDispatcherDynamicStateI	currentDynamicState
		) throws Exception ;

}
