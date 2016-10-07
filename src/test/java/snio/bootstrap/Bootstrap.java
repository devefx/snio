package snio.bootstrap;

import org.devefx.snio.LifecycleException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Bootstrap {

    public static void main(String[] args) throws LifecycleException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("snio.xml");
        context.start();
    }
}
