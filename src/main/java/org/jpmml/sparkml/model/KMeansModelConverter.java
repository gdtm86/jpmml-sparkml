/*
 * Copyright (c) 2016 Villu Ruusmann
 *
 * This file is part of JPMML-SparkML
 *
 * JPMML-SparkML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SparkML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SparkML.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.sparkml.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.primitives.Doubles;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.dmg.pmml.Array;
import org.dmg.pmml.Cluster;
import org.dmg.pmml.ClusteringField;
import org.dmg.pmml.ClusteringModel;
import org.dmg.pmml.CompareFunctionType;
import org.dmg.pmml.ComparisonMeasure;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Output;
import org.dmg.pmml.SquaredEuclidean;
import org.jpmml.converter.ClusteringModelUtil;
import org.jpmml.converter.Feature;
import org.jpmml.converter.ModelUtil;
import org.jpmml.converter.PMMLUtil;
import org.jpmml.converter.Schema;
import org.jpmml.sparkml.FeatureMapper;
import org.jpmml.sparkml.ModelConverter;

public class KMeansModelConverter extends ModelConverter<KMeansModel> {

	public KMeansModelConverter(KMeansModel model){
		super(model);
	}

	@Override
	public List<Feature> encodeFeatures(FeatureMapper featureMapper){
		KMeansModel model = getTransformer();

		// XXX
		return Collections.emptyList();
	}

	@Override
	public ClusteringModel encodeModel(Schema schema){
		KMeansModel model = getTransformer();

		List<Cluster> clusters = new ArrayList<>();

		Vector[] clusterCenters = model.clusterCenters();
		for(int i = 0; i < clusterCenters.length; i++){
			Vector clusterCenter = clusterCenters[i];

			Array array = PMMLUtil.createRealArray(Doubles.asList(clusterCenter.toArray()));

			Cluster cluster = new Cluster()
				.setId(String.valueOf(i))
				.setArray(array);

			clusters.add(cluster);
		}

		List<Feature> features = schema.getFeatures();

		List<ClusteringField> clusteringFields = ClusteringModelUtil.createClusteringFields(features);

		ComparisonMeasure comparisonMeasure = new ComparisonMeasure(ComparisonMeasure.Kind.DISTANCE)
			.setCompareFunction(CompareFunctionType.ABS_DIFF)
			.setMeasure(new SquaredEuclidean());

		MiningSchema miningSchema = ModelUtil.createMiningSchema(null, schema.getActiveFields());

		Output output = ClusteringModelUtil.createOutput(FieldName.create("cluster"), Collections.<Cluster>emptyList());

		ClusteringModel clusteringModel = new ClusteringModel(MiningFunctionType.CLUSTERING, ClusteringModel.ModelClass.CENTER_BASED, clusters.size(), miningSchema, comparisonMeasure, clusteringFields, clusters)
			.setOutput(output);

		return clusteringModel;
	}
}