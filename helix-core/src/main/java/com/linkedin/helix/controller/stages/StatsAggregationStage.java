package com.linkedin.helix.controller.stages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.linkedin.helix.ClusterDataAccessor;
import com.linkedin.helix.ClusterManager;
import com.linkedin.helix.PropertyType;
import com.linkedin.helix.ZNRecord;
import com.linkedin.helix.alerts.AlertProcessor;
import com.linkedin.helix.alerts.AlertValueAndStatus;
import com.linkedin.helix.alerts.AlertsHolder;
import com.linkedin.helix.alerts.ExpressionParser;
import com.linkedin.helix.alerts.StatsHolder;
import com.linkedin.helix.alerts.Tuple;
import com.linkedin.helix.controller.pipeline.AbstractBaseStage;
import com.linkedin.helix.controller.pipeline.StageContext;
import com.linkedin.helix.controller.pipeline.StageException;
import com.linkedin.helix.healthcheck.AggregationType;
import com.linkedin.helix.healthcheck.AggregationTypeFactory;
import com.linkedin.helix.healthcheck.PerformanceHealthReportProvider;
import com.linkedin.helix.healthcheck.Stat;
import com.linkedin.helix.healthcheck.StatHealthReportProvider;
import com.linkedin.helix.model.HealthStat;
import com.linkedin.helix.model.LiveInstance;
import com.linkedin.helix.monitoring.mbeans.ClusterAlertMBeanCollection;

/**
 * For each LiveInstances select currentState and message whose sessionId
 * matches sessionId from LiveInstance Get ResourceKey,State for all the
 * resources computed in previous State [ResourceComputationStage]
 * 
 * @author asilbers
 * 
 */
public class StatsAggregationStage extends AbstractBaseStage
{
  private static final Logger logger = Logger
		    .getLogger(StatsAggregationStage.class.getName());
	
  StatsHolder _statsHolder;
  AlertsHolder _alertsHolder;
  Map<String, Map<String, AlertValueAndStatus>> _alertStatus;
  Map<String, Tuple<String>> _statStatus;
  ClusterAlertMBeanCollection _alertBeanCollection = new ClusterAlertMBeanCollection();
  
  public final String PARTICIPANT_STAT_REPORT_NAME = StatHealthReportProvider.REPORT_NAME;
  public final String ESPRESSO_STAT_REPORT_NAME = "RestQueryStats";
  public final String REPORT_NAME = "AggStats";
  //public final String DEFAULT_AGG_TYPE = "decay";
  //public final String DEFAULT_DECAY_PARAM = "0.1";
  //public final String DEFAULT_AGG_TYPE = "window";
  //public final String DEFAULT_DECAY_PARAM = "5";
 
  public StatHealthReportProvider _aggStatsProvider;
  
  //public AggregationType _defaultAggType;
 
  public Map<String, Map<String, AlertValueAndStatus>> getAlertStatus() 
  {
	  return _alertStatus;
  }
  
  public Map<String, Tuple<String>> getStatStatus() 
  {
	return _statStatus;
  }

public void persistAggStats(ClusterManager manager)
  {
	  Map<String, String> report = _aggStatsProvider.getRecentHealthReport();
      Map<String, Map<String, String>> partitionReport = _aggStatsProvider
          .getRecentPartitionHealthReport();
      ZNRecord record = new ZNRecord(_aggStatsProvider.getReportName());
      if (report != null) {
      	record.setSimpleFields(report);
      }
      if (partitionReport != null) {
      	record.setMapFields(partitionReport);
      }
      
      ClusterDataAccessor accessor = manager.getDataAccessor();
      boolean retVal = accessor.setProperty(PropertyType.PERSISTENTSTATS,
              record);
      if (retVal == false) {
    	  logger.error("attempt to persist derived stats failed");
      }
  }
 
