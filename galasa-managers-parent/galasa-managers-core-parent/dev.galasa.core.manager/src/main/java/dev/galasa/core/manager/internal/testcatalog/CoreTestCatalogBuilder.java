/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.core.manager.internal.testcatalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.Tags;
import dev.galasa.framework.spi.ITestCatalogBuilder;
import dev.galasa.framework.spi.TestCatalogBuilder;

@TestCatalogBuilder
public class CoreTestCatalogBuilder implements ITestCatalogBuilder {

	@Override
	public void appendTestCatalog(JsonObject jsonRoot, JsonObject jsonTestClass, Class<?> testClass) {
		Tags annotationTag = testClass.getAnnotation(Tags.class); 
		if (annotationTag != null) {
			addTagsToTestCatalog(jsonRoot, jsonTestClass, annotationTag);
		}
	}

	private void addTagsToTestCatalog(JsonObject jsonRoot, JsonObject jsonTestClass, Tags annotationTag) {
		if (annotationTag == null || annotationTag.value().length < 1) {
			return;
		}
		
		// Check to see if the root Tags object exists
		JsonObject rootTag = jsonRoot.getAsJsonObject("tags");
		if (rootTag == null) {
			rootTag = new JsonObject();
			jsonRoot.add("tags", rootTag);
		}
		
		
		for(String tag : annotationTag.value() ) {
			if (tag == null) {
				continue;
			}
			
			tag = tag.trim();
			if (tag.isEmpty()) {
				continue;
			}
			
			JsonArray jsonTag = rootTag.getAsJsonArray(tag);
			if (jsonTag == null) {
				jsonTag = new JsonArray();
				rootTag.add(tag, jsonTag);
			}
			
			String bundle = jsonTestClass.get("bundle").getAsString();
			String name   = jsonTestClass.get("name").getAsString();
			
			jsonTag.add(bundle + "/" + name);;
		}
		
		
	}

	@Override
	public void appendTestCatalogForSharedEnvironment(JsonObject jsonSharedEnvironmentClass, Class<?> sharedEnvironmentClass) {
		// Nothing to build for shared environments
	}

}
