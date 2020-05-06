/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.artifact.manager.ivt;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.ArtifactManager;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.Logger;

@Test
public class ArtifactManagerIVT {

    @Logger
    public Log              logger;

    @ArtifactManager
    public IArtifactManager artifacts;

    @BundleResources
    public IBundleResources resources;

    @Test
    public void checkManagerNotNull() throws Exception {
        if (artifacts == null) {
            throw new Exception("Artifact Manager instance was null");
        }
        if(resources == null) {
            throw new Exception("Bundle resources was null");
        }
    }

    @Test
    public void testRetrieveFileAsStringMethod() throws Exception {
        String textContent = resources.retrieveFileAsString("/resources/textFiles/test1.txt");
        logger.debug("Read the following from the file test1.txt: " + textContent);
        if (!textContent.trim().equals("Hello from Galasa")) {
            throw new Exception("Unable to read text file resources/textFiles/test1.txt");
        }
    }

    @Test
    public void testRetrieveSkeletonFileAsStringMethod() throws Exception {
        String textContent = resources.retrieveSkeletonFileAsString("/resources/skeletons/test1.skel", buildHashMap());
        logger.info("Receivied the following from the skeleton file: " + textContent);
        if (!textContent.trim().equals("The third parameter is ITEM NUMBER THREE")) {
            throw new Exception("received the wrong result from retrieving a skeleton file, received: " + textContent);
        }
    }

    @Test
    public void readTextFileArtifactManager() throws Exception, TestBundleResourceException, IOException {
        InputStream file = artifacts.getBundleResources(this.getClass()).retrieveFile("/resources/textFiles/test1.txt");
        String textContent = artifacts.getBundleResources(this.getClass()).streamAsString(file);
        logger.debug("Read the following from the file test1.txt: " + textContent);
        if (!textContent.trim().equals("Hello from Galasa")) {
            throw new Exception("Unable to read text file resources/textFiles/test1.txt");
        }
    }

    @Test
    public void readTextFileBundleResources() throws Exception, TestBundleResourceException, IOException {
        String textContent = resources.streamAsString(resources.retrieveFile("/resources/textFiles/test1.txt"));
        logger.debug("Read the following from the file test1.txt: " + textContent);
        if (!textContent.trim().equals("Hello from Galasa")) {
            throw new Exception("Unable to read text file resources/textFiles/test1.txt");
        }
    }

    private HashMap<String,Object> buildHashMap(){
        HashMap<String, Object> props = new HashMap<>();
        props.put("ITEM1", "THIS IS ITEM1");
        props.put("ITEM2", "SECOND ITEM");
        props.put("ITEM3", "ITEM NUMBER THREE");
        return props;
    }

    @Test
    public void readSkeletonBundleResources() throws TestBundleResourceException, Exception, IOException {
        String textContent = resources.streamAsString(resources.retrieveSkeletonFile("/resources/skeletons/test1.skel", buildHashMap()));

        logger.info("Receivied the following from the skeleton file: " + textContent);
        if (!textContent.trim().equals("The third parameter is ITEM NUMBER THREE")) {
            throw new Exception("received the wrong result from retrieving a skeleton file, received: " + textContent);
        }
    }

    @Test
    public void readSkeletonFileArtifactManager() throws TestBundleResourceException, Exception, IOException {
        InputStream is = artifacts.getBundleResources(this.getClass())
                .retrieveSkeletonFile("/resources/skeletons/test1.skel", buildHashMap());
        String textContent = artifacts.getBundleResources(this.getClass()).streamAsString(is);

        logger.info("Receivied the following from the skeleton file: " + textContent);
        if (!textContent.trim().equals("The third parameter is ITEM NUMBER THREE")) {
            throw new Exception("received the wrong result from retrieving a skeleton file, received: " + textContent);
        }
    }

    @Test
    public void readSkeletonFile2ArtifactManager() throws TestBundleResourceException, Exception, IOException {
        HashMap<String, Object> props = new HashMap<>();
        props.put("ITEM1", "THIS IS ITEM1");
        props.put("ITEM2", "SECOND ITEM");
        props.put("ITEM3", "ITEM NUMBER THREE");

        InputStream is = artifacts.getBundleResources(this.getClass())
                .retrieveSkeletonFile("/resources/skeletons/test2.skel", props);
        String textContent = artifacts.getBundleResources(this.getClass()).streamAsString(is);
        logger.info("Receivied the following from the skeleton file: " + textContent);

        if (!textContent.trim().contains("The third parameter is ITEM NUMBER THREE")) {
            throw new IOException("received the wrong result from the first line of the skeleton");
        }
        if (!textContent.trim().contains("The first item was \"THIS IS ITEM1\" and this is the second line")) {
            throw new Exception("received the wrong result from retrieving a skeleton file, received: " + textContent);
        }
    }
}
