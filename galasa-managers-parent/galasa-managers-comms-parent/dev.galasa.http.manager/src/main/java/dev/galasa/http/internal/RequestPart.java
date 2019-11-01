/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.http.internal;

import org.apache.http.entity.mime.content.ContentBody;

public interface RequestPart {

    ContentBody getBody();

    String getType();
}
