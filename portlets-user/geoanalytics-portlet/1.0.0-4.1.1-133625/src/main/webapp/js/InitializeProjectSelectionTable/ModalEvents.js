function modalEvents(){
	allModalsEvents();
	bboxModalEvents();
	layersModalEvents();
	relateUsersToProjectModalEvents();
	nameAndDescriptionModalEvents();
}

function allModalsEvents(){
	$('.wizard .cancelBtns').off('click').on('click', function(){
		clearModals();
	});
	
	$('.wizard').on('hidden', function(){
		$('#createNewProject').removeClass('clicked');
	});
	
	$('#BBOXModal').on('shown', function(){
		coords = {};
		mapBBOX.updateSize();
		if(EDITMODE){
			var url = bboxURL;
			var callback = function(data){
				if(data.status === "Success"){
					extractCoordinates(data.response);
				} else {
					$('.wizard').modal('hide');
					$('#InternalServerErrorModal').modal('show');
				}
			};
			var data = userinfoObject;
			AJAX_Call_POST(bboxURL, callback, userinfoObject);
		}
	});
	
	$('#ChooseLayersModal').on('shown', function(){
		layersMap.updateSize();
		var theExtent;
		if(EDITMODE){
			if(editeModeCoordinatesLayersModal.length !== 0){
				layersMap.getView().fit(editeModeCoordinatesLayersModal, layersMap.getSize());
			}
			if(typeof coords.extent === "undefined"){
				theExtent = editeModeCoordinates;
				layersMap.getView().fit(theExtent, layersMap.getSize());
			}else{
				theExtent = coords.extent;
				layersMap.getView().fit(theExtent, layersMap.getSize());
			}
		}else{
			theExtent = coords.extent;
			layersMap.getView().fit(theExtent, layersMap.getSize());
		}
		
		theExtent = ol.proj.transformExtent(theExtent, ol.proj.get('EPSG:3857'),ol.proj.get('EPSG:4326'));
		
		layersMap.updateSize();
		if(LISTLAYERSFLAG) {
			var JSTREEToServerToken = {};
			JSTREEToServerToken.type = "LAYERTAXONOMY";
			JSTREEToServerToken.taxonomyID = null;
			JSTREEToServerToken.geographyExtent = theExtent;
			
			//fetch all layers
			$('#treeviewLayers')
			.on('select_node.jstree', function(e, data) {
				layersObject.jstreeLayers.push(data.node.text);
				layersMap.addLayer(fetchLayerByLayerNameModal(data.node.text));
			})
			.on('deselect_node.jstree', function(e, data) {
				var indexOfLayer = layersObject.jstreeLayers.indexOf(data.node.text);
				if(indexOfLayer > -1) {
					layersObject.jstreeLayers.splice(indexOfLayer, 1);
				}
				layersMap.removeLayer(layersByNameModal[data.node.text]);
				layersMap.updateSize();
			})
			.on('init.jstree Event',function(event, data){
				jstreeIsLoaded = true;
			})
			.jstree({
				plugins : [ 'checkbox', 'sort', 'wholerow' ],
				checkbox : {
					keep_selected_style : false
				},
				core : {
					themes : {
						'stripes' : true
					},
					data : {
						url : function(node) {
							if (node.id === '#') {
								return listOfAllLayers;
							}
						},
						type : 'post',
						dataType : "json",
						contentType : 'application/json',
						data : function(node) {
							var JSTREEToServerToken = {};
							JSTREEToServerToken.type = "LAYERTAXONOMY";
							JSTREEToServerToken.taxonomyID = null;
							JSTREEToServerToken.geographyExtent = theExtent;
							if (node.id !== '#') {
								JSTREEToServerToken.taxonomyID = node.id;
							}
							return JSON.stringify(JSTREEToServerToken);
						},
						contentType : 'application/json',
						success : function(serverResponse) {
							var layerNames = [];
							for (i = 0; i < serverResponse.length; i++) {
								layerNames.push(serverResponse[i].text);
							}
							
							if(EDITMODE && !EDITMODE_USER_PRESSED_CANCEL){
								var url = listLayersByProjectUrl;
								var callback = function(data){
									var projectLayers = [];
									$.each(data, function(index, value){
										projectLayers.push(value.text);
									});
									
									var mapNamesToIDs = {};
									$.each($('#treeviewLayers ul li'), function(index, value){
										var id = this.id;
										var name = $(this).find('a').text();
										mapNamesToIDs[name] = id;
									});
									
									$.each(projectLayers, function(index, value){
										$('#treeviewLayers').jstree().select_node(mapNamesToIDs[value]);
									});
									
								};
								
								AJAX_Call_POST(url, callback, userinfoObject);
							}
							
						},
						error : function(jqXHR, textStatus, errorThrown) {
							$('#errorModal').modal('show');
						},
						complete : function(data) {
							// $('#treeviewTaxonomiesLayers').jstree(true).refresh();
						}
					}
				}
			});
			
			LISTLAYERSFLAG = false;
		}
	});
}

