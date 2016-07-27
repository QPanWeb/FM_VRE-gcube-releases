/**
 * 
 */
package org.gcube.portlets.user.dataminermanager.client;

import org.gcube.portlets.user.dataminermanager.client.common.EventBusProvider;
import org.gcube.portlets.user.dataminermanager.client.events.MenuEvent;
import org.gcube.portlets.user.dataminermanager.client.events.MenuSwitchEvent;
import org.gcube.portlets.user.dataminermanager.client.type.MenuType;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * 
 * @author Giancarlo Panichi email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class Header extends HorizontalPanel {

	private Image menuGoBack, menuExperiment, menuDataSpace, menuComputations;
	private Enum<MenuType> currentSelection;

	public Header() {
		super();
		create();
		bind();
	}

	private void bind() {
		EventBusProvider.INSTANCE.addHandler(MenuSwitchEvent.TYPE,
				new MenuSwitchEvent.MenuSwitchEventHandler() {

					@Override
					public void onSelect(MenuSwitchEvent event) {
						Log.debug("Catch MenuSwitchEvent");
						menuSwitch(event);

					}
				});
	}

	private void create() {
		// this.setStyleAttribute("background-color", "#FFFFFF");
		Image logo = new Image(DataMinerManager.resources.logoLittle());
		logo.setAltText("Data Miner Manager");
		logo.setTitle("Data Miner Manager");
		logo.addStyleName("menuImgLogo");

		menuGoBack = new Image(DataMinerManager.resources.goBack());
		menuGoBack.addStyleName("menuItemImage");
		menuGoBack.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				MenuEvent menuEvent = new MenuEvent(MenuType.HOME);
				EventBusProvider.INSTANCE.fireEvent(menuEvent);

			}
		});

		menuDataSpace = new Image(
				DataMinerManager.resources.menuItemInputspace());
		menuDataSpace.addStyleName("menuItemImage");
		menuDataSpace.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug("Click Menu Data Space");
				MenuEvent menuEvent = new MenuEvent(MenuType.DATA_SPACE);
				EventBusProvider.INSTANCE.fireEvent(menuEvent);
			}
		});

		menuExperiment = new Image(
				DataMinerManager.resources.menuItemExperiment());
		menuExperiment.addStyleName("menuItemImage");
		menuExperiment.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug("Click Menu Experiment");
				MenuEvent menuEvent = new MenuEvent(MenuType.EXPERIMENT);
				EventBusProvider.INSTANCE.fireEvent(menuEvent);
			}
		});

		menuComputations = new Image(
				DataMinerManager.resources.menuItemComputations());
		menuComputations.addStyleName("menuItemImage");
		menuComputations.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Log.debug("Click Menu Computations");
				MenuEvent menuEvent = new MenuEvent(MenuType.COMPUTATIONS);
				EventBusProvider.INSTANCE.fireEvent(menuEvent);
			}
		});

		add(logo);
		add(menuGoBack);
		add(menuDataSpace);
		add(menuExperiment);
		add(menuComputations);

		this.setCellWidth(logo, "100px");
		this.setCellWidth(menuGoBack, "100px");
		this.setCellWidth(menuDataSpace, "80px");
		this.setCellWidth(menuExperiment, "80px"); //
		this.setCellWidth(menuComputations, "80px");

		menuGoBack.setVisible(false);
		menuDataSpace.setVisible(false);
		menuExperiment.setVisible(false);
		menuComputations.setVisible(false);
	}

	public void setMenu(MenuType menuType){
		Log.debug("SetMenu: " + menuType);

		if (menuType.compareTo(MenuType.HOME) == 0) {
			menuGoBack.setVisible(false);
			menuDataSpace.setVisible(false);
			menuExperiment.setVisible(false);
			menuComputations.setVisible(false);

			if (currentSelection != null
					&& currentSelection.compareTo(MenuType.EXPERIMENT) == 0)
				menuExperiment.removeStyleName("menuItemImage-selected");
			else if (currentSelection != null
					&& currentSelection.compareTo(MenuType.DATA_SPACE) == 0)
				menuDataSpace.removeStyleName("menuItemImage-selected");
			else if (currentSelection != null
					&& currentSelection.compareTo(MenuType.COMPUTATIONS) == 0)
				menuComputations.removeStyleName("menuItemImage-selected");
		} else {
			if (currentSelection == null
					|| (currentSelection != null && currentSelection
							.compareTo(MenuType.HOME) == 0)) {
				menuGoBack.setVisible(true);
				menuDataSpace.setVisible(true);
				menuExperiment.setVisible(true);
				menuComputations.setVisible(true);
			}

			if (currentSelection != null
					&& currentSelection.compareTo(MenuType.EXPERIMENT) == 0)
				menuExperiment.removeStyleName("menuItemImage-selected");
			else if (currentSelection != null
					&& currentSelection.compareTo(MenuType.DATA_SPACE) == 0)
				menuDataSpace.removeStyleName("menuItemImage-selected");
			else if (currentSelection != null
					&& currentSelection.compareTo(MenuType.COMPUTATIONS) == 0)
				menuComputations.removeStyleName("menuItemImage-selected");

			Image imgNew = (menuType.compareTo(MenuType.DATA_SPACE) == 0 ? menuDataSpace
					: (menuType.compareTo(MenuType.EXPERIMENT) == 0 ? menuExperiment
							: menuComputations));

			imgNew.addStyleName("menuItemImage-selected");
		}

		currentSelection = menuType;
		return;
	}
	/**
	 * @param inputSpace
	 */
	private void menuSwitch(MenuSwitchEvent event) {
		Log.debug("MenuSwitch: " + event);

		if (event.getMenuType().compareTo(MenuType.HOME) == 0) {
			menuGoBack.setVisible(false);
			menuDataSpace.setVisible(false);
			menuExperiment.setVisible(false);
			menuComputations.setVisible(false);

			if (currentSelection != null
					&& currentSelection.compareTo(MenuType.EXPERIMENT) == 0)
				menuExperiment.removeStyleName("menuItemImage-selected");
			else if (currentSelection != null
					&& currentSelection.compareTo(MenuType.DATA_SPACE) == 0)
				menuDataSpace.removeStyleName("menuItemImage-selected");
			else if (currentSelection != null
					&& currentSelection.compareTo(MenuType.COMPUTATIONS) == 0)
				menuComputations.removeStyleName("menuItemImage-selected");
		} else {
			if (currentSelection == null
					|| (currentSelection != null && currentSelection
							.compareTo(MenuType.HOME) == 0)) {
				menuGoBack.setVisible(true);
				menuDataSpace.setVisible(true);
				menuExperiment.setVisible(true);
				menuComputations.setVisible(true);
			}

			if (currentSelection != null
					&& currentSelection.compareTo(MenuType.EXPERIMENT) == 0)
				menuExperiment.removeStyleName("menuItemImage-selected");
			else if (currentSelection != null
					&& currentSelection.compareTo(MenuType.DATA_SPACE) == 0)
				menuDataSpace.removeStyleName("menuItemImage-selected");
			else if (currentSelection != null
					&& currentSelection.compareTo(MenuType.COMPUTATIONS) == 0)
				menuComputations.removeStyleName("menuItemImage-selected");

			Image imgNew = (event.getMenuType().compareTo(MenuType.DATA_SPACE) == 0 ? menuDataSpace
					: (event.getMenuType().compareTo(MenuType.EXPERIMENT) == 0 ? menuExperiment
							: menuComputations));

			imgNew.addStyleName("menuItemImage-selected");
		}

		currentSelection = event.getMenuType();
		return;
	}

}
