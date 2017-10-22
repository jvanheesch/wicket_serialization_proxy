package com.mycompany;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.serialize.ISerializer;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class WicketApplication extends WebApplication {

    @Override
    public Class<? extends WebPage> getHomePage() {
        return HomePage.class;
    }

    @Override
    protected void init() {
        super.init();

        ISerializer serializer = this.getFrameworkSettings().getSerializer();
        this.getFrameworkSettings().setSerializer((ISerializer) Proxy.newProxyInstance(
                ISerializer.class.getClassLoader(),
                new Class<?>[]{ISerializer.class},
                (proxy, method, args) -> {
                    System.out.println("method \t\t\t: " + method.getName());
                    LocalDateTime start = LocalDateTime.now();
                    System.out.println("start \t\t\t: " + start);
                    Object result = method.invoke(serializer, args);
                    LocalDateTime end = LocalDateTime.now();
                    System.out.println("end \t\t\t: " + end);
                    // millisecond = 10^6 x nanosecond
                    System.out.println("duration in ms \t: " + ChronoUnit.NANOS.between(start, end) / 1000000.0);
                    System.out.println("-------------------------------------------------------------------------");
                    return result;
                }
        ));
    }
}
