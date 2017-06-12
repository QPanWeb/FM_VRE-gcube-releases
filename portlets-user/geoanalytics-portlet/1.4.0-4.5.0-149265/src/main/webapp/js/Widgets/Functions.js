function buildAccordion(number, pluginObjects) {
	if(number === RIGHTSIDE_ACCORDION){
		buildAccordionForRightSidePanel(pluginObjects);
	}else if(number === RESPONSIVE_TOOLBAR_ACCORDION){
		buildAccordionForResponsiveToolbar();
	}
}

function buildAccordionForResponsiveToolbar(){
	populateAccordionWithHTMLEelemmentsBeforeInitializing();
	populateAccordionWithHTMLEelemmentsBeforeInitializing2();
	showMoreLessText();
	
	$('.levelOfDetailSlider').slider({
		range: 'min',
		value: 100,
		min: 0,
		max: 100,
		step: 1,
		slide: function(event, ui){
			$('.levelOfDetailValue').text(ui.value);
		}
	});
	$('.levelOfDetailValue').text(100);
	$( ".functionsAccordion .modelChooseALayer" ).selectmenu();
	$('#slider').slider();
	$('.functionsAccordion').accordion({
		active : 0,
		collapsible: true,
		header : 'h4',
		icons: false,
		heightStyle: "fill"
	});
}

function buildAccordionForRightSidePanel(pluginObjects){
	populateAccordionWithHTMLEelemmentsBeforeInitializing(pluginObjects);
	showMoreLessText();
	rightSidePanelHandlersAndEvents();
	
	$('.levelOfDetailSlider').slider({
		range: 'min',
		value: 100,
		min: 0,
		max: 100,
		step: 1,
		slide: function(event, ui){
			$('.levelOfDetailValue').text(ui.value);
		}
	});
	$('.levelOfDetailValue').text(100);
	$( ".functionsAccordion .modelChooseALayer" ).selectmenu({
		select : function(event, ui){
			var principalName = userinfoObject.fullname;
			var tenantName = userinfoObject.tenant;
			var pluginName = ui.item.label;
			var projectName = userinfoObject.projectName;
			var pluginId = ui.item.value;
			
			pluginIdOfLatestLoadedPlugin = pluginId;
			
			var parameters = "principalName=" + encodeURIComponent(principalName)
							+ "&tenantName=" + encodeURIComponent(tenantName)
							+ "&pluginName=" + encodeURIComponent(pluginName)
							+ "&projectName=" + encodeURIComponent(projectName)
							+ "&pluginId=" + encodeURIComponent(pluginId);
			
			var callback = function(data){
				hideSpinner();
			};
			
			var urlMVC = encodeURIComponent("plugin/loadPluginByNameAndTenant");
			var url = createLink(resourceURL, urlMVC, parameters);
			var context = null;
			
			showSpinner();
			appendFunctionContainerToDom(pluginName);
			
			$.getScript(url)
				.done(function( script, textStatus ) {
					hideSpinner();
					removeNewPluginHelperClasses();
				})
				.fail(function( jqxhr, settings, exception ) {
					$('.wizard').modal('hide');
					$('#InternalServerErrorModal').modal('show');
					destroyNewPluginContainer();
					hideSpinner();
				});
		}
	});
	$('#slider').slider();
	$('.functionsAccordion').accordion({
		active : 0,
		collapsible: true,
		header : 'h4',
		icons: false,
		heightStyle: "fill"
	});
}

