package com.nokiaspstraining.serverspool.demo.services;


import com.nokiaspstraining.serverspool.demo.models.User;
import com.nokiaspstraining.serverspool.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserService {


    @Autowired
    private UserRepo userRepo;


    public void addUser( User user){
        try{
            userRepo.save(user);
        }catch (Exception e){

        }
    }

    public User getUser( String name){
        User user = null;
        Iterable<User> users = userRepo.findAll();
        for(User u:users){
            if(u.getName().equals(name))
                user = u;
        }
        return user;

    }

    @Transactional
    public void deleteUser (int id){

        userRepo.deleteById(id);


    }

    public void updateUser ( int id, User user) throws  Exception{
        User user_2 = userRepo.findById(id).orElseThrow(Exception::new);
        user_2.copy(user);
        if(user_2.getId()!= id) throw new Exception();
        userRepo.save(user_2);


    }

}