/**************** bboxModal Events ****************/
function bboxModalEvents(){
	
	initializeMapForWizardModal();
	
	$('#clearMap').on('click', function(){
		moveEndForMapBBOX();
		editeModeCoordinatesLayersModal = [];
	});
	
	$('#goToChooseLayersModalFromBBOXModal').off().on('click', function(){
		var condition = $('.coordLabel').hasClass('hidden');
		
		if(!condition) {
			coordsObjectToBeSendToDSS = coords;
			$('#chooseAreaMessage').addClass('hidden');
		} else if(EDITMODE){
			if(coords !== undefined && coords !== null){
				coordsObjectToBeSendToDSS = {};
			}else{
				coordsObjectToBeSendToDSS = coords;
			}
			$('#chooseAreaMessage').addClass('hidden');
		}else {
			coordsObjectToBeSendToDSS = {};
			$('#chooseAreaMessage').removeClass('hidden');
			return;
		}
		
		$('#BBOXModal').modal('hide');
		
		$('#ChooseLayersModal').modal('show');
	});
}

function initializeMapForWizardModal() {
	source = new ol.source.Vector({wrapX: false});
	
	var maxPoints = 2;
	
	var baseLayer = new ol.layer.Tile({
    	source : new ol.source.OSM()
    });
	
	var vector = new ol.layer.Vector({
        source: source,
        style: new ol.style.Style({
          fill: new ol.style.Fill({
            color: 'rgba(255, 255, 255, 0.2)'
          }),
          stroke: new ol.style.Stroke({
            color: '#ffcc33',
            width: 2
          }),
          image: new ol.style.Circle({
            radius: 7,
            fill: new ol.style.Fill({
              color: '#ffcc33'
            })
          })
        })
      });
	
	mapBBOX = new ol.Map({
    	target: 'mapBBOX',
        controls: ol.control.defaults({
            zoom: true,
            attribution: false,
            rotate: false
          }),
        layers: [
                 baseLayer, vector
        ],
        view: new ol.View({
        	center: ol.proj.fromLonLat([22.00, 37.00]),
        	zoom: 4
        })
    });
	mapBBOX.set('mapId','mapBBOX');
	
	var geometryFunction = function(coordinates, geometry) {
		moveEndForMapBBOX();
		$('.coordLabel').removeClass('hidden');
        if (!geometry) {
        	geometry = new ol.geom.Polygon(null);
        }
        
        var start = coordinates[0];
        var end = coordinates[1];
        geometry.setCoordinates([
                                 [start, [start[0], end[1]], end, [end[0], start[1]], start]
                                 ]);
        var extent = geometry.getExtent();
        
//        $('#coord0').text(extent[0].toString() + '  , ' + extent[1].toString());
//        $('#coord1').text(extent[2].toString() + '  , ' + extent[1].toString());
//        $('#coord2').text(extent[0].toString() + '  , ' + extent[3].toString());
//        $('#coord3').text(extent[2].toString() + '  , ' + extent[3].toString());
        
        var divCoords;
        coords.coord0 = [extent[0], extent[1]];
        $('#coord0').text(transformTo4326(coords.coord0));
        
        coords.coord1 = [extent[2], extent[1]];
        $('#coord1').text(transformTo4326(coords.coord1));
        
        coords.coord2 = [extent[0], extent[3]];
        $('#coord2').text(transformTo4326(coords.coord2));
        
        coords.coord3 = [extent[2], extent[3]];
        $('#coord3').text(transformTo4326(coords.coord3));
        
        coords.extent = extent;
        
        if(EDITMODE){
        	editeModeCoordinatesLayersModal = extent;
        }
        
        $('#chooseAreaMessage').addClass('hidden');
        
        return geometry;
	};
	
	draw = new ol.interaction.Draw({
		source: source,
		type: /** @type {ol.geom.GeometryType} */ ('LineString'),//LineString-->Box
		geometryFunction: geometryFunction,
        maxPoints: maxPoints
	});
	
	mapBBOX.addInteraction(draw);
	mapBBOX.on('moveend', moveEndForMapBBOX);
	addControls('navcross', mapBBOX);
	addControls('zoomSlider', mapBBOX);
	addControls('setInitialExten', mapBBOX);
	
	initializemapForLayersModal();
}

