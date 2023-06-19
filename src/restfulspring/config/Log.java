package restfulspring.config;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import restfulspring.Activator;

public class Log {

    private static final String PLUGIN_ID = Activator.PLUGIN_ID;

    private static ILog log = Activator.getDefault().getLog();

    // 记录普通信息
    public static void info(String message) {
        IStatus status = new Status(IStatus.INFO, PLUGIN_ID, message);
        log.log(status);
    }

    // 记录警告信息
    public static void warn(String message) {
        IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message);
        log.log(status);
    }

    // 记录错误信息
    public static void error(String message, Throwable exception) {
        IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, exception);
        log.log(status);
    }
    
    // 记录OK信息
    public static void ok(String message) {
        IStatus status = new Status(IStatus.OK, PLUGIN_ID, message);
        log.log(status);
    }
    
    // 记录cancel信息
    public static void cancel(String message) {
        IStatus status = new Status(IStatus.CANCEL, PLUGIN_ID, message);
        log.log(status);
    }
}