function populateAccordionWithHTMLEelemmentsBeforeInitializing(pluginObjects) {
	$.each(pluginObjects, function(index, value){
		var header = $('<h4></h4>', {
			class: 'functionsAccordionTitle ',
			text: value.pluginName
		});
		
		var pluginLoadingUISpinner = $('<div></div>', {
			class : 'pluginLoadingUISpinner hidden',
			text : "Loading UI..."
		});
		
		var pluginProcessingSpinner = $('<div></div>', {
			class : 'pluginProcessingSpinner hidden',
			text : "Processing..."
		});
		
		var spinnerIcon = $('<i></i>',{
			class : 'fa fa-spinner fa-spin'
		});
		
		pluginLoadingUISpinner.append(spinnerIcon);
		pluginProcessingSpinner.append(spinnerIcon.clone());
		header.append(pluginLoadingUISpinner).append(pluginProcessingSpinner);
		
		var contentPanel = $('<div></div>', {
			class : 'functionsAccordionContentPanel'
		});
		
		var pluginProcessingSpinnerForPluginBody = $('<div></div>', {
			class : 'pluginProcessingSpinnerForPluginBody hidden'
		});
		
		pluginProcessingSpinnerForPluginBody.append(spinnerIcon.clone());
		contentPanel.append(pluginProcessingSpinnerForPluginBody);
		
		var descriptionElement = $('<div></div>', {
			class : 'more functionDescription',
			text: value.pluginDescription// + ' This is a test function.'//got to find a way to pass the description to the element
		});
		
		var form = $('<form></form>', {
			class: 'formFunction'
		});
		var levelOfDetailContainer = $('<div></div>', {
			class: 'row-fluid levelOfDetailContainer'
		});
		var levelOfDetailHeadingContainer = $('<div></div>', {
			class: 'levelOfDetailHeadingContainer span12'
		});
		var levelOfDetailHeading = $('<h5></h5>', {
			class : 'levelOfDetailHeading',
			text : 'LEVEL OF DETAIL'
		});
		var levelOfDetailSliderElement = $('<div></div>', {
			class: 'levelOfDetailSliderElement row-fluid span9'
		});
		var levelOfDetailSlider = $('<div></div>',{
			class : 'levelOfDetailSlider'
		});
		var levelOfDetailSubHeading = $('<div></div>',{
			class : 'levelOfDetailSubHeading span2'
		});
		var levelOfDetailSubHeadingText = $('<span></span>', {
			class : 'levelOfDetailSubHeadingText'
		});
		var levelOfDetailValue = $('<span></span>', {
			class : 'levelOfDetailValue',
			text : 100
		});
		var chooseSomeAttributeContainer = $('<div></div>', {
			class: 'row-fluid chooseSomeAttributeContainer',
			id: value.pluginId
		});
		chooseSomeAttributeContainer.data('pluginObject', value);
		
//		Reference to the plugin container element
		value.pluginContainerElement = chooseSomeAttributeContainer;
		value.headerElement = header;
		value.isPluginLoaded = false;
		var chooseSomeAttributeTitleAndSubtextContainer = $('<div></div>',{
			class : 'chooseSomeAttributeTitleAndSubtextContainer'
		});
		var chooseSomeAttributeTitle = $('<h5></h5>', {
			class : 'chooseSomeAttributeTitle',
			text : 'Choose Plugins'
		});
		var functionAtrributesDropdownContainer = $('<div></div>',{
			class : 'functionAtrributesDropdownContainer selector-wrapper input-large chooseLayer1'
		});
		var modelChooseALayer = $('<select></select>',{
			class: 'modelChooseALayer span12'
		});
		var option = $('<option></option>', {
			text : 'Choose a Layer',
			hidden: 'true',
			disabled:'',
			selected: ''
		});
		var exportButtonContainer = $('<div></div>', {
			class : 'exportButtonContainer'
		});
		var btngrp = $('<div></div>', {
			class : 'btn-group row-fluid'
		});
		var exportBtn = $('<button></button>', {
			class : 'exportButton span6',
			text : 'Export As '
		});
		exportBtn.prop('disabled',true);
		var caret = $('<i></i>',{
			class : 'fa fa-caret-down'
		});
		var btnsbmt = $('<button></button>',{
			class: 'runFunctionButton span6',
			type : 'submit',
			text: 'Run function'
		});
		btnsbmt.prop('disabled',true);
		value.pluginFunctionRunButton = btnsbmt;
		
		pluginObjects[index]=value;
		
		btnsbmt.click(function(event){
			event.preventDefault();
			setRunFunctionButtonClickHandler(btnsbmt, pluginObjects[index]);
			$(this).click();
		});
		
		exportBtn.append(caret);
		btngrp.append(exportBtn).append(btnsbmt);
		exportButtonContainer.append(btngrp);
		modelChooseALayer.append(option);
		functionAtrributesDropdownContainer.append(modelChooseALayer);
		chooseSomeAttributeTitleAndSubtextContainer.append(chooseSomeAttributeTitle).append(functionAtrributesDropdownContainer);
//		chooseSomeAttributeContainer.append(chooseSomeAttributeTitleAndSubtextContainer);
		levelOfDetailSubHeadingText.append(levelOfDetailValue);
		levelOfDetailSubHeading.append(levelOfDetailSubHeadingText);
		levelOfDetailSubHeadingText.html(levelOfDetailSubHeadingText.html() + '%');
		levelOfDetailSliderElement.append(levelOfDetailSlider);
		levelOfDetailHeadingContainer.append(levelOfDetailHeading);
//		levelOfDetailContainer
//		.append(levelOfDetailHeadingContainer)
//		.append(levelOfDetailSliderElement)
//		.append(levelOfDetailSubHeading);
		form.append(levelOfDetailContainer).append(chooseSomeAttributeContainer).append(exportButtonContainer);
		
		contentPanel.append(descriptionElement).append(form);
		$('.functionsAccordion').append(header).append(contentPanel);
		
//		$('.functionsAccordion.new').append(header).append(contentPanel);
//		$('.functionsAccordion.new').removeClass('new');
		
//		var $nextContainer = $('<div></div>', {
//			class : 'functionsAccordion new'
//		});
//		
//		$nextContainer.insertAfter('.functionsAccordion:last');
	});
}

