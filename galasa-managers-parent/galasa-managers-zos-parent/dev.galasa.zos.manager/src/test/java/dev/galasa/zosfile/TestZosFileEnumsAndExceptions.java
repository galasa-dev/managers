/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dev.galasa.zosfile.IZosDataset.DSType;
import dev.galasa.zosfile.IZosDataset.DatasetDataType;
import dev.galasa.zosfile.IZosDataset.DatasetOrganization;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.IZosVSAMDataset.BWOOption;
import dev.galasa.zosfile.IZosVSAMDataset.DatasetOrganisation;
import dev.galasa.zosfile.IZosVSAMDataset.EraseOption;
import dev.galasa.zosfile.IZosVSAMDataset.FRLogOption;
import dev.galasa.zosfile.IZosVSAMDataset.LogOption;
import dev.galasa.zosfile.IZosVSAMDataset.RecatalogOption;
import dev.galasa.zosfile.IZosVSAMDataset.ReuseOption;
import dev.galasa.zosfile.IZosVSAMDataset.SpanOption;
import dev.galasa.zosfile.IZosVSAMDataset.SpeedRecoveryOption;
import dev.galasa.zosfile.IZosVSAMDataset.VSAMSpaceUnit;
import dev.galasa.zosfile.IZosVSAMDataset.WriteCheckOption;

public class TestZosFileEnumsAndExceptions {
    
    private static final String EXCEPTION_MESSAGE = "exception-message";
    
    private static final String EXCEPTION_CAUSE = "exception-cause";
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private Matcher<? extends Throwable> cause = new BaseMatcher<Throwable>() {

        @Override
        public boolean matches(Object item) {
            return item.getClass().equals(Exception.class);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("\"" + EXCEPTION_CAUSE + "\"");
        }
    };
    
    @Test
    public void testDSType() {
        Assert.assertEquals("Problem with DSType", "HFS", DSType.HFS.toString());
        Assert.assertEquals("Problem with DSType", "PDS", DSType.PDS.toString());
        Assert.assertEquals("Problem with DSType", "PDSE", DSType.PDSE.toString());
        Assert.assertEquals("Problem with DSType", "LARGE", DSType.LARGE.toString());
        Assert.assertEquals("Problem with DSType", "BASIC", DSType.BASIC.toString());
        Assert.assertEquals("Problem with DSType", "EXTREQ", DSType.EXTREQ.toString());
        Assert.assertEquals("Problem with DSType", "EXTPREF", DSType.EXTPREF.toString());
        
        Assert.assertEquals("Problem with DSType", DSType.HFS, DSType.valueOfLabel("HFS"));
        Assert.assertEquals("Problem with DSType", DSType.PDS, DSType.valueOfLabel("PDS"));
        Assert.assertEquals("Problem with DSType", DSType.PDSE, DSType.valueOfLabel("PDSE"));
        Assert.assertEquals("Problem with DSType", DSType.LARGE, DSType.valueOfLabel("LARGE"));
        Assert.assertEquals("Problem with DSType", DSType.BASIC, DSType.valueOfLabel("BASIC"));
        Assert.assertEquals("Problem with DSType", DSType.EXTREQ, DSType.valueOfLabel("EXTREQ"));
        Assert.assertEquals("Problem with DSType", DSType.EXTPREF, DSType.valueOfLabel("EXTPREF"));
        Assert.assertNull("Problem with DSType", DSType.valueOfLabel("INVALID"));
    }
    
    @Test
    public void testRecordFormat() {
        Assert.assertEquals("Problem with RecordFormat", "B", RecordFormat.BLOCK.toString());
        Assert.assertEquals("Problem with RecordFormat", "F", RecordFormat.FIXED.toString());
        Assert.assertEquals("Problem with RecordFormat", "FB", RecordFormat.FIXED_BLOCKED.toString());
        Assert.assertEquals("Problem with RecordFormat", "V", RecordFormat.VARIABLE.toString());
        Assert.assertEquals("Problem with RecordFormat", "VB", RecordFormat.VARIABLE_BLOCKED.toString());
        Assert.assertEquals("Problem with RecordFormat", "U", RecordFormat.UNDEFINED.toString());

        Assert.assertEquals("Problem with RecordFormat", RecordFormat.BLOCK, RecordFormat.valueOfLabel("B"));
        Assert.assertEquals("Problem with RecordFormat", RecordFormat.FIXED, RecordFormat.valueOfLabel("F"));
        Assert.assertEquals("Problem with RecordFormat", RecordFormat.FIXED_BLOCKED, RecordFormat.valueOfLabel("FB"));
        Assert.assertEquals("Problem with RecordFormat", RecordFormat.VARIABLE, RecordFormat.valueOfLabel("V"));
        Assert.assertEquals("Problem with RecordFormat", RecordFormat.VARIABLE_BLOCKED, RecordFormat.valueOfLabel("VB"));
        Assert.assertEquals("Problem with RecordFormat", RecordFormat.UNDEFINED, RecordFormat.valueOfLabel("U"));
        Assert.assertNull("Problem with RecordFormat", RecordFormat.valueOfLabel("INVALID"));
    }
    
