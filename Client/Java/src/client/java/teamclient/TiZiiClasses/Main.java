package client.java.teamclient.TiZiiClasses;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Main {
    public static void main(String[] args) throws ScriptException {
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("js");
        System.out.println(((Double) scriptEngine.eval("(2*5)")).intValue());
    }
}
