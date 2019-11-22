package org.gcube.portlets.widgets.workspacesharingwidget.server.notifications;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.gcube.applicationsupportlayer.social.NotificationsManager;
import org.gcube.applicationsupportlayer.social.shared.SocialFileItem;
import org.gcube.applicationsupportlayer.social.shared.SocialSharedFolder;
import org.gcube.common.scope.impl.ScopeBean;
import org.gcube.common.storagehub.model.items.FolderItem;
import org.gcube.common.storagehub.model.items.Item;
import org.gcube.common.storagehub.model.items.SharedFolder;
import org.gcube.portlets.widgets.workspacesharingwidget.server.util.DiffereceBeetweenInfoContactModel;
import org.gcube.portlets.widgets.workspacesharingwidget.server.util.UserUtil;
import org.gcube.portlets.widgets.workspacesharingwidget.server.util.WsUtil;
import org.gcube.portlets.widgets.workspacesharingwidget.shared.InfoContactModel;

/**
 * The Class NotificationsProducer.
 *
 * @author Francesco Mangiacrapa  Nov 25, 2016
 */
public class NotificationsProducer {

	protected ScopeBean scope;
	protected static Logger logger = Logger.getLogger(NotificationsProducer.class);
	protected NotificationsManager notificationsMng;
	protected String userId;

	/**
	 * Instantiates a new notifications producer.
	 *
	 * @param request
	 *            the request
	 */
	public NotificationsProducer(HttpServletRequest request) {
		this.notificationsMng = WsUtil.getNotificationManager(request);
		this.userId = WsUtil.getPortalContext(request).getUsername();
	}

	/**
	 * Gets the notifications mng.
	 *
	 * @return the notifications mng
	 */
	public NotificationsManager getNotificationsMng() {
		return notificationsMng;
	}

	/**
	 * Sets the notification mng.
	 *
	 * @param notificationMng
	 *            the new notification mng
	 */
	public void setNotificationMng(NotificationsManager notificationMng) {
		this.notificationsMng = notificationMng;
	}

	/**
	 * Runs a new thread to notify the contacts passed in input.
	 *
	 * @param listContacts
	 *            the list contacts
	 * @param folderItem
	 *            the shared folder
	 */
	public void notifyFolderSharing(final List<InfoContactModel> listContacts, final FolderItem folderItem) {

		new Thread() {
			@Override
			public void run() {

				logger.trace("Send notifies folder sharing is running...");

				for (InfoContactModel infoContactModel : listContacts) {
					try {
						// NOTIFIES ONLY THE USERS THAT ARE DIFFERENT FROM
						// CURRENT USER
						if (infoContactModel.getLogin().compareTo(userId) != 0) {

							logger.trace("Sending notification new share folder " + folderItem.getName()
									+ " for user " + infoContactModel.getLogin());

							// DEBUG
							System.out.println("Sending notification new share folder " + folderItem.getName()
									+ " for user " + infoContactModel.getLogin());
							SocialItemFactory socialItemFactor = new SocialItemFactory();
							SocialSharedFolder socialSharedFolder = socialItemFactor
									.createSocialFolder(folderItem);

							boolean notify = notificationsMng.notifyFolderSharing(infoContactModel.getLogin(),
									socialSharedFolder);

							if (!notify)
								logger.error("An error occured when notify user: " + infoContactModel.getLogin());
						}
					} catch (Exception e) {
						logger.error("An error occured in notifyFolderSharing ", e);
						// e.printStackTrace();
					}
				}

				logger.trace("notifies share folder is completed");
			}
		}.start();

	}