    @Test
    public void testDatasetOrganization() {
        Assert.assertEquals("Problem with DatasetOrganization", "PO", DatasetOrganization.PARTITIONED.toString());
        Assert.assertEquals("Problem with DatasetOrganization", "PS", DatasetOrganization.SEQUENTIAL.toString());

        Assert.assertEquals("Problem with DatasetOrganization", DatasetOrganization.PARTITIONED, DatasetOrganization.valueOfLabel("PO"));
        Assert.assertEquals("Problem with DatasetOrganization", DatasetOrganization.SEQUENTIAL, DatasetOrganization.valueOfLabel("PS"));
        Assert.assertNull("Problem with DatasetOrganization", DatasetOrganization.valueOfLabel("INVALID"));
    }
    
    @Test
    public void testSpaceUnit() {
        Assert.assertEquals("Problem with SpaceUnit", "TRK", SpaceUnit.TRACKS.toString());
        Assert.assertEquals("Problem with SpaceUnit", "CYL", SpaceUnit.CYLINDERS.toString());

        Assert.assertEquals("Problem with SpaceUnit", SpaceUnit.TRACKS, SpaceUnit.valueOfLabel("TRK"));
        Assert.assertEquals("Problem with SpaceUnit", SpaceUnit.CYLINDERS, SpaceUnit.valueOfLabel("CYL"));
        Assert.assertNull("Problem with SpaceUnit", SpaceUnit.valueOfLabel("INVALID"));
    }
    
    @Test
    public void testDatasetDataType() {
        Assert.assertEquals("Problem with DatasetDataType", "text", DatasetDataType.TEXT.toString());
        Assert.assertEquals("Problem with DatasetDataType", "binary", DatasetDataType.BINARY.toString());
        Assert.assertEquals("Problem with DatasetDataType", "record", DatasetDataType.RECORD.toString());

        Assert.assertEquals("Problem with DatasetDataType", DatasetDataType.TEXT, DatasetDataType.valueOfLabel("text"));
        Assert.assertEquals("Problem with DatasetDataType", DatasetDataType.BINARY, DatasetDataType.valueOfLabel("binary"));
        Assert.assertEquals("Problem with DatasetDataType", DatasetDataType.RECORD, DatasetDataType.valueOfLabel("record"));
        Assert.assertNull("Problem with DatasetDataType", DatasetDataType.valueOfLabel("INVALID"));
    }
    
    @Test
    public void testBWOOption() {
        Assert.assertEquals("Problem with BWOOption", "TYPECICS", BWOOption.TYPECICS.toString());
        Assert.assertEquals("Problem with BWOOption", "TYPEIMS", BWOOption.TYPEIMS.toString());
        Assert.assertEquals("Problem with BWOOption", "NO", BWOOption.NO.toString());
    }
    
    @Test
    public void testEraseOption() {
        Assert.assertEquals("Problem with EraseOption", "ERASE", EraseOption.ERASE.toString());
        Assert.assertEquals("Problem with EraseOption", "NOERASE", EraseOption.NOERASE.toString());
    }
    
    @Test
    public void testDatasetOrganisation() {
        Assert.assertEquals("Problem with DatasetOrganisation", "INDEXED", DatasetOrganisation.INDEXED.toString());
        Assert.assertEquals("Problem with DatasetOrganisation", "LINEAR", DatasetOrganisation.LINEAR.toString());
        Assert.assertEquals("Problem with DatasetOrganisation", "NONINDEXED", DatasetOrganisation.NONINDEXED.toString());
        Assert.assertEquals("Problem with DatasetOrganisation", "NUMBERED", DatasetOrganisation.NUMBERED.toString());
    }
    
    @Test
    public void testFRLogOption() {
        Assert.assertEquals("Problem with FRLogOption", "NONE", FRLogOption.NONE.toString());
        Assert.assertEquals("Problem with FRLogOption", "REDO", FRLogOption.REDO.toString());
    }
    
    @Test
    public void testLogOption() {
        Assert.assertEquals("Problem with LogOption", "NONE", LogOption.NONE.toString());
        Assert.assertEquals("Problem with LogOption", "UNDO", LogOption.UNDO.toString());
        Assert.assertEquals("Problem with LogOption", "ALL", LogOption.ALL.toString());
    }
    
    @Test
    public void testRecatalogOption() {
        Assert.assertEquals("Problem with RecatalogOption", "RECATALOG", RecatalogOption.RECATALOG.toString());
        Assert.assertEquals("Problem with RecatalogOption", "NORECATALOG", RecatalogOption.NORECATALOG.toString());
    }
    