function populateAccordionWithHTMLEelemmentsBeforeInitializing2(){
	$.each(pluginObjects, function(index, value){
		var header = $('<h4></h4>', {
			class: 'functionsAccordionTitle ' + value,
			text: value
		});
		var contentPanel = $('<div></div>', {
			class : 'functionsAccordionContentPanel'
		});
		var descriptionElement = $('<div></div>', {
			class : 'more functionDescription',
			text: value + ' This is a test function.'//got to find a way to pass the description to the element
		});
		
		var form = $('<form></form>', {
			class: 'formFunction'
		});
		var levelOfDetailContainer = $('<div></div>', {
			class: 'row-fluid levelOfDetailContainer'
		});
		var levelOfDetailHeadingContainer = $('<div></div>', {
			class: 'levelOfDetailHeadingContainer span12'
		});
		var levelOfDetailHeading = $('<h5></h5>', {
			class : 'levelOfDetailHeading',
			text : 'LEVEL OF DETAIL'
		});
		var levelOfDetailSliderElement = $('<div></div>', {
			class: 'levelOfDetailSliderElement row-fluid span9'
		});
		var levelOfDetailSlider = $('<div></div>',{
			class : 'levelOfDetailSlider'
		});
		var levelOfDetailSubHeading = $('<div></div>',{
			class : 'levelOfDetailSubHeading span2'
		});
		var levelOfDetailSubHeadingText = $('<span></span>', {
			class : 'levelOfDetailSubHeadingText'
		});
		var levelOfDetailValue = $('<span></span>', {
			class : 'levelOfDetailValue',
			text : 100
		});
		var chooseSomeAttributeContainer = $('<div></div>', {
			class: 'row-fluid chooseSomeAttributeContainer'
		});
		var chooseSomeAttributeTitleAndSubtextContainer = $('<div></div>',{
			class : 'chooseSomeAttributeTitleAndSubtextContainer'
		});
		var chooseSomeAttributeTitle = $('<h5></h5>', {
			class : 'chooseSomeAttributeTitle',
			text : 'Choose Layers'
		});
		var functionAtrributesDropdownContainer = $('<div></div>',{
			class : 'functionAtrributesDropdownContainer selector-wrapper input-large chooseLayer1'
		});
		var modelChooseALayer = $('<select></select>',{
			class: 'modelChooseALayer span12'
		});
		var option = $('<option></option>', {
			text : 'Choose a Layer',
			hidden: 'true',
			disabled:'',
			selected: ''
		});
		var exportButtonContainer = $('<div></div>', {
			class : 'exportButtonContainer'
		});
		var btngrp = $('<div></div>', {
			class : 'btn-group row-fluid'
		});
		var exportBtn = $('<button></button>', {
			class : 'exportButton span6',
			text : 'Export As '
		});
		exportBtn.prop('disabled',true);
		var caret = $('<i></i>',{
			class : 'fa fa-caret-down'
		});
		var btnsbmt = $('<button></button>',{
			class: 'runFunctionButton span6',
			type : 'submit',
			text: 'Run function'
		});
		btnsbmt.prop('disabled',true);
		exportBtn.append(caret);
		btngrp.append(exportBtn).append(btnsbmt);
		exportButtonContainer.append(btngrp);
		modelChooseALayer.append(option);
		functionAtrributesDropdownContainer.append(modelChooseALayer);
		chooseSomeAttributeTitleAndSubtextContainer.append(chooseSomeAttributeTitle).append(functionAtrributesDropdownContainer);
		chooseSomeAttributeContainer.append(chooseSomeAttributeTitleAndSubtextContainer);
		levelOfDetailSubHeadingText.append(levelOfDetailValue);
		levelOfDetailSubHeading.append(levelOfDetailSubHeadingText);
		levelOfDetailSubHeadingText.html(levelOfDetailSubHeadingText.html() + '%');
		levelOfDetailSliderElement.append(levelOfDetailSlider);
		levelOfDetailHeadingContainer.append(levelOfDetailHeading);
		levelOfDetailContainer
		.append(levelOfDetailHeadingContainer)
		.append(levelOfDetailSliderElement)
		.append(levelOfDetailSubHeading);
		form.append(levelOfDetailContainer).append(chooseSomeAttributeContainer).append(exportButtonContainer);
		
		contentPanel.append(descriptionElement).append(form);
		var $div = $('<div></div>',{
			id:'auxRespDiv'
		});
		$div.append(header).append(contentPanel);
		$('#functionsResponsiveSlider').append($div);
	});
}