function transformTo4326(coordinates){
	return ol.proj.transform(coordinates, ol.proj.get('EPSG:3857'),ol.proj.get('EPSG:4326'));
}

function moveEndForMapBBOX(){
	source.clear();
	$('.coordLabel').text('');
	$('.coordLabel').addClass('hidden');
}

/*********** layers Modal ***********/
function layersModalEvents(){
	$('#goToRelateUsersToProjectModal').off().on('click', function() {
		
		$('#RelateUsersToProjectModal').modal('show');
		$('#ChooseLayersModal').modal('hide');
		
		if(! $.fn.DataTable.isDataTable( '#relateUsersToProjectsTable' )){
			initializeAssignUsersToProjectTable();	
			relateUsersToProjectsTableInitialized = false;
		}
		if(EDITMODE) {
			var url = participantsURL;
			var callback = function(data){
				$.each(data.response, function(index, value){
					$(".usersTagsinput").tagsinput('add', value);
				});
			};
			var data = userinfoObject;
			
			AJAX_Call_POST(participantsURL, callback, data);
		}
	});
	
	$('#goToRelateUsersToProjectModalSkipButton').off().on('click', function() {
		$('#RelateUsersToProjectModal').modal('show');
		$('#ChooseLayersModal').modal('hide');
	});
	
	$('#goToBBOXModal').off().on('click', function(){
		$('#ChooseLayersModal').modal('hide');
		$('#BBOXModal').modal('show');
	});
	
	$('#goToRelateUsersToProjectModalSkipButton').off('click').on('click', function(){
		layersObject.skipped = true;
		if(! $.fn.DataTable.isDataTable( '#relateUsersToProjectsTable' )){
			initializeAssignUsersToProjectTable();	
			relateUsersToProjectsTableInitialized = false;
		}
		$('#ChooseLayersModal').modal('hide');
		$('#RelateUsersToProjectModal').modal('show');
	});
}

/*********** relateUsersToProjectModal ***********/
function relateUsersToProjectModalEvents(){
	$('#goToChooseLayersModalFromRelateUsersToProjectModal').off().on('click', function() {
		$('#RelateUsersToProjectModal').modal('hide');
		$('#ChooseLayersModal').modal('show');
	});
	
	$('#goToProjectNameAndDescriptionModal').off().on('click', function(){
		usersArray = [];
		usersArray = $(".usersTagsinput").tagsinput('items');
		
		$('#RelateUsersToProjectModal').modal('hide');
		$('#projectNameAndDescriptionModal').modal('show');
		
		if(EDITMODE){
			$("#projectName").val(projectNameAndDescriptionObject.name);
			$("#projectDescription").val(projectNameAndDescriptionObject.description );
		}
	});
	
	$('.usersTagsinput')
	.on('itemAdded', function(e){
		$('.selectdUsersTagSection').removeClass('hidden');
		var numOfTags = $('.usersTagsinput').tagsinput('items').length;
		$('#numOfSelectedUsers').text(numOfTags);
		
		var $lastItemAdded = $('.selectdUsersTagSection .bootstrap-tagsinput').find('.tag.label.label-info').last();
	}).on('itemRemoved', function(e){
		var numOfTags = $('.usersTagsinput').tagsinput('items').length;
		if(numOfTags === 0){
			$('.selectdUsersTagSection').addClass('hidden');
			$('#numOfSelectedUsers').text('');
		}else {
			$('#numOfSelectedUsers').text(numOfTags);
		}
		
		$relateUsersToProjectsTable
	    .column( 0 )
	    .data()
	    .filter( function ( value, index ) {
	    	if(value === e.item){
	    		var data = $relateUsersToProjectsTable.rows(index).data();
	    		data[0][4] = createAddUserToProjecButton();
	    		
	    		$relateUsersToProjectsTable.row(index).data(data[0]).draw();
	    	}
	    });
		
	});
}

