package com.nokiaspstraining.serverspool.demo.repositories;

import com.nokiaspstraining.serverspool.demo.models.User;

import org.springframework.data.repository.CrudRepository;

import java.util.*;

public interface UserRepo extends CrudRepository<User,Integer> {


}