	/**
	 * Runs a new thread to notify the contacts passed in input.
	 *
	 * @param listSharedContact
	 *            the list shared contact
	 * @param folderItem
	 *            the folder item
	 * @param itemOldName
	 *            the item old name
	 * @param itemNewName
	 *            the item new name
	 * @param idsharedFolder
	 *            the idshared folder
	 */
	public void notifyFolderRenamed(final List<InfoContactModel> listSharedContact, final Item folderItem,
			final String itemOldName, final String itemNewName, final String idsharedFolder) {

		new Thread() {
			@Override
			public void run() {

				logger.trace("Send notifies shared folder was renamed is running...");

				try {
					NotificationsUtil notificationsUtil=new NotificationsUtil();
					if (notificationsUtil.checkIsRootFolderShared(folderItem.getId(), idsharedFolder)) {
						logger.trace("Notification isn't sent because the event is on root shared folder");
						return;
					}

				} catch (Exception e1) {
					logger.error("An error occurred in checkIsRootFolderShared ", e1);
					return;
				}

				for (InfoContactModel infoContactModel : listSharedContact) {
					try {
						// NOTIFIES ONLY THE USERS THAT ARE DIFFERENT FROM
						// CURRENT USER
						if (infoContactModel.getLogin().compareTo(userId) != 0) {

							logger.trace("Sending notification share folder " + itemOldName + " was renamed as "
									+ itemNewName + "for user " + infoContactModel.getLogin());

							// DEBUG
							System.out.println("Sending notification share folder " + itemOldName + " was renamed as "
									+ itemNewName + "for user " + infoContactModel.getLogin());

							boolean notify = notificationsMng.notifyFolderRenaming(infoContactModel.getLogin(),
									itemOldName, itemNewName, idsharedFolder);

							if (!notify)
								logger.error("An error occured when notify user: " + infoContactModel.getLogin());
						}
					} catch (Exception e) {
						logger.error("An error occured in notifyFolderRenamed ", e);
						// e.printStackTrace();
					}
				}

				logger.trace("notifies share folder was renamed is completed");
			}
		}.start();

	}

	/**
	 * Runs a new thread to notify the contacts passed in input.
	 *
	 * @param listSharedContact
	 *            the list shared contact
	 * @param previousName
	 *            the previous name
	 * @param item
	 *            the item
	 * @param folderItem
	 *            the shared folder
	 */
	public void notifyItemRenamed(final List<InfoContactModel> listSharedContact, final String previousName,
			final Item item, final FolderItem folderItem) {

		new Thread() {
			@Override
			public void run() {

				logger.trace("Send notifies shared item was updated is running...");

				for (InfoContactModel infoContactModel : listSharedContact) {
					try {
						// NOTIFIES ONLY THE USERS THAT ARE DIFFERENT FROM
						// CURRENT USER
						if (infoContactModel.getLogin().compareTo(userId) != 0) {

							logger.trace("Sending notification to user " + infoContactModel.getLogin()
									+ " updated item " + item.getName());

							// DEBUG
							System.out.println("Sending notification to user " + infoContactModel.getLogin()
									+ " updated item " + item.getName());

							// notificationsMng.notifyItemRenaming(infoContactModel.getLogin(),
							// previousName, item, sharedFolder);
							SocialItemFactory socialItemFactor = new SocialItemFactory();
							SocialSharedFolder socialSharedFolder = socialItemFactor
									.createSocialFolder(folderItem);
							SocialFileItem socialFileItem = socialItemFactor.createSocialFileItem(item, folderItem);

							boolean notify = notificationsMng.notifyItemRenaming(infoContactModel.getLogin(),
									previousName, socialFileItem, socialSharedFolder);

							if (!notify)
								logger.error("An error occured when notify user: " + infoContactModel.getLogin());
						}
					} catch (Exception e) {
						logger.error("An error occured in notifyItemUpdated ", e);
						// e.printStackTrace();
					}
				}

				logger.trace("notifies shared item was updated is completed");
			}
		}.start();

	}

