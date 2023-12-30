package com.faas.server_manager;

import com.faas.common.*;
import java.util.Map;
import java.util.Set;

import java.util.Objects;

public class Authenticator {
    private static final AtomicInteger currentAuthUsers = new AtomicInteger(0);
    private static final SynchronizedMap<String,User> users = new SynchronizedMap<>();

    public static int authenticateUser(AuthenticationRequest authReq){
        User user = null;
        if (authReq.getType() == 2) {
            if (!users.containsKey(authReq.getUsername())) {
                user = new User(authReq.getUsername(), authReq.getPassword());
                users.put(user.getUsername(), user);
                System.out.println("User novo:\n" + user.toString());
                currentAuthUsers.increment();
                return currentAuthUsers.get();
            }
        } else if (authReq.getType() == 1) {
            if (users.containsKey(authReq.getUsername()))
                if (Objects.equals(users.get(authReq.getUsername()).getPassword(), authReq.getPassword())) {
                    user = users.get(authReq.getUsername());
                    System.out.println("User existente:\n" + user.toString());
                    currentAuthUsers.increment();
                    return currentAuthUsers.get();
                }
        }
        System.out.println(currentAuthUsers.get());
        return 0;
    }

    public static Set<Map.Entry<String, User>> getUsersEntries(){
        return users.entrySet();
    }
}
