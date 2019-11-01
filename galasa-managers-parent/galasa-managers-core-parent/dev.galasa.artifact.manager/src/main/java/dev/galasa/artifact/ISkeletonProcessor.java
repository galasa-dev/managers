/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.artifact;

import java.io.InputStream;
import java.util.Map;

public interface ISkeletonProcessor {

    public class SkeletonType {

        public static final int PLUSPLUS = 0;
        public static final int VELOCITY = 1;
    }

    InputStream processSkeleton(InputStream skeleton, Map<String, Object> parameters) throws SkeletonProcessorException;

}
