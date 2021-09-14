package reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class Junit3TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;
        Method[] methods = clazz.getMethods();

        for (Method m : methods) {
            if (m.getName().startsWith("test")) {
                m.invoke(clazz.getDeclaredConstructor().newInstance());
            }
        }
    }
}
