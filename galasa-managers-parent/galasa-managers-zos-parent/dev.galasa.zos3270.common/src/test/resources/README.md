These files are all test data files used by the TerminalImageTransformTest test.
The 3270 screens are rendered, and compared to these files.

If a test fails for any reason, the image rendered is placed in a temporary folder,
and a System.out.println inside the testcase tells us where it was placed.

Go look at the temporary file and visually compare it to what you expect.
If it's good, copy that file here.

Note: Recorded images on a mac are not the same as those of other platforms.
Each platform has slightly different fonts, so the images render slightly differently, 
taking up different amounts of spacing in the .png format.
As a result of this, we have folders for each platform, not all of which may be populated/supported.