	/**
	 * Runs a new thread to notify the new contacts passed in input.
	 *
	 * @param listSharedContact
	 *            - list of contacts already shared
	 * @param listSharingContact
	 *            - list of "new" contacts witch share
	 * @param folderItem
	 *            - the shared folder
	 */
	public void notifyAddedUsersToSharing(final List<InfoContactModel> listSharedContact,
			final List<InfoContactModel> listSharingContact, final FolderItem folderItem) {

		new Thread() {
			@Override
			public void run() {

				try {

					DiffereceBeetweenInfoContactModel diff = new DiffereceBeetweenInfoContactModel(listSharingContact,
							listSharedContact);

					List<InfoContactModel> listExclusiveContacts = diff.getDifferentsContacts();

					System.out.println("list exclusive contacts: " + listExclusiveContacts);

					if (listExclusiveContacts.size() > 0) {

						if (listExclusiveContacts.size() == 1) { // CASE ONLY
																	// ONE
																	// CONTACS
																	// WAS ADDED

							InfoContactModel infoContactModel = listExclusiveContacts.get(0);

							for (InfoContactModel contact : listSharedContact) { // NOTIFIES
																					// ALREADY
																					// SHARED
																					// CONTACTS
																					// THATH
																					// A
																					// NEW
																					// USER
																					// WAS
																					// ADDED

								try {

									logger.trace("Sending notification to user " + contact.getLogin() + ", added user "
											+ infoContactModel.getLogin() + " to share folder "
											+ folderItem.getName());

									// DEBUG
									// System.out.println("Sending notification
									// added user "+ infoContactModel.getLogin()
									// +" to share folder
									// "+sharedFolder.getName() + " for user
									// "+contact.getLogin());
									SocialItemFactory socialItemFactor = new SocialItemFactory();
									SocialSharedFolder socialSharedFolder = socialItemFactor
											.createSocialFolder(folderItem);

									boolean notify = notificationsMng.notifyFolderAddedUser(contact.getLogin(),
											socialSharedFolder, infoContactModel.getLogin());

									if (!notify)
										logger.error("An error occured when notifies user: " + contact.getLogin());

								} catch (Exception e) {
									logger.error("An error occured in notifyFolderAddedUser ", e);
									// e.printStackTrace();
								}
							}

							List<InfoContactModel> listCts = new ArrayList<InfoContactModel>();
							listCts.add(infoContactModel);
							notifyFolderSharing(listCts, folderItem); // NOTIFIER
																		// NEW
																		// USER
																		// OF
																		// SHARING
																		// FOLDER

						} else { // CASE MORE THEN ONE CONTACS WAS ADDED

							List<String> listLogins = UserUtil.getListLoginByInfoContactModel(listExclusiveContacts);

							for (InfoContactModel contact : listSharedContact) { // NOTIFIES
																					// ALREADY
																					// SHARED
																					// CONTACTS
																					// THATH
																					// A
																					// NEW
																					// USER
																					// WAS
																					// ADDED

								try {

									logger.trace("Sending notification to user " + contact.getLogin() + ", added "
											+ listLogins.size() + " users to share folder " + folderItem.getName());

									// DEBUG
									// System.out.println("Sending notification
									// added user "+ infoContactModel.getLogin()
									// +" to share folder
									// "+sharedFolder.getName() + " for user
									// "+contact.getLogin());
									SocialItemFactory socialItemFactor = new SocialItemFactory();
									SocialSharedFolder socialSharedFolder = socialItemFactor
											.createSocialFolder(folderItem);

									boolean notify = notificationsMng.notifyFolderAddedUsers(contact.getLogin(),
											socialSharedFolder, listLogins);

									if (!notify)
										logger.error("An error occured when notifies user: " + contact.getLogin());

								} catch (Exception e) {
									logger.error("An error occured in notifyFolderAddedUser ", e);
									// e.printStackTrace();
								}
							}

							notifyFolderSharing(listExclusiveContacts, folderItem); // NOTIFIER
																						// NEW
																						// USER
																						// OF
																						// SHARING
																						// FOLDER

						}
					}

				} catch (Exception e) {
					logger.error("An error occured in notifyAddedUserToSharing ", e);
					// e.printStackTrace();
				}
			}

		}.start();

	}

	/**
	 * Runs a new thread to notify the contacts passed in input.
	 *
	 * @param listContacts
	 *            the list contacts
	 * @param folderItem
	 *            the un shared folder
	 */
	public void notifyFolderUnSharing(final List<InfoContactModel> listContacts, final FolderItem folderItem) {

		new Thread() {
			@Override
			public void run() {

				// printContacts(listContacts);
				logger.trace("Send notifies folder un share is running...");

				for (InfoContactModel infoContactModel : listContacts) {
					try {

						// NOTIFIES ONLY THE USERS THAT ARE DIFFERENT FROM
						// CURRENT USER
						if (infoContactModel.getLogin().compareTo(userId) != 0) {

							logger.trace("Sending notification to user " + infoContactModel.getLogin()
									+ " un shared folder " + folderItem.getName());

							// DEBUG
							// System.out.println("Sending notification to user
							// "+infoContactModel.getLogin() +" un shared folder
							// "+unSharedFolder.getName());
							SocialItemFactory socialItemFactor = new SocialItemFactory();
							SocialSharedFolder socialSharedFolder = socialItemFactor
									.createSocialFolder(folderItem);

							boolean notify = notificationsMng.notifyFolderRemovedUser(infoContactModel.getLogin(),
									socialSharedFolder);

							if (!notify)
								logger.error("An error occured when notifies user: " + infoContactModel.getLogin());
						}
					} catch (Exception e) {
						logger.error("An error occured in notifyFolderUnSharing ", e);
						// e.printStackTrace();
					}
				}

				logger.trace("notifies of un share notifications is completed");
			}

		}.start();

	}

