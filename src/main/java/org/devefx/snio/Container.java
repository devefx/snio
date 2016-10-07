package org.devefx.snio;

import java.beans.PropertyChangeListener;

public interface Container {
	
	String getInfo();
	
	Manager getManager();
	
	void setManager(Manager manager);
	
	int getBackgroundProcessorDelay();	
	
	void setBackgroundProcessorDelay(int delay);
	
    String getName();

    void setName(String name);
	
	void backgroundProcess();
	
	void addContainerListener(ContainerListener listener);

	ContainerListener[] findContainerListeners();

	void removeContainerListener(ContainerListener listener);
	
	void addPropertyChangeListener(PropertyChangeListener listener);
}
