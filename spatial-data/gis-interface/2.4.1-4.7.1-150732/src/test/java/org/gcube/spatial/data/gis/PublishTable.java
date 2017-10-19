package org.gcube.spatial.data.gis;

import java.util.Date;

import org.gcube.spatial.data.geonetwork.LoginLevel;
import org.gcube.spatial.data.geonetwork.iso.GcubeISOMetadata;
import org.gcube.spatial.data.geonetwork.iso.Thesaurus;
import org.gcube.spatial.data.gis.model.report.PublishResponse;
import org.geotoolkit.metadata.iso.extent.DefaultExtent;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.spatial.GeometricObjectType;
import org.opengis.metadata.spatial.TopologyLevel;

import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;

public class PublishTable {

	/**
	 * @param args
	 */
	
	
	
	private static final String crs="GEOGCS[\"WGS 84\", DATUM[\"World Geodetic System 1984\", SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]],"+ 
			"AUTHORITY[\"EPSG\",\"6326\"]], PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]],  UNIT[\"degree\", 0.017453292519943295],"+ 
			"AXIS[\"Geodetic longitude\", EAST],  AXIS[\"Geodetic latitude\", NORTH],  AUTHORITY[\"EPSG\",\"4326\"]]";
	
	
	public static void main(String[] args) throws Exception {
		
		String scope="/gcube/devsec/devVRE";
		String toPublishTable="shp_bf4c1d947fd4469ab6d9077bb21216cc";
		String datastore="shp_bf4c1d947fd4469ab6d9077bb21216cc";
		
		
		String defaultStyle="defaultStyle";
		String workspace="aquamaps";
		
		
		
		GSFeatureTypeEncoder fte=new GSFeatureTypeEncoder();
		fte.setEnabled(true);
		fte.setLatLonBoundingBox(-180.0, -90.0, 180.0, 90.0, crs);
		fte.setName(toPublishTable);
		fte.setNativeCRS(crs);
		
		GSLayerEncoder le=new GSLayerEncoder();
		le.setDefaultStyle(defaultStyle);
		le.setEnabled(true);
		
		TokenSetter.set(scope);
		GcubeISOMetadata meta=fillMeta();
		
		
		GISInterface gis=GISInterface.get();
		System.out.println(gis.getCurrentGeoServer());
		PublishResponse resp=gis.publishDBTable(workspace, datastore, fte, le, meta.getMetadata(), "datasets", "_none_", LoginLevel.DEFAULT,false);
		System.out.println(resp);
	}

	
	
	private static GcubeISOMetadata fillMeta() throws Exception{
		GcubeISOMetadata meta=new GcubeISOMetadata();
		meta.setAbstractField("This metadata is just a test");
		meta.setCreationDate(new Date(System.currentTimeMillis()));
		meta.setExtent((DefaultExtent) DefaultExtent.WORLD);
		meta.setGeometricObjectType(GeometricObjectType.SURFACE);
		meta.setPresentationForm(PresentationForm.MAP_DIGITAL);
		meta.setPurpose("Purpose of this layer is to test the library");
		meta.setResolution(0.5d);
		meta.setTitle("My Test Layer");
		meta.setTopologyLevel(TopologyLevel.GEOMETRY_ONLY);
		meta.setUser("fabio.sinibaldi");		
		
		
		meta.addCredits("Thanks to me");
		meta.addGraphicOverview("http://www.d4science.org/D4ScienceOrg-Social-theme/images/custom/D4ScienceInfrastructure.png");
		
		Thesaurus generalThesaurus=meta.getConfig().getThesauri().get("General");		
		meta.addKeyword("TEST", generalThesaurus);
		meta.addKeyword("Geoserverinterface", generalThesaurus);
		
		meta.addTopicCategory(TopicCategory.BIOTA);
		return meta;
	}
	
}