  /*
  public void addAggStat(Map<String, String> statName, String statVal, String statTimestamp)
  {
	  Stat es = new Stat(statName);
	  es.setAggType(_defaultAggType);
	  _aggStatsProvider.setStat(es, statVal, statTimestamp);
  }
  
  public void updateAggStat(Stat aggStat, String statVal, String statTimestamp)
  {
	  _aggStatsProvider.setStat(aggStat, statVal, statTimestamp);
  }
  */
  
  
  /*
   * Reconcile participant stat with set of agg stats
   */
  
  /*
  public void applyParticipantStat(Map<String, String> participantStatName, String participantStatVal, 
		  String participantStatTimestamp)
  {
	  
	  Stat participantStat = new Stat(participantStatName);
	  //check each agg stat to see if "contains"/equal to participant stat
	  for (Stat aggStat : _aggStatsProvider.keySet()) {
		  if (participantStat.equals(aggStat)) {
			  //check if participant stat is newer than agg stat
			  long currAggTimestamp = _aggStatsProvider.getStatTimestamp(aggStat);
			  if (Long.parseLong(participantStatTimestamp) > currAggTimestamp) {
				  //apply the stat
				  String currAggVal = _aggStatsProvider.getStatValue(participantStat);
				  AggregationType aggType = AggregationTypeFactory.getAggregationType(aggStat._aggTypeName);
				  String aggStatVal = aggType.merge(
				    participantStatVal, currAggVal, currAggTimestamp);
				  updateAggStat(aggStat, aggStatVal, participantStatTimestamp);
			  }
			  else {
				  //participant stat already applied, do nothing
			  }
		  }
	  }
	  //check if aggStats contains participant stat exactly.  if not, add.
	  if (!_aggStatsProvider.contains(participantStat)) {
		  addAggStat(participantStatName, participantStatVal, participantStatTimestamp);
	  }
	  */
	  
	  /*
	  //check if we have agg stat matching this participant stat
	  if (_aggStatsProvider.contains(participantStat)) {
		  //check if participant stat is newer than agg stat
		  long currAggTimestamp = _aggStatsProvider.getStatTimestamp(participantStat);
		  if (Long.parseLong(participantStatTimestamp) > currAggTimestamp) {
			  //apply the stat
			  //AggregationType agg = !!!!!!!!
			  double currAggVal = _aggStatsProvider.getStatValue(participantStat);
			  //TODO: something other than simple accumulation			  
			  String aggStatVal = String.valueOf(currAggVal + Double.parseDouble(participantStatVal));
			  addAggStat(participantStatName, aggStatVal, participantStatTimestamp);
		  }
		  else {
			  //participant stat already applied, do nothing
		  }
	  }
	  else {
		  //no agg stat for this participant stat yet
		  addAggStat(participantStatName, participantStatVal, participantStatTimestamp);
	  }
	  */
  //}
  
  /*
  public void initAggStats(ClusterDataCache cache) 
  {
	  _aggStatsProvider = new StatHealthReportProvider();
	  _aggStatsProvider.setReportName(REPORT_NAME);
	  HealthStat hs = cache.getGlobalStats();
	  if (hs != null) {
		  Map<String, Map<String, String>> derivedStatsMap = hs.getMapFields();
		  //most of map becomes the "stat", except value which becomes value, timestamp becomes timestamp
		  for (String key : derivedStatsMap.keySet()) {
			  addAggStat(derivedStatsMap.get(key), 
					  derivedStatsMap.get(key).get(StatHealthReportProvider.STAT_VALUE),
					  derivedStatsMap.get(key).get(StatHealthReportProvider.TIMESTAMP));		  
		  }
	  }
  }
  */
  
  @Override
  public void init(StageContext context) 
  {
  }
  
  public String getAgeStatName(String instance)
  {
	  return instance + ExpressionParser.statFieldDelim + "reportingage";
  }
  
