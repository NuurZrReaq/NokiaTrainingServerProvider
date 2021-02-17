package com.nokiaspstraining.serverspool.demo.services;


import com.nokiaspstraining.serverspool.demo.enums.Enumerations;
import com.nokiaspstraining.serverspool.demo.models.Server;
import com.nokiaspstraining.serverspool.demo.repositories.ServerRepo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ServerService {

    @Autowired
    private ServerRepo serverRepo;


    @Autowired
    private  StateMachineFactory<Enumerations.ServerStatus, Enumerations.ServerEvent> factory;



    public int allocate(int size) {
        List<Server> activeServers = new ArrayList<>();
        Iterable<Server> servers = serverRepo.findAll();
        for (Server server : servers) {
            if (server.getServerStatus().name().equals("ACTIVE")) {
                activeServers.add(server);
            }
        }
        for (Server server : activeServers) {
            if (size <= server.getAvaStorage()) {
                server.setAvaStorage(server.getAvaStorage() - size);
                serverRepo.save(server);
                return server.getKey();
            }
        }
        servers = serverRepo.findAll();
        for (Server server : servers) {
            if (size <= server.getAvaStorage()) {
                if (server.getServerStatus().equals(Enumerations.ServerStatus.CREATING)){
                    Server server1 = null;
                    while(true){
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        server1 = serverRepo.findById(server.getKey()).get();
                        if(server1.getServerStatus().equals(Enumerations.ServerStatus.ACTIVE))
                            break;
                    }
                    server1.setAvaStorage(server1.getAvaStorage() - size);
                    serverRepo.save(server1);
                    return server1.getKey();
                }
                server.setAvaStorage(server.getAvaStorage() - size);
                serverRepo.save(server);
                return server.getKey();
            }
        }

        Server server = create(size);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        activate(server.getKey());
        return server.getKey();
    }

    private void activate(int serverId) {
        StateMachine<Enumerations.ServerStatus,Enumerations.ServerEvent> sm = buildSM(serverId);
        Message<Enumerations.ServerEvent> eventMessage =MessageBuilder.withPayload(Enumerations.ServerEvent.ACTIVATE)
                .setHeader("serverId",serverId)
                .build();
        sm.sendEvent(eventMessage);



    }

    private StateMachine<Enumerations.ServerStatus, Enumerations.ServerEvent> buildSM(int serverId) {
        Server server = serverRepo.findById(serverId).orElseThrow();
        String serverID = String.valueOf(server.getKey());
        StateMachine<Enumerations.ServerStatus,Enumerations.ServerEvent> sm = factory.getStateMachine(serverID);
        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<Enumerations.ServerStatus, Enumerations.ServerEvent>>() {
            @Override
            public void apply(StateMachineAccess<Enumerations.ServerStatus, Enumerations.ServerEvent> sma) {
                sma.addStateMachineInterceptor(new StateMachineInterceptorAdapter<Enumerations.ServerStatus,Enumerations.ServerEvent>(){
                    @Override
                    public void preStateChange(State<Enumerations.ServerStatus, Enumerations.ServerEvent> state, Message<Enumerations.ServerEvent> message, Transition<Enumerations.ServerStatus, Enumerations.ServerEvent> transition, StateMachine<Enumerations.ServerStatus, Enumerations.ServerEvent> stateMachine) {
                        Optional.ofNullable(message).ifPresent(msg->{
                            Optional.ofNullable(Integer.class.cast(msg.getHeaders().getOrDefault("serverId",-1)))
                                    .ifPresent(serverId->{
                                        Server server1 = serverRepo.findById(serverId).orElseThrow();
                                        server1.setServerStatus(state.getId());
                                        serverRepo.save(server1);
                                    });
                        });
                    }
                });

                sma.resetStateMachine(new DefaultStateMachineContext<Enumerations.ServerStatus,Enumerations.ServerEvent>
                        (server.getServerStatus(),null,null,null));
            }
        });
        sm.start();
        return sm;
    }


    public Server create(int size) {

        int id;
        while(true){

            Random random = new Random( );
            id = random.nextInt(1000000);
            Server s=serverRepo.findById(id).orElse(null);
            if(s==null) break;
        }
        Server server = new Server();
        server.setKey(id);
        server.setServerStatus(Enumerations.ServerStatus.CREATING);
        server.setAvaStorage(100 - size);
        return serverRepo.save(server);
    }

    public void deleteById(int id) {
        serverRepo.deleteById(id);
    }
}
