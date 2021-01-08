package dev.galasa.zosconsole.oeconsol.manager;
/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
/**
 * zOS Console Manager - oeconsol Internal Implementation
 * 
 * @galasa.manager zOS Console oeconsol
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested.
 * 
 * @galasa.description
 * 
 * This Manager is the internal implementation of the z/OS Console Manager using oeconsol. The z/OS MF Console Manager is used in conjunction with the z/OS UNIX Command Manager. The z/OS Manager provides the interface for the z/OS console function and pulls in the oeconsol Console Manager to provide the implementation of the interface. If your test needs to request a z/OS console instance, issue a console command or retrieve the console command, you can call the z/OS Manager in your test code and the z/OS Manager will call the oeconsol Console Manager to provide the implementation via the z/OS console function. Multiple z/OS console images can be requested by a test.
 * <p>
 * See the <a href="/docs/managers/zos-manager">zOS Manager</a> for details of the z/OS Console Annotations.
 */
