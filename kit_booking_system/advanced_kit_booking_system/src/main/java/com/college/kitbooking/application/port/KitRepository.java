package com.college.kitbooking.application.port;

import com.college.kitbooking.domain.Kit;
import java.util.List;
import java.util.Optional;

public interface KitRepository {
    Optional<Kit> findById(int id);
    List<Kit> findAll();
    Kit save(Kit kit);
}
