       IDENTIFICATION DIVISION.
       PROGRAM-ID. APITEST.
       ENVIRONMENT DIVISION.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 BASIC-TEST-RECORD.
       02 FIRST-STRING       PIC X(10) VALUE SPACES.
       LINKAGE SECTION.
       01 DFHCOMMAREA.
       02 FRSTRING           PIC X(10).
       PROCEDURE DIVISION.
           MOVE DFHCOMMAREA TO BASIC-TEST-RECORD.
           MOVE FUNCTION UPPER-CASE(FIRST-STRING) TO FRSTRING.
           EXEC CICS RETURN END-EXEC.