function appendFunctionContainerToDom(pluginName) {
		var header = $('<h4></h4>', {
//			class: 'functionsAccordionTitle ' + value,
			class: 'functionsAccordionTitle',
			text: pluginName
		});
		var contentPanel = $('<div></div>', {
			class : 'functionsAccordionContentPanel newPlugin'
		});
		var descriptionElement = $('<div></div>', {
			class : 'more functionDescription',
			text: pluginName + ' This is a test function.'//got to find a way to pass the description to the element
		});
		
		var form = $('<form></form>', {
			class: 'formFunction'
		});
		var levelOfDetailContainer = $('<div></div>', {
			class: 'row-fluid levelOfDetailContainer'
		});
		var levelOfDetailHeadingContainer = $('<div></div>', {
			class: 'levelOfDetailHeadingContainer span12'
		});
		var levelOfDetailHeading = $('<h5></h5>', {
			class : 'levelOfDetailHeading',
			text : 'LEVEL OF DETAIL'
		});
		var levelOfDetailSliderElement = $('<div></div>', {
			class: 'levelOfDetailSliderElement row-fluid span9'
		});
		var levelOfDetailSlider = $('<div></div>',{
			class : 'levelOfDetailSlider'
		});
		var levelOfDetailSubHeading = $('<div></div>',{
			class : 'levelOfDetailSubHeading span2'
		});
		var levelOfDetailSubHeadingText = $('<span></span>', {
			class : 'levelOfDetailSubHeadingText'
		});
		var levelOfDetailValue = $('<span></span>', {
			class : 'levelOfDetailValue',
			text : 100
		});
		var chooseSomeAttributeContainer = $('<div></div>', {
			class: 'row-fluid chooseSomeAttributeContainer'
		});
		var chooseSomeAttributeTitleAndSubtextContainer = $('<div></div>',{
			class : 'chooseSomeAttributeTitleAndSubtextContainer'
		});
		var chooseSomeAttributeTitle = $('<h5></h5>', {
			class : 'chooseSomeAttributeTitle',
			text : 'Choose Plugins'
		});
		var functionAtrributesDropdownContainer = $('<div></div>',{
			class : 'functionAtrributesDropdownContainer selector-wrapper input-large chooseLayer1'
		});
		var modelChooseALayer = $('<select></select>',{
			class: 'modelChooseALayer span12'
		});
		var option = $('<option></option>', {
			text : 'Choose a Layer',
			hidden: 'true',
			disabled:'',
			selected: ''
		});
		var exportButtonContainer = $('<div></div>', {
			class : 'exportButtonContainer'
		});
		var btngrp = $('<div></div>', {
			class : 'btn-group row-fluid'
		});
		var exportBtn = $('<button></button>', {
			class : 'exportButton span6',
			text : 'Export As '
		});
		exportBtn.prop('disabled',true);
		var caret = $('<i></i>',{
			class : 'fa fa-caret-down'
		});
		var btnsbmt = $('<button></button>',{
			class: 'runFunctionButton span6',
			type : 'submit',
			text: 'Run function'
		});
		btnsbmt.prop('disabled',true);
		exportBtn.append(caret);
		btngrp.append(exportBtn).append(btnsbmt);
		exportButtonContainer.append(btngrp);
		modelChooseALayer.append(option);
		functionAtrributesDropdownContainer.append(modelChooseALayer);
		chooseSomeAttributeTitleAndSubtextContainer.append(chooseSomeAttributeTitle).append(functionAtrributesDropdownContainer);
		chooseSomeAttributeContainer.append(chooseSomeAttributeTitleAndSubtextContainer);
		levelOfDetailSubHeadingText.append(levelOfDetailValue);
		levelOfDetailSubHeading.append(levelOfDetailSubHeadingText);
		levelOfDetailSubHeadingText.html(levelOfDetailSubHeadingText.html() + '%');
		levelOfDetailSliderElement.append(levelOfDetailSlider);
		levelOfDetailHeadingContainer.append(levelOfDetailHeading);
		levelOfDetailContainer
		.append(levelOfDetailHeadingContainer)
		.append(levelOfDetailSliderElement)
		.append(levelOfDetailSubHeading);
		form.append(levelOfDetailContainer).append(chooseSomeAttributeContainer).append(exportButtonContainer);
		
		contentPanel.append(descriptionElement).append(form);
		$('.functionsAccordion').append(header).append(contentPanel);
		
}

