package org.example;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringStart {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("snio.xml");
        context.start();
    }

}
