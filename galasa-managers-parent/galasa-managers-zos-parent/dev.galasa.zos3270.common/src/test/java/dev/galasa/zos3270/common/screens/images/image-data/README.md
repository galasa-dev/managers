These files are all test data files used by the TerminalImageTransformTest test.
The 3270 screens are rendered, and compared to these files.

If a test fails for any reason, the image rendered is placed in a temporary folder,
and a System.out.println inside the testcase tells us where it was placed.

Go look at the temporary file and visually compare it to what you expect.
If it's good, copy that file here.