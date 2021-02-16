package com.nokiaspstraining.serverspool.demo.controllers;


import com.nokiaspstraining.serverspool.demo.models.User;
import com.nokiaspstraining.serverspool.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.awt.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;



    @PostMapping(value = "/add",consumes = {MediaType.APPLICATION_JSON_VALUE},produces = MediaType.APPLICATION_JSON_VALUE)
    public void addUser(@RequestBody  User user){
        userService.addUser(user);

    }

    @GetMapping("/get/{name}")
    public User getUser(@PathVariable("name") String name){

        return userService.getUser(name);

    }


    @DeleteMapping("delete/{id}")
    public void deleteUser (@PathVariable("id") int id){

        userService.deleteUser(id);

    }


    @PutMapping("update/{id}")
    public void updateUser (@PathVariable("id") int id, @RequestBody User user){

        try {
            userService.updateUser(id,user);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


}
