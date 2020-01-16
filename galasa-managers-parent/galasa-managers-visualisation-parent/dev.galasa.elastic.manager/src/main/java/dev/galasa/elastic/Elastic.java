package dev.galasa.elastic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import dev.galasa.elastic.internal.ElasticManagerField;

@Retention(RetentionPolicy.RUNTIME)
@ElasticManagerField
public @interface Elastic {

}