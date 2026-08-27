package com.andrefdias.dailynote.ui.screens.historico

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import com.andrefdias.dailynote.domain.model.MapOccurrence
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

import com.andrefdias.dailynote.domain.model.VisualizationMode

@Composable
fun MapLibreMapTab(ocorrencias: List<MapOccurrence>) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var mapType by remember { mutableStateOf("Carto") }
    var visMode by remember { mutableStateOf(VisualizationMode.CLUSTERS) }
    
    var showLayersMenu by remember { mutableStateOf(false) }
    
    // Measurement states
    var isMeasuring by remember { mutableStateOf(false) }
    val measurePoints = remember { mutableStateListOf<LatLng>() }
    var totalDistance by remember { mutableStateOf(0.0) }
    var totalArea by remember { mutableStateOf(0.0) }
    
    // We use a MapLibreMap reference to update measurement layer interactively
    var currentMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var lastAppliedMapType by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapLibre.getInstance(ctx)
                MapView(ctx).apply {
                    getMapAsync { mapLibreMap ->
                        currentMap = mapLibreMap
                        
                        mapLibreMap.addOnMapClickListener { point ->
                            if (isMeasuring) {
                                measurePoints.add(point)
                                updateMeasurementLayer(mapLibreMap, measurePoints)
                                
                                if (measurePoints.size > 1) {
                                    val last = measurePoints[measurePoints.size - 2]
                                    totalDistance += calculateDistanceMeters(last, point)
                                }
                                if (measurePoints.size > 2) {
                                    totalArea = calculatePolygonArea(measurePoints)
                                }
                                true
                            } else {
                                false
                            }
                        }

                        val styleBuilder = buildStyle(mapType)
                        mapLibreMap.setStyle(styleBuilder) { style ->
                            lastAppliedMapType = mapType
                            updateMarkers(mapLibreMap, style, ocorrencias, ctx, visMode)
                        }
                    }
                }
            },
            update = { mapView ->
                // READ STATE OUTSIDE ASYNC SO COMPOSE TRACKS RECOMPOSITION
                val currentMapType = mapType
                val currentVisMode = visMode
                
                mapView.getMapAsync { mapLibreMap ->
                    currentMap = mapLibreMap
                    val newStyle = buildStyle(currentMapType)
                    
                    if (mapLibreMap.style == null) {
                        mapLibreMap.setStyle(newStyle) { style ->
                            lastAppliedMapType = currentMapType
                            updateMarkers(mapLibreMap, style, ocorrencias, context, currentVisMode)
                            if (isMeasuring) updateMeasurementLayer(mapLibreMap, measurePoints)
                        }
                    } else {
                        if (lastAppliedMapType != currentMapType) {
                            mapLibreMap.setStyle(newStyle) { style ->
                                lastAppliedMapType = currentMapType
                                updateMarkers(mapLibreMap, style, ocorrencias, context, currentVisMode)
                                if (isMeasuring) updateMeasurementLayer(mapLibreMap, measurePoints)
                            }
                        } else {
                            mapLibreMap.style?.let { style ->
                                updateMarkers(mapLibreMap, style, ocorrencias, context, currentVisMode)
                                if (isMeasuring) updateMeasurementLayer(mapLibreMap, measurePoints)
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Measurement overlay UI
        if (isMeasuring) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color(0xDD1E2633L), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Modo Medição Ativo", color = Color.White, fontWeight = FontWeight.Bold)
                    if (measurePoints.size > 1) {
                        Text("Distância: ${String.format("%.2f", totalDistance / 1000)} km", color = Color.Cyan)
                    }
                    if (measurePoints.size > 2) {
                        Text("Área (aprox): ${String.format("%.2f", totalArea / 1000000)} km²", color = Color.Cyan)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = { 
                        measurePoints.clear()
                        totalDistance = 0.0
                        totalArea = 0.0
                        currentMap?.style?.let { updateMeasurementLayer(currentMap!!, measurePoints) }
                    }, modifier = Modifier.height(30.dp)) {
                        Text("Limpar", fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp))
                    }
                }
            }
        }

        // Floating Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Layers FAB
            Box {
                androidx.compose.material3.FloatingActionButton(
                    onClick = { showLayersMenu = !showLayersMenu },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Camadas"
                    )
                }
                androidx.compose.material3.DropdownMenu(
                    expanded = showLayersMenu,
                    onDismissRequest = { showLayersMenu = false },
                    modifier = Modifier.background(Color(0xEE1E2633L))
                ) {
                    androidx.compose.material3.Text("Terreno", color = Color.Gray, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                    listOf("Carto", "Dark", "Satélite").forEach { type ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(type, color = if (mapType == type) MaterialTheme.colorScheme.primary else Color.White) },
                            onClick = { mapType = type; showLayersMenu = false }
                        )
                    }
                    androidx.compose.material3.HorizontalDivider(color = Color.Gray)
                    androidx.compose.material3.Text("Visualização", color = Color.Gray, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                    VisualizationMode.values().forEach { mode ->
                        val label = when(mode) {
                            VisualizationMode.CLUSTERS -> "Grupos"
                            VisualizationMode.MARKERS -> "Pinos"
                            VisualizationMode.HEATMAP -> "Calor"
                        }
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(label, color = if (visMode == mode) MaterialTheme.colorScheme.tertiary else Color.White) },
                            onClick = { visMode = mode; showLayersMenu = false }
                        )
                    }
                }
            }
            
            // Measure FAB
            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    isMeasuring = !isMeasuring
                    if (!isMeasuring) {
                        measurePoints.clear()
                        totalDistance = 0.0
                        totalArea = 0.0
                        currentMap?.style?.let { updateMeasurementLayer(currentMap!!, measurePoints) }
                    }
                },
                containerColor = if (isMeasuring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (isMeasuring) Color.White else MaterialTheme.colorScheme.primary
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Medir"
                )
            }
        }
    }
}

