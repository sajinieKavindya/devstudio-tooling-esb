/*
 * Copyright 2016 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.developerstudio.datamapper.diagram.custom.model.transformers;

import static org.wso2.developerstudio.datamapper.diagram.custom.model.transformers.TransformerConstants.PROPERTY_NAME_TAG;
import static org.wso2.developerstudio.datamapper.diagram.custom.model.transformers.TransformerConstants.PROPERTY_SCOPE_TAG;
import static org.wso2.developerstudio.datamapper.diagram.custom.model.transformers.TransformerConstants.PROPERTY_TYPE_TAG;

import org.wso2.developerstudio.datamapper.DataMapperOperatorType;
import org.wso2.developerstudio.datamapper.diagram.custom.model.DMOperation;
import org.wso2.developerstudio.datamapper.diagram.custom.model.IModelTransformer;
import org.wso2.developerstudio.datamapper.impl.OperatorImpl;
import org.wso2.developerstudio.datamapper.impl.PropertiesImpl;

public class PropertiesModelTransformer implements IModelTransformer {

	@Override
	public DMOperation transform(OperatorImpl operator, int index) {
		if (operator instanceof PropertiesImpl) {
			PropertiesImpl propertyOperator = (PropertiesImpl) operator;
			DMOperation propertiesOperatorModel = new DMOperation(DataMapperOperatorType.PROPERTIES,
					operator.toString(), index);
			propertiesOperatorModel.addProperty(PROPERTY_NAME_TAG, propertyOperator.getName());
			propertiesOperatorModel.addProperty(PROPERTY_SCOPE_TAG, propertyOperator.getScope());
			return propertiesOperatorModel;
		}
		throw new IllegalArgumentException("PropertiesImpl operator expected. Found : " + operator.toString());
	}
}