	/**
	 * Runs a new thread to notify the contacts passed in input.
	 *
	 * @param listContacts
	 *            the list contacts
	 * @param item
	 *            the workspace item
	 * @param folderItem
	 *            the shared folder
	 */
	public void notifyAddedItemToSharing(final List<InfoContactModel> listContacts, final Item item,
			final FolderItem folderItem) {

		new Thread() {
			@Override
			public void run() {

				// printContacts(listContacts);
				logger.trace("Send notifies added item in sharedfolder is running...");

				// DEBUG
				System.out.println("Send notifies added item in sharedfolder is running...");

				for (InfoContactModel infoContactModel : listContacts) {
					try {

						// NOTIFIES ONLY THE USERS THAT ARE DIFFERENT FROM
						// CURRENT USER
						if (infoContactModel.getLogin().compareTo(userId) != 0) {

							logger.trace("Sending notification to user " + infoContactModel.getLogin() + " added item "
									+ item.getName() + " in shared folder " + folderItem.getName());

							// DEBUG
							System.out.println("Sending notification to user " + infoContactModel.getLogin()
									+ " added item " + item.getName() + " in shared folder " + folderItem.getName());

							// DEBUG
							// System.out.println("Send notify folder un share
							// user "+infoContactModel.getLogin());
							SocialItemFactory socialItemFactor = new SocialItemFactory();
							SocialSharedFolder socialSharedFolder = socialItemFactor
									.createSocialFolder(folderItem);
							SocialFileItem socialFileItem = socialItemFactor.createSocialFileItem(item, folderItem);

							boolean notify = notificationsMng.notifyAddedItem(infoContactModel.getLogin(),
									socialFileItem, socialSharedFolder);

							if (!notify) {
								logger.error("An error occured when notify user: " + infoContactModel.getLogin());
								// DEBUG
								// System.out.println("An error occured when
								// notify user: "+infoContactModel.getLogin());
							}
						}
					} catch (Exception e) {
						logger.error("An error occured in notifyAddedItemToSharing ", e);
						// e.printStackTrace();
					}
				}

				logger.trace("notifies of added item in shared folder is completed");

				// DEBUG
				// System.out.println("notifies of added item in shared folder
				// is completed");
			}

		}.start();
	}

	/**
	 * Runs a new thread to notify the contacts passed in input.
	 *
	 * @param listContacts
	 *            the list contacts
	 * @param item
	 *            the workspace item
	 * @param folderItem
	 *            the shared folder
	 */
	public void notifyUpdatedItemToSharing(final List<InfoContactModel> listContacts, final Item item,
			final FolderItem folderItem) {

		new Thread() {
			@Override
			public void run() {

				// printContacts(listContacts);
				logger.trace("Send notifies updated item in shared folder is running...");

				// DEBUG
				// System.out.println("Send notifies updated item in shared
				// folder is running...");

				for (InfoContactModel infoContactModel : listContacts) {
					try {

						// NOTIFIES ONLY THE USERS THAT ARE DIFFERENT FROM
						// CURRENT USER
						if (infoContactModel.getLogin().compareTo(userId) != 0) {

							logger.trace(
									"Sending notification to user " + infoContactModel.getLogin() + " updated item "
											+ item.getName() + " in shared folder " + folderItem.getName());

							// DEBUG
							// System.out.println("Sending notification to user
							// "+infoContactModel.getLogin() +" updated item
							// "+workspaceItem.getName()+" in shared folder
							// "+sharedFolder.getName());

							// DEBUG
							// System.out.println("Send notify folder un share
							// user "+infoContactModel.getLogin());
							SocialItemFactory socialItemFactor = new SocialItemFactory();
							SocialSharedFolder socialSharedFolder = socialItemFactor
									.createSocialFolder(folderItem);
							SocialFileItem socialFileItem = socialItemFactor.createSocialFileItem(item, folderItem);

							boolean notify = notificationsMng.notifyUpdatedItem(infoContactModel.getLogin(),
									socialFileItem, socialSharedFolder);

							if (!notify) {
								logger.error("An error updated when notify user: " + infoContactModel.getLogin());
								// DEBUG
								// System.out.println("An error updated when
								// notify user: "+infoContactModel.getLogin());
							}
						}
					} catch (Exception e) {
						logger.error("An error updated in notifyAddedItemToSharing ", e);
						e.printStackTrace();
					}
				}

				logger.trace("notifies of updated item in shared folder is completed");

				// DEBUG
				// System.out.println("notifies of updated item in shared folder
				// is completed");
			}

		}.start();
	}

