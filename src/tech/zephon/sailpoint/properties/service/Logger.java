
package tech.zephon.sailpoint.properties.service;

/**
 *
 * if you need a logger add one. this is for testing.
 */
public class Logger {
    
    private String className;
    
    public Logger(String className)
    {
        this.className = className;
    }
    
    public Logger()
    {
        
    }
    
    public static Logger getLogger(String className)
    {
        return new Logger(className);
    }
    
    public static Logger getLogger(Class clazz)
    {
        return new Logger(clazz.getName());
    }
    
    public void error(String message,Throwable ex)
    {
        System.out.println(this.className + "-ERROR- " + message);
        ex.printStackTrace();
    }
    
    public void error(String message)
    {
        System.out.println(this.className + "-ERROR- " + message);
    }
    
    public void info(String message)
    {
        System.out.println(this.className + "-INFO- " + message);
    }
    
    public void debug(String message)
    {
        System.out.println(this.className + "-DEBUG- " + message);
    }
    
}