    @Test
    public void testReuseOption() {
        Assert.assertEquals("Problem with ReuseOption", "REUSE", ReuseOption.REUSE.toString());
        Assert.assertEquals("Problem with ReuseOption", "NOREUSE", ReuseOption.NOREUSE.toString());
    }
    
    @Test
    public void testVSAMSpaceUnit() {
        Assert.assertEquals("Problem with VSAMSpaceUnit", "TRACKS", VSAMSpaceUnit.TRACKS.toString());
        Assert.assertEquals("Problem with VSAMSpaceUnit", "CYLINDERS", VSAMSpaceUnit.CYLINDERS.toString());
        Assert.assertEquals("Problem with VSAMSpaceUnit", "KILOBYTES", VSAMSpaceUnit.KILOBYTES.toString());
        Assert.assertEquals("Problem with VSAMSpaceUnit", "MEGABYTES", VSAMSpaceUnit.MEGABYTES.toString());
        Assert.assertEquals("Problem with VSAMSpaceUnit", "RECORDS", VSAMSpaceUnit.RECORDS.toString());
    }
    
    @Test
    public void testSpanOption() {
        Assert.assertEquals("Problem with SpanOption", "SPANNED", SpanOption.SPANNED.toString());
        Assert.assertEquals("Problem with SpanOption", "NONSPANNED", SpanOption.NONSPANNED.toString());
    }
    
    @Test
    public void testSpeedRecoveryOption() {
        Assert.assertEquals("Problem with SpeedRecoveryOption", "SPEED", SpeedRecoveryOption.SPEED.toString());
        Assert.assertEquals("Problem with SpeedRecoveryOption", "RECOVERY", SpeedRecoveryOption.RECOVERY.toString());
    }
    
    @Test
    public void testWriteCheckOption() {
        Assert.assertEquals("Problem with WriteCheckOption", "WRITECHECK", WriteCheckOption.WRITECHECK.toString());
        Assert.assertEquals("Problem with WriteCheckOption", "NOWRITECHECK", WriteCheckOption.NOWRITECHECK.toString());
    }
    
    @Test
    public void testZosDatasetException1() throws ZosDatasetException {
        exceptionRule.expect(ZosDatasetException.class);
        throw new ZosDatasetException();
    }
    
    @Test
    public void testZosDatasetException2() throws ZosDatasetException {
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosDatasetException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosDatasetException3() throws ZosDatasetException {
        exceptionRule.expect(ZosDatasetException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosDatasetException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosDatasetException4() throws ZosDatasetException {
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosDatasetException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosDatasetException5() throws ZosDatasetException {
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosDatasetException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
    
    @Test
    public void testZosUNIXFileException1() throws ZosUNIXFileException {
        exceptionRule.expect(ZosUNIXFileException.class);
        throw new ZosUNIXFileException();
    }
    
    @Test
    public void testZosUNIXFileException2() throws ZosUNIXFileException {
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosUNIXFileException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosUNIXFileException3() throws ZosUNIXFileException {
        exceptionRule.expect(ZosUNIXFileException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosUNIXFileException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosUNIXFileException4() throws ZosUNIXFileException {
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosUNIXFileException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosUNIXFileException5() throws ZosUNIXFileException {
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosUNIXFileException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
    
    @Test
    public void testZosVSAMDatasetException1() throws ZosVSAMDatasetException {
        exceptionRule.expect(ZosVSAMDatasetException.class);
        throw new ZosVSAMDatasetException();
    }
    
    @Test
    public void testZosVSAMDatasetException2() throws ZosVSAMDatasetException {
        exceptionRule.expect(ZosVSAMDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosVSAMDatasetException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosVSAMDatasetException3() throws ZosVSAMDatasetException {
        exceptionRule.expect(ZosVSAMDatasetException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosVSAMDatasetException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosVSAMDatasetException4() throws ZosVSAMDatasetException {
        exceptionRule.expect(ZosVSAMDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosVSAMDatasetException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosVSAMDatasetException5() throws ZosVSAMDatasetException {
        exceptionRule.expect(ZosVSAMDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosVSAMDatasetException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
    
    @Test
    public void testZosFileManagerException1() throws ZosFileManagerException {
        exceptionRule.expect(ZosFileManagerException.class);
        throw new ZosFileManagerException();
    }
    
    @Test
    public void testZosFileManagerException2() throws ZosFileManagerException {
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosFileManagerException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosFileManagerException3() throws ZosFileManagerException {
        exceptionRule.expect(ZosFileManagerException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosFileManagerException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosFileManagerException4() throws ZosFileManagerException {
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosFileManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosFileManagerException5() throws ZosFileManagerException {
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosFileManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
}