	/**
	 * Runs a new thread to notify the contacts passed in input.
	 *
	 * @param listContacts
	 *            the list contacts
	 * @param item
	 *            the workspace item
	 * @param folderItem
	 *            the shared folder
	 */
	public void notifyMovedItemToSharing(final List<InfoContactModel> listContacts, final Item item,
			final FolderItem folderItem) {

		new Thread() {
			@Override
			public void run() {

				logger.trace("Sending notification remove item in shared folder is running...");
				// printContacts(listContacts);

				try {
					NotificationsUtil notificationsUtil=new NotificationsUtil();
					if (notificationsUtil.checkIsRootFolderShared(item.getId(), folderItem.getId())) {
						logger.trace("Notification isn't sent because the event is on root shared folder");
						return;
					}

				} catch (Exception e1) {
					logger.error("An error occurred in checkIsRootFolderShared ", e1);
					return;
				}

				logger.trace("Sending notification moved item in shared folder is running...");

				// System.out.println("Sending notification moved item in shared
				// folder is running...");

				for (InfoContactModel infoContactModel : listContacts) {
					try {

						// NOTIFIES ONLY THE USERS THAT ARE DIFFERENT FROM
						// CURRENT USER
						if (infoContactModel.getLogin().compareTo(userId) != 0) {

							logger.trace("Sending notification  to user " + infoContactModel.getLogin() + " moved item "
									+ item.getName() + " in shared folder " + folderItem.getName());

							// DEBUG
							// System.out.println("Sending notification to user
							// "+infoContactModel.getLogin() +" moved item
							// "+workspaceItem.getName()+" in shared folder
							// "+sharedFolder.getName());
							SocialItemFactory socialItemFactor = new SocialItemFactory();
							SocialSharedFolder socialSharedFolder = socialItemFactor
									.createSocialFolder(folderItem);
							SocialFileItem socialFileItem = socialItemFactor.createSocialFileItem(item, folderItem);

							boolean notify = notificationsMng.notifyMovedItem(infoContactModel.getLogin(),
									socialFileItem, socialSharedFolder);

							if (!notify) {
								logger.error("An error occured when notify user: " + infoContactModel.getLogin());

								// DEBUG
								// System.out.println("An error occured when
								// notify user: "+infoContactModel.getLogin());
							}
						}
					} catch (Exception e) {
						logger.error("An error occurred in notifyMovedItemToSharing ", e);
						e.printStackTrace();
					}
				}

				logger.trace("notifies of moved item in shared folder is completed");

				// DEBUG
				// System.out.println("notifies of moved item in shared folder
				// is completed");
			}

		}.start();

	}

