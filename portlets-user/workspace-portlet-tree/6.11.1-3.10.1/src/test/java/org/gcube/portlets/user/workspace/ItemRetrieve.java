/**
 *
 */
package org.gcube.portlets.user.workspace;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.gcube.common.homelibrary.home.HomeLibrary;
import org.gcube.common.homelibrary.home.exceptions.HomeNotFoundException;
import org.gcube.common.homelibrary.home.exceptions.InternalErrorException;
import org.gcube.common.homelibrary.home.exceptions.UserNotFoundException;
import org.gcube.common.homelibrary.home.workspace.Workspace;
import org.gcube.common.homelibrary.home.workspace.WorkspaceFolder;
import org.gcube.common.homelibrary.home.workspace.WorkspaceItem;
import org.gcube.common.homelibrary.home.workspace.WorkspaceSharedFolder;
import org.gcube.common.homelibrary.home.workspace.exceptions.WorkspaceFolderNotFoundException;
import org.gcube.common.homelibrary.home.workspace.folder.FolderItem;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.common.scope.impl.ScopeBean;

/**
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 * @Jan 29, 2014
 *
 */
public class ItemRetrieve {


//	public static String DEFAULT_SCOPE = "/d4science.research-infrastructures.eu/gCubeApps"; //PRODUCTION
	public static String DEFAULT_SCOPE = "/gcube/devsec"; //DEV
	public static String TEST_USER = "giancarlo.panichi";
//	public static String ITEMID = "63832213-098d-42d1-8774-89b6349764c0"; //Activity T3.4 working drafts/T2-EC-IMAR-HO-14-015  iMarine Sustainability WP - Business Model tools.pdf
	public static String ITEMID = "79b2490b-43aa-40b0-98ec-e073cf7d4887";
//	d81d3a64-603f-4907-ae74-be8353211807
	protected static Logger logger = Logger.getLogger(ItemRetrieve.class);


	public static void main(String[] args) {

		ScopeBean scope = new ScopeBean(DEFAULT_SCOPE);
		ScopeProvider.instance.set(scope.toString());

		System.out.println("init HL");
		try {
			Workspace ws = getWorkspace();


//			retrieveFirstLevel(ws, null);

			retrieveFirstLevel(ws, ITEMID);

			System.out.println("get workspace -> OK");
			WorkspaceItem item = ws.getItem(ITEMID);
			System.out.println("get item id: "+item.getId()+", name: "+item.getName() +", parent: "+item.getParent());
			System.out.println(item);
		}catch(Exception e){
			e.printStackTrace();
		}

//		} catch (WorkspaceFolderNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InternalErrorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (HomeNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UserNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
	}

	public static Workspace getWorkspace() throws InternalErrorException, HomeNotFoundException, WorkspaceFolderNotFoundException, UserNotFoundException
	{

		logger.trace("Get Workspace scope: "+DEFAULT_SCOPE + " username: "+TEST_USER);
		ScopeProvider.instance.set(DEFAULT_SCOPE);
		logger.trace("Scope provider instancied");

//		return HomeLibrary.getUserWorkspace(TEST_USER);
		return HomeLibrary.getHomeManagerFactory().getHomeManager().getHome(TEST_USER).getWorkspace();
	}


	private static void retrieveFirstLevel(Workspace ws, String foundFolder){

		try {

			//TEST TIME
			Long startTime =  System.currentTimeMillis();
			Long endTime = System.currentTimeMillis() - startTime;
			String time = String.format("%d msc %d sec", endTime, TimeUnit.MILLISECONDS.toSeconds(endTime));

			startTime =  System.currentTimeMillis();
			System.out.println("Start foundFolder at time: "+startTime);
//			WorkspaceItem root = ws.getItem(ITEMID);
			WorkspaceItem fd = ws.getItem(foundFolder);
			System.out.println("foundFolder id: "+foundFolder);
			System.out.println("start get children");
			List<? extends WorkspaceItem> children = fd.getChildren();
			System.out.println("tree getChildren() returning "+children.size()+" elements in " + time);

			System.out.println("children size: "+children.size());

			int i=0;
			int foldersCounter = 0;
			int sharedFoldersCounter = 0;
			int folderItemCounter = 0;
			int othersCounter = 0;
			for (WorkspaceItem workspaceItem : children) {

				/*if(foundFolder!=null && !foundFolder.isEmpty()){
					if(workspaceItem.getId().compareTo(foundFolder)==0){
						System.out.println("ITEM FOUND id: "+workspaceItem.getId()+", name: "+workspaceItem.getName());
						return;
					}
				}*/


				switch (workspaceItem.getType()) {

				case FOLDER:

					WorkspaceFolder folder = (WorkspaceFolder) workspaceItem;

					System.out.println(++i+") "+folder.getId() +" folder name: "+folder.getName() + " owner "+folder.getOwner());
					foldersCounter++;

					break;

				case SHARED_FOLDER:

					WorkspaceSharedFolder shared = (WorkspaceSharedFolder) workspaceItem;

					System.out.println(++i+")  "+shared.getId() +" shared folder name: "+shared.getName() + " owner "+shared.getOwner()+" isVRE "+shared.isVreFolder() +" ACLOwner: "+shared.getACLOwner()+" AclUser: "+shared.getACLUser());

//					System.out.println(++i+") shared folder name: "+shared.getName() + " owner "+shared.getOwner().getPortalLogin());
					sharedFoldersCounter++;

					break;

				case FOLDER_ITEM:

					FolderItem folderItem = (FolderItem) workspaceItem;

					System.out.println(++i+") folderItem id: "+folderItem.getId() +", name: "+folderItem.getName() + ", own: "+folderItem.getOwner());
					folderItemCounter++;

					break;
				default:
					System.out.println(++i+") DEFAULT - item id: "+workspaceItem.getId() +", name: "+workspaceItem.getName() + ", own: "+workspaceItem.getOwner());
					othersCounter++;
					break;
				}
			}

			//TEST TIME
			endTime = System.currentTimeMillis();
			long difference = endTime - startTime;
			time = String.format("%d msc %d sec", difference, TimeUnit.MILLISECONDS.toSeconds(difference));
			System.out.println("End time is "+endTime+ " difference is "+time);
			System.out.println("Folders are: "+foldersCounter);
			System.out.println("Shared Folders are: "+sharedFoldersCounter);
			System.out.println("Folder Item are: "+folderItemCounter);
			System.out.println("Others are: "+othersCounter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
