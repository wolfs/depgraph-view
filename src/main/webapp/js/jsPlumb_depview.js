;
(function() {

	window.paper = jQuery("#paper");
	window.depview_colordep = '#FF0000'; // red
	window.depview_colordepnew = '#FFFF00'; // yellow
	window.depview_colorcopy = '#32CD32'; // green

	window.depview = {
		init : function() {
			jsPlumb.importDefaults({
				Connector : "Bezier", // Straight, Flowchart, Straight, Bezier
				// default drag options
				DragOptions : {
					cursor : 'pointer',
					zIndex : 2000
				},
				// default to blue at one end and green at the other
				EndpointStyles : [ {
					fillStyle : '#225588'
				}, {
					fillStyle : '#558822'
				} ],
				// blue endpoints 7px; green endpoints 7px.
				Endpoints : [ [ "Dot", {
					radius : 6
				} ], [ "Dot", {
					radius : 6
				} ] ],
				
				// connector line 2px
				PaintStyle : { 
					lineWidth : 2, 
					strokeStyle : window.depview_colordepnew,
					joinstyle:"round"}, 
				
				// the overlays to decorate each connection with. note that the
				// label overlay uses a function to generate the label text; in
				// this case it returns the 'labelText' member that we set on each
				// connection in the 'init' method below.
				ConnectionOverlays : [ [ "Arrow", {
					location : 1.0,
					foldback:0.5
				} ]
				]
				
			});
			
			jQuery.getJSON('graph.json', function(data) {

				var top = 120;
				
				var breite = 80; 
				var space = 20;
				
				var xOverall = 0;
				
				var clusters = data["clusters"];
				// iterate clusters
				jQuery.each(clusters, function(i, cluster) {
					var maxJobsOnLevel = 0;
					// get max jobs on a levels for one cluster
					jQuery.each(cluster, function(key, val) {
						if(val.length > maxJobsOnLevel) {
							maxJobsOnLevel = val.length;
						}
					});
					
					var additionalSpace = (maxJobsOnLevel - 1) * space;
					var clusterBreite = (maxJobsOnLevel * breite) + additionalSpace;
					
					var xCluster = clusterBreite + xOverall;
					
					// iterate levels of cluster
					jQuery.each(cluster, function(key, val) {
						var level = parseInt(key);
						var nrOfLevelJobs = val.length;
						
						var xPosition = xCluster;
						var xMove = 0;
						var spaceBetweenJobs = 0;
						var nrOfSpaces = nrOfLevelJobs - 1
						if(nrOfLevelJobs == 1){
							xPosition = xOverall + (clusterBreite / 2);
						}else{
							xPosition = xCluster - clusterBreite;
							var spareSpaceOnLevel = clusterBreite - ((nrOfLevelJobs -1) * breite);
							spaceBetweenJobs = spareSpaceOnLevel / nrOfSpaces;
							xMove = spaceBetweenJobs + breite;  
						}
						
						var yPosition = (level * 120) + top;
						// iterate jobs per level
						jQuery.each(val, function(i, jobName) {
							jQuery('<div>' + jobName + '</div>').attr('id', jobName).addClass('window').css('top', yPosition).css('left', xPosition).appendTo(window.paper);
							xPosition += xMove;
							
							// definitions for drag/drop connections 
							jsPlumb.makeSource(jobName, {
								anchor : "BottomCenter",
							});
							jsPlumb.makeTarget(jobName, {
								anchor : "TopCenter",
							});
							
						});
					});
					xOverall = xCluster + breite +(space * 2);
				});
				
				var edges = data["edges"];
				jQuery.each(edges, function(i, edge) {
					from = getJobDiv(edge["from"]);
					to = getJobDiv(edge["to"]);
					// creates/defines the look and feel of the loaded connections: red="dep", green="copy"
					var connection;
					if("copy" == edge["type"]){
						connection = jsPlumb.connect({ source : from, target : to, anchors : [ "BottomCenter", "TopCenter" ], paintStyle:{lineWidth : 2, strokeStyle: window.depview_colorcopy}, connector:"Straight",
													   overlays:[[ "Label", { label: "copy", id: from+'.'+to } ]]
													});
					}else{
						connection = jsPlumb.connect({ source : from, target : to, anchors : [ "BottomCenter", "TopCenter" ], paintStyle:{lineWidth : 2, strokeStyle: window.depview_colordep}, connector:"Straight"}); 
						// only allow deletion of "dep" connections
						connection.bind("click", function(conn) {
							if(confirm('delete connection: '+ conn.sourceId +" -> "+conn.targetId+'?')){
								jQuery.ajax({
									url : 'edge/' + conn.sourceId + '/'	+ conn.targetId,
									type : 'DELETE',
									success : function(response) {
										jsPlumb.detach(conn);
									},
									error: function (request, status, error) {
								        alert(status+": "+error);
									}
								});
							}
						});
					}
				});
				
				jsPlumb.bind("jsPlumbConnection", function(info) {
					jQuery.ajax({
						   url: 'edge/'+info.sourceId +'/'+info.targetId,
						   type: 'PUT',
						   success: function( response ) {
//							   alert('Load was performed.');
						   },
						   error: function (request, status, error) {
						        alert(request.responseText);
						   }
					});
					// allow deletion of newly created connection
					info.connection.bind("click", function(conn) {
						if(confirm('delete connection: '+ conn.sourceId +" -> "+conn.targetId+'?')){
							jQuery.ajax({
								url : 'edge/' + conn.sourceId + '/'	+ conn.targetId,
								type : 'DELETE',
								success : function(response) {
									jsPlumb.detach(conn);
								},
								error: function (request, status, error) {
							        alert(request.responseText);
							    }
							});
						}
					});
				});
				
				
				// make all the window divs draggable
				jsPlumb.draggable(jsPlumb.getSelector(".window"));
				
				
			});
		}
	};
})();

function getJobDiv(jobName) {
	return jQuery('#' + jobName);
}


// start jsPlumb
jsPlumb.bind("ready", function() {
	// chrome fix.
	document.onselectstart = function () { return false; };
	
	jsPlumb.setRenderMode(jsPlumb.SVG);
	depview.init();
});