	/**
	 * Runs a new thread to notify the contacts passed in input.
	 *
	 * @param listContacts
	 *            the list contacts
	 * @param itemName
	 *            the item name
	 * @param folderItem
	 *            the shared folder
	 */
	public void notifyRemovedItemToSharing(final List<InfoContactModel> listContacts, final String itemName,
			final FolderItem folderItem) {

		new Thread() {
			@Override
			public void run() {

				logger.trace("Sending notification remove item in shared folder is running...");
				// printContacts(listContacts);

				if (itemName == null || itemName.isEmpty()) {
					logger.trace("Notification isn't sent - itemName is null or empty");
					return;
				}

				if (folderItem == null) {
					logger.trace("Notification isn't sent - sharedFolder is null");
				}

				logger.trace("Sending notification removed item in shared folder is running...");

				// System.out.println("Sending notification removed item in
				// shared folder is running...");

				for (InfoContactModel infoContactModel : listContacts) {
					try {

						// NOTIFIES ONLY THE USERS THAT ARE DIFFERENT FROM
						// CURRENT USER
						if (infoContactModel.getLogin().compareTo(userId) != 0) {

							logger.trace("Sending notification  to user " + infoContactModel.getLogin()
									+ " removed item " + itemName + " in shared folder " + folderItem.getName());

							// DEBUG
							// System.out.println("Sending notification to user
							// "+infoContactModel.getLogin() +" removed item
							// "+itemName+" in shared folder
							// "+sharedFolder.getName());
							SocialItemFactory socialItemFactor=new SocialItemFactory();
							SocialSharedFolder socialSharedFolder=socialItemFactor.createSocialFolder(folderItem);
							
							boolean notify = notificationsMng.notifyRemovedItem(infoContactModel.getLogin(), itemName,
									socialSharedFolder);

							if (!notify) {
								logger.error("An error occured when notify user: " + infoContactModel.getLogin());

								// DEBUG
								// System.out.println("An error occured when
								// notify user: "+infoContactModel.getLogin());
							}
						}
					} catch (Exception e) {
						logger.error("An error occurred in notifyRemovedItemToSharing ", e);
						// e.printStackTrace();
					}
				}

				logger.trace("notifies of moved item in shared folder is completed");

				// DEBUG
				// System.out.println("notifies of moved item in shared folder
				// is completed");
			}
		}.start();

	}

	/**
	 * Runs a new thread to notify the contacts passed in input.
	 *
	 * @param listContacts
	 *            the list contacts
	 * @param folderNameDeleted
	 *            the folder name deleted
	 */
	public void notifySharedFolderDeleted(final List<InfoContactModel> listContacts, final String folderNameDeleted) {

		new Thread() {
			@Override
			public void run() {

				// printContacts(listContacts);
				logger.trace("Send notifies shared folder deleted is running...");

				for (InfoContactModel infoContactModel : listContacts) {
					try {

						// NOTIFIES ONLY THE USERS THAT ARE DIFFERENT FROM
						// CURRENT USER
						if (infoContactModel.getLogin().compareTo(userId) != 0) {

							logger.trace("Sending notification to user " + infoContactModel.getLogin()
									+ " deleted shared folder " + folderNameDeleted);

							// DEBUG
							System.out.println("Sending notification to user " + infoContactModel.getLogin()
									+ " deleted shared folder " + folderNameDeleted);

							// TODO
							// boolean notify = notificationsMng.

							// if(!notify)
							// logger.error("An error occured when notifies
							// user: "+infoContactModel.getLogin());
						}
					} catch (Exception e) {
						logger.error("An error occured in notifySharedFolderDeleted ", e);
						// e.printStackTrace();
					}
				}

				logger.trace("notifies of deleted shared foder is completed");
			}

		}.start();

	}

	// DEBUG
	/**
	 * Prints the contacts.
	 *
	 * @param listContacts
	 *            the list contacts
	 */
	private void printContacts(List<InfoContactModel> listContacts) {

		System.out.println("Print contacts");
		for (InfoContactModel infoContactModel : listContacts) {
			System.out.println(infoContactModel);
		}
		System.out.println("End print contacts");
	}

	/*
	 * public static void main(String[] args) throws Exception { String
	 * sessionID = "1"; String user = "francesco.mangiacrapa"; String
	 * scopeString = "/gcube/devsec/devVRE"; String fullName =
	 * "Francesco Mangiacrapa";
	 * 
	 * ScopeBean scope; ASLSession session;
	 * 
	 * session = SessionManager.getInstance().getASLSession(sessionID, user);
	 * scope = new ScopeBean(scopeString); session.setScope(scope.toString());
	 * session.setUserAvatarId(user + "Avatar");
	 * session.setUserFullName(fullName);
	 * 
	 * 
	 * NotificationsProducer feeder = new NotificationsProducer(session); }
	 */

}