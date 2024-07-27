package com.switix.roomservice.repository;

import com.switix.roomservice.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoomRepository extends MongoRepository<Room, String> {
}