function clearModals(){
//	BBOXModal
	moveEndForMapBBOX();
	$('#chooseAreaMessage').addClass('hidden');
	coordsObjectToBeSendToDSS = {};
	
//	LayersModal
	if(jstreeIsLoaded){
//		$('#treeviewLayers').jstree().deselect_all(true);
		$('#treeviewLayers').jstree().destroy();
		LISTLAYERSFLAG = true;
		jstreeIsLoaded = false;
		layersObject.jstreeLayers = [];
		layersObject.skipped = false;
	}
	removeLayersFromMapModal();
	
//	RelateUsersModal
	if($.fn.DataTable.isDataTable( '#relateUsersToProjectsTable' )){
		$relateUsersToProjectsTable.clear();
		$relateUsersToProjectsTable.destroy();
	}
	usersArray = [];
	$('.usersTagsinput').tagsinput('removeAll');
	$('.selectdUsersTagSection').addClass('hidden');
	
//	ProjectNameAndDescriptionModal
	$("#projectName").val('');
	$("#projectDescription").val('');
	projectNameAndDescriptionObject = {};
	$('#projectNameDescriptionValidation').addClass('hidden');
	$('#numOfSelectedUsers').text('');
	
	if(EDITMODE){
		EDITMODE_USER_PRESSED_CANCEL = true;
		
		if(layerNamesObject.length !== 0){
			for(var i in layersByName){
				layersMap.removeLayer(layersByName[i]);
			}
			layerNamesObject = [];
			layersByName = {};
		}
		

		EDITMODE_USER_PRESSED_CANCEL = false;
	}
}

function retrieveUsersAndGroups(){
	var url = theResourceURL;
	var callback = function(data){
		var dataObject = {};
		JSON.parse(data);
		
		if(data !== null || data !== ''){
			dataObject = JSON.parse(data);
			
			var usersArray = dataObject.users;
			
			$.each(usersArray, function(index, value) {
				for(var userData in value){
//					console.log(value[userData]);
				}
			});
		}
	};
	
	var theData = {};
	theData[nameSpace + 'usersAndGroups'] = true;
	
	$.ajax(
			{
				url: url,
				type: 'post',
				datatype:'json',
				data: theData,
				success: function(data){
					callback(data);
				},
				error: function (xhr, ajaxOptions, thrownError) {
					alert();
				}
			}
		);
}

