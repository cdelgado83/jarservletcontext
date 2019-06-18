package com.misc;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;

import org.apache.catalina.WebResourceSet;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public ServletRegistrationBean<MyServlet> servletRegistrationBean() {
    return new ServletRegistrationBean<>(new MyServlet(), "/module/dispatch");
  }

  //
  // BELOW: the code to allow getServletContext().getResourceAsStream() to
  // succeed when package inside a jar using embedded tomcat
  // (else, is will return null!)
  //

  /**
   * TODO: in spring boot 2.2, this won't be needed anymore, as the customizer
   * bean below will be applied automatically, IIUC.
   * 
   * @see https://github.com/spring-projects/spring-boot/issues/15062
   */
  @Bean
  @ConditionalOnClass(name = { "org.apache.catalina.startup.Tomcat" })
  @Profile("customizetomcatctx")
  public WebServerFactoryCustomizer<TomcatServletWebServerFactory> connectorCustomizer() {
    return tomcat -> tomcat.addContextCustomizers(tomcatContextCustomizer());
  }

  /**
   * When packaged as an executable jar using Tomcat, we want
   * getServletContext() bound to /static
   */
  @Bean
  @ConditionalOnClass(name = { "org.apache.catalina.startup.Tomcat" })
  @Profile("customizetomcatctx")
  public TomcatContextCustomizer tomcatContextCustomizer() {
    return ctx -> ctx.setResources(new StandardRoot(ctx) {
      @Override
      protected WebResourceSet createMainResourceSet() {

        String c = "org.springframework.boot.loader.Launcher";
        CodeSource codeSource;
        try {
          codeSource = ClassLoader.getSystemClassLoader().loadClass(c).getProtectionDomain().getCodeSource();
        } catch (ClassNotFoundException e1) {
          // we are not packaged and are probably running in the IDE
          return new DirResourceSet(this, "/", "src/main/resources/static", "/");
        }

        // so, we are in a spring-boot executable jar.
        try {
          URI location = codeSource.getLocation().toURI();
          String path = location.getSchemeSpecificPart();
          File jar = new File(path);
          if (jar.isFile()) {
            return new JarResourceSet(this, "/", jar.getAbsolutePath(), "/BOOT-INF/classes/static");
          }
          throw new UnsupportedOperationException("Executable jar " + jar + " is not a file?");
        } catch (URISyntaxException e) {
          throw new UnsupportedOperationException("Can't parse executable JAR URI?", e);
        }

      }
    });
  }

}
