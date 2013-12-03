package com.ajjpj.asysmon.server.init;

import com.ajjpj.asysmon.server.config.ASysMonServerConfig;
import com.ajjpj.asysmon.server.config.ASysMonServerConfigBuilder;
import com.ajjpj.asysmon.server.eventbus.EventBus;
import com.ajjpj.asysmon.server.eventbus.EventBusImpl;
import com.ajjpj.asysmon.server.services.BufferingPersistenceProcessor;
import com.ajjpj.asysmon.server.services.impl.BufferingPersistenceProcessorImpl;
import com.ajjpj.asysmon.server.storage.ScalarDataDao;
import com.ajjpj.asysmon.server.storage.impl.ScalarDataDaoImpl;
import com.ajjpj.asysmon.server.upload.preprocess.InputProcessor;
import com.ajjpj.asysmon.server.upload.preprocess.impl.InputProcessorImpl;
import com.ajjpj.asysmon.server.upload.preprocess.SystemClockCorrector;
import com.ajjpj.asysmon.server.upload.preprocess.impl.SystemClockCorrectorNullImpl;
import com.ajjpj.asysmon.server.services.AdminService;
import com.ajjpj.asysmon.server.services.impl.AdminServiceImpl;
import com.ajjpj.asysmon.server.storage.MonitoredApplicationDao;
import com.ajjpj.asysmon.server.storage.ScalarMetaDataDao;
import com.ajjpj.asysmon.server.storage.impl.MongoDbProvider;
import com.ajjpj.asysmon.server.storage.impl.MonitoredApplicationDaoImpl;
import com.ajjpj.asysmon.server.storage.impl.ScalarMetaDataDaoImpl;
import com.google.inject.*;
import com.google.inject.spi.BindingScopingVisitor;
import com.mongodb.DB;

import java.lang.annotation.Annotation;


/**
 * This is the guice module
 *
 * @author arno
 */
public class ASysMonModule extends AbstractModule {
    @Override protected void configure() {
        bind(DB.class).toProvider(MongoDbProvider.class).in(Singleton.class);

        // DAOs
        bind(MonitoredApplicationDao.class).to(MonitoredApplicationDaoImpl.class);
        bind(ScalarMetaDataDao.class).to(ScalarMetaDataDaoImpl.class);
        bind(ScalarDataDao.class).to(ScalarDataDaoImpl.class);

        // services
        bind(AdminService.class).to(AdminServiceImpl.class);

        // event bus and processing
        bind(EventBus.class).to(EventBusImpl.class).asEagerSingleton();

        bind(BufferingPersistenceProcessor.class).to(BufferingPersistenceProcessorImpl.class);

        // upload
        bind(InputProcessor.class).to(InputProcessorImpl.class).in(Singleton.class);
        bind(SystemClockCorrector.class).to(SystemClockCorrectorNullImpl.class).in(Singleton.class); //TODO make this configurable
    }


    @Provides
    public ASysMonServerConfig getConfig() { //TODO move config to the database
        return new ASysMonServerConfigBuilder().build();
    }






    //TODO shutdown

//    static {
//        for(Binding<?> b: INJECTOR.getAllBindings().values()) {
//            final boolean isSingleton = b.acceptScopingVisitor(new BindingScopingVisitor<Boolean>() {
//                @Override public Boolean visitEagerSingleton() {
//                    System.out.println(1);
//                    return true;
//                }
//
//                @Override public Boolean visitScope(Scope scope) {
//                    System.out.println(2);
//                    return scope == Scopes.SINGLETON;
//                }
//
//                @Override public Boolean visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
//                    System.out.println(3);
//                    return scopeAnnotation == Singleton.class;
//                }
//
//                @Override public Boolean visitNoScoping() {
//                    System.out.println(4);
//                    return false;
//                }
//            });
//            if(isSingleton) {
//                System.out.println("singleton: " + INJECTOR.getInstance(b.getKey()));
//            }
//        }
//    }
}