function nameAndDescriptionModalEvents(){
	$('#goBackToRelateUsersToProjectModal').off().on('click', function(){
		$('#projectNameAndDescriptionModal').modal('hide')
		$('#RelateUsersToProjectModal').modal('show');
	});
	
	$('#CreateProjectButton').off().on('click', function(){
		var projectName = $.trim($("#projectName").val());
		var projectDescription = $.trim($("#projectDescription").val());
		projectNameAndDescriptionObject = {};
		projectNameAndDescriptionObject.name = projectName;
		projectNameAndDescriptionObject.description = projectDescription;
		
		if(projectName === ''){
			$('#projectNameDescriptionValidation').removeClass('hidden');
			return;
		}
//		sendDataToServer set globalObject
		
		var NewProjectData = setProjectData();
		var url;
		if(EDITMODE){
			url= projectUpdateURL;
		}else{
			url =projectCreateURL;
		}
		
		showSpinner();
		
		$.ajax({ 
		url: url,
        type: 'post',
        dataType : 'json',
        contentType: 'application/json',
        data: JSON.stringify(NewProjectData),
        success: function(serverResponse){
        	hideSpinner();
        	if(serverResponse.status === "Success"){
        		var date;
        		var extent;
    			
        		if(EDITMODE){
        			if(typeof coords.extent === "undefined"){
        				extent = editeModeCoordinates;
        			}else{
        				extent = coords.extent;//mapBBOX.getView().calculateExtent(mapBBOX.getSize());
        			}
//             		extent = extent.toString().replace(/\./g,"d");
//             		extent = extent.toString().replace(new RegExp(",",'g'),"c");
        			
        			date = projectDateToBeEdited;
        			
        			loadProjectObject.projectName = projectName;
        			loadProjectObject.date = date;
        			loadProjectObject.extent = extent;
        			loadProject(loadProjectObject);
        			
        			$('#projectNameAndDescriptionModal').modal('hide');
        			userinfoObject.projectName = projectName;
        			$table.DataTable().ajax.reload();
        			extentForCenteringDSSMap = extent;
//            		projectName = encodeURIComponent(projectName);
//            		window.location.href = createLink(renderURL, "dss", "&projectName=" + projectName + "~~" + "&projectDate="+date + "~~" + "&projectExtent="+extent + "~~");
        		}else{
//        			extent = coords.extent;
//             		extent = extent.toString().replace(/\./g,"d");
//             		extent = extent.toString().replace(new RegExp(",",'g'),"c");
//            		projectName = encodeURIComponent(projectName);
        			extent = coords.extent;//mapBBOX.getView().calculateExtent(mapBBOX.getSize());
            		date = new Date().getTime();
        			
        			loadProjectObject.projectName = projectName;
        			loadProjectObject.date = date;
        			loadProjectObject.extent = extent;
        			loadProject(loadProjectObject);

        			extentForCenteringDSSMap = extent;
        			$('#projectNameAndDescriptionModal').modal('hide');
        			$table.DataTable().ajax.reload();
//        			window.location.href = createLink(renderURL, "dss", "&projectName=" + projectName + "~~" + "&projectDate="+date + "~~" + "&projectExtent="+extent + "~~");
        		}
//        		$('#treeviewTaxonomiesLayers').html('');
        		if(!mapLayersLoaded){
        			userinfoObject.projectName = projectName;
        			retrieveAvailableLayersAndPlaceThemOnTheLeft(userinfoObject);
        		}else{
        			removeLayersFromMap();
        			$('#treeviewTaxonomiesLayers').jstree().deselect_all(true);
        			$('#treeviewTaxonomiesLayers').jstree().refresh();
        		}
//        		retrieveAvailableLayersAndPlaceThemOnTheLeft(userinfoObject);
        	}

        	if(serverResponse.status === "Existing"){
        		$('#projectAlreadyExists').text(serverResponse.message).removeClass('hidden');
        		$('#projectNameDescriptionValidation').addClass('hidden');
        	}
        	
        },error: function(jqXHR, textStatus, errorThrown) {
        	hideSpinner();
        	$('#InternalServerErrorModal').modal('show');
        }
      });
		
	});
}

function initializemapForLayersModal(){
	var baseLayer2 = new ol.layer.Tile({
    	source : new ol.source.OSM()
    });
	
	layersMap = new ol.Map({
    	target: 'modalLayerMap',
        controls: ol.control.defaults({
            zoom: true,
            attribution: false,
            rotate: false
          }),
        layers: [ baseLayer2 ],
        view: new ol.View({
        	center: ol.proj.fromLonLat([22.00, 37.00]),
        	zoom: 4,
        })
    });
	layersMap.set('mapId','modalLayerMap');
	
	addControls('navcross', layersMap);
	addControls('zoomSlider', layersMap);
}

function setProjectData(){
	var projectData = {};
	
	projectData.coords = coordsObjectToBeSendToDSS;
	if(layersObject.skipped === false){
		projectData.layers = {};
	}
	projectData.layers = layersObject;
	projectData.users = usersArray;
	projectData.nameAndDescriptionObject = projectNameAndDescriptionObject;
	projectData.userinfoObject = userinfoObject;
	if(EDITMODE){
		projectData.nameAndDescriptionObject.oldName =projectNameToBeEdited;
	}else{
		projectData.nameAndDescriptionObject.oldName = '';
	}
	return projectData;
}