function destroyNewPluginContainer() {
	$('.newPlugin').remove();
}

function removeNewPluginHelperClasses() {
	$('.newPlugin').removeClass('newPlugin');
}

function pluginsCallBackendDSSUtilityFunction(jsonData, callback, thePluginId, theProjectId) {
	var url = executeFunctionURL;
	
	var PluginExecutionMessenger = {};
	PluginExecutionMessenger.parameters = jsonData;
	PluginExecutionMessenger.userInfoObject = userinfoObject;
	PluginExecutionMessenger.pluginId = thePluginId;
	PluginExecutionMessenger.projectId = theProjectId;
	
	AJAX_Call_POST(url, callback, PluginExecutionMessenger);
}

function getPluginIdDSSUtility() {
	return pluginIdOfLatestLoadedPlugin;
}

function loadPlugin(pluginObject){
//	var $accordionHeader = $('#'+pluginObject.pluginId.toString()).closest('.functionsAccordionContentPanel').prev();
	
	var principalName = userinfoObject.fullname;
	var tenantName = userinfoObject.tenant;
	var pluginName = pluginObject.pluginName//$accordionHeader.text();
	var projectName = userinfoObject.projectName;
	var pluginId = pluginObject.pluginId;
	var projectId = $('#nameOfProject').data("projectID");
	
//	pluginIdOfLatestLoadedPlugin = pluginId;
	
	var parameters = "principalName=" + encodeURIComponent(principalName)
					+ "&tenantName=" + encodeURIComponent(tenantName)
					+ "&pluginName=" + encodeURIComponent(pluginName)
					+ "&projectId=" + encodeURIComponent(projectId)
					+ "&pluginId=" + encodeURIComponent(pluginId);
	
	var urlMVC = encodeURIComponent("plugin/loadPluginByNameAndTenant");
	var url = createLink(resourceURL, urlMVC, parameters);
	var context = null;
	
	var obj = pluginObject.pluginContainerElement.data('pluginObject');
	obj.isPluginLoaded = true;
	pluginObject.pluginContainerElement.data('pluginObject', obj);
	
//	appendFunctionContainerToDom(pluginName);
	showPluginLoadingUISpinner(pluginObject.headerElement);
	
	var loadedScript;
	
	$.getScript(url)
		.done(function( script, textStatus ) {
//			hidePluginSpinner();
//			if(typeof jQuery()[getWidgetName(pluginObject)] === 'undefined') {
//				eval(script);
//			}
//			createControl(pluginObject);
			
			loadedScript = script;
			
//			pluginObject.isPluginLoaded = true;
//			pluginObject.pluginFunctionRunButton.prop('disabled', false);
//			var layers = $('#layersPanel').data('layers');
//			var name = getWidgetName(pluginObject);
//			if(layers.length > 0)
//				pluginObject.pluginContainerElement[name]('setLayers', layers);
			
//			removeNewPluginHelperClasses();
		})
		.then(function(){
			hidePluginSpinner();
			if(typeof jQuery()[getWidgetName(pluginObject)] === 'undefined') {
				eval(loadedScript);
			}
			createControl(pluginObject);
		})
		.fail(function( jqxhr, settings, exception ) {
			$('.wizard').modal('hide');
			$('#InternalServerErrorModal').modal('show');
			destroyNewPluginContainer();
//			hideSpinner();
			hidePluginLoadingUISpinner(pluginObject.headerElement);
		});
}

