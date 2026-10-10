package com.college.kitbooking.infrastructure;

import com.college.kitbooking.application.port.KitRepository;
import com.college.kitbooking.domain.Kit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKitRepository implements KitRepository {
    private final ConcurrentMap<Integer, Kit> data = new ConcurrentHashMap<>();

    @Override
    public Optional<Kit> findById(int id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Kit> findAll() {
        List<Kit> result = new ArrayList<>(data.values());
        result.sort(Comparator.comparingInt(Kit::id));
        return List.copyOf(result);
    }

    @Override
    public Kit save(Kit kit) {
        data.put(kit.id(), kit);
        return kit;
    }
}
