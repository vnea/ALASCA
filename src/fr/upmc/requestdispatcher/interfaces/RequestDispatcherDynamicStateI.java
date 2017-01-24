package fr.upmc.requestdispatcher.interfaces;

import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;

public interface RequestDispatcherDynamicStateI
extends 	DataOfferedI.DataI,
			DataRequiredI.DataI
{
	

	public Double		getMovingAverage() ;
}