function loadPluginOnAccordionHeaderClick() {
	$('#functionsPanel').off('click').on('click','.functionsAccordionTitle', function(event) {
		var $accordionHeader = $(this);
		var $accordionBody = $(this).next();
		var pluginId = $accordionBody.find('.chooseSomeAttributeContainer').attr('id');
		var pluginObject = $accordionBody.find('.chooseSomeAttributeContainer').data('pluginObject');
		var isLoaded = pluginObject.isPluginLoaded;
		
		if(!isLoaded) {
			loadPlugin(pluginObject);
			pluginObject.isPluginLoaded = true;
			$accordionBody.find('.chooseSomeAttributeContainer').data('pluginObject', pluginObject);
		}
	});
}

function getProjectLayers() {
	return $('#layersPanel').data('layers');
}

function getWidgetNamespace(pluginObject) {
	var namespaceParts = pluginObject.widgetName.split('.');
	var namespace = namespaceParts[0];
	
	return namespace;
}

function getWidgetName(pluginObject) {
	var namespaceParts = pluginObject.widgetName.split('.');
	var namespace = namespaceParts[0];
	var name = namespaceParts[1];
	
	return name;
}

function createControl(pluginObject) {
//	var name = getWidgetName(pluginObject);
	
//	pluginObject.pluginContainerElement[name]();
	
	var url = fetchPluginConfigurationClassURL;
	
	var $headerEl = pluginObject.headerElement;
	showPluginLoadingUISpinner($headerEl);
	
	var callback = function(data) {
		var name = getWidgetName(pluginObject);
		
		pluginObject.pluginContainerElement[name](data.response);
		
		pluginObject.isPluginLoaded = true;
		pluginObject.pluginFunctionRunButton.prop('disabled', false);
		var layers = $('#layersPanel').data('layers');
		var name = getWidgetName(pluginObject);
		if(layers.length > 0)
			pluginObject.pluginContainerElement[name]('setLayers', layers, data.response);
		
		hidePluginLoadingUISpinner($headerEl);
	}
	
	AJAX_Call_POST(url, callback, pluginObject.pluginId);
	
//	ajax call that will get the function model class
//	whatever the ajax call returns will be passed to the create above and will
//	change the widget code too
}

