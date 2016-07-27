package org.gcube.portlets.user.statisticalalgorithmsimporter.client.maindata;

import com.google.gwt.i18n.client.Messages;

/**
 * 
 * @author giancarlo email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public interface CodeEditMessages extends Messages {

	//
	@DefaultMessage("Save")
	String btnSaveText();
	
	@DefaultMessage("Save")
	String btnSaveToolTip();
	
	@DefaultMessage("Input")
	String btnAddInputText();
	
	@DefaultMessage("Add input variable from code")
	String btnAddInputToolTip();

	@DefaultMessage("Output")
	String btnAddOutputText();
	
	@DefaultMessage("Add output variable from code")
	String btnAddOutputToolTip();
	
	@DefaultMessage("Main:")
	String mainCodeFiledLabel();
	
	@DefaultMessage("Select parameter in the code!")
	String attentionSelectParameterInTheCode();
	
	@DefaultMessage("No valid selected row, change selection and try again!")
	String attentionNoValidSelectedRow();
	
	@DefaultMessage("Code Saved")
	String codeSavedHead();
	
	@DefaultMessage("Code is saved!")
	String codeSaved();
	
	@DefaultMessage("Attention invalid file name for Main Code!")
	String attentionInvalidFileNameForMainCode();
	
	
	
}