private fun buildStyle(mapType: String): Style.Builder {
    return if (mapType == "Satélite") {
        Style.Builder()
            .withSource(org.maplibre.android.style.sources.RasterSource("esri", org.maplibre.android.style.sources.TileSet("tileset", "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"), 256))
            .withLayer(org.maplibre.android.style.layers.RasterLayer("esri-layer", "esri"))
    } else if (mapType == "Dark") {
        Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json")
    } else {
        Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json")
    }
}

private fun calculateDistanceMeters(p1: LatLng, p2: LatLng): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLon = Math.toRadians(p2.longitude - p1.longitude)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(p1.latitude)) * Math.cos(Math.toRadians(p2.latitude)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

private fun calculatePolygonArea(locations: List<LatLng>): Double {
    if (locations.size < 3) return 0.0
    val radius = 6371000.0
    var area = 0.0
    for (i in locations.indices) {
        val j = (i + 1) % locations.size
        val p1 = locations[i]
        val p2 = locations[j]
        area += Math.toRadians(p2.longitude - p1.longitude) * (2 + Math.sin(Math.toRadians(p1.latitude)) + Math.sin(Math.toRadians(p2.latitude)))
    }
    return Math.abs(area * radius * radius / 2.0)
}

private fun updateMeasurementLayer(map: MapLibreMap, points: List<LatLng>) {
    val style = map.style ?: return
    val sourceId = "measure-source"
    
    val featureCollection = if (points.isNotEmpty()) {
        val pts = points.map { org.maplibre.geojson.Point.fromLngLat(it.longitude, it.latitude) }
        val feature = if (pts.size == 1) {
            org.maplibre.geojson.Feature.fromGeometry(pts.first())
        } else {
            // Draw a polygon if > 2 points, otherwise a line
            if (pts.size > 2) {
                // Close the polygon
                val closedPts = pts.toMutableList().apply { add(pts.first()) }
                org.maplibre.geojson.Feature.fromGeometry(org.maplibre.geojson.Polygon.fromLngLats(listOf(closedPts)))
            } else {
                org.maplibre.geojson.Feature.fromGeometry(org.maplibre.geojson.LineString.fromLngLats(pts))
            }
        }
        org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf(feature))
    } else {
        org.maplibre.geojson.FeatureCollection.fromFeatures(emptyArray())
    }

    var source = style.getSourceAs<org.maplibre.android.style.sources.GeoJsonSource>(sourceId)
    if (source == null) {
        source = org.maplibre.android.style.sources.GeoJsonSource(sourceId, featureCollection)
        style.addSource(source)
    } else {
        source.setGeoJson(featureCollection)
    }

    if (style.getLayer("measure-line") == null) {
        val lineLayer = org.maplibre.android.style.layers.LineLayer("measure-line", sourceId)
        lineLayer.setProperties(
            org.maplibre.android.style.layers.PropertyFactory.lineWidth(3f),
            org.maplibre.android.style.layers.PropertyFactory.lineColor(android.graphics.Color.CYAN)
        )
        style.addLayer(lineLayer)
        
        val fillLayer = org.maplibre.android.style.layers.FillLayer("measure-fill", sourceId)
        fillLayer.setProperties(
            org.maplibre.android.style.layers.PropertyFactory.fillColor(android.graphics.Color.CYAN),
            org.maplibre.android.style.layers.PropertyFactory.fillOpacity(0.3f)
        )
        style.addLayerBelow(fillLayer, "measure-line")
        
        val circleLayer = org.maplibre.android.style.layers.CircleLayer("measure-points", sourceId)
        circleLayer.setProperties(
            org.maplibre.android.style.layers.PropertyFactory.circleRadius(5f),
            org.maplibre.android.style.layers.PropertyFactory.circleColor(android.graphics.Color.WHITE),
            org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth(1.5f),
            org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor(android.graphics.Color.CYAN)
        )
        style.addLayer(circleLayer)
    }
}

