

public interface IScanner {

    /**
	 * Scans the output of the job for a particular string, behaviour is very flexible; 
	 * see arguments below:
	 * 
	 * @param job - the job to scan
	 * @param dd - if declared, only this dd will be scanned; set to 'null' to scan all output
	 * @param searchString - the string or pattern to search for
	 * @param failString - if declared the scan will fail if this string or pattern is found, if null then the argument is ignored
	 * @param count - the number of instances of the string/pattern to match
	 * @param exact - if true the scan will pass if only the exact number of matches specified in count are found (otherwise it passes if at least that number are found)
	 * @param regex - if true the searchString and failString will be treated as regex patterns (java style)
	 * @return true if all conditions are met, false otherwise
	 * @throws HelperException
	 */
	public boolean scanJob(IJob job, String dd, String searchString, String failString, int count, boolean exact, boolean regex) throws HelperException;
	
	/**
	 * Equivalent to calling scanJob(job, null, searchString, null, 1, false, false)
	 * Searches the entire output of the job and returns true if at least one instance of the
	 * literal searchString is found.
	 * 
	 * @param job
	 * @param searchString
	 * @return
	 * @throws HelperException
	 */
	public boolean scanJob(IJob job, String searchString) throws HelperException;

	public String scanJobForMatch(IJob job, String searchString, int occurence) throws HelperException;

	/**
	 * Scans the output of the non-ascii uss file for a particular string, behaviour is very flexible; 
	 * see arguments below:
	 * 
	 * @param filePath - path of the file to search
	 * @param searchString - the string or pattern to search for
	 * @param failString - if declared the scan will fail if this string or pattern is found, if null then the argument is ignored
	 * @param count - the number of instances of the string/pattern to match
	 * @param exact - if true the scan will pass if only the exact number of matches specified in count are found (otherwise it passes if at least that number are found)
	 * @param regex - if true the searchString and failString will be treated as regex patterns (java style)
	 * @return true if all conditions are met, false otherwise
	 * @throws HelperException
	 */
	public boolean scanFileUSS(String filePath, String searchString, String failString, int count, boolean exact, boolean regex) throws HelperException;

	/**
	 * Equivalent to calling scanFileUSS(filePath, searchString, null, 1, false, false)
	 * Searches the file and returns true if at least one instance of the
	 * literal searchString is found.
	 * 
	 * @param filePath
	 * @param searchString
	 * @return
	 * @throws HelperException
	 */
	public boolean scanFileUSS(String filePath, String searchString) throws HelperException;
	
	public String scanFileUSSForMatch(String filePath, String searchString, int occurence) throws HelperException;
	
	/**
	 * Scans the output of the ascii uss file for a particular string, behaviour is very flexible; 
	 * see arguments below:
	 * 
	 * @param filePath - path of the file to search
	 * @param searchString - the string or pattern to search for
	 * @param failString - if declared the scan will fail if this string or pattern is found, if null then the argument is ignored
	 * @param count - the number of instances of the string/pattern to match
	 * @param exact - if true the scan will pass if only the exact number of matches specified in count are found (otherwise it passes if at least that number are found)
	 * @param regex - if true the searchString and failString will be treated as regex patterns (java style)
	 * @return true if all conditions are met, false otherwise
	 * @throws HelperException
	 */
	public boolean scanFileUSSAscii(String filePath, String searchString, String failString, int count, boolean exact, boolean regex) throws HelperException;

	/**
	 * Equivalent to calling scanFileUSSAscii(filePath, searchString, null, 1, false, false)
	 * Searches the file and returns true if at least one instance of the
	 * literal searchString is found.
	 * 
	 * @param filePath
	 * @param searchString
	 * @return
	 * @throws HelperException
	 */
	public boolean scanFileUSSAscii(String filePath, String searchString) throws HelperException;

	public String scanFileUSSAsciiForMatch(String filePath, String searchString, int occurence) throws HelperException;

	/**
	 * Scans the output of the dataset for a particular string, behaviour is very flexible; 
	 * see arguments below:
	 * 
	 * @param dataset - dataset to search
	 * @param searchString - the string or pattern to search for
	 * @param failString - if declared the scan will fail if this string or pattern is found, if null then the argument is ignored
	 * @param count - the number of instances of the string/pattern to match
	 * @param exact - if true the scan will pass if only the exact number of matches specified in count are found (otherwise it passes if at least that number are found)
	 * @param regex - if true the searchString and failString will be treated as regex patterns (java style)
	 * @return true if all conditions are met, false otherwise
	 * @throws HelperException
	 */
	public boolean scanDataset(String datasetname, String searchString, String failString, int count, boolean exact, boolean regex) throws HelperException;

	/**
	 * Equivalent to calling scanFileUSSAscii(filePath, searchString, null, 1, false, false)
	 * Searches the file and returns true if at least one instance of the
	 * literal searchString is found.
	 * 
	 * @param dataset
	 * @param searchString
	 * @return
	 * @throws HelperException
	 */
	public boolean scanDataset(String datasetname, String searchString) throws HelperException;
	
	/**
	 * Scans a dataset for a string which matches the given regular expression. Will return the nth occurence of a match as specified by occurrence
	 * 
	 * @param datasetname - dataset to scan
	 * @param regex - expression to match
	 * @return - matched string
	 * @throws HelperException 
	 */
	public String scanDatasetForMatch(String datasetname, String regex, int occurence) throws HelperException;
    
}