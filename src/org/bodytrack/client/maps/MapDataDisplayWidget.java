package org.bodytrack.client.maps;

import java.util.ArrayList;
import java.util.HashMap;

import org.bodytrack.client.DataPlot;

import com.google.gwt.maps.client.MapType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class MapDataDisplayWidget extends AbsolutePanel {
	public static final String MAPS_API_KEY = "ABQIAAAAdR-Q_sI1Y8dS1vYvWivhNhSlSrlRFhTM6ng3Bk6KLjZBgSJbURS_YN65zNwDkownQjfGClEfKqSqcg";
	//public static final String MAPS_API_KEY = "ABQIAAAAdR-Q_sI1Y8dS1vYvWivhNhTyIbbAi0wlteY5p1ENx1gJCimCkBReonXsceCrZcFMHMKKO_GxSo2e-Q";
	private MapWidget map = new MapWidget();
	private PathOverlay pathOverlay = new PathOverlay(1);
	
	//holds a dataset in a hashmap using its name as the key and a size 2 array of arraylists holding time value pairs
	private HashMap<String, ArrayList<double[]>> dataMap = new HashMap<String, ArrayList<double[]>>();
	
	public MapDataDisplayWidget(int width, int height){
		super();
		setSize(width + "px",height + "px");
		add(map);
		map.setSize("100%","100%");
		map.setCurrentMapType(MapType.getHybridMap());
		map.setScrollWheelZoomEnabled(true);
		map.setSize("100%", "100%");
		// Add some controls for the zoom level
		map.addControl(new LargeMapControl());
		//add controls for map type
		map.addControl(new MapTypeControl());
		map.addOverlay(pathOverlay);
	}
	
	public static int getDefaultWidth(){
		return 800;
	}
	
	public static int getDefaultHeight(){
		return 400;
	}

	public void addPoint(DataPlot dataPlot, double date, double value) {
		String key = dataPlot.getDeviceName() + "." + dataPlot.getChannelName();
		ArrayList<double[]> data = dataMap.get(key);
		if (data == null){
			data = new ArrayList<double[]>();
			dataMap.put(key, data);
		}
		data.add(new double[]{date,value});
	}

	public void clearData(DataPlot dataPlot) {
		dataMap.put(dataPlot.getDeviceName() + "." + dataPlot.getChannelName(), new ArrayList<double[]>());
		if (dataPlot.getChannelName().equals("longitude") || dataPlot.getChannelName().equals("latitude"))
			pathOverlay.clearPoints();
	}

	public void processData(DataPlot plot, double minTime, double maxTime) {
		String key = plot.getDeviceName() + "." + plot.getChannelName();
		ArrayList<double[]> data = dataMap.get(key);
		if (data != null){
			if (plot.getChannelName().equals("longitude") ){
				ArrayList<double[]> longitudes = data;
				ArrayList<double[]> latitudes = dataMap.get(plot.getDeviceName() + ".latitude");
				ArrayList<double[]> uncertainty = dataMap.get(plot.getDeviceName() + ".uncertainty_in_meters");
				ArrayList<double[]> accX = dataMap.get(plot.getDeviceName() + ".acceleration_x");
				ArrayList<double[]> accY = dataMap.get(plot.getDeviceName() + ".acceleration_y");
				ArrayList<double[]> accZ = dataMap.get(plot.getDeviceName() + ".acceleration_z");
				double prevTime = 0;
				if (latitudes != null && uncertainty != null){
					int loni = 0;
					int lati = 0;
					int unci = 0;
					int count = 0;
					while (loni < longitudes.size() && lati < latitudes.size() && unci < uncertainty.size()){
						double[] lonPoint = longitudes.get(loni++);
						double[] latPoint = latitudes.get(lati++);
						double[] uncPoint = uncertainty.get(unci++);
						/*while (lonPoint[0] != latPoint[0] && loni < longitudes.size() && lati < latitudes.size()){
							if (lonPoint[0] < latPoint[0])
								lonPoint = longitudes.get(loni++);
							else
								latPoint = latitudes.get(lati++);
						}*/
						if (lonPoint[0] == latPoint[0] && uncPoint[1] < 35 && lonPoint[0] >= minTime && lonPoint[0] <= maxTime){
							pathOverlay.addPoint(LatLng.newInstance(latPoint[1], lonPoint[1]));
							if (count != 0){
								if (accX != null && accY != null && accZ != null){
									double acceleration = 0;
									int startIndex = -1;
									for (int i = 0; i < accX.size(); i++){
										if (Math.abs(prevTime - accX.get(i)[0]) < 20){
											startIndex = i;
											break;
										}
									}
									if (startIndex != -1){
										long endIndex = -1;
										for (int i = 0; i < accX.size(); i++){
											if (Math.abs(lonPoint[0] - accX.get(i)[0]) < 20){
												endIndex = i;
												break;
											}
										}
										for (int i = startIndex; i <= endIndex; i++){
											acceleration += Math.sqrt(Math.pow(accX.get(i)[1],2) + Math.pow(accY.get(i)[1],2) + Math.pow(accZ.get(i)[1],2));
										}
										acceleration /= endIndex - startIndex + 1;
									}
									
									pathOverlay.setSegmentColor(0, count - 1, getColorIntensity(0,15,acceleration));
								}
							}
							prevTime = lonPoint[0];
							count++;
						}
					}
				}
			}
		}
	}
	
	private String[] rainbow = new String[]{"violet","blueviolet","blue","darkcyan","green","yellowgreen","yellow",
					"orange","orangered","red"
	};
	
	private String getColorIntensity(double minValue, double maxValue, double value){
		double range = maxValue - minValue;
		value -= minValue;
    	if (value <= 0){
    		return "black";
    	}
    	for (int i = 0; i < rainbow.length - 1; i++){
    		if (value <= range * (1.0 / rainbow.length) * (i+1))
    			return rainbow[i];
    	}
    	return rainbow[rainbow.length - 1];
	}
}