function setRunFunctionButtonClickHandler($runButton, pluginObject ) {
	$runButton.off('click').click(function(event){
		event.preventDefault();
		
		var name = getWidgetName(pluginObject);
		var mapExtent = map.getView().calculateExtent(map.getSize());
		var extentEPSG4326 = ol.proj.transformExtent(mapExtent,ol.proj.get('EPSG:3857'),ol.proj.get('EPSG:4326'));
		
		pluginObject.pluginContainerElement[name]('setBBOX', extentEPSG4326);
		var jsonData = pluginObject.pluginContainerElement[name]('getParameters');
		
		var $elem = pluginObject.pluginContainerElement.closest('.functionsAccordionContentPanel');
		var $headerElem = pluginObject.headerElement;
		
		showPluginProcessingSpinnerForPluginBody($headerElem, $elem);

		var callback = function(data) {
			var layersMap = data.response;
//			var count = 0;
			for(var name in layersMap) {
				$('#treeviewTaxonomiesLayers').jstree("create_node","#", {text: name, id: layersMap[name]}, false, false);
				$('#treeviewTaxonomiesLayers').jstree().select_node(layersMap[name]);
			}
			$('#treeviewTaxonomiesLayers .jstree-themeicon').remove();
			
			
			hidePluginProcessingSpinnerForPluginBody($headerElem, $elem);
			
			
			if(!$.isEmptyObject(layersMap)){
				pluginObject.pluginContainerElement[name]('setExecutionResult', 'Function executed successfully');				
			} else {
				pluginObject.pluginContainerElement[name]('setExecutionResult', 'Function execution failed');
			}
			
//			hidePluginProcessingSpinnerForPluginBody($headerElement, $element);
		}
		
		
		pluginsCallBackendDSSUtilityFunction(jsonData, callback, pluginObject.pluginId, userinfoObject.projectId);
	});
}

function uploadPlugin() {
	$('#pluginSubmit').off('click').on('click', function(e){
		hidePluginModalMessages();
		
		if(!validatePluginUploadForm()) {
			showValidationMessage();
			return;
		}
		
		var beforeSendCallback = showSpinner;
		var errorThrown  = errorThrownCallback;
		var onCompleteCallback = hideSpinner;
		var context = null;
		var url = pluginUploadURL;
		var callback = onSuccesfullPluginUpload;
		
		var pluginFormData = collectPluginFormData();
		
		AJAX_Call_POST_Form(url, callback, pluginFormData, context, beforeSendCallback, errorThrownCallback, onCompleteCallback);
	});
	
	$('#goToPluginLibraryJAR').off('click').on('click', function(e){
		$('#pluginLibraryJAR').click();
	});
	
	$('#pluginLibraryJAR').off().on('change', function(){
		var fileName = $('#pluginLibraryJAR').val().split('\\').pop();
		if(fileName === '')
			fileName ='Choose file';
		$('#pluginFileName').text(fileName);
		$('#pluginFileName').attr('title',fileName);
	});
	
	$('#addPluginModal').off('click').on('click', '.pluginTiles', function(e){
		$(this).toggleClass('clicked');
		$(this).parent().find( ".pluginFormSection" ).animate({
			height: "toggle"
		});
	});
	
	$('#addPlugin').off().on('click', function(e){
		var lastCopy = $('.pluginContainers:last').data('copy');
		var newCopy = lastCopy + 1;
		
		var $clone = $( '.pluginContainers[data-copy="0"]' ).clone();
		
		$clone.find('input, textarea').val('');
		$clone.removeAttr('data-copy');
		$clone.attr('data-copy',newCopy);
		$clone.data('copy',newCopy);
		
		$.each($clone.find('input, textarea'), function(index, value) {
			$(this).attr('id', $(this).attr('id')+newCopy);
		});
		
		$.each($clone.find('label'), function(index, value) {
			$(this).attr('for', $(this).attr('for')+newCopy);
		});
		
		$clone.find( ".pluginFormSection" ).animate({
			height: "toggle"
		});
		
		$clone.find( ".pluginTiles" ).removeClass( "clicked" ); 
		
		var $removePluginButton = $('<div></div>', {
			class: 'removePluginFromForm'
		});
		
		$removePluginButton.click(function(){
			$(this).closest('.pluginContainers').remove();
		});
		
		$clone.append($removePluginButton);
		
		$clone.appendTo('.pluginsSection');
	});
}

