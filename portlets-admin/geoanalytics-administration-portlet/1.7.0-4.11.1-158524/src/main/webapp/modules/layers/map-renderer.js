(function() {

	"use strict";

	var mapRenderer = {
	    map : null,
	    layers : [],
	    currentLayer : null,
	    renderMode : null,
	    init : function(renderMode) {
		    this.renderMode = renderMode ? renderMode : "single";
		    this.loadCSS();
		    this.initializeMap();
	    },
	    loadCSS : function() {
		    $("<link/>", {
		        rel : "stylesheet",
		        type : "text/css",
		        href : window.config.contextPath + "/modules/layers/map-renderer.css"
		    }).appendTo("head");
	    },
	    initializeMap : function() {
		    this.addCenterButton();

		    var layer = new ol.layer.Tile({
			    source : new ol.source.OSM({
			    // wrapX: false // prevent multiple world maps in X axis
			    })
		    });

		    var map = new ol.Map({
		        target : "render-map",
		        controls : ol.control.defaults({
		            zoom : true,
		            attribution : false,
		            rotate : false
		        }).extend([ new mapRenderer.createCenterButton() ]),
		        layers : [ layer ],
		        view : new ol.View({
		            center : ol.proj.fromLonLat([ 22.00, 37.00 ]),
		            zoom : 4,
		            minZoom : 2
		        })
		    });

		    this.map = map;
	    },
	    renderLayer : function(layer) {

		    // empty parameters which will be filled later by WMS request of OpenLayers

		    var url = window.config.createResourceURL("wms", "");

		    var layerToRender = new ol.layer.Tile({
			    source : new ol.source.TileWMS({
			        url : url,
			        params : mapRenderer.createParameters(layer.workspace + ":" + layer.id),
			        tileLoadFunction : function(imageTile, src) {
				        var client = new XMLHttpRequest();
				        client.open("GET", src);
				        client.responseType = "arraybuffer";

				        client.onload = function() {
					        var arrayBufferView = new Uint8Array(this.response);
					        var blob = new Blob([ arrayBufferView ], {
						        type : "image/png"
					        });
					        var urlCreator = window.URL || window.webkitURL;
					        var imageUrl = urlCreator.createObjectURL(blob);
					        imageTile.getImage().src = imageUrl;
				        };
				        client.send();
			        },
			        projection: ol.proj.get('EPSG:4326')
//			        wrapX : false,
			    })
		    });

		    if (this.renderMode === "single" && this.currentLayer != null) {
			    this.map.removeLayer(this.currentLayer);
		    }

		    this.currentLayer = layerToRender;
		    this.map.addLayer(layerToRender);
		    this.map.updateSize(); // important when maps gets drawn on  an resizible container (modal, window, div etc). Map does now show otherwise due to width change
	    },
	    createParameters : function(layer) {
		    return {
		        "BGCOLOR" : "0xcccccc",
		        "Layers" : [ layer ],
		        "srs" : "EPSG:4326",
		    };
	    },
	    addCenterButton : function() {
		    ol.inherits(mapRenderer.createCenterButton, ol.control.Control);
	    },
	    createCenterButton : function(opt_options) {
		    var options = opt_options || {};

		    var button = document.createElement('button');
		    button.innerHTML = '<i class="fa fa-dot-circle-o" aria-hidden="true"></i>';
		    button.title = "Center Map";

		    var centerMapCallback = function(e) {
			    mapRenderer.map.getView().setCenter(ol.proj.fromLonLat([ 22.00, 37.00 ]))
		    };

		    button.addEventListener('click', centerMapCallback, false);

		    var element = document.createElement('div');
		    element.className = 'ol-unselectable ol-control';
		    element.appendChild(button);
		    element.style.top = "65px";
		    element.style.left = "0.5em";

		    ol.control.Control.call(this, {
		        element : element,
		        target : options.target
		    });
	    }
	};

	window.mapRenderer = mapRenderer;
	window.mapRenderer.init();

})();