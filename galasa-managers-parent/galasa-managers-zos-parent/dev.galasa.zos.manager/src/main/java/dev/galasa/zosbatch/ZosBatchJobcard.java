package dev.galasa.zosbatch;

/**
 * Provides overrides for the Jobcard for jobs submitted via the Batch Manager.
 * 
 * No validation is performed on the values that are set
 * 
 * @author Michael Baylis
 *
 */
public class ZosBatchJobcard {
    
    public enum Typrun {
        COPY, HOLD, JCLHOLD, SCAN
    }
    
    private String inputClass;
    private String msgClass;
    private String msgLevel;
    private String region;
    private String memlimit;
    private Typrun typrun;
    private String userid;
    private String password;
    private String cond;
    private String account;
    private String progname;

    public ZosBatchJobcard() {}
    
    /**
     * @param inputClass value for CLASS=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setInputClass(String inputClass) {
        this.inputClass = inputClass;
        return this;
    }

    /**
     * @param msgClass value for MSGCLASS=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setMsgClass(String msgClass) {
        this.msgClass = msgClass;
        return this;
    }

    /**
     * @param msgLevel value for MSGLEVEL=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setMsgLevel(String msgLevel) {
        this.msgLevel = msgLevel;
        return this;
    }

    /**
     * @param region value for REGION=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * @param memlimit value for MEMLIMIT=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setMemlimit(String memlimit) {
        this.memlimit = memlimit;
        return this;
    }

    /**
     * @param typrun value for TYPRUN=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setTyprun(Typrun typrun) {
        this.typrun = typrun;
        return this;
    }

    /**
     * @param userid value for USERID=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setUserid(String userid) {
        this.userid = userid;
        return this;
    }

    /**
     * @param password value for PASSWORD=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * @param cond value for COND=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setCond(String cond) {
        this.cond = cond;
        return this;
    }

    /**
     * @param account value for account field on JOB
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setAccount(String account) {
        this.account = account;
        return this;
    }

    /**
     * @param progname value for programmer field on JOB
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setProgrammerName(String progname) {
        this.progname = progname;
        return this;
    }

    /**
     * @return CLASS= value
     */
    public String getInputClass() {
        return nulled(inputClass);
    }

    /**
     * @return MSGCLASS= value
     */
    public String getMsgClass() {
        return nulled(msgClass);
    }

    /**
     * @return MSGLEVEL= value
     */
    public String getMsgLevel() {
        return nulled(msgLevel);
    }

    /**
     * @return REGION= value
     */
    public String getRegion() {
        return nulled(region);
    }

    /**
     * @return MEMLIMIT= value
     */
    public String getMemlimit() {
        return nulled(memlimit);
    }

    /**
     * @return TYPRUN= value
     */
    public Typrun getTyprun() {
        return typrun;
    }

    /**
     * @return USERID= value
     */
    public String getUserid() {
        return nulled(userid);
    }

    /**
     * @return PASSWORD= value
     */
    public String getPassword() {
        return nulled(password);
    }

    /**
     * @return COND= value
     */
    public String getCond() {
        return nulled(cond);
    }

    /**
     * @return JOB account value
     */
    public String getAccount() {
        return nulled(account);
    }

    /**
     * @return JOB programmer value
     */
    public String getProgrammerName() {
        return nulled(progname);
    }
    
    protected String nulled(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }
        return value;
    }



}