function collectPluginFormData() {
	var formData = new FormData();
	
	var pluginLibrary = {};
	pluginLibrary.pluginLibraryName = $('#pluginInLibraryName').val();
	var jarFile = $('#pluginLibraryJAR').prop('files')[0];
//	$('#pluginLibraryJAR')[0].files[0];
	
	formData.append('file', jarFile);
	
	pluginLibrary.pluginMessengers = [];
	
	var pluginContainers = $('.pluginContainers');
	
	$.each(pluginContainers, function(index, value){
		var pluginMessenger = {};
		
		var suffix = $(this).data('copy');
		if(suffix === 0) {
			suffix = '';
		}
		
		pluginMessenger.name = $.trim($('#pluginInName' + suffix).val());
		pluginMessenger.description = $('#pluginDescription' + suffix).val();
		pluginMessenger.widgetName = $('#widgetName' + suffix).val();
		pluginMessenger.className = $('#className' + suffix).val();
		pluginMessenger.methodName = $('#methodName' + suffix).val();
		pluginMessenger.jsFileName = $('#jsFileName' + suffix).val();
		pluginMessenger.configurationClass = $('#configurationClass' + suffix).val();
		pluginMessenger.type = 0;
		
		pluginLibrary.pluginMessengers.push(pluginMessenger);
	});
	
	formData.append('pluginLibrary', new Blob([JSON.stringify(pluginLibrary)], {type: "application/json"}));
	
	return formData;
}

function onSuccesfullPluginUpload(data) {
	if(data.status === "Success") {
//		if(!thereAreAlreadyLoadedPlugins()){
			var url = fetchPluginsOfLibraryURL;
			var pluginLibraryId = data.response;
			
			callback = function(theData) {
				var newPluginObjects = theData.response;
					
				$.each(newPluginObjects, function(index, value){
					pluginObjects.push(value);
				});
						
				if(newPluginObjects.length > 0){
						
					buildAccordion(RIGHTSIDE_ACCORDION, newPluginObjects);
						
//					$.each(newPluginObjects, function(i,v){
//						loadPlugin(newPluginObjects[0]);
//					});
					
//					loadPluginOnAccordionHeaderClick();
				}
			}
			
			
			AJAX_Call_POST(url, callback, pluginLibraryId);
//		}
		
		showSuccessMessage();
		
		
		
	} else if(data.status === "Failure") {
		showFailureMessage();
	}
}

function thereAreAlreadyLoadedPlugins(){
	return $('.functionsAccordion').children().length > 0;
}

function validatePluginUploadForm() {
	var result = true;
	
	hideValidationMessage();
	
	$.each($('#addPluginModal input, #addPluginModal textarea'), function(i, v){
		if($(v).val() === ''){
			if($(v).attr('id') === 'pluginLibraryJAR'){
				$('#pluginFileName').addClass('red');
			}
			
			$(v).focus();
			location.href='#'+$(v).attr('id');
			
			result = false;
			return false;
		}
	});

	return result;
}

function hidePluginModalMessages() {
	$('.addPluginMessages').addClass('hidden');
	$('#pluginFileName').removeClass('red');
}

function showValidationMessage() {
	$('.addPluginMessages.validation').removeClass('hidden');
}

function hideValidationMessage() {
	$('.addPluginMessages.validation').addClass('hidden');
	$('#pluginFileName').removeClass('red');
}

function showSuccessMessage() {
	$('.addPluginMessages.successfullUpload').removeClass('hidden');
}

function showFailureMessage() {
	$('.addPluginMessages.failedUpload').removeClass('hidden');
}

function errorThrownCallback() {
	$('#addPluginModal').modal('hide');
	$('#InternalServerErrorModal').modal('show');
}