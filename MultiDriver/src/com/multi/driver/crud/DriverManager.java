package com.multi.driver.crud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * DriverManager class extends DriverFactory to store each pair in the map.
 * It is in charge of setting and closing connections.
 * Additionally, is the parent class for specific format driver behavior,
 * thus, it has abstract methods such as retrieveData, cloneDriver and.
 * The reason behind not having CRUD operations , is because depending on
 * the file format, different entries are required and, at the same time, it
 * is not its purpose to manipulate files, just to manage them by setting
 * and losing connectivity.
 * DriverManager uses generics, as each driver manages formats in a different way.
 */
public abstract class DriverManager<T> extends DriverFactory implements CRUDOperations{

	/**
	 * Both source and FileFormat are protected, as subclasses require access
	 * to them.
	 */
	protected String source;
	protected FileFormat format;
	
	//Both functional interfaces are useful to check  setConnection methods.
	private Function<String, Path> getPath= Paths::get;
	private Predicate<Path> pathExists = Files::exists;
	
	public DriverManager() {
		super();
	}
	
	public DriverManager(String source) {
		setConnection(source, FileFormat.determineFormat(source));
	}
	
	public DriverManager(String source, FileFormat format) {
		setConnection(source, format);
	}
	
	private void connectToSource(String source, FileFormat format) {
		if (!DRIVERS.containsKey(source)) { //If drivers do not have source, then it will put the connection
			try {
				this.format = format;
				this.source = source;
				DRIVERS.put(source, retrieveData(format));
			} catch (DriverException e) {
				System.err.println(DriverExceptionsCodes.FILE);
			}
		} else 
			System.err.println("Connection to " + source + " already exists.");
	}


	public void setConnection(String source) {
		Path path = Paths.get(source);
		if (!Files.exists(path)) System.err.println("The specified path " + source + " is nonexistent. Check the correct name.");
	    FileFormat format = FileFormat.determineFormat(source);
	    connectToSource(source, format);
	}

	public void setConnection(String source, FileFormat format) {
		Path path = Paths.get(source);
		if (!Files.exists(path)) System.err.println("The specified path " + source + " is nonexistent. Check the correct name.");
	    connectToSource(source, format); 
	}
	public void closeConnection(){
		if(DriverFactory.DRIVERS.containsKey(this.source)) {
			DriverFactory.DRIVERS.remove(this.source);
		}
		else System.err.println(DriverExceptionsCodes.PATH);
	}
	
	@SuppressWarnings("unchecked")
	public T getTable() {
		return (T) DriverFactory.DRIVERS.get(this.source);
	}
	
	/**
	 * Due to files having different structures, clone() calls to an abstract 
	 * method in which each driver class compares the content of one file to
	 * the other.
	 */
	@Override
	public Object clone() { 
		Path sourcePath = getPath.apply(this.source);
	    if (!pathExists.test(sourcePath)) {
	        System.err.println(DriverExceptionsCodes.FILE);
	        return null;
	    }

	    /** Generate a new file name
	     * (\\.[^.]+)?$ means:
	     * \\. = "."
	     * [^.]+ = whatever comes after the period
	     * )? = optional, there is no specific sequence
	     * $ = ensure the match happens at the end of the string
	     * _copy$1, ensures _copy is inserted at the end
	     */
	    String newFileName = sourcePath.getFileName().toString().replaceFirst("(\\.[^.]+)?$", "_copy$1");
	    Path newPath = sourcePath.getParent() != null
	        ? sourcePath.getParent().resolve(newFileName)
	        : Paths.get(newFileName);

	    // Copy file content
	    try {
	        Files.copy(sourcePath, newPath);
	    } catch (IOException e) {
	        System.err.println("Error copying file: " + e.getMessage());
	        return null;
	    }

	    // Delegate to subclass-specific cloning logic
	    return this.cloneDriver(newPath.toString());
	}
	
	protected void updateAndWrite(T table) {
	    DriverFactory.DRIVERS.replace(this.source, table);
	    write();
	}
	
	protected abstract T retrieveData(FileFormat format) throws DriverException;
	protected abstract DriverManager<T> cloneDriver(String newSource);
	protected abstract void write();
}