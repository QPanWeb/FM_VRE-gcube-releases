$.widget("cite.WFSImport", {
	options : {
		headerDiv: {},
		content: {}
	},
	_create : function() { 	/* happens only once*/
	
		this.wfsImportLayoutHead ='<div class="span4" id="urlInput">															\
									<input type="text" id="url" class="span12" placeholder="http://..."> 				\
									<label id="urlInputError">Invalid url</label>	\
					   			  </div>																					\
								<div class="span6" id="moreOptions"> 																		\
									<div class="spinner" style="display: none"></div>												\
									<div class="row"> 																				\
										<div class="span6" id="selectVersionWfs"> 													\
											<span id="selectVersion">Select Version<span class="makeMeOrange">*</span></span> 		\
											<div class="btn-group"> 																\
									  			<a class="btn dropdown-toggle" data-toggle="dropdown" href="#"> 					\
									   				<span class="caret"></span> 													\
									  			</a> 																				\
												<ul class="dropdown-menu" id="listVersion"> 										\
												</ul> 																				\
											</div> 																					\
										</div> 																						\
																																	\
										<div class="span6" id="fetchLayers"> 														\
											<button disabled class="btn portlet-button" id="fetchLayersWfs">Fetch</button>																	\
										</div>																						\
										<div id="wfsimporter-notificator"></div>														\
									</div></div><br><br>';
		this.wfsImportLayoutContent = '<div class="row layers">																			\
										<span class="headerDescription">LAYERS</span>																\
										<hr>																								\
										<table id="layersTable" class="hover order-column no-footer dataTable" cellspacing="0" role="grid" aria-describedby="layersTable_info">	\
											<thead>																																\
												<tr role="row">																													\
													<th class="tickMeHead" tabindex="0" aria-controls="layersTable" rowspan="1" colspan="1" aria-label="TickMe">	\
			                							<span class="tickMe">																								\
													    </span>																													\
													</th>																														\
													<th  class="title borderOfTableHeads" tabindex="0" aria-controls="layersTable" rowspan="1" colspan="1" aria-label="Title">Title	\
								                	</th>																														\
								                	<th  class="name" tabindex="0" aria-controls="layersTable" rowspan="1" colspan="1" aria-label="Name">Name	\
								                	</th>																																\
								                	<th  class="abstract" tabindex="0" aria-controls="layersTable" rowspan="1" colspan="1" aria-label="Abstract">Abstract	\
								                	</th>																																\
								                	<th  class="keywords" tabindex="0" aria-controls="layersTable" rowspan="1" colspan="1" aria-label="Keywords" >Keywords				\
								                	</th>																																\
								                	<th  class="srs" tabindex="0" aria-controls="layersTable" rowspan="1" colspan="1" aria-label="SRS">SRS	\
								                	</th>																																	\
								               </tr>																																		\
								        	</thead>																																			\
											<tbody>																																				\
											</tbody>																																			\
										</table>																																				\
									</div>																																						\
									<div class="actionButtons row">																																\
										<button disabled class="btn saveImport portlet-button" id="wfs-nextImport">Next</button>																					\
									</div>';
		
		this.wfsImportLayerInfo = ' <div class="modal fade" id="layerInfoModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">	\
									    <div class="modal-dialog">																									\
									      <div class="modal-content">																								\
									        <div class="modal-header">																								\
									          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>							\
									          <h4 class="modal-title">Set Layer Information</h4>																			\
									        </div>																													\
									        <div class="modal-body" id="layerInfoModalBody">																		\
																																			\
									        </div>																													\
											<div class="modal-footer">																								\
										       <button type="button" id="cancelWfsLayers" class="btn portlet-button" data-dismiss="modal">Close</button>									\
			 								   <button type="button" id="importWfsLayers" class="btn portlet-button">Import</button>									\
										    </div>																													\
									      </div><!-- /.modal-content -->																							\
									    </div><!-- /.modal-dialog -->																								\
									</div><!-- /.Add Taxonomy modal -->';
			
		this.selfId = this.element.attr('id');
	},
	createAsModal: function(pageState) {   /* modal version has not ever used*/
		this.element.append('<button class="wfsImporterButton"></button>');
		
		$('.wfsImporterButton').attr('data-target', "#"+this.selfId+"Modal")
		   .attr('data-toggle', 'modal')
		   .html("WFS importer");
		
		var modalLayout = '<div id="'+this.selfId+"Modal"+'" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="wfsImportModalLabel" aria-hidden="true">' +
								'<div>' +
									'<div class="header-top row">' +
										'<div class="headerOfModal">' +
											'<h1 class="titleOfModal">Wfs Importer</h1>' +
										'</div>' +
									'</div>' +
									'<div class="bodyOfModal">' +
									'</div>' +
								'</div>' +
						  '</div>';
		$('.adminContainer').append(modalLayout);
		$('.adminContainer #' + this.selfId+"Modal .bodyOfModal").append(this.wfsImportLayoutHead);
		$('.adminContainer #' + this.selfId+"Modal .bodyOfModal").append(this.wfsImportLayoutContent);
		$('.adminContainer #' + this.selfId+"Modal .bodyOfModal").append(this.wfsImportLayerInfo);
		

	},
	createAsDiv: function(pageState) {
//		$(this.options.headerDiv).append(this.wfsImportLayoutHead);
		$(this.options.content).append(this.wfsImportLayoutHead);
		$(this.options.content).append(this.wfsImportLayoutContent);
		$(this.options.content).append(this.wfsImportLayerInfo);
		
		this._initializeContent(pageState);
		
	},
	_initializeContent: function(pageState) {
		var self = this;
		var notificator = $('#wfsimporter-notificator');
		pageState.countTicks = 0;
																			/** dropdown for version **/
		$('#selectVersionWfs .btn-group ul#listVersion').append('<li id="firstV">1.0.0</li>');

		$('#selectVersionWfs .btn-group ul#listVersion li').click(function(event){
			$('#selectVersionWfs a').html(event.target.textContent+'<span class="caret"></span>');
			pageState.version = event.target.textContent;
		});
		$('#selectVersionWfs .btn-group ul#listVersion li#firstV').trigger('click');
		
																			/** dropdown for format type**/
		$('#selectFormatTypeWfs .btn-group ul#listFormatTypes').append('<li id="zipFile">zip-file</li>');
		
		$('#selectFormatTypeWfs .btn-group ul#listFormatTypes li').click(function(event){
			$('#selectFormatTypeWfs a').html(event.target.textContent+'<span class="caret"></span>');
		});
		$('#selectFormatTypeWfs .btn-group ul#listFormatTypes li#zipFile').trigger('click');
		
		$('.abstractTooltip').tooltip();
		$('.keywordsTooltip').tooltip();
		
		$('.dataTables_paginate li').on('click', function() { 
			$('.abstractTooltip').tooltip();
			$('.keywordsTooltip').tooltip();
		});
		
		pageState.url = "";
		
		function isValidUrl(url){

			var myVariable = url;
		    if(/^(http|https|ftp):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/i.test(myVariable)) {
		    	return 1;
		    } else {
		    	return -1;
		    }   
		}
		
		// validation of url while texting
		$('#url').on('input', function(e) {
			if ($(this).val().replace(/\s/g, '').length){
				pageState.url = $(this).val();
		        
		        if (isValidUrl(pageState.url)==1){
		        	$('#fetchLayers button').prop('disabled', false); 
		        	$('#url').css({"border":"1px solid #DDD"});
					$('#urlInputError').css({"display":"none"});
		        }else{
		        	pageState.url = "";
		        	$('#fetchLayers button').prop('disabled', true); 
		        	$('#url').css({"border":"1px solid #b94a48"});
					$('#urlInputError').css({"display":"block"});
		        }
				
			}else {
				pageState.url = "";
				$('#fetchLayers button').prop('disabled', false); 
				$('#url').css({"border":"1px solid #b94a48"});
				$('#urlInputError').css({"display":"block"});
			}
		});
		
		$('#fetchLayers button').on('click', function(e){
			if(!$(this).is(':disabled')){
				if (pageState.url === ""){
					return;
				}else{
					self._populateLayers(pageState);
				}
			}
			
		});
	},
	_populateLayers: function(pageState) {
		
		var notificator = $('#wfsimporter-notificator');
		pageState.featureTypesToSave = [];
		var wfsRequestMessenger = {
			url: pageState.url,
			version: pageState.version,
			featureTypes: pageState.featureTypesToSave
		};
			
		postDataToServer(wfsRequestMessenger,  window.config.createResourceURL('import/getCapabilities'), function(response) {
			if(response.status) {
			
				$('#layersTable tbody').empty();
				
				for (var row=0; row<response.data.length; row++) {
					var tr;
					if(response.data[row].abstractText)
						response.data[row].abstractText = " ";
					tr =  '<tr id='+row+' class="basicT">' +
								'<td class="tickMeTd"></td>' +
								'<td class="title" data-toggle="tooltip" title="'+response.data[row].title+'">'+ response.data[row].title +'</td>' +
								'<td class="name" data-toggle="tooltip" title="'+response.data[row].name+'">'+ response.data[row].name +'</td>' +
								'<td class="abstract abstractTooltip" id="abstractOfLayer" data-container="body" data-placement="top">'+ response.data[row].abstractText +'</td>' +
								'<td class="keywords keywordsTooltip" id="keywordsOfLayer" data-container="body" data-placement="top" data-toggle="tooltip" title="'+response.data[row].keywords+'">'+ response.data[row].keywords +'</td>' +
								'<td class="srs">'+ response.data[row].srs +'</td>' +
							'</tr><hr>';
					
					$('#layersTable tbody').append(tr);
		
				}
				
				var countTicks = 0;
				$('#layersTable tr td').on('click', function(){ 
					
					pageState.preventModal = false;
					
					if ($('tr#'+ $(this).parent().attr('id') +' .tickMeTd').children().length==0) {
						$('tr#'+ $(this).parent().attr('id') +' .tickMeTd').append('<button type="button" class="tickMe" id="tickMe'+ $(this).parent().attr('id') +'"></button>');
					
						pageState.featureTypesToSave.push($('tr#'+ $(this).parent().attr('id') +' .name').html());
						countTicks++;
					
					} else {
						$('tr#'+ $(this).parent().attr('id') +' .tickMeTd button').remove();
						
						for(i=0; i < pageState.featureTypesToSave.length; i++){
					        if(pageState.featureTypesToSave[i].match($('tr#'+ $(this).parent().attr('id') +' .name').html())){
					        	pageState.featureTypesToSave.splice( pageState.featureTypesToSave.indexOf($('tr#'+ $(this).parent().attr('id') +' .name').html()), 1 );
					        	break;
					        }
					    } 
						
						countTicks--;
					}
				
					//make import button to be active or not
					if(countTicks == 1){
						$('.saveImport').prop('disabled', false); 
					}else if(countTicks == 0){
						$('.saveImport').prop('disabled', true); 
					}
					
				});	
//				$('#layersTable').dataTable().fnDestroy();
				pageState.endPointsTable = $('#layersTable').DataTable({
					"aaSorting": [],
					"bDestroy": true,
					destroy: true,
					"language": {
						"info": "Items _START_ to _END_ of _TOTAL_ entries",
						"loadingRecords": "Loading...",
						 "emptyTable": "No data available in table"
					},
					"columnDefs": [
						{
							className: "borderOfTableHeads",
							"targets": [0]
						},
						{
							"orderable": false,
							"targets": [0, 5]
						}
					]
				});
				
				$('.abstractTooltip').tooltip();
				$('.keywordsTooltip').tooltip();
				
				$('.dataTables_paginate li').on('click', function() { 
					$('.abstractTooltip').tooltip();
					$('.keywordsTooltip').tooltip();
				});
				
				
//				$('#layersTable tr td').on('click', function(){ 
//					
//					pageState.preventModal = false;
//					
//					if ($('tr#'+ $(this).parent().attr('id') +' .tickMeTd').children().length==0) {
//						$('tr#'+ $(this).parent().attr('id') +' .tickMeTd').append('<button type="button" class="tickMe" id="tickMe'+ $(this).parent().attr('id') +'"></button>');
//					
//						pageState.featureTypesToSave.push($('tr#'+ $(this).parent().attr('id') +' .name').html());
//						countTicks++;
//					
//					} else {
//						$('tr#'+ $(this).parent().attr('id') +' .tickMeTd button').remove();
//						
//						for(i=0; i < pageState.featureTypesToSave.length; i++){
//					        if(pageState.featureTypesToSave[i].match($('tr#'+ $(this).parent().attr('id') +' .name').html())){
//					        	pageState.featureTypesToSave.splice( pageState.featureTypesToSave.indexOf($('tr#'+ $(this).parent().attr('id') +' .name').html()), 1 );
//					        	break;
//					        }
//					    } 
//						
//						countTicks--;
//					}
//				
//					//make import button to be active or not
//					if(countTicks == 1){
//						$('.saveImport').removeClass('idleMe');
//					}else if(countTicks == 0){
//						$('.saveImport').addClass('idleMe');
//					}
//					
//				});	
				
			} else {
				notificator.noty({
				    text: 'Malformed url',
				    type: 'error',
				    template: '<div class="noty_message"><span class="noty_text"></span><div class="noty_close"></div></div>',
				    theme: 'relax',
				    closeWith: ['button'],
				    timeout: 3000,
				    maxVisible:1,
				    animation: {
				        open: 'animated flipInX', 
				        close: 'animated flipOutX',
				        easing: 'swing',
				        speed: 400
				    }
				});	
				return;
			}
			
			var taxonomyTransfer = {
					active: true
			};

		});
			
		$('.actionButtons .saveImport').on('click', function(e) {
			//if there is at least one layer to import
			if(!$(this).is(':disabled')){
			
				$('#layerInfoModalBody').empty();
				for(i=0; i < pageState.featureTypesToSave.length; i++){
			        $('#layerInfoModalBody').append('<h4>'+pageState.featureTypesToSave[i]+'</h4>');
			        $('#layerInfoModalBody').append('<label for="modalLayerName'+i+'">Layer Name <span class="makeMeOrange">*</span></label>');
			        $('#layerInfoModalBody').append('<input type="text" class="modalLayerName" id="modalLayerName'+i+'" placeholder="Please fill in your Layer Name"></input>');
			        $('#layerInfoModalBody').append('<label class="modalLayerNameError" id="modalLayerNameError'+i+'">This field is required</label>');
			        $('#layerInfoModalBody').append('<label for="modalLayerDescription'+i+'">Layer Description</label>');
			        $('#layerInfoModalBody').append('<textarea type="text" class="modalLayerDescription" id="modalLayerDescription'+i+'" placeholder="Please fill in your Layer Description"></textarea>');
			        $('#layerInfoModalBody').append('<label for="modalLayerStyle'+i+'">Layer Style <span class="makeMeOrange">*</span></label>');
			        $('#layerInfoModalBody').append('<select class="modalLayerStyle" id="modalLayerStyle'+i+'"><option  value="" disabled selected>Choose a Style</option></select>');
			        $('#layerInfoModalBody').append('<label class="modalLayerStyleError" id="modalLayerStyleError'+i+'">This field is required</label>');
			        $('#layerInfoModalBody').append('<hr>');
			        
			    } 
				
				$.ajax({
					url: window.config.createResourceURL('styles/getAllStyles'),
					type: 'GET',
					cache : false,
					dataType: 'json',
					success: function(response) {
						$.each(response, function(i,v){
							var $option = $('<option></option>', {
								text : v,
								value : i
							});
							$('.modalLayerStyle').append($option);
						});

					},
					error : function(jqXHR, textStatus, errorThrown) {
						window.noty.errorHandlingNoty(notificator, jqXHR, exception);
					}
				});	
				
				$('#layerInfoModal').modal();
				
			}
		});
		
		//while typing layerName, remove error message for empty field
		$('body').on('keyup', '.modalLayerName',function(e) {

			for(i=0; i < pageState.featureTypesToSave.length; i++){
				if($('#modalLayerName'+i+'').val()!=null && $('#modalLayerName'+i+'').val().replace(/\s/g, '').length > 0) {
					$('#modalLayerName'+i+'').css({"border":"1px solid #DDD"});
					$('#modalLayerNameError'+i+'').css({"display":"none"});
				}
				
			}

		});
		
		$('body').on('change', '.modalLayerStyle' ,function(e) {
			
			for(i=0; i < pageState.featureTypesToSave.length; i++){
				if($('#modalLayerStyle'+i+'').val()!=null && $('#modalLayerStyle'+i+'').val().replace(/\s/g, '').length > 0) {
					$('#modalLayerStyle'+i+'').css({"border":"1px solid #DDD"});
					$('#modalLayerStyleError'+i+'').css({"display":"none"});
				}
				
			}
		});
		
		$('#importWfsLayers').on('click', function(e) {
		
			var wfsRequestMessenger;
			var layersInfo = [];
			var emptyLayerName = false;
			var emptyLayerStyle = false;
			for(i=0; i < pageState.featureTypesToSave.length; i++){
				
				//Check if there are Layer names for each wfs layer
				if($('#modalLayerName'+i+'').val()==null || $('#modalLayerName'+i+'').val().replace(/\s/g, '').length == 0) {
					
					$('#modalLayerName'+i+'').css({"border":"1px solid #b94a48"});
					$('#modalLayerNameError'+i+'').css({"display":"block"});
					
					emptyLayerName = true;
					
				}
				
				if($('#modalLayerStyle'+i+'').val()==null || $('#modalLayerStyle'+i+'').val().replace(/\s/g, '').length == 0) {
					
					$('#modalLayerStyle'+i+'').css({"border":"1px solid #b94a48"});
					$('#modalLayerStyleError'+i+'').css({"display":"block"});
					
					emptyLayerStyle = true;
					
				}
				
				var wfsReaquestLayer = {};
				wfsReaquestLayer.layerName = $('#modalLayerName'+i+'').val().replace(/ /g,"_");
				wfsReaquestLayer.layerDescription = $('#modalLayerDescription'+i+'').val();
				wfsReaquestLayer.featureTypes = pageState.featureTypesToSave[i];
				wfsReaquestLayer.style = $('#modalLayerStyle'+i+' option:selected').text();
				
				layersInfo.push(wfsReaquestLayer);
				
		    } 
			
			if(emptyLayerName == true || emptyLayerStyle == true)
				return;
			
			$('#layerInfoModal').modal('hide');
			
			var wfsRequestMessenger = {
		    	url: $('#url').val(),
//					    	version: $('#version').val(),
		    	version: "1.0.0",
		    	layersInfo: layersInfo
			};
			
			
			postDataToServer(wfsRequestMessenger,  window.config.createResourceURL('import/storeShapeFilesForFeatureType'), function(response){
				if (response.status) {
					notificator.noty({
					    text: response.message,
					    type: 'success',
					    template: '<div class="noty_message"><span class="noty_text"></span><div class="noty_close"></div></div>',
					    theme: 'relax',
					    closeWith: ['button'],
					    timeout: 3000,
					    maxVisible:1,
					    animation: {
					        open: 'animated flipInX', 
					        close: 'animated flipOutX',
					        easing: 'swing',
					        speed: 400
					    }
					});	
					return;
				} else {
					notificator.noty({
					    text: response.message,
					    type: 'error',
					    template: '<div class="noty_message"><span class="noty_text"></span><div class="noty_close"></div></div>',
					    theme: 'relax',
					    closeWith: ['button'],
					    timeout: false,
					    maxVisible:1,
					    animation: {
					        open: 'animated flipInX', 
					        close: 'animated flipOutX',
					        easing: 'swing',
					        speed: 400
					    }
					});	
				return;
				}
			});
			
		});
	},
	cleanMe: function(e) {
		$(this.options.content).children().remove();
	}	
});