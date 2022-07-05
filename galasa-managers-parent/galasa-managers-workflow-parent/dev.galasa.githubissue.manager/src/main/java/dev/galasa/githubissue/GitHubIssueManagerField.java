/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.githubissue;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Used to annotate annotations that are to be used for Test Class fields. To be
 * populated by the Manager.
 *
 * @author Jade Carino
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface GitHubIssueManagerField {

}
