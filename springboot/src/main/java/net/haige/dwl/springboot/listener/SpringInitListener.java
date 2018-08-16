package net.haige.dwl.springboot.listener;

import javafx.application.Application;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SpringInitListener implements ServletContextListener {

    private static WebApplicationContext springContext;
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        springContext=WebApplicationContextUtils.getWebApplicationContext(servletContextEvent.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    public static org.springframework.context.ApplicationContext getApplicationContext(){
        return  springContext;
    }

}