private fun updateMarkers(map: MapLibreMap, style: Style, ocorrencias: List<MapOccurrence>, context: Context, visMode: VisualizationMode) {
    if (ocorrencias.isEmpty()) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.5505, -46.6333), 7.0))
        return
    }

    val sourceId = "ocorrencias-source"
    
    val features = ocorrencias.filter { it.latitude != 0.0 && it.longitude != 0.0 }.map { occ ->
        val point = org.maplibre.geojson.Point.fromLngLat(occ.longitude, occ.latitude)
        val feature = org.maplibre.geojson.Feature.fromGeometry(point)
        feature.addStringProperty("id", occ.id)
        feature.addStringProperty("talao", occ.talao ?: "")
        feature.addStringProperty("nature", occ.nature ?: "")
        feature.addStringProperty("color", occ.color)
        feature
    }
    
    val featureCollection = org.maplibre.geojson.FeatureCollection.fromFeatures(features)

    // Remove existing layers and source to re-configure cleanly based on mode
    val layersToRemove = listOf("clusters", "cluster-count", "unclustered-points", "unclustered-text", "heatmap-layer")
    layersToRemove.forEach { if (style.getLayer(it) != null) style.removeLayer(it) }
    if (style.getSource(sourceId) != null) style.removeSource(sourceId)

    val isCluster = visMode == VisualizationMode.CLUSTERS
    
    val source = org.maplibre.android.style.sources.GeoJsonSource(
        sourceId,
        featureCollection,
        org.maplibre.android.style.sources.GeoJsonOptions()
            .withCluster(isCluster)
            .withClusterMaxZoom(14)
            .withClusterRadius(50)
    )
    style.addSource(source)

    if (visMode == VisualizationMode.CLUSTERS) {
        val clusterColors = arrayOf<org.maplibre.android.style.expressions.Expression>(
            org.maplibre.android.style.expressions.Expression.step(
                org.maplibre.android.style.expressions.Expression.get("point_count"),
                org.maplibre.android.style.expressions.Expression.color(android.graphics.Color.parseColor("#51bbd6")),
                org.maplibre.android.style.expressions.Expression.stop(10, org.maplibre.android.style.expressions.Expression.color(android.graphics.Color.parseColor("#f1f075"))),
                org.maplibre.android.style.expressions.Expression.stop(50, org.maplibre.android.style.expressions.Expression.color(android.graphics.Color.parseColor("#f28cb1")))
            )
        )

        val circles = org.maplibre.android.style.layers.CircleLayer("clusters", sourceId)
        circles.setProperties(
            org.maplibre.android.style.layers.PropertyFactory.circleColor(clusterColors[0]),
            org.maplibre.android.style.layers.PropertyFactory.circleRadius(
                org.maplibre.android.style.expressions.Expression.step(
                    org.maplibre.android.style.expressions.Expression.get("point_count"),
                    org.maplibre.android.style.expressions.Expression.literal(18f),
                    org.maplibre.android.style.expressions.Expression.stop(10, 25f),
                    org.maplibre.android.style.expressions.Expression.stop(50, 30f)
                )
            ),
            org.maplibre.android.style.layers.PropertyFactory.circleOpacity(0.8f)
        )
        circles.setFilter(org.maplibre.android.style.expressions.Expression.has("point_count"))
        style.addLayer(circles)

        val count = org.maplibre.android.style.layers.SymbolLayer("cluster-count", sourceId)
        count.setProperties(
            org.maplibre.android.style.layers.PropertyFactory.textField(org.maplibre.android.style.expressions.Expression.toString(org.maplibre.android.style.expressions.Expression.get("point_count"))),
            org.maplibre.android.style.layers.PropertyFactory.textSize(12f),
            org.maplibre.android.style.layers.PropertyFactory.textColor(android.graphics.Color.BLACK)
        )
        count.setFilter(org.maplibre.android.style.expressions.Expression.has("point_count"))
        style.addLayer(count)
    }

    if (visMode == VisualizationMode.CLUSTERS || visMode == VisualizationMode.MARKERS) {
        val unclustered = org.maplibre.android.style.layers.CircleLayer("unclustered-points", sourceId)
        unclustered.setProperties(
            org.maplibre.android.style.layers.PropertyFactory.circleColor(
                org.maplibre.android.style.expressions.Expression.toColor(org.maplibre.android.style.expressions.Expression.get("color"))
            ),
            org.maplibre.android.style.layers.PropertyFactory.circleRadius(8f),
            org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth(1.5f),
            org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor(android.graphics.Color.WHITE)
        )
        if (visMode == VisualizationMode.CLUSTERS) {
            unclustered.setFilter(org.maplibre.android.style.expressions.Expression.not(org.maplibre.android.style.expressions.Expression.has("point_count")))
        }
        style.addLayer(unclustered)
        
        val unclusteredText = org.maplibre.android.style.layers.SymbolLayer("unclustered-text", sourceId)
        unclusteredText.setProperties(
            org.maplibre.android.style.layers.PropertyFactory.textField(org.maplibre.android.style.expressions.Expression.get("talao")),
            org.maplibre.android.style.layers.PropertyFactory.textSize(10f),
            org.maplibre.android.style.layers.PropertyFactory.textOffset(arrayOf(0f, 1.5f)),
            org.maplibre.android.style.layers.PropertyFactory.textColor(android.graphics.Color.WHITE)
        )
        if (visMode == VisualizationMode.CLUSTERS) {
            unclusteredText.setFilter(org.maplibre.android.style.expressions.Expression.not(org.maplibre.android.style.expressions.Expression.has("point_count")))
        }
        style.addLayer(unclusteredText)
    }

    if (visMode == VisualizationMode.HEATMAP) {
        val heatmapLayer = org.maplibre.android.style.layers.HeatmapLayer("heatmap-layer", sourceId)
        heatmapLayer.setProperties(
            org.maplibre.android.style.layers.PropertyFactory.heatmapColor(
                org.maplibre.android.style.expressions.Expression.interpolate(
                    org.maplibre.android.style.expressions.Expression.linear(),
                    org.maplibre.android.style.expressions.Expression.heatmapDensity(),
                    org.maplibre.android.style.expressions.Expression.literal(0), org.maplibre.android.style.expressions.Expression.rgba(33, 102, 172, 0),
                    org.maplibre.android.style.expressions.Expression.literal(0.2), org.maplibre.android.style.expressions.Expression.rgb(103, 169, 207),
                    org.maplibre.android.style.expressions.Expression.literal(0.4), org.maplibre.android.style.expressions.Expression.rgb(209, 229, 240),
                    org.maplibre.android.style.expressions.Expression.literal(0.6), org.maplibre.android.style.expressions.Expression.rgb(253, 219, 199),
                    org.maplibre.android.style.expressions.Expression.literal(0.8), org.maplibre.android.style.expressions.Expression.rgb(239, 138, 98),
                    org.maplibre.android.style.expressions.Expression.literal(1), org.maplibre.android.style.expressions.Expression.rgb(178, 24, 43)
                )
            ),
            org.maplibre.android.style.layers.PropertyFactory.heatmapRadius(
                org.maplibre.android.style.expressions.Expression.interpolate(
                    org.maplibre.android.style.expressions.Expression.linear(),
                    org.maplibre.android.style.expressions.Expression.zoom(),
                    org.maplibre.android.style.expressions.Expression.literal(1), org.maplibre.android.style.expressions.Expression.literal(10),
                    org.maplibre.android.style.expressions.Expression.literal(15), org.maplibre.android.style.expressions.Expression.literal(30)
                )
            ),
            org.maplibre.android.style.layers.PropertyFactory.heatmapOpacity(0.8f),
            org.maplibre.android.style.layers.PropertyFactory.heatmapIntensity(
                org.maplibre.android.style.expressions.Expression.interpolate(
                    org.maplibre.android.style.expressions.Expression.linear(),
                    org.maplibre.android.style.expressions.Expression.zoom(),
                    org.maplibre.android.style.expressions.Expression.literal(1), org.maplibre.android.style.expressions.Expression.literal(1),
                    org.maplibre.android.style.expressions.Expression.literal(15), org.maplibre.android.style.expressions.Expression.literal(3)
                )
            )
        )
        style.addLayer(heatmapLayer)
    }

    if (features.isNotEmpty()) {
        try {
            val bounds = LatLngBounds.Builder()
            features.forEach { f ->
                val pt = f.geometry() as org.maplibre.geojson.Point
                bounds.include(LatLng(pt.latitude(), pt.longitude()))
            }
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
        } catch (e: Exception) {
            if (features.size > 0) {
                val pt = features.first().geometry() as org.maplibre.geojson.Point
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pt.latitude(), pt.longitude()), 12.0))
            }
        }
    }
}
