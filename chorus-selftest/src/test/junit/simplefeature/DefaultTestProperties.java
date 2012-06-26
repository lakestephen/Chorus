package simplefeature;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 26/06/12
 * Time: 08:43
 *
 * Standard set of properties for self-testing
 */
public class DefaultTestProperties extends Properties {

    public DefaultTestProperties() {
        //test output at log level info using chorus built in log provider
        put("chorusLogProvider", "org.chorusbdd.chorus.util.logging.StandardErrLogProvider");
        put("chorusLogLevel", "info");
    }
}
