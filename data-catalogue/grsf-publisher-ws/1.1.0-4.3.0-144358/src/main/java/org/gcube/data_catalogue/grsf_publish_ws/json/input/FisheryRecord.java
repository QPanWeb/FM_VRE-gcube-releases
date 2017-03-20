package org.gcube.data_catalogue.grsf_publish_ws.json.input;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gcube.data_catalogue.grsf_publish_ws.custom_annotations.CustomField;
import org.gcube.data_catalogue.grsf_publish_ws.custom_annotations.Group;
import org.gcube.data_catalogue.grsf_publish_ws.custom_annotations.Tag;
import org.gcube.data_catalogue.grsf_publish_ws.custom_annotations.TimeSeries;
import org.gcube.data_catalogue.grsf_publish_ws.utils.groups.Fishery_Type;
import org.gcube.data_catalogue.grsf_publish_ws.utils.groups.Production_System_Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A fishery record bean
 * @author Costantino Perciante at ISTI-CNR (costantino.perciante@isti.cnr.it)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FisheryRecord extends Common{

	@JsonProperty("fishery_name")
	@NotNull(message="fishery_name cannot be null")
	@Size(min=1, message="fishery_name cannot be empty")
	@CustomField(key="Fishery Name")
	private String fisheryName;

	@JsonProperty("fishery_id")
	@CustomField(key="Fishery id")
	private String fisheryId;

	@JsonProperty("species")
	@Tag
	@CustomField(key="Species")
	private List<String> species;

	@JsonProperty("fishing_area")
	@CustomField(key="Fishing area")
	@Tag
	private List<String> fishingArea;

	@JsonProperty("exploited_stocks")
	@CustomField(key="Exploited stocks")
	private List<String> exploitingStocks;

	@JsonProperty("management_entity")
	@CustomField(key="Management entity")
	@Tag
	private List<String> managementEntity;

	@JsonProperty("jurisdiction_area")
	@CustomField(key="Jurisdiction Area")
	@Tag
	private List<String> jurisdictionArea;

	@JsonProperty("production_system_type")
	//@Group
	@Tag
	@CustomField(key="Production system type")
	private List<Production_System_Type> productionSystemType;

	@JsonProperty("flag_state")
	@Tag
	@CustomField(key="Flag state")
	private List<String> flagState;

	@JsonProperty("fishing_gear")
	@Tag
	@CustomField(key="Fishing gear")
	private List<String> fishingGear;

	@JsonProperty("environment")
	@CustomField(key="Environment")
	private String environment;

	@JsonProperty("catches_or_landings")
	@CustomField(key="Catches or landings")
	//@Tag
	@TimeSeries
	@Valid
	private List<TimeSeriesBean<String, String>> catchesOrLandings;

	@JsonProperty("type")
	@CustomField(key="Type")
	//@Tag
	@Group
	private Fishery_Type type;

	public FisheryRecord() {
		super();
	}

	/**
	 * Create a Fishery record.
	 * @param fisheryName
	 * @param fisheryId
	 * @param scientificName
	 * @param fishingArea
	 * @param exploitingStocks
	 * @param managementEntity
	 * @param jurisdictionArea
	 * @param productionSystemType
	 * @param flagState
	 * @param fishingGear
	 * @param environment
	 * @param catchesOrLandings
	 * @param type
	 */
	public FisheryRecord(String fisheryName, String fisheryId,
			List<String> species, List<String> fishingArea,
			List<String> exploitingStocks, List<String> managementEntity,
			List<String> jurisdictionArea,
			List<Production_System_Type> productionSystemType,
			List<String> flagState, List<String> fishingGear,
			String environment,
			List<TimeSeriesBean<String, String>> catchesOrLandings,
			Fishery_Type type) {
		super();
		this.fisheryName = fisheryName;
		this.fisheryId = fisheryId;
		this.species = species;
		this.fishingArea = fishingArea;
		this.exploitingStocks = exploitingStocks;
		this.managementEntity = managementEntity;
		this.jurisdictionArea = jurisdictionArea;
		this.productionSystemType = productionSystemType;
		this.flagState = flagState;
		this.fishingGear = fishingGear;
		this.environment = environment;
		this.catchesOrLandings = catchesOrLandings;
		this.type = type;
	}
	
	public String getFisheryName() {
		return fisheryName;
	}

	public void setFisheryName(String fisheryName) {
		this.fisheryName = fisheryName;
	}

	public String getFisheryId() {
		return fisheryId;
	}

	public void setFisheryId(String fisheryId) {
		this.fisheryId = fisheryId;
	}

	public List<String> getFishingArea() {
		return fishingArea;
	}

	public void setFishingArea(List<String> fishingArea) {
		this.fishingArea = fishingArea;
	}

	public List<String> getJurisdictionArea() {
		return jurisdictionArea;
	}

	public void setJurisdictionArea(List<String> jurisdictionArea) {
		this.jurisdictionArea = jurisdictionArea;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public List<TimeSeriesBean<String, String>> getCatchesOrLandings() {
		return catchesOrLandings;
	}

	public void setCatchesOrLandings(List<TimeSeriesBean<String, String>> catchesOrLandings) {
		this.catchesOrLandings = catchesOrLandings;
	}

	public Fishery_Type getType() {
		return type;
	}

	public void setType(Fishery_Type type) {
		this.type = type;
	}

	public List<String> getSpecies() {
		return species;
	}

	public void setSpecies(List<String> species) {
		this.species = species;
	}

	public List<String> getExploitingStocks() {
		return exploitingStocks;
	}

	public void setExploitingStocks(List<String> exploitingStocks) {
		this.exploitingStocks = exploitingStocks;
	}

	public List<String> getManagementEntity() {
		return managementEntity;
	}

	public void setManagementEntity(List<String> managementEntity) {
		this.managementEntity = managementEntity;
	}

	public List<Production_System_Type> getProductionSystemType() {
		return productionSystemType;
	}

	public void setProductionSystemType(
			List<Production_System_Type> productionSystemType) {
		this.productionSystemType = productionSystemType;
	}

	public List<String> getFlagState() {
		return flagState;
	}

	public void setFlagState(List<String> flagState) {
		this.flagState = flagState;
	}

	public List<String> getFishingGear() {
		return fishingGear;
	}

	public void setFishingGear(List<String> fishingGear) {
		this.fishingGear = fishingGear;
	}

	@Override
	public String toString() {
		return "FisheryRecord [fisheryName=" + fisheryName + ", fisheryId="
				+ fisheryId + ", species=" + species + ", fishingArea="
				+ fishingArea + ", exploitingStocks=" + exploitingStocks
				+ ", managementEntity=" + managementEntity
				+ ", jurisdictionArea=" + jurisdictionArea
				+ ", productionSystemType=" + productionSystemType
				+ ", flagState=" + flagState + ", fishingGear=" + fishingGear
				+ ", environment=" + environment + ", catchesOrLandings="
				+ catchesOrLandings + ", type=" + type + "]";
	}

}
