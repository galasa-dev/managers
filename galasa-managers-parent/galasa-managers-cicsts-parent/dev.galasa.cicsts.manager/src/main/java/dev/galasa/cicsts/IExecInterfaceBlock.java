/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

/**
 * Represents the fields in the CICS/TS EXEC Interface Block (EIB).
 *
 */
public interface IExecInterfaceBlock {
    
    /**
     * Returns a {@link String} representation of the EIBRESP field if available
     * @return
     */
    public String getResponse();
    
    /**
     * Returns the value of the EIBTIME field in the EIB.
     * @return
     */
    public int getEIBTIME();

    /**
     * Returns the value of the EIBDATE field in the EIB.
     * @return
     */
    public int getEIBDATE();

    /**
     * Returns the value of the EIBTRNID field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * @return
     */
    public String getEIBTRNID(boolean hex);

    /**
     * Returns the value of the EIBTASKN field in the EIB.
     * @return
     */
    public int getEIBTASKN();

    /**
     * Returns the value of the EIBTRMID field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * @return
     */
    public String getEIBTRMID(boolean hex);

    /**
     * Returns the value of the EIBCPOSN field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * @return
     */
    public int getEIBCPOSN();
    
    /**
     * Returns the value of the EIBCALEN field in the EIB.
     * @return
     */
    public int getEIBCALEN();
    
    /**
     * Returns the value of the EIBAID field in the EIB.
     * @return
     */
    public char getEIBAID();

    /**
     * Returns the value of the EIBFN field in the EIB.
     * @return
     */
    public char[] getEIBFN();

    /**
     * Returns the value of the EIBRCODE  field in the EIB.
     * @return
     */
    public char[] getEIBRCODE();

    /**
     * Returns the value of the EIBDS  field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * @return
     */
    public String getEIBDS(boolean hex);

    /**
     * Returns the value of the EIBREQID field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * @return
     */
    public String getEIBREQID(boolean hex);

    /**
     * Returns the value of the EIBRSRCE field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * @return
     */
    public String getEIBRSRCE(boolean hex);

    /**
     * Returns the value of the EIBSYNC field in the EIB.
     * @return
     */
    public char getEIBSYNC();

    /**
     * Returns the value of the EIBFREE field in the EIB.
     * @return
     */
    public char getEIBFREE();

    /**
     * Returns the value of the EIBRECV field in the EIB.
     * @return
     */
    public char getEIBRECV();
    
    /**
     * Returns the value of the EIBATT field in the EIB.
     * @return
     */
    public char getEIBATT();

    /**
     * Returns the value of the EIBEOC field in the EIB.
     * @return
     */
    public char getEIBEOC();

    /**
     * Returns the value of the  field in the EIB.
     * @return
     */
    public char getEIBFMH();

    /**
     * Returns the value of the EIBCOMPL field in the EIB.
     * @return
     */
    public char getEIBCOMPL();

    /**
     * Returns the value of the EIBSIG field in the EIB.
     * @return
     */
    public char getEIBSIG();

    /**
     * Returns the value of the EIBCONF field in the EIB.
     * @return
     */
    public char getEIBCONF();

    /**
     * Returns the value of the EIBERR field in the EIB.
     * @return
     */
    public char getEIBERR();

    /**
     * Returns the value of the EIBERRCD field in the EIB.
     * @return
     */
    public char[] getEIBERRCD();

    /**
     * Returns the value of the EIBSYNRB field in the EIB.
     * @return
     */
    public char getEIBSYNRB();

    /**
     * Returns the value of the EIBNODAT field in the EIB.
     * @return
     */
    public char getEIBNODAT();

    /**
     * Returns the value of the EIBRESP field in the EIB.
     * @return
     */
    public int getEIBRESP();

    /**
     * Returns the value of the EIBRESP2 field in the EIB.
     * @return
     */
    public int getEIBRESP2();

    /**
     * Returns the value of the EIBRLDBK field in the EIB.
     * @return
     */
    public char getEIBRLDBK();
}