  //currTime in seconds
  public void reportAgeStat(LiveInstance instance, long currTime)
  {
	  String statName = getAgeStatName(instance.getInstanceName());
	  //TODO: call to get modifiedTime is a stub right now
	  long modifiedTime = instance.getModifiedTime();
	  long age = currTime - modifiedTime; //XXX: ensure this is in seconds
	  Map<String, String> ageStatMap = new HashMap<String, String>();
	  ageStatMap.put(StatsHolder.TIMESTAMP_NAME, String.valueOf(currTime));
	  ageStatMap.put(StatsHolder.VALUE_NAME, String.valueOf(age));
	 //note that applyStat will only work if alert already added
	  _statsHolder.applyStat(statName, ageStatMap);
  }
  
  @Override
  public void process(ClusterEvent event) throws Exception
  {
	//String aggTypeName = DEFAULT_AGG_TYPE+AggregationType.DELIM+DEFAULT_DECAY_PARAM;
	//_defaultAggType = AggregationTypeFactory.getAggregationType(aggTypeName);
	
    ClusterManager manager = event.getAttribute("clustermanager");
    if (manager == null)
    {
      throw new StageException("clustermanager attribute value is null");
    }
    
    _statsHolder = new StatsHolder(manager);
    _alertsHolder = new AlertsHolder(manager);
    
    ClusterDataCache cache = event.getAttribute("ClusterDataCache");

    //init agg stats from cache
    //initAggStats(cache);
    
    Map<String, LiveInstance> liveInstances = cache.getLiveInstances();
    
    long currTime = System.currentTimeMillis()/1000;
    //for each live node, read node's stats
    for (LiveInstance instance : liveInstances.values())
    {
    	//generate and report stats for how old this node's report is
    	reportAgeStat(instance, currTime);

    	String instanceName = instance.getInstanceName();
    	logger.debug("instanceName: "+instanceName);
    	//XXX: now have map of HealthStats, so no need to traverse them...verify correctness
    	Map<String, HealthStat> stats;
    	stats = cache.getHealthStats(instanceName);
    	//find participants stats
    	HealthStat participantStat = stats.get(ESPRESSO_STAT_REPORT_NAME);

    	//XXX: need to convert participantStat to a better format
    	//need to get instanceName in here
    	
    	if (participantStat != null) {
    		String timestamp = String.valueOf(instance.getModifiedTime());
    		Map<String, Map<String, String>> statMap = participantStat.getHealthFields(instanceName,timestamp);
    		for (String key : statMap.keySet()) {
    			_statsHolder.applyStat(key, statMap.get(key));
    		}

    	}
    }
    
    //populate _statStatus
    _statStatus = _statsHolder.getStatsMap();
    
    for (String statKey : _statStatus.keySet()) {
    	logger.debug("Stat key, value: "+statKey+": "+_statStatus.get(statKey));
    }
   
    //execute alerts, populate _alertStatus
    _alertStatus = AlertProcessor.executeAllAlerts(_alertsHolder.getAlertList(), _statsHolder.getStatsList());
    for(String originAlertName : _alertStatus.keySet())
    {
      _alertBeanCollection.setAlerts(originAlertName, _alertStatus.get(originAlertName));
    }
    //write out alert status (to zk)
    _alertsHolder.addAlertStatusSet(_alertStatus);
    
    //TODO: access the 2 status variables from somewhere to populate graphs
    
    //logging alert status
    for (String alertOuterKey : _alertStatus.keySet()) {
    	logger.debug("Alert Outer Key: "+alertOuterKey);
    	Map<String, AlertValueAndStatus>alertInnerMap = _alertStatus.get(alertOuterKey);
    	if (alertInnerMap == null) {
    		logger.debug(alertOuterKey + " has no alerts to report.");
    		continue;
    	}
    	for (String alertInnerKey: alertInnerMap.keySet()) {
    		logger.debug("  "+alertInnerKey+" value: "+alertInnerMap.get(alertInnerKey).getValue()+
    				", status: "+alertInnerMap.get(alertInnerKey).isFired());
    	}
    }
  }
}